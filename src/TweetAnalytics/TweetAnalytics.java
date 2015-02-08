package TweetAnalytics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.Cursor;
import com.mongodb.DBObject;

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

		// Use to remove the frequency field for all documents
		// db.users.update({},{$unset : {frequency: "" }}, {multi: true})

		// Use to query the documents which don't contain the frequency field
		// db.users.find({ "frequency" : { $exists : true } })

		// dbm.countFrequencyByUser();

		// int[] qr = dbm.calculateQuartiles("users_backup");
		// System.out.println("Quartiles:");
		// System.out.println("\tQ1 = " + qr[0] + "\n\tQ2 = " + qr[1]
		// + "\n\tQ3 = " + qr[2]);

		// System.out.println("Choosing random users");

		// long[] user_ids = dbm.fetchChosenUsers("chosen_users");

		// new TwitterUserTracker(user_ids);

		// 4a
		generateBasicUserStats();

	}

	public static void generateBasicUserStats() {
		UserDataFetcher fetcher = new UserDataFetcher();

		Cursor users = dbm.getCollection("users").find();
		while (users.hasNext())
			fetcher.fetchBasicUserData(fetchLong(users.next(), "_id"));

		users.close();
	}

	/*
	 * db.tweets.aggregate([ { $group: {_id : '$user.id', tweet_id : {$push:
	 * '$id'}, count: { $sum: 1 }} }, { $sort: {'count':-1} } ])
	 */
	@SuppressWarnings("deprecation")
	public static void groupTweetsByUser() {

		dbm.initUserCollection();
		// In order to avoid deleting potentially large amounts of data..again

		List<DBObject> pipeline = new ArrayList<>();

		// Group by user id and keep tweet texts and ids
		// Query: { $group: {_id : '$user.id', tweets : {
		// $push: '$text' },
		// count: { $sum: 1 } } };

		DBObject groupByUser = new BasicDBObject("$group", new BasicDBObject(
				"_id", "$user.id").append("tweets",
				new BasicDBObject("$push", "$text")).append("count",
				new BasicDBObject("$sum", 1)));

		// Sort by tweet count
		// DBObject sortByCount = new BasicDBObject("$sort", new BasicDBObject(
		// "count", -1));

		// Filter the users with single a tweet out
		// { $match : { "count" : { $gt : 1 } } }
		DBObject filterUsers = new BasicDBObject("$match", new BasicDBObject(
				"count", new BasicDBObject("$gt", 1)));

		// Output to new table
		DBObject outputToNewCollection = new BasicDBObject("$out", "users");

		pipeline.add(groupByUser);
		// pipeline.add(sortByCount);
		// pipeline.add(filterUsers);
		pipeline.add(outputToNewCollection);

		// Disk use is necessary for collections resulting in more than 100mb of
		// data.
		AggregationOptions opts = AggregationOptions.builder()
				.allowDiskUse(true).build();

		dbm.getCollection("tweets").aggregate(pipeline, opts);
	}

	@SuppressWarnings("deprecation")
	/**
	 * Counts the number of trending topics each user appears in and saves it as a
	 * field in the user collection
	 */
	public static void countFrequencyByUser() {

		Cursor trends = dbm.getCollection("trends").find();

		while (trends.hasNext()) {
			// db.users.update({ // $text : { $search : "\"<Trend>\"", $language
			// : "none"} },
			// { $inc : { frequency: 1 }}, // { multi : true });
			// Note: Escaping hyphens for phrase search
			String s = (String) trends.next().get("name");
			System.out.println(s);
			DBObject query = new BasicDBObject("$text", new BasicDBObject(
					"$search", "\"" + s + "\"").append("$language", "none"));

			DBObject updateOp = new BasicDBObject("$inc", new BasicDBObject(
					"frequency", 1));

			dbm.getCollection("users").update(query, updateOp, false, true);
		}
	}

	@SuppressWarnings("deprecation")
	public static int[] calculateQuartiles(String collection) {

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
		System.out.println("ch1: " + index);
		q1 = (int) fetchLong(users.next(), "frequency");

		while (index++ < second_halve)
			users.next();
		System.out.println("ch2: " + index);
		q2 = (int) fetchLong(users.next(), "frequency");

		while (index++ < third_halve)
			users.next();
		System.out.println("ch3: " + index);
		q3 = (int) fetchLong(users.next(), "frequency");

		users.close();
		return new int[] { q1, q2, q3 };
	}

	/**
	 * If there is no frequency field (text doesn't belong to any trend(wonders
	 * of twitter API)) return 0
	 * 
	 * @param usr
	 * @return frequency value
	 */
	static long fetchLong(DBObject o, String field) {
		if (o.get(field) == null)
			return 0;
		if (o.get(field) instanceof Long)
			return (long) o.get(field);
		if (o.get(field) instanceof Integer)
			return (long) ((Integer) o.get(field)).longValue();

		throw new RuntimeException("MONGO fetchLong: Can't cast value");
	}

	public static long[] pickUsersPerQuartile(String inCol, String outCol,
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
					user_ids[(int) (q * num_users + i)] = fetchLong(e, "_id");
				}
			}
		}
		return user_ids;
	}

}
