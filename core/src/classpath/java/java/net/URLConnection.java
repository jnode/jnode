/* URLConnection.java -- Abstract superclass for reading from URL's
   Copyright (C) 1998, 2002 Free Software Foundation, Inc.

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
import java.io.OutputStream;
import java.security.Permission;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
  * This class models a connection that retrieves the information pointed
  * to by a URL object.  This is typically a connection to a remote node
  * on the network, but could be a simple disk read.
  * <p>
  * A URLConnection object is normally created by calling the openConnection()
  * method of a URL object.  This method is somewhat misnamed because it does
  * not actually open the connection.  Instead, it return an unconnected
  * instance of this object.  The caller then has the opportunity to set
  * various connection options prior to calling the actual connect() method.
  * <p>
  * After the connection has been opened, there are a number of methods in
  * this class that access various attributes of the data, typically 
  * represented by headers sent in advance of the actual data itself.
  * <p>
  * Also of note are the getInputStream and getContent() methods which allow
  * the caller to retrieve the actual data from the connection.  Note that
  * for some types of connections, writing is also allowed.  The setDoOutput()
  * method must be called prior to connecing in order to enable this, then
  * the getOutputStream method called after the connection in order to
  * obtain a stream to write the output to.
  * <p>
  * The getContent() method is of particular note.  This method returns an
  * Object that encapsulates the data returned.  There is no way do determine
  * the type of object that will be returned in advance.  This is determined
  * by the actual content handlers as described in the description of that
  * method.
  *
  * @version 0.5
  *
  * @author Aaron M. Renn (arenn@urbanophile.com)
  */
public abstract class URLConnection {

	/*************************************************************************/

	/*
	 * Class Variables
	 */

	/**
	  * This is an object that maps filenames to MIME types.  The interface
	  * to do this is implemented by this class, so just create an empty
	  * instance and store it here.
	  */
	private static FileNameMap fileNameMap = new MimeTypeMapper();

	/**
	  * This is the ContentHandlerFactory set by the caller, if any
	  */
	private static ContentHandlerFactory factory;

	/**
	  * This is the default value that will be used to determine whether or
	  * not user interaction should be allowed.
	  */
	private static boolean def_allow_user_inter;

	/**
	  * This is the default flag indicating whether or not to use caches to
	  * store the data returned from a server
	  */
	private static boolean def_use_caches;

	/**
	  * This is a Hashable for setting default request properties
	  */
	private static Hashtable def_req_props = new Hashtable();

	/*************************************************************************/

	/*
	 * Instance Variables
	 */

	/**
	  * This variable determines whether or not interaction is allowed with
	  * the user.  For example, to prompt for a username and password.
	  */
	protected boolean allowUserInteraction;

	/**
	  * Indicates whether or not a connection has been established to the
	  * destination specified in the URL
	  */
	protected boolean connected;

	/**
	  * Indicates whether or not input can be read from this URL
	  */
	protected boolean doInput;

	/**
	  * Indicates whether or not output can be sent to this URL
	  */
	protected boolean doOutput;

	/**
	  * If this flag is set, the protocol is allowed to cache data whenever
	  * it can (caching is not guaranteed). If it is not set, the protocol must a get a fresh copy of
	  * the data. 
	  * <p>
	  * This field is set by the setUseCaches method and returned by the 
	  * getUseCaches method.
	  *
	  * Its default value is that determined by the last invocation of 
	  * setDefaultUseCaches
	  *
	  */
	protected boolean useCaches;

	/**
	  * If this value is non-zero, then the connection will only attempt to
	  * fetch the document pointed to by the URL if the document has been
	  * modified more recently than the date set in this variable.  That date
	  * should be specified as the number of seconds since 1/1/1970 GMT.
	  */
	protected long ifModifiedSince;

	/**
	  * This is the URL associated with this connection
	  */
	protected URL url;

	/**
	  * The list of request properties for this connection
	  */
	private final Hashtable req_props;

	/*************************************************************************/

	/*
	 * Class Methods
	 */

	/**
	  * Set's the ContentHandlerFactory for an application.  This can be called
	  * once and only once.  If it is called again, then an Error is thrown.
	  * Unlike for other set factory methods, this one does not do a security
	  * check prior to setting the factory.
	  *
	  * @param factory The ContentHandlerFactory for this application
	  *
	  * @error Error If the factory is already set
	  */
	public static synchronized void setContentHandlerFactory(ContentHandlerFactory fac) {
		if (factory != null)
			throw new Error("The ContentHandlerFactory is already set");

		factory = fac;
	}

	/*************************************************************************/

	/**
	  * Returns the default flag for whether or not interaction with a user
	  * is allowed.  This will be used for all connections unless overidden
	  *
	  * @return true if user interaction is allowed, false otherwise
	  */
	public static boolean getDefaultAllowUserInteraction() {
		return (def_allow_user_inter);
	}

	/*************************************************************************/

	/**
	  * Sets the default flag for whether or not interaction with a user
	  * is allowed.  This will be used for all connections unless overridden
	  *
	  * @param allow true to allow user interaction, false otherwise
	  */
	public static synchronized void setDefaultAllowUserInteraction(boolean allow) {
		def_allow_user_inter = allow;
	}

	/*************************************************************************/

	/**
	  * Returns the default value of a request property.  This will be used
	  * for all connections unless the value of the property is manually
	  * overridden.
	  *
	  * @param key The request property to return the default value of
	  * 
	  * @return The default request property
	  */
	public static String getDefaultRequestProperty(String key) {
		return ((String) def_req_props.get(key.toLowerCase()));
	}

	/*************************************************************************/

	/**
	  * Sets the default value of a request property.  This will be used
	  * for all connections unless the value of the property is manually
	  * overridden.
	  *
	  * @param key The request property name the default is being set for
	  * @param value The value to set the default to
	  */
	public static synchronized void setDefaultRequestProperty(String key, String value) {
		def_req_props.put(key.toLowerCase(), value);
	}

	/*************************************************************************/

	/**
	  * Returns the MIME type of a file based on the name of the file.  This
	  * works by searching for the file's extension in a list of file extensions
	  * and returning the MIME type associated with it.  If no type is found,
	  * then a MIME type of "application/octet-stream" will be returned.
	  *
	  * @param filename The filename to determine the MIME type for
	  *
	  * @return The MIME type String
	  *
	  * @specnote public since jdk 1.4
	  */
	public static String guessContentTypeFromName(String filename) {
		return (fileNameMap.getContentTypeFor(filename.toLowerCase()));
	}

	/*************************************************************************/

	/**
	  * Returns the MIME type of a stream based on the first few characters
	  * at the beginning of the stream.  This routine can be used to determine
	  * the MIME type if a server is believed to be returning an incorrect
	  * MIME type.  This method returns "application/octet-stream" if it 
	  * cannot determine the MIME type.
	  * <p>
	  * NOTE: Overriding MIME types sent from the server can be obnoxious
	  * to user's.  See Internet Exploder 4 if you don't believe me.
	  *
	  * @param is The InputStream to determine the MIME type from
	  *
	  * @return The MIME type
	  *
	  * @exception IOException If an error occurs
	  */
	public static String guessContentTypeFromStream(InputStream is) throws IOException {
		return ("application/octet-stream");
	}

	/*************************************************************************/

	/**
	  * This method returns the <code>FileNameMap</code> object being used
	  * to decode MIME types by file extension.
	  *
	  * @return The <code>FileNameMap</code>.
	  */
	public static FileNameMap getFileNameMap() {
		return (fileNameMap);
	}

	/*************************************************************************/

	/**
	  * This method set the <code>FileNameMap</code> object being used
	  * to decode MIME types by file extension.
	  *
	  * @param fileNameMap The <code>FileNameMap</code>.
	  */
	public static void setFileNameMap(FileNameMap fileNameMap) {
		URLConnection.fileNameMap = fileNameMap;
	}

	/*************************************************************************/

	/*
	 * Constructors
	 */

	/**
	  * This constructs a URLConnection from a URL object
	  *
	  * @param url The URL for this connection
	  */
	protected URLConnection(URL url) {
		// Set up all our instance variables
		this.url = url;
		allowUserInteraction = def_allow_user_inter;
		useCaches = def_use_caches;

		req_props = new Hashtable(def_req_props);
	}

	/*************************************************************************/

	/*
	 * Instance Methods
	 */

	/**
	  * Returns the default value used to determine whether or not caching
	  * of documents will be done when possible.
	  *
	  * @return true if caches will be used, false otherwise
	  */
	public boolean getDefaultUseCaches() {
		return (def_use_caches);
	}

	/*************************************************************************/

	/**
	  * Sets the default value used to determine whether or not caching
	  * of documents will be done when possible.
	  *
	  * @param use true to use caches if possible by default, false otherwise
	  */
	public synchronized void setDefaultUseCaches(boolean use) {
		def_use_caches = use;
	}

	/*************************************************************************/

	/**
	  * Returns a boolean flag indicating whether or not user interaction is
	  * allowed for this connection.  (For example, in order to prompt for
	  * username and password info.
	  *
	  * @return true if user interaction is allowed, false otherwise
	  */
	public boolean getAllowUserInteraction() {
		return (allowUserInteraction);
	}

	/*************************************************************************/

	/**
	  * Sets a boolean flag indicating whether or not user interaction is
	  * allowed for this connection.  (For example, in order to prompt for
	  * username and password info.
	  *
	  * @param allow true if user interaction should be allowed, false otherwise
	  */
	public void setAllowUserInteraction(boolean allow) {
		allowUserInteraction = allow;
	}

	/*************************************************************************/

	/**
	  * Returns the value of a flag indicating whether or not input is going
	  * to be done for this connection.  This default to true unless the
	  * doOutput flag is set to false, in which case this defaults to false.
	  *
	  * @return true if input is to be done, false otherwise
	  */
	public boolean getDoInput() {
		return (doInput);
	}

	/*************************************************************************/

	/**
	  * Returns the value of a flag indicating whether or not input is going
	  * to be done for this connection.  This default to true unless the
	  * doOutput flag is set to false, in which case this defaults to false.
	  *
	  * @param input true if input is to be done, false otherwise
	  */
	public void setDoInput(boolean input) {
		doInput = input;
	}

	/*************************************************************************/

	/**
	  * Returns a boolean flag indicating whether or not output will be done
	  * on this connection.  This defaults to false.
	  *
	  * @return true if output is to be done, false otherwise
	  */
	public boolean getDoOutput() {
		return (doOutput);
	}

	/*************************************************************************/

	/**
	  * Returns a boolean flag indicating whether or not output will be done
	  * on this connection.  The default value is false, so this method can
	  * be used to override the default
	  *
	  * @param output ture if output is to be done, false otherwise
	  */
	public void setDoOutput(boolean output) {
		doOutput = output;
	}

	/*************************************************************************/

	/**
	  * Returns a boolean flag indicating whether or not caching will be used
	  * (if possible) to store data downloaded via the connection.
	  *
	  * @return true if caching should be used if possible, false otherwise
	  */
	public boolean getUseCaches() {
		return (useCaches);
	}

	/*************************************************************************/

	/**
	  * Sets a boolean flag indicating whether or not caching will be used
	  * (if possible) to store data downloaded via the connection.
	  *
	  * @param use_cache true if caching should be used if possible, false otherwise
	  */
	public void setUseCaches(boolean use_caches) {
		useCaches = use_caches;
	}

	/*************************************************************************/

	/**
	  * Returns the ifModified since instance variable.  If this value is non
	  * zero and the underlying protocol supports it, the actual document will
	  * not be fetched unless it has been modified since this time.  The value
	  * returned will be 0 if this feature is disabled or the time expressed
	  * as the number of seconds since midnight 1/1/1970 GMT otherwise
	  *
	  * @return The ifModifiedSince value
	  */
	public long getIfModifiedSince() {
		return (ifModifiedSince);
	}

	/*************************************************************************/

	/**
	  * Sets the ifModified since instance variable.  If this value is non
	  * zero and the underlying protocol supports it, the actual document will
	  * not be fetched unless it has been modified since this time.  The value
	  * passed should  be 0 if this feature is to be disabled or the time expressed
	  * as the number of seconds since midnight 1/1/1970 GMT otherwise.
	  *
	  * @param modified_since The new ifModifiedSince value
	  */
	public void setIfModifiedSince(long modified_since) {
		ifModifiedSince = modified_since;
	}

	/*************************************************************************/

	/**
	  * Returns the value of the named request property.
	  *
	  * @param key The name of the property
	  *
	  * @return The value of the property
	  */
	public String getRequestProperty(String key) {
		return ((String) req_props.get(key.toLowerCase()));
	}

	/*************************************************************************/

	/**
	  * Sets the value of the named request property
	  *
	  * @param key The name of the property
	  * @param value The value of the property
	  */
	public void setRequestProperty(String key, String value) {
		req_props.put(key.toLowerCase(), value);
	}

	/**
	 * Returns an unmodifiable Map containing the request properties.
	 *
	 * @since 1.4
	 */
	public Map getRequestProperties() {
		return Collections.unmodifiableMap(req_props);
	}

	/*************************************************************************/

	/**
	  * Returns the URL object associated with this connection
	  *
	  * @return The URL for this connection.
	  */
	public URL getURL() {
		return (url);
	}

	/*************************************************************************/

	/**
	  * Establishes the actual connection to the URL associated with this
	  * connection object
	  */
	public abstract void connect() throws IOException;

	/*************************************************************************/

	/**
	  * Returns an InputStream for this connection.  As this default
	  * implementation returns null, subclasses should override this method
	  *
	  * @return An InputStream for this connection
	  *
	  * @exception IOException If an error occurs
	  */
	public InputStream getInputStream() throws IOException {
		return (null);
	}

	/*************************************************************************/

	/**
	  * Returns an OutputStream for this connection.  As this default
	  * implementation returns null, subclasses should override this method
	  *
	  * @return An OutputStream for this connection
	  *
	  * @exception IOException If an error occurs
	  */
	public OutputStream getOutputStream() throws IOException {
		return (null);
	}

	/*************************************************************************/

	/**
	  * Returns the value of the content-encoding field or null if it is not
	  * known or not present.
	  * 
	  * @return The content-encoding field
	  */
	public String getContentEncoding() {
		return (getHeaderField("content-encoding"));
	}

	/*************************************************************************/

	/**
	  * Returns the value of the content-length header field or -1 if the value
	  * is not known or not present.
	  *
	  * @return The content-length field
	  */
	public int getContentLength() {
		return (getHeaderFieldInt("content-length", -1));
	}

	/*************************************************************************/

	/**
	  * Returns the the content-type of the data pointed to by the URL.  This
	  * method first tries looking for a content-type header.  If that is not
	  * present, it attempts to use the file name to determine the content's
	  * MIME type.  If that is unsuccessful, the method returns null.  The caller
	  * may then still attempt to determine the MIME type by a call to
	  * guessContentTypeFromStream()
	  *
	  * @return The content MIME type
	  */
	public String getContentType() {
		String type = getHeaderField("content-type");
		if (type == null)
			type = guessContentTypeFromName(getURL().getFile());

		return (type);
	}

	/*************************************************************************/

	/**
	  * Returns the date of the document pointed to by the URL as reported in
	  * the date field of the header or 0 if the value is not present or not
	  * known. If populated, the return value is number of seconds since
	  * midnight on 1/1/1970 GMT.
	  *
	  * @return The document date
	  */
	public long getDate() {
		return (getHeaderFieldDate("date", 0));
	}

	/*************************************************************************/

	/**
	  * Returns the value of the expires header or 0 if not known or present.
	  * If populated, the return value is number of seconds since midnight
	  * on 1/1/1970 GMT.
	  *
	  * @return The expiration time.
	  */
	public long getExpiration() {
		return (getHeaderFieldDate("expires", 0));
	}

	/*************************************************************************/

	/**
	  * Returns the value of the last-modified header field or 0 if not known known
	  * or not present.  If populated, the return value is the number of seconds
	  * since midnight on 1/1/1970.
	  *
	  * @return The last modified time
	  */
	public long getLastModified() {
		return (getHeaderFieldDate("last-modified", 0));
	}

	/*************************************************************************/

	/**
	  * Returns a String representing the header key at the specified index.
	  * This allows the caller to walk the list of header fields.  The analogous
	  * getHeaderField(int) method allows access to the corresponding value for
	  * this tag.
	  *
	  * @param index The index into the header field list to retrieve the key for. 
	  *
	  * @return The header field key or null if index is past the end of the headers
	  */
	public String getHeaderFieldKey(int index) {
		return (null);
	}

	/*************************************************************************/

	/**
	  * Return a String representing the header value at the specified index.
	  * This allows the caller to walk the list of header fields.  The analogous
	  * getHeaderFieldKey(int) method allows access to the corresponding key
	  * for this header field
	  *
	  * @param index The index into the header field list to retrieve the value for
	  *
	  * @return The header value or null if index is past the end of the headers
	  */
	public String getHeaderField(int index) {
		return (null);
	}

	/*************************************************************************/

	/**
	  * Returns a String representing the value of the header field having
	  * the named key.  Returns null if the header field does not exist.
	  *
	  * @param The key of the header field
	  *
	  * @return The value of the header field as a String
	  */
	public String getHeaderField(String name) {
		for (int i = 0;; i++) {
			String key = getHeaderFieldKey(i);
			if (key == null)
				return (null);

			if (key.toLowerCase().equals(name.toLowerCase()))
				return (getHeaderField(i));
		}
	}

	/*************************************************************************/

	/**
	  * Returns the value of the named header field as a date.  This date will
	  * be the number of seconds since midnight 1/1/1970 GMT or the default
	  * value if the field is not present or cannot be converted to a date.
	  *
	  * @param key The header field key to lookup
	  * @param def The default value if the header field is not found or can't be converted
	  */
	public long getHeaderFieldDate(String key, long def) {
		String value = getHeaderField(key);
		if (value == null)
			return (def);

		// This needs to change since Date(String) is deprecated, but DateFormat
		// doesn't seem to be working for some reason
		//DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
		//df.setLenient(true);

		//Date d = df.parse(value, new ParsePosition(0));
		Date d = new Date(value);

		if (d == null)
			return (def);

		return (d.getTime() / 1000);
	}

	/*************************************************************************/

	/**
	  * Returns the value of the named header field as an int.  If the field
	  * is not present or cannot be parsed as an integer, the default value
	  * will be returned.
	  *
	  * @param key The header field key to lookup
	  * @param def The defaule value if the header field is not found or can't be parsed
	  */
	public int getHeaderFieldInt(String key, int def) {
		String value = getHeaderField(key);
		if (value == null)
			return (def);

		int retval = def;
		try {
			retval = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return (def);
		}

		return (retval);
	}

	/*************************************************************************/

	/**
	  * This method returns a <code>Permission</code> object representing the
	  * permissions required to access this URL.  This method returns
	  * <code>java.security.AllPermission</code> by default.  Subclasses should
	  * override it to return a more specific permission.  For example, an
	  * HTTP URL should return an instance of <code>SocketPermission</code>
	  * for the appropriate host and port.
	  * <p>
	  * Note that because of items such as HTTP redirects, the permission
	  * object returned might be different before and after connecting.
	  *
	  * @return A Permission object
	  *
	  * @exception IOException If an error occurs
	  */
	public Permission getPermission() throws IOException {
		return (new java.security.AllPermission());
	}

	/*************************************************************************/

	/**
	  * This method returns the content of the document pointed to by the URL
	  * as an Object.  The type of object depends on the MIME type of the
	  * object and particular content hander loaded.  Most text type content
	  * handlers will return a subclass of InputStream.  Images usually return
	  * a class that implements ImageProducer.  There is not guarantee what
	  * type of object will be returned, however.
	  * <p>
	  * This class first determines the MIME type of the content, then creates
	  * a ContentHandler object to process the input.  If the ContentHandlerFactory
	  * is set, then that object is called to load a content handler, otherwise
	  * a class called gnu.java.net.content.<content_type> is tried.
	  * The default class will also be used if the content handler factory returns
	  * a null content handler.
	  *
	  * @exception IOException If an error occurs.
	  */
	public Object getContent() throws IOException {
		//  connect();
		String type = getContentType();

		// First try the factory
		ContentHandler ch = null;
		if (factory != null)
			ch = factory.createContentHandler(type);

		if (ch != null)
			return (ch.getContent(this));

		// Then try our default class
		try {
			Class cls = Class.forName("gnu.java.net.content." + type.replace('/', '.'));

			Object obj = cls.newInstance();
			if (!(obj instanceof ContentHandler))
				throw new UnknownServiceException(type);

			ch = (ContentHandler) obj;
			return (ch.getContent(this));
		} catch (ClassNotFoundException e) {
			// Do nothing
		} catch (InstantiationException e) {
			// Do nothing
		} catch (IllegalAccessException e) {
			// Do nothing
		}

		throw new UnknownServiceException(type);
	}

	/*************************************************************************/

	/**
	  * The methods prints the value of this object as a String by calling the
	  * toString() method of its associated URL.  Overrides Object.toString()
	  * 
	  * @return A String representation of this object
	  */
	public String toString() {
		return (url.toString());
	}

} // class URLConnection
