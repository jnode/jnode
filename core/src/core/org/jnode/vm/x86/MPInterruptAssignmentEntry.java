/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class MPInterruptAssignmentEntry extends MPEntry {

    /**
     * @param mem
     */
    MPInterruptAssignmentEntry(MemoryResource mem) {
        super(mem);
    }
    
    public int getInterruptType() {
        return mem.getByte(1) & 0xFF;
    }
    
    public int getFlags() {
        return mem.getChar(2);
    }
    
    public int getSourceBusID() {
        return mem.getByte(4) & 0xFF;
    }

    public int getSourceBusIRQ() {
        return mem.getByte(5) & 0xFF;
    }

    public int getDestinationApicID() {
        return mem.getByte(6) & 0xFF;
    }
    
    public int getDestinationApicINTN() {
        return mem.getByte(7) & 0xFF;
    }
    
    
    /**
     * @see org.jnode.vm.x86.MPEntry#toString()
     */
    public String toString() {
        return super.toString() + " type " + getInterruptType() +
        	", flags 0x" + NumberUtils.hex(getFlags(), 4) +
        	", src bus:0x" + NumberUtils.hex(getSourceBusID(), 2) +
        	",irq:" + getSourceBusIRQ() +
        	"), dst ID:0x" + NumberUtils.hex(getDestinationApicID(), 2) +
        	",INTN:" + getDestinationApicINTN() + ")";
    }
}
