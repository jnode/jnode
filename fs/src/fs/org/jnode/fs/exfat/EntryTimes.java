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
 
package org.jnode.fs.exfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import org.apache.log4j.Logger;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
final class EntryTimes {

    public static EntryTimes read(ByteBuffer src) throws IOException {

        /* read create date/time */
        final int cTime = DeviceAccess.getUint16(src);
        final int cDate = DeviceAccess.getUint16(src);

        /* read modify date/time */
        final int mTime = DeviceAccess.getUint16(src);
        final int mDate = DeviceAccess.getUint16(src);

        /* read access date/time */
        final int aTime = DeviceAccess.getUint16(src);
        final int aDate = DeviceAccess.getUint16(src);
        
        /* read c/m centiseconds */
        final int cTimeCs = DeviceAccess.getUint8(src);
        final int mTimeCs = DeviceAccess.getUint8(src);
        
        /* read time zone offsets */
        final int cOffset = DeviceAccess.getUint8(src);
        final int mOffset = DeviceAccess.getUint8(src);
        final int aOffset = DeviceAccess.getUint8(src);

        final Date created = exfatToUnix(cDate, cTime, cTimeCs, cOffset);
        final Date modified = exfatToUnix(mDate, mTime, mTimeCs, mOffset);
        final Date accessed = exfatToUnix(aDate, aTime, 0, aOffset);

        return new EntryTimes(created, modified, accessed);
    }

    private static final int EXFAT_EPOCH = 1980;

    private static Date exfatToUnix(int date, int time, int cs, int tzOffset)  {

        try {
            final GregorianCalendar cal =
                new GregorianCalendar(TimeZone.getTimeZone("UTC"));

            cal.setTimeInMillis(0);

            final int day = date & 0x1f;
            final int month = ((date >> 5) & 0x0f);
            final int year = ((date >> 9) & 0x7f) + EXFAT_EPOCH;

            if ((day == 0) || (month <= 0) || (month > 12)) {
                throw new IllegalStateException(
                    "bad day (" + day + ") or month ("
                        + month + ") value");
            }

            cal.set(year, month - 1, day);

            final int twoSec = (time & 0x1f);
            final int min = ((time >> 5) & 0x0f);
            final int hour = (time >> 11);

            if ((hour > 23) || (min > 59) || (twoSec > 29)) {
                throw new IllegalStateException("bad hour ("
                    + hour + ") or minute ("
                    + min + ") value");
            }

            if (cs > 199) {
                throw new IllegalStateException("bad centiseconds value");
            }

            cal.add(GregorianCalendar.HOUR_OF_DAY, hour);
            cal.add(GregorianCalendar.MINUTE, min);
            cal.add(GregorianCalendar.SECOND, twoSec * 2);

            cal.setTimeInMillis(cal.getTimeInMillis() + (cs * 10));

            /* adjust for TZ offset */

            final boolean tzNeg = ((tzOffset & 0x40) != 0);
            tzOffset &= 0x3f;
            tzOffset *= tzNeg ? -15 : 15;

            cal.add(GregorianCalendar.MINUTE, tzOffset);

            return cal.getTime();
        } catch (Exception e) {
            Logger.getLogger(EntryTimes.class).error("Error getting entry times", e);
            return null;
        }
    }

    private final Date created;
    private final Date modified;
    private final Date accessed;

    public EntryTimes(Date created, Date modified, Date accessed) {
        this.created = created;
        this.modified = modified;
        this.accessed = accessed;
    }

    public Date getCreated() {
        return created == null ? null : (Date) created.clone();
    }

    public Date getModified() {
        return modified == null ? null :  (Date) modified.clone();
    }

    public Date getAccessed() {
        return accessed == null ? null :  (Date) accessed.clone();
    }

}
