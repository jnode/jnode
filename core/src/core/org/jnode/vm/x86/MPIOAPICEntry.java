/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPIOAPICEntry extends MPEntry {

    /**
     * @param mem
     */
    MPIOAPICEntry(MemoryResource mem) {
        super(mem);
    }
    
    public int getApicID() {
        return mem.getByte(1) & 0xFF;
    }

    public int getApicVersion() {
        return mem.getByte(2) & 0xFF;
    }

    public int getFlags() {
        return mem.getByte(3) & 0xFF;
    }
    
    public Address getAddress() {
        return Address.valueOf(mem.getInt(4));
    }
    
    
    /**
     * @see org.jnode.vm.x86.MPEntry#toString()
     */
    public String toString() {
        return super.toString() + " ID 0x" + NumberUtils.hex(getApicID(), 2) +
        	", version " + getApicVersion() +
        	", flags 0x" + NumberUtils.hex(getFlags(), 2) +
        	", addr 0x" + Address.toString(getAddress());
    }
    
    /**
     * @see org.jnode.vm.x86.MPEntry#getEntryTypeName()
     */
    public String getEntryTypeName() {
        return "I/O APIC";
    }
}
