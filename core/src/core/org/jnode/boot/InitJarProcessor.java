/*
 * $Id$
 */
package org.jnode.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginLoader;
import org.jnode.plugin.model.PluginRegistryModel;
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
	public List loadPlugins(PluginRegistryModel piRegistry) {
		if (jarFile == null) {
			return null;
		}

		final InitJarPluginLoader loader = new InitJarPluginLoader();
		final ArrayList descriptors = new ArrayList();
		for (Enumeration e = jarFile.entries(); e.hasMoreElements();) {
			final JarEntry entry = (JarEntry) e.nextElement();
			if (entry.getName().endsWith(".jar")) {
				try {
					// Load it
				    loader.setIs(jarFile.getInputStream(entry));
					final PluginDescriptor descr = piRegistry.loadPlugin(loader, "", "", false);
					descriptors.add(descr);
				} catch (IOException ex) {
					BootLog.error("Cannot load " + entry.getName(), ex);
				} catch (PluginException ex) {
					BootLog.error("Cannot load " + entry.getName(), ex);
				}
			}
		}
		return descriptors;
	}
	
	static class InitJarPluginLoader extends PluginLoader {
	    private InputStream is;
	    public InitJarPluginLoader() {
	    }
	    
	    
	    
        /**
         * @see org.jnode.plugin.PluginLoader#getPluginStream(java.lang.String, java.lang.String)
         */
        public InputStream getPluginStream(String pluginId, String pluginVersion) {
            return is;
        }
        /**
         * @param is The is to set.
         */
        final void setIs(InputStream is) {
            this.is = is;
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