package TweetAnalytics;

import java.util.Calendar;
import java.util.Date;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.api.UsersResources;

public class UserDataFetcher {

	private static final Twitter t = new TwitterFactory(
			Utils.TwitterConfBuilder.buildConf()).getInstance();

	String fetchBasicUserData(long user_id) {

		UsersResources ur = t.users();
		User u = null;
		try {
			u = ur.showUser(user_id);

			// Epipedo A
			int numberOfFollowers = u.getFollowersCount();
			int numberOfFriends = u.getFriendsCount(); // followees
			Date creationDate = u.getCreatedAt();// created
			Date currentDate = Calendar.getInstance().getTime(); // current
			int age = getAccountAge(user_id);
			
			TweetAnalytics.dbm.getCollection("users").update(new BasicDBObject("$match", 
					new BasicDBObject("_id", user_id)), 
					new BasicDBObject("$set", 
							new BasicDBObject("friends", numberOfFriends)
							.append("followers", numberOfFollowers)
							.append("follower_friend_ratio", (numberOfFollowers + 0.0000001) / numberOfFriends)
							.append("age", age)));
			System.out.println("Fetched data for:\t" + u.getName());

		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Twitter API stopped while fetching user data.");
		}
		return t.toString();
	}
	// PartB, better name?
	String fetchAnalyticalData(long user_id) {
		// Epipedo B
	    //u.getStatusesCount();
		
		return null;
	}
	
	public int getNumberOfFollowers(long userId){
		UsersResources ur = t.users();
		User u = null;
		try {
			u = ur.showUser(userId);
			return u.getFollowersCount();

		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Twitter API stopped while fetching user data.");
		}
	}
	
	public int getNumberOfFriends(long userId){
		UsersResources ur = t.users();
		User u = null;
		try {
			u = ur.showUser(userId);
			return u.getFriendsCount();

		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Twitter API stopped while fetching user data.");
		}
	}
	
	public int getAccountAge(long userId){
		UsersResources ur = t.users();
		User u = null;
		try {
			u = ur.showUser(userId);
			
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
			return age;

		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Twitter API stopped while fetching user data.");
		}
	}
	
	public int getStatusesCount(long userId){
		UsersResources ur = t.users();
		User u = null;
		try {
			u = ur.showUser(userId);
			
			return u.getStatusesCount();

		} catch (TwitterException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Twitter API stopped while fetching user data.");
		}
	}

}
