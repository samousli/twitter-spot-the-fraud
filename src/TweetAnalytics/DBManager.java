/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetAnalytics;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.AggregationOptions;
import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONSerializers;

/**
 *
 * @author avail
 */
public class DBManager {

	DB db;
	DBCollection tweetsCollection;

	public DBManager() {
		this("mongodb://localhost:28888");
	}

	public DBManager(String conStr) {

		// create the database
		try {
			// make the initial connection to the mongoDB
			Mongo tweetsMongoClient = new Mongo(new MongoURI(conStr));
			this.db = tweetsMongoClient.getDB("twitter_mini");
		} catch (UnknownHostException ex) {
			System.err.println("The database could not be initialized because of an UnknownHostException.");
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		
		this.tweetsCollection = this.db.getCollection("tweets");

	}

	public void groupTweetsByUser() {
		
		List<DBObject> pipeline = new ArrayList<>();
		
		// Group by user id
		DBObject groupByUser = new BasicDBObject("$group", 
				new BasicDBObject("_id", "$user.id")
					.append("tweets", 
						new BasicDBObject("$push", "$$ROOT"))
					.append("size", 
						new BasicDBObject("$sum", 1)));
		
		// Sort by tweet count
		DBObject sortByTweets  = new BasicDBObject("$sort", 
				new BasicDBObject("size", -1) );
	
		// Output to new table
		DBObject outputToNewCollection = new BasicDBObject("$out", "by_user");
		
		db.command("db.tweets.aggregate([{ $group : { _id : \"$user.id\" } } ] );");
		
		pipeline.add(groupByUser);
		pipeline.add(sortByTweets);
		
		// Used for debugging, in production a single pipeline is sought after
		pipeline.add(outputToNewCollection);
		
		AggregationOptions opts = AggregationOptions.builder().allowDiskUse(true).build();
		
		Cursor e = this.tweetsCollection.aggregate(pipeline, opts);
		// while (e.hasNext()) System.out.println(e.next());

		System.out.println(this.tweetsCollection.count());
	}
}
