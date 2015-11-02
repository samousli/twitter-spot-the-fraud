package TweetAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import Utils.Helpers;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBObject;

/**
 *
 * @author avail
 */
public class TweetAnalytics {

	public static final DBManager dbm = new DBManager();

	public static void main(String[] args) {

		// Wait for console input
		System.out.println("1) Group tweets by user.");
		System.out.println("2) Generate basic analytics(4a).");
		System.out.println("3) Calculate quartiles.");
		System.out.println("4) Track selected users.");
		System.out.println("5) Generate detailed analytics(4b).");

		Scanner reader = new Scanner(System.in);
		int c = reader.nextInt();

		switch (c) {
		case 1:
			System.out.println("Grouping tweets by user..");
			groupTweetsByUser("users", "tweets");
			System.out.println("Done.");
			break;
		case 2:
			System.out.println("Generating basic statistics..");
			runBasicAnalytics("users");
			countFrequencyByUser("users", "trends");
			System.out.println("Done.");
			break;
		case 3:
			System.out.println("Calculating quartiles..");
			int[] qr = calculateQuartiles("users");
			System.out.println("Done.");
			System.out.println("\tQ1 = " + qr[0] + "\n\tMedian = " + qr[1]
					+ "\n\tQ3 = " + qr[2]);
			break;
		case 4:
			long[] user_ids = dbm.fetchChosenUsers("chosen_users");
			if (user_ids.length == 0) {
				System.out.println("No chosen users, choosing now..");
				int[] qr1 = calculateQuartiles("users");
				user_ids = pickUsersPerQuartile("users", "chosen_users", qr1,
						10);
			}
			new TwitterUserTracker("chosen_user_tweets", user_ids);
			System.out.println("Done.");
			break;
		case 5:
			System.out.println("Generating advanced statistics..");
			filterTweetsByUser("chosen_users", "chosen_user_tweets",
					"chosen_user_tweets_filtered");
			runDetailedAnalytics("chosen_users", "chosen_user_tweets_filtered");
			System.out.println("Done.");
			break;
		default:
			System.err.println("Invalid choice.");
		}

		reader.close();

		// Use to remove the frequency field for all documents
		// db.users.update({},{$unset : {frequency: "" }}, {multi: true})

		// Use to query the documents which don't contain the frequency field
		// db.users.find({ "frequency" : { $exists : true } })

	}

	private static void runBasicAnalytics(String usr_col) {
		Cursor usrs = dbm.getCollection(usr_col).find();
		while (usrs.hasNext()) {
			DBObject usr = usrs.next();
			long id = Helpers.fetchLong(usr, "_id");
			float ff_ratio = ((float) (Helpers.fetchLong(usr, "followers")) / Helpers
					.fetchLong(usr, "friends"));
			int age = Helpers.getAccountAge((String) usr.get("created_at"));

			System.out.println(id + "\t" + ff_ratio + "\t" + age);

			/**
			 * { '_id' : id }, { $set : { 'ff_ratio' : ff_ratio, 'age' : age } }
			 */
			dbm.getCollection(usr_col).update(
					new BasicDBObject("_id", id),
					new BasicDBObject("$set", new BasicDBObject("ff_ratio",
							ff_ratio).append("age", age)));
		}
		usrs.close();
	}

	private static void runDetailedAnalytics(String usr_col, String tweet_col) {
		CharacteristicsExtractor e = new CharacteristicsExtractor(
				new CharacteristicsDB(), dbm);
		Cursor usrs = dbm.getCollection(usr_col).find();
		while (usrs.hasNext())
			e.extract(tweet_col, Helpers.fetchLong(usrs.next(), "_id"));
		usrs.close();
	}

	/*
	 * db.tweets.aggregate([ { $group: {_id : '$user.id', tweet_id : {$push:
	 * '$id'}, count: { $sum: 1 }} }, { $sort: {'count':-1} } ])
	 */
	@SuppressWarnings("deprecation")
	private static void groupTweetsByUser(String user_col, String tweet_col) {

		dbm.initUserCollection(user_col);
		// In order to avoid deleting potentially large amounts of data..again

		List<DBObject> pipeline = new ArrayList<>();
		/*
		 * Group by user id and keep tweet texts and ids Query: { $group: { _id
		 * : '$user.id', tweets : { $push: '$text' }, count: { $sum: 1 } friends
		 * : { $first : $user.friends_count } followers : { $first :
		 * $user.followers_count } ff_ratio : { $first : $user.followers_count /
		 * $user.friends_count } age : { $first : $user.created_at } } };
		 */
		DBObject groupByUser = new BasicDBObject("$group", new BasicDBObject(
				"_id", "$user.id")
				.append("tweets", new BasicDBObject("$push", "$text"))
				.append("count", new BasicDBObject("$sum", 1))
				.append("friends",
						new BasicDBObject("$first", "$user.friends_count"))
				.append("followers",
						new BasicDBObject("$first", "$user.followers_count"))
				.append("created_at",
						new BasicDBObject("$first", "$user.created_at")));

		// Filter the users with single a tweet out
		// { $match : { "count" : { $gt : 1 } } }
		DBObject filterUsers = new BasicDBObject("$match", new BasicDBObject(
				"count", new BasicDBObject("$gt", 1)));

		// Output to new table
		DBObject outputToNewCollection = new BasicDBObject("$out", user_col);

		pipeline.add(groupByUser);
		pipeline.add(filterUsers);
		pipeline.add(outputToNewCollection);

		// Disk use is necessary for collections resulting in more than 100mb of
		// data.
		AggregationOptions opts = AggregationOptions.builder()
				.allowDiskUse(true).build();

		dbm.getCollection(tweet_col).aggregate(pipeline, opts);
	}

	@SuppressWarnings("deprecation")
	/**
	 * Counts the number of trending topics each user appears in and saves it as a
	 * field in the user collection
	 */
	private static void countFrequencyByUser(String user_col, String trend_col) {

		Cursor trends = dbm.getCollection(trend_col).find();

		while (trends.hasNext()) {
			// db.users.update({ // $text : { $search : "\"<Trend>\"", $language
			// : "none"} },
			// { $inc : { frequency: 1 }}, // { multi : true });
			// Note: Escaping hyphens for phrase search
			String s = (String) trends.next().get("name");
			// System.out.println(s);
			DBObject query = new BasicDBObject("$text", new BasicDBObject(
					"$search", "\"" + s + "\"").append("$language", "none"));

			DBObject updateOp = new BasicDBObject("$inc", new BasicDBObject(
					"frequency", 1));

			dbm.getCollection(user_col).update(query, updateOp, false, true);
		}
	}

	@SuppressWarnings("deprecation")
	private static int[] calculateQuartiles(String collection) {

		long c = dbm.getCollection(collection).getCount();
		System.out.println("Document count: " + c);
		long first_halve = c / 4, second_halve = c / 2, third_halve = 3 * c / 4;
		int q1, q2, q3, index = 0;
		dbm.getCollection(collection).ensureIndex(
				new BasicDBObject("frequency", 1));
		Cursor users = dbm.getCollection(collection).find()
				.sort(new BasicDBObject("frequency", 1));

		while (index++ < first_halve)
			users.next();
		// System.out.println("ch1: " + index);
		q1 = (int) Helpers.fetchLong(users.next(), "frequency");

		while (index++ < second_halve)
			users.next();
		// System.out.println("ch2: " + index);
		q2 = (int) Helpers.fetchLong(users.next(), "frequency");

		while (index++ < third_halve)
			users.next();
		// System.out.println("ch3: " + index);
		q3 = (int) Helpers.fetchLong(users.next(), "frequency");

		users.close();
		return new int[] { q1, q2, q3 };
	}

	private static long[] pickUsersPerQuartile(String inCol, String outCol,
			int[] quartiles, int num_users) {

		long[] user_ids = new long[num_users * 4];
		// If collection exists, fetch users, else create it
		if (dbm.exists(outCol)) {
			Cursor cursor = dbm.getCollection(outCol).find();
			int i = 0;
			while (cursor.hasNext()) {
				DBObject e = cursor.next();
				if (e.get("_id") instanceof Long)
					user_ids[i++] = (Long) e.get("_id");
				else if (e.get("_id") instanceof Integer)
					user_ids[i++] = ((Integer) e.get("_id")).longValue();
			}
		} else {

			long c = dbm.getCollection(inCol).getCount();
			int qr = (int) (0.25 * c);
			Random n = new Random(Calendar.getInstance().getTimeInMillis());
			for (int q = 0; q < 4; q++) {
				for (int i = 0; i < num_users; i++) {

					// System.out.println(q + " " + c + " " + l);
					DBObject e;
					do {
						int l = (int) (q * qr) + n.nextInt(qr);
						e = dbm.getCollection(inCol).find()
								.sort(new BasicDBObject("frequency", 1))
								.limit(-1).skip(l).next();
					} while (e.get("frequency") == null);
					e.put("quartile", q);
					dbm.getCollection(outCol).insert(e);
					user_ids[(int) (q * num_users + i)] = Helpers.fetchLong(e,
							"_id");
				}
			}
		}
		return user_ids;
	}

	// Due to most likely retweets retaining their original user_ids..had to
	// filter some out.
	private static void filterTweetsByUser(String user_col, String tweet_col,
			String result_col) {
		Cursor e = dbm.getCollection(user_col).find();

		while (e.hasNext()) {
			Cursor t = dbm.getCollection(tweet_col).find(
					new BasicDBObject("user.id", Helpers.fetchLong(e.next(),
							"_id")));
			while (t.hasNext()) {
				dbm.getCollection(result_col).insert(t.next());
			}
		}
	}

}
