/*
 * $Id$
 */
package org.jnode.test.net;

import java.io.InputStream;
import java.net.URL;

/**
 * @author epr
 */
public class URLTest {

	public static void main(String[] args) throws Exception {

		final URL url = new URL((args.length > 0) ? args[0] : "http://192.168.200.1");
		final InputStream is = url.openConnection().getInputStream();
		try {
			int ch;
			final StringBuffer buf = new StringBuffer();
			while ((ch = is.read()) >= 0) {
				buf.append((char) ch);
			}
			System.out.println("Result:\n" + buf);
		} finally {
			is.close();
		}
	}
}
