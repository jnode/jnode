/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.compiler.GCMapIterator;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class X86GCMapIterator extends GCMapIterator {

    /** Address of the current stack frame */
    private Address framePtr;
    
    /**
     * @see org.jnode.vm.compiler.GCMapIterator#getNextReferenceAddress()
     */
    @Override
    public Address getNextReferenceAddress() {
        // TODO Implement me
        return Address.zero();
    }

    /**
     * @see org.jnode.vm.compiler.GCMapIterator#iterationComplete()
     */
    @Override
    public void iterationComplete() {
        this.framePtr = null;        
    }

    /**
     * @see org.jnode.vm.compiler.GCMapIterator#setupIteration(org.jnode.vm.classmgr.VmCompiledCode, org.vmmagic.unboxed.Offset, org.vmmagic.unboxed.Address)
     */
    @Override
    public void setupIteration(VmCompiledCode method, Offset instructionOffset, Address framePtr) {
        this.framePtr = framePtr;
        // TODO Implement me
        
    }

}
