/* MulticastSocket.java -- Class for using multicast sockets
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003
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
import java.util.Enumeration;

/**
 * Written using on-line Java Platform 1.2 API Specification, as well
 * as "The Java Class Libraries", 2nd edition (Addison-Wesley, 1998).
 * Status:  Believed complete and correct.
 */

/**
 * This class models a multicast UDP socket.  A multicast address is a
 * class D internet address (one whose most significant bits are 1110).  
 * A multicast group consists of a multicast address and a well known
 * port number.  All members of the group listening on that address and
 * port will receive all the broadcasts to the group.
 * <p>
 * Please note that applets are not allowed to use multicast sockets 
 * 
 * Written using on-line Java Platform 1.2 API Specification, as well
 * as "The Java Class Libraries", 2nd edition (Addison-Wesley, 1998).
 * Status:  Believed complete and correct.
 *
 * @author Warren Levy <warrenl@cygnus.com>
 * @author Aaron M. Renn (arenn@urbanophile.com) (Documentation comments)
 * @since 1.1
 * @date May 18, 1999.
 */
public class MulticastSocket extends DatagramSocket {
	// FIXME: the local addr bound to the multicast socket can be reused;
	// unlike unicast sockets.  It binds to any available network interface.
	// See p.1159 JCL book.

	/**
	 * Create a MulticastSocket that this not bound to any address
	 *
	 * @exception IOException If an error occurs
	 * @exception SecurityException If a security manager exists and its
	 * checkListen method doesn't allow the operation
	 */
	public MulticastSocket() throws IOException {
		super(0, null);
	}

	/**
	 * Create a multicast socket bound to the specified port
	 *
	 * @param port The port to bind to
	 *
	 * @exception IOException If an error occurs
	 * @exception SecurityException If a security manager exists and its
	 * checkListen method doesn't allow the operation
	 */
	public MulticastSocket(int port) throws IOException {
		super(port, null);
	}

	/**
	 * Create a multicast socket bound to the specified SocketAddress.
	 *
	 * @param address The SocketAddress the multicast socket will be bound to
	 *
	 * @exception IOException If an error occurs
	 * @exception SecurityException If a security manager exists and its
	 * checkListen method doesn't allow the operation
	 *
	 * @since 1.4
	 */
	public MulticastSocket(SocketAddress address) throws IOException {
		super(address);
	}

	/**
	 * Returns the interface being used for multicast packets
	 * 
	 * @return The multicast interface
	 *
	 * @exception SocketException If an error occurs
	 */
	public InetAddress getInterface() throws SocketException {
		return (InetAddress)impl.getOption(SocketOptions.IP_MULTICAST_IF);
	}

	/**
	 * Returns the current value of the "Time to Live" option.  This is the
	 * number of hops a packet can make before it "expires".   This method id
	 * deprecated.  Use <code>getTimeToLive</code> instead.
	 * 
	 * @return The TTL value
	 *
	 * @exception IOException If an error occurs
	 *
	 * @deprecated 1.2 Replaced by getTimeToLive()
	 *
	 * @see Multicastsocket:getTimeToLive
	 */
	public byte getTTL() throws IOException {
		// Use getTTL here rather than getTimeToLive in case we're using an impl
		// other than the default PlainDatagramSocketImpl and it doesn't have
		// getTimeToLive yet.
		return impl.getTTL();
	}

	/**
	 * Returns the current value of the "Time to Live" option.  This is the
	 * number of hops a packet can make before it "expires". 
	 * 
	 * @return The TTL value
	 *
	 * @exception IOException If an error occurs
	 *
	 * @since 1.2
	 */
	public int getTimeToLive() throws IOException {
		return impl.getTimeToLive();
	}

	/**
	 * Sets the interface to use for sending multicast packets.
	 *
	 * @param addr The new interface to use.
	 *
	 * @exception SocketException If an error occurs.
	 *
	 * @since 1.4
	 */
	public void setInterface(InetAddress addr) throws SocketException {
		impl.setOption(SocketOptions.IP_MULTICAST_IF, addr);
	}

	/**
	 * Sets the local network interface used to send multicast messages
	 *
	 * @param netIF The local network interface used to send multicast messages
	 * 
	 * @exception SocketException If an error occurs
	 * 
	 * @see MulticastSocket:getNetworkInterface
	 * 
	 * @since 1.4
	 */
	public void setNetworkInterface(NetworkInterface netIf)
		throws SocketException {
		if (impl == null) {
			throw new SocketException("MulticastSocket: Cant access socket implementation");
		}

		if (impl instanceof ExSocketOptions) {
			impl.setOption(ExSocketOptions.SO_TRANSMIT_IF, netIf);
		} else {
			Enumeration e = netIf.getInetAddresses();

			if (!e.hasMoreElements())
				throw new SocketException("MulticastSocket: Error");

			InetAddress address = (InetAddress)e.nextElement();
			impl.setOption(SocketOptions.IP_MULTICAST_IF, address);
		}
	}

	/**
	 * Gets the local network interface which is used to send multicast messages
	 *
	 * @return The local network interface to send multicast messages
	 *
	 * @exception SocketException If an error occurs
	 *
	 * @see MulticastSocket:setNetworkInterface
	 * 
	 * @since 1.4
	 */
	public NetworkInterface getNetworkInterface() throws SocketException {
		if (impl == null) {
			throw new SocketException("MulticastSocket: Cant access socket implementation");
		}

		if (impl instanceof ExSocketOptions) {
			return (NetworkInterface)impl.getOption(ExSocketOptions.SO_TRANSMIT_IF);
		} else {
			InetAddress address = (InetAddress)impl.getOption(SocketOptions.IP_MULTICAST_IF);
			NetworkInterface netIf = NetworkInterface.getByInetAddress(address);
			return netIf;
		}
	}

	/**
	 * Disable/Enable local loopback of multicast packets.  The option is used by
	 * the platform's networking code as a hint for setting whether multicast
	 * data will be looped back to the local socket. 
	 *
	 * Because this option is a hint, applications that want to verify what
	 * loopback mode is set to should call #getLoopbackMode
	 *
	 * @param disable True to disable loopback mode
	 *
	 * @exception SocketException If an error occurs
	 *
	 * @since 1.4
	 */
	public void setLoopbackMode(boolean disable) throws SocketException {
		if (impl == null)
			throw new SocketException("MulticastSocket: Cant access socket implementation");

		impl.setOption(SocketOptions.IP_MULTICAST_LOOP, new Boolean(disable));
	}

	/**
	 * Checks if local loopback mode is enabled or not
	 *
	 * @exception SocketException If an error occurs
	 *
	 * @since 1.4
	 */
	public boolean getLoopbackMode() throws SocketException {
		Object obj = impl.getOption(SocketOptions.IP_MULTICAST_LOOP);

		if (obj instanceof Boolean)
			return ((Boolean)obj).booleanValue();
		else
			throw new SocketException("Unexpected type");
	}

	/**
	 * Sets the "Time to Live" value for a socket.  The value must be between
	 * 1 and 255.
	 *
	 * @param ttl The new TTL value
	 *
	 * @exception IOException If an error occurs
	 *
	 * @deprecated 1.2 Replaced by <code>setTimeToLive</code>
	 *
	 * @see MulticastSocket:setTimeToLive
	 */
	public void setTTL(byte ttl) throws IOException {
		// Use setTTL here rather than setTimeToLive in case we're using an impl
		// other than the default PlainDatagramSocketImpl and it doesn't have
		// setTimeToLive yet.
		impl.setTTL(ttl);
	}

	/**
	 * Sets the "Time to Live" value for a socket.  The value must be between
	 * 1 and 255.  
	 *
	 * @param ttl The new TTL value
	 *
	 * @exception IOException If an error occurs
	 * 
	 * @since 1.2
	 */
	public void setTimeToLive(int ttl) throws IOException {
		if (ttl <= 0 || ttl > 255)
			throw new IllegalArgumentException("Invalid ttl: " + ttl);

		impl.setTimeToLive(ttl);
	}

	/**
	 * Joins the specified mulitcast group.
	 *
	 * @param addr The address of the group to join
	 * 
	 * @exception IOException If an error occurs
	 * @exception SecurityException If a security manager exists and its
	 * checkMulticast method doesn't allow the operation
	 */
	public void joinGroup(InetAddress mcastaddr) throws IOException {
		if (!mcastaddr.isMulticastAddress())
			throw new IOException("Not a Multicast address");

		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkMulticast(mcastaddr);

		impl.join(mcastaddr);
	}

	/**
	 * Leaves the specified multicast group
	 *
	 * @param addr The address of the group to leave
	 *
	 * @exception IOException If an error occurs
	 * @exception SecurityException If a security manager exists and its
	 * checkMulticast method doesn't allow the operation
	 */
	public void leaveGroup(InetAddress mcastaddr) throws IOException {
		if (!mcastaddr.isMulticastAddress())
			throw new IOException("Not a Multicast address");

		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkMulticast(mcastaddr);

		impl.leave(mcastaddr);
	}

	/**
	 * Joins the specified mulitcast group on a specified interface.
	 *
	 * @param mcastaddr The multicast address to join
	 * @param netIf The local network interface to receive the multicast
	 * messages on or null to defer the interface set by #setInterface or
	 * #setNetworkInterface
	 * 
	 * @exception IOException If an error occurs
	 * @exception IllegalArgumentException If address type is not supported
	 * @exception SecurityException If a security manager exists and its
	 * checkMulticast method doesn't allow the operation
	 *
	 * @see MulticastSocket:setInterface
	 * @see MulticastSocket:setNetworkInterface
	 *
	 * @since 1.4
	 */
	public void joinGroup(SocketAddress mcastaddr, NetworkInterface netIf)
		throws IOException {
		if (!(mcastaddr instanceof InetSocketAddress))
			throw new IllegalArgumentException("SocketAddress type not supported");

		InetSocketAddress tmp = (InetSocketAddress)mcastaddr;

		if (!tmp.getAddress().isMulticastAddress())
			throw new IOException("Not a Multicast address");

		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkMulticast(tmp.getAddress());

		impl.joinGroup(mcastaddr, netIf);
	}

	/**
	 * Leaves the specified mulitcast group on a specified interface.
	 *
	 * @param mcastaddr The multicast address to leave
	 * @param netIf The local networki interface or null to defer to the
	 * interface set by setInterface or setNetworkInterface 
	 *
	 * @exception IOException If an error occurs
	 * @exception IllegalArgumentException If address type is not supported
	 * @exception SecurityException If a security manager exists and its
	 * checkMulticast method doesn't allow the operation
	 *
	 * @see MulticastSocket:setInterface
	 * @see MulticastSocket:setNetworkInterface
	 *
	 * @since 1.4
	 */
	public void leaveGroup(SocketAddress mcastaddr, NetworkInterface netIf)
		throws IOException {
		InetSocketAddress tmp = (InetSocketAddress)mcastaddr;

		if (!tmp.getAddress().isMulticastAddress())
			throw new IOException("Not a Multicast address");

		SecurityManager s = System.getSecurityManager();
		if (s != null)
			s.checkMulticast(tmp.getAddress());

		impl.leaveGroup(mcastaddr, netIf);
	}

	/**
	 * Sends a packet of data to a multicast address with a TTL that is
	 * different from the default TTL on this socket.  The default TTL for
	 * the socket is not changed.
	 *
	 * @param packet The packet of data to send
	 * @param ttl The TTL for this packet
	 *
	 * @exception IOException If an error occurs
	 * @exception SecurityException If a security manager exists and its
	 * checkConnect or checkMulticast method doesn't allow the operation
	 */
	public synchronized void send(DatagramPacket p, byte ttl)
		throws IOException {
		SecurityManager s = System.getSecurityManager();
		if (s != null) {
			InetAddress addr = p.getAddress();
			if (addr.isMulticastAddress())
				s.checkMulticast(addr, ttl);
			else
				s.checkConnect(addr.getHostAddress(), p.getPort());
		}

		int oldttl = impl.getTimeToLive();
		impl.setTimeToLive(((int)ttl) & 0xFF);
		impl.send(p);
		impl.setTimeToLive(oldttl);
	}
} // class MulticastSocket
