package TweetAnalytics;

import java.util.Calendar;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.UsersResources;

public class UserDataFetcher {

	private static final Twitter t = new TwitterFactory(
			Utils.TwitterConfBuilder.buildConf()).getInstance();

	String fetchUserData(long userId) {

		UsersResources ur = t.users();
		User u = null;
		try {
			u = ur.showUser(userId);

			// Epipedo A
			u.getFollowersCount();
			u.getFriendsCount(); // followees
			u.getCreatedAt(); // creation
			Calendar.getInstance().getTime(); // current

			// Epipedo B
			u.getStatusesCount();

		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Twitter API stopped while fetching user data.");
		}
		return t.toString();
	}

}
