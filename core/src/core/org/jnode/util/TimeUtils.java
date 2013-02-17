/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.util;



/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TimeUtils {

    /**
     * Sleep for ms milliseconds.
     *
     * @return True if return normal, false on InterruptedException.
     */
    public static boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    /**
     * Converts Gregorian date to milliseconds since 1970-01-01 00:00:00 .
     *
     * @param year  the year
     * @param mon   the month 1..12
     * @param day   the day of month 1..31
     * @param hours hours of day 0..23
     * @param mins  minutes 0..59
     * @param secs  seconds 0..59
     * @return the milliseconds since 1970-01-01 00:00:00
     */
    public static long time2millis(int year, int mon, int day, int hours, int mins, int secs) {
        if (0 >= (mon -= 2)) {    /* 1..12 -> 11,12,1..10 */
            mon += 12;            /* Puts Feb last since it has leap day */
            year -= 1;
        }

        return ((((
            ((long) (year / 4 - year / 100 + year / 400 + 367 * mon / 12 + day) + year * 365 - 719499)) /* days */
            * 24l + hours) /* hours */
            * 60l + mins) /* minutes */
            * 60l + secs) /* seconds */
            * 1000l; /* milliseconds */
    }
}
