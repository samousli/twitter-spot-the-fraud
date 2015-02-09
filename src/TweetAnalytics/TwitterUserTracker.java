package TweetAnalytics;

import java.util.Scanner;

import TweetCollector.DBManager;
import TweetCollector.TweetCollector;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

public class TwitterUserTracker {

	TwitterStream twitterStream = new TwitterStreamFactory(
			Utils.TwitterConfBuilder.buildConf()).getInstance();

	private String col_name;

	private Scanner reader;

	public TwitterUserTracker(String out_col, long[] user_ids) {
		this.col_name = out_col;

		twitterStream.addListener(new UserFeedListener());
		FilterQuery fq = new FilterQuery(user_ids);
		twitterStream.filter(fq);

		// Wait for console input
		System.out.println("type 'exit' or 'q' to exit.");
		System.out
				.println("type 'count' or 'c' to check the current tweet count.");
		reader = new Scanner(System.in);
		String s = reader.nextLine().trim().toLowerCase();
		while (!s.equals("exit") && !s.equals("q")) {
			s = reader.nextLine().trim().toLowerCase();
			if (s.equals("count") || s.equals("c")) {
				System.out
						.println("Count:\t" + TweetAnalytics.dbm.tweetCount());
			}
		}
	}

	class UserFeedListener implements StatusListener {

		@Override
		public void onStatus(Status status) {
			// TODO Auto-generated method stub
			String json = TwitterObjectFactory.getRawJSON(status);
			TweetAnalytics.dbm.insertDoc(col_name, json);
		}

		@Override
		public void onTrackLimitationNotice(int i) {
			System.out.println(i + " tweets missed due to track limitation.");
		}

		@Override
		public void onStallWarning(StallWarning sw) {
			System.out.println(sw);
		}

		@Override
		public void onDeletionNotice(StatusDeletionNotice sdn) {
		}

		@Override
		public void onScrubGeo(long l, long l1) {
		}

		@Override
		public void onException(Exception arg0) {
			// TODO Auto-generated method stub
		}

	}
}
