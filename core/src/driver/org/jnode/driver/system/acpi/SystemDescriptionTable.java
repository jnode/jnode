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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.jnode.driver.system.acpi.aml.ParseNode;
import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SystemDescriptionTable extends AcpiSystemTable {

    private final ArrayList<AcpiTable> tables = new ArrayList<AcpiTable>();
    private final HashMap<String, AcpiTable> tableMap = new HashMap<String, AcpiTable>();

    /**
     * Initialize this instance.
     *
     * @param tableResource
     */
    public SystemDescriptionTable(AcpiDriver driver, MemoryResource tableResource) {
        super(driver, tableResource);
    }

    /**
     * List of AcpiTable.
     *
     * @return
     */
    public final List<AcpiTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    /**
     * Release all resources.
     *
     * @see org.jnode.driver.system.acpi.AcpiTable#release()
     */
    public void release() {
        super.release();
        for (AcpiTable table : tables) {
            table.release();
        }
    }

    public final FixedAcpiDescriptionTable getFACP() {
        return (FixedAcpiDescriptionTable) getTable("FACP");
    }

    public ParseNode getParsedAml() {
        final FixedAcpiDescriptionTable facp = getFACP();
        if (facp == null) {
            return null;
        }
        final DifferentiatedSystemDescriptionTable dsdt = facp.getDSDT();
        if (dsdt != null) {
            return dsdt.getParsedAml();
        }
        return null;
    }

    protected final void addTable(AcpiTable table) {
        tables.add(table);
        tableMap.put(table.getSignature(), table);
    }

    protected final AcpiTable getTable(String signature) {
        return (AcpiTable) tableMap.get(signature);
    }
}
