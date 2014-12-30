package TweetAnalytics;

import TweetAnalytics.DBManager;

/**
 *
 * @author avail
 */
public class TweetAnalytics {
    
	public static final DBManager dbm = new DBManager(); // mongoConnectionString
	
	public static final String mongoConnectionString = "mongodb://pspi:pspi@ds063240.mongolab.com:63240";

    public static void main(String []args) {
        
    	dbm.groupTweetsByUser();
    }
}
