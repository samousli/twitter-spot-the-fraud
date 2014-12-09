/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pspi_twitter;

import java.util.Date;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author george
 */
public class PSPI_Twitter {

    public static final String requestTokenString = "ER8jr24e1yXP0zU07a6ath0cb";
    public static final String requestSecretString = "ASySBOVsFZlm7GummsRBD2YbBoRAmvgRhOLWtKVfxTP3E0SS4g";

    public static final String accessTokenString = "2843777541-TMnA2qa58IYANJmcRXLvrtk8Mp75ybGn50Avi3n";
    public static final String accessSecretString = "shsIsXdkDlptw2yTL5vvOGVMXZMJzmsWXF2D0VMolJ7kj";
    
    public static String connectionString = ""; //TODO: I dont know actually :P

    public static final long REPEAT_INTERVAL_IN_SECS = 300;
    

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        // First arg is the thread name, second arg is whether the thread is a daemon
        // Setting it to false means that the process won't terminate unless 
        // the timer is canceled
        Timer t = new Timer("TrendGetter", false);
        // Schedule now and every X milliseconds afterwards
        t.scheduleAtFixedRate(new TrendGetter(), new Date(),
                REPEAT_INTERVAL_IN_SECS * 1000);
        
        //create the database manager
        DBManager dbm=new DBManager();
        
        // Initialize the streaming API
        
        // Configure to accept JSON files
        ConfigurationBuilder conf = new ConfigurationBuilder();
        conf.setJSONStoreEnabled(true);
        // Initialize twitter with the custom configuration
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        // Set tokens
        twitterStream.setOAuthConsumer(requestTokenString, requestSecretString);
        twitterStream.setOAuthAccessToken(new AccessToken(accessTokenString, accessSecretString));
        
        twitterStream.addListener(new TweetStreamListener());
        
        // Wait till the list has elements, check every 5 seconds
        while(TrendList.getInstance().isEmpty()) {
            TimeUnit.SECONDS.sleep(5);
        }
        
        // Set the filter query and start tracking the trending topics
        FilterQuery fq = new FilterQuery();
        //You can think of commas as logical ORs, 
        // while spaces are equivalent to logical ANDs 
        // (e.g. ‘the twitter’ is the AND twitter, 
        //  and ‘the,twitter’ is the OR twitter).
        String[] tracked = TrendList.getInstance().getNewTrendTracker();
        for (String c : tracked) {
            System.out.println("Query:" + c);
        }
        
        fq.track(tracked);
        twitterStream.filter(fq);
        
        // Stream loop
        // Every 5 minutes update the 
        while(!TrendList.getInstance().isEmpty()) {
            TimeUnit.MINUTES.sleep(5);
            fq.track(TrendList.getInstance().getNewTrendTracker());
            twitterStream.filter(fq);
        }
        
    }

}
