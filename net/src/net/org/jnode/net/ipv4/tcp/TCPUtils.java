/*
 * $Id$
 */
package org.jnode.net.ipv4.tcp;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TCPUtils {

	/**
	 * Is sequence number a &lt; b.
	 * @param a
	 * @param b
	 */
	public static boolean SEQ_LT(int a, int b) {
		return ((a-b) < 0);
	}

	/**
	 * Is sequence number a &lt;= b.
	 * @param a
	 * @param b
	 */
	public static boolean SEQ_LE(int a, int b) {
		return ((a-b) <= 0);
	}

	/**
	 * Is sequence number a &gt; b.
	 * @param a
	 * @param b
	 */
	public static boolean SEQ_GT(int a, int b) {
		return ((a-b) > 0);
	}

	/**
	 * Is sequence number a &gt;= b.
	 * @param a
	 * @param b
	 */
	public static boolean SEQ_GE(int a, int b) {
		return ((a-b) >= 0);
	}

}
