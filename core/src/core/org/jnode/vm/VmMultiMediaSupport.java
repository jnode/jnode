/*
 * $Id$
 */
package org.jnode.vm;

import org.vmmagic.unboxed.Address;

/**
 * Class used to implement multi media support for
 * {@link org.jnode.system.MultiMediaMemoryResource}.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmMultiMediaSupport {

    /**
     * Merge 32-bit ARGB values at the given memory address.
     * @param src The source address (points to 32-bit ARGB int's)
     * @param dst The destination address (points to 32-bit RGB int's)
     * @param length The number of 32-bit int's to merge.
     */
    public abstract void setARGB32bpp(Address src, Address dst, int length);
}
