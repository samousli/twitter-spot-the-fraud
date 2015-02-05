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

		//dbm.groupTweetsByUser();
		
		// Use to remove the frequency field for all documents
		// 	db.users.update({},{$unset : {frequency: "" }}, {multi: true}) 
		
		// Use to query the documents which don't contain the frequency field
		//	db.users.find({ "frequency" : { $exists : true } })


		//dbm.countFrequencyByUser();

		//int[] qr = dbm.calculateQuartiles("users_backup");
		//System.out.println("Quartiles:");
		//System.out.println("\tQ1 = " + qr[0] + "\n\tQ2 = " + qr[1]
		//		+ "\n\tQ3 = " + qr[2]);

		//System.out.println("Choosing random users");
		
		// Sta grigora gia na treksei se sena xwris db connection
		long[] user_ids = dbm.fetchChosenUsers("chosen_users");
		
		new TwitterUserTracker(user_ids);

	}
}
