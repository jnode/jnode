/*
 * $Id$
 */
package org.jnode.vm.x86;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jnode.system.BootLog;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPConfigTable {
    
    private final MemoryResource mem;
    private static final byte[] ENTRY_LENGTH = { 20, 8, 8, 8, 8 };
    private final List entries;
    
    private static final byte[] SIGNATURE = { 'P', 'C', 'M', 'P' };
    
    /**
     * Initialize this instance.
     * @param mem
     */
    MPConfigTable(MemoryResource mem) {
        this.mem = mem;
        if (isValid()) {
            this.entries = parse();
        } else {
            this.entries = null;
        }
    }
       
    /**
     * Does the table in the given memory resource have a valid signature?
     */
    boolean isValid() {
        for (int i = 0; i < SIGNATURE.length; i++) {
            if (mem.getByte(i) != SIGNATURE[i]) {
                // Invalid signature
                return false;
            }
        }
        final int specRev = mem.getByte(6);
        if ((specRev != 0x01) && (specRev != 0x04)) {
            // Invalid specification revision
            return false;
        }
        return true;
    }
    
    /**
     * Gets the number of entries
     */
    public int getEntryCount() {
        return mem.getChar(34);
    }

    /**
     * Gets the length of this table in bytes.
     */
    public int getBaseTableLength() {
        return mem.getChar(4);
    }

    /**
     * Gets the length of the extended table in bytes.
     */
    public int getExtendedTableLength() {
        return mem.getChar(40);
    }

    /**
     * Gets the physical address of the local APIC.
     */
    public Address getLocalApicAddress() {
        return Address.fromIntZeroExtend(mem.getInt(36));
    }
    
    /**
     * Gets the systems manufacturer identification string.
     */
    public String getOemID() {
        final byte[] data = new byte[8];
        mem.getBytes(8, data, 0, data.length);
        return new String(data).trim();
    }
    
    /**
     * Gets the product family identification string.
     */
    public String getProductID() {
        final byte[] data = new byte[12];
        mem.getBytes(16, data, 0, data.length);
        return new String(data).trim();
    }

    /**
     * Gets a list of all MP entries in this table.
     */
    final List entries() {
        return this.entries;
    }

    /**
     * Release all resource hold.
     */
    final void release() {
        mem.release();
    }
    
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "MPConfigTable";
    }
    
    public void dump(PrintStream out) {
        out.println("MPConfigTable");       
        out.println("Address        0x" + NumberUtils.hex(mem.getAddress().toInt()));
        out.println("Size           " + mem.getSize());
        out.println("Manufacturer   " + getOemID());
        out.println("Product        " + getProductID());
        out.println("Local APIC ptr 0x" + NumberUtils.hex(getLocalApicAddress().toInt()));
        out.println("Entries");
        for (Iterator i = entries.iterator(); i.hasNext(); ) {
            final MPEntry e = (MPEntry)i.next();
            out.println("  " + e);            
        }
    }
    
    private final List parse() {
        final int cnt = getEntryCount();
        final ArrayList list = new ArrayList(cnt);
        int offset = 0x2C;
        final int size = (int)mem.getSize().toInt();
        try {
            while (offset < size) {
                final int type = mem.getByte(offset);
                final int len = ENTRY_LENGTH[type];
                final MemoryResource mem = this.mem.claimChildResource(offset, len, false);
                final MPEntry entry;
                switch (type) {
                case 0: entry = new MPProcessorEntry(mem); break;
                case 1: entry = new MPBusEntry(mem); break;
                case 2: entry = new MPIOAPICEntry(mem); break;
                case 3: entry = new MPIOInterruptAssignmentEntry(mem); break;
                case 4: entry = new MPLocalInterruptAssignmentEntry(mem); break;
                default: entry = null;
                }
                list.add(entry);
                offset += len;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            BootLog.error("Error parsing the MP config table", ex);
        } catch (ResourceNotFreeException ex) {
            BootLog.error("Cannot claim MP entry region");
        }
        return list;
    }
}
