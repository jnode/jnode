/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class NetDeviceConfig {
    
    /**
     * Initialize this instance.
     */
    public NetDeviceConfig() {
    }

    /**
     * Apply this configuration for the device.
     */
    public final void apply(final Device device)
    throws NetworkException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws NetworkException {
                    doApply(device);
                    return null;
                    }});
        } catch (PrivilegedActionException ex) {
            throw (NetworkException)ex.getException();
        }
    }

    /**
     * Apply this configuration for the device.
     */
    protected abstract void doApply(Device device)
    throws NetworkException;    
}
