/*
 * $Id$
 *
 * Copyright (C) 2003-2016 JNode.org
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
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.jnasm.assembler.Address;
import org.jnode.jnasm.assembler.Instruction;
import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.jnasm.assembler.Register;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class FPU extends AbstractX86Module {

    public static final int FADDP_ISN = 0;
    public static final int FCHS_ISN = FADDP_ISN + 1;
    public static final int FDIVP_ISN = FCHS_ISN + 1;
    public static final int FILD_ISN = FDIVP_ISN + 1;
    public static final int FISTP_ISN = FILD_ISN + 1;
    public static final int FLD_ISN = FISTP_ISN + 1;
    public static final int FMULP_ISN = FLD_ISN + 1;
    public static final int FNSTSW_ISN = FMULP_ISN + 1;
    public static final int FPREM_ISN = FNSTSW_ISN + 1;
    public static final int FSTP_ISN = FPREM_ISN + 1;
    public static final int FSUBP_ISN = FSTP_ISN + 1;
    public static final int FUCOMPP_ISN = FSUBP_ISN + 1;
    public static final int FXCH_ISN = FUCOMPP_ISN + 1;

    protected static final Map<String, Integer> INSTRUCTION_MAP;
    private static final String[] MNEMONICS;

    static {
        Map<String, Integer> map = InstructionUtils.getInstructionMap(FPU.class);
        String[] mnemonics = InstructionUtils.getMnemonicArray(map);
        INSTRUCTION_MAP = map;
        MNEMONICS = mnemonics;
    }

    public FPU(Map<String, Label> labels, Map<String, Integer> constants) {
        super(labels, constants);
    }

    String[] getMnemonics() {
        return MNEMONICS;
    }

    public boolean emit(String mnemonic, List<Object> operands, int operandSize, Instruction instruction) {
        this.operands = operands;
        this.operandSize = operandSize;
        Integer key = INSTRUCTION_MAP.get(mnemonic);

        if (key == null) return false;

        switch (key) {
            case FADDP_ISN:
                emitFADDP();
                break;
            case FCHS_ISN:
                emitFCHS();
                break;
            case FDIVP_ISN:
                emitFDIVP();
                break;
            case FILD_ISN:
                emitFILD();
                break;
            case FISTP_ISN:
                emitFISTP();
                break;
            case FLD_ISN:
                emitFLD();
                break;
            case FMULP_ISN:
                emitFMULP();
                break;
            case FNSTSW_ISN:
                emitFNSTSW();
                break;
            case FPREM_ISN:
                emitFPREM();
                break;
            case FSTP_ISN:
                emitFSTP();
                break;
            case FSUBP_ISN:
                emitFSUBP();
                break;
            case FUCOMPP_ISN:
                emitFUCOMPP();
                break;
            case FXCH_ISN:
                emitFXCH();
                break;
            default:
                throw new Error("Invalid instruction binding " + key + " for " + mnemonic);

        }
        return true;
    }

    private void emitFADDP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.FPU r = getRegFPU(0);
                stream.writeFADDP(r);
                break;
            default:
                reportAddressingError(FADDP_ISN, addr);
        }
    }

    private void emitFCHS() {
        stream.writeFCHS();
    }

    private void emitFDIVP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.FPU r = getRegFPU(0);
                stream.writeFDIVP(r);
                break;
            default:
                reportAddressingError(FDIVP_ISN, addr);
        }
    }

    private void emitFILD() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                if (operandSize == X86Constants.BITS32) {
                    stream.writeFILD32(getRegister(ind.getImg()), ind.disp);
                } else if (operandSize == X86Constants.BITS64) {
                    stream.writeFILD64(getRegister(ind.getImg()), ind.disp);
                } else {
                    throw new IllegalArgumentException(
                        "Illegal operand size " + operandSize + " for " + getMnemonics()[FILD_ISN]);
                }
                break;
            default:
                reportAddressingError(FILD_ISN, addr);
        }
    }

    private void emitFISTP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                if (operandSize == X86Constants.BITS32) {
                    stream.writeFISTP32(getRegister(ind.getImg()), ind.disp);
                } else if (operandSize == X86Constants.BITS64) {
                    stream.writeFISTP64(getRegister(ind.getImg()), ind.disp);
                } else {
                    throw new IllegalArgumentException(
                        "Illegal operand size " + operandSize + " for " + getMnemonics()[FISTP_ISN]);
                }
                break;
            default:
                reportAddressingError(FISTP_ISN, addr);
        }
    }

    private void emitFLD() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                if (operandSize == X86Constants.BITS32) {
                    stream.writeFLD32(getRegister(ind.getImg()), ind.disp);
                } else if (operandSize == X86Constants.BITS64) {
                    stream.writeFLD64(getRegister(ind.getImg()), ind.disp);
                } else {
                    throw new IllegalArgumentException(
                        "Illegal operand size " + operandSize + " for " + getMnemonics()[FLD_ISN]);
                }
                break;
            case S_ADDR:
                ind = getAddress(0);
                if (operandSize == X86Constants.BITS32) {
                    stream.writeFLD32(getRegister(ind.getImg()), getRegister(ind.sreg), ind.scale, ind.disp);
                } else if (operandSize == X86Constants.BITS64) {
                    stream.writeFLD64(getRegister(ind.getImg()), getRegister(ind.sreg), ind.scale, ind.disp);
                } else {
                    throw new IllegalArgumentException(
                        "Illegal operand size " + operandSize + " for " + getMnemonics()[FLD_ISN]);
                }
                break;
            default:
                reportAddressingError(FLD_ISN, addr);
        }
    }

    private void emitFMULP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.FPU r = getRegFPU(0);
                stream.writeFMULP(r);
                break;
            default:
                reportAddressingError(FMULP_ISN, addr);
        }
    }

    private void emitFNSTSW() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.GPR r = getReg(0);
                if (r.equals(X86Register.GPR16.AX)) {
                    stream.writeFNSTSW_AX();
                } else {
                    throw new IllegalArgumentException(
                        "Illegal operand " + r + " for " + getMnemonics()[FNSTSW_ISN]);
                }
                break;
            default:
                reportAddressingError(FNSTSW_ISN, addr);
        }
    }

    private void emitFPREM() {
        stream.writeFPREM();
    }

    private void emitFSTP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.FPU r = getRegFPU(0);
                stream.writeFSTP(r);
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                if (operandSize == X86Constants.BITS32) {
                    stream.writeFSTP32(getRegister(ind.getImg()), ind.disp);
                } else if (operandSize == X86Constants.BITS64) {
                    stream.writeFSTP64(getRegister(ind.getImg()), ind.disp);
                } else {
                    throw new IllegalArgumentException(
                        "Illegal operand size " + operandSize + " for " + getMnemonics()[FLD_ISN]);
                }
                break;
            default:
                reportAddressingError(FSTP_ISN, addr);
        }
    }

    private void emitFSUBP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.FPU r = getRegFPU(0);
                stream.writeFSUBP(r);
                break;
            default:
                reportAddressingError(FSUBP_ISN, addr);
        }
    }

    private void emitFUCOMPP() {
        stream.writeFUCOMPP();
    }

    private void emitFXCH() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register.FPU r = getRegFPU(0);
                stream.writeFXCH(r);
                break;
            default:
                reportAddressingError(FXCH_ISN, addr);
        }
    }

    final X86Register.FPU getRegFPU(int i) {
        return getRegisterFPU(((Register) args[i]).name);
    }
}
