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
 
package org.jnode.vm.x86;

import org.vmmagic.pragma.Uninterruptible;

/**
 * Wrapper around a Model Specific Register.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class MSR implements Uninterruptible {

    /**
     * Number of the MSR
     */
    private final int id;

    /**
     * Last read value of the MSR
     */
    private long value;

    public MSR(int id) {
        this(id, 0);
    }

    public MSR(int id, long value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Gets the last known value of this MSR.
     *
     * @return Returns the value.
     */
    public final long getValue() {
        return value;
    }

    /**
     * Sets the value of this MSR.
     */
    public final void setValue(long value) {
        this.value = value;
    }
}
