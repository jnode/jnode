/*
 * $Id $
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.protocol.nfs.nfs2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Andrei Dore
 */
public class NFS2URLConnection extends URLConnection {

    /**
     * @param url
     */
    public NFS2URLConnection(URL url) {
        super(url);

    }

    /**
     * @see java.net.URLConnection#getInputStream()
     */
    public InputStream getInputStream() throws IOException {

        return new NFS2InputStream(getURL());

    }

    @Override
    public OutputStream getOutputStream() throws IOException {

        return new NFS2OutputStream(getURL());

    }

    @Override
    public void connect() throws IOException {

    }

}
