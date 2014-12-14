/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import static TweetCollector.TweetCollector.accessSecretString;
import static TweetCollector.TweetCollector.accessTokenString;
import static TweetCollector.TweetCollector.requestSecretString;
import static TweetCollector.TweetCollector.requestTokenString;

import java.util.TimerTask;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Uses the Async Twitter API to get the current worldwide trending topics every time run() is called
 * @author avail
 */
public class ScheduledTrendFetcher extends TimerTask {

    private boolean initialized = false;
    private AsyncTwitter twitter;
    
    private void initialize() {
        
    	// 
        twitter = new AsyncTwitterFactory().getInstance();
        
        // Authenticate using the credentials (Tokens don't time out)
        twitter.setOAuthConsumer(requestTokenString, requestSecretString);
        twitter.setOAuthAccessToken(new AccessToken(accessTokenString, accessSecretString) );
        
        
        twitter.addListener(new AsyncTrendListener());
        
        initialized = true;
    }
    
    @Override
    public void run() {
        if (!initialized)
            initialize();
        System.out.println("Requesting new trends");
        twitter.getPlaceTrends(1);
        
    }
}
