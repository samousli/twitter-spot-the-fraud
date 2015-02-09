package TweetAnalytics;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;

public class CharacteristicsDB {

	DB db;
	DBCollection allUsersCollection;// collection with the characteristics of
									// all the users
	DBCollection selectedUsersCollection;// collection with the selected users'
											// characteristics

	public CharacteristicsDB() {
		this("mongodb://localhost:28888");
	}

	public CharacteristicsDB(String conStr) {

		// create the database
		try {
			// make the initial connection to the mongoDB
			Mongo tweetsMongoClient = new Mongo(new MongoURI(conStr));
			this.db = tweetsMongoClient.getDB("twitter_mini");
		} catch (UnknownHostException ex) {
			System.err
					.println("The database could not be initialized because of an UnknownHostException.");
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		this.allUsersCollection = this.db.getCollection("allUsers");
		this.selectedUsersCollection = this.db.getCollection("selectedUsers");

	}

	//
	public void insertSimpleUser(long id, int numberOfFollowers,
			int numberOfFriends, int accountAge) {
		BasicDBObject characteristics = new BasicDBObject();
		characteristics.put("user_id", id);
		characteristics.put("number_of_followers", numberOfFollowers);
		characteristics.put("number_of_friends", numberOfFriends);
		characteristics.put("fraction", (float) numberOfFollowers
				/ numberOfFriends);
		characteristics.put("account_age", accountAge);

		// insert the characteristics in the collection
		this.allUsersCollection.insert(characteristics);
	}

	public void insertSelectedUser(long id, int numberOfFollowers,
			int numberOfFriends, int accountAge, int numberOfTweets,
			int numberOfRetweets, int numberOfReplies, int numberOfMentions,
			int numberOfOthersRetweets, int numberOfHashtags,
			int numberOfHashtagTweets, int numberOfUrlTweets,
			int multipleTweets, String mostUsedSource, int numberOfUniqueUrls,
			int numberOfUniqueDomains) {
		BasicDBObject characteristics = new BasicDBObject();
		BasicDBObject simpleNumbers = new BasicDBObject();
		BasicDBObject uniqueCharacteristics = new BasicDBObject();

		// put the simple number characteristics
		characteristics.put("user_id", id);
		simpleNumbers.put("number_of_followers", numberOfFollowers);
		simpleNumbers.put("number_of_friends", numberOfFriends);
		simpleNumbers.put("ff_ratio", (float) numberOfFollowers
				/ numberOfFriends);
		simpleNumbers.put("account_age", accountAge);
		simpleNumbers.put("number_of_tweets", numberOfTweets);
		simpleNumbers.put("number_of_retweets", numberOfRetweets);
		simpleNumbers.put("number_of_replies", numberOfReplies);
		simpleNumbers.put("number_of_mentions", numberOfMentions);
		simpleNumbers.put("number_of_others_retweets", numberOfOthersRetweets);
		simpleNumbers.put("average_retweets_per_tweet",
				(float) numberOfOthersRetweets / numberOfTweets);
		simpleNumbers.put("average_hashtags_per_tweet",
				(float) numberOfHashtags / numberOfTweets);
		simpleNumbers.put("hashtag_tweets_percentage",
				((float) numberOfHashtagTweets / numberOfTweets) * 100);
		simpleNumbers.put("url_tweets_percentage",
				((float) numberOfUrlTweets / numberOfTweets) * 100);
		characteristics.put("simple_numbers", simpleNumbers);

		// put the rest of the characteristics
		characteristics.put("multiple_tweets", multipleTweets);
		characteristics.put("most_used_source", mostUsedSource);

		// put the "unique" characteristics
		uniqueCharacteristics.put("number_of_unique_urls", numberOfUniqueUrls);
		uniqueCharacteristics.put("number_of_unique_domains",
				numberOfUniqueDomains);

		characteristics.put("unique_characteristics", uniqueCharacteristics);

		// insert the characteristics in the collection
		this.selectedUsersCollection.insert(characteristics);
	}

}
