/*
 * $Id$
 */
package org.jnode.driver;

import java.io.PrintWriter;


/**
 * Devices should implement this API to allow a user to view information
 * of a specific device.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DeviceInfoAPI extends DeviceAPI {

    /**
     * Show all information of this device to the given writer.
     * @param out
     */
    public void showInfo(PrintWriter out);
    
}
