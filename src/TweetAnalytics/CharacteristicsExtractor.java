package TweetAnalytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Utils.Helpers;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CharacteristicsExtractor {
	private CharacteristicsDB cdb;
	private DBManager tdbm;
	private DBCollection tweets;
	Pattern urlPattern;

	public CharacteristicsExtractor(CharacteristicsDB cdb, DBManager dbm) {
		this.cdb = cdb;
		this.tdbm = dbm;
		// this.tweets = dbm.getTweets();
		this.urlPattern = Pattern
				.compile("<\\b(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>");
	}

	/**
	 *
	 * @param tweets
	 *            list of all the tweets a user has posted
	 * @return returns the number of the tweets that should be considered as
	 *         "multiple"
	 */
	private int numberOfSameTweets(ArrayList<DBObject> tweets) {

		ArrayList<String> tweetStrings = new ArrayList<>();
		for (DBObject dbo : tweets) {
			tweetStrings.add((String) dbo.get("text"));
		}

		int result = 0;
		ArrayList<String> filteredTweets = new ArrayList<>();

		// filter the tweets
		for (int i = 0; i < tweetStrings.size(); i++) {
			filteredTweets.add(this.filterTweet(tweetStrings.get(i)));
		}

		// count the multiple tweets
		for (int i = 0; i < filteredTweets.size() - 1; i++) {
			for (int j = i + 1; j < filteredTweets.size(); j++) {
				String tweet1 = filteredTweets.get(i);
				String tweet2 = filteredTweets.get(j);

				// compute the levenshtein distance
				int ld = computeLevenshteinDistance(tweet1, tweet2);
				int maxLD = Math.max(tweet1.length(), tweet2.length());
				float distance = (float) ld / maxLD;

				// if the tweet is considered as multiple increase the counter
				if (distance < 0.1) {
					result++;
				}
			}
		}

		return result;
	}

	private int numOfOthersRetweets(ArrayList<DBObject> tweets) {
		int count = 0;
		for (DBObject tweet : tweets) {
			count += (int) tweet.get("retweet_count");
		}

		return count;
	}

	private int numOfTweetsContainingHashtags(ArrayList<DBObject> tweets) {
		int count = 0;
		for (DBObject tweet : tweets) {
			DBObject entities = (DBObject) tweet.get("entities");
			BasicDBList h = (BasicDBList) entities.get("hashtags");
			if (!h.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private int numOfHashtags(ArrayList<DBObject> tweets) {
		int sum = 0;
		for (DBObject tweet : tweets) {
			DBObject entities = (DBObject) tweet.get("entities");
			BasicDBList h = (BasicDBList) entities.get("hashtags");
			sum += h.size();
		}
		return sum;
	}

	private int numOfTweetsContainingUrls(ArrayList<DBObject> tweets) {
		int count = 0;
		for (DBObject tweet : tweets) {
			DBObject dbo = (DBObject) tweet.get("entities");
			BasicDBList dburl = (BasicDBList) dbo.get("urls");
			if (!dburl.isEmpty()) {
				count++;
			}
		}
		return count;
	}

	private int numOfMentions(ArrayList<DBObject> tweets) {
		int count = 0;
		for (DBObject tweet : tweets) {
			DBObject et = (DBObject) tweet.get("entities");
			BasicDBList t = (BasicDBList) et.get("user_mentions");
			if (t.size() > 0) {
				count++;
			}
		}
		return count;
	}

	private int numOfRetweets(ArrayList<DBObject> tweets) {
		int count = 0;
		for (DBObject tweet : tweets) {
			if ((boolean) tweet.get("retweeted"))
				count++;
		}
		return count;
	}

	/**
	 * 
	 * @param tweet
	 *            an unfiltered tweet
	 * @return the filtered tweet
	 */
	private String filterTweet(String tweet) {
		String[] tok = tweet.split(" ");

		ArrayList<String> tokens = new ArrayList<>();
		for (String token : tok) {
			tokens.add(token);
		}

		Iterator<String> it = tokens.iterator();

		// iterate through the tokens and filter the unwanted ones
		while (it.hasNext()) {
			String token = it.next();
			if (token.startsWith("@") || this.isURL(token)) {
				it.remove();
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) {
			sb.append(token);
			sb.append(" ");
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		String string = sb.toString().trim();

		return string;
	}

	private HashSet<String> UniqueUrls(ArrayList<DBObject> tweets) {
		HashSet<String> set = new HashSet<>();

		for (DBObject tweet : tweets) {
			DBObject dbo = (DBObject) tweet.get("entities");
			BasicDBList dburl = (BasicDBList) dbo.get("urls");
			for (int i = 0; i < dburl.size(); ++i) {
				DBObject url = (DBObject) dburl.get(i);
				String eurl = (String) url.get("expanded_url");
				set.add(eurl);
			}
		}

		return set;
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	private static int computeLevenshteinDistance(String str1, String str2) {
		int[][] distance = new int[str1.length() + 1][str2.length() + 1];

		for (int i = 0; i <= str1.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= str2.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= str1.length(); i++)
			for (int j = 1; j <= str2.length(); j++)
				distance[i][j] = minimum(
						distance[i - 1][j] + 1,
						distance[i][j - 1] + 1,
						distance[i - 1][j - 1]
								+ ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
										: 1));

		return distance[str1.length()][str2.length()];
	}

	private String findMostUsedSource(ArrayList<DBObject> tweets) {
		HashMap<String, Integer> map = new HashMap<>();

		for (int i = 0; i < tweets.size(); i++) {
			DBObject tweet = tweets.get(i);

			// find the source of the tweet
			String source = tweet.get("source").toString();

			Integer appearances = map.get(source);
			if (appearances != null) {
				map.put(source, appearances + 1);
			} else {
				map.put(source, 1);
			}
		}

		// find most used source
		Set<String> keys = map.keySet();
		int max = 0;
		String maxSource = null;
		for (String key : keys) {
			if (map.get(key) > max) {
				max = map.get(key);
				maxSource = key;
			}
		}
		// Removing html tags 
		return maxSource.replaceAll("\\<.*?\\>", "");
	}

	private boolean isURL(String urlStr) {
		/*
		 * try { URI uri = new URI(urlStr); return true; } catch
		 * (URISyntaxException e) { return false; }
		 */

		Matcher matcher = this.urlPattern.matcher(urlStr);
		return matcher.matches();
	}

	private int numOfUniqueDomains(HashSet<String> urls) {
		HashSet<String> domains = new HashSet<>();
		for (String url : urls) {
			String trimmed = url.replaceAll("(https?)://", "").replace("www.",
					"");
			domains.add(trimmed.substring(0, trimmed.indexOf(".")));
		}
		return domains.size();
	}

	public void extract(String tweet_col_name, long user_id) {
		// TODO: get all the tweets from user with id = userID
		// create the tweets arraylist

		Cursor c = tdbm.getCollection(tweet_col_name).find(
				new BasicDBObject("user.id", user_id));
		ArrayList<DBObject> tweets = new ArrayList<>();
		while (c.hasNext())
			tweets.add(c.next());
		
		if (tweets.isEmpty())
			return;
			//throw new RuntimeException("Bye bye cruel world!");
		
		DBObject user = (DBObject) tweets.get(0).get("user");

		// extract the characteristics
		int numberOfFriends = (int) user.get("friends_count");
		int numberOfFollowers = (int) user.get("followers_count");
		float ffRatio = (float) numberOfFollowers / numberOfFriends;
		int accountAge = Helpers.getAccountAge((String) user
				.get("created_at"));
		int numberOfTweets = tweets.size();
		int numberOfRetweets = this.numOfRetweets(tweets);// ??
		int numberOfReplies = 0;// ***
		int numberOfMentions = this.numOfMentions(tweets);
		int numberOfOthersRetweets = this.numOfOthersRetweets(tweets);
		int numberOfHashtags = this.numOfHashtags(tweets);
		int numberOfHashtagTweets = this.numOfTweetsContainingHashtags(tweets);
		int numberOfUrlTweets = this.numOfTweetsContainingUrls(tweets);
		int numberOfCopies = this.numberOfSameTweets(tweets);
		String mostUsedSource = this.findMostUsedSource(tweets);
		HashSet<String> urls = this.UniqueUrls(tweets);
		int numberOfUniqueUrls = urls.size();
		int numberOfUniqueDomains = numOfUniqueDomains(urls);

		// insert them in the database
		this.cdb.insertSelectedUser(user_id, numberOfFollowers,
				numberOfFriends, accountAge, numberOfTweets, numberOfRetweets,
				numberOfReplies, numberOfMentions, numberOfOthersRetweets,
				numberOfHashtags, numberOfHashtagTweets, numberOfUrlTweets,
				numberOfCopies, mostUsedSource, numberOfUniqueUrls,
				numberOfUniqueDomains);

	}

}
