/*
 * $Id$
 */
package org.jnode.protocol.ftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

/**
 * @author markhale
 */
public class FTPURLConnection extends URLConnection {

	private final String host;
	private final String path;
	private final String username;
	private final String password;

	/**
	 * @param url
	 */
	public FTPURLConnection(URL url) {
		super(url);
		this.host = url.getHost();
		this.path = url.getPath();
		String userinfo = url.getUserInfo();
		if(userinfo != null) {
			final int pos = userinfo.indexOf(':');
			if(pos != -1) {
				username = userinfo.substring(0, pos);
				password = userinfo.substring(pos+1);
			} else {
				username = userinfo;
				password = "";
			}
		} else {
			username = "anonymous";
			password = "jnode-user@jnode.org";
		}
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
		FTPClient client = new FTPClient();
		client.connect(host);
		String replyString = client.getReplyString();
		int replyCode = client.getReplyCode();
		if(!FTPReply.isPositiveCompletion(replyCode)) {
			client.disconnect();
			throw new IOException(replyString);
		}
		if(!client.login(username, password)) {
			replyString = client.getReplyString();
			client.logout();
			throw new IOException(replyString);
		}
		client.setFileType(FTP.IMAGE_FILE_TYPE);
		client.enterLocalPassiveMode();

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			//Syslog.debug("Getting " + path + " from " + host);
			/*boolean success =*/ client.retrieveFile(path, os);
			client.logout();
		} finally {
			client.disconnect();
		}
		return new ByteArrayInputStream(os.toByteArray());
	}

}
