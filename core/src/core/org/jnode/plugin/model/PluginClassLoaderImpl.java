/*
 * $Id$
 */
package org.jnode.plugin.model;

import gnu.java.security.actions.GetPolicyAction;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Policy;
import java.security.ProtectionDomain;

import org.jnode.plugin.PluginClassLoader;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.BootLog;
import org.jnode.util.FileUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PluginClassLoaderImpl extends PluginClassLoader {

    /** The registry */
    private final PluginRegistryModel registry;
    /** The descriptor */
    private final PluginDescriptorModel descriptor;
	/** The plugin jar file */
	private final PluginJar jar;
	/** The classloaders of the prerequisite plugins */
	private final PluginClassLoaderImpl[] prerequisiteLoaders;

	/**
	 * Initialize this instance.
	 * 
	 * @param jar
	 */
	public PluginClassLoaderImpl(PluginRegistryModel registry, PluginDescriptorModel descr, PluginJar jar, PluginClassLoaderImpl[] prerequisiteLoaders) {
	    this.registry = registry;
	    this.descriptor = descr;
		this.jar = jar;
		this.prerequisiteLoaders = prerequisiteLoaders;
	}

	/**
	 * Finds the specified class. This method should be overridden by class loader implementations that follow the new delegation model for loading classes, and will be called by the loadClass method
	 * after checking the parent class loader for the requested class. The default implementation throws ClassNotFoundException.
	 * 
	 * @param name
	 * @return Class
	 * @throws ClassNotFoundException
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	protected final Class findClass(String name) throws ClassNotFoundException {
		final Class cls = findPluginClass(name);
		if (cls != null) {
			return cls;
		} else {
			// Not found
			throw new ClassNotFoundException(name);
		}
	}

	/**
	 * Finds the specified class. This method should be overridden by class loader implementations that follow the new delegation model for loading classes, and will be called by the loadClass method
	 * after checking the parent class loader for the requested class. The default implementation throws ClassNotFoundException.
	 * 
	 * @param name
	 * @return Class The class, or null if not found.
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	private final Class findPluginClass(String name) {
		// Try the prerequisite loaders first
		final int max = prerequisiteLoaders.length;
		for (int i = 0; i < max; i++) {
			final PluginClassLoaderImpl cl = prerequisiteLoaders[i];
			if (cl != null) {
				final Class cls = cl.findPluginClass(name);
				if (cls != null) {
					return cls;
				}
			}
		}
		// Try the loaded classes first
		final Class loadedCls = findLoadedClass(name);
		if (loadedCls != null) {
			return loadedCls;
		}
		// Look for it in our own jar
		final byte[] b = loadClassData(name);
		if (b != null) {
		    // We're are now going to use one of my classes, 
		    // so make sure that my plugin has been started.
		    try {
	            startPlugin();
	        } catch (PluginException ex) {
	            BootLog.error("Error starting plugin", ex);
	        }
		    final URL sourceUrl = jar.getResource(name.replace('.', '/') + ".class");
		    final CodeSource cs = new CodeSource(sourceUrl, null);
		    final Policy policy = (Policy)AccessController.doPrivileged(GetPolicyAction.getInstance());
		    final ProtectionDomain pd = new ProtectionDomain(cs, policy.getPermissions(cs));
			final Class cls = defineClass(name, b, 0, b.length, pd);
			resolveClass(cls);
			return cls;
		} else {
			return null;
		}
	}

	/**
	 * Does this classloader contain the specified class.
	 * 
	 * @return boolean
	 */
	protected final boolean containsClass(String name) {
		return jar.containsResource(name.replace('.', '/') + ".class");
	}

	/**
	 * Finds the resource with the given name. Class loader implementations should override this method to specify where to find resources.
	 * 
	 * @param name
	 * @return URL
	 * @see java.lang.ClassLoader#findResource(java.lang.String)
	 */
	protected final URL findResource(String name) {
		// Try the prerequisite loaders first
		final int max = prerequisiteLoaders.length;
		for (int i = 0; i < max; i++) {
			final PluginClassLoaderImpl cl = prerequisiteLoaders[i];
			if (cl != null) {
				final URL url = cl.findResource(name);
				if (url != null) {
					return url;
				}
			}
		}
		// Not found, try my own plugin
		//System.out.println("Try resource " + name + " on " + jar.getDescriptor().getId());
		final URL url = jar.getResource(name);
		if (url != null) {
		    try {
		        startPlugin();
		    } catch (PluginException ex) {
		        BootLog.error("Cannot start plugin", ex);
		    }
		}
		return url;
	}

	/**
	 * Try to load the data of a class with a given name.
	 * 
	 * @param name
	 * @return The loaded class data or null if not found.
	 */
	private final byte[] loadClassData(String name) {
		final InputStream is = jar.getResourceAsStream(name.replace('.', '/') + ".class");
		if (is == null) {
			return null;
		}
		try {
			return FileUtils.load(is, true);
		} catch (IOException ex) {
			return null;
		}
	}
	
	/**
	 * Make sure that the plugin gets started.
	 * This method ensures that this classloader can be used to start
	 * the plugin.
	 */
	private final void startPlugin() 
	throws PluginException {
        descriptor.startPlugin(registry);
	}
	
    /**
     * @see org.jnode.plugin.PluginClassLoader#getDeclaringPluginDescriptor()
     */
    public PluginDescriptor getDeclaringPluginDescriptor() {
        return descriptor;
    }
}
