/*
 * $Id$
 */
package org.jnode.driver.net.wireless.spi;

import org.jnode.driver.DriverException;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.net.wireless.AuthenticationMode;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class WirelessDeviceCore extends AbstractDeviceCore {

    /**
     * Start a scan for available networks.
     */
    public abstract void startScan()
    throws DriverException;
    
    /**
     * Gets the current authentication mode.
     * @return
     */
    protected abstract AuthenticationMode getAuthenticationMode()
    throws DriverException;
    
    /**
     * Sets the current authentication mode.
     * @param mode
     */
    protected abstract void setAuthenticationMode(AuthenticationMode mode)
    throws DriverException;
    
    /**
     * Gets the current ESS ID.
     * @return A valid ESSID, or null if not ESSID is present.
     */
    protected abstract String getESSID()
    throws DriverException;

    /**
     * Sets the current ESSID.
     * @param essid A valid ESSID, or null for any ESS.
     */
    protected abstract void setESSID(String essid)
    throws DriverException;
}
