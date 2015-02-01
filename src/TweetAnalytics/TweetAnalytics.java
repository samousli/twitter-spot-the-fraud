package TweetAnalytics;

import java.util.Arrays;

/**
 *
 * @author avail
 */
public class TweetAnalytics {

	public static final DBManager dbm = new DBManager(); // mongoConnectionString

	// public static final String mongoConnectionString =
	// "mongodb://pspi:pspi@ds063240.mongolab.com:63240";

	public static void main(String[] args) {

		dbm.groupTweetsByUser();

		dbm.countAppearanceByUser();

		int[] qr = dbm.calculateQuartiles();
		
		System.out.println("Q1 = " + qr[0] + ", Q2 = " + qr[1] + ", Q3 = " + qr[2]);
	}
}
