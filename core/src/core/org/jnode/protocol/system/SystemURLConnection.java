/*
 * $Id$
 */
package org.jnode.protocol.system;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.jnode.vm.VmSystem;

/**
 * @author epr
 */
public class SystemURLConnection extends URLConnection {

	private final String path;

	/**
	 * @param url
	 */
	public SystemURLConnection(URL url) {
		super(url);
		this.path = url.getPath();
	}

	/**
	 * @see java.net.URLConnection#connect()
	 */
	public void connect() throws IOException {
		/* Do nothing */
	}

	/**
	 * @see java.net.URLConnection#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		InputStream is = VmSystem.getSystemClassLoader().getResourceAsStream(path);
		if (is == null) {
			throw new IOException("Unknown system resource " + path);
		} else {
			return is;
		}
	}
}
