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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.FPU;
import org.jnode.vm.bytecode.StackException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPUStack extends ItemStack {

    private static final FPU[] REGS = {X86Register.ST0, X86Register.ST1,
        X86Register.ST2, X86Register.ST3, X86Register.ST4, X86Register.ST5,
        X86Register.ST6, X86Register.ST7};

    /**
     * Initialize this instance.
     */
    FPUStack() {
        super(Item.Kind.FPUSTACK, REGS.length);
    }

    /**
     * Gets the FPU register that contains the given item.
     *
     * @param item
     * @return
     * @see org.jnode.assembler.x86.X86Register#ST1
     */
    final FPU getRegister(Item item) {
        for (int i = 0; i < tos; i++) {
            if (stack[tos - (i + 1)] == item) {
                return REGS[i];
            }
        }
        throw new StackException("Item not found on FPU stack");
    }

    /**
     * Gets the item that is contained in the given register.
     */
    final Item getItem(FPU fpuReg) {
        final int idx = tos - (fpuReg.getNr() + 1);
        return stack[idx];
    }

    /**
     * Swap the top of the stack (ST0) with the given FPU reg.
     *
     * @param fpuReg
     */
    final void fxch(FPU fpuReg) {
        final int idx1 = tos - 1;
        final int idx2 = tos - (fpuReg.getNr() + 1);
        final Item tmp = stack[idx1];
        stack[idx1] = stack[idx2];
        stack[idx2] = tmp;
    }
}
