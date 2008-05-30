/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm;

import org.jnode.system.MultiMediaMemoryResource;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;

/**
 * Implementation class for {@link org.jnode.system.MultiMediaMemoryResource}.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class MultiMediaMemoryResourceImpl extends MemoryResourceImpl implements
    MultiMediaMemoryResource {

    /**
     * The multi media support class
     */
    private final VmMultiMediaSupport multiMediaSupport;

    /**
     * @param parent
     * @param owner
     * @param start
     * @param size
     */
    public MultiMediaMemoryResourceImpl(MemoryResourceImpl parent,
                                        VmMultiMediaSupport mmSupport) {
        super(parent, parent.getOwner(), parent.getAddress(), parent.getSize());
        this.multiMediaSupport = mmSupport;
    }

    /**
     * @see org.jnode.system.MultiMediaMemoryResource#setARGB32bpp(int[], int, int, int)
     */
    public final void setARGB32bpp(int[] src, int srcOfs, int dstPtr, int length) {
        if (srcOfs < 0) {
            throw new IndexOutOfBoundsException("srcOfs < 0");
        }
        if (length < 0) {
            throw new IndexOutOfBoundsException("length < 0");
        }
        if (srcOfs + length > src.length) {
            throw new IndexOutOfBoundsException("dstOfs + length > dst.length");
        }
        testMemPtr(dstPtr, length * 4);
        final Address srcPtr = VmMagic.getArrayData(src).add(srcOfs * 4);
        multiMediaSupport.setARGB32bpp(srcPtr, start.add(dstPtr), length);
    }
}
