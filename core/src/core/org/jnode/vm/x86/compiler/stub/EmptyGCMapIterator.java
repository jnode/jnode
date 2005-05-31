/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.stub;

import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.compiler.GCMapIterator;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class EmptyGCMapIterator extends GCMapIterator {

    /**
     * @see org.jnode.vm.compiler.GCMapIterator#getNextReferenceAddress()
     */
    @Override
    public Address getNextReferenceAddress() {
        return Address.zero();
    }

    /**
     * @see org.jnode.vm.compiler.GCMapIterator#iterationComplete()
     */
    @Override
    public void iterationComplete() {
        // Do nothing
    }

    /**
     * @see org.jnode.vm.compiler.GCMapIterator#setupIteration(org.jnode.vm.classmgr.VmCompiledCode, org.vmmagic.unboxed.Offset, org.vmmagic.unboxed.Address)
     */
    @Override
    public void setupIteration(VmCompiledCode method, Offset instructionOffset, Address framePtr) {
        // Do nothing
    }
}
