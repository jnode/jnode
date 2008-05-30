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

package org.jnode.vm.x86;

import org.jnode.vm.VmMultiMediaSupport;
import org.vmmagic.unboxed.Address;

/**
 * Multi media support class optimized using MMX instructions.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MMXMultiMediaSupport extends VmMultiMediaSupport {

    /**
     * @see org.jnode.vm.VmMultiMediaSupport#setARGB32bpp(org.vmmagic.unboxed.Address, org.vmmagic.unboxed.Address, int)
     */
    public final void setARGB32bpp(Address src, Address dst, int length) {
        UnsafeX86.setARGB32bppMMX(src, dst, length);
    }
}
