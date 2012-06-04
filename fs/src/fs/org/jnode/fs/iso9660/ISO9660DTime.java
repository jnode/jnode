/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.fs.iso9660;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.jnode.util.LittleEndian;

/**
 * ISO9660 datetime structure.
 *
 * @author Daniel Noll
 */
public class ISO9660DTime {

    /**
     * Length of the structure.
     */
    static final int LENGTH = 7;

    /**
     * Number of years since 1900.
     */
    private final int/*uint8*/ year;

    /**
     * Month of the year from 1 to 12.
     */
    private final int/*uint8*/ month;

    /**
     * Day of the month from 1 to 31.
     */
    private final int/*uint8*/ day;

    /**
     * Hour of the day from 0 to 23.
     */
    private final int/*uint8*/ hour;

    /**
     * Minute of the hour from 0 to 59.
     */
    private final int/*uint8*/ minute;

    /**
     * Second of the minute from 0 to 59.
     */
    private final int/*uint8*/ second;

    /**
     * GMT values -48 .. + 52 in 15 minute intervals (e.g. -48 = GMT-12, +48=GMT+12, 0=UTC.)
     */
    private final int/*int8*/ gmtOffset;

    /**
     * Constructs an ISO9660 datetime structure by reading from the provided byte array.
     *
     * @param buff the byte array from which to read.
     * @param off  offset into the byte array at which to read the struct.
     */
    public ISO9660DTime(byte[] buff, int off) {
        year = LittleEndian.getUInt8(buff, off);
        month = LittleEndian.getUInt8(buff, off + 1);
        day = LittleEndian.getUInt8(buff, off + 2);
        hour = LittleEndian.getUInt8(buff, off + 3);
        minute = LittleEndian.getUInt8(buff, off + 4);
        second = LittleEndian.getUInt8(buff, off + 5);
        gmtOffset = LittleEndian.getInt8(buff, off + 6);
    }

    /**
     * Converts to a Java date, milliseconds since 1970.
     *
     * @return the date as millis since 1970.
     */
    public long toJavaMillis() {
        // Force use of the Gregorian calendar in UTC and manually offset hours later.
        // Mainly because it isn't convenient to create a TimeZone from a UTC/GMT offset.
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.clear(); // kills milliseconds or any other partial crap.
        cal.set(year + 1900, month - 1, day, hour, minute, second);

        // GMT offset in 15 minute intervals.
        // Example, if offset is GMT+10, the value will be +40, and we need to subtract 600 minutes.
        cal.add(Calendar.MINUTE, -gmtOffset * 15);

        return cal.getTimeInMillis();
    }
}
