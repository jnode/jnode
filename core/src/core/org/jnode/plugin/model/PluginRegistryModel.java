/*
 * $Id$
 */
package org.jnode.plugin.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jnode.plugin.Extension;
import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginLoader;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.PluginSecurityConstants;
import org.jnode.util.BootableHashMap;
import org.jnode.vm.VmSystemObject;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author epr
 */
public class PluginRegistryModel extends VmSystemObject implements
        PluginRegistry {

    /** A map of all descriptors (id, descriptor) */
    private final BootableHashMap descriptorMap;

    /** A map off all extensionpoints (id, ep) */
    private final BootableHashMap extensionPoints;

    private transient PluginsClassLoader classLoader;

    /**
     * Initialize this instance.
     * 
     * @param pluginFiles
     */
    public PluginRegistryModel(URL[] pluginFiles) throws PluginException {
        this.extensionPoints = new BootableHashMap();
        this.descriptorMap = new BootableHashMap();
        final List descriptors = loadDescriptors(pluginFiles);
        resolveDescriptors(descriptors);
    }

    /**
     * Gets the descriptor of the plugin with the given id.
     * 
     * @param pluginId
     * @return The plugin descriptor found, or null if not found
     */
    public PluginDescriptor getPluginDescriptor(String pluginId) {
        return (PluginDescriptor) descriptorMap.get(pluginId);
    }

    /**
     * Gets the extension point with the given id.
     * 
     * @param id
     * @return The extension point found, or null if not found
     */
    public ExtensionPoint getExtensionPoint(String id) {
        return (ExtensionPoint) extensionPoints.get(id);
    }

    /**
     * Returns an iterator to iterate over all PluginDescriptor's.
     * 
     * @return Iterator&lt;PluginDescriptor&gt;
     */
    public Iterator getDescriptorIterator() {
        return descriptorMap.values().iterator();
    }

    /**
     * Load all plugin descriptors.
     * 
     * @param pluginUrls
     */
    private List loadDescriptors(URL[] pluginUrls) throws PluginException {
        final int max = pluginUrls.length;
        final ArrayList descriptors = new ArrayList(max);
        
        for (int i = 0; i < max; i++) {
            //System.out.println(pluginUrls[i]);
            descriptors.add(loadPlugin(pluginUrls[ i], false));
        }
        return descriptors;
    }

    /**
     * Resolve all plugin descriptors.
     */
    public void resolveDescriptors() throws PluginException {
        for (Iterator i = descriptorMap.values().iterator(); i.hasNext();) {
            final PluginDescriptorModel descr = (PluginDescriptorModel) i
                    .next();
            descr.resolve(this);
        }
    }

    /**
     * Resolve all given plugin descriptors in such an order that the
     * depencencies are dealt with 
     */
    public void resolveDescriptors(Collection descriptors) throws PluginException {
    	while (!descriptors.isEmpty()) {
    		boolean change = false;
    		for (Iterator i = descriptors.iterator(); i.hasNext(); ) {
    			final PluginDescriptorModel descr = (PluginDescriptorModel)i.next();
    			if (canResolve(descr)) {
    				descr.resolve(this);
    				i.remove();
    				change = true;
    			}
    		}
    		if (!change) {
    			throw new PluginException("Failed to resolve all descriptors");
    		}
    	}
    }
    
    private final boolean canResolve(PluginDescriptorModel descr) {
    	final PluginPrerequisite reqs[] = descr.getPrerequisites();
    	final int length = reqs.length;
    	for (int i = 0; i < length; i++) {
    		if (getPluginDescriptor(reqs[i].getPluginId()) == null) {
    			return false;
    		}
    	}
    	final Extension[] exts = descr.getExtensions();
    	final int extsLength = exts.length;
    	for (int i = 0; i < extsLength; i++) {
    		if (getPluginDescriptor(exts[i].getExtensionPointPluginId()) == null) {
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * Register a plugin descriptor.
     * 
     * @param descr
     */
    protected synchronized void registerPlugin(PluginDescriptorModel descr)
            throws PluginException {
        final String id = descr.getId();
        if (descriptorMap.containsKey(id)) { throw new PluginException(
                "Duplicate plugin " + id); }
        descriptorMap.put(id, descr);
    }

    /**
     * Register a plugin descriptor.
     * 
     * @param descr
     */
    protected synchronized void unregisterPlugin(PluginDescriptorModel descr)
            throws PluginException {
        final String id = descr.getId();
        descriptorMap.remove(id);
    }

    /**
     * Register a known extension point.
     * 
     * @param ep
     */
    protected synchronized void registerExtensionPoint(ExtensionPoint ep)
            throws PluginException {
        final BootableHashMap epMap = this.extensionPoints;
        if (epMap.containsKey(ep.getUniqueIdentifier())) { throw new PluginException(
                "Duplicate extension point " + ep.getUniqueIdentifier()); }
        epMap.put(ep.getUniqueIdentifier(), ep);
    }

    /**
     * Unregister a known extension point.
     * 
     * @param ep
     */
    protected synchronized void unregisterExtensionPoint(ExtensionPoint ep)
            throws PluginException {
        final BootableHashMap epMap = this.extensionPoints;
        epMap.remove(ep.getUniqueIdentifier());
    }

    /**
     * Load a plugin from a given URL. This will not activate the plugin.
     * 
     * @param pluginUrl
     * @return The descriptor of the loaded plugin.
     * @throws PluginException
     */
    private PluginDescriptor loadPlugin(final URL pluginUrl, boolean resolve)
            throws PluginException {
        final PluginRegistryModel registry = this;
        final PluginJar pluginJar;
        try {
            pluginJar = (PluginJar) AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {

                        public Object run() throws PluginException, IOException {
                            return new PluginJar(registry, pluginUrl);
                        }
                    });
        } catch (PrivilegedActionException pax) {
            final Throwable ex = pax.getException();
            if (ex instanceof PluginException) {
                throw (PluginException) ex;
            } else {
                throw new PluginException(ex);
            }
        }
        final PluginDescriptorModel descr = pluginJar.getDescriptorModel();
        if (resolve) {
            descr.resolve(this);
        }
        return descr;
    }

	/**
	 * Load a plugin from a given loader.
	 * 
	 * @param loader
	 * @param pluginId
	 * @param pluginVersion 
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(final PluginLoader loader, final String pluginId, final String pluginVersion) throws PluginException {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(PluginSecurityConstants.LOAD_PERM);
	    }
		// Load the requested plugin
		final HashMap descriptors = new HashMap();
		final PluginDescriptor descr = loadPlugin(loader, pluginId, pluginVersion, false);
		descriptors.put(descr.getId(), descr);
		// Load the dependent plugins
		loadDependencies(loader, descr, descriptors);
		
		// Resolve the loaded descriptors.
		resolveDescriptors(descriptors.values());
		return descr;
	}
	
	private final void loadDependencies(PluginLoader loader, PluginDescriptor descr, Map descriptors) throws PluginException {
		// Prerequisites
		final PluginPrerequisite reqs[] = descr.getPrerequisites();
		final int reqLength = reqs.length;
		for (int i = 0; i < reqLength; i++) {
			final PluginPrerequisite req = reqs[i];
			final String id = req.getPluginId();
			final String version = req.getPluginVersion();
			loadDependency(loader, id, version, descriptors);
		}
		// Extensions
		final Extension[] exts = descr.getExtensions();
		final int extLength = exts.length;
		for (int i = 0; i < extLength; i++) {
			final Extension ext = exts[i];
			final String id = ext.getExtensionPointPluginId();
			loadDependency(loader, id, descr.getVersion(), descriptors);
		}
	}
	
	private final void loadDependency(PluginLoader loader, String id, String version, Map descriptors) throws PluginException {
		if (getPluginDescriptor(id) != null) {
			return;
		}
		if (descriptors.containsKey(id)) {
			return;
		}
		final PluginDescriptor descr = loadPlugin(loader, id, version, false);
		descriptors.put(descr.getId(), descr);
		loadDependencies(loader, descr, descriptors);
	}
	
	/**
	 * Load a plugin from a given loader.
	 * 
	 * @param loader
	 * @param pluginId
	 * @param pluginVersion 
	 * @return The descriptor of the loaded plugin.
	 * @throws PluginException
	 */
	public PluginDescriptor loadPlugin(final PluginLoader loader, final String pluginId, final String pluginVersion, boolean resolve) throws PluginException {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(PluginSecurityConstants.LOAD_PERM);
	    }
        final PluginRegistryModel registry = this;
        final PluginJar pluginJar;
        try {
            pluginJar = (PluginJar) AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {

                        public Object run() throws PluginException, IOException {
                            final InputStream is = loader.getPluginStream(pluginId, pluginVersion);
                            if (is == null) {
                                throw new PluginException("Plugin " + pluginId + ", version " + pluginVersion + " not found");
                            }
                            return new PluginJar(registry, is, null);
                        }
                    });
        } catch (PrivilegedActionException pax) {
            final Throwable ex = pax.getException();
            if (ex instanceof PluginException) {
                throw (PluginException) ex;
            } else {
                throw new PluginException(ex);
            }
        }
        final PluginDescriptorModel descr = pluginJar.getDescriptorModel();
        if (resolve) {
            descr.resolve(this);
        }
        return descr;
	}
	
    /**
     * Remove the plugin with the given id from this registry.
     * 
     * @param pluginId
     * @throws PluginException
     */
    public synchronized void unloadPlugin(String pluginId)
            throws PluginException {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(PluginSecurityConstants.UNLOAD_PERM);
	    }
        final PluginDescriptorModel descr = (PluginDescriptorModel)getPluginDescriptor(pluginId);
        if (descr != null) {
            if (descr.isSystemPlugin()) { throw new PluginException(
                    "Cannot unload a system plugin"); }
            
            // Unload all plugins that depend on this plugin
            for (Iterator i = descriptorMap.values().iterator(); i.hasNext();) {
                final PluginDescriptor dep = (PluginDescriptor) i.next();
                if (dep.depends(pluginId)) {
                	unloadPlugin(dep.getId());
                }
            }

            // Now remove it
            descr.unresolve(this);
        }
    }

    /**
     * Gets the classloader that loads classes from all loaded plugins.
     * 
     * @return ClassLoader
     */
    public ClassLoader getPluginsClassLoader() {
        if (classLoader == null) {
            classLoader = new PluginsClassLoader(this);
        }
        return classLoader;
    }

    static class DTDResolver implements EntityResolver {

        /**
         * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
         *      java.lang.String)
         */
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            if ((systemId != null) && systemId.endsWith("jnode.dtd")) {
                return new InputSource(getClass().getResourceAsStream(
                        "/jnode.dtd"));
            } else {
                return null;
            }
        }

    }
}