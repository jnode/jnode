/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ObjectUtils {

	/**
	 * Compare the two objects and return true if they are equal, false otherwise.
	 * If one of the object is null and the other is not, false is returned.
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
