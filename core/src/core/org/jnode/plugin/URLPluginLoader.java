/*
 * $Id$
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
