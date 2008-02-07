/* SecureClassLoader.java --- A Secure Class Loader
   Copyright (C) 1999, 2004  Free Software Foundation, Inc.

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

package java.security;

import java.util.HashMap;
import sun.security.util.Debug;

/**
 * A Secure Class Loader for loading classes with additional 
 * support for specifying code source and permissions when
 * they are retrieved by the system policy handler.
 *
 * @since 1.2
 *
 * @author Mark Benvenuto
 */
public class SecureClassLoader extends ClassLoader
{
  java.util.WeakHashMap protectionDomainCache = new java.util.WeakHashMap();

  protected SecureClassLoader(ClassLoader parent)
  {
		super(parent);
		SecurityManager sm = System.getSecurityManager();
    if(sm != null)
			sm.checkCreateClassLoader();
	}

  protected SecureClassLoader()
  {
		SecurityManager sm = System.getSecurityManager();
    if(sm != null)
			sm.checkCreateClassLoader();
	}

	/** 
   * Creates a class using an array of bytes and a 
   * CodeSource.
   *
   * @param name the name to give the class.  null if unknown.
   * @param b the data representing the classfile, in classfile format.
   * @param off the offset into the data where the classfile starts.
   * @param len the length of the classfile data in the array.
   * @param cs the CodeSource for the class or null when unknown.
   *
   * @return the class that was defined and optional CodeSource.
   *
   * @exception ClassFormatError if the byte array is not in proper classfile format.
	 */
  protected final Class defineClass(String name, byte[] b, int off, int len,
				    CodeSource cs)
  {
    if (cs != null)
      {
	ProtectionDomain protectionDomain;
	  
	synchronized (protectionDomainCache)
	  {
	    protectionDomain = (ProtectionDomain)protectionDomainCache.get(cs);
	  }

	if (protectionDomain == null)
	  {
	    protectionDomain 
          = new ProtectionDomain(cs, getPermissions(cs), this, null);
	    synchronized (protectionDomainCache)
	      {
		ProtectionDomain domain 
		  = (ProtectionDomain)protectionDomainCache.get(cs);
		if (domain == null)
		  protectionDomainCache.put(cs, protectionDomain);
		else
		  protectionDomain = domain;
	      }
	  }
			return super.defineClass(name, b, off, len, protectionDomain);
      } 
    else
			return super.defineClass(name, b, off, len);
	}

	/**
   * Returns a PermissionCollection for the specified CodeSource.
   * The default implementation invokes 
   * java.security.Policy.getPermissions.
   *
   * This method is called by defineClass that takes a CodeSource
   * arguement to build a proper ProtectionDomain for the class
   * being defined.
	 */
  protected PermissionCollection getPermissions(CodeSource cs)
  {
    Policy policy = Policy.getPolicyNoCheck();
		return policy.getPermissions(cs);
	}

    //jnode + openjdk
/**
     * Converts a {@link java.nio.ByteBuffer <tt>ByteBuffer</tt>}
     * into an instance of class <tt>Class</tt>, with an optional CodeSource.
     * Before the class can be used it must be resolved.
     * <p>
     * If a non-null CodeSource is supplied a ProtectionDomain is
     * constructed and associated with the class being defined.
     * <p>
     * @param      name the expected name of the class, or <code>null</code>
     *                  if not known, using '.' and not '/' as the separator
     *                  and without a trailing ".class" suffix.
     * @param      b    the bytes that make up the class data.  The bytes from positions
     *                  <tt>b.position()</tt> through <tt>b.position() + b.limit() -1</tt>
     *                  should have the format of a valid class file as defined by the
     *                  <a href="http://java.sun.com/docs/books/vmspec/">Java Virtual
     *                  Machine Specification</a>.
     * @param      cs   the associated CodeSource, or <code>null</code> if none
     * @return the <code>Class</code> object created from the data,
     *         and optional CodeSource.
     * @exception  ClassFormatError if the data did not contain a valid class
     * @exception  SecurityException if an attempt is made to add this class
     *             to a package that contains classes that were signed by
     *             a different set of certificates than this class, or if
     *             the class name begins with "java.".
     *
     * @since  1.5
     */
    protected final Class<?> defineClass(String name, java.nio.ByteBuffer b,
					 CodeSource cs)
    {
	if (cs == null)
	    return defineClass(name, b, (ProtectionDomain)null);
	else
	    return defineClass(name, b, getProtectionDomain(cs));
    }


    // HashMap that maps CodeSource to ProtectionDomain
    private HashMap<CodeSource, ProtectionDomain> pdcache =
			new HashMap<CodeSource, ProtectionDomain>(11);

    private static final Debug debug = Debug.getInstance("scl");
    /*
     * Returned cached ProtectionDomain for the specified CodeSource.
     */
    private ProtectionDomain getProtectionDomain(CodeSource cs) {
	if (cs == null)
	    return null;


    ProtectionDomain pd = null;
	synchronized (pdcache) {
	    pd = pdcache.get(cs);
	    if (pd == null) {
		PermissionCollection perms = getPermissions(cs);
		pd = new ProtectionDomain(cs, perms, this, null);
		if (pd != null) {
		    pdcache.put(cs, pd);
		    if (debug != null) {
			debug.println(" getPermissions "+ pd);
			debug.println("");
		    }
		}
	    }
	}
	return pd;
    }
}
