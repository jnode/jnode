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

import org.jnode.system.MemoryResource;
import org.vmmagic.unboxed.Address;

/**
 * Root System Description Pointer Structure.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class RSDP extends AcpiTable {

    /**
     * @param tableResource
     */
    public RSDP(AcpiDriver driver, MemoryResource tableResource) {
        super(driver, tableResource);
    }

    /**
     * Gets the signature of this structure.
     *
     * @see org.jnode.driver.system.acpi.AcpiTable#getSignature()
     */
    public final String getSignature() {
        return getString(0, 8);
    }

    /**
     * Gets the OEM ID.
     *
     * @return
     */
    public final String getOemId() {
        return getString(9, 6);
    }

    /**
     * Gets the revision of the table.
     *
     * @return 0 for ACPI 1.0, 2 for ACPI 2 and 3.
     */
    public final int getRevision() {
        return getByte(15);
    }

    /**
     * Gets the address of the RSDT.
     *
     * @return
     */
    public final Address getRsdtAddress() {
        return getAddress32(16);
    }

    /**
     * Get the length of the table, in bytes, including the header, starting
     * from offset 0.
     * This field is used to record the size of the entire table.
     *
     * @return
     */
    public final int getLength() {
        if (getRevision() == 0) {
            return 0;
        } else {
            return getInt(20);
        }
    }

    /**
     * Gets the address of the XSDT.
     *
     * @return The address of the XSDT of Address.zero for ACPI 1.0.
     */
    public final Address getXsdtAddress() {
        if (getRevision() == 0) {
            return Address.zero();
        } else {
            return getAddress64(24);
        }
    }
}
