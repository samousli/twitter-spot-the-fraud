/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import twitter4j.Trend;
import TweetCollector.TrendList.TrendData;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

/**
 *
 * @author george
 */
public class DBManager {

	private static DB db;
	private static DBCollection tweetsCollection;
	private static DBCollection trendsCollection;

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

		// create the tweets collection
		this.tweetsCollection = this.db.getCollection("tweets");

		// create the trends collection
		this.trendsCollection = this.db.getCollection("trends");
	}

	// update( {"name": name}, {"$set" : { "last_active_at": time } }, {"upsert": true});
	// Inserts trend if it doesn't exist in db, else simply updates the post time
	public void insertTrends(Set<Entry<Trend, TrendData>> trends) {
		for (Entry<Trend, TrendData> e : trends) {
			
			BasicDBObject query = new BasicDBObject("name", e.getKey().getName());
			BasicDBObject op = new BasicDBObject("$set", 
					new BasicDBObject( "last_active_at", e.getValue().time.getTime()));
			
			db.getCollection("trends").update(query, op, true, false);
		}
	}

	/*
	 * public void insertTrends(String json) {
	 * 
	 * //parse the json with the current trends DBObject ob = (DBObject)
	 * JSON.parse(json);
	 * 
	 * //get the date of the current trends json file String
	 * dateString=(String)ob.get("as_of");
	 * 
	 * //store the trends in a list BasicDBList trends = (BasicDBList)
	 * ob.get("trends");
	 * 
	 * for(Object trend : trends){ DBObject t = (DBObject) trend;
	 * 
	 * //get the name of the trend String name=(String) t.get("name");
	 * 
	 * BasicDBObject query = new BasicDBObject(); ArrayList<BasicDBObject> obj =
	 * new ArrayList<BasicDBObject>(); obj.add(new BasicDBObject("name", name));
	 * obj.add(new BasicDBObject("off_date", null)); query.put("$and", obj);
	 * DBCursor cursor = trendsCollection.find(query);
	 * 
	 * //check to see if it's a- running hot trend or not if(!cursor.hasNext()){
	 * BasicDBObject newTrend = new BasicDBObject("name",name).append("on_date",
	 * dateString).append("off_date", null); trendsCollection.insert(newTrend);
	 * }
	 * 
	 * }
	 * 
	 * //now check if some trends should be considered as not running from now
	 * on BasicDBObject query = new BasicDBObject(); ArrayList<String> list =
	 * new ArrayList<>(); for(Object trend : trends){ DBObject t = (DBObject)
	 * trend; list.add((String) t.get("name")); } ArrayList<BasicDBObject> obj =
	 * new ArrayList<BasicDBObject>(); obj.add(new BasicDBObject("name", new
	 * BasicDBObject("$nin", list))); obj.add(new BasicDBObject("off_date",
	 * null)); query.put("$and", obj);
	 * 
	 * //for every such document update its off_date to the current date
	 * BasicDBObject newDocument = new BasicDBObject().append("$set", new
	 * BasicDBObject().append("off_date", dateString));
	 * trendsCollection.update(query, newDocument); }
	 */

	public void insertTweet(String json) {
		DBObject ob = (DBObject) JSON.parse(json);

		tweetsCollection.insert(ob);
	}

	public static long tweetCount() {
		return tweetsCollection.getCount();

	}
	
	public DBCollection getTweets(){
		return tweetsCollection;
	}
}
