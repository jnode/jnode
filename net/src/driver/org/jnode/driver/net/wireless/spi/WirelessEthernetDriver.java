/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.driver.net.wireless.spi;

import java.io.PrintWriter;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.WirelessNetDeviceAPI;
import org.jnode.driver.net.ethernet.spi.BasicEthernetDriver;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.net.wireless.AuthenticationMode;
import org.jnode.system.ResourceNotFreeException;

/**
 * Base class for wireless ethernet drivers.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class WirelessEthernetDriver extends BasicEthernetDriver implements WirelessNetDeviceAPI {

    /**
     * Device prefix for ethernet devices
     */
    public static final String WLAN_DEVICE_PREFIX = "wlan";

    /**
     * @see org.jnode.driver.net.WirelessNetDeviceAPI#getAuthenticationMode()
     */
    public AuthenticationMode getAuthenticationMode() {
        try {
            return getWirelessCore().getAuthenticationMode();
        } catch (DriverException ex) {
            log.debug("Cannot read AuthenticationMode, defaulting to OPENSYSTEM", ex);
            return AuthenticationMode.OPENSYSTEM;
        }
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#getDevicePrefix()
     */
    protected final String getDevicePrefix() {
        return WLAN_DEVICE_PREFIX;
    }

    /**
     * @see org.jnode.driver.net.WirelessNetDeviceAPI#getESSID()
     */
    public String getESSID() {
        try {
            return getWirelessCore().getESSID();
        } catch (DriverException ex) {
            log.debug("Cannot read ESSID", ex);
            return null;
        }
    }

    /**
     * Gets the wireless device core.
     *
     * @return
     */
    protected final WirelessDeviceCore getWirelessCore() {
        return (WirelessDeviceCore) getDeviceCore();
    }

    /**
     * @see org.jnode.driver.net.ethernet.spi.BasicEthernetDriver#newCore(org.jnode.driver.Device, 
     * org.jnode.driver.net.ethernet.spi.Flags)
     */
    protected final AbstractDeviceCore newCore(Device device, Flags flags)
        throws DriverException, ResourceNotFreeException {
        return newWirelessCore(device, flags);
    }

    /**
     * Create a new device code instance
     */
    protected abstract WirelessDeviceCore newWirelessCore(Device device, Flags flags)
        throws DriverException, ResourceNotFreeException;

    /**
     * @see org.jnode.driver.net.WirelessNetDeviceAPI#setAuthenticationMode(
     * org.jnode.net.wireless.AuthenticationMode)
     */
    public void setAuthenticationMode(AuthenticationMode mode) throws NetworkException {
        try {
            getWirelessCore().setAuthenticationMode(mode);
        } catch (DriverException ex) {
            log.debug("setAuthenticationMode failed", ex);
            throw new NetworkException(ex);
        }
    }

    /**
     * @see org.jnode.driver.net.WirelessNetDeviceAPI#setESSID(java.lang.String)
     */
    public void setESSID(String essid) throws NetworkException {
        try {
            getWirelessCore().setESSID(essid);
        } catch (DriverException ex) {
            log.debug("setESSID failed", ex);
            throw new NetworkException(ex);
        }
    }

    /**
     * @see org.jnode.driver.net.spi.AbstractNetDriver#showInfo(java.io.PrintWriter)
     */
    public void showInfo(PrintWriter out) {
        super.showInfo(out);
        out.println("Current ESSID " + getESSID());
    }

    /**
     * @see org.jnode.driver.net.ethernet.spi.BasicEthernetDriver#startDevice()
     */
    protected void startDevice() throws DriverException {
        super.startDevice();
        getDevice().registerAPI(WirelessNetDeviceAPI.class, this);
    }

    /**
     * @see org.jnode.driver.net.ethernet.spi.BasicEthernetDriver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        getDevice().unregisterAPI(WirelessNetDeviceAPI.class);
        super.stopDevice();
    }
}
