/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.MemoryScanner;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPFloatingPointerStructure {

    private MemoryResource mem;
    private static final int MAGIC = 0x5F504D5F; // _MP_
    private MPConfigTable configTable;
    
    /**
     * Find the MP floating pointer structure.
     * @return The found structure, or null if not found.
     */
    public static MPFloatingPointerStructure find(ResourceManager rm, ResourceOwner owner) {
        MPFloatingPointerStructure mp;
        mp = find(rm, owner, 639 * 1024, 640 * 1024);
        if (mp == null) {
            mp = find(rm, owner, 0xF0000, 0xFFFFF);
        }
        if (mp == null) {
            return null;
        }
        return mp;
    }
    
    /**
     * Release the resources hold by this structure.
     */
    public void release() {
        mem.release();
    }
    
    /**
     * Gets the length of this structure in bytes.
     * @return
     */
    final int getLength() {
        return (16 * (mem.getByte(0x08) & 0xFF));
    }
    
    /**
	 * Gets the specification revision level
	 */
    final int getSpecRevision() {
    	return mem.getByte(0x09);
    }
    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	return "MP 1." + getSpecRevision() + ", ConfigTableAt 0x" + NumberUtils.hex(mem.getInt(0x04));
    }
    
    /**
     * Gets the physical address of the MP configuration table.
     * @return
     */
    final Address getMPConfigTablePtr() {
        return Address.valueOf(mem.getInt(0x04));
    }
    
    /**
     * Gets the MPConfig table.
     */
    final MPConfigTable getMPConfigTable() {
        return configTable; 
    }
    
    /**
     * Initialize this instance.
     * @param mem
     */
    private MPFloatingPointerStructure(MemoryResource mem) {
        this.mem = mem;
    }
    
    /**
     * Is this a valid MPFP structure?
     */
    private final boolean isValid() {
        // Length should be 16
        if (getLength() != 16) {
            return false;
        }
        // Check checksum
        int sum = 0;
        for (int i = 0; i < 16; i++) {
            sum += mem.getByte(i) & 0xFF;
            sum &= 0xFF;
        }
        if (sum != 0) {
            return false;
        }
   
        return true;
    }
    
    private final void initConfigTable(ResourceManager rm, ResourceOwner owner) {
        final Address tablePtr = getMPConfigTablePtr();
        int size = 0x2C; // Base table length
        try {
            MemoryResource mem = rm.claimMemoryResource(owner, tablePtr, size, ResourceManager.MEMMODE_NORMAL);
            // Read the table length
            int baseTableLen = mem.getChar(4);
            mem.release();
            // Claim the full table.
            //BootLog.info("baseTableLength " + baseTableLen);
            size = baseTableLen;
            mem = rm.claimMemoryResource(owner, tablePtr, size, ResourceManager.MEMMODE_NORMAL);
            this.configTable = new MPConfigTable(mem);
        } catch (ResourceNotFreeException ex) {
            BootLog.warn("Cannot claim MP config table region");
            ex.printStackTrace();
        }
    }
    
    /**
     * Find the structure between to pointers.
     * @param rm
     * @param owner
     * @param startPtr
     * @param endPtr
     * @return
     */
    private static MPFloatingPointerStructure find(ResourceManager rm, ResourceOwner owner, int startPtr, int endPtr) {
        final MemoryScanner ms = rm.getMemoryScanner();
        Address ptr = Address.valueOf(startPtr);
        int size = endPtr - startPtr;  
        final int stepSize = 16;
        while (size > 0) {
            Address res = ms.findInt32(ptr, size, MAGIC, stepSize);
            if (res != null) {
                try {
                    final MemoryResource mem;
                    mem = rm.claimMemoryResource(owner, ptr, 16, ResourceManager.MEMMODE_NORMAL);
                    final MPFloatingPointerStructure mp = new MPFloatingPointerStructure(mem);
                    if (mp.isValid()) {
                        mp.initConfigTable(rm, owner);
                        return mp;
                    }
                    mp.release();
                } catch (ResourceNotFreeException ex) {
                    BootLog.warn("Cannot claim MP region");
                }
            }
            ptr = Address.add(ptr, stepSize);
            size -= stepSize;
        }
        return null;
    }
}
