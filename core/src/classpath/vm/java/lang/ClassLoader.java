/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package java.lang;

import gnu.classpath.SystemProperties;
import gnu.java.util.EmptyEnumeration;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jnode.vm.VmJavaClassLoader;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemClassLoader;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmType;

public abstract class ClassLoader {

    private final ClassLoader parent;

    private final VmClassLoader vmClassLoader;

    private ProtectionDomain defaultProtectionDomain;

    /**
     * All packages defined by this classloader. It is not private in order to
     * allow native code (and trusted subclasses) access to this field.
     */
    final HashMap<String, Package> definedPackages = new HashMap<String, Package>();

    /**
     * The desired assertion status of classes loaded by this loader, if not
     * overridden by package or class instructions.
     */
    // Package visible for use by Class.
    boolean defaultAssertionStatus = VMClassLoader.defaultAssertionStatus();

    /**
     * The map of package assertion status overrides, or null if no package
     * overrides have been specified yet. The values of the map should be
     * Boolean.TRUE or Boolean.FALSE, and the unnamed package is represented by
     * the null key. This map must be synchronized on this instance.
     */
    // Package visible for use by Class.
    Map<String, Boolean> packageAssertionStatus;

    /**
     * The map of class assertion status overrides, or null if no class
     * overrides have been specified yet. The values of the map should be
     * Boolean.TRUE or Boolean.FALSE. This map must be synchronized on this
     * instance.
     */
    // Package visible for use by Class.
    Map<String, Boolean> classAssertionStatus;

    static class StaticData {
        /**
         * The System Class Loader (a.k.a. Application Class Loader). The one
         * returned by ClassLoader.getSystemClassLoader.
         */
        static final ClassLoader systemClassLoader = VMClassLoader
                .getSystemClassLoader();
        static {
            // Find out if we have to install a default security manager. Note
            // that
            // this is done here because we potentially need the system class
            // loader
            // to load the security manager and note also that we don't need the
            // security manager until the system class loader is created.
            // If the runtime chooses to use a class loader that doesn't have
            // the
            // system class loader as its parent, it is responsible for setting
            // up a security manager before doing so.
            String secman = SystemProperties
                    .getProperty("java.security.manager");
            if (secman != null && SecurityManager.current == null) {
                if (secman.equals("") || secman.equals("default")) {
                    SecurityManager.current = new SecurityManager();
                } else {
                    try {
                        Class< ? > cl = Class.forName(secman, false,
                                StaticData.systemClassLoader);
                        SecurityManager.current = (SecurityManager) cl
                                .newInstance();
                    } catch (Exception x) {
                        throw (InternalError) new InternalError(
                                "Unable to create SecurityManager")
                                .initCause(x);
                    }
                }
            }
        }

        /**
         * The default protection domain, used when defining a class with a null
         * parameter for the domain.
         */
        static final ProtectionDomain defaultProtectionDomain;
        static {
            final CodeSource cs = new CodeSource(null, null);
            PermissionCollection perm = (PermissionCollection) AccessController
                    .doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return Policy.getPolicy().getPermissions(cs);
                        }
                    });
            defaultProtectionDomain = new ProtectionDomain(cs, perm);
        }

        /**
         * The command-line state of the package assertion status overrides.
         * This map is never modified, so it does not need to be synchronized.
         */
        // Package visible for use by Class.
        static final Map<String, Boolean> systemPackageAssertionStatus = VMClassLoader
                .packageAssertionStatus();

        /**
         * The command-line state of the class assertion status overrides. This
         * map is never modified, so it does not need to be synchronized.
         */
        // Package visible for use by Class.
        static final Map<String, Boolean> systemClassAssertionStatus = VMClassLoader
                .classAssertionStatus();
    }

    /**
     * Create a new ClassLoader with the specified parent. The parent will be
     * consulted when a class or resource is requested through
     * <code>loadClass()</code> or <code>getResource()</code>. Only when
     * the parent classloader cannot provide the requested class or resource the
     * <code>findClass()</code> or <code>findResource()</code> method of
     * this classloader will be called. There may be a security check for
     * <code>checkCreateClassLoader</code>.
     * 
     * @param parent
     *            the classloader's parent
     * @throws SecurityException
     *             if the security check fails
     * @since 1.2
     */
    protected ClassLoader(ClassLoader parent) {
        /* May we create a new classloader? */
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
        }
        if (parent == null) {
            throw new IllegalArgumentException("parent cannot be null");
        }
        this.parent = parent;
        this.vmClassLoader = new VmJavaClassLoader(this);
    }

    /**
     * Create a new instance
     * 
     * @see java.lang.Object#Object()
     */
    protected ClassLoader() {
        this(getSystemClassLoader());
    }

    /**
     * Create a new classloader wrapped around a given VmClassLoader.
     * 
     * @param vmClassLoader
     */
    protected ClassLoader(VmSystemClassLoader vmClassLoader) {
        /* May we create a new classloader? */
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkCreateClassLoader();
        }
        if (vmClassLoader == null) {
            throw new IllegalArgumentException("vmClassLoader cannot be null");
        }
        if (!vmClassLoader.isSystemClassLoader()) {
            throw new IllegalArgumentException(
                    "vmClassLoader must be system classloader");
        }
        this.parent = null;
        this.vmClassLoader = vmClassLoader;
    }

    /**
     * Load and resolve a class with a given name.
     * 
     * @param name
     * @return Class
     * @throws ClassNotFoundException
     */
    public Class loadClass(String name) throws ClassNotFoundException {
        // return vmClassLoader.loadClass(name, true).asClass();
        return loadClass(name, true);
    }

    /**
     * Load and optionally resolve a class with a given name.
     * 
     * @param name
     * @param resolve
     * @return Class
     * @throws ClassNotFoundException
     */
    protected Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {

        /* Have we already loaded this class? */
        final Class cls = findLoadedClass(name);
        if (cls != null) {
            return cls;
        }

        /* Can the class been loaded by a parent? */
        try {
            if ((parent == null) || skipParentLoader(name)) {
                if (vmClassLoader.isSystemClassLoader()) {
                    return vmClassLoader.loadClass(name, resolve).asClass();
                }
            } else {
                return parent.loadClass(name, resolve);
            }
        } catch (ClassNotFoundException e) {
            // e.printStackTrace();
        }
        /* Still not found, we have to do it ourself. */
        final Class c = findClass(name);
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    /**
     * Define a byte-array of class data into a loaded class.
     * 
     * @param data
     * @param offset
     * @param length
     * @return Class
     * @deprecated Replaced by {@link #defineClass(String, byte[], int, int)}
     */
    protected final Class defineClass(byte[] data, int offset, int length) {
        return defineClass(null, data, offset, length, null);
    }

    /**
     * Define a byte-array of class data into a loaded class.
     * 
     * @param name
     * @param data
     * @param offset
     * @param length
     * @return Class
     */
    protected final Class defineClass(String name, byte[] data, int offset,
            int length) {
        return defineClass(name, data, offset, length, null);
    }

    /**
     * Defines a new package and creates a Package object. The package should be
     * defined before any class in the package is defined with
     * <code>defineClass()</code>. The package should not yet be defined
     * before in this classloader or in one of its parents (which means that
     * <code>getPackage()</code> should return <code>null</code>). All
     * parameters except the <code>name</code> of the package may be
     * <code>null</code>.
     * 
     * <p>
     * Subclasses should call this method from their <code>findClass()</code>
     * implementation before calling <code>defineClass()</code> on a Class in
     * a not yet defined Package (which can be checked by calling
     * <code>getPackage()</code>).
     * 
     * @param name
     *            the name of the Package
     * @param specTitle
     *            the name of the specification
     * @param specVendor
     *            the name of the specification designer
     * @param specVersion
     *            the version of this specification
     * @param implTitle
     *            the name of the implementation
     * @param implVendor
     *            the vendor that wrote this implementation
     * @param implVersion
     *            the version of this implementation
     * @param sealed
     *            if sealed the origin of the package classes
     * @return the Package object for the specified package
     * @throws IllegalArgumentException
     *             if the package name is null or it was already defined by this
     *             classloader or one of its parents
     * @see Package
     * @since 1.2
     */
    protected Package definePackage(String name, String specTitle,
            String specVendor, String specVersion, String implTitle,
            String implVendor, String implVersion, URL sealed) {
        if (getPackage(name) != null)
            throw new IllegalArgumentException("Package " + name
                    + " already defined");
        Package p = new Package(name, specTitle, specVendor, specVersion,
                implTitle, implVendor, implVersion, sealed);
        synchronized (definedPackages) {
            definedPackages.put(name, p);
        }
        return p;
    }

    /**
     * Define a byte-array of class data into a loaded class.
     * 
     * @param name
     * @param data
     * @param offset
     * @param length
     * @param protDomain
     * @return Class
     */
    protected final Class defineClass(String name, byte[] data, int offset,
            int length, ProtectionDomain protDomain) {
        if (data == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || length < 0 || (offset + length) > data.length) {
            throw new IndexOutOfBoundsException();
        }
        if (protDomain == null) {

            protDomain = (ProtectionDomain) AccessController
                    .doPrivileged(new PrivilegedAction() {

                        public Object run() {
                            return getDefaultProtectionDomain();
                        }
                    });
        }
        return vmClassLoader
                .defineClass(name, data, offset, length, protDomain).asClass();
    }

    /**
     * Define a byte-array of class data into a loaded class.
     * 
     * @param name
     * @param data
     * @param offset
     * @param length
     * @param protDomain
     * @return Class
     */
    protected final Class defineClass(String name, ByteBuffer data,
            ProtectionDomain protDomain) {
        if (data == null) {
            throw new NullPointerException();
        }
        if (protDomain == null) {
            protDomain = (ProtectionDomain) AccessController
                    .doPrivileged(new PrivilegedAction() {

                        public Object run() {
                            return getDefaultProtectionDomain();
                        }
                    });
        }
        return vmClassLoader.defineClass(name, data, protDomain).asClass();
    }

    private ProtectionDomain getDefaultProtectionDomain() {
        if (defaultProtectionDomain == null) {
            final CodeSource cs = new CodeSource(null, null);
            defaultProtectionDomain = new ProtectionDomain(cs, Policy
                    .getPolicy().getPermissions(cs));
        }
        return defaultProtectionDomain;
    }

    /**
     * Resolve all references in the given class.
     * 
     * @param c
     */
    protected final void resolveClass(Class c) {
        if (c == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Finds the class with the given name if it had been previously loaded
     * through this class loader.
     * 
     * @param name
     * @return the Class object, or null if the class has not been loaded
     */
    protected final Class findLoadedClass(String name) {
        VmType< ? > vmClass = vmClassLoader.findLoadedClass(name);
        if (vmClass != null) {
            return vmClass.asClass();
        } else {
            return null;
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
     * @return Class
     * @throws ClassNotFoundException
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
    }

    /**
     * Find a system class.
     * 
     * @param name
     * @return Class
     * @throws ClassNotFoundException
     */
    protected final Class findSystemClass(String name)
            throws ClassNotFoundException {
        if (name == null) {
            throw new NullPointerException();
        } else {
            return VmSystem.getSystemClassLoader().loadClass(name, true)
                    .asClass();
        }
    }

    /**
     * Gets a system resource as stream by name.
     * 
     * @param name
     * @return InputStream
     */
    public static final InputStream getSystemResourceAsStream(String name) {
        try {
            return VmSystem.getSystemClassLoader().getResourceAsStream(name);
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Gets a resource as stream by name.
     * 
     * @param name
     * @return InputStream
     */
    public InputStream getResourceAsStream(String name) {
        URL url = getResource(name);
        if (url != null) {
            try {
                return url.openStream();
            } catch (IOException ex) {
                // Syslog.debug("Cannot load resource " + name, ex);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets a URL to a system resource by name.
     * 
     * @param name
     * @return URL
     */
    public static final URL getSystemResource(String name) {
        try {
            if (name.startsWith("/")) {
                return new URL("system://" + name);
            } else {
                return new URL("system:///" + name);
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an URL to a resource by name.
     * 
     * @param name
     * @return URL
     */
    public URL getResource(String name) {
        URL result = null;

        if (parent == null) {
            if (vmClassLoader.resourceExists(name)) {
                try {
                    if (name.startsWith("/")) {
                        result = new URL("system://" + name);
                    } else {
                        result = new URL("system:///" + name);
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    result = null;
                }
            }
        } else {
            result = parent.getResource(name);
        }

        if (result == null) {
            result = findResource(name);
        }
        return result;
    }

    public Enumeration getResources(String name) throws IOException {    	
        final List<URL> urls = new ArrayList<URL>();
        getResourcesImpl(name, urls);
        
    	return new Enumeration<URL>()
    	{
    		private Iterator<URL> it = urls.iterator();

			public boolean hasMoreElements() {
				return it.hasNext();
			}

			public URL nextElement() {
				return it.next();
			}    		
    	};
    }
    
    protected boolean getResourcesImpl(String name, List<URL> urls) throws IOException {
    	URL result = null;
        if (parent == null) {
            if (vmClassLoader.resourceExists(name)) {
                try {
                    if (name.startsWith("/")) {
                    	result = new URL("system://" + name);
                    } else {
                    	result = new URL("system:///" + name);
                    }
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                    result = null;
                }
                if(result != null)
                {
                	if(!urls.contains(result)) urls.add(result);
                }
            }
        } else {
            parent.getResourcesImpl(name, urls);
        }

        if (result == null) {
            result = findResource(name);
            if(result != null)
            {
            	if(!urls.contains(result)) urls.add(result);
            }
        }
        
        return (result != null);
    }

    /**
     * Finds the resource with the given name. Class loader implementations
     * should override this method to specify where to find resources.
     * 
     * @param name
     * @return URL
     */
    protected URL findResource(String name) {
        return null;
    }

    /**
     * Returns the Package object for the requested package name. It returns
     * null when the package is not defined by this classloader or one of its
     * parents.
     * 
     * @param name
     *            the package name to find
     * @return the package, if defined
     * @since 1.2
     */
    protected Package getPackage(String name) {
        Package p;
        if (parent == null) {
            p = VMClassLoader.getPackage(name);
        } else {
            p = parent.getPackage(name);
        }

        if (p == null) {
            synchronized (definedPackages) {
                p = definedPackages.get(name);
            }
        }
        return p;
    }

    /**
     * Returns all Package objects defined by this classloader and its parents.
     * 
     * @return an array of all defined packages
     * @since 1.2
     */
    protected Package[] getPackages() {
        // Get all our packages.
        Package[] packages;
        synchronized (definedPackages) {
            packages = new Package[definedPackages.size()];
            definedPackages.values().toArray(packages);
        }

        // If we have a parent get all packages defined by our parents.
        Package[] parentPackages;
        if (parent == null)
            parentPackages = VMClassLoader.getPackages();
        else
            parentPackages = parent.getPackages();

        Package[] allPackages = new Package[parentPackages.length
                + packages.length];
        System.arraycopy(parentPackages, 0, allPackages, 0,
                parentPackages.length);
        System.arraycopy(packages, 0, allPackages, parentPackages.length,
                packages.length);
        return allPackages;
    }

    /**
     * Avoid trying to load the given class via its parent classloader?
     * 
     * @param name
     * @return
     */
    public boolean skipParentLoader(String name) {
        return false;
    }

    /**
     * Gets the system classloader.
     * 
     * @return ClassLoader
     */
    public static ClassLoader getSystemClassLoader() {
        return VmSystem.getSystemClassLoader().asClassLoader();
    }

    public static Enumeration getSystemResources(String name)
            throws IOException {
        return EmptyEnumeration.getInstance();
    }

    /**
     * @return
     */
    public ClassLoader getParent() {
        return parent;
    }

    /**
     * Called by <code>Runtime.loadLibrary()</code> to get an absolute path to
     * a (system specific) library that was requested by a class loaded by this
     * classloader. The default implementation returns <code>null</code>. It
     * should be implemented by subclasses when they have a way to find the
     * absolute path to a library. If this method returns null the library is
     * searched for in the default locations (the directories listed in the
     * <code>java.library.path</code> system property).
     * 
     * @param name
     *            the (system specific) name of the requested library
     * @return the full pathname to the requested library, or null
     * @see Runtime#loadLibrary()
     * @since 1.2
     */
    protected String findLibrary(String name) {
        return null;
    }

    /**
     * Sets the signers of a class. This should be invoked after defining a
     * class.
     * 
     * @param clazz
     *            The class object.
     * @param signers
     *            The signers.
     */
    protected final void setSigners(Class clazz, Object[] signers) {
        // TODO implement me
    }

    /**
     * Set the default assertion status for classes loaded by this classloader,
     * used unless overridden by a package or class request.
     * 
     * @param enabled
     *            true to set the default to enabled
     * @see #setClassAssertionStatus(String, boolean)
     * @see #setPackageAssertionStatus(String, boolean)
     * @see #clearAssertionStatus()
     * @since 1.4
     */
    public void setDefaultAssertionStatus(boolean enabled) {
        defaultAssertionStatus = enabled;
    }

    /**
     * Set the default assertion status for packages, used unless overridden by
     * a class request. This default also covers subpackages, unless they are
     * also specified. The unnamed package should use null for the name.
     * 
     * @param name
     *            the package (and subpackages) to affect
     * @param enabled
     *            true to set the default to enabled
     * @see #setDefaultAssertionStatus(String, boolean)
     * @see #setClassAssertionStatus(String, boolean)
     * @see #clearAssertionStatus()
     * @since 1.4
     */
    public synchronized void setPackageAssertionStatus(String name,
            boolean enabled) {
        if (packageAssertionStatus == null)
            packageAssertionStatus = new HashMap<String, Boolean>(
                    StaticData.systemPackageAssertionStatus);
        packageAssertionStatus.put(name, Boolean.valueOf(enabled));
    }

    /**
     * Set the default assertion status for a class. This only affects the
     * status of top-level classes, any other string is harmless.
     * 
     * @param name
     *            the class to affect
     * @param enabled
     *            true to set the default to enabled
     * @throws NullPointerException
     *             if name is null
     * @see #setDefaultAssertionStatus(String, boolean)
     * @see #setPackageAssertionStatus(String, boolean)
     * @see #clearAssertionStatus()
     * @since 1.4
     */
    public synchronized void setClassAssertionStatus(String name,
            boolean enabled) {
        if (classAssertionStatus == null)
            classAssertionStatus = new HashMap<String, Boolean>(
                    StaticData.systemClassAssertionStatus);
        // The toString() hack catches null, as required.
        classAssertionStatus.put(name.toString(), Boolean.valueOf(enabled));
    }

    /**
     * Resets the default assertion status of this classloader, its packages and
     * classes, all to false. This allows overriding defaults inherited from the
     * command line.
     * 
     * @see #setDefaultAssertionStatus(boolean)
     * @see #setClassAssertionStatus(String, boolean)
     * @see #setPackageAssertionStatus(String, boolean)
     * @since 1.4
     */
    public synchronized void clearAssertionStatus() {
        defaultAssertionStatus = false;
        packageAssertionStatus = null;
        classAssertionStatus = null;
    }

}
