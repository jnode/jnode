/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package org.jnode.net.nfs.nfs2;

import java.util.Date;

public class Time {

    private int seconds;

    private int microSeconds;

    public Time() {
        this(0, 0);
    }

    /**
     * Constructs the time from a Java date object.
     *
     * @param date the date.
     */
    public Time(Date date) {
        this(date.getTime());
    }

    /**
     * Constructs the time from milliseconds since 1970.
     *
     * @param javaMillis the time in milliseconds since 1970.
     */
    public Time(long javaMillis) {
        this((int) (javaMillis / 1000L),
             (int) (1000 * (javaMillis % 1000)));
    }

    public Time(int seconds, int microSeconds) {
        this.seconds = seconds;
        this.microSeconds = microSeconds;
    }

    public void setSeconds(int x) {
        this.seconds = x;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public void setMicroSeconds(int x) {
        this.microSeconds = x;
    }

    public int getMicroSeconds() {
        return this.microSeconds;
    }

    /**
     * Converts the time to milliseconds since 1970.
     *
     * @return the time in milliseconds since 1970.
     */
    public long toJavaMillis() {
        return seconds * 1000L + microSeconds / 1000;
    }

    @Override
    public String toString() {
        Date date = new Date((long) seconds * 1000);
        return date.toString();
    }

}
