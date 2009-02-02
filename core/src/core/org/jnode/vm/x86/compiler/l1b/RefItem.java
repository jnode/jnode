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
 
package org.jnode.vm.x86.compiler.l1b;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class RefItem extends WordItem implements X86CompilerConstants {

    // generate unique labels for writeStatics (should use current label)
    private long labelCounter;

    private VmConstString value;

    /**
     * Initialize a blank item
     */
    RefItem(ItemFactory factory) {
        super(factory);
    }

    final void initialize(EmitterContext ec, byte kind, short offsetToFP, X86Register reg, VmConstString val) {
        super.initialize(ec, kind, reg, offsetToFP);
        this.value = val;
    }

    /**
     * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
     */
    protected WordItem cloneConstant(EmitterContext ec) {
        return factory.createAConst(ec, getValue());
    }

    /**
     * Get the JVM type of this item
     *
     * @return the JVM type
     */
    int getType() {
        return JvmType.REFERENCE;
    }

    /**
     * Gets the value of this reference. Item must have a CONSTANT kind.
     *
     * @return
     */
    VmConstString getValue() {
        if (Vm.VerifyAssertions) Vm._assert(getKind() == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return value;
    }

    /**
     * Is the value of this item null.
     * Item must have a CONSTANT kind.
     *
     * @return
     */
    boolean isNull() {
        if (Vm.VerifyAssertions) Vm._assert(getKind() == Kind.CONSTANT, "kind == Kind.CONSTANT");
        return (value == null);
    }

    /**
     * Load my constant to the given os.
     *
     * @param os
     * @param reg
     */
    protected void loadToConstant(EmitterContext ec, X86Assembler os,
                                  GPR reg) {
        if (value == null) {
            os.writeXOR(reg, reg);
        } else {
            X86CompilerHelper helper = ec.getHelper();
            final Label l = new Label(Long.toString(labelCounter++));
            if (os.isCode32()) {
                helper.writeGetStaticsEntry(l, reg, value);
            } else {
                helper.writeGetStaticsEntry64(l, (GPR64) reg, value);
            }
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
        notImplemented();
    }

    /**
     * Push my constant on the stack using the given os.
     *
     * @param os
     */
    protected void pushConstant(EmitterContext ec, X86Assembler os) {
        if (value == null) {
            // Push const not supported in 64-bit mode,
            // but PUSH imm32 will sign-extend to 64-bit
            os.writePUSH(0);
        } else {
            X86CompilerHelper helper = ec.getHelper();
            Label l = new Label(Long.toString(labelCounter++));
            helper.writePushStaticsEntry(l, value);
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
        notImplemented();
    }

}
