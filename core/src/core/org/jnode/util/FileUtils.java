/**
 * $Id$
 */
package org.jnode.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * <description>
 * 
 * @author epr
 */
public class FileUtils {

	/**
	 * Copy dest.length bytes from the inputstream into the dest bytearray.
	 * @param is
	 * @param dest
	 * @throws IOException
	 */
	public static void copy(InputStream is, byte[] dest)
	throws IOException {
		int len = dest.length;
		int ofs = 0;
		while (len > 0) {
			int size = is.read(dest, ofs, len);
			ofs += size;
			len -= size;
		} 
	}

}
