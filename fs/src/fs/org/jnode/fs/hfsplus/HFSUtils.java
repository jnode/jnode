package org.jnode.fs.hfsplus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class HFSUtils {
	 public static final long DIFF_TO_JAVA_DATE_IN_MILLIS = 2082844800000L;
	/**
	 * 
	 * @param time time in second since midnight, January 1, 1904, GMT.
	 * @return
	 */
	public static Calendar decodeDate(int time){
		Calendar ref = Calendar.getInstance();
		ref.setTime(new Date((time*1000)-DIFF_TO_JAVA_DATE_IN_MILLIS));
		return ref;
	}
	/**
	 * 
	 * @param time
	 * @param dateFormat
	 * @return
	 */
	public static String printDate(int time, String dateFormat){
		Calendar ref = decodeDate(time);
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(ref.getTime());
	}
}
