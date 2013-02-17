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
public final class StopWatch {

    private long startTime;
    private long stopTime;

    public StopWatch() {
        start();
    }

    public final void start() {
        this.stopTime = 0;
        this.startTime = System.currentTimeMillis();
    }

    public final void stop() {
        this.stopTime = System.currentTimeMillis();
    }

    public final long getElapsedTimed() {
        if (stopTime != 0) {
            return stopTime - startTime;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }

    public final boolean isElapsedLongerThen(long ms) {
        return getElapsedTimed() > ms;
    }

    public String toString() {
        return String.valueOf(getElapsedTimed()) + "ms";
    }
}
