/*
 * $Id$
 */
package org.jnode.boot;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginRegistry;
import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.util.MemoryResourceRandomAccessBuffer;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class InitJarProcessor {

	private final JarFile jarFile;

	/**
	 * Initialize this instance.
	 * 
	 * @param initJarRes
	 */
	public InitJarProcessor(MemoryResource initJarRes) {
		JarFile jarFile = null;
		if (initJarRes != null) {
			try {
				jarFile = new JarFile(new MemoryResourceRandomAccessBuffer(initJarRes), "intjar");
			} catch (IOException ex) {
				BootLog.error("Cannot instantiate initjar", ex);
			}
		}
		this.jarFile = jarFile;
	}

	/**
	 * Load all plugins found in the initjar.
	 * 
	 * @param piRegistry
	 */
	public void loadPlugins(PluginRegistry piRegistry) {
		if (jarFile == null) {
			return;
		}

		for (Enumeration e = jarFile.entries(); e.hasMoreElements();) {
			final JarEntry entry = (JarEntry) e.nextElement();
			if (entry.getName().endsWith(".jar")) {
				try {
					// Load it
					piRegistry.loadPlugin(jarFile.getInputStream(entry));
				} catch (IOException ex) {
					BootLog.error("Cannot load " + entry.getName(), ex);
				} catch (PluginException ex) {
					BootLog.error("Cannot load " + entry.getName(), ex);
				}
			}
		}
	}
	
	/**
	 * Gets the name of the Main-Class from the initjar manifest.
	 * 
	 * @return The classname of the main class, or null.
	 */
	public String getMainClassName() {
		if (jarFile != null) {
			try {
				return jarFile.getManifest().getMainAttributes().getValue("Main-Class");
			} catch (IOException ex) {
				BootLog.error("Cannot obtain Main-Class attribute", ex);
			}
		}
		return null;
	}
}