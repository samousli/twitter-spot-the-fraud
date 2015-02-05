/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetAnalytics;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import twitter4j.api.TweetsResources;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.util.JSON;

/**
 *
 * @author avail
 */
public class DBManager {

	private static DB db;

	public DBManager() {
		this("mongodb://localhost:28888");
	}

	public DBManager(String conStr) {

		// create the database
		try {
			// make the initial connection to the mongoDB
			@SuppressWarnings("deprecation")
			Mongo tweetsMongoClient = new Mongo(new MongoURI(conStr));
			db = tweetsMongoClient.getDB("twitter_mini");
		} catch (UnknownHostException ex) {
			System.err
					.println("The database could not be initialized because of an UnknownHostException.");
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
		}
	}

	/*
	 * db.tweets.aggregate([ { $group: {_id : '$user.id', tweet_id : {$push:
	 * '$id'}, count: { $sum: 1 }} }, { $sort: {'count':-1} } ])
	 */
	@SuppressWarnings("deprecation")
	public void groupTweetsByUser() {

		// In order to avoid deleting potentially large amounts of data..again
		if (db.collectionExists("users"))
			throw new RuntimeException(
					"Mongo: Users collection already exists.");

		db.getCollection("users").drop();
		db.getCollection("users").ensureIndex(
				new BasicDBObject("tweets", "text"));

		List<DBObject> pipeline = new ArrayList<>();

		// Group by user id and keep tweet texts and ids
		// Query: { $group: {_id : '$user.id', tweets : {
		// $push: '$text' },
		// count: { $sum: 1 } } };

		DBObject groupByUser = new BasicDBObject("$group", new BasicDBObject(
				"_id", "$user.id").append("tweets",
				new BasicDBObject("$push", "$text")).append("count",
				new BasicDBObject("$sum", 1)));

		// Sort by tweet count
		// DBObject sortByCount = new BasicDBObject("$sort", new BasicDBObject(
		// "count", -1));

		// Filter the users with single a tweet out
		// { $match : { "count" : { $gt : 1 } } }
		DBObject filterUsers = new BasicDBObject("$match", new BasicDBObject(
				"count", new BasicDBObject("$gt", 1)));

		// Output to new table
		DBObject outputToNewCollection = new BasicDBObject("$out", "users");

		pipeline.add(groupByUser);
		// pipeline.add(sortByCount);
		pipeline.add(filterUsers);
		pipeline.add(outputToNewCollection);

		// Disk use is necessary for collections resulting in more than 100mb of
		// data.
		AggregationOptions opts = AggregationOptions.builder()
				.allowDiskUse(true).build();

		db.getCollection("tweets").aggregate(pipeline, opts);
	}

	@SuppressWarnings("deprecation")
	/**
	 * Counts the number of trending topics each user appears in and saves it as a
	 * field in the user collection
	 */
	public void countFrequencyByUser() {

		// Index user collection by freq count in desc order
		db.getCollection("users")
				.ensureIndex(new BasicDBObject("frequency", 1));

		Cursor trends = db.getCollection("trends").find();
		/*
		 * while (trends.hasNext()) { // db.users.update({ // $text : { $search
		 * : "\"<Trend>\"", $language : "none"} }, // { $inc : { frequency: 1 }
		 * }, // { multi : true }); // Note: Escaping hyphens for phrase search
		 * String s = (String) trends.next().get("name"); System.out.println(s);
		 * DBObject query = new BasicDBObject("$text", new BasicDBObject(
		 * "$search", "\"" + s + "\"").append("$language", "none"));
		 * 
		 * DBObject updateOp = new BasicDBObject("$inc", new BasicDBObject(
		 * "frequency", 1));
		 * 
		 * db.getCollection("users").update(query, updateOp, false, true); }
		 */

		// Pure java version, most likely faster
		List<String> topics = new ArrayList<>();
		while (trends.hasNext())
			topics.add((String) trends.next().get("name"));
		trends.close();

		Cursor users = db.getCollection("users").find();
		int i = 0;
		while (users.hasNext()) {
			DBObject user = users.next();
			System.out.println("User " + i++);
			@SuppressWarnings("unchecked")
			List<String> tweets = (List<String>) (List<?>) (BasicDBList) user
					.get("tweets");
			int freq = 0;
			for (String tr : topics) {
				for (String tw : tweets)
					if (tw.toLowerCase().contains(tr.toLowerCase())) {
						++freq;
						break; // If you find a tweet matching the topic,
								// don't check the rest
					}
			}
			// Update users appearance frequency in trends
			db.getCollection("users").update(
					new BasicDBObject("$match", new BasicDBObject("_id",
							user.get("_id"))),
					new BasicDBObject("$set", new BasicDBObject("frequency",
							freq)), false, false);
		}
		users.close();
	}

	@SuppressWarnings("deprecation")
	public int[] calculateQuartiles(String collection) {

		long c = db.getCollection(collection).getCount();
		System.out.println("Document count: " + c);
		long first_halve = c / 4, second_halve = c / 2, third_halve = 3 * c / 4;
		int q1, q2, q3, index = 0;
		db.getCollection(collection).ensureIndex(
				new BasicDBObject("frequency", 1));
		Cursor users = db.getCollection(collection).find()
				.sort(new BasicDBObject("frequency", 1));

		while (index++ < first_halve)
			users.next();
		System.out.println("ch1: " + index);
		q1 = getFreq(users.next());

		while (index++ < second_halve)
			users.next();
		System.out.println("ch2: " + index);
		q2 = getFreq(users.next());

		while (index++ < third_halve)
			users.next();
		System.out.println("ch3: " + index);
		q3 = getFreq(users.next());

		users.close();
		return new int[] { q1, q2, q3 };
	}

	/**
	 * If there is no frequency field (text doesn't belong to any trend(wonders
	 * of twitter API)) return 0
	 * 
	 * @param usr
	 * @return frequency value
	 */
	int getFreq(DBObject usr) {
		if (usr.get("frequency") == null)
			return 0;
		return (int) usr.get("frequency");
	}

	public long[] pickUsersPerQuartile(String inCol, String outCol,
			int[] quartiles, int num_users) {

		long[] user_ids = new long[num_users * 4];
		// If collection exists, fetch users, else create it
		if (db.collectionExists(outCol)) {
			Cursor cursor = db.getCollection(outCol).find();
			int i = 0;
			while (cursor.hasNext()) {
				DBObject e = cursor.next();
				if (e.get("_id") instanceof Long)
					user_ids[i++] = (Long) e.get("_id");
				else if (e.get("_id") instanceof Integer)
					user_ids[i++] = ((Integer) e.get("_id")).longValue();
			}
		} else {

			long c = db.getCollection(inCol).getCount();
			int qr = (int) (0.25 * c);
			Random n = new Random(Calendar.getInstance().getTimeInMillis());
			for (int q = 0; q < 4; q++) {
				for (int i = 0; i < num_users; i++) {

					// System.out.println(q + " " + c + " " + l);
					DBObject e;
					do {
						int l = (int) (q * qr) + n.nextInt(qr);
						e = db.getCollection(inCol).find()
								.sort(new BasicDBObject("frequency", 1))
								.limit(-1).skip(l).next();
					} while (e.get("frequency") == null);
					e.put("quartile", q);
					db.getCollection(outCol).insert(e);
					if (e.get("_id") instanceof Long)
						user_ids[(int) (q * num_users + i)] = (Long) e
								.get("_id");
					else if (e.get("_id") instanceof Integer)
						user_ids[(int) (q * num_users + i)] = ((Integer) e
								.get("_id")).longValue();
				}
			}
		}
		return user_ids;
	}

	public long[] fetchChosenUsers(String col) {
		long[] user_ids = new long[(int) db.getCollection(col).count()];
		Cursor cursor = db.getCollection(col).find();
		int i = 0;
		while (cursor.hasNext()) {
			DBObject e = cursor.next();
			if (e.get("_id") instanceof Long)
				user_ids[i++] = (Long) e.get("_id");
			else if (e.get("_id") instanceof Integer)
				user_ids[i++] = ((Integer) e.get("_id")).longValue();
		}
		return user_ids;
	}

	public DBCollection getTweets() {
		return db.getCollection("tweets");
	}

	public void insertTweet(String json) {
		DBObject ob = (DBObject) JSON.parse(json);
		db.getCollection("chosen_user_tweets").insert(ob);
	}

	public long tweetCount() {
		return db.getCollection("chosen_user_tweets").getCount();
	}

}
