/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPBusEntry extends MPEntry {

    /**
     * @param mem
     */
    MPBusEntry(MemoryResource mem) {
        super(mem);
    }
    
    public int getBusID() {
        return mem.getByte(1) & 0xFF;
    }
    
    public String getBusType() {
        final byte[] data = new byte[6];
        mem.getBytes(2, data, 0, data.length);
        return new String(data).trim();
    }
    
    
    /**
     * @see org.jnode.vm.x86.MPEntry#toString()
     */
    public String toString() {
        return super.toString() + " ID 0x" + NumberUtils.hex(getBusID(), 2) + ", Type " + getBusType();
    }
    
    /**
     * @see org.jnode.vm.x86.MPEntry#getEntryTypeName()
     */
    public String getEntryTypeName() {
        return "Bus";
    }
}
