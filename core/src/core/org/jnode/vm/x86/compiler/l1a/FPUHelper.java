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
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.FPU;
import org.jnode.vm.bytecode.StackException;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPUHelper implements X86CompilerConstants {

    /**
     * Swap ST0 and the given item. Action is emitted to code & performed on
     * fpuStack.
     *
     * @param os
     * @param fpuStack
     * @param item
     */
    static final void fxch(X86Assembler os, FPUStack fpuStack, Item item) {
        if (!fpuStack.isTos(item)) {
            final FPU fpuReg = fpuStack.getRegister(item);
            fxch(os, fpuStack, fpuReg);
        }
    }

    /**
     * Swap ST0 and fpuReg. Action is emitted to code & performed on fpuStack.
     *
     * @param os
     * @param fpuStack
     * @param fpuReg
     */
    static final void fxch(X86Assembler os, FPUStack fpuStack,
                           FPU fpuReg) {
        if (fpuReg == X86Register.ST0) {
            throw new StackException("Cannot fxch ST0");
        }
        os.writeFXCH(fpuReg);
        fpuStack.fxch(fpuReg);
    }
}
