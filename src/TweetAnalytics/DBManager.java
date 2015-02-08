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

	@SuppressWarnings("deprecation")
	public void initUserCollection() {
		if (db.collectionExists("users"))
			throw new RuntimeException(
					"Mongo: Users collection already exists.");

		db.getCollection("users").drop();
		db.getCollection("users").ensureIndex(
				new BasicDBObject("tweets", "text"));
	}

	public DBCollection getCollection(String collection) {
		return db.getCollection(collection);
	}

	public boolean exists(String collection) {
		return db.collectionExists(collection);
	}

}
