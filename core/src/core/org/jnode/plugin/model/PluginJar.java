/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Hashtable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import nanoxml.XMLElement;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.FileUtils;
import org.jnode.util.OsUtils;
import org.jnode.vm.BootableObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PluginJar implements BootableObject {

	/** The descriptor of this file */
	private final PluginDescriptor descriptor;
	/** The bytes of the plugin jar file */
	private final byte[] pluginJarData;
	/** The cached JarFile instance */
	private transient JarFile pluginJar;

	/**
	 * Initialize this instance
	 * 
	 * @param registry
	 * @param pluginUrl
	 */
	public PluginJar(PluginRegistryModel registry, URL pluginUrl) throws PluginException, IOException {
		this(registry, pluginUrl.openStream(), null);
	}

	/**
	 * Initialize this instance
	 * 
	 * @param registry
	 * @param pluginIs
	 */
	public PluginJar(PluginRegistryModel registry, InputStream pluginIs, URL pluginUrl) throws PluginException {

		// Load the plugin into memory
		final byte[] pluginJarData;
		try {
			final byte[] buf = new byte[4096];
			final ByteArrayOutputStream pluginOs = new ByteArrayOutputStream();
			FileUtils.copy(pluginIs, pluginOs, buf, true);
			pluginJarData = pluginOs.toByteArray();
		} catch (IOException ex) {
			throw new PluginException("Error loading jarfile", ex);
		}

		final XMLElement root;
		try {
			// Not find the plugin.xml
			final JarFile jarFile = getJar(pluginUrl, pluginJarData);
			JarEntry entry = jarFile.getJarEntry("plugin.xml");
			if (entry == null) {
				throw new PluginException("plugin.xml not found in jar file");
			}

			// Now parse plugin.xml
			root = new XMLElement(new Hashtable(), true, false);
			final Reader r = new InputStreamReader(jarFile.getInputStream(entry));
			try {
				root.parseFromReader(r);
			} finally {
				r.close();
			}
		} catch (IOException ex) {
			throw new PluginException("Plugin " + pluginUrl, ex);
		}
		if (!root.getName().equals("plugin")) {
			throw new PluginException("plugin element expected");
		}
		this.descriptor = new PluginDescriptorModel(registry, this, root);
		if (this.descriptor.isSystemPlugin()) {
			this.pluginJarData = null;
		} else {
			this.pluginJarData = pluginJarData;
		}
	}

	/**
	 * Does this jar-file contain the resource with the given name.
	 * 
	 * @param resourceName
	 * @return boolean
	 */
	public final InputStream getResourceAsStream(String resourceName) {
		try {
			final JarFile jarFile = getJar(null, pluginJarData);
			final JarEntry entry = jarFile.getJarEntry(resourceName);
			if (entry == null) {
				return null;
			} else {
				return jarFile.getInputStream(entry);
			}
		} catch (IOException ex) {
			return null;
		}
	}

	/**
	 * Does this jar-file contain the resource with the given name.
	 * 
	 * @param resourceName
	 * @return boolean
	 */
	public final boolean containsResource(String resourceName) {
		try {
			final JarFile jarFile = getJar(null, pluginJarData);
			final JarEntry entry = jarFile.getJarEntry(resourceName);
			return (entry != null);
		} catch (IOException ex) {
			return false;
		}
	}

	/**
	 * Does this jar-file contain the resource with the given name.
	 * 
	 * @param resourceName
	 * @return boolean
	 */
	public final URL getResource(String resourceName) {
		if (resourceName.startsWith("/")) {
			resourceName = resourceName.substring(1);
		}
		try {
			final JarFile jarFile = getJar(null, pluginJarData);
			final JarEntry entry = jarFile.getJarEntry(resourceName);
			if (entry == null) {
				return null;
			} else {
				final String id = descriptor.getId();
				return new URL("plugin:" + id + "!/" + resourceName);
			}
		} catch (IOException ex) {
			System.out.println("ioex: " + ex.getMessage());
			return null;
		}
	}

	/**
	 * Gets the descriptor of this plugin-jar file.
	 * 
	 * @return Returns the descriptor.
	 */
	public final PluginDescriptor getDescriptor() {
		return this.descriptor;
	}

	/**
	 * Gets the JarFile of this pluginjar.
	 * 
	 * @param pluginUrl
	 *            Can be null
	 * @return The plugin jarfile.
	 * @throws IOException
	 */
	private JarFile getJar(URL pluginUrl, byte[] pluginJarData) throws IOException {
		if (pluginJar == null) {
			final String protocol = (pluginUrl != null) ? pluginUrl.getProtocol() : "";
			if (protocol.equals("file")) {
				pluginJar = new JarFile(pluginUrl.getFile());
			} else if (OsUtils.isJNode()) {
				pluginJar = new JarFile(pluginJarData);
			} else {
				final File tmp = File.createTempFile("jnode", "jartmp");
				final FileOutputStream fos = new FileOutputStream(tmp);
				fos.write(pluginJarData);
				fos.close();
				pluginJar = new JarFile(tmp);
				tmp.deleteOnExit();
			}
		}
		return pluginJar;
	}
}
