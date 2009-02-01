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
 
package org.jnode.protocol.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.naming.NameNotFoundException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.model.PluginDescriptorModel;
import org.jnode.plugin.model.PluginJar;
import org.jnode.util.ByteBufferInputStream;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginURLConnection extends URLConnection {

    private final String pluginId;
    private final String path;
    private transient PluginJar jarFile;

    /**
     * @param url
     */
    public PluginURLConnection(URL url) throws MalformedURLException {
        super(url);

        final String protoPrefix = "plugin:";
        final String fullUrl = url.toExternalForm();
        if (!fullUrl.startsWith(protoPrefix)) {
            throw new MalformedURLException("plugin protocol expected");
        }

        final int idx = fullUrl.indexOf("!/");
        if (idx < 0) {
            throw new MalformedURLException("!/ expected");
        }
        this.pluginId = fullUrl.substring(protoPrefix.length(), idx);
        this.path = fullUrl.substring(idx + 2);
    }

    /**
     * @see java.net.URLConnection#connect()
     */
    public void connect() throws IOException {
        try {
            final PluginManager pluginMgr = InitialNaming.lookup(PluginManager.NAME);
            final PluginDescriptor descr = pluginMgr.getRegistry().getPluginDescriptor(pluginId);
            if (descr == null) {
                throw new IOException("Plugin " + pluginId + " not found");
            }
            this.jarFile = ((PluginDescriptorModel) descr).getJarFile();
            if (this.jarFile == null) {
                throw new IOException("Plugin jarfile not found");
            }
        } catch (NameNotFoundException ex) {
            final IOException ioe = new IOException("Cannot find plugin manager");
            ioe.initCause(ex);
            throw ioe;
        }
    }

    /**
     * @see java.net.URLConnection#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        if (jarFile == null) {
            connect();
        }
        return new ByteBufferInputStream(jarFile.getResourceAsBuffer(path));
    }
}
