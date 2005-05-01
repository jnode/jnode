/*
 * $Id$
 */
package org.jnode.fs.service.def;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;

/**
 * Device for the VirtualFS.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VirtualFSDevice extends Device {

    public VirtualFSDevice() {
        super(null, "vfs");
        try {
            setDriver(new VirtualFSDriver());
        } catch (DriverException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private static class VirtualFSDriver extends Driver {

        /**
         * @see org.jnode.driver.Driver#startDevice()
         */
        protected void startDevice() throws DriverException {
            // Nothing
        }

        /**
         * @see org.jnode.driver.Driver#stopDevice()
         */
        protected void stopDevice() throws DriverException {
            // Nothing
        }        
    }
}
