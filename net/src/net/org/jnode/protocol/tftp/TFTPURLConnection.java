/*
 * $Id$
 */
package org.jnode.protocol.tftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;

/**
 * @author epr
 */
public class TFTPURLConnection extends URLConnection {

	private final String host;
	private final String path;

	/**
	 * @param url
	 */
	public TFTPURLConnection(URL url) {
		super(url);
		this.host = url.getHost();
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
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		final TFTPClient tftp = new TFTPClient();
		final InetAddress hostAddr = InetAddress.getByName(host);
		tftp.open(TFTP.DEFAULT_PORT);
		try {
			//Syslog.debug("Getting " + path + " from " + hostAddr);
			tftp.receiveFile(path, TFTP.BINARY_MODE, os, hostAddr);
		} finally {
			tftp.close();
		}
		return new ByteArrayInputStream(os.toByteArray());
	}

}
