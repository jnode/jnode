/*
 * $Id$
 */
package org.jnode.driver.net;

/**
 * Device API for wireless network devices.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface WirelessNetDeviceAPI extends NetDeviceAPI {

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
