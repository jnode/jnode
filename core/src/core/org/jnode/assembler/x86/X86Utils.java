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
 
package org.jnode.assembler.x86;

/**
 * <description>
 *
 * @author epr
 */
public class X86Utils {

    /**
     * Does the given value fit in an 8-bit signed byte?
     *
     * @param value
     * @return boolean
     */
    public static boolean isByte(int value) {
        return ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE));
    }

    /**
     * Does the given value fit in an 16-bit signed byte?
     *
     * @param value
     * @return boolean
     */
    public static boolean isShort(int value) {
        return ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE));
    }

}
