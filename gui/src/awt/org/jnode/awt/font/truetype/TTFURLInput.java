/*
 * $Id$
 */
package org.jnode.awt.font.truetype;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author epr
 */
public class TTFURLInput extends TTFMemoryInput {

	/**
	 * @param url
	 * @throws IOException
	 */
	public TTFURLInput(URL url) 
	throws IOException {
		super(getData(url));
	}

	private static byte[] getData(URL url) 
	throws IOException {
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final InputStream is = url.openStream();
		final byte[] buf = new byte[4096];
		int len;
		while ((len = is.read(buf)) > 0) {
			os.write(buf, 0, len);
		}
		is.close();
		System.out.println("Got " + os.size() + " bytes");
		return os.toByteArray();
	}
}
