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
            tftp.receiveFile(path, TFTP.BINARY_MODE, os, hostAddr);
        } finally {
            tftp.close();
        }
        return new ByteArrayInputStream(os.toByteArray());
    }
}
