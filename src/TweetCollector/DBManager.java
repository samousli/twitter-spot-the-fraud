/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.util.JSON;

/**
 *
 * @author george
 */
public class DBManager {

	private DB db;
	private DBCollection tweetsCollection;
	private DBCollection trendsCollection;

	public DBManager() {
		this("mongodb://localhost:28888");
	}

	public DBManager(String conStr) {

		// create the database
		try {
			// make the initial connection to the mongoDB
			Mongo tweetsMongoClient = new Mongo(new MongoURI(conStr));
			this.db = tweetsMongoClient.getDB("twitter");
		} catch (UnknownHostException ex) {
			System.err.println("The database could not be initialized because of an UnknownHostException.");
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
		}

		// create the tweets collection
		this.tweetsCollection = this.db.getCollection("tweets");

		// create the trends collection
		this.trendsCollection = this.db.getCollection("trends");
	}

	public void insertTrend(String json) {
		
		DBObject ob = (DBObject) JSON.parse(json);
		this.trendsCollection.insert(ob);
	}
	
	
	public void insertTweet(String json) {
		DBObject ob = (DBObject) JSON.parse(json);
		
		this.tweetsCollection.insert(ob);
	}
}
