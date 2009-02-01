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
import org.vmmagic.unboxed.Address;

/**
 * Structure wrapper for the RootSystemDescriptionTable (RSDT).
 *
 * @author Francois-Frederic Ozog
 */
public class RootSystemDescriptionTable extends SystemDescriptionTable {

    public RootSystemDescriptionTable(AcpiDriver driver, ResourceManager rm,
                                      MemoryResource tableResource) throws ResourceNotFreeException {
        super(driver, tableResource);
        parse(rm);
    }

    private final void parse(ResourceManager rm)
        throws ResourceNotFreeException {
        final int startOfTablePointers = 36;
        final int tablesCount = (getSize() - startOfTablePointers) / 4;
        final ResourceOwner owner = new SimpleResourceOwner(
            "ACPI-RootSystemDescriptionTable");
        for (int index = 0; index < tablesCount; index++) {
            // log.debug("Handling RSDT index " + index + "/" + tablesCount);
            try {
                final int ptrOffset = startOfTablePointers + (index * 4);
                final Address ptr = getAddress32(ptrOffset);
                final AcpiTable table = AcpiTable.getTable(getDriver(), owner, rm, ptr);
                if (table != null) {
                    addTable(table);
                }
            } catch (Exception ex) {
                log.error("Error loading table " + index, ex);
            }
        }
    }
}
