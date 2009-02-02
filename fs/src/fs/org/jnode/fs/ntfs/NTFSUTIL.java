/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.fs.ntfs;

import java.util.Date;

/**
 * @author vali
 */
public class NTFSUTIL {
    /**
     * Converts a 64-bit NTFS time value (number of 100-nanosecond intervals since January 1, 1601 UTC)
     * to a Java time value (number of milliseconds since January 1, 1970 UTC.)
     *
     * @param filetime the FILETIME value.
     * @return the number of milliseconds since 1970.
     */
    public static long filetimeToMillis(long filetime) {
        // Move the starting epoch to January 1, 1970.
        filetime -= 116444736000000000L;

        // Now convert the time into milliseconds, rather than 100-nanosecond units.
        if (filetime < 0) {
            filetime = -1 - ((-filetime - 1) / 10000);
        } else {
            filetime = filetime / 10000;
        }
        return filetime;
    }

    /**
     * Converts a 64-bit NTFS time value (number of 100-nanosecond intervals since January 1, 1601 UTC)
     * to a Java {@link Date}.
     *
     * @param filetime the FILETIME value.
     * @return the Java date.
     */
    public static Date filetimeToDate(long filetime) {
        return new Date(filetimeToMillis(filetime));
    }
}
