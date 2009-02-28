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
 
package org.jnode.apps.vmware.disk.handler.sparse;

import org.apache.log4j.Logger;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class AllocationTable {
    private static final Logger LOG = Logger.getLogger(AllocationTable.class);

    private final GrainDirectory grainDirectory;
    private final GrainTable[] grainTables;

    /**
     * 
     * @param grainDirectory
     * @param grainTables
     */
    public AllocationTable(GrainDirectory grainDirectory, GrainTable[] grainTables) {
        this.grainDirectory = grainDirectory;
        this.grainTables = grainTables;
    }

    /**
     * 
     * @return
     */
    public GrainDirectory getGrainDirectory() {
        return grainDirectory;
    }

    /**
     * 
     * @return
     */
    public int getNbGrainTables() {
        return grainTables.length;
    }

    /**
     * 
     * @param tableNum
     * @return
     */
    public GrainTable getGrainTable(int tableNum) {
        if ((tableNum < 0) || (tableNum >= grainTables.length)) {
            // TODO fix the bug
            LOG.fatal("getGrainTable: FATAL: index out of bounds, actual=" + tableNum + ", max=" +
                    (grainTables.length - 1) + ", using max");
            tableNum = (grainTables.length - 1);
        }
        return grainTables[tableNum];
    }
}
