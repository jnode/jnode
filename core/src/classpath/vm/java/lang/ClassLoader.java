/*
 * $Id$
 */

package java.lang;

import gnu.java.util.EmptyEnumeration;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.Enumeration;

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
     * Defines a package by name in this ClassLoader. This allows class loaders
     * to define the packages for their classes. Packages must be created before
     * the class is defined, and package names must be unique within a class
     * loader and cannot be redefined or changed once created.
     * 
     * @param name
     * @param specTitle
     * @param specVersion
     * @param specVendor
     * @param implTitle
     * @param implVersion
     * @param implVendor
     * @param url
     * @return Package
     */
    protected Package definePackage(String name, String specTitle,
            String specVersion, String specVendor, String implTitle,
            String implVersion, String implVendor, URL url) {
        return null;
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
        VmType vmClass = vmClassLoader.findLoadedClass(name);
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
        return EmptyEnumeration.getInstance();
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

    protected Package getPackage(String name) {
        return null;
    }

    protected Package[] getPackages() {
        return new Package[0];
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
}