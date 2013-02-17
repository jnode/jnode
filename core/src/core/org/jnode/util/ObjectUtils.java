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
public class ObjectUtils {

    /**
     * Compare the two objects and return true if they are equal, false otherwise.
     * If one of the object is null and the other is not, false is returned.
     *
     * @param a Can be null
     * @param b Can be null
     * @return True if (a == b) || a.equals(b)
     */
    public static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        } else if ((a != null) && (b != null)) {
            return a.equals(b);
        } else {
            return false;
        }
    }
}
