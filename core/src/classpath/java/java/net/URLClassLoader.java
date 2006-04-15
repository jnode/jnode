/* URLClassLoader.java --  ClassLoader that loads classes from one or more URLs
   Copyright (C) 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation, Inc.

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
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

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
import java.util.Iterator;
import java.util.StringTokenizer;
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
   * Store pre-parsed information for each url into this vector: each
   * element is a URL loader.  A jar file has its own class-path
   * attribute which adds to the URLs that will be searched, but this
   * does not add to the list of urls.
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
  abstract static class URLLoader
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
      this(classloader, baseURL, baseURL);
    }

    URLLoader(URLClassLoader classloader, URL baseURL, URL overrideURL)
    {
      this.classloader = classloader;
      this.baseURL = baseURL;
      this.noCertCodeSource = new CodeSource(overrideURL, null);
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

    Vector getClassPath()
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
  abstract static class Resource
  {
    final URLLoader loader;

    Resource(URLLoader loader)
    {
      this.loader = loader;
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
  static final class JarURLLoader extends URLLoader
  {
    final JarFile jarfile; // The jar file for this url
    final URL baseJarURL;  // Base jar: url for all resources loaded from jar

    Vector classPath;	// The "Class-Path" attribute of this Jar's manifest

    public JarURLLoader(URLClassLoader classloader, URL baseURL,
			URL absoluteUrl)
    {
      super(classloader, baseURL, absoluteUrl);

      // Cache url prefix for all resources in this jar url.
      String external = baseURL.toExternalForm();
      StringBuffer sb = new StringBuffer(external.length() + 6);
      sb.append("jar:");
      sb.append(external);
      sb.append("!/");
      String jarURL = sb.toString();

      this.classPath = null;
      URL baseJarURL = null;
      JarFile jarfile = null;
      try
	{
	  baseJarURL =
	    new URL(null, jarURL, classloader.getURLStreamHandler("jar"));

	  jarfile =
	    ((JarURLConnection) baseJarURL.openConnection()).getJarFile();
	  
	  Manifest manifest;
	  Attributes attributes;
	  String classPathString;

	  if ((manifest = jarfile.getManifest()) != null
	      && (attributes = manifest.getMainAttributes()) != null
	      && ((classPathString 
		   = attributes.getValue(Attributes.Name.CLASS_PATH)) 
		  != null))
	    {
	      this.classPath = new Vector();
	      
	      StringTokenizer st = new StringTokenizer(classPathString, " ");
	      while (st.hasMoreElements ()) 
		{  
		  String e = st.nextToken ();
		  try
		    {
		      URL url = new URL(baseURL, e);
		      this.classPath.add(url);
		    } 
		  catch (java.net.MalformedURLException xx)
		    {
		      // Give up
		    }
		}
	    }
        }
      catch (IOException ioe)
        {
	  /* ignored */
	}

      this.baseJarURL = baseJarURL;
      this.jarfile = jarfile;
    }

    /** get resource with the name "name" in the jar url */
    Resource getResource(String name)
    {
      if (jarfile == null)
	return null;

      if (name.startsWith("/"))
	name = name.substring(1);

      JarEntry je = jarfile.getJarEntry(name);
      if (je != null)
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

    Vector getClassPath()
    {
      return classPath;
    }
  }

  static final class JarURLResource extends Resource
  {
    private final JarEntry entry;
    private final String name;

    JarURLResource(JarURLLoader loader, String name, JarEntry entry)
    {
      super(loader);
      this.entry = entry;
      this.name = name;
    }

    InputStream getInputStream() throws IOException
    {
      return ((JarURLLoader) loader).jarfile.getInputStream(entry);
    }

    int getLength()
    {
      return (int) entry.getSize();
    }

    Certificate[] getCertificates()
    {
      // We have to get the entry from the jar file again, because the
      // certificates will not be available until the entire entry has
      // been read.
      return ((JarEntry) ((JarURLLoader) loader).jarfile.getEntry(name))
        .getCertificates();
    }

    URL getURL()
    {
      try
	{
	  return new URL(((JarURLLoader) loader).baseJarURL, name,
			 loader.classloader.getURLStreamHandler("jar"));
	}
      catch (MalformedURLException e)
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
  static final class RemoteURLLoader extends URLLoader
  {
    private final String protocol;

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
	  URL url =
	    new URL(baseURL, name, classloader.getURLStreamHandler(protocol));
	  URLConnection connection = url.openConnection();

	  // Open the connection and check the stream
	  // just to be sure it exists.
	  int length = connection.getContentLength();
	  InputStream stream = connection.getInputStream();

	  // We can do some extra checking if it is a http request
	  if (connection instanceof HttpURLConnection)
	    {
	      int response =
		((HttpURLConnection) connection).getResponseCode();
	      if (response / 100 != 2)
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
  static final class RemoteResource extends Resource
  {
    private final URL url;
    private final InputStream stream;
    private final int length;

    RemoteResource(RemoteURLLoader loader, String name, URL url,
		   InputStream stream, int length)
    {
      super(loader);
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
  static final class FileURLLoader extends URLLoader
  {
    File dir;   //the file for this file url

    FileURLLoader(URLClassLoader classloader, URL url, URL absoluteUrl)
    {
      super(classloader, url, absoluteUrl);
      dir = new File(absoluteUrl.getFile());
    }

    /** get resource with the name "name" in the file url */
    Resource getResource(String name)
    {
      try 
	{
          // Make sure that all components in name are valid by walking through
          // them
          File file = walkPathComponents(name);

          if (file == null)
            return null;

	    return new FileResource(this, file);
	}
      catch (IOException e)
	{
	  // Fall through...
	}
      return null;
    }

    /**
     * Walk all path tokens and check them for validity. At no moment, we are
     * allowed to reach a directory located "above" the root directory, stored
     * in "dir" property. We are also not allowed to enter a non existing
     * directory or a non directory component (plain file, symbolic link, ...).
     * An empty or null path is valid. Pathnames components are separated by
     * <code>File.separatorChar</code>
     * 
     * @param resourceFileName the name to be checked for validity.
     * @return the canonical file pointed by the resourceFileName or null if the
     *         walking failed
     * @throws IOException in case of issue when creating the canonical
     *           resulting file
     * @see File#separatorChar
     */
    private File walkPathComponents(String resourceFileName) throws IOException
    {
      StringTokenizer stringTokenizer = new StringTokenizer(resourceFileName, File.separator);
      File currentFile = dir;
      int tokenCount = stringTokenizer.countTokens();

      for (int i = 0; i < tokenCount - 1; i++)
        {
          String currentToken = stringTokenizer.nextToken();
          
          // If we are at the root directory and trying to go up, the walking is
          // finished with an error
          if ("..".equals(currentToken) && currentFile.equals(dir))
            return null;
          
          currentFile = new File(currentFile, currentToken);

          // If the current file doesn't exist or is not a directory, the walking is
          // finished with an error
          if (! (currentFile.exists() && currentFile.isDirectory()))
            return null;
          
        }
      
      // Treat the last token differently, if it exists, because it does not need
      // to be a directory
      if (tokenCount > 0)
        {
          String currentToken = stringTokenizer.nextToken();
          
          if ("..".equals(currentToken) && currentFile.equals(dir))
            return null;
          
          currentFile = new File(currentFile, currentToken);

          // If the current file doesn't exist, the walking is
          // finished with an error
          if (! currentFile.exists())
            return null;
      }
      
      return currentFile.getCanonicalFile();
    }
  }

  static final class FileResource extends Resource
  {
    final File file;

    FileResource(FileURLLoader loader, File file)
    {
      super(loader);
      this.file = file;
    }

    InputStream getInputStream() throws IOException
    {
      return new FileInputStream(file);
    }
                        
    public int getLength()
    {
      return (int) file.length();
    }

    public URL getURL()
    {
      try
	{
          return file.toURL();
	}
      catch (MalformedURLException e)
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
   * @param urls Locations that should be searched by this ClassLoader when
   * resolving Classes or Resources.
   * @exception SecurityException if the SecurityManager disallows the
   * creation of a ClassLoader.
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
   * Creates a <code>URLClassLoader</code> that gets classes from the supplied
   * <code>URL</code>s.
   * To determine if this classloader may be created the constructor of
   * the super class (<code>SecureClassLoader</code>) is called first, which
   * can throw a SecurityException. Then the supplied URLs are added
   * in the order given to the URLClassLoader which uses these URLs to
   * load classes and resources (after using the supplied parent ClassLoader).
   * @param urls Locations that should be searched by this ClassLoader when
   * resolving Classes or Resources.
   * @param parent The parent class loader used before trying this class
   * loader.
   * @exception SecurityException if the SecurityManager disallows the
   * creation of a ClassLoader.
   * @exception SecurityException
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

  // Package-private to avoid a trampoline constructor.
  /**
   * Package-private constructor used by the static
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
  URLClassLoader(ClassLoader parent, AccessControlContext securityContext)
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
   * @param urls Locations that should be searched by this ClassLoader when
   * resolving Classes or Resources.
   * @param parent The parent class loader used before trying this class
   * loader.
   * @param factory Used to get the protocol handler for the URLs.
   * @exception SecurityException if the SecurityManager disallows the
   * creation of a ClassLoader.
   * @exception SecurityException
   * @see SecureClassLoader
   */
  public URLClassLoader(URL[] urls, ClassLoader parent,
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
    synchronized (factoryCache)
      {
	if (factory != null && factoryCache.get(factory) == null)
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
    urls.add(newUrl);
    addURLImpl(newUrl);
  }

  private void addURLImpl(URL newUrl)
  {
    synchronized (this)
      {
	if (newUrl == null)
	  return; // Silently ignore...
        
	// Reset the toString() value.
	thisString = null;

	// Check global cache to see if there're already url loader
	// for this url.
	URLLoader loader = (URLLoader) urlloaders.get(newUrl);
	if (loader == null)
	  {
	    String file = newUrl.getFile();
	    String protocol = newUrl.getProtocol();

	    // If we have a file: URL, we want to make it absolute
	    // here, before we decide whether it is really a jar.
	    URL absoluteURL;
	    if ("file".equals (protocol))
	      {
		File dir = new File(file);
		URL absUrl;
		try
		  {
		    absoluteURL = dir.getCanonicalFile().toURL();
		  }
		catch (IOException ignore)
		  {
		    try
		      {
			absoluteURL = dir.getAbsoluteFile().toURL();
		      }
		    catch (MalformedURLException _)
		      {
			// This really should not happen.
			absoluteURL = newUrl;
		      }
		  }
	      }
	    else
	      {
		// This doesn't hurt, and it simplifies the logic a
		// little.
		absoluteURL = newUrl;
	      }

	    // Check that it is not a directory
	    if (! (file.endsWith("/") || file.endsWith(File.separator)))
              loader = new JarURLLoader(this, newUrl, absoluteURL);
	    else if ("file".equals(protocol))
              loader = new FileURLLoader(this, newUrl, absoluteURL);
	    else
	      loader = new RemoteURLLoader(this, newUrl);

	    // Cache it.
	    urlloaders.put(newUrl, loader);
	  }

	urlinfos.add(loader);

	Vector extraUrls = loader.getClassPath();
	if (extraUrls != null)
	  {
	    Iterator it = extraUrls.iterator();
	    while (it.hasNext())
	      {
		URL url = (URL)it.next();
		URLLoader extraLoader = (URLLoader) urlloaders.get(url);
		if (! urlinfos.contains (extraLoader))
		  addURLImpl(url);
	      }
	  }

      }
  }

  /**
   * Adds an array of new locations to the end of the internal URL
   * store.  Called from the the constructors. Should not call to the
   * protected addURL() method since that can be overridden and
   * subclasses are not yet in a good state at this point.
   * jboss 4.0.3 for example depends on this.
   *
   * @param newUrls the locations to add
   */
  private void addURLs(URL[] newUrls)
  {
    for (int i = 0; i < newUrls.length; i++)
      {
	urls.add(newUrls[i]);
	addURLImpl(newUrls[i]);
      }
  }

  /** 
   * Look in both Attributes for a given value.  The first Attributes
   * object, if not null, has precedence.
   */
  private String getAttributeValue(Attributes.Name name, Attributes first,
				   Attributes second)
  {
    String result = null;
    if (first != null)
      result = first.getValue(name);
    if (result == null)
      result = second.getValue(name);
    return result;
  }

  /**
   * Defines a Package based on the given name and the supplied manifest
   * information. The manifest indicates the title, version and
   * vendor information of the specification and implementation and whether the
   * package is sealed. If the Manifest indicates that the package is sealed
   * then the Package will be sealed with respect to the supplied URL.
   *
   * @param name The name of the package
   * @param manifest The manifest describing the specification,
   * implementation and sealing details of the package
   * @param url the code source url to seal the package
   * @return the defined Package
   * @throws IllegalArgumentException If this package name already exists
   * in this class loader
   */
  protected Package definePackage(String name, Manifest manifest, URL url) 
    throws IllegalArgumentException
  {
    // Compute the name of the package as it may appear in the
    // Manifest.
    StringBuffer xform = new StringBuffer(name);
    for (int i = xform.length () - 1; i >= 0; --i)
      if (xform.charAt(i) == '.')
	xform.setCharAt(i, '/');
    xform.append('/');
    String xformName = xform.toString();

    Attributes entryAttr = manifest.getAttributes(xformName);
    Attributes attr = manifest.getMainAttributes();

    String specTitle
      = getAttributeValue(Attributes.Name.SPECIFICATION_TITLE,
			  entryAttr, attr);
    String specVersion
      = getAttributeValue(Attributes.Name.SPECIFICATION_VERSION,
			  entryAttr, attr);
    String specVendor
      = getAttributeValue(Attributes.Name.SPECIFICATION_VENDOR,
			  entryAttr, attr);
    String implTitle
      = getAttributeValue(Attributes.Name.IMPLEMENTATION_TITLE,
			  entryAttr, attr);
    String implVersion
      = getAttributeValue(Attributes.Name.IMPLEMENTATION_VERSION,
			  entryAttr, attr);
    String implVendor
      = getAttributeValue(Attributes.Name.IMPLEMENTATION_VENDOR,
			  entryAttr, attr);

    // Look if the Manifest indicates that this package is sealed
    // XXX - most likely not completely correct!
    // Shouldn't we also check the sealed attribute of the complete jar?
    // http://java.sun.com/products/jdk/1.4/docs/guide/extensions/spec.html#bundled
    // But how do we get that jar manifest here?
    String sealed = attr.getValue(Attributes.Name.SEALED);
    if ("false".equals(sealed))
      // make sure that the URL is null so the package is not sealed
      url = null;

    return definePackage(name,
			 specTitle, specVendor, specVersion,
			 implTitle, implVendor, implVersion,
			 url);
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
      throw new ClassNotFoundException(className + " not found in " + this);

    // Try to read the class data, create the CodeSource, Package and
    // construct the class (and watch out for those nasty IOExceptions)
    try
      {
	byte[] data;
	InputStream in = resource.getInputStream();
	try
	  {
	int length = resource.getLength();
	if (length != -1)
	  {
	    // We know the length of the data.
	    // Just try to read it in all at once
	    data = new byte[length];
	    int pos = 0;
		while (length - pos > 0)
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
		byte[] b = new byte[4096];
	    int l = 0;
	    while (l != -1)
	      {
		l = in.read(b);
		if (l != -1)
		  out.write(b, 0, l);
	      }
	    data = out.toByteArray();
	  }
	  }
	finally
	  {
	    in.close();
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
	      definePackage(packageName, null, null, null, null, null, null,
	                    null);
	    else
	      definePackage(packageName, manifest, resource.loader.baseURL);
	  }
	
	// And finally construct the class!
	SecurityManager sm = System.getSecurityManager();
        Class result = null;
	if (sm != null && securityContext != null)
	  {
            result = (Class)AccessController.doPrivileged
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
          result = defineClass(className, classData, 0, classData.length, source);

        // Avoid NullPointerExceptions.
        Certificate[] resourceCertificates = resource.getCertificates();
        if(resourceCertificates != null)
          super.setSigners(result, resourceCertificates);
        
        return result;
      }
    catch (IOException ioe)
      {
	ClassNotFoundException cnfe;
	cnfe = new ClassNotFoundException(className + " not found in " + this);
	cnfe.initCause(ioe);
	throw cnfe;
      }
  }
  
  // Cached String representation of this URLClassLoader
  private String thisString;
  
  /**
   * Returns a String representation of this URLClassLoader giving the
   * actual Class name, the URLs that are searched and the parent
   * ClassLoader.
   */
  public String toString()
  {
    synchronized (this)
      {
	if (thisString == null)
	  {
	    StringBuffer sb = new StringBuffer();
	    sb.append(this.getClass().getName());
	    sb.append("{urls=[" );
	    URL[] thisURLs = getURLs();
	    for (int i = 0; i < thisURLs.length; i++)
	      {
		sb.append(thisURLs[i]);
		if (i < thisURLs.length - 1)
		  sb.append(',');
	      }
	    sb.append(']');
	    sb.append(", parent=");
	    sb.append(getParent());
	    sb.append('}');
	    thisString = sb.toString();
	  }
	return thisString;
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
    int max = urlinfos.size();
    for (int i = 0; i < max; i++)
      {
	URLLoader loader = (URLLoader) urlinfos.elementAt(i);
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
	// Check if there're handler for the same protocol in cache.
	HashMap cache = (HashMap) factoryCache.get(factory);
	handler = (URLStreamHandler) cache.get(protocol);
	if (handler == null)
	  {
	    // Add it to cache.
	    handler = factory.createURLStreamHandler(protocol);
	    cache.put(protocol, handler);
	  }
      }
    return handler;
  }

  /**
   * Finds all the resources with a particular name from all the locations.
   *
   * @param resourceName the name of the resource to lookup
   * @return a (possible empty) enumeration of URLs where the resource can be
   * found
   * @exception IOException when an error occurs accessing one of the
   * locations
   */
  public Enumeration findResources(String resourceName)
    throws IOException
  {
    Vector resources = new Vector();
    int max = urlinfos.size();
    for (int i = 0; i < max; i++)
      {
	URLLoader loader = (URLLoader) urlinfos.elementAt(i);
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
   * @see java.security.SecureClassLoader#getPermissions(CodeSource)
   */
  protected PermissionCollection getPermissions(CodeSource source)
  {
    // XXX - This implementation does exactly as the Javadoc describes.
    // But maybe we should/could use URLConnection.getPermissions()?
    // First get the permissions that would normally be granted
    PermissionCollection permissions = super.getPermissions(source);
        
    // Now add any extra permissions depending on the URL location.
    URL url = source.getLocation();
    String protocol = url.getProtocol();
    if (protocol.equals("file"))
      {
	String file = url.getFile();

	// If the file end in / it must be an directory.
	if (file.endsWith("/") || file.endsWith(File.separator))
	  {
	    // Grant permission to read everything in that directory and
	  // all subdirectories.
	    permissions.add(new FilePermission(file + "-", "read"));
	  }
	else
	  {
	  // It is a 'normal' file.
	  // Grant permission to access that file.
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
   * @return the class loader
   *
   * @exception SecurityException when the calling code does not have
   * permission to access the given <code>URL</code>s
   */
  public static URLClassLoader newInstance(URL[] urls)
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
   * @return the class loader
   *
   * @exception SecurityException when the calling code does not have
   * permission to access the given <code>URL</code>s
   */
  public static URLClassLoader newInstance(URL[] urls, final ClassLoader parent)
    throws SecurityException
  {
    SecurityManager sm = System.getSecurityManager();
    if (sm == null)
      return new URLClassLoader(urls, parent);
    else
      {
	final Object securityContext = sm.getSecurityContext();

	// XXX - What to do with anything else then an AccessControlContext?
	if (! (securityContext instanceof AccessControlContext))
	  throw new SecurityException("securityContext must be AccessControlContext: "
	     + securityContext);
	
	URLClassLoader loader =
	  (URLClassLoader) AccessController.doPrivileged(new PrivilegedAction()
	    {
	      public Object run()
	      {
		  return new URLClassLoader(parent,
		                            (AccessControlContext) securityContext);
	      }
	    });
	loader.addURLs(urls);
	return loader;
      }
  }
}
