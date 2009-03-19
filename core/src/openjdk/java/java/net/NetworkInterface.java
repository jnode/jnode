/*
 * Copyright 2000-2006 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.net.SocketException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import sun.security.action.*;
import java.security.AccessController;

/**
 * This class represents a Network Interface made up of a name,
 * and a list of IP addresses assigned to this interface.
 * It is used to identify the local interface on which a multicast group
 * is joined.
 *
 * Interfaces are normally known by names such as "le0".
 *
 * @since 1.4
 */
public final class NetworkInterface {
    private String name;
    private String displayName;
    private int index;
    private InetAddress addrs[];
    private InterfaceAddress bindings[];
    private NetworkInterface childs[];
    private NetworkInterface parent = null;
    private boolean virtual = false;

    static {
        AccessController.doPrivileged(new LoadLibraryAction("net"));
        init();
    }

    /**
     * Returns an NetworkInterface object with index set to 0 and name to null.
     * Setting such an interface on a MulticastSocket will cause the
     * kernel to choose one interface for sending multicast packets.
     *
     */
    NetworkInterface() {
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    /**
     * Get the name of this network interface.
     *
     * @return the name of this network interface
     */
    public String getName() {
            return name;
    }

    /**
     * Convenience method to return an Enumeration with all or a
     * subset of the InetAddresses bound to this network interface.
     * <p>
     * If there is a security manager, its <code>checkConnect</code>
     * method is called for each InetAddress. Only InetAddresses where
     * the <code>checkConnect</code> doesn't throw a SecurityException
     * will be returned in the Enumeration.
     * @return an Enumeration object with all or a subset of the InetAddresses
     * bound to this network interface
     */
    public Enumeration<InetAddress> getInetAddresses() {

        class checkedAddresses implements Enumeration<InetAddress> {

            private int i=0, count=0;
            private InetAddress local_addrs[];

            checkedAddresses() {
                local_addrs = new InetAddress[addrs.length];

                SecurityManager sec = System.getSecurityManager();
                for (int j=0; j<addrs.length; j++) {
                    try {
                        if (sec != null) {
                            sec.checkConnect(addrs[j].getHostAddress(), -1);
                        }
                        local_addrs[count++] = addrs[j];
                    } catch (SecurityException e) { }
                }

            }

            public InetAddress nextElement() {
                if (i < count) {
                    return local_addrs[i++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (i < count);
            }
        }
        return new checkedAddresses();

    }

    /**
     * Get a List of all or a subset of the <code>InterfaceAddresses</code>
     * of this network interface.
     * <p>
     * If there is a security manager, its <code>checkConnect</code>
     * method is called with the InetAddress for each InterfaceAddress.
     * Only InterfaceAddresses where the <code>checkConnect</code> doesn't throw
     * a SecurityException will be returned in the List.
     *
     * @return a <code>List</code> object with all or a subset of the
     *         InterfaceAddresss of this network interface
     * @since 1.6
     */
    public java.util.List<InterfaceAddress> getInterfaceAddresses() {
        java.util.List<InterfaceAddress> lst = new java.util.ArrayList<InterfaceAddress>(1);
        SecurityManager sec = System.getSecurityManager();
        for (int j=0; j<bindings.length; j++) {
            try {
                if (sec != null) {
                    sec.checkConnect(bindings[j].getAddress().getHostAddress(), -1);
                }
                lst.add(bindings[j]);
            } catch (SecurityException e) { }
        }
        return lst;
    }

    /**
     * Get an Enumeration with all the subinterfaces (also known as virtual
     * interfaces) attached to this network interface.
     * <p>
     * For instance eth0:1 will be a subinterface to eth0.
     *
     * @return an Enumeration object with all of the subinterfaces
     * of this network interface
     * @since 1.6
     */
    public Enumeration<NetworkInterface> getSubInterfaces() {
        class subIFs implements Enumeration<NetworkInterface> {

            private int i=0;

            subIFs() {
            }

            public NetworkInterface nextElement() {
                if (i < childs.length) {
                    return childs[i++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (i < childs.length);
            }
        }
        return new subIFs();

    }

    /**
     * Returns the parent NetworkInterface of this interface if this is
     * a subinterface, or <code>null</code> if it is a physical
     * (non virtual) interface or has no parent.
     *
     * @return The <code>NetworkInterface</code> this interface is attached to.
     * @since 1.6
     */
    public NetworkInterface getParent() {
        return parent;
    }

    /**
     * Get the index of this network interface.
     *
     * @return the index of this network interface
     */
    int getIndex() {
        return index;
    }

    /**
     * Get the display name of this network interface.
     * A display name is a human readable String describing the network
     * device.
     *
     * @return the display name of this network interface,
     *         or null if no display name is available.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Searches for the network interface with the specified name.
     *
     * @param   name
     *          The name of the network interface.
     *
     * @return  A <tt>NetworkInterface</tt> with the specified name,
     *          or <tt>null</tt> if there is no network interface
     *          with the specified name.
     *
     * @throws  SocketException
     *          If an I/O error occurs.
     *
     * @throws  NullPointerException
     *          If the specified name is <tt>null</tt>.
     */
    public static NetworkInterface getByName(String name) throws SocketException {
        if (name == null)
            throw new NullPointerException();
        return getByName0(name);
    }

    /**
     * Get a network interface given its index.
     *
     * @param index an integer, the index of the interface
     * @return the NetworkInterface obtained from its index
     * @exception  SocketException  if an I/O error occurs.
     */
    native static NetworkInterface getByIndex(int index)
        throws SocketException;

    /**
     * Convenience method to search for a network interface that
     * has the specified Internet Protocol (IP) address bound to
     * it.
     * <p>
     * If the specified IP address is bound to multiple network
     * interfaces it is not defined which network interface is
     * returned.
     *
     * @param   addr
     *          The <tt>InetAddress</tt> to search with.
     *
     * @return  A <tt>NetworkInterface</tt>
     *          or <tt>null</tt> if there is no network interface
     *          with the specified IP address.
     *
     * @throws  SocketException
     *          If an I/O error occurs.
     *
     * @throws  NullPointerException
     *          If the specified address is <tt>null</tt>.
     */
    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null)
            throw new NullPointerException();
        return getByInetAddress0(addr);
    }

    /**
     * Returns all the interfaces on this machine. Returns null if no
     * network interfaces could be found on this machine.
     *
     * NOTE: can use getNetworkInterfaces()+getInetAddresses()
     *       to obtain all IP addresses for this node
     *
     * @return an Enumeration of NetworkInterfaces found on this machine
     * @exception  SocketException  if an I/O error occurs.
     */

    public static Enumeration<NetworkInterface> getNetworkInterfaces()
        throws SocketException {
        final NetworkInterface[] netifs = getAll();

        // specified to return null if no network interfaces
        if (netifs == null)
            return null;

        return new Enumeration<NetworkInterface>() {
            private int i = 0;
            public NetworkInterface nextElement() {
                if (netifs != null && i < netifs.length) {
                    NetworkInterface netif = netifs[i++];
                    return netif;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (netifs != null && i < netifs.length);
            }
        };
    }

    private native static NetworkInterface[] getAll()
        throws SocketException;

    private native static NetworkInterface getByName0(String name)
        throws SocketException;

    private native static NetworkInterface getByInetAddress0(InetAddress addr)
        throws SocketException;

    /**
     * Returns whether a network interface is up and running.
     *
     * @return  <code>true</code> if the interface is up and running.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean isUp() throws SocketException {
        return isUp0(name, index);
    }

    /**
     * Returns whether a network interface is a loopback interface.
     *
     * @return  <code>true</code> if the interface is a loopback interface.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean isLoopback() throws SocketException {
        return isLoopback0(name, index);
    }

    /**
     * Returns whether a network interface is a point to point interface.
     * A typical point to point interface would be a PPP connection through
     * a modem.
     *
     * @return  <code>true</code> if the interface is a point to point
     *          interface.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean isPointToPoint() throws SocketException {
        return isP2P0(name, index);
    }

    /**
     * Returns whether a network interface supports multicasting or not.
     *
     * @return  <code>true</code> if the interface supports Multicasting.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean supportsMulticast() throws SocketException {
        return supportsMulticast0(name, index);
    }

    /**
     * Returns the hardware address (usually MAC) of the interface if it
     * has one and if it can be accessed given the current privileges.
     *
     * @return  a byte array containing the address or <code>null</code> if
     *          the address doesn't exist or is not accessible.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */
    public byte[] getHardwareAddress() throws SocketException {
        for (InetAddress addr : addrs) {
            if (addr instanceof Inet4Address) {
                return getMacAddr0(((Inet4Address)addr).getAddress(), name, index);
            }
        }
        return getMacAddr0(null, name, index);
    }

    /**
     * Returns the Maximum Transmission Unit (MTU) of this interface.
     *
     * @return the value of the MTU for that interface.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */
    public int getMTU() throws SocketException {
        return getMTU0(name, index);
    }

    /**
     * Returns whether this interface is a virtual interface (also called
     * subinterface).
     * Virtual interfaces are, on some systems, interfaces created as a child
     * of a physical interface and given different settings (like address or
     * MTU). Usually the name of the interface will the name of the parent
     * followed by a colon (:) and a number identifying the child since there
     * can be several virtual interfaces attached to a single physical
     * interface.
     *
     * @return <code>true</code> if this interface is a virtual interface.
     * @since 1.6
     */
    public boolean isVirtual() {
        return virtual;
    }

    private native static long getSubnet0(String name, int ind) throws SocketException;
    private native static Inet4Address getBroadcast0(String name, int ind) throws SocketException;
    private native static boolean isUp0(String name, int ind) throws SocketException;
    private native static boolean isLoopback0(String name, int ind) throws SocketException;
    private native static boolean supportsMulticast0(String name, int ind) throws SocketException;
    private native static boolean isP2P0(String name, int ind) throws SocketException;
    private native static byte[] getMacAddr0(byte[] inAddr, String name, int ind) throws SocketException;
    private native static int getMTU0(String name, int ind) throws SocketException;

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and it represents the same NetworkInterface
     * as this object.
     * <p>
     * Two instances of <code>NetworkInterface</code> represent the same
     * NetworkInterface if both name and addrs are the same for both.
     *
     * @param   obj   the object to compare against.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface netIF = (NetworkInterface)obj;
        if (name != null ) {
            if (netIF.getName() != null) {
                if (!name.equals(netIF.getName())) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (netIF.getName() != null) {
                return false;
            }
        }
        Enumeration newAddrs = netIF.getInetAddresses();
        int i = 0;
        for (i = 0; newAddrs.hasMoreElements();newAddrs.nextElement(), i++);
        if (addrs == null) {
            if (i != 0) {
                return false;
            }
        } else {
            /*
             * Compare number of addresses (in the checked subset)
             */
            int count = 0;
            Enumeration e = getInetAddresses();
            for (; e.hasMoreElements(); count++) {
                e.nextElement();
            }
            if (i != count) {
                return false;
            }
        }
        newAddrs = netIF.getInetAddresses();
        for (; newAddrs.hasMoreElements();) {
            boolean equal = false;
            Enumeration thisAddrs = getInetAddresses();
            InetAddress newAddr = (InetAddress)newAddrs.nextElement();
            for (; thisAddrs.hasMoreElements();) {
                InetAddress thisAddr = (InetAddress)thisAddrs.nextElement();
                if (thisAddr.equals(newAddr)) {
                    equal = true;
                }
            }
            if (!equal) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int count = 0;
        if (addrs != null) {
            for (int i = 0; i < addrs.length; i++) {
                count += addrs[i].hashCode();
            }
        }
        return count;
    }

    public String toString() {
        String result = "name:";
        result += name == null? "null": name;
        if (displayName != null) {
            result += " (" + displayName + ")";
        }
        result += " index: "+index+" addresses:\n";
        for (Enumeration e = getInetAddresses(); e.hasMoreElements(); ) {
            InetAddress addr = (InetAddress)e.nextElement();
            result += addr+";\n";
        }
        return result;
    }
    private static native void init();

}
