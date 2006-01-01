/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * You should have received a copy of the GNU General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.mmtk.vm;

/**
 * $Id$
 * 
 * @author <a href="http://cs.anu.edu.au/~Steve.Blackburn">Steve Blackburn</a>
 * @author Perry Cheng
 * @version $Revision$
 * @date $Date$
 */
public class Statistics {
    
    /**
     * Returns the number of collections that have occured.
     * 
     * @return The number of collections that have occured.
     */
    public static final int getCollectionCount() {
        return 0;
    }

    /**
     * Read cycle counter
     */
    public static long cycles() {
        return 0L;
    }

    /**
     * Convert cycles to milliseconds
     */
    public static double cyclesToMillis(long c) {
        return (double) 0;
    }

    /**
     * Convert cycles to seconds
     */
    public static double cyclesToSecs(long c) {
        return (double) 0;
    }

    /**
     * Convert milliseconds to cycles
     */
    public static long millisToCycles(double t) {
        return 0L;
    }

    /**
     * Convert seconds to cycles
     */
    public static long secsToCycles(double t) {
        return 0L;
    }
}
