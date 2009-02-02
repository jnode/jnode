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
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;

/**
 * @author Patrik Reali
 */
final class DoubleItem extends DoubleWordItem {

    private double value;

    /**
     * Initialize a blank item.
     */
    DoubleItem(ItemFactory factory) {
        super(factory);
    }

    /**
     * @param kind
     * @param offsetToFP
     * @param value
     */
    final void initialize(EmitterContext ec, byte kind, short offsetToFP, X86Register.GPR lsb,
                          X86Register.GPR msb, X86Register.GPR64 reg, X86Register.XMM xmm,
                          double value) {
        super.initialize(ec, kind, offsetToFP, lsb, msb, reg, xmm);
        this.value = value;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.DoubleWordItem#cloneConstant()
     */
    protected DoubleWordItem cloneConstant(EmitterContext ec) {
        return factory.createDConst(ec, getValue());
    }

    /**
     * Get the JVM type of this item
     *
     * @return the JVM type
     */
    final int getType() {
        return JvmType.DOUBLE;
    }

    /**
     * Gets the constant value.
     *
     * @return
     */
    final double getValue() {
        if (Vm.VerifyAssertions) {
            Vm._assert(isConstant(), "kind == Kind.CONSTANT");
        }
        return value;
    }

    /**
     * Load my constant to the given os in 32-bit mode.
     *
     * @param os
     * @param lsb
     * @param msb
     */
    protected final void loadToConstant32(EmitterContext ec, X86Assembler os,
                                          GPR32 lsb, GPR32 msb) {
        final long lvalue = Double.doubleToLongBits(value);
        final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
        final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);

        os.writeMOV_Const(lsb, lsbv);
        os.writeMOV_Const(msb, msbv);
    }

    /**
     * Load my constant to the given os in 64-bit mode.
     *
     * @param os
     * @param reg
     */
    protected final void loadToConstant64(EmitterContext ec, X86Assembler os,
                                          GPR64 reg) {
        final long lvalue = Double.doubleToLongBits(value);
        os.writeMOV_Const(reg, lvalue);
    }

    /**
     * Pop the top of the FPU stack into the given memory location.
     *
     * @param os
     * @param reg
     * @param disp
     */
    protected void popFromFPU(X86Assembler os, GPR reg, int disp) {
        os.writeFSTP64(reg, disp);
    }

    /**
     * Push my constant on the stack using the given os.
     *
     * @param os
     */
    protected final void pushConstant(EmitterContext ec, X86Assembler os) {
        final long lvalue = Double.doubleToLongBits(value);
        final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
        final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
        if (os.isCode32()) {
            os.writePUSH(msbv);
            os.writePUSH(lsbv);
        } else {
            os.writeLEA(X86Register.RSP, X86Register.RSP, -8);
            os.writeMOV_Const(BITS32, X86Register.RSP, LSB, lsbv);
            os.writeMOV_Const(BITS32, X86Register.RSP, MSB, msbv);
        }
    }

    /**
     * Push the given memory location on the FPU stack.
     *
     * @param os
     * @param reg
     * @param disp
     */
    protected void pushToFPU(X86Assembler os, GPR reg, int disp) {
        os.writeFLD64(reg, disp);
    }
}
