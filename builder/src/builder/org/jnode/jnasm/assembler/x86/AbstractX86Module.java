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
 
package org.jnode.jnasm.assembler.x86;

import java.util.List;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.jnasm.assembler.Address;
import org.jnode.jnasm.assembler.AssemblerModule;
import org.jnode.jnasm.assembler.Register;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class AbstractX86Module extends AssemblerModule {
    static final int NUL_ARG = 0;
    static final int CON_ARG = 1;
    static final int REG_ARG = 2;
    static final int REL_ARG = 3;
    static final int ABS_ARG = 4;
    static final int SCL_ARG = 5;
    static final int ZSC_ARG = 6;
    static final int SEG_ARG = 7;
    static final String[] ARG_TYPES = {
        "noargument", "constant", "register", "relative", "absolute", "scaled", "simplescaled", "segment"
    };
    static final int DISP = 3;
    static final int DISP_MASK = ((2 << (DISP - 1)) - 1);
    static final int N_ADDR = NUL_ARG;
    static final int C_ADDR = CON_ARG;
    static final int R_ADDR = REG_ARG;
    static final int RR_ADDR = REG_ARG | REG_ARG << DISP;
    static final int RRC_ADDR = REG_ARG | REG_ARG << DISP | CON_ARG << 2 * DISP;
    static final int RC_ADDR = REG_ARG | CON_ARG << DISP;
    static final int RE_ADDR = REG_ARG | REL_ARG << DISP;
    static final int RA_ADDR = REG_ARG | ABS_ARG << DISP;
    static final int RS_ADDR = REG_ARG | SCL_ARG << DISP;
    static final int RZ_ADDR = REG_ARG | ZSC_ARG << DISP;
    static final int CR_ADDR = CON_ARG | REG_ARG << DISP;
    static final int E_ADDR = REL_ARG;
    static final int ER_ADDR = REL_ARG | REG_ARG << DISP;
    static final int EC_ADDR = REL_ARG | CON_ARG << DISP;
    static final int A_ADDR = ABS_ARG;
    static final int AC_ADDR = ABS_ARG | CON_ARG << DISP;
    static final int AR_ADDR = ABS_ARG | REG_ARG << DISP;
    static final int S_ADDR = SCL_ARG;
    static final int SR_ADDR = SCL_ARG | REG_ARG << DISP;
    static final int G_ADDR = SEG_ARG;
    static final int GR_ADDR = SEG_ARG | REG_ARG << DISP;
    static final int GC_ADDR = SEG_ARG | CON_ARG << DISP;
    static final int RG_ADDR = REG_ARG | SEG_ARG << DISP;
    final Object[] args = new Object[3];
    List<Object> operands;
    int operandSize;
    X86Assembler stream;

    AbstractX86Module(Map<String, Label> labels, Map<String, Integer> constants) {
        super(labels, constants);
    }

    public void setNativeStream(NativeStream stream) {
        this.stream = (X86Assembler) stream;
    }

    abstract String[] getMnemonics();

    int getAddressingMode(int maxArgs) {
        int ret = N_ADDR;
        if (maxArgs > 3) {
            throw new Error("Invalid number of arguments: " + maxArgs);
        }

        for (int i = 0; i < maxArgs; i++) {
            try {
                if (operands == null) break;

                Object o = operands.get(i);
                if (o == null) break;

                if (o instanceof Integer) {
                    ret |= CON_ARG << DISP * i;
                } else if (o instanceof Register) {
                    ret |= REG_ARG << DISP * i;
                } else if (o instanceof Address) {
                    Address ind = (Address) o;
                    if (ind.segment) {
                        ret |= SEG_ARG << DISP * i;
                    } else if (ind.reg != null && ind.sreg != null) {
                        ret |= SCL_ARG << DISP * i;
                    } else if (ind.reg != null && ind.sreg == null) {
                        ret |= REL_ARG << DISP * i;
                    } else if (ind.reg == null && ind.sreg != null) {
                        ret |= ZSC_ARG << DISP * i;
                    } else if (ind.reg == null && ind.sreg == null) {
                        ret |= ABS_ARG << DISP * i;
                    } else {
                        throw new IllegalArgumentException("Unknown indirect: " + ind);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown operand: " + o + " " + o.getClass().getName());
                }

                args[i] = o;

            } catch (IndexOutOfBoundsException x) {
                break;
            }
        }
        return ret;
    }

    final int getInt(int i) {
        return ((Integer) args[i]).intValue();
    }

    final X86Register.GPR getReg(int i) {
        return getRegister(((Register) args[i]).name);
    }

    final Address getAddress(int i) {
        return (Address) args[i];
    }

    final void reportAddressingError(int instruction, int addressing) {
        String err = "";
        int ad = addressing;
        do {
            err += " " + ARG_TYPES[ad & DISP_MASK];
            ad >>= DISP;
        } while (ad != 0);

        throw new IllegalArgumentException(
            "Unknown addressing mode " + addressing + " (" + err + " ) for " + getMnemonics()[instruction]);
    }

    static final X86Register.GPR getRegister(String name) {
        return X86Register.getGPR(name);
    }

    static final X86Register.MMX getRegisterMMX(String name) {
        return (X86Register.MMX) X86Register.getRegister(name);
    }
}
