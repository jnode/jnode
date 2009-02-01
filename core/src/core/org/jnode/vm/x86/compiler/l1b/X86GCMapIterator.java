/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.x86.compiler.l1b;

import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.compiler.GCMapIterator;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class X86GCMapIterator extends GCMapIterator {

    /** Address of the current stack frame */
//    private Address framePtr;

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
//        this.framePtr = null;        
    }

    /**
     * @see org.jnode.vm.compiler.GCMapIterator#setupIteration(org.jnode.vm.classmgr.VmCompiledCode,
     * org.vmmagic.unboxed.Offset, org.vmmagic.unboxed.Address)
     */
    @Override
    public void setupIteration(VmCompiledCode method, Offset instructionOffset, Address framePtr) {
//        this.framePtr = framePtr;
        // TODO Implement me

    }

}
