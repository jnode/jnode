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
 
package org.jnode.assembler;

import org.jnode.vm.VmAddress;

/**
 * A Label is a reference to an address in the native code.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Label extends VmAddress implements Comparable<Object> {

    private final String label;

    /**
     * Create a new instance
     *
     * @param l
     */
    public Label(String l) {
        label = l;
    }

    /**
     * Convert myself to a String representation
     *
     * @return The string representation
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return label;
    }

    /**
     * Is this object equal to the given object?
     *
     * @param o
     * @return True if o is equal to this, false otherwise.
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object o) {
        if (o instanceof Label)
            return label.equals(((Label) o).label);
        else
            return false;
    }

    /**
     * Gets the hashcode of this object.
     *
     * @return The hashcode
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return label.hashCode();
    }

    /**
     * Compare myself to the given object.
     *
     * @param o
     * @return 0 if equal, less then 0 if this is less then o, greater then 0 otherwise
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }

        return label.compareTo(o.toString());
    }
}
