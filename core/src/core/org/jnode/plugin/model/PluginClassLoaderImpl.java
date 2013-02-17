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
 
package org.jnode.plugin.model;

import gnu.java.security.action.GetPolicyAction;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.jnode.bootlog.BootLogInstance;
import org.jnode.plugin.PluginClassLoader;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.vm.ResourceLoader;
import org.jnode.vm.classmgr.VmClassLoader;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class PluginClassLoaderImpl extends ClassLoader implements PluginClassLoader {

    /**
     * The registry
     */
    private final PluginRegistryModel registry;

    /**
     * The descriptor
     */
    private final PluginDescriptorModel descriptor;

    /**
     * The plugin jar file
     */
    private final PluginJar jar;

    /**
     * The classloaders of the prerequisite plugins
     */
    private final PluginClassLoaderImpl[] prerequisiteLoaders;

    /**
     * Initialize this instance.
     *
     * @param jar
     */
    public PluginClassLoaderImpl(PluginRegistryModel registry,
                                 PluginDescriptorModel descr, PluginJar jar,
                                 PluginClassLoaderImpl[] prerequisiteLoaders) {
        this.registry = registry;
        this.descriptor = descr;
        this.jar = jar;
        this.prerequisiteLoaders = prerequisiteLoaders;
    }

    /**
     * Wrap this {@link ClassLoader} around the given vmClassLoader.
     * Requires special permission.
     * 
     * @param vmClassLoader
     * @param registry
     * @param descr
     * @param jar
     * @param prerequisiteLoaders
     */
    protected PluginClassLoaderImpl(VmClassLoader vmClassLoader, PluginRegistryModel registry,
                                    PluginDescriptorModel descr, PluginJar jar,
                                    PluginClassLoaderImpl[] prerequisiteLoaders) {
        super(ClassLoader.getSystemClassLoader(), vmClassLoader);
        this.registry = registry;
        this.descriptor = descr;
        this.jar = jar;
        this.prerequisiteLoaders = prerequisiteLoaders;
    }

    /**
     * Gets the names of the classes contained in this plugin.
     *
     * @return
     */
    public Set<String> getClassNames() {
        HashSet<String> classNames = new HashSet<String>();
        for (String name : jar.resourceNames()) {
            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
                classNames.add(name.replace('/', '.'));
            }
        }
        return classNames;
    }

    /**
     * Gets the names of the resources contained in this plugin.
     *
     * @return the set of contained resources
     */
    public Collection<String> getResources() {
        return jar.resourceNames();
    }
    
    /**
     * Finds the specified class. This method should be overridden by class
     * loader implementations that follow the new delegation model for loading
     * classes, and will be called by the loadClass method after checking the
     * parent class loader for the requested class. The default implementation
     * throws ClassNotFoundException.
     *
     * @param name
     * @return Class
     * @throws ClassNotFoundException
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    protected final Class<?> findClass(String name) throws ClassNotFoundException {
        final Class<?> cls = findPluginClass(name);
        if (cls != null) {
            return cls;
        } else {
            // Not found
            throw new ClassNotFoundException(name);
        }
    }

    /**
     * Finds the specified class. This method should be overridden by class
     * loader implementations that follow the new delegation model for loading
     * classes, and will be called by the loadClass method after checking the
     * parent class loader for the requested class. The default implementation
     * throws ClassNotFoundException.
     *
     * @param name
     * @return Class The class, or null if not found.
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    private final Class<?> findPluginClass(String name) {
        // Try the prerequisite loaders first
        final int max = prerequisiteLoaders.length;
        for (int i = 0; i < max; i++) {
            final PluginClassLoaderImpl cl = prerequisiteLoaders[i];
            if (cl != null) {
                final Class<?> cls = cl.findPluginClass(name);
                if (cls != null) {
                    return cls;
                }
            }
        }
        // Try the loaded classes first
        final Class<?> loadedCls = findLoadedClass(name);
        if (loadedCls != null) {
            return loadedCls;
        }

        // Look for it in the fragments
        ByteBuffer b = null;
        FragmentDescriptorModel fragment = null;
        for (FragmentDescriptorModel l : descriptor.fragments()) {
            b = loadClassData(l, name);
            if (b != null) {
                fragment = l;
                break;
            }
        }

        // Look for it in our own jar
        if (b == null) {
            b = loadClassData(jar, name);
        }
        if (b != null) {
            // We're are now going to use one of my classes,
            // so make sure that my plugin has been started.
            try {
                startPlugin();
                if (fragment != null) {
                    fragment.startPlugin(registry);
                }
            } catch (PluginException ex) {
                BootLogInstance.get().error("Error starting plugin", ex);
            }

            // Define package (if needed)
            final int lastDotIndex = name.lastIndexOf('.');
            if (lastDotIndex > 0) {
                String packageName = name.substring(0, lastDotIndex);
                if (getPackage(packageName) == null) {
                    String specTitle = null;
                    String specVendor = null;
                    String specVersion = null;
                    String implTitle = descriptor.getName();
                    String implVendor = descriptor.getProviderName();
                    String implVersion = descriptor.getVersion();
                    URL sealed = null;
                    definePackage(packageName, specTitle, specVendor,
                        specVersion, implTitle, implVendor, implVersion,
                        sealed);
                }
            }

            final URL sourceUrl = jar.getResource(name.replace('.', '/')
                + ".class");
            final CodeSource cs = new CodeSource(sourceUrl, (Certificate[]) null);
            final Policy policy = (Policy) AccessController
                .doPrivileged(GetPolicyAction.getInstance());
            final ProtectionDomain pd = new ProtectionDomain(cs, policy
                .getPermissions(cs));
            final Class<?> cls = defineClass(name, b, pd);
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
        final String resName = name.replace('.', '/') + ".class";
        if (jar.containsResource(resName)) {
            return true;
        }
        for (ResourceLoader l : descriptor.fragments()) {
            if (l.containsResource(resName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.
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

        // Try the fragments
        URL url = null;
        FragmentDescriptorModel fragment = null;
        for (FragmentDescriptorModel f : descriptor.fragments()) {
            url = f.getResource(name);
            if (url != null) {
                fragment = f;
                break;
            }
        }

        // Not found, try my own plugin
        // System.out.println("Try resource " + name + " on " +
        // jar.getDescriptor().getId());
        if (url == null) {
            url = jar.getResource(name);
        }
        if (url != null) {
            try {
                startPlugin();
                if (fragment != null) {
                    fragment.startPlugin(registry);
                }
            } catch (PluginException ex) {
                BootLogInstance.get().error("Cannot start plugin", ex);
            }
        }
        return url;
    }

    public Enumeration<?> getResources(String name) {
        System.err.println("getResources " + name);
        final List<URL> urls = new ArrayList<URL>();

        //
        // Try the prerequisite loaders first
        final int max = prerequisiteLoaders.length;
        for (int i = 0; i < max; i++) {
            final PluginClassLoaderImpl cl = prerequisiteLoaders[i];
            if (cl != null) {
                final URL url = cl.findResource(name);
                if (url != null) {
                    System.err.println("adding " + url);
                    if (!urls.contains(url))
                        urls.add(url);
                }
            }
        }

        // Try the fragments
        URL url = null;
        for (FragmentDescriptorModel fragment : descriptor.fragments()) {
            url = fragment.getResource(name);
            if (url != null) {
                try {
                    startPlugin();
                    fragment.startPlugin(registry);
                } catch (PluginException ex) {
                    BootLogInstance.get().error("Cannot start plugin", ex);
                }
                System.err.println("adding " + url);
                if (!urls.contains(url))
                    urls.add(url);
            }
        }

        // Not found, try my own plugin
        // System.out.println("Try resource " + name + " on " +
        // jar.getDescriptor().getId());
        url = jar.getResource(name);
        if (url != null) {
            try {
                startPlugin();
            } catch (PluginException ex) {
                BootLogInstance.get().error("Cannot start plugin", ex);
            }
            System.err.println("adding " + url);
            if (!urls.contains(url))
                urls.add(url);
        }
        //

        return new Enumeration<URL>() {
            private Iterator<URL> it = urls.iterator();

            public boolean hasMoreElements() {
                return it.hasNext();
            }

            public URL nextElement() {
                return it.next();
            }
        };
    }

    /**
     * Try to load the data of a class with a given name.
     *
     * @param name
     * @return The loaded class data or null if not found.
     */
    private final ByteBuffer loadClassData(ResourceLoader loader, String name) {
        return loader.getResourceAsBuffer(name.replace('.', '/') + ".class");
    }

    /**
     * Make sure that the plugin gets started. This method ensures that this
     * classloader can be used to start the plugin.
     */
    private final void startPlugin() throws PluginException {
        descriptor.startPlugin(registry);
    }

    /**
     * @see org.jnode.plugin.PluginClassLoader#getDeclaringPluginDescriptor()
     */
    public PluginDescriptor getDeclaringPluginDescriptor() {
        return descriptor;
    }
        
    public String toString() {
        return getClass().getName() + '(' + getDeclaringPluginDescriptor().getId() + ')';
    }    
}
