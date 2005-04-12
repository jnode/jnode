/*
 * $Id$
 */
package org.jnode.driver.net;

import java.util.EventListener;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface NetDeviceListener extends EventListener {

    /**
     * A net device has fired the given event.
     * @param event
     */
    public void processEvent(NetDeviceEvent event);
}
