/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author epr
 */
public class BeanUtils {
	
	/**
	 * Convert a name to a name conforming to beans specifications.
	 * The first letter of the string to converted to uppercase, the rest to
	 * lowercase.
	 * @param name
	 * @return String
	 */
	public static String getBeanName(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
	}

}
