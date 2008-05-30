/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.system.acpi;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.driver.system.acpi.aml.Aml;
import org.jnode.driver.system.acpi.aml.NameString;
import org.jnode.driver.system.acpi.aml.ParseNode;
import org.jnode.driver.system.acpi.vm.NameSpace;
import org.jnode.driver.system.firmware.AcpiDevice;
import org.jnode.driver.system.firmware.AcpiRSDPInfo;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.util.NumberUtils;
import org.jnode.vm.MemoryMapEntry;
import static org.jnode.vm.VirtualMemoryRegion.ACPI;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.MagicUtils;
import org.vmmagic.unboxed.Word;

/**
 * AcpiDriver.
 *
 * @author Francois-Frederic Ozog
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class AcpiDriver extends Driver implements AcpiAPI {

    private static final Logger log = Logger.getLogger(AcpiDriver.class);

    private SystemDescriptionTable sdt;

    private NameSpace root;

    private RSDP rsdp;

    /**
     * Physical address of the first ACPI memory map entry
     */
    private Address acpiPhysStart;
    /**
     * Virtual address of the first ACPI memory map entry
     */
    private Address acpiVirtStart;

    public void reset() {

    }

    /**
     * Initialize this instance.
     */
    public AcpiDriver() {
    }

    /**
     * @see org.jnode.driver.Driver#startDevice()
     */
    protected void startDevice() throws DriverException {
        final AcpiDevice dev = (AcpiDevice) getDevice();
        try {
            final ResourceManager rm;
            rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
            mmapAcpiRegion();
            AcpiRSDPInfo acpiInfo = ((AcpiDevice) dev).getRsdpInfo();
            loadRootTable(rm, acpiInfo);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Could not find ResourceManager", ex);
        } catch (ResourceNotFreeException ex) {
            throw new DriverException("Cannot load ACPI info", ex);
        }

        // Register the API's
        dev.registerAPI(AcpiAPI.class, this);
    }

    /**
     * @see org.jnode.driver.Driver#stopDevice()
     */
    protected void stopDevice() throws DriverException {
        final AcpiDevice dev = (AcpiDevice) getDevice();

        // Unregister the API's
        dev.unregisterAPI(AcpiAPI.class);

        // Reset the values
        root = null;
        if (sdt != null) {
            sdt.release();
            sdt = null;
        }
        if (rsdp != null) {
            rsdp.release();
            rsdp = null;
        }
    }

    /**
     * Dump the namespace.
     *
     * @see org.jnode.driver.system.acpi.AcpiAPI#dump(java.io.PrintWriter)
     */
    public void dump(PrintWriter out) {
        if (sdt != null) {
            out.println("Tables:");
            out.println(sdt);
            for (AcpiTable table : sdt.getTables()) {
                out.println(table);
            }
            out.println();
        }
        if (root != null) {
            out.println("Namespace:");
            root.dump(out);
        }
    }

    public void dumpBattery(PrintWriter out) {
        ParseNode battery;
        battery = sdt.getParsedAml().findName(new NameString("BAT0"),
            Aml.AML_DEVICE);
        if (battery != null) {
            out.println(battery.toString());
        }
        battery = sdt.getParsedAml().findName(new NameString("BAT1"),
            Aml.AML_DEVICE);
        if (battery != null) {
            out.println(battery.toString());
        }
    }

    /**
     * Map the ACPI virtual memory region.
     */
    private void mmapAcpiRegion()
        throws DriverException {
        final VmArchitecture arch = Vm.getArch();
        final MemoryMapEntry[] mmap = (MemoryMapEntry[]) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return arch.getMemoryMap();
            }
        });

        final Address acpiStart = arch.getStart(ACPI);
        final Address acpiEnd = arch.getEnd(ACPI);
        // First unmap the entire ACPI region
        arch.munmap(ACPI, acpiStart, acpiEnd.toWord().sub(acpiStart.toWord()).toExtent());
        this.acpiVirtStart = acpiStart;

        MemoryMapEntry lastAcpiEntry = null;
        Address start = acpiStart;
        for (MemoryMapEntry e : mmap) {
            if (e.isAcpi() || e.isAcpiNVS()) {
                // Calculate page aligned boundaries & sizes
                final Address alignedPhysStart = arch.pageAlign(ACPI, e.getStart(), false);
                final Word diff = e.getStart().toWord().sub(alignedPhysStart.toWord());
                final Extent size = e.getSize().add(diff);

                // Check for adjacent memory blocks
                if (lastAcpiEntry != null) {
                    Address expected = lastAcpiEntry.getStart().add(lastAcpiEntry.getSize());
                    if (e.getStart().NE(expected)) {
                        throw new DriverException("ACPI memory map entries are not adjacent");
                    }
                } else {
                    this.acpiPhysStart = alignedPhysStart;
                }

                // Map entry
                log.info("Mapping ACPI memory map entry to " + MagicUtils.toString(start));
                arch.mmap(ACPI, start, size, alignedPhysStart);
                start = start.add(e.getSize());
                lastAcpiEntry = e;
            }
        }
        if (lastAcpiEntry == null) {
            throw new DriverException("No ACPI memory map entries found");
        }
    }

    private void loadRootTable(ResourceManager rm, AcpiRSDPInfo acpiInfo)
        throws ResourceNotFreeException {
        if (acpiInfo != null) {
            final ResourceOwner owner = new SimpleResourceOwner("ACPI");
            final MemoryResource rsdtptrRes;
            rsdtptrRes = rm.claimMemoryResource(owner, acpiInfo.getRsdpStart(),
                acpiInfo.getLength(), ResourceManager.MEMMODE_NORMAL);
            this.rsdp = new RSDP(this, rsdtptrRes);
            this.root = NameSpace.getRoot();
            Address ptr = rsdp.getXsdtAddress();
            if (ptr.isZero()) {
                log.info("Using RSDT, length " + NumberUtils.hex(rsdp.getLength()));
                ptr = rsdp.getRsdtAddress();

                // Use the RSDT (ACPI 1.0)
                sdt = (RootSystemDescriptionTable) AcpiTable.getTable(this, owner,
                    rm, ptr);
                root.parse(sdt.getParsedAml());
            } else {
                // Use the XSDT (ACPI >= 2.0)
                sdt = (ExtendedSystemDescriptionTable) AcpiTable.getTable(this,
                    owner, rm, ptr);
                root.parse(sdt.getParsedAml());
            }
        }

    }

    /**
     * Convert a physical address of an ACPI table to a virtual address
     * in the ACPI virtual memory region.
     *
     * @param physAddr
     * @return
     */
    final Address physToVirtual(Address physAddr) {
        final Word offset = physAddr.toWord().sub(acpiPhysStart.toWord());
        return acpiVirtStart.add(offset);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        /* FixedAcpiDescriptionTable facp = */
        sdt.getFACP();
        buffer.append(rsdp.getOemId());
        buffer.append(' ');
        buffer.append(sdt.getOemTableId());
        buffer.append(": ACPI version ");
        final int revision = rsdp.getRevision();
        buffer.append(revision == 0 ? "1.0" : (revision == 2 ? "2.0"
            : "unknown-" + revision));
        return buffer.toString();
    }

    public String toDetailedString() {
        StringBuffer buffer = new StringBuffer();
        FixedAcpiDescriptionTable facp = sdt.getFACP();
        buffer.append(rsdp.getOemId());
        buffer.append(' ');
        buffer.append(sdt.getOemTableId());
        buffer.append(" ACPI version ");
        final int revision = rsdp.getRevision();
        buffer.append(revision == 0 ? "1.0" : (revision == 2 ? "2.0"
            : "unknown-" + revision));
        buffer.append("\n    PM1A(");
        buffer.append(Integer.toHexString(facp.getPm1aControl()));
        buffer.append(", ");
        buffer.append(Integer.toHexString(facp.getPm1aEvent()));
        buffer.append("), PM1B(");
        buffer.append(Integer.toHexString(facp.getPm1bControl()));
        buffer.append(", ");
        buffer.append(Integer.toHexString(facp.getPm1bEvent()));
        buffer.append("), PM2(");
        buffer.append(Integer.toHexString(facp.getPm2Control()));
        buffer.append("), Timer(");
        buffer.append(Integer.toHexString(facp.getPmTimer()));
        buffer.append("), Events(");
        buffer.append(Integer.toHexString(facp.getGeneralPurposeEvent0()));
        buffer.append(", ");
        buffer.append(Integer.toHexString(facp.getGeneralPurposeEvent1()));
        buffer.append("), Flags(");
        buffer.append(Integer.toHexString(facp.getFlags()));
        buffer.append(")");
        return buffer.toString();
    }
}
