/*
 * $Id$
 *
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
 
package org.jnode.protocol.nfs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.acplt.oncrpc.OncRpcException;
import org.acplt.oncrpc.OncRpcPortmapClient;
import org.acplt.oncrpc.OncRpcProtocols;
import org.acplt.oncrpc.OncRpcServerIdent;
import org.jnode.protocol.nfs.nfs2.NFS2URLConnection;

/**
 * @author Andrei Dore
 */
public class Handler extends URLStreamHandler {

    /**
     * @see java.net.URLStreamHandler#openConnection(java.net.URL)
     */
    protected URLConnection openConnection(URL url) throws IOException {
        OncRpcPortmapClient client = null;
        int version = 0;
        try {
            client = new OncRpcPortmapClient(
                    InetAddress.getByName(url.getHost()), OncRpcProtocols.ONCRPC_TCP);
            OncRpcServerIdent[] servers = client.listServers();
            for (int i = 0; i < servers.length; i++) {
                OncRpcServerIdent server = servers[i];
                if (server.program == 100003 && server.version > version) {
                    version = server.version;
                }
            }
        } catch (OncRpcException e) {
            throw new IOException(e);
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        switch (version) {
            case 2:
            case 3:
            case 4:
                return new NFS2URLConnection(url);
            default:
                throw new IOException("The host " + url.getHost() + " doesn't have a nfs service.");
        }
    }
}
