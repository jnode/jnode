/*
 * $Id$
 */
package org.jnode.protocol.ftp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author markhale
 */
public class Handler extends URLStreamHandler {

	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new FTPURLConnection(url);
	}

}
