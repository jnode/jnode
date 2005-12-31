/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.jnode.vm.x86;

import org.jnode.vm.VmMultiMediaSupport;
import org.vmmagic.unboxed.Address;

/**
 * Multi media support class optimized using MMX instructions.
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
