/*
 * $Id$
 */
package org.jnode.protocol.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Handler extends URLStreamHandler {

	/**
	 * @see java.net.URLStreamHandler#openConnection(java.net.URL)
	 */
	protected URLConnection openConnection(URL url) throws IOException {
		return new PluginURLConnection(url);
	}

}
