/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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

import org.jnode.system.resource.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class MPIOInterruptAssignmentEntry extends MPInterruptAssignmentEntry {

    /**
     * @param mem
     */
    MPIOInterruptAssignmentEntry(MemoryResource mem) {
        super(mem);
    }

    /**
     * @see org.jnode.vm.x86.MPEntry#getEntryTypeName()
     */
    public String getEntryTypeName() {
        return "I/O Int.Ass.";
    }
}
