/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package org.jnode.plugin;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;

import org.jnode.util.FileUtils;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class URLPluginLoader extends PluginLoader {

    private final URL baseUrl;

    /**
     * Initialize this instance.
     *
     * @param baseUrl
     */
    public URLPluginLoader(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @see org.jnode.plugin.PluginLoader#getPluginBuffer(String, String)
     */
    public ByteBuffer getPluginBuffer(String pluginId, String pluginVersion) {
        try {
            final URL url = new URL(baseUrl, getPluginFileName(pluginId, pluginVersion));
            System.out.println("url=" + url);
            return ByteBuffer.wrap(FileUtils.load(url.openStream(), true));
        } catch (IOException ex) {
            return null;
        }
    }
}
