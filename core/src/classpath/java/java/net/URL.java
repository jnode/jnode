/* URL.java -- Uniform Resource Locator Class
   Copyright (C) 1998, 1999, 2000, 2002 Free Software Foundation, Inc.

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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.StringTokenizer;

/*
 * Written using on-line Java Platform 1.2 API Specification, as well
 * as "The Java Class Libraries", 2nd edition (Addison-Wesley, 1998).
 * Status:  Believed complete and correct.
 */

/**
  * This final class represents an Internet Uniform Resource Locator (URL).
  * For details on the syntax of URL's and what they can be used for,
  * refer to RFC 1738, available from <a 
  * href="http://ds.internic.net/rfcs/rfc1738.txt">http://ds.internic.net/rfcs/rfc1738.txt</a>
  * <p>
  * There are a great many protocols supported by URL's such as "http",
  * "ftp", and "file".  This object can handle any arbitrary URL for which
  * a URLStreamHandler object can be written.  Default protocol handlers
  * are provided for the "http" and "ftp" protocols.  Additional protocols
  * handler implementations may be provided in the future.  In any case,
  * an application or applet can install its own protocol handlers that
  * can be "chained" with other protocol hanlders in the system to extend
  * the base functionality provided with this class. (Note, however, that
  * unsigned applets cannot access properties by default or install their
  * own protocol handlers).
  * <p>
  * This chaining is done via the system property java.protocol.handler.pkgs
  * If this property is set, it is assumed to be a "|" separated list of
  * package names in which to attempt locating protocol handlers.  The
  * protocol handler is searched for by appending the string 
  * ".<protocol>.Handler" to each packed in the list until a hander is found.
  * If a protocol handler is not found in this list of packages, or if the
  * property does not exist, then the default protocol handler of
  * "gnu.java.net.<protocol>.Handler" is tried.  If this is
  * unsuccessful, a MalformedURLException is thrown.
  * <p>
  * All of the constructor methods of URL attempt to load a protocol
  * handler and so any needed protocol handlers must be installed when
  * the URL is constructed.
  * <p>
  * Here is an example of how URL searches for protocol handlers.  Assume
  * the value of java.protocol.handler.pkgs is "com.foo|com.bar" and the
  * URL is "news://comp.lang.java.programmer".  URL would looking the 
  * following places for protocol handlers:
  * <p><pre>
  * com.foo.news.Handler
  * com.bar.news.Handler
  * gnu.java.net.news.Handler
  * </pre><p>
  * If the protocol handler is not found in any of those locations, a
  * MalformedURLException would be thrown.
  * <p>
  * Please note that a protocol handler must be a subclass of
  * URLStreamHandler.
  * <p>
  * Normally, this class caches protocol handlers.  Once it finds a handler
  * for a particular protocol, it never tries to look up a new handler
  * again.  However, if the system property
  * gnu.java.net.nocache_protocol_handlers is set, then this
  * caching behavior is disabled.  This property is specific to this
  * implementation.  Sun's JDK may or may not do protocol caching, but it
  * almost certainly does not examine this property.
  * <p>
  * Please also note that an application can install its own factory for
  * loading protocol handlers (see setURLStreamHandlerFactory).  If this is
  * done, then the above information is superseded and the behavior of this
  * class in loading protocol handlers is dependent on that factory.
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  * @author Warren Levy <warrenl@cygnus.com>
  *
  * @see URLStreamHandler
  */
public final class URL implements Serializable {
	/**
	 * The name of the protocol for this URL.
	 * The protocol is always stored in lower case.
	 */
	private String protocol;

	/**
	 * The "authority" portion of the URL.
	 */
	private String authority;

	/**
	 * The hostname or IP address of this protocol.
	 * This includes a possible user. For example <code>joe@some.host.net</code>.
	 */
	private String host;

	/**
	 * The port number of this protocol or -1 if the port number used is
	 * the default for this protocol.
	 */
	private int port = -1; // Initialize for constructor using context.

	/**
	 * The "file" portion of the URL. It is defined as <code>path[?query]</code>.
	 */
	private String file;

	/**
	 * The anchor portion of the URL.
	 */
	private String ref;

	/**
	 * This is the hashCode for this URL
	 */
	private int hashCode = 0;

	/**
	 * The protocol handler in use for this URL
	 */
	transient private URLStreamHandler handler;

	/**
	 * This a table where we cache protocol handlers to avoid the overhead
	 * of looking them up each time.
	 */
	//private static Hashtable handlers = new Hashtable();

	/**
	 * If an application installs its own protocol handler factory, this is
	 * where we keep track of it.
	 */
	private static URLStreamHandlerFactory factory;

	private static final long serialVersionUID = -7627629688361524110L;

	/**
	  * This a table where we cache protocol handlers to avoid the overhead
	  * of looking them up each time.
	  */
	private static Hashtable ph_cache = new Hashtable();

	/**
	  * Whether or not to cache protocol handlers.
	  */
	private static boolean cache_handlers;

	/**
	  * The search path of packages to search for protocol handlers in.
	  */
	private static String ph_search_path;

	static {
		String s = System.getProperty("gnu.java.net.nocache_protocol_handlers");
		if (s == null)
			cache_handlers = true;
		else
			cache_handlers = false;

		ph_search_path = System.getProperty("java.protocol.handler.pkgs");

		// Tack our default package on at the ends
		if (ph_search_path != null)
			ph_search_path = "org.jnode.protocol" + "|" + ph_search_path + "|" + "gnu.java.net.protocol";
		else
			ph_search_path = "org.jnode.protocol" + "|" + "gnu.java.net.protocol";
	}

	/**
	  * This internal method is used in two different constructors to load
	  * a protocol handler for this URL.
	  *
	  * @param protocol The protocol to load a handler for
	  *
	  * @return A URLStreamHandler for this protocol, or null when not found.
	  */
	private static synchronized URLStreamHandler getURLStreamHandler(String protocol) {
		URLStreamHandler ph;

		// First, see if a protocol handler is in our cache
		if (cache_handlers) {
			Class cls = (Class)ph_cache.get(protocol);
			if (cls != null) {
				try {
					ph = (URLStreamHandler)cls.newInstance();
					return (ph);
				} catch (Exception e) {
					// Do nothing
				}
			}
		}

		// Next check the factory and use that if set
		if (factory != null) {
			ph = factory.createURLStreamHandler(protocol);
			if (ph != null) {
				if (cache_handlers)
					ph_cache.put(protocol, ph.getClass());

				return (ph);
			}
		}

		// Finally loop through our search path looking for a match
		StringTokenizer st = new StringTokenizer(ph_search_path, "|");
		while (st.hasMoreTokens()) {
			String clsname = st.nextToken() + "." + protocol + ".Handler";

			try {
				final ClassLoader cl = Thread.currentThread().getContextClassLoader();
				final Class cls = cl.loadClass(clsname);
				Object obj = cls.newInstance();
				if (!(obj instanceof URLStreamHandler))
					continue;
				else
					ph = (URLStreamHandler)obj;

				if (cache_handlers)
					ph_cache.put(protocol, cls);

				return (ph);
			} catch (Exception e) {
				// Do nothing
			}
		}

		// Still here, which is bad
		return null;
	}

	/**
	 * Constructs a URL and loads a protocol handler for the values passed as
	 * arguments.
	 * 
	 * @param protocol The protocol for this URL ("http", "ftp", etc)
	 * @param host The hostname or IP address to connect to
	 * @param port The port number to use, or -1 to use the protocol's
	 * default port
	 * @param file The "file" portion of the URL.
	 *
	 * @exception MalformedURLException If a protocol handler cannot be loaded or
	 * a parse error occurs.
	 */
	public URL(String protocol, String host, int port, String file)
		throws MalformedURLException {
		this(protocol, host, port, file, null);
	}

	/**
	 * Constructs a URL and loads a protocol handler for the values passed in
	 * as arugments.  Uses the default port for the protocol.
	 *
	 * @param protocol The protocol for this URL ("http", "ftp", etc)
	 * @param host The hostname or IP address for this URL
	 * @param file The "file" portion of this URL.
	 *
	 * @exception MalformedURLException If a protocol handler cannot be loaded or
	 * a parse error occurs.
	 */
	public URL(String protocol, String host, String file)
		throws MalformedURLException {
		this(protocol, host, -1, file, null);
	}

	/**
	 * This method initializes a new instance of <code>URL</code> with the
	 * specified protocol, host, port, and file.  Additionally, this method
	 * allows the caller to specify a protocol handler to use instead of 
	 * the default.  If this handler is specified, the caller must have
	 * the "specifyStreamHandler" permission (see <code>NetPermission</code>)
	 * or a <code>SecurityException</code> will be thrown.
	 *
	 * @param protocol The protocol for this URL ("http", "ftp", etc)
	 * @param host The hostname or IP address to connect to
	 * @param port The port number to use, or -1 to use the protocol's default
	 * port
	 * @param file The "file" portion of the URL.
	 * @param handler The protocol handler to use with this URL.
	 *
	 * @exception MalformedURLException If no protocol handler can be loaded
	 * for the specified protocol.
	 * @exception SecurityException If the <code>SecurityManager</code> exists
	 * and does not allow the caller to specify its own protocol handler.
	 *
	 * @since 1.2
	 */
	public URL(
		String protocol,
		String host,
		int port,
		String file,
		URLStreamHandler handler)
		throws MalformedURLException {
		if (protocol == null)
			throw new MalformedURLException("null protocol");
		this.protocol = protocol.toLowerCase();

		if (handler != null) {
			SecurityManager s = System.getSecurityManager();
			if (s != null)
				s.checkPermission(new NetPermission("specifyStreamHandler"));

			this.handler = handler;
		} else
			this.handler = getURLStreamHandler(protocol);

		if (this.handler == null)
			throw new MalformedURLException(
				"Protocol handler not found: " + protocol);

		this.host = host;
		this.port = port;
		this.authority = null;

		int hashAt = file.indexOf('#');
		if (hashAt < 0) {
			this.file = file;
			this.ref = null;
		} else {
			this.file = file.substring(0, hashAt);
			this.ref = file.substring(hashAt + 1);
		}
		hashCode = hashCode(); // Used for serialization.
	}

	/**
	 * Initializes a URL from a complete string specification such as
	 * "http://www.urbanophile.com/arenn/".  First the protocol name is parsed
	 * out of the string.  Then a handler is located for that protocol and
	 * the parseURL() method of that protocol handler is used to parse the
	 * remaining fields.
	 *
	 * @param spec The complete String representation of a URL
	 *
	 * @exception MalformedURLException If a protocol handler cannot be found
	 * or the URL cannot be parsed
	 */
	public URL(String spec) throws MalformedURLException {
		this((URL) null, spec, (URLStreamHandler) null);
	}

	/*
	 * This method parses a String representation of a URL within the
	 * context of an existing URL.  Principally this means that any
	 * fields not present the URL are inheritied from the context URL.
	 * This allows relative URL's to be easily constructed.  If the
	 * context argument is null, then a complete URL must be specified
	 * in the URL string.  If the protocol parsed out of the URL is
	 * different from the context URL's protocol, then then URL String
	 * is also expected to be a complete URL.
	 *
	 * @param context The context on which to parse the specification
	 * @param spec The string to parse an URL
	 *
	 * @exception MalformedURLException If a protocol handler cannot be found 
	 * for the URL cannot be parsed
	 */
	public URL(URL context, String spec) throws MalformedURLException {
		this(context, spec, (URLStreamHandler) null);
	}

	/**
	 * Creates an URL from given arguments
	 * This method parses a String representation of a URL within the
	 * context of an existing URL.  Principally this means that any fields
	 * not present the URL are inheritied from the context URL.  This allows
	 * relative URL's to be easily constructed.  If the context argument is
	 * null, then a complete URL must be specified in the URL string.
	 * If the protocol parsed out of the URL is different 
	 * from the context URL's protocol, then then URL String is also
	 * expected to be a complete URL.
	 * <p>
	 * Additionally, this method allows the caller to specify a protocol handler
	 * to use instead of  the default.  If this handler is specified, the caller
	 * must have the "specifyStreamHandler" permission
	 * (see <code>NetPermission</code>) or a <code>SecurityException</code>
	 * will be thrown.
	 *
	 * @param context The context in which to parse the specification
	 * @param spec The string to parse as an URL
	 * @param handler The stream handler for the URL
	 *
	 * @exception MalformedURLException If a protocol handler cannot be found
	 * or the URL cannot be parsed
	 * @exception SecurityException If the <code>SecurityManager</code> exists
	 * and does not allow the caller to specify its own protocol handler.
	 *
	 * @since 1.2
	 */
	public URL(URL context, String spec, URLStreamHandler handler)
		throws MalformedURLException {
		/* A protocol is defined by the doc as the substring before a ':'
		 * as long as the ':' occurs before any '/'.
		 *
		 * If context is null, then spec must be an absolute URL.
		 *
		 * The relative URL need not specify all the components of a URL.
		 * If the protocol, host name, or port number is missing, the value
		 * is inherited from the context.  A bare file component is appended
		 * to the context's file.  The optional anchor is not inherited. 
		 */

		// If this is an absolute URL, then ignore context completely.
		// An absolute URL must have chars prior to "://" but cannot have a colon
		// right after the "://".  The second colon is for an optional port value
		// and implies that the host from the context is used if available.
		int colon;
		if ((colon = spec.indexOf("://", 1)) > 0
			&& !spec.regionMatches(colon, "://:", 0, 4))
			context = null;

		int slash;
		if ((colon = spec.indexOf(':')) > 0
			&& (colon < (slash = spec.indexOf('/')) || slash < 0)) {
			// Protocol specified in spec string.
			protocol = spec.substring(0, colon).toLowerCase();
			if (context != null && context.protocol.equals(protocol)) {
				// The 1.2 doc specifically says these are copied to the new URL.
				host = context.host;
				port = context.port;
				file = context.file;
				authority = context.authority;
			}
		} else if (context != null) {
			// Protocol NOT specified in spec string.
			// Use context fields (except ref) as a foundation for relative URLs.
			colon = -1;
			protocol = context.protocol;
			host = context.host;
			port = context.port;
			file = context.file;
			authority = context.authority;
		} else // Protocol NOT specified in spec. and no context available.
			throw new MalformedURLException("Absolute URL required with null context");

		if (handler != null) {
			SecurityManager s = System.getSecurityManager();
			if (s != null)
				s.checkPermission(new NetPermission("specifyStreamHandler"));

			this.handler = handler;
		} else
			this.handler = getURLStreamHandler(protocol);

		if (this.handler == null)
			throw new MalformedURLException(
				"Protocol handler not found: " + protocol);

		// JDK 1.2 doc for parseURL specifically states that any '#' ref
		// is to be excluded by passing the 'limit' as the indexOf the '#'
		// if one exists, otherwise pass the end of the string.
		int hashAt = spec.indexOf('#', colon + 1);
		this.handler.parseURL(
			this,
			spec,
			colon + 1,
			hashAt < 0 ? spec.length() : hashAt);
		if (hashAt >= 0)
			ref = spec.substring(hashAt + 1);

		hashCode = hashCode(); // Used for serialization.
	}

	/**
	 * Test another URL for equality with this one.  This will be true only if
	 * the argument is non-null and all of the fields in the URL's match 
	 * exactly (ie, protocol, host, port, file, and ref).  Overrides
	 * Object.equals(), implemented by calling the equals method of the handler.
	 *
	 * @param obj The URL to compare with
	 *
	 * @return true if the URL is equal, false otherwise
	 */
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof URL))
			return false;

		URL uObj = (URL)obj;

		return handler.equals(this, uObj);
	}

	/**
	 * Returns the contents of this URL as an object by first opening a
	 * connection, then calling the getContent() method against the connection
	 *
	 * @return A content object for this URL
	 * @exception IOException If opening the connection or getting the
	 * content fails.
	 *
	 * @since 1.3
	 */
	public final Object getContent() throws IOException {
		return openConnection().getContent();
	}

	/**
	 * Gets the contents of this URL
	 *
	 * @exception IOException If an error occurs
	 */
	public final Object getContent(Class[] classes) throws IOException {
		// FIXME: implement this
		return getContent();
	}

	/**
	 * Returns the file portion of the URL.
	 * Defined as <code>path[?query]</code>.
	 * Returns the empty string if there is no file portion.
	 */
	public String getFile() {
		return file == null ? "" : file;
	}

	/**
	 * Returns the path of the URL. This is the part of the file before any '?'
	 * character.
	 *
	 * @since 1.3
	 */
	public String getPath() {
		int quest = (file == null) ? -1 : file.indexOf('?');
		return quest < 0 ? getFile() : file.substring(0, quest);
	}

	/**
	 * Returns the authority of the URL
	 * 
	 * @since 1.3
	 */
	public String getAuthority() {
		return authority;
	}

	/**
	 * Returns the host of the URL
	 */
	public String getHost() {
		int at = (host == null) ? -1 : host.indexOf('@');
		return at < 0 ? host : host.substring(at + 1, host.length());
	}

	/**
	 * Returns the port number of this URL or -1 if the default port number is
	 * being used.
	 *
	 * @return The port number
	 *
	 * @see #getDefaultPort()
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns the default port of the URL. If the StreamHandler for the URL
	 * protocol does not define a default port it returns -1.
	 */
	public int getDefaultPort() {
		return handler.getDefaultPort();
	}

	/**
	 * Returns the protocol of the URL
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * Returns the ref (sometimes called the "# reference" or "anchor") portion
	 * of the URL.
	 *
	 * @return The ref
	 */
	public String getRef() {
		return ref;
	}

	/**
	 * Returns the user information of the URL. This is the part of the host
	 * name before the '@'.
	 *
	 * @return the user at a particular host or null when no user defined.
	 */
	public String getUserInfo() {
		int at = (host == null) ? -1 : host.indexOf('@');
		return at < 0 ? null : host.substring(0, at);
	}

	/**
	 * Returns the query of the URL. This is the part of the file before the
	 * '?'.
	 *
	 * @ return the query part of the file, or null when there is no query part.
	 */
	public String getQuery() {
		int quest = (file == null) ? -1 : file.indexOf('?');
		return quest < 0 ? null : file.substring(quest + 1, file.length());
	}

	/**
	 * Returns a hashcode computed by the URLStreamHandler of this URL
	 */
	public int hashCode() {
		if (hashCode != 0)
			return hashCode; // Use cached value if available.
		else
			return handler.hashCode(this);
	}

	/**
	 * Returns a URLConnection object that represents a connection to the remote
	 * object referred to by the URL. The URLConnection is created by calling the
	 * openConnection() method of the protocol handler
	 *
	 * @return A URLConnection for this URL
	 * @exception IOException If an error occurs
	 */
	public URLConnection openConnection() throws IOException {
		return handler.openConnection(this);
	}

	/**
	 * Opens a connection to this URL and returns an InputStream for reading
	 * from that connection
	 *
	 * @exception IOException If an error occurs
	 */
	public final InputStream openStream() throws IOException {
		return openConnection().getInputStream();
	}

	/**
	 * Tests whether or not another URL refers to the same "file" as this one.
	 * This will be true if and only if the passed object is not null, is a
	 * URL, and matches all fields but the ref (ie, protocol, host, port,
	 * and file);
	 *
	 * @param other The URL object to test with
	 *
	 * @return true if URL matches this URL's file, false otherwise
	 */
	public boolean sameFile(URL other) {
		return handler.sameFile(this, other);
	}

	/**
	 * Sets the specified fields of the URL. This is not a public method so
	 * that only URLStreamHandlers can modify URL fields. This might be called
	 * by the <code>parseURL()</code> method in that class. URLs are otherwise
	 * constant.
	 *
	 * @param protocol The protocol name for this URL
	 * @param host The hostname or IP address for this URL
	 * @param port The port number of this URL
	 * @param file The "file" portion of this URL.
	 * @param ref The anchor portion of this URL.
	 */
	protected void set(
		String protocol,
		String host,
		int port,
		String file,
		String ref) {
		// TBD: Theoretically, a poorly written StreamHandler could pass an
		// invalid protocol.  It will cause the handler to be set to null
		// thus overriding a valid handler.  Callers of this method should
		// be aware of this.
		this.handler = getURLStreamHandler(protocol);
		this.protocol = protocol.toLowerCase();
		this.authority = null;
		this.port = port;
		this.host = host;
		this.file = file;
		this.ref = ref;
		hashCode = hashCode(); // Used for serialization.
	}

	/**
	 * Sets the specified fields of the URL. This is not a public method so
	 * that only URLStreamHandlers can modify URL fields. URLs are otherwise
	 * constant.
	 *
	 * @since 1.3
	 */
	protected void set(
		String protocol,
		String host,
		int port,
		String authority,
		String userInfo,
		String path,
		String query,
		String ref) {
		// TBD: Theoretically, a poorly written StreamHandler could pass an
		// invalid protocol.  It will cause the handler to be set to null
		// thus overriding a valid handler.  Callers of this method should
		// be aware of this.
		this.handler = getURLStreamHandler(protocol);
		this.protocol = protocol.toLowerCase();
		if (userInfo == null)
			this.host = host;
		else
			this.host = userInfo + "@" + host;
		this.port = port;
		if (query == null)
			this.file = path;
		else
			this.file = path + "?" + query;
		this.ref = ref;
		hashCode = hashCode(); // Used for serialization.
	}

	/**
	 * Sets the URLStreamHandlerFactory for this class.  This factory is
	 * responsible for returning the appropriate protocol handler for
	 * a given URL.
	 *
	 * @param fac The URLStreamHandlerFactory class to use
	 *
	 * @exception Error If the factory is alread set.
	 * @exception SecurityException If a security manager exists and its
	 * checkSetFactory method doesn't allow the operation
	 */
	public static synchronized void setURLStreamHandlerFactory(URLStreamHandlerFactory fac) {
		if (factory != null)
			throw new Error("URLStreamHandlerFactory already set");

		// Throw an exception if an extant security mgr precludes
		// setting the factory.
		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkSetFactory();
		factory = fac;
	}

	/**
	 * Returns a String representing this URL.  The String returned is
	 * created by calling the protocol handler's toExternalForm() method.
	 *
	 * @return A string for this URL
	 */
	public String toExternalForm() {
		// Identical to toString().
		return handler.toExternalForm(this);
	}

	/**
	 * Returns a String representing this URL.  Identical to toExternalForm().
	 * The value returned is created by the protocol handler's 
	 * toExternalForm method.  Overrides Object.toString()
	 *
	 * @return A string for this URL
	 */
	public String toString() {
		// Identical to toExternalForm().
		return handler.toExternalForm(this);
	}

	private void readObject(ObjectInputStream ois)
		throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		this.handler = getURLStreamHandler(protocol);
		if (this.handler == null)
			throw new IOException(
				"Handler for protocol " + protocol + " not found");
	}

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}
}
