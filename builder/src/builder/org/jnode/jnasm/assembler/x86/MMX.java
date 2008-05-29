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

package org.jnode.jnasm.assembler.x86;

import java.util.List;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Register;
import org.jnode.jnasm.assembler.Address;
import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.jnasm.assembler.Register;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class MMX extends AbstractX86Module {

    public static final int EMMS_ISN = 0;
    public static final int MOVD_ISN = EMMS_ISN + 1;
    public static final int MOVQ_ISN = MOVD_ISN + 1;
    public static final int PACKUSWB_ISN = MOVQ_ISN + 1;
    public static final int PADDW_ISN = PACKUSWB_ISN + 1;
    public static final int PAND_ISN = PADDW_ISN + 1;
    public static final int PCMPGTW_ISN = PAND_ISN + 1;
    public static final int PMULLW_ISN = PCMPGTW_ISN + 1;
    public static final int PSHUFW_ISN = PMULLW_ISN + 1;
    public static final int PSRLW_ISN = PSHUFW_ISN + 1;
    public static final int PSUBW_ISN = PSRLW_ISN + 1;
    public static final int PUNPCKLBW_ISN = PSUBW_ISN + 1;
    public static final int PXOR_ISN = PUNPCKLBW_ISN + 1;

    protected static final Map<String, Integer> INSTRUCTION_MAP;
    private static final String[] MNEMONICS;

    static {
        Map<String, Integer> map = InstructionUtils.getInstructionMap(MMX.class);
        String[] mnemonics = InstructionUtils.getMnemonicArray(map);
        INSTRUCTION_MAP = map;
        MNEMONICS = mnemonics;
    }

    public MMX(Map<String, Label> labels, Map<String, Integer> constants) {
        super(labels, constants);
    }

    String[] getMnemonics() {
        return MNEMONICS;
    }

    public boolean emit(String mnemonic, List<Object> operands, int operandSize) {
        this.operands = operands;
        this.operandSize = operandSize;
        Integer key = (Integer) INSTRUCTION_MAP.get(mnemonic);

        if (key == null) return false;

        switch (key.intValue()) {
            case EMMS_ISN:
                emitEMMS();
                break;
            case MOVD_ISN:
                emitMOVD();
                break;
            case MOVQ_ISN:
                emitMOVQ();
                break;
            case PACKUSWB_ISN:
                emitPACKUSWB();
                break;
            case PADDW_ISN:
                emitPADDW();
                break;
            case PAND_ISN:
                emitPAND();
                break;
            case PCMPGTW_ISN:
                emitPCMPGTW();
                break;
            case PMULLW_ISN:
                emitPMULLW();
                break;
            case PSHUFW_ISN:
                emitPSHUFW();
                break;
            case PSRLW_ISN:
                emitPSRLW();
                break;
            case PSUBW_ISN:
                emitPSUBW();
                break;
            case PUNPCKLBW_ISN:
                emitPUNPCKLBW();
                break;
            case PXOR_ISN:
                emitPXOR();
                break;
            default:
                throw new Error("Invalid instruction binding " + key.intValue() + " for " + mnemonic);

        }
        return true;
    }

    private void emitEMMS() {
        stream.writeEMMS();
    }

    private void emitMOVD() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RE_ADDR:
                X86Register.MMX r = getRegMMX(0);
                Address ind = getAddress(1);
                stream.writeMOVD(operandSize, r, getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                r = getRegMMX(1);
                stream.writeMOVD(operandSize, getRegister(ind.getImg()), ind.disp, r);
                break;
            default:
                reportAddressingError(MOVD_ISN, addr);
        }
    }

    private void emitMOVQ() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writeMOVQ(r1, r2);
                break;
            case RE_ADDR:
                X86Register.MMX r = getRegMMX(0);
                Address ind = getAddress(1);
                stream.writeMOVQ(operandSize, r, getRegister(ind.getImg()), ind.disp);
                break;
            case RA_ADDR:
                r = getRegMMX(0);
                ind = getAddress(1);
                stream.writeMOVQ(operandSize, r, ind.disp);
                break;
            default:
                reportAddressingError(MOVQ_ISN, addr);
        }
    }

    private void emitPACKUSWB() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePACKUSWB(r1, r2);
                break;
            default:
                reportAddressingError(PACKUSWB_ISN, addr);
        }
    }

    private void emitPADDW() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePADDW(r1, r2);
                break;
            default:
                reportAddressingError(PADDW_ISN, addr);
        }
    }

    private void emitPAND() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePAND(r1, r2);
                break;
            default:
                reportAddressingError(PAND_ISN, addr);
        }
    }

    private void emitPCMPGTW() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePCMPGTW(r1, r2);
                break;
            default:
                reportAddressingError(PCMPGTW_ISN, addr);
        }
    }

    private void emitPMULLW() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePMULLW(r1, r2);
                break;
            default:
                reportAddressingError(PMULLW_ISN, addr);
        }
    }

    private void emitPSHUFW() {
        int addr = getAddressingMode(3);
        switch (addr) {
            case RRC_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePSHUFW(r1, r2, getInt(2));
                break;
            default:
                reportAddressingError(PSHUFW_ISN, addr);
        }
    }

    private void emitPSRLW() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RC_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                stream.writePSRLW(r1, getInt(1));
                break;
            default:
                reportAddressingError(PSRLW_ISN, addr);
        }
    }

    private void emitPSUBW() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePSUBW(r1, r2);
                break;
            default:
                reportAddressingError(PSUBW_ISN, addr);
        }
    }

    private void emitPUNPCKLBW() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePUNPCKLBW(r1, r2);
                break;
            default:
                reportAddressingError(PUNPCKLBW_ISN, addr);
        }
    }

    private void emitPXOR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register.MMX r1 = getRegMMX(0);
                X86Register.MMX r2 = getRegMMX(1);
                stream.writePXOR(r1, r2);
                break;
            default:
                reportAddressingError(PXOR_ISN, addr);
        }
    }

    final X86Register.MMX getRegMMX(int i) {
        return getRegisterMMX(((Register) args[i]).name);
    }
}
