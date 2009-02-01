/*
 * $Id$
 *
 * JNode.org
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
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;

/**
 * FixedAcpiDescriptionTable.
 *
 * @author Francois-Frederic Ozog
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FixedAcpiDescriptionTable extends AcpiSystemTable {

    private FirmwareAcpiControlStructure facs;

    private DifferentiatedSystemDescriptionTable dsdt;

    public FixedAcpiDescriptionTable(AcpiDriver driver, ResourceManager rm,
                                     MemoryResource tableResource) throws ResourceNotFreeException {
        super(driver, tableResource);
        parse(rm);
    }

    public FirmwareAcpiControlStructure getFACS() {
        return facs;
    }

    public DifferentiatedSystemDescriptionTable getDSDT() {
        return dsdt;
    }

    public void release() {
        super.release();
        if (facs != null) {
            facs.release();
            facs = null;
        }
        if (dsdt != null) {
            dsdt.release();
            dsdt = null;
        }
    }

    public GenericAddress getResetRegister() {
        final byte[] raw = new byte[12];
        for (int i = 0; i < 12; i++) {
            raw[i] = (byte) getByte(116 + i);
        }
        return new GenericAddress(raw);
    }

    public int getSmiCmd() {
        return getInt(48);
    }

    public int getSciInt() {
        return getShort(46);
    }

    int getAcpiEnable() {
        return getByte(52);
    }

    int getAcpiDisable() {
        return getByte(53);
    }

    public void acpiEnable() {
        if (getSmiCmd() != 0) {
            // IOOUT(getSmiCmd(), getAcpiEnable());
        }
    }

    public void acpiDisable() {
        if (getSmiCmd() != 0) {
            // IOOUT(getSmiCmd(), getAcpiDisable());
        }
    }

    public int getPm1aEvent() {
        return getInt(56);
    }

    public int getPm1bEvent() {
        return getInt(60);
    }

    public int getPm1aControl() {
        return getInt(64);
    }

    public int getPm1bControl() {
        return getInt(68);
    }

    public int getPm2Control() {
        return getInt(72);
    }

    public int getPmTimer() {
        return getInt(76);
    }

    public int getGeneralPurposeEvent0() {
        return getInt(80);
    }

    public int getGeneralPurposeEvent1() {
        return getInt(84);
    }

    public int getFlags() {
        return getInt(112);
    }

    private final void parse(ResourceManager rm)
        throws ResourceNotFreeException {
        final ResourceOwner owner = new SimpleResourceOwner(
            "ACPI-FixedAcpiDescriptionTable");
        facs = (FirmwareAcpiControlStructure) AcpiTable.getTable(getDriver(), owner, rm,
            getAddress32(36));
        dsdt = (DifferentiatedSystemDescriptionTable) AcpiTable.getTable(getDriver(), owner,
            rm, getAddress32(40));
    }

}
