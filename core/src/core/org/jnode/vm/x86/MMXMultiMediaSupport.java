/*
 * $Id$
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
