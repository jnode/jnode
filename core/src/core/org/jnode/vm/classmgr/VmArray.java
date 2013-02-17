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
 
package org.jnode.vm.classmgr;

import org.jnode.vm.objects.VmSystemObject;

/**
 * <description>
 *
 * @author epr
 */
public class VmArray extends VmSystemObject {

    public static final int LENGTH_OFFSET = 0;
    public static final int DATA_OFFSET = LENGTH_OFFSET + 1;

    /**
     * Are the two given char-arrays equal in length and contents?
     *
     * @param a
     * @param b
     * @return boolean
     */
    public static boolean equals(char[] a, char[] b) {
        int len = a.length;
        if (len != b.length) {
            return false;
        }
        for (int i = len - 1; i >= 0; i--) {
            if (a[i] != b[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Is the given char-arrays equal in length and contents to the given
     * string?
     *
     * @param a
     * @param b
     * @return boolean
     */
    public static boolean equals(char[] a, String b) {
        int len = a.length;
        if (len != b.length()) {
            return false;
        }
        for (int i = len - 1; i >= 0; i--) {
            if (a[i] != b.charAt(i)) {
                return false;
            }
        }

        return true;
    }
}
