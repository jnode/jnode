/**
 * $Id$
 */
package org.jnode.assembler;

import org.jnode.vm.Address;

/**
 * A Label is a reference to an address in the native code.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Label extends Address implements Comparable {
	
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

		if (!(o instanceof String)) {
			o = o.toString();
		}
		return label.compareTo(o);
	}
}