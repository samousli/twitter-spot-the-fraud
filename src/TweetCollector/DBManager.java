/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author george
 */
public class DBManager {

    DB db;
    DBCollection tweetsCollection;
    DBCollection trendsCollection;

    public DBManager() {

        //create the database
        try {
            //make the initial connection to the mongoDB
            MongoClient tweetsMongoClient = new MongoClient("localhost");
            this.db = tweetsMongoClient.getDB("DB");
        } catch (UnknownHostException ex) {
            System.out.println("The database could not be initialized because of an UnknownHostException.");
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        //create the tweets collection
        this.tweetsCollection = this.db.getCollection("tweetsCollection");

        //create the trends collection
        this.tweetsCollection = this.db.getCollection("trendsCollection");
    }
    
    public void insertTrend(BasicDBObject trendObject){
        this.trendsCollection.insert(trendObject);
    }
    
    public void insertTweet(BasicDBObject tweetObject){
        this.tweetsCollection.insert(tweetObject);
    }
}
