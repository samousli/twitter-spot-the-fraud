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
import com.mongodb.DBCursor;
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
			System.err
					.println("The database could not be initialized because of an UnknownHostException.");
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		this.tweetsCollection = this.db.getCollection("tweets");

	}

	/*
	 * db.tweets.aggregate([ { $group: {_id : '$user.id', tweet_id : {$push:
	 * '$id'}, count: { $sum: 1 }} }, { $sort: {'count':-1} } ])
	 */
	public void groupTweetsByUser() {

		List<DBObject> pipeline = new ArrayList<>();

		// Group by user id
		// Query: { $group: {_id : '$user.id', tweet_ids : {$push: '$id'},
		// count: { $sum: 1 }} }
		DBObject groupByUser = new BasicDBObject("$group", new BasicDBObject(
				"_id", "$user.id").append("tweet_ids",
				new BasicDBObject("$push", "$id")).append("count",
				new BasicDBObject("$sum", 1)));

		// Sort by tweet count
		DBObject sortByCount = new BasicDBObject("$sort", new BasicDBObject(
				"count", -1));

		// Output to new table
		DBObject outputToNewCollection = new BasicDBObject("$out", "users");

		// db.command("db.tweets.aggregate([{ $group : { _id : \"$user.id\" } } ] );");

		pipeline.add(groupByUser);
		pipeline.add(sortByCount);
		pipeline.add(outputToNewCollection);

		// Disk use is required for collections resulting in above 100mb of data
		// at the group stage.
		AggregationOptions opts = AggregationOptions.builder()
				.allowDiskUse(true).build();

		Cursor e = this.tweetsCollection.aggregate(pipeline, opts);
		// while (e.hasNext()) System.out.println(e.next());

		// db.users.find({ $query: {}, $orderby: { count : -1 } })//.limit(100)
		DBCursor c = db.getCollection("users").find()
				.addSpecial("$orderby", new BasicDBObject("count", -1));
		
		
		long user_count = c.count(), i = 0;
		while (c.hasNext()) {
			System.out.println(c.next());
		}//*/

	}

	public DBCollection getTweets() {
		return this.tweetsCollection;
	}

}
