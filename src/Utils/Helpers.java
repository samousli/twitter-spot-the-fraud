package Utils;

import java.util.Calendar;
import java.util.Date;

import com.mongodb.DBObject;

public class Helpers {
	
	@SuppressWarnings("deprecation")
	public static int getAccountAge(String created_at) {
		long timeDifference = Calendar.getInstance().getTimeInMillis()
				- Date.parse(created_at);
		float daysDifference = timeDifference / (1000 * 60 * 60 * 24);

		return Math.round(daysDifference);
	}
	
	public static long fetchLong(DBObject o, String field) {
		if (o.get(field) == null)
			return 0;
		if (o.get(field) instanceof Long)
			return (long) o.get(field);
		if (o.get(field) instanceof Integer)
			return (long) ((Integer) o.get(field)).longValue();

		throw new RuntimeException("MONGO fetchLong: Can't cast value");
	}

}
