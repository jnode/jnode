/* URLClassLoader.java --  ClassLoader that loads classes from one or more URLs
   Copyright (C) 1999, 2000, 2001, 2002 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.
 
GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.net;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * A secure class loader that can load classes and resources from
 * multiple locations.  Given an array of <code>URL</code>s this class
 * loader will retrieve classes and resources by fetching them from
 * possible remote locations.  Each <code>URL</code> is searched in
 * order in which it was added.  If the file portion of the
 * <code>URL</code> ends with a '/' character then it is interpreted
 * as a base directory, otherwise it is interpreted as a jar file from
 * which the classes/resources are resolved.
 *
 * <p>New instances can be created by two static
 * <code>newInstance()</code> methods or by three public
 * contructors. Both ways give the option to supply an initial array
 * of <code>URL</code>s and (optionally) a parent classloader (that is
 * different from the standard system class loader).</p>
 *
 * <p>Normally creating a <code>URLClassLoader</code> throws a
 * <code>SecurityException</code> if a <code>SecurityManager</code> is
 * installed and the <code>checkCreateClassLoader()</code> method does
 * not return true.  But the <code>newInstance()</code> methods may be
 * used by any code as long as it has permission to acces the given
 * <code>URL</code>s.  <code>URLClassLoaders</code> created by the
 * <code>newInstance()</code> methods also explicitly call the
 * <code>checkPackageAccess()</code> method of
 * <code>SecurityManager</code> if one is installed before trying to
 * load a class.  Note that only subclasses of
 * <code>URLClassLoader</code> can add new URLs after the
 * URLClassLoader had been created. But it is always possible to get
 * an array of all URLs that the class loader uses to resolve classes
 * and resources by way of the <code>getURLs()</code> method.</p>
 *
 * <p>Open issues:
 * <ul>
 *
 * <li>Should the URLClassLoader actually add the locations found in
 * the manifest or is this the responsibility of some other
 * loader/(sub)class?  (see <a
 * href="http://java.sun.com/products/jdk/1.4/docs/guide/extensions/spec.html">
 * Extension Mechanism Architecture - Bundles Extensions</a>)</li>
 *
 * <li>How does <code>definePackage()</code> and sealing work
 * precisely?</li>
 *
 * <li>We save and use the security context (when a created by
 * <code>newInstance()</code> but do we have to use it in more
 * places?</li>
 *
 * <li>The use of <code>URLStreamHandler</code>s has not been tested.</li>
 *
 * </ul>
 * </p>
 *
 * @since 1.2
 *
 * @author Mark Wielaard (mark@klomp.org)
 * @author Wu Gansha (gansha.wu@intel.com)
 */
 
public class URLClassLoader extends SecureClassLoader
{
  // Class Variables

  /**
   * A global cache to store mappings between URLLoader and URL,
   * so we can avoid do all the homework each time the same URL
   * comes.
   * XXX - Keeps these loaders forever which prevents garbage collection.
   */
  private static HashMap urlloaders = new HashMap();
    
  /**
   * A cache to store mappings between handler factory and its
   * private protocol handler cache (also a HashMap), so we can avoid
   * create handlers each time the same protocol comes.
   */
  private static HashMap factoryCache = new HashMap(5);

  // Instance variables

  /** Locations to load classes from */
  private final Vector urls = new Vector();

  /**
   * Store pre-parsed information for each url into this vector 
   * each element is a URL loader, corresponding to the URL of 
   * the same index in "urls"
   */
  private final Vector urlinfos = new Vector();
    
  /** Factory used to get the protocol handlers of the URLs */
  private final URLStreamHandlerFactory factory;

  /**
   * The security context when created from <code>newInstance()</code>
   * or null when created through a normal constructor or when no
   * <code>SecurityManager</code> was installed.
   */
  private final AccessControlContext securityContext;

  // Helper classes
 
  /** 
   * A <code>URLLoader</code> contains all logic to load resources from a
   * given base <code>URL</code>.
   */
  static abstract class URLLoader
  {
    /**
     * Our classloader to get info from if needed.
     */
    final URLClassLoader classloader;

    /**
     * The base URL from which all resources are loaded.
     */
    final URL baseURL;

    /**
     * A <code>CodeSource</code> without any associated certificates.
     * It is common for classes to not have certificates associated
     * with them.  If they come from the same <code>URLLoader</code>
     * then it is safe to share the associated <code>CodeSource</code>
     * between them since <code>CodeSource</code> is immutable.
     */
    final CodeSource noCertCodeSource;

    URLLoader(URLClassLoader classloader, URL baseURL)
    {
      this.classloader = classloader;
      this.baseURL = baseURL;
      this.noCertCodeSource = new CodeSource(baseURL, null);
    }

    /**
     * Returns a <code>Resource</code> loaded by this
     * <code>URLLoader</code>, or <code>null</code> when no
     * <code>Resource</code> with the given name exists.
     */
    abstract Resource getResource(String s);

    /**
     * Returns the <code>Manifest</code> associated with the
     * <code>Resource</code>s loaded by this <code>URLLoader</code> or
     * <code>null</code> there is no such <code>Manifest</code>.
     */
    Manifest getManifest()
    {
      return null;
    }
  }

  /** 
   * A <code>Resource</code> represents a resource in some
   * <code>URLLoader</code>. It also contains all information (e.g.,
   * <code>URL</code>, <code>CodeSource</code>, <code>Manifest</code> and
   * <code>InputStream</code>) that is necessary for loading resources
   * and creating classes from a <code>URL</code>.
   */
  static abstract class Resource
  {
    final URLLoader loader;
    final String name;

    Resource(URLLoader loader, String name)
    {
      this.loader = loader;
      this.name = name;
    }

    /**
     * Returns the non-null <code>CodeSource</code> associated with
     * this resource.
     */
    CodeSource getCodeSource()
    {
      Certificate[] certs = getCertificates();
      if (certs == null)
	return loader.noCertCodeSource;
      else
	return new CodeSource(loader.baseURL, certs);
    }

    /**
     * Returns <code>Certificates</code> associated with this
     * resource, or null when there are none.
     */
    Certificate[] getCertificates()
    {
      return null;
    }

    /**
     * Return a <code>URL</code> that can be used to access this resource.
     */
    abstract URL getURL();

    /**
     * Returns the size of this <code>Resource</code> in bytes or
     * <code>-1</code> when unknown.
     */
    abstract int getLength();

    /**
     * Returns the non-null <code>InputStream</code> through which
     * this resource can be loaded.
     */
    abstract InputStream getInputStream() throws IOException;
  }

  /**
   * A <code>JarURLLoader</code> is a type of <code>URLLoader</code>
   * only loading from jar url.
   */
  final static class JarURLLoader extends URLLoader
  {
    final JarFile jarfile; // The jar file for this url
    final URL baseJarURL;  // Base jar: url for all resources loaded from jar

    public JarURLLoader(URLClassLoader classloader, URL baseURL)
    {
      super(classloader, baseURL);

      // cache url prefix for all resources in this jar url
      String external = baseURL.toExternalForm();
      StringBuffer sb = new StringBuffer(external.length() + 6);
      sb.append("jar:");
      sb.append(external);
      sb.append("!/");
      String jarURL = sb.toString();

      URL baseJarURL = null;
      JarFile jarfile = null;
      try
	{
	  baseJarURL
	    = new URL(null, jarURL, classloader.getURLStreamHandler("jar"));
	  jarfile
	    = ((JarURLConnection) baseJarURL.openConnection()).getJarFile();
	}
      catch (IOException ioe) { /* ignored */ }

      this.baseJarURL = baseJarURL;
      this.jarfile = jarfile;
    }

    /** get resource with the name "name" in the jar url */
    Resource getResource(String name)
    {
      if (jarfile == null)
	return null;

      JarEntry je = jarfile.getJarEntry(name);
      if(je != null)
	return new JarURLResource(this, name, je);
      else
	return null;
    }

    Manifest getManifest()
    {
      try
	{
	  return (jarfile == null) ? null : jarfile.getManifest();
	}
      catch (IOException ioe)
	{
	  return null;
	}
    }
  }

  final static class JarURLResource extends Resource
  {
    private final JarEntry entry;

    JarURLResource(JarURLLoader loader, String name, JarEntry entry)
    {
      super(loader, name);
      this.entry = entry;
    }

    InputStream getInputStream() throws IOException
    {
      return ((JarURLLoader)loader).jarfile.getInputStream(entry);
    }

    int getLength()
    {
      return (int)entry.getSize();
    }

    Certificate[] getCertificates()
    {
      return entry.getCertificates();
    }

    URL getURL()
    {
      try
	{
	  return new URL(((JarURLLoader)loader).baseJarURL, name,
			 loader.classloader.getURLStreamHandler("jar"));
	}
      catch(MalformedURLException e)
	{
	  InternalError ie = new InternalError();
	  ie.initCause(e);
	  throw ie;
	}
    }
  }

  /**
   * Loader for remote directories.
   */
  final static class RemoteURLLoader extends URLLoader
  {
    final private String protocol;

    RemoteURLLoader(URLClassLoader classloader, URL url)
    {
      super(classloader, url);
      protocol = url.getProtocol();
    }

    /**
     * Get a remote resource.
     * Returns null if no such resource exists.
     */
    Resource getResource(String name)
    {
      try
	{
	  URL url = new URL(baseURL, name,
			    classloader.getURLStreamHandler(protocol));
	  URLConnection connection = url.openConnection();

	  // Open the connection and check the stream
	  // just to be sure it exists.
	  int length = connection.getContentLength();
	  InputStream stream = connection.getInputStream();

	  // We can do some extra checking if it is a http request
	  if (connection instanceof HttpURLConnection)
	    {
	      int response
		= ((HttpURLConnection)connection).getResponseCode();
	      if (response/100 != 2)
		return null;
	    }

	  if (stream != null)
	    return new RemoteResource(this, name, url, stream, length);
	  else
	    return null;
	}
      catch (IOException ioe)
	{
	  return null;
	}
    }
  }

  /**
   * A resource from some remote location.
   */
  final static class RemoteResource extends Resource
  {
    final private URL url;
    final private InputStream stream;
    final private int length;

    RemoteResource(RemoteURLLoader loader, String name, URL url,
		   InputStream stream, int length)
    {
      super(loader, name);
      this.url = url;
      this.stream = stream;
      this.length = length;
    }

    InputStream getInputStream() throws IOException
    {
      return stream;
    }
                        
    public int getLength()
    {
      return length;
    }
                
    public URL getURL()
    {
      return url;
    }
  }

  /**
   * A <code>FileURLLoader</code> is a type of <code>URLLoader</code>
   * only loading from file url.
   */
  final static class FileURLLoader extends URLLoader
  {
    File dir;   //the file for this file url

    FileURLLoader(URLClassLoader classloader, URL url)
    {
      super(classloader, url);
      dir = new File(baseURL.getFile());
    }

    /** get resource with the name "name" in the file url */
    Resource getResource(String name)
    {
      File file = new File(dir, name);
      if (file.exists() && !file.isDirectory())
	return new FileResource(this, name, file);
      return null;
    }
  }

  final static class FileResource extends Resource
  {
    final File file;

    FileResource(FileURLLoader loader, String name, File file)
    {
      super(loader, name);
      this.file = file;
    }

    InputStream getInputStream() throws IOException
    {
      return new FileInputStream(file);
    }
                        
    public int getLength()
    {
      return (int)file.length();
    }

    public URL getURL()
    {
      try
	{
	  return new URL(loader.baseURL, name,
			 loader.classloader.getURLStreamHandler("file"));
	}
      catch(MalformedURLException e)
	{
	  InternalError ie = new InternalError();
	  ie.initCause(e);
	  throw ie;
	}
    }
  }
    
  // Constructors

  /**
   * Creates a URLClassLoader that gets classes from the supplied URLs.
   * To determine if this classloader may be created the constructor of
   * the super class (<code>SecureClassLoader</code>) is called first, which
   * can throw a SecurityException. Then the supplied URLs are added
   * in the order given to the URLClassLoader which uses these URLs to
   * load classes and resources (after using the default parent ClassLoader).
   *
   * @exception SecurityException if the SecurityManager disallows the
   * creation of a ClassLoader.
   * @param urls Locations that should be searched by this ClassLoader when
   * resolving Classes or Resources.
   * @see SecureClassLoader
   */
  public URLClassLoader(URL[] urls) throws SecurityException
  {
    super();
    this.factory = null;
    this.securityContext = null;
    addURLs(urls);
  }

  /**
   * Private constructor used by the static
   * <code>newInstance(URL[])</code> method.  Creates an
   * <code>URLClassLoader</code> without any <code>URL</code>s
   * yet. This is used to bypass the normal security check for
   * creating classloaders, but remembers the security context which
   * will be used when defining classes.  The <code>URL</code>s to
   * load from must be added by the <code>newInstance()</code> method
   * in the security context of the caller.
   *
   * @param securityContext the security context of the unprivileged code.
   */
  private URLClassLoader(AccessControlContext securityContext)
  {
    super();
    this.factory = null;
    this.securityContext = securityContext;
  }

  /**
   * Creates a <code>URLClassLoader</code> that gets classes from the supplied
   * <code>URL</code>s.
   * To determine if this classloader may be created the constructor of
   * the super class (<code>SecureClassLoader</code>) is called first, which
   * can throw a SecurityException. Then the supplied URLs are added
   * in the order given to the URLClassLoader which uses these URLs to
   * load classes and resources (after using the supplied parent ClassLoader).
   * @exception SecurityException if the SecurityManager disallows the
   * creation of a ClassLoader.
   * @exception SecurityException 
   * @param urls Locations that should be searched by this ClassLoader when
   * resolving Classes or Resources.
   * @param parent The parent class loader used before trying this class
   * loader.
   * @see SecureClassLoader
   */
  public URLClassLoader(URL[] urls, ClassLoader parent)
    throws SecurityException
  {
    super(parent);
    this.factory = null;
    this.securityContext = null;
    addURLs(urls);
  }

  /**
   * Private constructor used by the static
   * <code>newInstance(URL[])</code> method.  Creates an
   * <code>URLClassLoader</code> with the given parent but without any
   * <code>URL</code>s yet. This is used to bypass the normal security
   * check for creating classloaders, but remembers the security
   * context which will be used when defining classes.  The
   * <code>URL</code>s to load from must be added by the
   * <code>newInstance()</code> method in the security context of the
   * caller.
   *
   * @param securityContext the security context of the unprivileged code.
   */
  private URLClassLoader(ClassLoader parent,
			 AccessControlContext securityContext)
  {
    super(parent);
    this.factory = null;
    this.securityContext = securityContext;
  }

  /**
   * Creates a URLClassLoader that gets classes from the supplied URLs.
   * To determine if this classloader may be created the constructor of
   * the super class (<CODE>SecureClassLoader</CODE>) is called first, which
   * can throw a SecurityException. Then the supplied URLs are added
   * in the order given to the URLClassLoader which uses these URLs to
   * load classes and resources (after using the supplied parent ClassLoader).
   * It will use the supplied <CODE>URLStreamHandlerFactory</CODE> to get the
   * protocol handlers of the supplied URLs.
   * @exception SecurityException if the SecurityManager disallows the
   * creation of a ClassLoader.
   * @exception SecurityException 
   * @param urls Locations that should be searched by this ClassLoader when
   * resolving Classes or Resources.
   * @param parent The parent class loader used before trying this class
   * loader.
   * @param factory Used to get the protocol handler for the URLs.
   * @see SecureClassLoader
   */
  public URLClassLoader(URL[] urls,
			ClassLoader parent,
			URLStreamHandlerFactory factory)
    throws SecurityException
  {
    super(parent);
    this.securityContext = null;
    this.factory = factory;
    addURLs(urls);

    // If this factory is still not in factoryCache, add it,
    //   since we only support three protocols so far, 5 is enough 
    //   for cache initial size
    synchronized(factoryCache)
      {
	if(factory != null && factoryCache.get(factory) == null)
	  factoryCache.put(factory, new HashMap(5));
      }
  }

  // Methods

  /**
   * Adds a new location to the end of the internal URL store.
   * @param newUrl the location to add
   */
  protected void addURL(URL newUrl)
  {
    synchronized(urlloaders)
      {
	if (newUrl == null)
	  return; // Silently ignore...
        
	// check global cache to see if there're already url loader
	// for this url
	URLLoader loader = (URLLoader)urlloaders.get(newUrl);
	if (loader == null)
	  {
	    String file = newUrl.getFile();
	    // Check that it is not a directory
	    if (! (file.endsWith("/") || file.endsWith(File.separator)))
	      loader = new JarURLLoader(this, newUrl);
	    else if ("file".equals(newUrl.getProtocol()))
	      loader = new FileURLLoader(this, newUrl);
	    else
	      loader = new RemoteURLLoader(this, newUrl);

	    // cache it
	    urlloaders.put(newUrl, loader);
	  }

	urls.add(newUrl);
	urlinfos.add(loader);
      }
  }

  /**
   * Adds an array of new locations to the end of the internal URL store.
   * @param newUrls the locations to add
   */
  private void addURLs(URL[] newUrls)
  {
    for (int i = 0; i < newUrls.length; i++)
    {
      addURL(newUrls[i]);
    }
  }

  /** 
   * Defines a Package based on the given name and the supplied manifest
   * information. The manifest indicates the tile, version and
   * vendor information of the specification and implementation and wheter the
   * package is sealed. If the Manifest indicates that the package is sealed
   * then the Package will be sealed with respect to the supplied URL.
   *
   * @exception IllegalArgumentException If this package name already exists
   * in this class loader
   * @param name The name of the package
   * @param manifest The manifest describing the specification,
   * implementation and sealing details of the package
   * @param url the code source url to seal the package
   * @return the defined Package
   */
  protected Package definePackage(String name, Manifest manifest, URL url) 
    throws IllegalArgumentException
  {
    Attributes attr = manifest.getMainAttributes();
    String specTitle =
      attr.getValue(Attributes.Name.SPECIFICATION_TITLE); 
    String specVersion =
      attr.getValue(Attributes.Name.SPECIFICATION_VERSION); 
    String specVendor =
      attr.getValue(Attributes.Name.SPECIFICATION_VENDOR); 
    String implTitle =
      attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE); 
    String implVersion =
      attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION); 
    String implVendor =
      attr.getValue(Attributes.Name.IMPLEMENTATION_VENDOR);

    // Look if the Manifest indicates that this package is sealed
    // XXX - most likely not completely correct!
    // Shouldn't we also check the sealed attribute of the complete jar?
    // http://java.sun.com/products/jdk/1.4/docs/guide/extensions/spec.html#bundled
    // But how do we get that jar manifest here?
    String sealed = attr.getValue(Attributes.Name.SEALED);
    if ("false".equals(sealed))
    {
      // make sure that the URL is null so the package is not sealed
      url = null;
    }

    return definePackage(name, specTitle, specVersion, specVendor,
			 implTitle, implVersion, implVendor, url);
  }

  /**
   * Finds (the first) class by name from one of the locations. The locations
   * are searched in the order they were added to the URLClassLoader.
   *
   * @param className the classname to find
   * @exception ClassNotFoundException when the class could not be found or
   * loaded
   * @return a Class object representing the found class
   */
  protected Class findClass(final String className)
    throws ClassNotFoundException
  {
    // Just try to find the resource by the (almost) same name
    String resourceName = className.replace('.', '/') + ".class";
    Resource resource = findURLResource(resourceName);
    if (resource == null)
      throw new ClassNotFoundException(className + " not found in " + urls);

    // Try to read the class data, create the CodeSource, Package and
    // construct the class (and watch out for those nasty IOExceptions)
    try
      {
	byte [] data;
	InputStream in = resource.getInputStream();
	int length = resource.getLength();
	if (length != -1)
	  {
	    // We know the length of the data.
	    // Just try to read it in all at once
	    data = new byte[length];
	    int pos = 0;
	    while(length - pos > 0)
	      {
		int len = in.read(data, pos, length - pos);
		if (len == -1)
		  throw new EOFException("Not enough data reading from: "
					 + in);
		pos += len;
	      }
	  }
	else
	  {
	    // We don't know the data length.
	    // Have to read it in chunks.
	    ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
	    byte b[] = new byte[4096];
	    int l = 0;
	    while (l != -1)
	      {
		l = in.read(b);
		if (l != -1)
		  out.write(b, 0, l);
	      }
	    data = out.toByteArray();
	  }
	final byte[] classData = data;

	// Now get the CodeSource
	final CodeSource source = resource.getCodeSource();
	
	// Find out package name
	String packageName = null;
	int lastDot = className.lastIndexOf('.');
	if (lastDot != -1)
	  packageName = className.substring(0, lastDot);
	
	if (packageName != null && getPackage(packageName) == null)
	  {
	    // define the package
	    Manifest manifest = resource.loader.getManifest();
	    if (manifest == null)
	      definePackage(packageName,
			    null, null, null, null, null, null, null);
	    else
	      definePackage(packageName, manifest, resource.loader.baseURL);
	  }
	
	// And finally construct the class!
	SecurityManager sm = System.getSecurityManager();
	if (sm != null && securityContext != null)
	  {
	    return (Class)AccessController.doPrivileged
	      (new PrivilegedAction()
		{
		  public Object run()
		  {
		    return defineClass(className, classData,
				       0, classData.length,
				       source);
		  }
		}, securityContext);
	  }
	else
	  return defineClass(className, classData,
			     0, classData.length,
			     source);
      }
    catch (IOException ioe)
      {
	throw new ClassNotFoundException(className, ioe);
      }
  }

  /**
   * Finds the first occurrence of a resource that can be found. The locations
   * are searched in the order they were added to the URLClassLoader.
   *
   * @param resourceName the resource name to look for
   * @return the URLResource for the resource if found, null otherwise
   */
  private Resource findURLResource(String resourceName)
  {
    int max = urls.size();
    for (int i = 0; i < max; i++)
      {
	URLLoader loader = (URLLoader)urlinfos.elementAt(i);
	if (loader == null)
	  continue;
	
	Resource resource = loader.getResource(resourceName);
	if (resource != null)
	  return resource;
      }
    return null;
  }

  /**
   * Finds the first occurrence of a resource that can be found.
   *
   * @param resourceName the resource name to look for
   * @return the URL if found, null otherwise
   */
  public URL findResource(String resourceName)
  {
    Resource resource = findURLResource(resourceName);
    if (resource != null)
      return resource.getURL();
    
    // Resource not found
    return null;
  }

  /**
   * If the URLStreamHandlerFactory has been set this return the appropriate
   * URLStreamHandler for the given protocol, if not set returns null.
   *
   * @param protocol the protocol for which we need a URLStreamHandler
   * @return the appropriate URLStreamHandler or null
   */
  URLStreamHandler getURLStreamHandler(String protocol)
  {
    if (factory == null)
      return null;

    URLStreamHandler handler;
    synchronized (factoryCache)
      {
	// check if there're handler for the same protocol in cache
	HashMap cache = (HashMap)factoryCache.get(factory);
	handler = (URLStreamHandler)cache.get(protocol);
	if(handler == null)
	  {
	    // add it to cache
	    handler = factory.createURLStreamHandler(protocol);
	    cache.put(protocol, handler);
	  }
      }
    return handler;
  }

  /**
   * Finds all the resources with a particular name from all the locations.
   *
   * @exception IOException when an error occurs accessing one of the
   * locations
   * @param resourceName the name of the resource to lookup
   * @return a (possible empty) enumeration of URLs where the resource can be
   * found
   */
  public Enumeration findResources(String resourceName) throws IOException
  {
    Vector resources = new Vector();
    int max = urls.size();
    for (int i = 0; i < max; i++)
      {
	URLLoader loader = (URLLoader)urlinfos.elementAt(i);
	Resource resource = loader.getResource(resourceName);
	if (resource != null)
	  resources.add(resource.getURL());
      }
    return resources.elements();
  }

  /**
   * Returns the permissions needed to access a particular code
   * source.  These permissions includes those returned by
   * <code>SecureClassLoader.getPermissions()</code> and the actual
   * permissions to access the objects referenced by the URL of the
   * code source.  The extra permissions added depend on the protocol
   * and file portion of the URL in the code source. If the URL has
   * the "file" protocol ends with a '/' character then it must be a
   * directory and a file Permission to read everything in that
   * directory and all subdirectories is added. If the URL had the
   * "file" protocol and doesn't end with a '/' character then it must
   * be a normal file and a file permission to read that file is
   * added. If the <code>URL</code> has any other protocol then a
   * socket permission to connect and accept connections from the host
   * portion of the URL is added.
   *
   * @param source The codesource that needs the permissions to be accessed
   * @return the collection of permissions needed to access the code resource
   * @see java.security.SecureClassLoader#getPermissions()
   */
  protected PermissionCollection getPermissions(CodeSource source)
  {
    // XXX - This implementation does exactly as the Javadoc describes.
    // But maybe we should/could use URLConnection.getPermissions()?

    // First get the permissions that would normally be granted
    PermissionCollection permissions = super.getPermissions(source);
        
    // Now add the any extra permissions depending on the URL location
    URL url = source.getLocation();
    String protocol = url.getProtocol();
    if (protocol.equals("file"))
      {
	String file = url.getFile();
	// If the file end in / it must be an directory
	if (file.endsWith("/") || file.endsWith(File.separator))
	  {
	    // Grant permission to read everything in that directory and
	    // all subdirectories
	    permissions.add(new FilePermission(file + "-", "read"));
	  }
	else
	  {
	    // It is a 'normal' file
	    // Grant permission to access that file
	    permissions.add(new FilePermission(file, "read"));
	  }
      }
    else
      {
	// Grant permission to connect to and accept connections from host
	String host = url.getHost();
	if (host != null)
	  permissions.add(new SocketPermission(host, "connect,accept"));
      }

    return permissions;
  }
    
  /**
   * Returns all the locations that this class loader currently uses the
   * resolve classes and resource. This includes both the initially supplied
   * URLs as any URLs added later by the loader.
   * @return All the currently used URLs
   */
  public URL[] getURLs()
  {
    return (URL[]) urls.toArray(new URL[urls.size()]);
  }

  /**
   * Creates a new instance of a <code>URLClassLoader</code> that gets
   * classes from the supplied <code>URL</code>s. This class loader
   * will have as parent the standard system class loader.
   *
   * @param urls the initial URLs used to resolve classes and
   * resources
   *
   * @exception SecurityException when the calling code does not have
   * permission to access the given <code>URL</code>s
   */
  public static URLClassLoader newInstance(URL urls[])
    throws SecurityException
  {
    return newInstance(urls, null);
  }

  /**
   * Creates a new instance of a <code>URLClassLoader</code> that gets
   * classes from the supplied <code>URL</code>s and with the supplied
   * loader as parent class loader.
   *
   * @param urls the initial URLs used to resolve classes and
   * resources
   * @param parent the parent class loader
   *
   * @exception SecurityException when the calling code does not have
   * permission to access the given <code>URL</code>s
   */
  public static URLClassLoader newInstance(URL urls[],
					   final ClassLoader parent)
    throws SecurityException
  {
    SecurityManager sm = System.getSecurityManager();
    if (sm == null)
      return new URLClassLoader(urls, parent);
    else
      {
	final Object securityContext = sm.getSecurityContext();
	// XXX - What to do with anything else then an AccessControlContext?
	if (!(securityContext instanceof AccessControlContext))
	  throw new SecurityException
	    ("securityContext must be AccessControlContext: "
	     + securityContext);
	
	URLClassLoader loader =
	  (URLClassLoader)AccessController.doPrivileged(new PrivilegedAction()
	    {
	      public Object run()
	      {
		return new URLClassLoader
		  (parent, (AccessControlContext)securityContext);
	      }
	    });
	loader.addURLs(urls);
	return loader;
      }
  }
}
