/*
 * $Id$
 */
package org.jnode.driver.net;

import org.jnode.net.wireless.AuthenticationMode;

/**
 * Device API for wireless network devices.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface WirelessNetDeviceAPI extends NetDeviceAPI {

    /**
     * Gets the current authentication mode.
     * @return
     */
    public AuthenticationMode getAuthenticationMode();
    
    /**
     * Sets the current authentication mode.
     * @param mode
     */
    public void setAuthenticationMode(AuthenticationMode mode)
    throws NetworkException;
    
    /**
     * Gets the current ESS ID.
     * @return A valid ESSID, or null if not ESSID is present.
     */
    public String getESSID();

    /**
     * Sets the current ESS ID.
     * @param essid A valid ESSID, or null for any ESS.
     */
    public void setESSID(String essid)
    throws NetworkException;
    
}
