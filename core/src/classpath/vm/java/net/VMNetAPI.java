/*
 * $Id$
 */
package java.net;

import java.util.Collection;
import java.util.List;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface VMNetAPI {

    /**
     * Gets a network device by its name, or null if not found.
     * 
     * @param name
     * @return
     */
    public VMNetDevice getByName(String name);

    /**
     * Create an list of all InetAddresses of the given device.
     * 
     * @param netDevice
     * @return List of InetAddress instances.
     */
    public List getInetAddresses(VMNetDevice netDevice);

    /**
     * Return a network device by its address
     * 
     * @param addr
     *            The address of the interface to return
     * 
     * @exception SocketException
     *                If an error occurs
     * @exception NullPointerException
     *                If the specified addess is null
     */
    public VMNetDevice getByInetAddress(InetAddress addr)
            throws SocketException;

    /**
     * Gets all net devices.
     * 
     * @return A list of VMNetDevice instances.
     */
    public Collection getNetDevices();

    /**
     * Gets the default local address.
     * 
     * @return InetAddress
     */
    public InetAddress getLocalAddress() throws UnknownHostException;

    public InetAddress[] getHostByName(String hostname)
            throws UnknownHostException;
}
