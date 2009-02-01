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

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR32;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class LongItem extends DoubleWordItem implements X86CompilerConstants {

    private long value;

    /**
     * Initialize a blank item.
     */
    LongItem(ItemFactory factory) {
        super(factory);
    }

    /**
     * @param kind
     * @param offsetToFP
     * @param lsb
     * @param msb
     * @param val
     */
    final void initialize(EmitterContext ec, byte kind, short offsetToFP, X86Register.GPR lsb,
                          X86Register.GPR msb, X86Register.GPR64 reg, X86Register.XMM xmm,
                          long val) {
        super.initialize(ec, kind, offsetToFP, lsb, msb, reg, xmm);
        this.value = val;
    }

    /**
     * Load my constant to the given os in 32-bit mode.
     *
     * @param os
     * @param reg
     */
    protected final void loadToConstant32(EmitterContext ec, X86Assembler os,
                                          GPR32 lsb, GPR32 msb) {

        if (value != 0) {
            final int lsbv = (int) (value & 0xFFFFFFFFL);
            final int msbv = (int) ((value >>> 32) & 0xFFFFFFFFL);

            os.writeMOV_Const(lsb, lsbv);
            os.writeMOV_Const(msb, msbv);
        } else {
            os.writeXOR(lsb, lsb);
            os.writeXOR(msb, msb);
        }
    }

    /**
     * Load my constant to the given os in 64-bit mode.
     *
     * @param os
     * @param reg
     */
    protected final void loadToConstant64(EmitterContext ec, X86Assembler os,
                                          GPR64 reg) {
        if (value == 0) {
            os.writeXOR(reg, reg);
        } else {
            os.writeMOV_Const(reg, value);
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
        os.writeFISTP64(reg, disp);
    }

    /**
     * Push my constant on the stack using the given os.
     *
     * @param os
     */
    protected final void pushConstant(EmitterContext ec, X86Assembler os) {
        if (os.isCode32()) {
            os.writePUSH(getMsbValue());
            os.writePUSH(getLsbValue());
        } else {
            os.writeLEA(X86Register.RSP, X86Register.RSP, -8);
            os.writeMOV_Const(BITS32, X86Register.RSP, LSB, getLsbValue());
            os.writeMOV_Const(BITS32, X86Register.RSP, MSB, getMsbValue());
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
        os.writeFILD64(reg, disp);
    }

    /**
     * Gets the LSB part of the constant value of this item.
     *
     * @return
     */
    final int getLsbValue() {
        if (Vm.VerifyAssertions) {
            Vm._assert(isConstant(), "kind == Kind.CONSTANT");
        }
        return (int) (value & 0xFFFFFFFFL);
    }

    /**
     * Gets the MSB part of the constant value of this item.
     *
     * @return
     */
    final int getMsbValue() {
        if (Vm.VerifyAssertions) {
            Vm._assert(isConstant(), "kind == Kind.CONSTANT");
        }
        return (int) ((value >>> 32) & 0xFFFFFFFFL);
    }

    /**
     * Get the JVM type of this item
     *
     * @return the JVM type
     */
    final int getType() {
        return JvmType.LONG;
    }

    /**
     * Gets the constant value of this item.
     *
     * @return
     */
    final long getValue() {
        if (Vm.VerifyAssertions) {
            Vm._assert(isConstant(), "kind == Kind.CONSTANT");
        }
        return value;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.DoubleWordItem#cloneConstant()
     */
    protected DoubleWordItem cloneConstant(EmitterContext ec) {
        return factory.createLConst(ec, getValue());
    }
}
