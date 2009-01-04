/*
 * Copyright 1997-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package java.net;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.File;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.security.CodeSigner;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import java.security.AccessControlContext;
import java.security.SecureClassLoader;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import sun.misc.Resource;
import sun.misc.URLClassPath;
import sun.net.www.ParseUtil;
import sun.security.util.SecurityConstants;

/**
 * This class loader is used to load classes and resources from a search
 * path of URLs referring to both JAR files and directories. Any URL that
 * ends with a '/' is assumed to refer to a directory. Otherwise, the URL
 * is assumed to refer to a JAR file which will be opened as needed.
 * <p>
 * The AccessControlContext of the thread that created the instance of
 * URLClassLoader will be used when subsequently loading classes and
 * resources.
 * <p>
 * The classes that are loaded are by default granted permission only to
 * access the URLs specified when the URLClassLoader was created.
 *
 * @author  David Connelly
 * @since   1.2
 */
public class URLClassLoader extends SecureClassLoader {
    /* The search path for classes and resources */
    URLClassPath ucp;

    /* The context to be used when loading classes and resources */
    private AccessControlContext acc;

    /**
     * Constructs a new URLClassLoader for the given URLs. The URLs will be
     * searched in the order specified for classes and resources after first
     * searching in the specified parent class loader. Any URL that ends with
     * a '/' is assumed to refer to a directory. Otherwise, the URL is assumed
     * to refer to a JAR file which will be downloaded and opened as needed.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkCreateClassLoader</code> method
     * to ensure creation of a class loader is allowed.
     *
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkCreateClassLoader</code> method doesn't allow
     *             creation of a class loader.
     * @see SecurityManager#checkCreateClassLoader
     */
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        super(parent);
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new URLClassPath(urls);
        acc = AccessController.getContext();
    }

    /**
     * Constructs a new URLClassLoader for the specified URLs using the
     * default delegation parent <code>ClassLoader</code>. The URLs will
     * be searched in the order specified for classes and resources after
     * first searching in the parent class loader. Any URL that ends with
     * a '/' is assumed to refer to a directory. Otherwise, the URL is
     * assumed to refer to a JAR file which will be downloaded and opened
     * as needed.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkCreateClassLoader</code> method
     * to ensure creation of a class loader is allowed.
     *
     * @param urls the URLs from which to load classes and resources
     *
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkCreateClassLoader</code> method doesn't allow
     *             creation of a class loader.
     * @see SecurityManager#checkCreateClassLoader
     */
    public URLClassLoader(URL[] urls) {
        super();
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new URLClassPath(urls);
        acc = AccessController.getContext();
    }

    /**
     * Constructs a new URLClassLoader for the specified URLs, parent
     * class loader, and URLStreamHandlerFactory. The parent argument
     * will be used as the parent class loader for delegation. The
     * factory argument will be used as the stream handler factory to
     * obtain protocol handlers when creating new jar URLs.
     *
     * <p>If there is a security manager, this method first
     * calls the security manager's <code>checkCreateClassLoader</code> method
     * to ensure creation of a class loader is allowed.
     *
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     * @param factory the URLStreamHandlerFactory to use when creating URLs
     *
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkCreateClassLoader</code> method doesn't allow
     *             creation of a class loader.
     * @see SecurityManager#checkCreateClassLoader
     */
    public URLClassLoader(URL[] urls, ClassLoader parent,
                          URLStreamHandlerFactory factory) {
        super(parent);
        // this is to make the stack depth consistent with 1.1
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkCreateClassLoader();
        }
        ucp = new URLClassPath(urls, factory);
        acc = AccessController.getContext();
    }

    /**
     * Appends the specified URL to the list of URLs to search for
     * classes and resources.
     *
     * @param url the URL to be added to the search path of URLs
     */
    protected void addURL(URL url) {
        ucp.addURL(url);
    }

    /**
     * Returns the search path of URLs for loading classes and resources.
     * This includes the original list of URLs specified to the constructor,
     * along with any URLs subsequently appended by the addURL() method.
     * @return the search path of URLs for loading classes and resources.
     */
    public URL[] getURLs() {
        return ucp.getURLs();
    }

    /**
     * Finds and loads the class with the specified name from the URL search
     * path. Any URLs referring to JAR files are loaded and opened as needed
     * until the class is found.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
    protected Class<?> findClass(final String name)
         throws ClassNotFoundException
    {
        try {
            return (Class)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException {
                        String path = name.replace('.', '/').concat(".class");
                        Resource res = ucp.getResource(path, false);
                        if (res != null) {
                            try {
                                return defineClass(name, res);
                            } catch (IOException e) {
                                throw new ClassNotFoundException(name, e);
                            }
                        } else {
                            throw new ClassNotFoundException(name);
                        }
                    }
                }, acc);
        } catch (java.security.PrivilegedActionException pae) {
            throw (ClassNotFoundException) pae.getException();
        }
    }

    /*
     * Defines a Class using the class bytes obtained from the specified
     * Resource. The resulting Class must be resolved before it can be
     * used.
     */
    private Class defineClass(String name, Resource res) throws IOException {
        int i = name.lastIndexOf('.');
        URL url = res.getCodeSourceURL();
        if (i != -1) {
            String pkgname = name.substring(0, i);
            // Check if package already loaded.
            Package pkg = getPackage(pkgname);
            Manifest man = res.getManifest();
            if (pkg != null) {
                // Package found, so check package sealing.
                if (pkg.isSealed()) {
                    // Verify that code source URL is the same.
                    if (!pkg.isSealed(url)) {
                        throw new SecurityException(
                            "sealing violation: package " + pkgname + " is sealed");
                    }

                } else {
                    // Make sure we are not attempting to seal the package
                    // at this code source URL.
                    if ((man != null) && isSealed(pkgname, man)) {
                        throw new SecurityException(
                            "sealing violation: can't seal package " + pkgname +
                            ": already loaded");
                    }
                }
            } else {
                if (man != null) {
                    definePackage(pkgname, man, url);
                } else {
                    definePackage(pkgname, null, null, null, null, null, null, null);
                }
            }
        }
        // Now read the class bytes and define the class
        java.nio.ByteBuffer bb = res.getByteBuffer();
        if (bb != null) {
            // Use (direct) ByteBuffer:
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            return defineClass(name, bb, cs);
        } else {
            byte[] b = res.getBytes();
            // must read certificates AFTER reading bytes.
            CodeSigner[] signers = res.getCodeSigners();
            CodeSource cs = new CodeSource(url, signers);
            return defineClass(name, b, 0, b.length, cs);
        }
    }

    /**
     * Defines a new package by name in this ClassLoader. The attributes
     * contained in the specified Manifest will be used to obtain package
     * version and sealing information. For sealed packages, the additional
     * URL specifies the code source URL from which the package was loaded.
     *
     * @param name  the package name
     * @param man   the Manifest containing package version and sealing
     *              information
     * @param url   the code source url for the package, or null if none
     * @exception   IllegalArgumentException if the package name duplicates
     *              an existing package either in this class loader or one
     *              of its ancestors
     * @return the newly defined Package object
     */
    protected Package definePackage(String name, Manifest man, URL url)
        throws IllegalArgumentException
    {
        String path = name.replace('.', '/').concat("/");
        String specTitle = null, specVersion = null, specVendor = null;
        String implTitle = null, implVersion = null, implVendor = null;
        String sealed = null;
        URL sealBase = null;

        Attributes attr = man.getAttributes(path);
        if (attr != null) {
            specTitle   = attr.getValue(Name.SPECIFICATION_TITLE);
            specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            specVendor  = attr.getValue(Name.SPECIFICATION_VENDOR);
            implTitle   = attr.getValue(Name.IMPLEMENTATION_TITLE);
            implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            implVendor  = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            sealed      = attr.getValue(Name.SEALED);
        }
        attr = man.getMainAttributes();
        if (attr != null) {
            if (specTitle == null) {
                specTitle = attr.getValue(Name.SPECIFICATION_TITLE);
            }
            if (specVersion == null) {
                specVersion = attr.getValue(Name.SPECIFICATION_VERSION);
            }
            if (specVendor == null) {
                specVendor = attr.getValue(Name.SPECIFICATION_VENDOR);
            }
            if (implTitle == null) {
                implTitle = attr.getValue(Name.IMPLEMENTATION_TITLE);
            }
            if (implVersion == null) {
                implVersion = attr.getValue(Name.IMPLEMENTATION_VERSION);
            }
            if (implVendor == null) {
                implVendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
            }
            if (sealed == null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        if ("true".equalsIgnoreCase(sealed)) {
            sealBase = url;
        }
        return definePackage(name, specTitle, specVersion, specVendor,
                             implTitle, implVersion, implVendor, sealBase);
    }

    /*
     * Returns true if the specified package name is sealed according to the
     * given manifest.
     */
    private boolean isSealed(String name, Manifest man) {
        String path = name.replace('.', '/').concat("/");
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);
    }

    /**
     * Finds the resource with the specified name on the URL search path.
     *
     * @param name the name of the resource
     * @return a <code>URL</code> for the resource, or <code>null</code>
     * if the resource could not be found.
     */
    public URL findResource(final String name) {
        /*
         * The same restriction to finding classes applies to resources
         */
        URL url =
            (URL) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return ucp.findResource(name, true);
                }
            }, acc);

        return url != null ? ucp.checkURL(url) : null;
    }

    /**
     * Returns an Enumeration of URLs representing all of the resources
     * on the URL search path having the specified name.
     *
     * @param name the resource name
     * @exception IOException if an I/O exception occurs
     * @return an <code>Enumeration</code> of <code>URL</code>s
     */
    public Enumeration<URL> findResources(final String name)
        throws IOException
    {
        final Enumeration e = ucp.findResources(name, true);

        return new Enumeration<URL>() {
            private URL url = null;

            private boolean next() {
                if (url != null) {
                    return true;
                }
                do {
                    URL u = (URL)
                        AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                if (!e.hasMoreElements())
                                    return null;
                                return e.nextElement();
                            }
                        }, acc);
                    if (u == null)
                        break;
                    url = ucp.checkURL(u);
                } while (url == null);
                return url != null;
            }

            public URL nextElement() {
                if (!next()) {
                    throw new NoSuchElementException();
                }
                URL u = url;
                url = null;
                return u;
            }

            public boolean hasMoreElements() {
                return next();
            }
        };
    }

    /**
     * Returns the permissions for the given codesource object.
     * The implementation of this method first calls super.getPermissions
     * and then adds permissions based on the URL of the codesource.
     * <p>
     * If the protocol of this URL is "jar", then the permission granted
     * is based on the permission that is required by the URL of the Jar
     * file.
     * <p>
     * If the protocol is "file"
     * and the path specifies a file, then permission to read that
     * file is granted. If protocol is "file" and the path is
     * a directory, permission is granted to read all files
     * and (recursively) all files and subdirectories contained in
     * that directory.
     * <p>
     * If the protocol is not "file", then permission
     * to connect to and accept connections from the URL's host is granted.
     * @param codesource the codesource
     * @return the permissions granted to the codesource
     */
    protected PermissionCollection getPermissions(CodeSource codesource)
    {
        PermissionCollection perms = super.getPermissions(codesource);

        URL url = codesource.getLocation();

        Permission p;
        URLConnection urlConnection;

        try {
            urlConnection = url.openConnection();
            p = urlConnection.getPermission();
        } catch (java.io.IOException ioe) {
            p = null;
            urlConnection = null;
        }

        if (p instanceof FilePermission) {
            // if the permission has a separator char on the end,
            // it means the codebase is a directory, and we need
            // to add an additional permission to read recursively
            String path = p.getName();
            if (path.endsWith(File.separator)) {
                path += "-";
                p = new FilePermission(path, SecurityConstants.FILE_READ_ACTION);
            }
        } else if ((p == null) && (url.getProtocol().equals("file"))) {
            String path = url.getFile().replace('/', File.separatorChar);
            path = ParseUtil.decode(path);
            if (path.endsWith(File.separator))
                path += "-";
            p =  new FilePermission(path, SecurityConstants.FILE_READ_ACTION);
        } else {
            /**
             * Not loading from a 'file:' URL so we want to give the class
             * permission to connect to and accept from the remote host
             * after we've made sure the host is the correct one and is valid.
             */
            URL locUrl = url;
            if (urlConnection instanceof JarURLConnection) {
                locUrl = ((JarURLConnection)urlConnection).getJarFileURL();
            }
            String host = locUrl.getHost();
            if (host != null && (host.length() > 0))
                p = new SocketPermission(host,
                                         SecurityConstants.SOCKET_CONNECT_ACCEPT_ACTION);
        }

        // make sure the person that created this class loader
        // would have this permission

        if (p != null) {
            final SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                final Permission fp = p;
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() throws SecurityException {
                        sm.checkPermission(fp);
                        return null;
                    }
                }, acc);
            }
            perms.add(p);
        }
        return perms;
    }

    /**
     * Creates a new instance of URLClassLoader for the specified
     * URLs and parent class loader. If a security manager is
     * installed, the <code>loadClass</code> method of the URLClassLoader
     * returned by this method will invoke the
     * <code>SecurityManager.checkPackageAccess</code> method before
     * loading the class.
     *
     * @param urls the URLs to search for classes and resources
     * @param parent the parent class loader for delegation
     * @return the resulting class loader
     */
    public static URLClassLoader newInstance(final URL[] urls,
                                             final ClassLoader parent) {
        // Save the caller's context
        AccessControlContext acc = AccessController.getContext();
        // Need a privileged block to create the class loader
        URLClassLoader ucl =
            (URLClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return new FactoryURLClassLoader(urls, parent);
                }
            });
        // Now set the context on the loader using the one we saved,
        // not the one inside the privileged block...
        ucl.acc = acc;
        return ucl;
    }

    /**
     * Creates a new instance of URLClassLoader for the specified
     * URLs and default parent class loader. If a security manager is
     * installed, the <code>loadClass</code> method of the URLClassLoader
     * returned by this method will invoke the
     * <code>SecurityManager.checkPackageAccess</code> before
     * loading the class.
     *
     * @param urls the URLs to search for classes and resources
     * @return the resulting class loader
     */
    public static URLClassLoader newInstance(final URL[] urls) {
        // Save the caller's context
        AccessControlContext acc = AccessController.getContext();
        // Need a privileged block to create the class loader
        URLClassLoader ucl = (URLClassLoader)
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return new FactoryURLClassLoader(urls);
                }
            });

        // Now set the context on the loader using the one we saved,
        // not the one inside the privileged block...
        ucl.acc = acc;
        return ucl;
    }

    static {
        sun.misc.SharedSecrets.setJavaNetAccess (
            new sun.misc.JavaNetAccess() {
                public URLClassPath getURLClassPath (URLClassLoader u) {
                    return u.ucp;
                }
            }
        );
    }
}

final class FactoryURLClassLoader extends URLClassLoader {

    FactoryURLClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    FactoryURLClassLoader(URL[] urls) {
        super(urls);
    }

    public final synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        // First check if we have permission to access the package. This
        // should go away once we've added support for exported packages.
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            int i = name.lastIndexOf('.');
            if (i != -1) {
                sm.checkPackageAccess(name.substring(0, i));
            }
        }
        return super.loadClass(name, resolve);
    }
}
