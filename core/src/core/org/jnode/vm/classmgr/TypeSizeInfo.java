/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.classmgr;

import org.jnode.vm.JvmType;
import org.jnode.vm.VmSystemObject;

/**
 * This class provides information on the size of the various types for a
 * specific architecture.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TypeSizeInfo extends VmSystemObject {

    private final int intStackSlots;

    private final int floatStackSlots;

    private final int longStackSlots;

    private final int doubleStackSlots;

    private final int refStackSlots;

    /**
     * @param intStackSlots
     * @param floatStackSlots
     * @param longStackSlots
     * @param doubleStackSlots
     * @param refStackSlots
     */
    public TypeSizeInfo(int intStackSlots, int floatStackSlots,
            int longStackSlots, int doubleStackSlots, int refStackSlots) {
        this.intStackSlots = intStackSlots;
        this.floatStackSlots = floatStackSlots;
        this.longStackSlots = longStackSlots;
        this.doubleStackSlots = doubleStackSlots;
        this.refStackSlots = refStackSlots;
    }

    /**
     * Gets the number of stack slots an argument of the given type takes.
     * A slot is equal to the address size of the architecture.
     * 
     * @param jvmType
     * @return
     */
    public final int getStackSlots(int jvmType) {
        switch (jvmType) {
        case JvmType.BYTE:
        case JvmType.BOOLEAN:
        case JvmType.CHAR:
        case JvmType.SHORT:
        case JvmType.INT:
            return intStackSlots;
        case JvmType.FLOAT:
            return floatStackSlots;
        case JvmType.LONG:
            return longStackSlots;
        case JvmType.DOUBLE:
            return doubleStackSlots;
        case JvmType.REFERENCE:
            return refStackSlots;
        default:
            throw new IllegalArgumentException("Unknown JvmType " + jvmType);
        }
    }
}
