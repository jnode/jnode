/*
 * $Id$
 *
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
