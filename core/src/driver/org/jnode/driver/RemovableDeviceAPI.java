/*
 * $Id$
 */
package org.jnode.driver;

import java.io.IOException;

/**
 * API that must be implemented by removable devices.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface RemovableDeviceAPI extends DeviceAPI {

    /**
     * Can this device be locked.
     * @return
     */
    public boolean canLock();
    
    /**
     * Can this device be ejected.
     * @return
     */
    public boolean canEject();
    
    /**
     * Lock the device.
     * @throws IOException
     */
    public void lock()
    throws IOException;
    
    /**
     * Unlock the device.
     * @throws IOException
     */
    public void unlock()
    throws IOException;
    
    /**
     * Is this device locked.
     * @return
     */
    public boolean isLocked();
    
    /**
     * Eject this device.
     * @throws IOException
     */
    public void eject()
    throws IOException;
}
