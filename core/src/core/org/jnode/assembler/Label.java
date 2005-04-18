/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.assembler;

import org.jnode.vm.VmAddress;

/**
 * A Label is a reference to an address in the native code.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Label extends VmAddress implements Comparable {
	
	private final String label;

	/**
	 * Create a new instance
	 * @param l
	 */
	public Label(String l) {
		label = l;
	}

	/**
	 * Convert myself to a String representation
	 * @see java.lang.Object#toString()
	 * @return The string representation
	 */
	public String toString() {
		return label;
	}

	/**
	 * Is this object equal to the given object?
	 * @param o
	 * @see java.lang.Object#equals(Object)
	 * @return True if o is equal to this, false otherwise.
	 */
	public boolean equals(Object o) {
		if (o instanceof Label)
			return label.equals(((Label) o).label);
		else
			return false;
	}

	/**
	 * Gets the hashcode of this object.
	 * @see java.lang.Object#hashCode()
	 * @return The hashcode
	 */
	public int hashCode() {
		return label.hashCode();
	}

	/**
	 * Compare myself to the given object.
	 * @param o
	 * @see java.lang.Comparable#compareTo(Object)
	 * @return 0 if equal, less then 0 if this is less then o, greater then 0 otherwise
	 */
	public int compareTo(Object o) {
		if (o == null) {
			return -1;
		}

		return label.compareTo(o.toString());
	}
}
