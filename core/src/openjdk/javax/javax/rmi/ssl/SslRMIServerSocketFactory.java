/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.rmi.ssl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * <p>An <code>SslRMIServerSocketFactory</code> instance is used by the RMI
 * runtime in order to obtain server sockets for RMI calls via SSL.</p>
 *
 * <p>This class implements <code>RMIServerSocketFactory</code> over
 * the Secure Sockets Layer (SSL) or Transport Layer Security (TLS)
 * protocols.</p>
 *
 * <p>This class creates SSL sockets using the default
 * <code>SSLSocketFactory</code> (see {@link
 * SSLSocketFactory#getDefault}) or the default
 * <code>SSLServerSocketFactory</code> (see {@link
 * SSLServerSocketFactory#getDefault}).  Therefore, all instances of
 * this class share the same keystore, and the same truststore, when
 * client authentication is required by the server.  This behavior
 * can be modified in subclasses by overriding the {@link
 * #createServerSocket(int)} method; in that case, {@link
 * #equals(Object) equals} and {@link #hashCode() hashCode} may also
 * need to be overridden.</p>
 *
 * @see javax.net.ssl.SSLSocketFactory
 * @see javax.net.ssl.SSLServerSocketFactory
 * @see javax.rmi.ssl.SslRMIClientSocketFactory
 * @since 1.5
 */
public class SslRMIServerSocketFactory implements RMIServerSocketFactory {

    /**
     * <p>Creates a new <code>SslRMIServerSocketFactory</code> with
     * the default SSL socket configuration.</p>
     *
     * <p>SSL connections accepted by server sockets created by this
     * factory have the default cipher suites and protocol versions
     * enabled and do not require client authentication.</p>
     */
    public SslRMIServerSocketFactory() {
        this(null, null, false);
    }

    /**
     * <p>Creates a new <code>SslRMIServerSocketFactory</code> with
     * the specified SSL socket configuration.</p>
     *
     * @param enabledCipherSuites names of all the cipher suites to
     * enable on SSL connections accepted by server sockets created by
     * this factory, or <code>null</code> to use the cipher suites
     * that are enabled by default
     *
     * @param enabledProtocols names of all the protocol versions to
     * enable on SSL connections accepted by server sockets created by
     * this factory, or <code>null</code> to use the protocol versions
     * that are enabled by default
     *
     * @param needClientAuth <code>true</code> to require client
     * authentication on SSL connections accepted by server sockets
     * created by this factory; <code>false</code> to not require
     * client authentication
     *
     * @exception IllegalArgumentException when one or more of the cipher
     * suites named by the <code>enabledCipherSuites</code> parameter is
     * not supported, when one or more of the protocols named by the
     * <code>enabledProtocols</code> parameter is not supported or when
     * a problem is encountered while trying to check if the supplied
     * cipher suites and protocols to be enabled are supported.
     *
     * @see SSLSocket#setEnabledCipherSuites
     * @see SSLSocket#setEnabledProtocols
     * @see SSLSocket#setNeedClientAuth
     */
    public SslRMIServerSocketFactory(String[] enabledCipherSuites,
                                     String[] enabledProtocols,
                                     boolean needClientAuth)
	throws IllegalArgumentException {

        // Initialize the configuration parameters.
        //
        this.enabledCipherSuites = enabledCipherSuites == null ?
	    null : (String[]) enabledCipherSuites.clone();
        this.enabledProtocols = enabledProtocols == null ?
	    null : (String[]) enabledProtocols.clone();
        this.needClientAuth = needClientAuth;

        // Force the initialization of the default at construction time,
        // rather than delaying it to the first time createServerSocket()
        // is called.
        //
        final SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();
	SSLSocket sslSocket = null;
        if (this.enabledCipherSuites != null || this.enabledProtocols != null) {
	    try {
		sslSocket = (SSLSocket) sslSocketFactory.createSocket();
	    } catch (Exception e) {
		final String msg = "Unable to check if the cipher suites " +
		    "and protocols to enable are supported";
		throw (IllegalArgumentException)
		    new IllegalArgumentException(msg).initCause(e);
	    }
	}

	// Check if all the cipher suites and protocol versions to enable
	// are supported by the underlying SSL/TLS implementation and if
	// true create lists from arrays.
	//
        if (this.enabledCipherSuites != null) {
	    sslSocket.setEnabledCipherSuites(this.enabledCipherSuites);
            enabledCipherSuitesList =
                Arrays.asList((String[]) this.enabledCipherSuites);
	}
        if (this.enabledProtocols != null) {
	    sslSocket.setEnabledProtocols(this.enabledProtocols);
            enabledProtocolsList =
                Arrays.asList((String[]) this.enabledProtocols);
	}
    }

    /**
     * <p>Returns the names of the cipher suites enabled on SSL
     * connections accepted by server sockets created by this factory,
     * or <code>null</code> if this factory uses the cipher suites
     * that are enabled by default.</p>
     *
     * @return an array of cipher suites enabled, or <code>null</code>
     *
     * @see SSLSocket#setEnabledCipherSuites
     */
    public final String[] getEnabledCipherSuites() {
        return enabledCipherSuites == null ?
	null : (String[]) enabledCipherSuites.clone();
    }

    /**
     * <p>Returns the names of the protocol versions enabled on SSL
     * connections accepted by server sockets created by this factory,
     * or <code>null</code> if this factory uses the protocol versions
     * that are enabled by default.</p>
     *
     * @return an array of protocol versions enabled, or
     * <code>null</code>
     *
     * @see SSLSocket#setEnabledProtocols
     */
    public final String[] getEnabledProtocols() {
        return enabledProtocols == null ?
	null : (String[]) enabledProtocols.clone();
    }

    /**
     * <p>Returns <code>true</code> if client authentication is
     * required on SSL connections accepted by server sockets created
     * by this factory.</p>
     *
     * @return <code>true</code> if client authentication is required
     *
     * @see SSLSocket#setNeedClientAuth
     */
    public final boolean getNeedClientAuth() {
        return needClientAuth;
    }

    /**
     * <p>Creates a server socket that accepts SSL connections
     * configured according to this factory's SSL socket configuration
     * parameters.</p>
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        final SSLSocketFactory sslSocketFactory = getDefaultSSLSocketFactory();
        return new ServerSocket(port) {
            public Socket accept() throws IOException {
                Socket socket = super.accept();
                SSLSocket sslSocket = (SSLSocket)
                    sslSocketFactory.createSocket(
                        socket, socket.getInetAddress().getHostName(),
                        socket.getPort(), true);
                sslSocket.setUseClientMode(false);
                if (enabledCipherSuites != null) {
                    sslSocket.setEnabledCipherSuites(enabledCipherSuites);
                }
                if (enabledProtocols != null) {
                    sslSocket.setEnabledProtocols(enabledProtocols);
                }
                sslSocket.setNeedClientAuth(needClientAuth);
                return sslSocket;
            }
        };

        // If we do not instantiate the server socket class, but
        // instead must layer on top of an arbitrary server socket,
        // then this implementation would become uglier, like this
        // (given "serverSocket" to layer on top of):
        //
        // return new ForwardingServerSocket(serverSocket) {
        //    public Socket accept() throws IOException {
        //      Socket socket = serverSocket.accept();
        //      SSLSocket sslSocket =
        //          (SSLSocket) sslSocketFactory.createSocket(
        //              socket,
        //              socket.getInetAddress().getHostName(),
        //              socket.getPort(),
        //              true);
        //      sslSocket.setUseClientMode(false);
        //      if (enabledProtocols != null) {
        //          sslSocket.setEnabledProtocols(enabledProtocols);
        //      }
        //      if (enabledCipherSuites != null) {
        //          sslSocket.setEnabledCipherSuites(enabledCipherSuites);
        //      }
        //      sslSocket.setNeedClientAuth(needClientAuth);
        //      return sslSocket;
        //    }
        //    public ServerSocketChannel getChannel() {
        //      return null;
        //    }
        //    public String toString() {
        //      return serverSocket.toString();
        //    }
        // };
    }

    /**
     * <p>Indicates whether some other object is "equal to" this one.</p>
     *
     * <p>Two <code>SslRMIServerSocketFactory</code> objects are equal
     * if they have been constructed with the same SSL socket
     * configuration parameters.</p>
     *
     * <p>A subclass should override this method (as well as
     * {@link #hashCode()}) if it adds instance state that affects
     * equality.</p>
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof SslRMIServerSocketFactory))
            return false;
        SslRMIServerSocketFactory that = (SslRMIServerSocketFactory) obj;
        return (getClass().equals(that.getClass()) && checkParameters(that));
    }

    private boolean checkParameters(SslRMIServerSocketFactory that) {
        // needClientAuth flag
        //
        if (needClientAuth != that.needClientAuth)
            return false;

        // enabledCipherSuites
        //
        if ((enabledCipherSuites == null && that.enabledCipherSuites != null) ||
            (enabledCipherSuites != null && that.enabledCipherSuites == null))
            return false;
        if (enabledCipherSuites != null && that.enabledCipherSuites != null) {
            List thatEnabledCipherSuitesList =
                Arrays.asList((String[]) that.enabledCipherSuites);
            if (!enabledCipherSuitesList.equals(thatEnabledCipherSuitesList))
                return false;
        }

        // enabledProtocols
        //
        if ((enabledProtocols == null && that.enabledProtocols != null) ||
            (enabledProtocols != null && that.enabledProtocols == null))
            return false;
        if (enabledProtocols != null && that.enabledProtocols != null) {
            List thatEnabledProtocolsList =
                Arrays.asList((String[]) that.enabledProtocols);
            if (!enabledProtocolsList.equals(thatEnabledProtocolsList))
                return false;
        }

        return true;
    }

    /**
     * <p>Returns a hash code value for this
     * <code>SslRMIServerSocketFactory</code>.</p>
     *
     * @return a hash code value for this
     * <code>SslRMIServerSocketFactory</code>.
     */
    public int hashCode() {
        return getClass().hashCode() +
        (needClientAuth ? Boolean.TRUE.hashCode() : Boolean.FALSE.hashCode()) +
        (enabledCipherSuites == null ? 0 : enabledCipherSuitesList.hashCode()) +
        (enabledProtocols == null ? 0 : enabledProtocolsList.hashCode());
    }

    // We use a static field because:
    //
    //    SSLSocketFactory.getDefault() always returns the same object
    //    (at least on Sun's implementation), and we want to make sure that
    //    the Javadoc & the implementation stay in sync.
    //
    // If someone needs to have different SslRMIServerSocketFactory factories
    // with different underlying SSLSocketFactory objects using different
    // key and trust stores, he can always do so by subclassing this class and
    // overriding createServerSocket(int port).
    //
    private static SSLSocketFactory defaultSSLSocketFactory = null;

    private static synchronized SSLSocketFactory getDefaultSSLSocketFactory() {
        if (defaultSSLSocketFactory == null)
            defaultSSLSocketFactory =
                (SSLSocketFactory) SSLSocketFactory.getDefault();
        return defaultSSLSocketFactory;
    }

    private final String[] enabledCipherSuites;
    private final String[] enabledProtocols;
    private final boolean needClientAuth;
    private List enabledCipherSuitesList;
    private List enabledProtocolsList;

//    private static class ForwardingServerSocket extends ServerSocket {
//      private final ServerSocket ss;
//      ForwardingServerSocket(ServerSocket ss) throws IOException {
//          super();
//          this.ss = ss;
//      }
//      public void bind(SocketAddress endpoint) throws IOException {
//          ss.bind(endpoint);
//      }
//      public void bind(SocketAddress endpoint, int backlog)
//          throws IOException
//      {
//          ss.bind(endpoint, backlog);
//      }
//      public InetAddress getInetAddress() {
//          return ss.getInetAddress();
//      }
//      public int getLocalPort() {
//          return ss.getLocalPort();
//      }
//      public SocketAddress getLocalSocketAddress() {
//          return ss.getLocalSocketAddress();
//      }
//      public Socket accept() throws IOException {
//          return ss.accept();
//      }
//      public void close() throws IOException {
//          ss.close();
//      }
//      public ServerSocketChannel getChannel() {
//          return ss.getChannel();
//      }
//      public boolean isBound() {
//          return ss.isBound();
//      }
//      public boolean isClosed() {
//          return ss.isClosed();
//      }
//      public void setSoTimeout(int timeout) throws SocketException {
//          ss.setSoTimeout(timeout);
//      }
//      public int getSoTimeout() throws IOException {
//          return ss.getSoTimeout();
//      }
//      public void setReuseAddress(boolean on) throws SocketException {
//          ss.setReuseAddress(on);
//      }
//      public boolean getReuseAddress() throws SocketException {
//          return ss.getReuseAddress();
//      }
//      public String toString() {
//          return ss.toString();
//      }
//      public void setReceiveBufferSize(int size) throws SocketException {
//          ss.setReceiveBufferSize(size);
//      }
//      public int getReceiveBufferSize() throws SocketException {
//          return ss.getReceiveBufferSize();
//      }
//    }
}
