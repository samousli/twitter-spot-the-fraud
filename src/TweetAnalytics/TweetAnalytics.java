package TweetAnalytics;

/**
 *
 * @author avail
 */
public class TweetAnalytics {

	public static final DBManager dbm = new DBManager(); // mongoConnectionString

	// public static final String mongoConnectionString =
	// "mongodb://pspi:pspi@ds063240.mongolab.com:63240";

	public static void main(String[] args) {

		// dbm.groupTweetsByUser();

		// dbm.countAppearanceByUser();

		int[] qr = dbm.calculateQuartiles();
		System.out.println("Quartiles:");
		System.out.println("\tQ1 = " + qr[0] + "\n\tQ2 = " + qr[1]
				+ "\n\tQ3 = " + qr[2]);

		System.out.println("Choosing random users");

		long[] user_ids = dbm.pickRandomUsersPerQuartile(qr, 10);
		
		new TwitterUserTracker(user_ids);

	}
}
