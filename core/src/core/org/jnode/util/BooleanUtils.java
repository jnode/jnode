/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BooleanUtils {

	/**
	 * Returns true if value equals "true", "on", "yes" or "1".
	 * @param value Can be null
	 * @return
	 */
	public static boolean valueOf(String value) {
		if (value == null) {
			return false;
		}
		value = value.trim().toLowerCase();
		return (value.equals("true") || value.equals("on") || value.equals("yes") || value.equals("1"));
	}
}
