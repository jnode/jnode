/**
 * $Id$
 */
package org.jnode.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

	/**
	 * Copy the contents of is to os.
	 * @param is
	 * @param os
	 * @param buf Can be null
	 * @param close If true, is is closed after the copy.
	 * @throws IOException
	 */
	public static final void copy(InputStream is, OutputStream os, byte[] buf, boolean close) throws IOException {
		int len;
		if (buf == null) {
			buf = new byte[4096];
		}
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
		os.flush();
		if (close) {
			is.close();
		}
	}

	/**
	 * Copy the contents of is to the returned byte array.
	 * @param is
	 * @param close If true, is is closed after the copy.
	 * @throws IOException
	 */
	public static final byte[] load(InputStream is, boolean close) throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		copy(is, os, null, close);
		return os.toByteArray();
	}
}
