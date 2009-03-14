/*
 * Copyright 2003-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.IOException;
import java.util.List;
import sun.security.util.SecurityConstants;

/**
 * Selects the proxy server to use, if any, when connecting to the
 * network resource referenced by a URL. A proxy selector is a
 * concrete sub-class of this class and is registered by invoking the
 * {@link java.net.ProxySelector#setDefault setDefault} method. The
 * currently registered proxy selector can be retrieved by calling
 * {@link java.net.ProxySelector#getDefault getDefault} method.
 *
 * <p> When a proxy selector is registered, for instance, a subclass
 * of URLConnection class should call the {@link #select select}
 * method for each URL request so that the proxy selector can decide
 * if a direct, or proxied connection should be used. The {@link
 * #select select} method returns an iterator over a collection with
 * the preferred connection approach.
 *
 * <p> If a connection cannot be established to a proxy (PROXY or
 * SOCKS) servers then the caller should call the proxy selector's
 * {@link #connectFailed connectFailed} method to notify the proxy
 * selector that the proxy server is unavailable. </p>
 *
 * @author Yingxian Wang
 * @author Jean-Christophe Collet
 * @since 1.5
 */
public abstract class ProxySelector {
    /**
     * The system wide proxy selector that selects the proxy server to
     * use, if any, when connecting to a remote object referenced by
     * an URL.
     *
     * @see #setDefault(ProxySelector)
     */
    private static ProxySelector theProxySelector;

    static {
        try {
            Class c = Class.forName("sun.net.spi.DefaultProxySelector");
            if (c != null && ProxySelector.class.isAssignableFrom(c)) {
                theProxySelector = (ProxySelector) c.newInstance();
            }
        } catch (Exception e) {
            theProxySelector = null;
        }
    }

    /**
     * Gets the system-wide proxy selector.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     * {@link NetPermission}<tt>("getProxySelector")</tt>
     * @see #setDefault(ProxySelector)
     * @return the system-wide <code>ProxySelector</code>
     * @since 1.5
     */
    public static ProxySelector getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.GET_PROXYSELECTOR_PERMISSION);
        }
        return theProxySelector;
    }

    /**
     * Sets (or unsets) the system-wide proxy selector.
     *
     * Note: non-standard protocol handlers may ignore this setting.
     *
     * @param ps The HTTP proxy selector, or
     *          <code>null</code> to unset the proxy selector.
     *
     * @throws  SecurityException
     *          If a security manager has been installed and it denies
     * {@link NetPermission}<tt>("setProxySelector")</tt>
     *
     * @see #getDefault()
     * @since 1.5
     */
    public static void setDefault(ProxySelector ps) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SecurityConstants.SET_PROXYSELECTOR_PERMISSION);
        }
        theProxySelector = ps;
    }

    /**
     * Selects all the applicable proxies based on the protocol to
     * access the resource with and a destination address to access
     * the resource at.
     * The format of the URI is defined as follow:
     * <UL>
     * <LI>http URI for http connections</LI>
     * <LI>https URI for https connections
     * <LI>ftp URI for ftp connections</LI>
     * <LI><code>socket://host:port</code><br>
     *     for tcp client sockets connections</LI>
     * </UL>
     *
     * @param   uri
     *          The URI that a connection is required to
     *
     * @return  a List of Proxies. Each element in the
     *          the List is of type
     *          {@link java.net.Proxy Proxy};
     *          when no proxy is available, the list will
     *          contain one element of type
     *          {@link java.net.Proxy Proxy}
     *          that represents a direct connection.
     * @throws IllegalArgumentException if the argument is null
     */
    public abstract List<Proxy> select(URI uri);

    /**
     * Called to indicate that a connection could not be established
     * to a proxy/socks server. An implementation of this method can
     * temporarily remove the proxies or reorder the sequence of
     * proxies returned by {@link #select(URI)}, using the address
     * and the IOException caught when trying to connect.
     *
     * @param   uri
     *          The URI that the proxy at sa failed to serve.
     * @param   sa
     *          The socket address of the proxy/SOCKS server
     *
     * @param   ioe
     *          The I/O exception thrown when the connect failed.
     * @throws IllegalArgumentException if either argument is null
     */
    public abstract void connectFailed(URI uri, SocketAddress sa, IOException ioe);
}
