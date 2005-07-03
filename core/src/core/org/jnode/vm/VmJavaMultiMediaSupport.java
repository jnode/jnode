/*
 * $Id$
 */
package org.jnode.vm;

import org.vmmagic.unboxed.Address;

/**
 * The java implementation of {@link org.jnode.vm.VmMultiMediaSupport}.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class VmJavaMultiMediaSupport extends VmMultiMediaSupport {

    /**
     * @see org.jnode.vm.VmMultiMediaSupport#setARGB32bpp(org.vmmagic.unboxed.Address, org.vmmagic.unboxed.Address, int)
     */
    public void setARGB32bpp(Address src, Address dst, int length) {
        for (int i = length; i > 0; i--) {
            final int c = src.loadInt();
            final int alpha = (c >>> 24) & 0xFF;
            
            if (alpha != 0) {
                final int d = dst.loadInt();
                final int c1 = c & 0xFF;
                final int c2 = (c >> 8) & 0xFF;
                final int c3 = (c >> 16) & 0xFF;
                final int d1 = d & 0xFF;
                final int d2 = (d >> 8) & 0xFF;
                final int d3 = (d >> 16) & 0xFF;
                final int r1 = (((alpha * (c1 - d1)) >> 8) + d1) & 0xFF;
                final int r2 = (((alpha * (c2 - d2)) >> 8) + d2) & 0xFF;
                final int r3 = (((alpha * (c3 - d3)) >> 8) + d3) & 0xFF;
                dst.store(r1 | (r2 << 8) | (r3 << 16));
            }
            src = src.add(4);
            dst = dst.add(4);
        }
    }
}
