/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.x86;

import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.MagicPermission;
import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class TSS32 {

    private final int[] tss;
    byte[] kernelStack;    // Only here to keep a reference to the stack, to avoid garbage collection
    byte[] userStack;    // Only here to keep a reference to the stack, to avoid garbage collection

    /**
     * Initialize this instance.
     */
    public TSS32() {
        final int len = UnsafeX86.getTSS(null);
        this.tss = new int[len];
        UnsafeX86.getTSS(tss);
    }

    /**
     * Gets the address of the TSS.
     */
    public final Address getAddress() {
        return VmMagic.getArrayData(tss);
    }

    public final int[] getTSS() {
        return tss;
    }

    /**
     * Sets the kernel stack
     *
     * @param stack
     */
    public final void setKernelStack(byte[] stack) {
        this.kernelStack = stack;
        final Address stackBase = VmMagic.getArrayData(stack);
        final Address esp = stackBase.add(stack.length - 4);
        tss[1] = esp.toInt();
    }

    /**
     * Sets the user stack
     *
     * @param stack
     */
    public final void setUserStack(byte[] stack) {
        this.userStack = stack;
        final Address stackBase = VmMagic.getArrayData(stack);
        final Address esp = stackBase.add(stack.length - 4);
        tss[14] = esp.toInt();
    }
}
