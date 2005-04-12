/*
 * $Id$
 */
package org.jnode.driver.net.event;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetDeviceEvent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class LinkStatusEvent extends NetDeviceEvent {

    /** The link status has changed */ 
    public static final int LINK_STATUS_CHANGED = 100;
    
    /** The connection status of the link */
    private final boolean connected;
    
    /**
     * @param source
     * @param id
     */
    public LinkStatusEvent(Device source, boolean connected) {
        super(source, LINK_STATUS_CHANGED);
        this.connected = connected;
    }

    /**
     * @return Returns the connected.
     */
    public final boolean isConnected() {
        return connected;
    }    
}
