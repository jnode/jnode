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
 
package org.jnode.driver.system.acpi;

import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.MagicUtils;
import org.vmmagic.unboxed.Offset;

/**
 * ACPI abstract table.
 *
 * @author Francois-Frederic Ozog
 */
@MagicPermission
public abstract class AcpiTable {

    protected final Logger log = Logger.getLogger(getClass());

    private final AcpiDriver driver;
    private final MemoryResource tableResource;

    private final String signature;

    /**
     * Initialize this instance.
     *
     * @param tableResource
     */
    public AcpiTable(AcpiDriver driver, MemoryResource tableResource) {
        this.driver = driver;
        this.tableResource = tableResource;
        signature = getString(0, 4);
    }

    /**
     * Gets the length of this table in bytes.
     *
     * @return
     */
    public final int getSize() {
        return tableResource.getSize().toInt();
    }

    /**
     * Release all resources.
     */
    public void release() {
        tableResource.release();
    }

    /**
     * Gets the signature of this table.
     *
     * @return
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Gets a 32-bit address from a given offset in the table.
     *
     * @param offset
     * @return
     */
    protected final Address getAddress32(int offset) {
        return Address.fromIntZeroExtend(tableResource.getInt(offset));
    }

    /**
     * Gets a 64-bit address from a given offset in the table.
     *
     * @param offset
     * @return
     */
    protected final Address getAddress64(int offset) {
        return Address.fromLong(tableResource.getLong(offset));
    }

    /**
     * Gets a 32-bit integer from a given offset in the table.
     *
     * @param offset
     * @return
     */
    protected final int getInt(int offset) {
        return tableResource.getInt(offset);
    }

    /**
     * Gets a 16-bit integer from a given offset in the table.
     *
     * @param offset
     * @return
     */
    protected final int getShort(int offset) {
        return ((tableResource.getByte(offset + 1) & 0xff) << 8)
            + (tableResource.getByte(offset) & 0xff);
    }

    /**
     * Gets an ASCII string from a given offset in the table.
     *
     * @param offset
     * @param length Length in bytes of the string.
     * @return
     */
    protected final String getString(int offset, int length) {
        final char[] arr = new char[length];
        for (int i = 0; i < length; i++) {
            arr[i] = (char) (tableResource.getByte(offset + i) & 0xFF);
        }
        return String.valueOf(arr);
    }

    /**
     * Gets a 8-bit integer from a given offset in the table.
     *
     * @param offset
     * @return
     */
    protected final int getByte(int offset) {
        return tableResource.getByte(offset) & 0xff;
    }

    /**
     * Gets a series 8-bit integer from a given offset in the table.
     *
     * @param offset
     * @return
     */
    protected final void getBytes(int offset, byte[] dst, int dstOfs, int length) {
        tableResource.getBytes(offset, dst, dstOfs, length);
    }

    /**
     * Convert to a String representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final String start = MagicUtils.toString(tableResource.getAddress());
        final int size = getSize();
        return getSignature() + " [0x" + start + ":len " + size + "]";
    }

    /**
     * Load an ACPI table from a given address.
     *
     * @param owner
     * @param rm
     * @param ptr
     * @return The table or null for an unknown table.
     * @throws ResourceNotFreeException
     */
    static AcpiTable getTable(AcpiDriver drv, ResourceOwner owner, ResourceManager rm,
                                    Address ptr) throws ResourceNotFreeException {
        try {
            // Convert physical address to virtual address.
            ptr = drv.physToVirtual(ptr);

            // Load the length of the entire table.
            final int length = ptr.loadInt(Offset.fromIntZeroExtend(4));
            // Claim the full table memory region.
            final MemoryResource table;
            table = rm.claimMemoryResource(owner, ptr, length,
                ResourceManager.MEMMODE_NORMAL);
            // Load the signature
            final char[] signatureArr = new char[4];
            for (int i = 0; i < 4; i++) {
                signatureArr[i] = (char) (table.getByte(i) & 0xff);
            }
            final String signature = String.valueOf(signatureArr);

            if (signature.equals("RSDT")) {
                return new RootSystemDescriptionTable(drv, rm, table);
            } else if (signature.equals("DSDT")) {
                return new DifferentiatedSystemDescriptionTable(drv, rm, table);
            } else if (signature.equals("FACS")) {
                return new FirmwareAcpiControlStructure(drv, rm, table);
            } else if (signature.equals("FACP")) {
                return new FixedAcpiDescriptionTable(drv, rm, table);
            } else {
                table.release();
                return null;
            }
        } catch (ResourceNotFreeException ex1) {
            throw new ResourceNotFreeException(
                "Could not get table header begining memory range: "
                    + MagicUtils.toString(ptr) + "(8 bytes)");
        }

    }

    /**
     * @return Returns the driver.
     */
    final AcpiDriver getDriver() {
        return driver;
    }
}
