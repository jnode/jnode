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
 * @author epr
 */
public class BCDUtils {

    /**
     * Convert a BCD encoded value into a (normal) binary value
     *
     * @param bcd
     * @return int
     */
    public static int bcd2bin(int bcd) {
        return (bcd & 15) + ((bcd >> 4) * 10);
    }

    /**
     * Convert a (normal) binary value into a BCD encoded value.
     *
     * @param bin
     * @return int
     */
    public static int bin2bcd(int bin) {
        return ((bin / 10) << 4) + (bin % 10);
    }
}
