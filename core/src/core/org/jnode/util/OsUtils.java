/*
 * $Id$
 */
package org.jnode.util;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class OsUtils {
	
	/**
	 * Are we running in JNode.
	 * @return boolean
	 */
	public static final boolean isJNode() {
		final String osName = System.getProperty("os.name", "");
		return osName.equals("JNode");
	}

}
