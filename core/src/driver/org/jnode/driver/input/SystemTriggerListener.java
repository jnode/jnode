/*
 * $Id$
 */
package org.jnode.driver.input;

import org.jnode.system.event.SystemEvent;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface SystemTriggerListener {
    
    /**
     * The system function is triggered.
     * @param event
     */
    public void systemTrigger(SystemEvent event);

}
