/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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
package org.jnode.vm;

import org.jnode.vm.classmgr.VmClassType;

/**
 * Class containing "magic" methods that are interpreted by the VM itself,
 * instead of being executed as normal java methods.
 * 
 * Methods is this class can also be called from inside JNode.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmMagic {

    /**
     * Gets the VmType of the given object.
     * @param object
     */
    public static VmClassType getObjectType(Object object) {
        return null;
    }
    
    /**
     * Gets the Type Information Block of the given object.
     * 
     * @param object
     * @return TIB
     */
    public static Object[] getTIB(Object object) {
        return null;
    }

    /**
     * Gets all of the flags of the given object.
     * 
     * @param object
     * @return int
     */
    public static int getObjectFlags(Object object) {
        return 0;
    }

    /**
     * Gets the GC color flags of the given object.
     * 
     * @param object
     * @return int
     */
    public static int getObjectColor(Object object) {
        return 0;
    }

    /**
     * Sets all of the flags of the given object.
     * 
     * @param object
     * @param flags
     */
    public static void setObjectFlags(Object object, int flags) {
        
    }

    /**
     * Has the given object been finalized.
     * @param src
     */
    public static boolean isFinalized(Object src) {
        return false;
    }
    
    /**
     * Do not instantiate this class.
     */
    private VmMagic() {}
}
