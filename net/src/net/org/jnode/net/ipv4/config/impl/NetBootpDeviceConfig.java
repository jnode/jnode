/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import java.io.IOException;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.net.ipv4.bootp.BOOTPClient;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetBootpDeviceConfig extends NetDeviceConfig {

    /**
     * @see org.jnode.net.ipv4.config.impl.NetDeviceConfig#apply(org.jnode.driver.Device)
     */
    public void doApply(Device device) throws NetworkException {
        final BOOTPClient bootp = new BOOTPClient();
        try {
            bootp.configureDevice(device);
        } catch (IOException ex) {
            throw new NetworkException(ex);
        }
    }

}
