/* DatagramSocket.java -- A class to model UDP sockets
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
import java.nio.channels.DatagramChannel;

/**
 * Written using on-line Java Platform 1.2 API Specification, as well
 * as "The Java Class Libraries", 2nd edition (Addison-Wesley, 1998).
 * Status:  Believed complete and correct.
 */

/**
 * This class models a connectionless datagram socket that sends 
 * individual packets of data across the network.  In the TCP/IP world,
 * this means UDP.  Datagram packets do not have guaranteed delivery,
 * or any guarantee about the order the data will be received on the
 * remote host.
 * 
 * @author Aaron M. Renn (arenn@urbanophile.com)
 * @author Warren Levy (warrenl@cygnus.com)
 * @author E.W. Prangsma (connection to JNode)
 * 
 * @date May 3, 1999.
 */

public class DatagramSocket {
	/**
	 * This is the user DatagramSocketImplFactory for this class.  If this
	 * variable is null, a default factory is used.
	 */
	static DatagramSocketImplFactory factory;
	static DatagramSocketImplFactory systemFactory;

	/**
	 * This is the implementation object used by this socket.
	 */
	final DatagramSocketImpl impl;

	/**
	 * The unique DatagramChannel object associated with this datagram socket,
	 * or null.
	 */
	DatagramChannel ch;

	/**
	 * This is the local address which cannot be changed
	 */
	private InetAddress local_addr;

	/**
	 * This is the address we are "connected" to
	 */
	private InetAddress remoteAddress;

	/**
	 * This is the port we are "connected" to
	 */
	private int remotePort = -1;

	/**
	 * Is this a "connected" datagram socket?
	 */
	private boolean connected = false;

	/**
	 * Initializes a new instance of <code>DatagramSocket</code> that binds to 
	 * a random port and every address on the local machine.
	 *
	 * @exception SocketException If an error occurs.
	 * @exception SecurityException If a security manager exists and
	 * its checkListen method doesn't allow the operation.
	 */
	public DatagramSocket() throws SocketException {
		this(0, null);
	}

	/**
	 * Initializes a new instance of <code>DatagramSocket</code> that binds to 
	 * the specified port and every address on the local machine.
	 *
	 * @param port The local port number to bind to.
	 *
	 * @exception SecurityException If a security manager exists and its
	 * checkListen method doesn't allow the operation.
	 * @exception SocketException If an error occurs.
	 */
	public DatagramSocket(int port) throws SocketException {
		this(port, null);
	}

	/**
	 * Initializes a new instance of <code>DatagramSocket</code> that binds to 
	 * the specified local port and address.
	 *
	 * @param port The local port number to bind to.
	 * @param laddr The local address to bind to.
	 *
	 * @exception SecurityException If a security manager exists and its
	 * checkListen method doesn't allow the operation.
	 * @exception SocketException If an error occurs.
	 */
	public DatagramSocket(int port, InetAddress laddr) throws SocketException {
		this(
			new InetSocketAddress(
				laddr != null ? laddr : InetAddress.ANY_IF,
				port));
	}

	/**
	 * Initializes a new instance of <code>DatagramSocket</code> that binds to 
	 * the specified local port and address.
	 *
	 * @param port The local port number to bind to.
	 * @param laddr The local address to bind to.
	 *
	 * @exception SecurityException If a security manager exists and its
	 * checkListen method doesn't allow the operation.
	 * @exception SocketException If an error occurs.
	 * 
	 * @since 1.4
	 */
	public DatagramSocket(SocketAddress address) throws SocketException {
		InetSocketAddress tmp = (InetSocketAddress)address;

		SecurityManager s = System.getSecurityManager();
		if (s != null) {
			s.checkListen(tmp.getPort());
		}

		if (factory != null) {
			impl = factory.createDatagramSocketImpl();
		} else if (systemFactory != null) {
			impl = systemFactory.createDatagramSocketImpl();
		} else {
			throw new SocketException("No DatagramSocketFactory set");
		}
		impl.create();

		if (address != null) {
			local_addr = tmp.getAddress();
			impl.bind(tmp.getPort(), tmp.getAddress());
		}
	}

	/**
	 * Closes this datagram socket.
	 */
	public void close() {
		impl.close();
	}

	/**
	 * This method returns the remote address to which this socket is 
	 * connected.  If this socket is not connected, then this method will
	 * return <code>null</code>.
	 *
	 * @return The remote address.
	 *
	 * @since 1.2
	 */
	public InetAddress getInetAddress() {
		return remoteAddress;
	}

	/**
	 * This method returns the remote port to which this socket is
	 * connected.  If this socket is not connected, then this method will
	 * return -1.
	 *
	 * @return The remote port.
	 *
	 * @since 1.2
	 */
	public int getPort() {
		return remotePort;
	}

	/**
	 * Returns the local address this datagram socket is bound to.
	 * 
	 * @since 1.1
	 */
	public InetAddress getLocalAddress() {
		if (impl == null)
			return null;

		// FIXME: According to libgcj, checkConnect() is supposed to be called
		// before performing this operation.  Problems: 1) We don't have the
		// addr until after we do it, so we do a post check.  2). The docs I
		// see don't require this in the Socket case, only DatagramSocket, but
		// we'll assume they mean both.
		SecurityManager sm = System.getSecurityManager();
		if (sm != null)
			sm.checkConnect(local_addr.getHostName(), getLocalPort());

		return local_addr;
	}

	/**
	 * Returns the local port this socket is bound to.
	 *
	 * @return The local port number.
	 */
	public int getLocalPort() {
		return impl.getLocalPort();
	}

	/**
	 * Returns the value of the socket's SO_TIMEOUT setting.  If this method
	 * returns 0 then SO_TIMEOUT is disabled.
	 *
	 * @return The current timeout in milliseconds.
	 *
	 * @exception SocketException If an error occurs.
	 * 
	 * @since 1.1
	 */
	public synchronized int getSoTimeout() throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		Object timeout = impl.getOption(SocketOptions.SO_TIMEOUT);

		if (timeout instanceof Integer)
			return ((Integer)timeout).intValue();
		else
			throw new SocketException("Internal Error");
	}

	/**
	 * Sets the value of the socket's SO_TIMEOUT value.  A value of 0 will
	 * disable SO_TIMEOUT.  Any other value is the number of milliseconds
	 * a socket read/write will block before timing out.
	 *
	 * @param timeout The new SO_TIMEOUT value in milliseconds.
	 *
	 * @exception SocketException If an error occurs.
	 *
	 * @since 1.1
	 */
	public void setSoTimeout(int timeout) throws SocketException {
		if (timeout < 0)
			throw new IllegalArgumentException("Timeout value is less than 0");

		impl.setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
	}

	/**
	 * This method returns the value of the system level socket option
	 * SO_SNDBUF, which is used by the operating system to tune buffer
	 * sizes for data transfers.
	 *
	 * @return The send buffer size.
	 *
	 * @exception SocketException If an error occurs.
	 *
	 * @since 1.2
	 */
	public int getSendBufferSize() throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		Object obj = impl.getOption(SocketOptions.SO_SNDBUF);

		if ((obj != null) && (obj instanceof Integer))
			return (((Integer)obj).intValue());
		else
			throw new SocketException("Unexpected type");
	}

	/**
	 * This method sets the value for the system level socket option
	 * SO_SNDBUF to the specified value.  Note that valid values for this
	 * option are specific to a given operating system.
	 *
	 * @param size The new send buffer size.
	 *
	 * @exception SocketException If an error occurs.
	 * @exception IllegalArgumentException If size is 0 or negative.
	 *
	 * @since 1.2
	 */
	public void setSendBufferSize(int size) throws SocketException {
		if (size < 0)
			throw new IllegalArgumentException("Buffer size is less than 0");

		impl.setOption(SocketOptions.SO_SNDBUF, new Integer(size));
	}

	/**
	 * This method returns the value of the system level socket option
	 * SO_RCVBUF, which is used by the operating system to tune buffer
	 * sizes for data transfers.
	 *
	 * @return The receive buffer size.
	 *
	 * @exception SocketException If an error occurs.
	 *
	 * @since 1.2
	 */
	public int getReceiveBufferSize() throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		Object obj = impl.getOption(SocketOptions.SO_RCVBUF);

		if (obj instanceof Integer)
			return (((Integer)obj).intValue());
		else
			throw new SocketException("Unexpected type");
	}

	/**
	 * This method sets the value for the system level socket option
	 * SO_RCVBUF to the specified value.  Note that valid values for this
	 * option are specific to a given operating system.
	 *
	 * @param size The new receive buffer size.
	 *
	 * @exception SocketException If an error occurs.
	 * @exception IllegalArgumentException If size is 0 or negative.
	 *  
	 * @since 1.2
	 */
	public void setReceiveBufferSize(int size) throws SocketException {
		if (size < 0)
			throw new IllegalArgumentException("Buffer size is less than 0");

		impl.setOption(SocketOptions.SO_RCVBUF, new Integer(size));
	}

	/**
	 * This method connects this socket to the specified address and port.
	 * When a datagram socket is connected, it will only send or receive
	 * packate to and from the host to which it is connected.  A multicast
	 * socket that is connected may only send and not receive packets.
	 *
	 * @param addr The address to connect this socket to.
	 * @param port The port to connect this socket to.
	 *
	 * @exception SecurityException If connections to this addr/port are not
	 * allowed.
	 * @exception IllegalArgumentException If the addr or port are invalid.
	 *
	 * @since 1.2
	 */
	public void connect(InetAddress addr, int port)
		throws SecurityException, IllegalArgumentException {
		if (addr == null)
			throw new IllegalArgumentException("Connect address is null");

		if ((port < 1) || (port > 65535))
			throw new IllegalArgumentException("Bad port number: " + port);

		SecurityManager sm = System.getSecurityManager();
		if (sm != null)
			sm.checkConnect(addr.getHostName(), port);

		this.remoteAddress = addr;
		this.remotePort = port;

		/* FIXME: Shit, we can't do this even though the OS supports it since this 
		   method isn't in DatagramSocketImpl. */
		//  impl.connect(addr, port);

		connected = true;
	}

	/**
	 * This method disconnects this socket from the addr/port it was 
	 * connected to.  If the socket was not connected in the first place,
	 * this method does nothing.
	 *
	 * @since 1.2
	 */
	public void disconnect() {
		// FIXME: See my comments on connect()
		this.remoteAddress = null;
		this.remotePort = -1;
		connected = false;
	}

	/**
	 * Reads a datagram packet from the socket.  Note that this method
	 * will block until a packet is received from the network.  On return,
	 * the passed in <code>DatagramPacket</code> is populated with the data 
	 * received and all the other information about the packet.
	 *
	 * @param p A <code>DatagramPacket</code> for storing the data
	 *
	 * @exception IOException If an error occurs
	 */
	public synchronized void receive(DatagramPacket p) throws IOException {
		impl.receive(p);

		SecurityManager s = System.getSecurityManager();
		if (s != null && isConnected())
			s.checkAccept(p.getAddress().getHostName(), p.getPort());
	}

	/**
	 * Sends the specified packet.  The host and port to which the packet
	 * are to be sent should be set inside the packet.
	 *
	 * @param p The datagram packet to send.
	 *
	 * @exception IOException If an error occurs.
	 * @exception SecurityException If a security manager exists and its
	 * checkMulticast or checkConnect method doesn't allow the send.
	 * @exception PortUnreachableException If the socket is connected to a
	 * currently unreachable destination. Note, there is no guarantee that the
	 * exception will be thrown.
	 * @exception IllegalBlockingModeException If this socket has an associated
	 * channel, and the channel is in non-blocking mode.
	 */
	public void send(DatagramPacket p) throws IOException {
		if (!connected) {
			SecurityManager s = System.getSecurityManager();
			if (s != null) {
				InetAddress addr = p.getAddress();
				if (addr.isMulticastAddress())
					s.checkMulticast(addr);
				else
					s.checkConnect(addr.getHostAddress(), p.getPort());
			}
		} else {
			if (!p.getAddress().equals(remoteAddress)) {
				throw new SecurityException("Can only send to the connected remote address");
			}
			if (p.getPort() != remotePort) {
				throw new SecurityException("Can only send to the connected remote port");
			}
		}

		// FIXME: if this is a subclass of MulticastSocket, use getTTL for TTL val.
		impl.send(p);
	}

	/**
	 * Binds the socket to the given socket address.
	 *
	 * @param address The socket address to bind to.
	 *
	 * @exception SocketException If an error occurs.
	 * @exception SecurityException If a security manager exists and
	 * its checkListen method doesn't allow the operation.
	 * @exception IllegalArgumentException If address type is not supported.
	 *
	 * @since 1.4
	 */
	public void bind(SocketAddress address) throws SocketException {
		if (!(address instanceof InetSocketAddress))
			throw new IllegalArgumentException();

		InetSocketAddress tmp = (InetSocketAddress)address;

		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkListen(tmp.getPort());

		impl.bind(tmp.getPort(), tmp.getAddress());
	}

	/**
	 * Returns the datagram channel assoziated with this datagram socket.
	 * 
	 * @since 1.4
	 */
	public DatagramChannel getChannel() {
		return ch;
	}

	/**
	 * Connects the datagram socket to a specified socket address.
	 *
	 * @param address The socket address to connect to.
	 *
	 * @exception SocketException If an error occurs.
	 * @exception IllegalArgumentException If address type is not supported.
	 *
	 * @since 1.4
	 */
	public void connect(SocketAddress address) throws SocketException {
		if (!(address instanceof InetSocketAddress))
			throw new IllegalArgumentException("SocketAddress is not InetSocketAddress");

		InetSocketAddress tmp = (InetSocketAddress)address;
		connect(tmp.getAddress(), tmp.getPort());
	}

	/**
	 * Returns the binding state of the socket.
	 * 
	 * @since 1.4
	 */
	public boolean isBound() {
		try {
			Object bindaddr = impl.getOption(SocketOptions.SO_BINDADDR);
		} catch (SocketException e) {
			return false;
		}

		return true;
	}

	/**
	 * Returns the connection state of the socket.
	 * 
	 * @since 1.4
	 */
	public boolean isConnected() {
		return remoteAddress != null;
	}

	/**
	 * Returns the SocketAddress of the host this socket is conneted to
	 * or null if this socket is not connected.
	 * 
	 * @since 1.4
	 */
	public SocketAddress getRemoteSocketAddress() {
		if (!isConnected())
			return null;

		return new InetSocketAddress(remoteAddress, remotePort);
	}

	/**
	 * Returns the local SocketAddress this socket is bound to
	 * or null if it is not bound.
	 * 
	 * @since 1.4
	 */
	public SocketAddress getLocalSocketAddress() {
		InetAddress addr;

		try {
			addr = (InetAddress)impl.getOption(SocketOptions.SO_BINDADDR);
		} catch (SocketException e) {
			return null;
		}

		return new InetSocketAddress(local_addr, impl.localPort);
	}

	/**
	 * Enables/Disables SO_REUSEADDR.
	 * 
	 * @param on Whether or not to have SO_REUSEADDR turned on.
	 *
	 * @exception SocketException If an error occurs.
	 *
	 * @since 1.4
	 */
	public void setReuseAddress(boolean on) throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		impl.setOption(SocketOptions.SO_REUSEADDR, new Boolean(on));
	}

	/**
	 * Checks if SO_REUSEADDR is enabled.
	 *
	 * @exception SocketException If an error occurs.
	 * 
	 * @since 1.4
	 */
	public boolean getReuseAddress() throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		Object obj = impl.getOption(SocketOptions.SO_REUSEADDR);

		if (obj instanceof Boolean)
			return (((Boolean)obj).booleanValue());
		else
			throw new SocketException("Unexpected type");
	}

	/**
	 * Enables/Disables SO_BROADCAST
	 * 
	 * @param on Whether or not to have SO_BROADCAST turned on
	 *
	 * @exception SocketException If an error occurs
	 *
	 * @since 1.4
	 */
	public void setBroadcast(boolean on) throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		impl.setOption(SocketOptions.SO_BROADCAST, new Boolean(on));
	}

	/**
	 * Checks if SO_BROADCAST is enabled
	 * 
	 * @exception SocketException If an error occurs
	 * 
	 * @since 1.4
	 */
	public boolean getBroadcast() throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		Object obj = impl.getOption(SocketOptions.SO_BROADCAST);

		if (obj instanceof Boolean)
			return ((Boolean)obj).booleanValue();
		else
			throw new SocketException("Unexpected type");
	}

	/**
	 * Sets the traffic class value
	 *
	 * @param tc The traffic class
	 *
	 * @exception SocketException If an error occurs
	 * @exception IllegalArgumentException If tc value is illegal
	 *
	 * @see DatagramSocket:getTrafficClass
	 * 
	 * @since 1.4
	 */
	public void setTrafficClass(int tc) throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		if (tc < 0 || tc > 255)
			throw new IllegalArgumentException();

		impl.setOption(SocketOptions.IP_TOS, new Integer(tc));
	}

	/**
	 * Returns the current traffic class
	 * 
	 * @see DatagramSocket:setTrafficClass
	 *
	 * @exception SocketException If an error occurs
	 * 
	 * @since 1.4
	 */
	public int getTrafficClass() throws SocketException {
		if (impl == null)
			throw new SocketException("Cannot initialize Socket implementation");

		Object obj = impl.getOption(SocketOptions.IP_TOS);

		if (obj instanceof Integer)
			return ((Integer)obj).intValue();
		else
			throw new SocketException("Unexpected type");
	}

	/**
	 * Sets the datagram socket implementation factory for the application
	 *
	 * @param fac The factory to set
	 *
	 * @exception IOException If an error occurs
	 * @exception SocketException If the factory is already defined
	 * @exception SecurityException If a security manager exists and its
	 * checkSetFactory method doesn't allow the operation
	 */
	public static void setDatagramSocketImplFactory(DatagramSocketImplFactory fac)
		throws IOException {
		if (factory != null)
			throw new SocketException("DatagramSocketImplFactory already defined");

		SecurityManager sm = System.getSecurityManager();
		if (sm != null)
			sm.checkSetFactory();

		factory = fac;
	}

	/**
	 * Sets the datagram socket implementation factory for the application
	 *
	 * @param fac The factory to set
	 *
	 * @exception IOException If an error occurs
	 * @exception SocketException If the factory is already defined
	 * @exception SecurityException If a security manager exists and its
	 * checkSetFactory method doesn't allow the operation
	 */
	public static void setSystemDatagramSocketImplFactory(DatagramSocketImplFactory fac)
	throws IOException {
		if (systemFactory != null) {
			throw new SocketException("System wide DatagramSocketImplFactory already defined");
		}

		final SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			sm.checkSetFactory();
		}

		systemFactory = fac;
	}
} // class DatagramSocket
