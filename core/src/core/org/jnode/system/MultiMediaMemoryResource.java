/*
 * $Id$
 */
package org.jnode.system;

/**
 * An MM memory resource contains methods that are optimized
 * multi media application.
 *  
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface MultiMediaMemoryResource extends MemoryResource {

    /**
     * Merge 32-bit ARGB values at the given memory address.
     * @param src
     * @param srcOfs
     * @param dstPtr
     * @param length
     */
    public void setARGB32bpp(int[] src, int srcOfs, int dstPtr, int length);
    
}
