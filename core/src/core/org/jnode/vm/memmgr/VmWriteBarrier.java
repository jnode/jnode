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
 
package org.jnode.vm.memmgr;

import org.jnode.vm.VmSystemObject;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmWriteBarrier extends VmSystemObject {

    /**
     * This method is inlined to implement the write barrier for aastores
     *
     * @param ref   The base pointer of the array
     * @param index The array index being stored into.  NOTE: This is the "natural" index; a[3] will pass 3.
     * @param value The value being stored
     */
    public abstract void arrayStoreWriteBarrier(Object ref, int index, Object value)
        throws UninterruptiblePragma;

    /**
     * This method implements the write barrier for putfields of references
     *
     * @param ref    The base pointer of the array
     * @param offset The offset being stored into.  NOTE: This is in bytes.
     * @param value  The value being stored
     */
    public abstract void putfieldWriteBarrier(Object ref, int offset, Object value)
        throws UninterruptiblePragma;

    /**
     * This method is inlined to implement the write barrier for putstatics of references
     *
     * @param shared       Is this a shared static field
     * @param staticsIndex The offset of static field (in VmSharedStatics or VmIsolatedStatics)
     * @param value        The value being stored
     */
    public abstract void putstaticWriteBarrier(boolean shared, int staticsIndex, Object value)
        throws UninterruptiblePragma;

    /**
     * This method generates write barrier entries needed as a consequence of
     * an explicit user array copies.
     *
     * @param array The referring (source) array.
     * @param start The first "natural" index into the array (e.g. for
     *              <code>a[1]</code>, index = 1).
     * @param end   The last "natural" index into the array
     */
    public abstract void arrayCopyWriteBarrier(Object array, int start, int end)
        throws UninterruptiblePragma;
}
