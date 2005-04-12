/*
 * $Id$
 */
package org.jnode.driver.net;

import java.util.EventObject;

import org.jnode.driver.Device;

/**
 * Base class for all net device events.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class NetDeviceEvent extends EventObject {

    /** Identification of the event */
    private final int id;
    
    /**
     * @param source
     */
    public NetDeviceEvent(Device source, int id) {
        super(source);
        this.id = id;
    }

    /**
     * Gets the device that it the source of this event.
     * @return
     */
    public final Device getDevice() {
        return (Device)getSource();
    }
    
    /**
     * Gets the event ID.
     * @return Returns the id.
     */
    public final int getId() {
        return id;
    }
}
