package TweetAnalytics;

import java.util.Calendar;
import java.util.Date;

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
			int numberOfFollowers = u.getFollowersCount();
			int numberOfFriends = u.getFriendsCount(); // followees
			Date creationDate = u.getCreatedAt();// created
			Date currentDate = Calendar.getInstance().getTime(); // current

			// calculate the age of the account
			long time1 = creationDate.getTime();
			long time2 = creationDate.getTime();
			long timeDifference = time2 - time1;
			float daysDifference = timeDifference / (1000 * 60 * 60 * 24);
			int age = (int) daysDifference;
			if (daysDifference % 10 >= 5) {
				age++;
			}

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
