/**
 * $Id$
 */

package org.jnode.vm;

import java.util.StringTokenizer;

public abstract class VmSystemObject implements BootableObject {
	/**
	 * Mangle an identifier into a ASCII C name
	 * @param s
	 * @return String
	 */
	public static String mangle(String s) {
		final StringBuffer res = new StringBuffer();
		final int cnt = s.length();
		for (int i = 0; i < cnt; i++) {
			char ch = s.charAt(i);
			if (((ch >= 'a') && (ch <= 'z'))
				|| ((ch >= 'A') && (ch <= 'Z'))
				|| ((ch >= '0') && (ch <= '9'))) {
				res.append(ch);
			} else {
				res.append(Integer.toHexString(ch));
			}
		}
		return res.toString();
	}

	/**
	 * Mangle a classname into a ASCII C name
	 * @param s
	 * @return String
	 */
	public static String mangleClassName(String s) {
		s = s.replace('/', '.');
		final StringTokenizer tok = new StringTokenizer(s, ".");
		final StringBuffer res = new StringBuffer();
		int q = tok.countTokens();
		res.append('Q');
		res.append(q);
		while (tok.hasMoreTokens()) {
			String v = tok.nextToken();
			res.append(v.length());
			res.append(v);
		}
		return res.toString();
	}
	
	/**
	 * Verify this object, just before it is written to the boot image during
	 * the build process.
	 */
	public void verifyBeforeEmit() {}
	
	/**
	 * This method is called in the build process to get extra information
	 * on this object. This extra information is added to the listing file.
	 * @return String
	 */
	public String getExtraInfo() {
		return null;
	}
}
