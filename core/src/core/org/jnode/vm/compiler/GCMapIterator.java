/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.vm.classmgr.VmCompiledCode;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class GCMapIterator {

    /**
     * Prepare this iterator to iterate over the references found in the 
     * stack frame of the given compiled method.
     * 
     * @param method
     * @param instructionOffset
     * @param framePtr
     */
    public abstract void setupIteration(VmCompiledCode method, Offset instructionOffset, Address framePtr);
    
    /**
     * Gets the address of the next object reference on the stack frame
     * of the current method.
     * 
     * @return Address.zero() is no other object references are available.
     */
    public abstract Address getNextReferenceAddress();
    
    /**
     * Called after the iteration.
     * Cleanup any references that are only needed during an iteration.
     */
    public abstract void iterationComplete();
}
