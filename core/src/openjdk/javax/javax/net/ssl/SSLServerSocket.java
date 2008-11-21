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


package javax.net.ssl;

import java.io.*;
import java.net.*;


/**
 * This class extends <code>ServerSocket</code>s and
 * provides secure server sockets using protocols such as the Secure
 * Sockets Layer (SSL) or Transport Layer Security (TLS) protocols.
 * <P>
 * Instances of this class are generally created using a
 * <code>SSLServerSocketFactory</code>.  The primary function
 * of <code>SSLServerSocket</code>s
 * is to create <code>SSLSocket</code>s by <code>accept</code>ing
 * connections.
 * <P>
 * <code>SSLServerSocket</code>s contain several pieces of state data
 * which are inherited by the <code>SSLSocket</code> at
 * socket creation.  These include the enabled cipher
 * suites and protocols, whether client
 * authentication is necessary, and whether created sockets should
 * begin handshaking in client or server mode.  The state
 * inherited by the created <code>SSLSocket</code> can be
 * overriden by calling the appropriate methods.
 *
 * @see java.net.ServerSocket
 * @see SSLSocket
 *
 * @since 1.4
 * @author David Brownell
 */
public abstract class SSLServerSocket extends ServerSocket
{
    /**
     * Used only by subclasses.
     * <P>
     * Create an unbound TCP server socket using the default authentication
     * context.
     *
     * @throws IOException if an I/O error occurs when creating the socket
     */
    protected SSLServerSocket()
    throws IOException
	{ super(); }


    /**
     * Used only by subclasses.
     * <P>
     * Create a TCP server socket on a port, using the default
     * authentication context.  The connection backlog defaults to
     * fifty connections queued up before the system starts to
     * reject new connection requests.
     *
     * @param port the port on which to listen
     * @throws IOException if an I/O error occurs when creating the socket
     */
    protected SSLServerSocket(int port)
    throws IOException
	{ super(port); }


    /**
     * Used only by subclasses.
     * <P>
     * Create a TCP server socket on a port, using the default
     * authentication context and a specified backlog of connections.
     *
     * @param port the port on which to listen
     * @param backlog how many connections may be pending before
     *		the system should start rejecting new requests
     * @throws IOException if an I/O error occurs when creating the socket
     */
    protected SSLServerSocket(int port, int backlog)
    throws IOException
	{ super(port, backlog); }


    /**
     * Used only by subclasses.
     * <P>
     * Create a TCP server socket on a port, using the default
     * authentication context and a specified backlog of connections
     * as well as a particular specified network interface.  This
     * constructor is used on multihomed hosts, such as those used
     * for firewalls or as routers, to control through which interface
     * a network service is provided.
     *
     * @param port the port on which to listen
     * @param backlog how many connections may be pending before
     *		the system should start rejecting new requests
     * @param address the address of the network interface through
     *		which connections will be accepted
     * @throws IOException if an I/O error occurs when creating the socket
     */
    protected SSLServerSocket(int port, int backlog, InetAddress address)
    throws IOException
	{ super(port, backlog, address); }



    /**
     * Returns the list of cipher suites which are currently enabled
     * for use by newly accepted connections.
     * <P>
     * If this list has not been explicitly modified, a system-provided
     * default guarantees a minimum quality of service in all enabled
     * cipher suites.
     * <P>
     * There are several reasons why an enabled cipher suite might
     * not actually be used.  For example:  the server socket might
     * not have appropriate private keys available to it or the cipher
     * suite might be anonymous, precluding the use of client authentication,
     * while the server socket has been told to require that sort of
     * authentication.
     *
     * @return an array of cipher suites enabled
     * @see #getSupportedCipherSuites()
     * @see #setEnabledCipherSuites(String [])
     */
    public abstract String [] getEnabledCipherSuites();


    /**
     * Sets the cipher suites enabled for use by accepted connections.
     * <P>
     * The cipher suites must have been listed by getSupportedCipherSuites()
     * as being supported.  Following a successful call to this method,
     * only suites listed in the <code>suites</code> parameter are enabled
     * for use.
     * <P>
     * Suites that require authentication information which is not available
     * in this ServerSocket's authentication context will not be used
     * in any case, even if they are enabled.
     * <P>
     * <code>SSLSocket</code>s returned from <code>accept()</code>
     * inherit this setting.
     *
     * @param suites Names of all the cipher suites to enable
     * @exception IllegalArgumentException when one or more of ciphers
     *		named by the parameter is not supported, or when
     *		the parameter is null.
     * @see #getSupportedCipherSuites()
     * @see #getEnabledCipherSuites()
     */
    public abstract void setEnabledCipherSuites(String suites []);


    /**
     * Returns the names of the cipher suites which could be enabled for use
     * on an SSL connection.
     * <P>
     * Normally, only a subset of these will actually
     * be enabled by default, since this list may include cipher suites which
     * do not meet quality of service requirements for those defaults.  Such
     * cipher suites are useful in specialized applications.
     *
     * @return an array of cipher suite names
     * @see #getEnabledCipherSuites()
     * @see #setEnabledCipherSuites(String [])
     */
    public abstract String [] getSupportedCipherSuites();


    /**
     * Returns the names of the protocols which could be enabled for use.
     *
     * @return an array of protocol names supported
     * @see #getEnabledProtocols()
     * @see #setEnabledProtocols(String [])
     */
    public abstract String [] getSupportedProtocols();


    /**
     * Returns the names of the protocols which are currently
     * enabled for use by the newly accepted connections.
     *
     * @return an array of protocol names
     * @see #getSupportedProtocols()
     * @see #setEnabledProtocols(String [])
     */
    public abstract String [] getEnabledProtocols();


    /**
     * Controls which particular protocols are enabled for use by
     * accepted connections.
     * <P>
     * The protocols must have been listed by
     * getSupportedProtocols() as being supported.
     * Following a successful call to this method, only protocols listed
     * in the <code>protocols</code> parameter are enabled for use.
     * <P>
     * <code>SSLSocket</code>s returned from <code>accept()</code>
     * inherit this setting.
     *
     * @param protocols Names of all the protocols to enable.
     * @exception IllegalArgumentException when one or more of
     *		  the protocols named by the parameter is not supported or
     *		  when the protocols parameter is null.
     * @see #getEnabledProtocols()
     * @see #getSupportedProtocols()
     */
    public abstract void setEnabledProtocols(String protocols[]);


    /**
     * Controls whether <code>accept</code>ed server-mode
     * <code>SSLSockets</code> will be initially configured to
     * <i>require</i> client authentication.
     * <P>
     * A socket's client authentication setting is one of the following:
     * <ul>
     * <li> client authentication required
     * <li> client authentication requested
     * <li> no client authentication desired
     * </ul>
     * <P>
     * Unlike {@link #setWantClientAuth(boolean)}, if the accepted
     * socket's option is set and the client chooses not to provide
     * authentication information about itself, <i>the negotiations
     * will stop and the connection will be dropped</i>.
     * <P>
     * Calling this method overrides any previous setting made by
     * this method or {@link #setWantClientAuth(boolean)}.
     * <P>
     * The initial inherited setting may be overridden by calling
     * {@link SSLSocket#setNeedClientAuth(boolean)} or
     * {@link SSLSocket#setWantClientAuth(boolean)}.
     *
     * @param	need set to true if client authentication is required,
     *		or false if no client authentication is desired.
     * @see #getNeedClientAuth()
     * @see #setWantClientAuth(boolean)
     * @see #getWantClientAuth()
     * @see #setUseClientMode(boolean)
     */
    public abstract void setNeedClientAuth(boolean need);


    /**
     * Returns true if client authentication will be <i>required</i> on
     * newly <code>accept</code>ed server-mode <code>SSLSocket</code>s.
     * <P>
     * The initial inherited setting may be overridden by calling
     * {@link SSLSocket#setNeedClientAuth(boolean)} or
     * {@link SSLSocket#setWantClientAuth(boolean)}.
     *
     * @return	true if client authentication is required,
     *		or false if no client authentication is desired.
     * @see #setNeedClientAuth(boolean)
     * @see #setWantClientAuth(boolean)
     * @see #getWantClientAuth()
     * @see #setUseClientMode(boolean)
     */
    public abstract boolean getNeedClientAuth();


    /**
     * Controls whether <code>accept</code>ed server-mode
     * <code>SSLSockets</code> will be initially configured to
     * <i>request</i> client authentication.
     * <P>
     * A socket's client authentication setting is one of the following:
     * <ul>
     * <li> client authentication required
     * <li> client authentication requested
     * <li> no client authentication desired
     * </ul>
     * <P>
     * Unlike {@link #setNeedClientAuth(boolean)}, if the accepted
     * socket's option is set and the client chooses not to provide
     * authentication information about itself, <i>the negotiations
     * will continue</i>.
     * <P>
     * Calling this method overrides any previous setting made by
     * this method or {@link #setNeedClientAuth(boolean)}.
     * <P>
     * The initial inherited setting may be overridden by calling
     * {@link SSLSocket#setNeedClientAuth(boolean)} or
     * {@link SSLSocket#setWantClientAuth(boolean)}.
     *
     * @param	want set to true if client authentication is requested,
     *		or false if no client authentication is desired.
     * @see #getWantClientAuth()
     * @see #setNeedClientAuth(boolean)
     * @see #getNeedClientAuth()
     * @see #setUseClientMode(boolean)
     */
    public abstract void setWantClientAuth(boolean want);


    /**
     * Returns true if client authentication will be <i>requested</i> on
     * newly accepted server-mode connections.
     * <P>
     * The initial inherited setting may be overridden by calling
     * {@link SSLSocket#setNeedClientAuth(boolean)} or
     * {@link SSLSocket#setWantClientAuth(boolean)}.
     *
     * @return	true if client authentication is requested,
     *		or false if no client authentication is desired.
     * @see #setWantClientAuth(boolean)
     * @see #setNeedClientAuth(boolean)
     * @see #getNeedClientAuth()
     * @see #setUseClientMode(boolean)
     */
    public abstract boolean getWantClientAuth();


    /**
     * Controls whether accepted connections are in the (default) SSL
     * server mode, or the SSL client mode.
     * <P>
     * Servers normally authenticate themselves, and clients are not
     * required to do so.
     * <P>
     * In rare cases, TCP servers
     * need to act in the SSL client mode on newly accepted
     * connections. For example, FTP clients acquire server sockets
     * and listen there for reverse connections from the server. An
     * FTP client would use an SSLServerSocket in "client" mode to
     * accept the reverse connection while the FTP server uses an
     * SSLSocket with "client" mode disabled to initiate the
     * connection. During the resulting handshake, existing SSL
     * sessions may be reused.
     * <P>
     * <code>SSLSocket</code>s returned from <code>accept()</code>
     * inherit this setting.
     *
     * @param mode true if newly accepted connections should use SSL
     *		client mode.
     * @see #getUseClientMode()
     */
    public abstract void setUseClientMode(boolean mode);


    /**
     * Returns true if accepted connections will be in SSL client mode.
     *
     * @see #setUseClientMode(boolean)
     * @return true if the connection should use SSL client mode.
     */
    public abstract boolean getUseClientMode();


    /**
     * Controls whether new SSL sessions may be established by the
     * sockets which are created from this server socket.
     * <P>
     * <code>SSLSocket</code>s returned from <code>accept()</code>
     * inherit this setting.
     *
     * @param flag true indicates that sessions may be created; this
     *		is the default. false indicates that an existing session
     *		must be resumed.
     * @see #getEnableSessionCreation()
     */
    public abstract void setEnableSessionCreation(boolean flag);


    /**
     * Returns true if new SSL sessions may be established by the
     * sockets which are created from this server socket.
     *
     * @return true indicates that sessions may be created; this
     *		is the default.  false indicates that an existing
     *		session must be resumed.
     * @see #setEnableSessionCreation(boolean)
     */
    public abstract boolean getEnableSessionCreation();
}
