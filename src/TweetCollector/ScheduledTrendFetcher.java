/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import java.util.TimerTask;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.TwitterAdapter;

/**
 * Uses the Async Twitter API to get the current worldwide trending topics every time run() is called
 * @author avail
 */
public class ScheduledTrendFetcher extends TimerTask {

    private boolean initialized = false;
    private AsyncTwitter twitter;
    
    private void initialize() {
     
        twitter = new AsyncTwitterFactory(Utils.TwitterConfBuilder.buildConf()).getInstance();
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

class AsyncTrendListener extends TwitterAdapter {

    @Override
    public void gotPlaceTrends(Trends trends) {
        System.out.println("Fetched the new trends..");
        for (Trend t : trends.getTrends()) {
            //System.out.println("TREND: " + t);
            TrendList.getInstance().addTrend(t);

        }
    }
}
