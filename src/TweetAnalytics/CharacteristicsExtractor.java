package TweetAnalytics;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class CharacteristicsExtractor {
	private CharacteristicsDB cdb;
	private DBManager tdbm;
	private DBCollection tweets;
	Pattern urlPattern;
	
	public CharacteristicsExtractor(CharacteristicsDB cdb, DBManager dbm){
		this.cdb = cdb;
		this.tdbm=dbm;
		this.tweets = dbm.getTweets();
		this.urlPattern = Pattern.compile("<\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>");
	}
	
	/**
	 *
	 * @param tweets list of all the tweets a user has posted
	 * @return returns the number of the tweets that should be considered as "multiple"
	 */
	public int numberOfSameTweets(ArrayList<DBObject> tweets){
		
		ArrayList<String> tweetStrings = new ArrayList<>();
		for(DBObject dbo:tweets){
			tweetStrings.add((String)dbo.get("text"));
		}
		
		int result = 0;
		ArrayList<String> filteredTweets = new ArrayList<>();
		
		//filter the tweets
		for(int i=0; i < tweetStrings.size() ; i++){
			filteredTweets.add(this.filterTweet(tweetStrings.get(i)));
		}
		
		//count the multiple tweets
		for(int i=0;i<filteredTweets.size()-1;i++){
			for (int j=i+1;j<filteredTweets.size();j++){
				String tweet1 = filteredTweets.get(i);
				String tweet2 = filteredTweets.get(j);
				
				//compute the levenshtein distance
				int ld = computeLevenshteinDistance(tweet1, tweet2);
				int maxLD = Math.max(tweet1.length(), tweet2.length());
				float distance = (float)ld/maxLD;
				
				//if the tweet is considered as multiple increase the counter
				if(distance < 0.1){
					result++;
				}
			}
		}
		
		return result;
	}
	
	private int numOfOthersRetweets(ArrayList<DBObject> tweets){
		int count = 0;
		for(DBObject tweet:tweets){
			count += (int)tweet.get("retweet_count");
		}
		
		return count;
	}
	
	private int numOfTweetsContainingHashtags(ArrayList<DBObject> tweets){
		int count = 0;
		for(DBObject tweet:tweets){
			String[] ht = (String[])tweet.get("hashtags");
			if(ht.length>0){
				count++;
			}
		}
		return count;
	}
	
	private int numOfHashtags(ArrayList<DBObject> tweets){
		int sum=0;
		for(DBObject tweet:tweets){
			String[] h = (String[])tweet.get("hashtags");
			sum += h.length;
		}
		return sum;
	}
	
	private int numOfTweetsContainingUrls(ArrayList<DBObject> tweets){
		int count = 0;
		for(DBObject tweet:tweets){
			String[] ut = (String[])tweet.get("urls");
			if(ut.length>0){
				count++;
			}
		}
		return count;
	}
	
	private int numOfMentions(ArrayList<DBObject> tweets){
		int count = 0;
		for(DBObject tweet:tweets){
			DBObject[] t = (DBObject[])tweet.get("user_mentions");
			if(t.length>0){
				count++;
			}
		}
		return count;
	}
	
	private int numOfRetweets(ArrayList<DBObject> tweets){
		int count = 0;
		for(DBObject tweet:tweets){
			if((boolean)tweet.get("retweeted")) count++;
		}
		return count;
	}
	
	/**
	 * 
	 * @param tweet an unfiltered tweet
	 * @return the filtered tweet
	 */
	private String filterTweet(String tweet){
		String[] tok = tweet.split(" ");
		
		ArrayList<String> tokens = new ArrayList<>();
		for(String token:tok){
			tokens.add(token);
		}
		
		Iterator<String> it = tokens.iterator();
		
		//iterate through the tokens and filter the unwanted ones
		while(it.hasNext()){
			String token = it.next();
			if( (token.charAt(0) == '@') || (this.isURL(token)) ){
				it.remove();
			}
		}
		StringBuilder sb=new StringBuilder();
		for(String token:tokens){
			sb.append(token);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length()-1);
		String string=sb.toString().trim();
		
		return string;
	}
	
	
	private int numOfUniqueUrls(ArrayList<DBObject> tweets){
		int uniqueUrls = 0;
		HashSet<String> set = new HashSet<>();
		
		for(DBObject tweet:tweets){
			String[] urls = (String[])tweet.get("urls");
			for(String url:urls){
				if(set.add(url)) uniqueUrls++;
			}
		}
		
		return uniqueUrls;
	}
	
	private static int minimum(int a, int b, int c) {                            
        return Math.min(Math.min(a, b), c);                                      
    }   
	
 
    private static int computeLevenshteinDistance(String str1,String str2) {      
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];        
 
        for (int i = 0; i <= str1.length(); i++)                                 
            distance[i][0] = i;                                                  
        for (int j = 1; j <= str2.length(); j++)                                 
            distance[0][j] = j;                                                  
 
        for (int i = 1; i <= str1.length(); i++)                                 
            for (int j = 1; j <= str2.length(); j++)                             
                distance[i][j] = minimum(                                        
                        distance[i - 1][j] + 1,                                  
                        distance[i][j - 1] + 1,                                  
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));
 
        return distance[str1.length()][str2.length()];                           
    }
    
    private String findMostUsedSource(ArrayList<DBObject> tweets){
    	HashMap<String,Integer> map = new HashMap<>();
    	
    	for(int i=0; i < tweets.size(); i++){
    		DBObject tweet = tweets.get(i);
    		
    		//find the source of the tweet
    		String source=tweet.get("source").toString();
    		
    		Integer appearances = map.get(source);
    		if(appearances!=null){
    			map.put(source, appearances+1);
    		}
    		else{
    			map.put(source, 1);
    		}
    	}
    	
    	//find most used source
    	Set<String> keys = map.keySet();
    	int max = 0;
    	String maxSource=null;
    	for(String key:keys){
    		if(map.get(key)>max){
    			max=map.get(key);
    			maxSource=key;
    		}
    	}
    	
    	return maxSource;
    }
    
	private boolean isURL(String urlStr) {
	    /*try {
	      URI uri = new URI(urlStr);
	      return true;
	    }
	    catch (URISyntaxException e) {
	        return false;
	    }*/
		
        Matcher matcher = this.urlPattern.matcher(urlStr);
        return matcher.matches();
	}
	
	private static int getAccountAge(String created_at) {
		long timeDifference = Calendar.getInstance().getTimeInMillis()
				- Date.parse(created_at);
		float daysDifference = timeDifference / (1000 * 60 * 60 * 24);
		
		return Math.round(daysDifference);
	}
	
	private void extract(long userID){
		//TODO: get all the tweets from user with id = userID
		
		//create the tweets arraylist
		ArrayList<DBObject> tweets = new ArrayList<>();
		
		//extract the characteristics
		int numberOfFriends=0, numberOfFollowers=0;//***
		float ffRatio = (float)numberOfFollowers/numberOfFriends;
		int accountAge=0;//***
		int numberOfTweets = tweets.size();
		int numberOfRetweets=this.numOfRetweets(tweets);//??
		int numberOfReplies=0;//***
		int numberOfMentions = this.numOfMentions(tweets);
		int numberOfOthersRetweets = this.numOfOthersRetweets(tweets);
		int numberOfHashtags = this.numOfHashtags(tweets);
		int numberOfHashtagTweets = this.numOfTweetsContainingHashtags(tweets);		
		int numberOfUrlTweets = this.numOfTweetsContainingUrls(tweets);		
		int numberOfCopies = this.numberOfSameTweets(tweets);
		String mostUsedSource = this.findMostUsedSource(tweets);
		int numberOfUniqueUrls = this.numOfUniqueUrls(tweets);
		int numberOfUniqueDomains=0;//***
		
		//insert them in the database
		this.cdb.insertSelectedUser(userID, numberOfFollowers, numberOfFriends, accountAge, numberOfTweets, numberOfRetweets, numberOfReplies, numberOfMentions, numberOfOthersRetweets, numberOfHashtags, numberOfHashtagTweets, numberOfUrlTweets, numberOfCopies, mostUsedSource, numberOfUniqueUrls, numberOfUniqueDomains);
		
	}
	
	public void runExtractor(){
		//get the chosen users' ids
		long[] chosenUsersIds = this.tdbm.fetchChosenUsers("chosen_users");
		
		//extract the characteristics of each one
		for(long userID:chosenUsersIds){
			this.extract(userID);
		}
	}
}
