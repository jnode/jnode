/*
 * $Id$
 */
package org.jnode.driver.net.prism2;

import org.jnode.driver.DriverException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.TimeoutException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Prism2Core extends AbstractDeviceCore {

    private final Prism2Driver driver;

    private final Prism2Flags flags;

    public Prism2Core(Prism2Driver driver, ResourceOwner owner,
            PCIDevice device, Flags flags) throws DriverException,
            ResourceNotFreeException {
        if (!(flags instanceof Prism2Flags))
            throw new DriverException("Wrong flags to the Prism2 driver");

        this.driver = driver;
        this.flags = (Prism2Flags) flags;

        log.info("Found " + flags.getName());
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#disable()
     */
    public void disable() {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#getHwAddress()
     */
    public HardwareAddress getHwAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#initialize()
     */
    public void initialize() {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#release()
     */
    public void release() {
        // TODO Auto-generated method stub

    }

    /**
     * @see org.jnode.driver.net.spi.AbstractDeviceCore#transmit(org.jnode.net.SocketBuffer,
     *      long)
     */
    public void transmit(SocketBuffer buf, long timeout)
            throws InterruptedException, TimeoutException {
        // TODO Auto-generated method stub

    }
}
