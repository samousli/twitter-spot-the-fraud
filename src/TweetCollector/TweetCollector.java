/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author avail
 */
public class TweetCollector {
	public static final String mongoConnectionString = "mongodb://pspi:pspi@ds063240.mongolab.com:63240";

	public static final long REPEAT_INTERVAL_IN_SECS = 300;

	public static final DBManager dbm = new DBManager(); // mongoConnectionString

	/**
	 * @param args
	 *            the command line arguments
	 * @throws java.lang.InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		// First arg is the thread name, second arg is whether the thread is a
		// daemon
		// Setting it to false means that the process won't terminate unless
		// the timer is canceled
		Timer fetchTimer = new Timer("TrendFetcher", false);
		// Schedule now and every X milliseconds afterwards
		fetchTimer.scheduleAtFixedRate(new ScheduledTrendFetcher(), new Date(),
				REPEAT_INTERVAL_IN_SECS * 1000);

		
		// Initialize twitter with the custom configuration
		TwitterStream twitterStream = new TwitterStreamFactory(
				Utils.TwitterConfBuilder.buildConf())
				.getInstance();

		twitterStream.addListener(new StreamingTweetListener());

		// Wait till the list has elements, check every 5 seconds
		while (TrendList.getInstance().isEmpty()) {
			TimeUnit.SECONDS.sleep(1);
		}

		// Set the filter query and start tracking the trending topics
		FilterQuery fq = new FilterQuery();
		
		Timer updateTimer = new Timer("TrendTrackerUpdater", false);
		// Schedule now and every X milliseconds afterwards
		updateTimer.scheduleAtFixedRate(new TrendTrackerUpdater(twitterStream, fq),
				new Date(), REPEAT_INTERVAL_IN_SECS * 1000);
		
		
		// Wait for console input
		System.out.println("type 'exit' or 'q' to exit.");
		System.out.println("type 'count' or 'c' to check the current tweet count.");
		Scanner reader = new Scanner(System.in);
		String s = reader.nextLine().trim().toLowerCase();
		while(!s.equals("exit") && !s.equals("q")) {
			s = reader.nextLine().trim().toLowerCase();
			if (s.equals("count") || s.equals("c")) {
				System.out.println("Count:\t" + DBManager.tweetCount());
			}
		}
		reader.close();
		
		fetchTimer.cancel();
		updateTimer.cancel();
		twitterStream.shutdown();
	}

}

class TrendTrackerUpdater extends TimerTask {
	private TwitterStream twitterStream;
	private FilterQuery fq;

	TrendTrackerUpdater(TwitterStream twitterStream, FilterQuery fq) {
		this.twitterStream = twitterStream;
		this.fq = fq;
	}

	@Override
	public void run() {
		// commas can be thought of as logical ORs,
		// while spaces are equivalent to logical ANDs
		// (e.g. ‘the twitter’ equals (the AND twitter),
		// and ‘the,twitter’ equals (the OR twitter).
		fq.track(TrendList.getInstance().getNewTrendTracker());
		twitterStream.filter(fq);
	}

}
