/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class URLPluginLoader extends PluginLoader {
    
    private final URL baseUrl;
    
    /**
     * Initialize this instance.
     * @param baseUrl
     */
    public URLPluginLoader(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @see org.jnode.plugin.PluginLoader#getPluginStream(java.lang.String, java.lang.String)
     */
    public InputStream getPluginStream(String pluginId, String pluginVersion) {
        try {
            final URL url = new URL(baseUrl, getPluginFileName(pluginId, pluginVersion));
            System.out.println("url=" + url);
            return url.openStream();
        } catch (IOException ex) {
            return null;
        }
    }
}
