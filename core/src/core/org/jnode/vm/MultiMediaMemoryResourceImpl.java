/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.system.MultiMediaMemoryResource;
import org.vmmagic.unboxed.Address;

/**
 * Implementation class for {@link org.jnode.system.MultiMediaMemoryResource}.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class MultiMediaMemoryResourceImpl extends MemoryResourceImpl implements
        MultiMediaMemoryResource {

    /** The multi media support class */
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
