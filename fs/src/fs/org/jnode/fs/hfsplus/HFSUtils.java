package org.jnode.fs.hfsplus;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class HFSUtils {

    /**
     * Difference in second between 01/01/1970 00:00:00 (java reference time)
     * and 01/01/1904 00:00:00 (HFS reference time).
     */
    public static final long MAC_DATE_CONVERTION = 2082844800L;

    /**
     * Convert time from/to java time to/from mac time.
     * 
     * @param time
     *            in seconds since reference date.
     * @param encode
     *            if set to true, convert from java to mac. If set to false,
     *            convert from mac to java.
     * 
     * @return
     */
    public static long getDate(long time, boolean encode) {
        time = (encode) ? time + MAC_DATE_CONVERTION : time - MAC_DATE_CONVERTION;
        return time;
    }

    /**
     * 
     * @param time
     * @param dateFormat
     * @return
     */
    public static String printDate(final long time, final String dateFormat) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getDate(time, false) * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }
}
