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
 
package org.jnode.vm.classmgr;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmConstString extends VmConstObject implements VmSharedStaticsEntry {

    /**
     * Index in the statics table of the string constant.
     */
    private final int staticsIndex;

    /**
     * Initialize this instance.
     *
     * @param staticsIndex
     */
    VmConstString(int staticsIndex) {
        this.staticsIndex = staticsIndex;
    }

    /**
     * Gets the index in the statics table of the string constants.
     *
     * @return Returns the staticsIndex.
     */
    public final int getSharedStaticsIndex() {
        return this.staticsIndex;
    }

    /**
     * @see org.jnode.vm.classmgr.VmConstObject#getConstType()
     */
    public final int getConstType() {
        return CONST_STRING;
    }
}
