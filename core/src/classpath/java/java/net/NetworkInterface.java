/* NetworkInterface.java
   Copyright (C) 2002 Free Software Foundation, Inc.

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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.jnode.driver.Device;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;

/**
 * @author Michael Koch <konqueror@gmx.de>
 * @author E. Prangsma, connection to JNode
 * @since 1.4
 */
public final class NetworkInterface {

	private final Device device;

	/**
	 * Create a new instance
	 * @param device
	 */
	private NetworkInterface(Device device) {
		this.device = device;
	}

	/**
	 *  Returns the name of the network interface
	 */
	public String getName() {
		return device.getId();
	}

	/**
	 *  Returns all available addresses of the network interface
	 *  
	 *  If a @see SecurityManager is available all addresses are checked
	 *  with @see SecurityManager::checkConnect() if they are available.
	 *  Only InetAddresses are returned where the security manager doesn't
	 *  thrown an exception.
	 *  
	 *  @return An enumeration of all addresses.
	 */
	public Enumeration getInetAddresses() {
		final Vector list = new Vector(VMNetUtils.getAPI().getInetAddresses(device));
		return list.elements();
	}

	/**
	 *  Returns the display name of the interface
	 */
	public String getDisplayName() {
		return device.getId();
	}

	/**
	 *  Returns an network interface by name
	 *
	 *  @param name The name of the interface to return
	 *
	 *  @exception SocketException If an error occurs
	 *  @exception NullPointerException If the specified name is null
	 */
	public static NetworkInterface getByName(String name)
	throws SocketException {
		try {
			final Device dev = DeviceUtils.getDevice(name);
			if (VMNetUtils.getAPI().isNetDevice(dev)) {
				return new NetworkInterface(dev);
			} else {
				throw new SocketException("Device " + name + " is not a network device");
			}
		} catch (DeviceNotFoundException ex) {
			throw new SocketException("No network interface with this name exists");
		}
	}

	/**
	 *  Return a network interface by its address
	 *
	 *  @param addr The address of the interface to return
	 *
	 *  @exception SocketException If an error occurs
	 *  @exception NullPointerException If the specified addess is null
	 */
	public static NetworkInterface getByInetAddress(InetAddress addr)
	throws SocketException {
		return new NetworkInterface(VMNetUtils.getAPI().getByInetAddress(addr));
	}

	/**
	 *  Return an Enumeration of all available network interfaces
	 *
	 *  @exception SocketException If an error occurs
	 */
	public static Enumeration getNetworkInterfaces() throws SocketException {
		
		final Vector list = new Vector();
		
		for (Iterator i = VMNetUtils.getAPI().getNetDevices().iterator(); i.hasNext(); ) {
			final Device dev = (Device)i.next();
			list.add(new NetworkInterface(dev));
		}

		return list.elements();
	}

	/**
	 *  Checks if the current instance is equal to obj
	 *
	 *  @param obj The object to compare with
	 */
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof NetworkInterface)) {
			return false;
		}

		final NetworkInterface tmp = (NetworkInterface)obj;
		return (this.device == tmp.device);
	}

	/**
	 *  Returns the hashcode of the current instance
	 */
	public int hashCode() {
		return device.getId().hashCode();
	}

	/**
	 *  Returns a string representation of the interface
	 */
	public String toString() {
		// FIXME: check if this is correct
		String result;
		String separator = System.getProperty("line.separator");

		result =
			"name: "
				+ getDisplayName()
				+ " ("
				+ getName()
				+ ") addresses:"
				+ separator;

		for (Enumeration e = getInetAddresses(); e.hasMoreElements();) {
			InetAddress address = (InetAddress)e.nextElement();
			result += address.toString() + separator;
		}

		return result;
	}
} // class NetworkInterface
