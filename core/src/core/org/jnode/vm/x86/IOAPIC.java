/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.x86;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class IOAPIC {

    private final MemoryResource mem;
    private final List<IOAPICRedirectionEntry> entries;

    private static final int IOREGSEL = 0x00;
    private static final int IOWIN = 0x10;

    private static final int REG_IOAPICID = 0x00;
    private static final int REG_IOAPICVER = 0x01;
    private static final int REG_IOAPICARB = 0x02;

    /**
     * Initialize this instance.
     *
     * @param rm
     * @param owner
     * @param ptr
     * @throws ResourceNotFreeException
     */
    public IOAPIC(ResourceManager rm, ResourceOwner owner, Address ptr) throws ResourceNotFreeException {
        this.mem = rm.claimMemoryResource(owner, ptr, Extent.fromIntZeroExtend(0x20), ResourceManager.MEMMODE_NORMAL);
        final int cnt = getMaximumRedirectionEntryIndex() + 1;
        this.entries = new ArrayList<IOAPICRedirectionEntry>(cnt);
        for (int i = 0; i < cnt; i++) {
            entries.add(new IOAPICRedirectionEntry(this, 0x10 + i * 2));
        }
    }

    /**
     * Release all resources.
     */
    public void release() {
        mem.release();
    }

    /**
     * Gets the ID of this I/O APIC.
     *
     * @return the id
     */
    public int getId() {
        return (getReg(REG_IOAPICID) >> 24) & 0xF;
    }

    /**
     * Gets the version of this I/O APIC.
     *
     * @return the version
     */
    public int getVersion() {
        return getReg(REG_IOAPICVER) & 0xFF;
    }

    /**
     * Gets the maximum index of the redirection table.
     * The number of redirection entries equals this number + 1.
     *
     * @return the maximum index of the redirection table.
     */
    public int getMaximumRedirectionEntryIndex() {
        return (getReg(REG_IOAPICVER) >> 16) & 0xFF;
    }

    /**
     * Gets the arbitration ID of this I/O APIC.
     *
     * @return the arbitration id
     */
    public int getArbitrationId() {
        return (getReg(REG_IOAPICARB) >> 24) & 0xF;
    }

    /**
     * Gets all redirection entries.
     *
     * @return the redirection entries
     */
    public List<IOAPICRedirectionEntry> getRedirectionEntries() {
        return entries;
    }

    /**
     * Dump all info about this I/O APIC to the given stream.
     *
     * @param out
     */
    public void dump(PrintStream out) {
        out.println("I/O APIC ID 0x" + NumberUtils.hex(getId(), 2) + ", version " + getVersion() + ", arb 0x" +
            NumberUtils.hex(getArbitrationId(), 2));
        int idx = 0;
        for (IOAPICRedirectionEntry entry : getRedirectionEntries()) {
            out.println("REDIR" + (idx++) + " " + entry);
        }
    }

    /**
     * Read an I/O APIC register.
     *
     * @param regNr
     * @return the register value
     */
    final synchronized int getReg(int regNr) {
        mem.setInt(IOREGSEL, regNr);
        return mem.getInt(IOWIN);
    }

    /**
     * Write an I/O APIC register.
     *
     * @param regNr
     */
    final synchronized void setReg(int regNr, int value) {
        mem.setInt(IOREGSEL, regNr);
        mem.setInt(IOWIN, value);
    }
}
