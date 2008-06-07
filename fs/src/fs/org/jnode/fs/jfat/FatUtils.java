/*
 *
 */

package org.jnode.fs.jfat;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author gvt
 * @author Tango
 */
public class FatUtils {
    private static final SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    private static final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");

    public static long decodeDateTime(int dosDate, int dosTime, int dosTenth) {
        Calendar cal = Calendar.getInstance();

        if (dosDate < 0 || dosDate > 0xFFFF)
            throw new IllegalArgumentException("dosDate is invalid: " + dosDate);

        if (dosTime < 0 || dosTime > 0xFFFF)
            throw new IllegalArgumentException("dosTime is invalid: " + dosTime);

        if (dosTenth < 0 || dosTenth > 199)
            throw new IllegalArgumentException("dosTime is invalid: " + dosTenth);

        int seconds = dosTenth / 100;
        int milliseconds = (dosTenth % 100) * 10;

        cal.set(Calendar.MILLISECOND, milliseconds);

        cal.set(Calendar.SECOND, (dosTime & 0x1f) * 2 + seconds);
        cal.set(Calendar.MINUTE, (dosTime >> 5) & 0x3f);
        cal.set(Calendar.HOUR_OF_DAY, dosTime >> 11);

        cal.set(Calendar.DATE, dosDate & 0x1f);
        cal.set(Calendar.MONTH, ((dosDate >> 5) & 0x0f) - 1);
        cal.set(Calendar.YEAR, 1980 + (dosDate >> 9));

        return cal.getTimeInMillis();
    }

    public static long decodeDateTime(int dosDate, int dosTime) {
        return decodeDateTime(dosDate, dosTime, 0);
    }

    public static long getMinDateTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        /*
         * dos Minimum DateTime is: January 1, 1980 00:00:00
         */
        cal.set(1980, 0, 1, 0, 0, 0);
        return cal.getTimeInMillis();
    }

    public static long getMaxDateTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MILLISECOND, 0);
        /*
         * dos Maximum DateTime is: December 31, 2107 23:59:58
         */
        cal.set(2107, 11, 31, 23, 59, 58);
        return cal.getTimeInMillis();
    }

    public static long checkDateTime(long javaDateTime) {
        long dosMinDateTime = getMinDateTime();

        if (javaDateTime < dosMinDateTime || javaDateTime > getMaxDateTime())
            return dosMinDateTime;
        else
            return javaDateTime;
    }

    public static int encodeTime(long javaDateTime) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(checkDateTime(javaDateTime));

        return 2048 * cal.get(Calendar.HOUR_OF_DAY) + 32 * cal.get(Calendar.MINUTE) +
                cal.get(Calendar.SECOND) / 2;
    }

    public static int encodeDate(long javaDateTime) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(checkDateTime(javaDateTime));

        return 512 * (cal.get(Calendar.YEAR) - 1980) + 32 * (cal.get(Calendar.MONTH) + 1) +
                cal.get(Calendar.DATE);
    }

    public static int encodeTenth(long javaDateTime) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(checkDateTime(javaDateTime));

        return 100 * (cal.get(Calendar.SECOND) % 2) + (cal.get(Calendar.MILLISECOND) / 10);
    }

    public static String fTime(long javaDateTime) {
        return time.format(javaDateTime);
    }

    public static String fDate(long javaDateTime) {
        return date.format(javaDateTime);
    }

    public static final boolean compareIgnoreCase(String s1, String s2) {
        return s1.equalsIgnoreCase(s2);
    }

    public static final String toIgnoreCase(String s) {
        //
        // it may be the right solution for what
        // the spec (pag 29) ditctate and to
        // solve the "Georgian Alphabet" problem
        // (source of Java String)?
        //
        return s.toUpperCase().toLowerCase();
    }

    public static final String toUpperCase(String s) {
        return toIgnoreCase(s).toUpperCase();
    }

    public static final boolean isLowerCase(String s) {
        if (s.length() == 0)
            return false;

        char[] ca = s.toCharArray();

        for (int i = 0; i < ca.length; i++)
            if (!(Character.toLowerCase(ca[i]) == ca[i]))
                return false;

        return true;
    }

    public static final boolean isUpperCase(String s) {
        if (s.length() == 0)
            return false;

        char[] ca = s.toCharArray();

        for (int i = 0; i < ca.length; i++)
            if (!(Character.toUpperCase(ca[i]) == ca[i]))
                return false;

        return true;
    }

    public static final String longName(String name) {
        String lname = name.trim();
        lname = lname.replaceAll("\\.*$", "");
        return lname;
    }

    public static int getMonth(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.MONTH);
    }

    public static int getHours(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        // For Calendar, Sunday is 1. For Date, Sunday is 0.
        return cal.get(Calendar.DAY_OF_WEEK) - 1;
    }

    public static int getYear(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.YEAR) - 1980;
    }

    public static int getMinutes(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.MINUTE);
    }

    public static int getSeconds(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal.get(Calendar.SECOND);
    }

    public static long getMilliSeconds(long time) {

        Calendar cal = Calendar.getInstance();
        return cal.getTimeInMillis();
    }

}
