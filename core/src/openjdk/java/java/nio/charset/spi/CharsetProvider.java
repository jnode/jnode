/*
 * Copyright 2000-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package java.nio.charset.spi;

import java.nio.charset.Charset;
import java.util.Iterator;


/**
 * Charset service-provider class.
 *
 * <p> A charset provider is a concrete subclass of this class that has a
 * zero-argument constructor and some number of associated charset
 * implementation classes.  Charset providers may be installed in an instance
 * of the Java platform as extensions, that is, jar files placed into any of
 * the usual extension directories.  Providers may also be made available by
 * adding them to the applet or application class path or by some other
 * platform-specific means.  Charset providers are looked up via the current
 * thread's {@link java.lang.Thread#getContextClassLoader() </code>context
 * class loader<code>}.
 *
 * <p> A charset provider identifies itself with a provider-configuration file
 * named <tt>java.nio.charset.spi.CharsetProvider</tt> in the resource
 * directory <tt>META-INF/services</tt>.  The file should contain a list of
 * fully-qualified concrete charset-provider class names, one per line.  A line
 * is terminated by any one of a line feed (<tt>'\n'</tt>), a carriage return
 * (<tt>'\r'</tt>), or a carriage return followed immediately by a line feed.
 * Space and tab characters surrounding each name, as well as blank lines, are
 * ignored.  The comment character is <tt>'#'</tt> (<tt>'&#92;u0023'</tt>); on
 * each line all characters following the first comment character are ignored.
 * The file must be encoded in UTF-8.
 *
 * <p> If a particular concrete charset provider class is named in more than
 * one configuration file, or is named in the same configuration file more than
 * once, then the duplicates will be ignored.  The configuration file naming a
 * particular provider need not be in the same jar file or other distribution
 * unit as the provider itself.  The provider must be accessible from the same
 * class loader that was initially queried to locate the configuration file;
 * this is not necessarily the class loader that loaded the file. </p>
 *
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 *
 * @see java.nio.charset.Charset
 */

public abstract class CharsetProvider {

    /**
     * Initializes a new charset provider. </p>
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     *          {@link RuntimePermission}<tt>("charsetProvider")</tt>
     */
    protected CharsetProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("charsetProvider"));
    }

    /**
     * Creates an iterator that iterates over the charsets supported by this
     * provider.  This method is used in the implementation of the {@link
     * java.nio.charset.Charset#availableCharsets Charset.availableCharsets}
     * method. </p>
     *
     * @return  The new iterator
     */
    public abstract Iterator<Charset> charsets();

    /**
     * Retrieves a charset for the given charset name. </p>
     *
     * @param  charsetName
     *         The name of the requested charset; may be either
     *         a canonical name or an alias
     *
     * @return  A charset object for the named charset,
     *          or <tt>null</tt> if the named charset
     *          is not supported by this provider
     */
    public abstract Charset charsetForName(String charsetName);

}
