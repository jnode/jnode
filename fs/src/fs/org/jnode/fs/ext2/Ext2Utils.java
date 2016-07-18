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
 
package org.jnode.fs.ext2;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author epr
 * (every method copied from DosUtils)
 */
public class Ext2Utils {
    /**
     * ceil(a/b) if a>=0
     * 
     * @param a
     * @param b
     * @return the result of the computation
     */
    public static long ceilDiv(long a, long b) {
        return (int) ((a + b - 1) / b);
    }

    /**
     * Sets an unsigned 8-bit byte at a given offset
     *
     * @param offset
     */
    public static void set8(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
    }

    /**
     * Sets an unsigned 32-bit word at a given offset
     *
     * @param offset
     */
    public static void set32(byte[] data, int offset, long value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >>> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >>> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >>> 24) & 0xFF);
    }

    /**
     * @param time the time in seconds since the UNIX epoch
     * @return the decoded time in a {@link Calendar}
     */
    public static Calendar decodeDate(long time) {
        Calendar ref = Calendar.getInstance();
        ref.setTimeZone(TimeZone.getTimeZone("GMT"));
        ref.set(1970, 0, 1, 0, 0, 0);
        ref.add(Calendar.SECOND, (int) time);
        return ref;
    }

    /**
     * @param date the time encoded as a {@link Date}
     * @return the time in seconds since the UNIX epocj
     */
    public static long encodeDate(Date date) {
        Calendar ref = Calendar.getInstance();
        ref.setTimeZone(TimeZone.getTimeZone("GMT"));
        ref.setTime(date);
        return ref.getTimeInMillis() / 1000;
    }

}
