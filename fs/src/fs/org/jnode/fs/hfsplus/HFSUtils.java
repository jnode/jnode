package org.jnode.fs.hfsplus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HFSUtils {
	 public static final long DIFF_TO_JAVA_DATE_IN_MILLIS = 2082844800000L;
	/**
	 * 
	 * @param time time in second since midnight, January 1, 1904, GMT.
	 * @return
	 */
	public static Date decodeDate(int time){
		return new Date(time * 1000 - DIFF_TO_JAVA_DATE_IN_MILLIS);
	}
	/**
	 * 
	 * @param time
	 * @param dateFormat
	 * @return
	 */
	public static String printDate(int time, String dateFormat){
		Date date = decodeDate(time);
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(date.getTime());
	}
}
