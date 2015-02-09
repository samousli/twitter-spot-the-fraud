package Utils;

import java.util.Calendar;
import java.util.Date;

public class HelperFunctions {
	
	public static int getAccountAge(String created_at) {
		long timeDifference = Calendar.getInstance().getTimeInMillis()
				- Date.parse(created_at);
		float daysDifference = timeDifference / (1000 * 60 * 60 * 24);

		return Math.round(daysDifference);
	}

}
