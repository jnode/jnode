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
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 *         <p/>
 *         IntItems are items with type INT
 */

final class IntItem extends WordItem implements X86CompilerConstants {

    private int value;

    final void initialize(EmitterContext ec, byte kind, short offsetToFP, X86Register reg, int value) {
        super.initialize(ec, kind, reg, offsetToFP);
        this.value = value;
    }

    IntItem(ItemFactory factory) {
        super(factory);
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
     */
    protected WordItem cloneConstant(EmitterContext ec) {
        return factory.createIConst(ec, getValue());
    }

    /**
     * Get the JVM type of this item
     *
     * @return the JVM type
     */
    final int getType() {
        return JvmType.INT;
    }

    final int getValue() {
        if (Vm.VerifyAssertions) {
            Vm._assert(isConstant(), "kind == Kind.CONSTANT");
        }
        return value;
    }

    /**
     * Load my constant to the given os.
     *
     * @param os
     * @param reg
     */
    protected void loadToConstant(EmitterContext ec, X86Assembler os,
                                  GPR reg) {
        if (value != 0) {
            os.writeMOV_Const(reg, value);
        } else {
            os.writeXOR(reg, reg);
        }
    }

    /**
     * Pop the top of the FPU stack into the given memory location.
     *
     * @param os
     * @param reg
     * @param disp
     */
    protected void popFromFPU(X86Assembler os, GPR reg, int disp) {
        os.writeFISTP32(reg, disp);
    }

    /**
     * Push my constant on the stack using the given os.
     *
     * @param os
     */
    protected void pushConstant(EmitterContext ec, X86Assembler os) {
        os.writePUSH(value);
    }

    /**
     * Push the given memory location on the FPU stack.
     *
     * @param os
     * @param reg
     * @param disp
     */
    protected void pushToFPU(X86Assembler os, GPR reg, int disp) {
        os.writeFILD32(reg, disp);
    }
}
