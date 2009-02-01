/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
        if (userinfo != null) {
            final int pos = userinfo.indexOf(':');
            if (pos != -1) {
                username = userinfo.substring(0, pos);
                password = userinfo.substring(pos + 1);
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
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            client.disconnect();
            throw new IOException(replyString);
        }
        if (!client.login(username, password)) {
            replyString = client.getReplyString();
            client.logout();
            throw new IOException(replyString);
        }
        client.setFileType(FTP.IMAGE_FILE_TYPE);
        client.enterLocalPassiveMode();

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            client.retrieveFile(path, os);
            client.logout();
        } finally {
            client.disconnect();
        }
        return new ByteArrayInputStream(os.toByteArray());
    }

}
