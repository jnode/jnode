/**
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

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
	 * @param a
	 * @param b
	 * @return boolean
	 */
	public static boolean equals(char[] a, char[] b) {
		int len = a.length;
		if (len != b.length) {
			return false;
		}
		for (int i = len-1; i >= 0; i--) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Is the given char-arrays equal in length and contents to the given
	 * string?
	 * @param a
	 * @param b
	 * @return boolean
	 */
	public static boolean equals(char[] a, String b) {
		int len = a.length;
		if (len != b.length()) {
			return false;
		}
		for (int i = len-1; i >= 0; i--) {
			if (a[i] != b.charAt(i)) {
				return false;
			}
		}
		
		return true;
	}
}
