/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.hfsplus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HfsUtils {

    /**
     * Difference in second between 01/01/1970 00:00:00 (java reference time)
     * and 01/01/1904 00:00:00 (HFS reference time).
     */
    public static final long MAC_DATE_CONVERSION = 2082844800L;

    /**
     * Convert time from/to java time to/from mac time.
     * 
     * @param time in seconds since reference date.
     * @param encode if {code true}, convert from java to mac, otherwise convert
     *            from mac to java.
     * 
     * @return the converted time
     */
    public static long getDate(long time, boolean encode) {
        time = (encode) ? time + MAC_DATE_CONVERSION : time - MAC_DATE_CONVERSION;
        return time;
    }

    /**
     * 
     * @param time a date/time in seconds since the UNIX epoch.
     * @param dateFormat the date/time format string
     * @return the date/time formatted as a String
     */
    public static String printDate(final long time, final String dateFormat) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getDate(time, false) * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return sdf.format(cal.getTime());
    }

    /**
     * Returns current date and time in mac format.
     * 
     * @return current date and time.
     */
    public static int getNow() {
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        return (int) HfsUtils.getDate(now.getTimeInMillis() / 1000, true);
    }
}
