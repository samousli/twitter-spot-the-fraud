package TweetAnalytics;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class CharacteristicsExtractor {
	private CharacteristicsDB cdb;
	private DBManager tdbm;
	private DBCollection tweets;

	public CharacteristicsExtractor(CharacteristicsDB cdb, DBManager dbm) {
		this.cdb = cdb;
		this.tdbm = dbm;
		this.tweets = dbm.getTweets();
	}

	/**
	 *
	 * @param tweets
	 *            list of all the tweets a user has posted
	 * @returnthe number of the tweets that should be considered as "multiple"
	 */
	public int numberOfSameTweets(ArrayList<String> tweets) {
		int result = 0;
		ArrayList<String> filteredTweets = new ArrayList<>();

		// filter the tweets
		for (int i = 0; i < tweets.size(); i++) {
			filteredTweets.add(this.filterTweet(tweets.get(i)));
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
			if ((token.charAt(0) == '@') || (this.isValidURI(token))) {
				it.remove();
			}
		}
		return null;
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
			String source = null;
			// TODO:find the source

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

		return maxSource;
	}

	private boolean isValidURI(String uriStr) {
		try {
			URI uri = new URI(uriStr);
			return true;
		} catch (URISyntaxException e) {
			return false;
		}
	}
}
