/*
 * $Id$
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
			final PluginManager pluginMgr = (PluginManager) InitialNaming.lookup(PluginManager.NAME);
			final PluginDescriptor descr = pluginMgr.getRegistry().getPluginDescriptor(pluginId);
			if (descr == null) {
				throw new IOException("Plugin " + pluginId + " not found");
			}
			this.jarFile = ((PluginDescriptorModel) descr).getJarFile();
			if (this.jarFile == null) {
				throw new IOException("Plugin jarfile not found");
			}
		} catch (NameNotFoundException ex) {
			throw new IOException("Cannot find plugin manager", ex);
		}
	}

	/**
	 * @see java.net.URLConnection#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		if (jarFile == null) {
			connect();
		}
		return jarFile.getResourceAsStream(path);
	}
}
