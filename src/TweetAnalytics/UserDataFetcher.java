package TweetAnalytics;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.UsersResources;
import twitter4j.conf.ConfigurationBuilder;

public class UserDataFetcher {
	
	
	private static final Twitter t = new TwitterFactory(
			Utils.TwitterConfBuilder.buildConf()).getInstance();
	
	String fetchUserData(long userId) {
		
		UsersResources ur = t.users();
		User t = null;
		try {
			t = ur.showUser(userId);
		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException("Twitter API stopped while fetching user data.");
		}		
		return t.toString();
	}
	
}
