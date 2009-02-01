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

/**
 * ACPI system table.
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */
public abstract class AcpiSystemTable extends AcpiTable {
    private final String oemId;

    private final int revision;

    private final String oemTableId;

    private final String oemRevision;

    private final String creatorId;

    private final String creatorRevision;

    public AcpiSystemTable(AcpiDriver driver, MemoryResource tableResource) {
        super(driver, tableResource);
        this.revision = getByte(8);
        this.oemId = getString(10, 6);
        this.oemTableId = getString(16, 8);
        this.oemRevision = getString(24, 4);
        this.creatorId = getString(28, 4);
        this.creatorRevision = getString(32, 4);
    }

    /**
     * Convert to a String representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return super.toString() + "/{" + oemId + ", " + creatorId + "}";
    }

    /**
     * Gets the OEM ID.
     *
     * @return
     */
    public final String getOemId() {
        return oemId;
    }

    /**
     * Gets the OEM table ID.
     *
     * @return
     */
    public final String getOemTableId() {
        return oemTableId;
    }

    /**
     * @return Returns the creatorId.
     */
    public final String getCreatorId() {
        return creatorId;
    }

    /**
     * @return Returns the creatorRevision.
     */
    public final String getCreatorRevision() {
        return creatorRevision;
    }

    /**
     * @return Returns the oemRevision.
     */
    public final String getOemRevision() {
        return oemRevision;
    }

    /**
     * @return Returns the revision.
     */
    public final int getRevision() {
        return revision;
    }
}
