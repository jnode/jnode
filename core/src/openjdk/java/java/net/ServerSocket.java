/*
 * Copyright 1995-2007 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * This class implements server sockets. A server socket waits for
 * requests to come in over the network. It performs some operation
 * based on that request, and then possibly returns a result to the requester.
 * <p>
 * The actual work of the server socket is performed by an instance
 * of the <code>SocketImpl</code> class. An application can
 * change the socket factory that creates the socket
 * implementation to configure itself to create sockets
 * appropriate to the local firewall.
 *
 * @author  unascribed
 * @see     java.net.SocketImpl
 * @see     java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
 * @see     java.nio.channels.ServerSocketChannel
 * @since   JDK1.0
 */
public
class ServerSocket {
    /**
     * Various states of this socket.
     */
    private boolean created = false;
    private boolean bound = false;
    private boolean closed = false;
    private Object closeLock = new Object();

    /**
     * The implementation of this Socket.
     */
    private SocketImpl impl;

    /**
     * Are we using an older SocketImpl?
     */
    private boolean oldImpl = false;

    /**
     * Creates an unbound server socket.
     *
     * @exception IOException IO error when opening the socket.
     * @revised 1.4
     */
    public ServerSocket() throws IOException {
        setImpl();
    }

    /**
     * Creates a server socket, bound to the specified port. A port of
     * <code>0</code> creates a socket on any free port.
     * <p>
     * The maximum queue length for incoming connection indications (a
     * request to connect) is set to <code>50</code>. If a connection
     * indication arrives when the queue is full, the connection is refused.
     * <p>
     * If the application has specified a server socket factory, that
     * factory's <code>createSocketImpl</code> method is called to create
     * the actual socket implementation. Otherwise a "plain" socket is created.
     * <p>
     * If there is a security manager,
     * its <code>checkListen</code> method is called
     * with the <code>port</code> argument
     * as its argument to ensure the operation is allowed.
     * This could result in a SecurityException.
     *
     *
     * @param      port  the port number, or <code>0</code> to use any
     *                   free port.
     *
     * @exception  IOException  if an I/O error occurs when opening the socket.
     * @exception  SecurityException
     * if a security manager exists and its <code>checkListen</code>
     * method doesn't allow the operation.
     *
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see        SecurityManager#checkListen
     */
    public ServerSocket(int port) throws IOException {
        this(port, 50, null);
    }

    /**
     * Creates a server socket and binds it to the specified local port
     * number, with the specified backlog.
     * A port number of <code>0</code> creates a socket on any
     * free port.
     * <p>
     * The maximum queue length for incoming connection indications (a
     * request to connect) is set to the <code>backlog</code> parameter. If
     * a connection indication arrives when the queue is full, the
     * connection is refused.
     * <p>
     * If the application has specified a server socket factory, that
     * factory's <code>createSocketImpl</code> method is called to create
     * the actual socket implementation. Otherwise a "plain" socket is created.
     * <p>
     * If there is a security manager,
     * its <code>checkListen</code> method is called
     * with the <code>port</code> argument
     * as its argument to ensure the operation is allowed.
     * This could result in a SecurityException.
     *
     * <P>The <code>backlog</code> argument must be a positive
     * value greater than 0. If the value passed is equal or less
     * than 0, then the default value will be assumed.
     * <P>
     *
     * @param      port     the port number, or <code>0</code> to use
     *                      any free port.
     * @param      backlog  the maximum length of the queue.
     *
     * @exception  IOException  if an I/O error occurs when opening the socket.
     * @exception  SecurityException
     * if a security manager exists and its <code>checkListen</code>
     * method doesn't allow the operation.
     *
     * @see        java.net.SocketImpl
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        java.net.ServerSocket#setSocketFactory(java.net.SocketImplFactory)
     * @see        SecurityManager#checkListen
     */
    public ServerSocket(int port, int backlog) throws IOException {
        this(port, backlog, null);
    }

    /**
     * Create a server with the specified port, listen backlog, and
     * local IP address to bind to.  The <i>bindAddr</i> argument
     * can be used on a multi-homed host for a ServerSocket that
     * will only accept connect requests to one of its addresses.
     * If <i>bindAddr</i> is null, it will default accepting
     * connections on any/all local addresses.
     * The port must be between 0 and 65535, inclusive.
     *
     * <P>If there is a security manager, this method
     * calls its <code>checkListen</code> method
     * with the <code>port</code> argument
     * as its argument to ensure the operation is allowed.
     * This could result in a SecurityException.
     *
     * <P>The <code>backlog</code> argument must be a positive
     * value greater than 0. If the value passed is equal or less
     * than 0, then the default value will be assumed.
     * <P>
     * @param port the local TCP port
     * @param backlog the listen backlog
     * @param bindAddr the local InetAddress the server will bind to
     *
     * @throws  SecurityException if a security manager exists and
     * its <code>checkListen</code> method doesn't allow the operation.
     *
     * @throws  IOException if an I/O error occurs when opening the socket.
     *
     * @see SocketOptions
     * @see SocketImpl
     * @see SecurityManager#checkListen
     * @since   JDK1.1
     */
    public ServerSocket(int port, int backlog, InetAddress bindAddr) throws IOException {
        setImpl();
        if (port < 0 || port > 0xFFFF)
            throw new IllegalArgumentException(
                       "Port value out of range: " + port);
        if (backlog < 1)
          backlog = 50;
        try {
            bind(new InetSocketAddress(bindAddr, port), backlog);
        } catch(SecurityException e) {
            close();
            throw e;
        } catch(IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Get the <code>SocketImpl</code> attached to this socket, creating
     * it if necessary.
     *
     * @return  the <code>SocketImpl</code> attached to that ServerSocket.
     * @throws SocketException if creation fails.
     * @since 1.4
     */
    SocketImpl getImpl() throws SocketException {
        if (!created)
            createImpl();
        return impl;
    }

    private void checkOldImpl() {
        if (impl == null)
            return;
        // SocketImpl.connect() is a protected method, therefore we need to use
        // getDeclaredMethod, therefore we need permission to access the member
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                    public Object run() throws NoSuchMethodException {
                        Class[] cl = new Class[2];
                        cl[0] = SocketAddress.class;
                        cl[1] = Integer.TYPE;
                        impl.getClass().getDeclaredMethod("connect", cl);
                        return null;
                    }
                });
        } catch (java.security.PrivilegedActionException e) {
            oldImpl = true;
        }
    }

    private void setImpl() {
        if (factory != null) {
            impl = factory.createSocketImpl();
            checkOldImpl();
        } else {
            // No need to do a checkOldImpl() here, we know it's an up to date
            // SocketImpl!
            impl = new SocksSocketImpl();
        }
        if (impl != null)
            impl.setServerSocket(this);
    }

    /**
     * Creates the socket implementation.
     *
     * @throws IOException if creation fails
     * @since 1.4
     */
    void createImpl() throws SocketException {
        if (impl == null)
            setImpl();
        try {
            impl.create(true);
            created = true;
        } catch (IOException e) {
            throw new SocketException(e.getMessage());
        }
    }

    /**
     *
     * Binds the <code>ServerSocket</code> to a specific address
     * (IP address and port number).
     * <p>
     * If the address is <code>null</code>, then the system will pick up
     * an ephemeral port and a valid local address to bind the socket.
     * <p>
     * @param   endpoint        The IP address & port number to bind to.
     * @throws  IOException if the bind operation fails, or if the socket
     *                     is already bound.
     * @throws  SecurityException       if a <code>SecurityManager</code> is present and
     * its <code>checkListen</code> method doesn't allow the operation.
     * @throws  IllegalArgumentException if endpoint is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */
    public void bind(SocketAddress endpoint) throws IOException {
        bind(endpoint, 50);
    }

    /**
     *
     * Binds the <code>ServerSocket</code> to a specific address
     * (IP address and port number).
     * <p>
     * If the address is <code>null</code>, then the system will pick up
     * an ephemeral port and a valid local address to bind the socket.
     * <P>
     * The <code>backlog</code> argument must be a positive
     * value greater than 0. If the value passed is equal or less
     * than 0, then the default value will be assumed.
     * @param   endpoint        The IP address & port number to bind to.
     * @param   backlog         The listen backlog length.
     * @throws  IOException if the bind operation fails, or if the socket
     *                     is already bound.
     * @throws  SecurityException       if a <code>SecurityManager</code> is present and
     * its <code>checkListen</code> method doesn't allow the operation.
     * @throws  IllegalArgumentException if endpoint is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */
    public void bind(SocketAddress endpoint, int backlog) throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!oldImpl && isBound())
            throw new SocketException("Already bound");
        if (endpoint == null)
            endpoint = new InetSocketAddress(0);
        if (!(endpoint instanceof InetSocketAddress))
            throw new IllegalArgumentException("Unsupported address type");
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (epoint.isUnresolved())
            throw new SocketException("Unresolved address");
        if (backlog < 1)
          backlog = 50;
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null)
                security.checkListen(epoint.getPort());
            getImpl().bind(epoint.getAddress(), epoint.getPort());
            getImpl().listen(backlog);
            bound = true;
        } catch(SecurityException e) {
            bound = false;
            throw e;
        } catch(IOException e) {
            bound = false;
            throw e;
        }
    }

    /**
     * Returns the local address of this server socket.
     *
     * @return  the address to which this socket is bound,
     *          or <code>null</code> if the socket is unbound.
     */
    public InetAddress getInetAddress() {
        if (!isBound())
            return null;
        try {
            return getImpl().getInetAddress();
        } catch (SocketException e) {
            // nothing
            // If we're bound, the impl has been created
            // so we shouldn't get here
        }
        return null;
    }

    /**
     * Returns the port number on which this socket is listening.
     *
     * @return  the port number to which this socket is listening or
     *          -1 if the socket is not bound yet.
     */
    public int getLocalPort() {
        if (!isBound())
            return -1;
        try {
            return getImpl().getLocalPort();
        } catch (SocketException e) {
            // nothing
            // If we're bound, the impl has been created
            // so we shouldn't get here
        }
        return -1;
    }

    /**
     * Returns the address of the endpoint this socket is bound to, or
     * <code>null</code> if it is not bound yet.
     *
     * @return a <code>SocketAddress</code> representing the local endpoint of this
     *         socket, or <code>null</code> if it is not bound yet.
     * @see #getInetAddress()
     * @see #getLocalPort()
     * @see #bind(SocketAddress)
     * @since 1.4
     */

    public SocketAddress getLocalSocketAddress() {
        if (!isBound())
            return null;
        return new InetSocketAddress(getInetAddress(), getLocalPort());
    }

    /**
     * Listens for a connection to be made to this socket and accepts
     * it. The method blocks until a connection is made.
     *
     * <p>A new Socket <code>s</code> is created and, if there
     * is a security manager,
     * the security manager's <code>checkAccept</code> method is called
     * with <code>s.getInetAddress().getHostAddress()</code> and
     * <code>s.getPort()</code>
     * as its arguments to ensure the operation is allowed.
     * This could result in a SecurityException.
     *
     * @exception  IOException  if an I/O error occurs when waiting for a
     *               connection.
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkAccept</code> method doesn't allow the operation.
     * @exception  SocketTimeoutException if a timeout was previously set with setSoTimeout and
     *             the timeout has been reached.
     * @exception  java.nio.channels.IllegalBlockingModeException
     *             if this socket has an associated channel, the channel is in
     *             non-blocking mode, and there is no connection ready to be
     *             accepted
     *
     * @return the new Socket
     * @see SecurityManager#checkAccept
     * @revised 1.4
     * @spec JSR-51
     */
    public Socket accept() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        if (!isBound())
            throw new SocketException("Socket is not bound yet");
        Socket s = new Socket((SocketImpl) null);
        implAccept(s);
        return s;
    }

    /**
     * Subclasses of ServerSocket use this method to override accept()
     * to return their own subclass of socket.  So a FooServerSocket
     * will typically hand this method an <i>empty</i> FooSocket.  On
     * return from implAccept the FooSocket will be connected to a client.
     *
     * @param s the Socket
     * @throws java.nio.channels.IllegalBlockingModeException
     *         if this socket has an associated channel,
     *         and the channel is in non-blocking mode
     * @throws IOException if an I/O error occurs when waiting
     * for a connection.
     * @since   JDK1.1
     * @revised 1.4
     * @spec JSR-51
     */
    protected final void implAccept(Socket s) throws IOException {
        SocketImpl si = null;
        try {
            if (s.impl == null)
              s.setImpl();
            else {
                s.impl.reset();
            }
            si = s.impl;
            s.impl = null;
            si.address = new InetAddress();
            si.fd = new FileDescriptor();
            getImpl().accept(si);

            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkAccept(si.getInetAddress().getHostAddress(),
                                     si.getPort());
            }
        } catch (IOException e) {
            if (si != null)
                si.reset();
            s.impl = si;
            throw e;
        } catch (SecurityException e) {
            if (si != null)
                si.reset();
            s.impl = si;
            throw e;
        }
        s.impl = si;
        s.postAccept();
    }

    /**
     * Closes this socket.
     *
     * Any thread currently blocked in {@link #accept()} will throw
     * a {@link SocketException}.
     *
     * <p> If this socket has an associated channel then the channel is closed
     * as well.
     *
     * @exception  IOException  if an I/O error occurs when closing the socket.
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() throws IOException {
        synchronized(closeLock) {
            if (isClosed())
                return;
            if (created)
                impl.close();
            closed = true;
        }
    }

    /**
     * Returns the unique {@link java.nio.channels.ServerSocketChannel} object
     * associated with this socket, if any.
     *
     * <p> A server socket will have a channel if, and only if, the channel
     * itself was created via the {@link
     * java.nio.channels.ServerSocketChannel#open ServerSocketChannel.open}
     * method.
     *
     * @return  the server-socket channel associated with this socket,
     *          or <tt>null</tt> if this socket was not created
     *          for a channel
     *
     * @since 1.4
     * @spec JSR-51
     */
    public ServerSocketChannel getChannel() {
        return null;
    }

    /**
     * Returns the binding state of the ServerSocket.
     *
     * @return true if the ServerSocket succesfuly bound to an address
     * @since 1.4
     */
    public boolean isBound() {
        // Before 1.3 ServerSockets were always bound during creation
        return bound || oldImpl;
    }

    /**
     * Returns the closed state of the ServerSocket.
     *
     * @return true if the socket has been closed
     * @since 1.4
     */
    public boolean isClosed() {
        synchronized(closeLock) {
            return closed;
        }
    }

    /**
     * Enable/disable SO_TIMEOUT with the specified timeout, in
     * milliseconds.  With this option set to a non-zero timeout,
     * a call to accept() for this ServerSocket
     * will block for only this amount of time.  If the timeout expires,
     * a <B>java.net.SocketTimeoutException</B> is raised, though the
     * ServerSocket is still valid.  The option <B>must</B> be enabled
     * prior to entering the blocking operation to have effect.  The
     * timeout must be > 0.
     * A timeout of zero is interpreted as an infinite timeout.
     * @param timeout the specified timeout, in milliseconds
     * @exception SocketException if there is an error in
     * the underlying protocol, such as a TCP error.
     * @since   JDK1.1
     * @see #getSoTimeout()
     */
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
    }

    /**
     * Retrieve setting for SO_TIMEOUT.  0 returns implies that the
     * option is disabled (i.e., timeout of infinity).
     * @return the SO_TIMEOUT value
     * @exception IOException if an I/O error occurs
     * @since   JDK1.1
     * @see #setSoTimeout(int)
     */
    public synchronized int getSoTimeout() throws IOException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        Object o = getImpl().getOption(SocketOptions.SO_TIMEOUT);
        /* extra type safety */
        if (o instanceof Integer) {
            return ((Integer) o).intValue();
        } else {
            return 0;
        }
    }

    /**
     * Enable/disable the SO_REUSEADDR socket option.
     * <p>
     * When a TCP connection is closed the connection may remain
     * in a timeout state for a period of time after the connection
     * is closed (typically known as the <tt>TIME_WAIT</tt> state
     * or <tt>2MSL</tt> wait state).
     * For applications using a well known socket address or port
     * it may not be possible to bind a socket to the required
     * <tt>SocketAddress</tt> if there is a connection in the
     * timeout state involving the socket address or port.
     * <p>
     * Enabling <tt>SO_REUSEADDR</tt> prior to binding the socket
     * using {@link #bind(SocketAddress)} allows the socket to be
     * bound even though a previous connection is in a timeout
     * state.
     * <p>
     * When a <tt>ServerSocket</tt> is created the initial setting
     * of <tt>SO_REUSEADDR</tt> is not defined. Applications can
     * use {@link #getReuseAddress()} to determine the initial
     * setting of <tt>SO_REUSEADDR</tt>.
     * <p>
     * The behaviour when <tt>SO_REUSEADDR</tt> is enabled or
     * disabled after a socket is bound (See {@link #isBound()})
     * is not defined.
     *
     * @param on  whether to enable or disable the socket option
     * @exception SocketException if an error occurs enabling or
     *            disabling the <tt>SO_RESUEADDR</tt> socket option,
     *            or the socket is closed.
     * @since 1.4
     * @see #getReuseAddress()
     * @see #bind(SocketAddress)
     * @see #isBound()
     * @see #isClosed()
     */
    public void setReuseAddress(boolean on) throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_REUSEADDR, Boolean.valueOf(on));
    }

    /**
     * Tests if SO_REUSEADDR is enabled.
     *
     * @return a <code>boolean</code> indicating whether or not SO_REUSEADDR is enabled.
     * @exception SocketException if there is an error
     * in the underlying protocol, such as a TCP error.
     * @since   1.4
     * @see #setReuseAddress(boolean)
     */
    public boolean getReuseAddress() throws SocketException {
        if (isClosed())
            throw new SocketException("Socket is closed");
        return ((Boolean) (getImpl().getOption(SocketOptions.SO_REUSEADDR))).booleanValue();
    }

    /**
     * Returns the implementation address and implementation port of
     * this socket as a <code>String</code>.
     *
     * @return  a string representation of this socket.
     */
    public String toString() {
        if (!isBound())
            return "ServerSocket[unbound]";
        return "ServerSocket[addr=" + impl.getInetAddress() +
                ",port=" + impl.getPort() +
                ",localport=" + impl.getLocalPort()  + "]";
    }

    void setBound() {
        bound = true;
    }

    void setCreated() {
        created = true;
    }

    /**
     * The factory for all server sockets.
     */
    private static SocketImplFactory factory = null;

    /**
     * Sets the server socket implementation factory for the
     * application. The factory can be specified only once.
     * <p>
     * When an application creates a new server socket, the socket
     * implementation factory's <code>createSocketImpl</code> method is
     * called to create the actual socket implementation.
     * <p>
     * Passing <code>null</code> to the method is a no-op unless the factory
     * was already set.
     * <p>
     * If there is a security manager, this method first calls
     * the security manager's <code>checkSetFactory</code> method
     * to ensure the operation is allowed.
     * This could result in a SecurityException.
     *
     * @param      fac   the desired factory.
     * @exception  IOException  if an I/O error occurs when setting the
     *               socket factory.
     * @exception  SocketException  if the factory has already been defined.
     * @exception  SecurityException  if a security manager exists and its
     *             <code>checkSetFactory</code> method doesn't allow the operation.
     * @see        java.net.SocketImplFactory#createSocketImpl()
     * @see        SecurityManager#checkSetFactory
     */
    public static synchronized void setSocketFactory(SocketImplFactory fac) throws IOException {
        if (factory != null) {
            throw new SocketException("factory already defined");
        }
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkSetFactory();
        }
        factory = fac;
    }

    /**
     * Sets a default proposed value for the SO_RCVBUF option for sockets
     * accepted from this <tt>ServerSocket</tt>. The value actually set
     * in the accepted socket must be determined by calling
     * {@link Socket#getReceiveBufferSize()} after the socket
     * is returned by {@link #accept()}.
     * <p>
     * The value of SO_RCVBUF is used both to set the size of the internal
     * socket receive buffer, and to set the size of the TCP receive window
     * that is advertized to the remote peer.
     * <p>
     * It is possible to change the value subsequently, by calling
     * {@link Socket#setReceiveBufferSize(int)}. However, if the application
     * wishes to allow a receive window larger than 64K bytes, as defined by RFC1323
     * then the proposed value must be set in the ServerSocket <B>before</B>
     * it is bound to a local address. This implies, that the ServerSocket must be
     * created with the no-argument constructor, then setReceiveBufferSize() must
     * be called and lastly the ServerSocket is bound to an address by calling bind().
     * <p>
     * Failure to do this will not cause an error, and the buffer size may be set to the
     * requested value but the TCP receive window in sockets accepted from
     * this ServerSocket will be no larger than 64K bytes.
     *
     * @exception SocketException if there is an error
     * in the underlying protocol, such as a TCP error.
     *
     * @param size the size to which to set the receive buffer
     * size. This value must be greater than 0.
     *
     * @exception IllegalArgumentException if the
     * value is 0 or is negative.
     *
     * @since 1.4
     * @see #getReceiveBufferSize
     */
     public synchronized void setReceiveBufferSize (int size) throws SocketException {
        if (!(size > 0)) {
            throw new IllegalArgumentException("negative receive size");
        }
        if (isClosed())
            throw new SocketException("Socket is closed");
        getImpl().setOption(SocketOptions.SO_RCVBUF, new Integer(size));
    }

    /**
     * Gets the value of the SO_RCVBUF option for this <tt>ServerSocket</tt>,
     * that is the proposed buffer size that will be used for Sockets accepted
     * from this <tt>ServerSocket</tt>.
     *
     * <p>Note, the value actually set in the accepted socket is determined by
     * calling {@link Socket#getReceiveBufferSize()}.
     * @return the value of the SO_RCVBUF option for this <tt>Socket</tt>.
     * @exception SocketException if there is an error
     * in the underlying protocol, such as a TCP error.
     * @see #setReceiveBufferSize(int)
     * @since 1.4
     */
    public synchronized int getReceiveBufferSize()
    throws SocketException{
        if (isClosed())
            throw new SocketException("Socket is closed");
        int result = 0;
        Object o = getImpl().getOption(SocketOptions.SO_RCVBUF);
        if (o instanceof Integer) {
            result = ((Integer)o).intValue();
        }
        return result;
    }

    /**
     * Sets performance preferences for this ServerSocket.
     *
     * <p> Sockets use the TCP/IP protocol by default.  Some implementations
     * may offer alternative protocols which have different performance
     * characteristics than TCP/IP.  This method allows the application to
     * express its own preferences as to how these tradeoffs should be made
     * when the implementation chooses from the available protocols.
     *
     * <p> Performance preferences are described by three integers
     * whose values indicate the relative importance of short connection time,
     * low latency, and high bandwidth.  The absolute values of the integers
     * are irrelevant; in order to choose a protocol the values are simply
     * compared, with larger values indicating stronger preferences.  If the
     * application prefers short connection time over both low latency and high
     * bandwidth, for example, then it could invoke this method with the values
     * <tt>(1, 0, 0)</tt>.  If the application prefers high bandwidth above low
     * latency, and low latency above short connection time, then it could
     * invoke this method with the values <tt>(0, 1, 2)</tt>.
     *
     * <p> Invoking this method after this socket has been bound
     * will have no effect. This implies that in order to use this capability
     * requires the socket to be created with the no-argument constructor.
     *
     * @param  connectionTime
     *         An <tt>int</tt> expressing the relative importance of a short
     *         connection time
     *
     * @param  latency
     *         An <tt>int</tt> expressing the relative importance of low
     *         latency
     *
     * @param  bandwidth
     *         An <tt>int</tt> expressing the relative importance of high
     *         bandwidth
     *
     * @since 1.5
     */
    public void setPerformancePreferences(int connectionTime,
                                          int latency,
                                          int bandwidth)
    {
        /* Not implemented yet */
    }

}
