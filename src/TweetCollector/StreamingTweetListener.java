/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package TweetCollector;

import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterObjectFactory;

/**
 *
 * @author avail
 */
public class StreamingTweetListener implements StatusListener {

	@Override
	public void onStatus(Status status) {
		// System.out.println(status.getUser().getName() + " : " +
		// status.getText());
		String json = TwitterObjectFactory.getRawJSON(status);
		TweetCollector.dbm.insertTweet(json);
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
	public void onTrackLimitationNotice(int i) {
		System.out.println(i + " tweets missed due to track limitation.");
	}

	@Override
	public void onException(Exception excptn) {
	}
}
