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
 
package org.jnode.jnasm.assembler.x86;

import java.util.List;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.CRX;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.SR;
import org.jnode.jnasm.assembler.Address;
import org.jnode.jnasm.assembler.Identifier;
import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.jnasm.assembler.Register;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class X86Core extends AbstractX86Module {
    public static final int ADC_ISN = 0;
    public static final int ADD_ISN = ADC_ISN + 1;
    public static final int ALIGN_ISN = ADD_ISN + 1;
    public static final int AND_ISN = ALIGN_ISN + 1;
    public static final int CALL_ISN = AND_ISN + 1;
    public static final int CLD_ISN = CALL_ISN + 1;
    public static final int CLI_ISN = CLD_ISN + 1;
    public static final int CLTS_ISN = CLI_ISN + 1;
    public static final int CMP_ISN = CLTS_ISN + 1;
    public static final int CPUID_ISN = CMP_ISN + 1;
    public static final int DEC_ISN = CPUID_ISN + 1;
    public static final int DIV_ISN = DEC_ISN + 1;
    public static final int FLDCW_ISN = DIV_ISN + 1;
    public static final int FNINIT_ISN = FLDCW_ISN + 1;
    public static final int FNSAVE_ISN = FNINIT_ISN + 1;
    public static final int FRSTOR_ISN = FNSAVE_ISN + 1;
    public static final int FSTCW_ISN = FRSTOR_ISN + 1;
    public static final int FXRSTOR_ISN = FSTCW_ISN + 1;
    public static final int FXSAVE_ISN = FXRSTOR_ISN + 1;
    public static final int HLT_ISN = FXSAVE_ISN + 1;
    public static final int IN_ISN = HLT_ISN + 1;
    public static final int INC_ISN = IN_ISN + 1;
    public static final int INT_ISN = INC_ISN + 1;
    public static final int IRET_ISN = INT_ISN + 1;
    public static final int JA_ISN = IRET_ISN + 1;
    public static final int JAE_ISN = JA_ISN + 1;
    public static final int JB_ISN = JAE_ISN + 1;
    public static final int JE_ISN = JB_ISN + 1;
    public static final int JGE_ISN = JE_ISN + 1;
    public static final int JL_ISN = JGE_ISN + 1;
    public static final int JLE_ISN = JL_ISN + 1;
    public static final int JMP_ISN = JLE_ISN + 1;
    public static final int JNE_ISN = JMP_ISN + 1;
    public static final int JNZ_ISN = JNE_ISN + 1;
    public static final int JZ_ISN = JNZ_ISN + 1;
    public static final int JECXZ_ISN = JZ_ISN + 1;
    public static final int LDMXCSR_ISN = JECXZ_ISN + 1;
    public static final int LEA_ISN = LDMXCSR_ISN + 1;
    public static final int LGDT_ISN = LEA_ISN + 1;
    public static final int LIDT_ISN = LGDT_ISN + 1;
    public static final int LMSW_ISN = LIDT_ISN + 1;
    public static final int LODSW_ISN = LMSW_ISN + 1;
    public static final int LOOP_ISN = LODSW_ISN + 1;
    public static final int LTR_ISN = LOOP_ISN + 1;
    public static final int MOV_ISN = LTR_ISN + 1;
    public static final int MOVSB_ISN = MOV_ISN + 1;
    public static final int MOVSW_ISN = MOVSB_ISN + 1;
    public static final int MOVSD_ISN = MOVSW_ISN + 1;
    public static final int MOVZX_ISN = MOVSD_ISN + 1;
    public static final int NEG_ISN = MOVZX_ISN + 1;
    public static final int NOP_ISN = NEG_ISN + 1;
    public static final int OR_ISN = NOP_ISN + 1;
    public static final int OUT_ISN = OR_ISN + 1;
    public static final int POP_ISN = OUT_ISN + 1;
    public static final int POPA_ISN = POP_ISN + 1;
    public static final int POPF_ISN = POPA_ISN + 1;
    public static final int PUSH_ISN = POPF_ISN + 1;
    public static final int PUSHA_ISN = PUSH_ISN + 1;
    public static final int PUSHF_ISN = PUSHA_ISN + 1;
    public static final int RDMSR_ISN = PUSHF_ISN + 1;
    public static final int RET_ISN = RDMSR_ISN + 1;
    public static final int SHL_ISN = RET_ISN + 1;
    public static final int SHR_ISN = SHL_ISN + 1;
    public static final int STD_ISN = SHR_ISN + 1;
    public static final int STI_ISN = STD_ISN + 1;
    public static final int STMXCSR_ISN = STI_ISN + 1;
    public static final int STOSB_ISN = STMXCSR_ISN + 1;
    public static final int STOSD_ISN = STOSB_ISN + 1;
    public static final int STOSW_ISN = STOSD_ISN + 1;
    public static final int SUB_ISN = STOSW_ISN + 1;
    public static final int TEST_ISN = SUB_ISN + 1;
    public static final int WRMSR_ISN = TEST_ISN + 1;
    public static final int XCHG_ISN = WRMSR_ISN + 1;
    public static final int XOR_ISN = XCHG_ISN + 1;


    protected static final Map<String, Integer> INSTRUCTION_MAP;
    private static final String[] MNEMONICS;

    static {
        Map<String, Integer> map = InstructionUtils.getInstructionMap(X86Core.class);
        String[] mnemonics = InstructionUtils.getMnemonicArray(map);
        INSTRUCTION_MAP = map;
        MNEMONICS = mnemonics;
    }

    X86Core(Map<String, Label> labels, Map<String, Integer> constants) {
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
            case ADC_ISN:
                emitADC();
                break;
            case ADD_ISN:
                emitADD();
                break;
            case ALIGN_ISN:
                emitALIGN();
                break;
            case AND_ISN:
                emitAND();
                break;
            case CALL_ISN:
                emitCALL();
                break;
            case CLD_ISN:
                emitCLD();
                break;
            case CLI_ISN:
                emitCLI();
                break;
            case CLTS_ISN:
                emitCLTS();
                break;
            case CMP_ISN:
                emitCMP();
                break;
            case CPUID_ISN:
                emitCPUID();
                break;
            case DEC_ISN:
                emitDEC();
                break;
            case DIV_ISN:
                emitDIV();
                break;
            case FLDCW_ISN:
                emitFLDCW();
                break;
            case FNINIT_ISN:
                emitFNINIT();
                break;
            case FNSAVE_ISN:
                emitFNSAVE();
                break;
            case FRSTOR_ISN:
                emitFRSTOR();
                break;
            case FSTCW_ISN:
                emitFSTCW();
                break;
            case FXRSTOR_ISN:
                emitFXRSTOR();
                break;
            case FXSAVE_ISN:
                emitFXSAVE();
                break;
            case HLT_ISN:
                emitHLT();
                break;
            case IN_ISN:
                emitIN();
                break;
            case INC_ISN:
                emitINC();
                break;
            case INT_ISN:
                emitINT();
                break;
            case IRET_ISN:
                emitIRET();
                break;
            case JA_ISN:
                emitJCC(X86Assembler.JA);
                break;
            case JAE_ISN:
                emitJCC(X86Assembler.JAE);
                break;
            case JB_ISN:
                emitJCC(X86Assembler.JB);
                break;
            case JE_ISN:
                emitJCC(X86Assembler.JE);
                break;
            case JECXZ_ISN:
                emitJECXZ();
                break;
            case JGE_ISN:
                emitJCC(X86Assembler.JGE);
                break;
            case JL_ISN:
                emitJCC(X86Assembler.JL);
                break;
            case JLE_ISN:
                emitJCC(X86Assembler.JLE);
                break;
            case JMP_ISN:
                emitJMP();
                break;
            case JNE_ISN:
                emitJCC(X86Assembler.JNE);
                break;
            case JNZ_ISN:
                emitJCC(X86Assembler.JNZ);
                break;
            case JZ_ISN:
                emitJCC(X86Assembler.JZ);
                break;
            case LDMXCSR_ISN:
                emitLDMXCSR();
                break;
            case LEA_ISN:
                emitLEA();
                break;
            case LGDT_ISN:
                emitLGDT();
                break;
            case LIDT_ISN:
                emitLIDT();
                break;
            case LMSW_ISN:
                emitLMSW();
                break;
            case LODSW_ISN:
                emitLODSW();
                break;
            case LOOP_ISN:
                emitLOOP();
                break;
            case LTR_ISN:
                emitLTR();
                break;
            case MOV_ISN:
                emitMOV();
                break;
            case MOVSB_ISN:
                emitMOVSB();
                break;
            case MOVSW_ISN:
                emitMOVSW();
                break;
            case MOVSD_ISN:
                emitMOVSD();
                break;
            case MOVZX_ISN:
                emitMOVZX();
                break;
            case NEG_ISN:
                emitNEG();
                break;
            case NOP_ISN:
                emitNOP();
                break;
            case OR_ISN:
                emitOR();
                break;
            case OUT_ISN:
                emitOUT();
                break;
            case POP_ISN:
                emitPOP();
                break;
            case POPA_ISN:
                emitPOPA();
                break;
            case POPF_ISN:
                emitPOPF();
                break;
            case PUSH_ISN:
                emitPUSH();
                break;
            case PUSHA_ISN:
                emitPUSHA();
                break;
            case PUSHF_ISN:
                emitPUSHF();
                break;
            case RDMSR_ISN:
                emitRDMSR();
                break;
            case RET_ISN:
                emitRET();
                break;
            case SHL_ISN:
                emitSHL();
                break;
            case SHR_ISN:
                emitSHR();
                break;
            case STD_ISN:
                emitSTD();
                break;
            case STI_ISN:
                emitSTI();
                break;
            case STMXCSR_ISN:
                emitSTMXCSR();
                break;
            case STOSB_ISN:
                emitSTOSB();
                break;
            case STOSD_ISN:
                emitSTOSD();
                break;
            case STOSW_ISN:
                emitSTOSW();
                break;
            case SUB_ISN:
                emitSUB();
                break;
            case TEST_ISN:
                emitTEST();
                break;
            case WRMSR_ISN:
                emitWRMSR();
                break;
            case XCHG_ISN:
                emitXCHG();
                break;
            case XOR_ISN:
                emitXOR();
                break;
            default:
                throw new Error("Invalid instruction binding " + key.intValue() + " for " + mnemonic);
        }

        return true;
    }

    private final void emitADC() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeADC(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeADC(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeADC(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeADC(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeADC(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(ADC_ISN, addr);
        }
    }

    private final void emitADD() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeADD(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeADD(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeADD(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case RA_ADDR:
                ind = getAddress(1);
                stream.writeADD_MEM(getReg(0), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeADD(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeADD(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AC_ADDR:
                stream.writeADD(operandSize, getAddress(0).disp, getInt(1));
                break;
            case GC_ADDR:
                ind = getAddress(0);
                stream.writeADD(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(ADD_ISN, addr);
        }
    }

    private final void emitALIGN() {
        Object o1 = operands.get(0);
        if (o1 instanceof Integer) {
            stream.align(((Integer) o1).intValue());
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }

    }

    private final void emitAND() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeAND(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeAND(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeAND(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeAND(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeAND(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AC_ADDR:
                stream.writeAND(operandSize, getAddress(0).disp, getInt(1));
                break;
            case GC_ADDR:
                ind = getAddress(0);
                stream.writeAND(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(AND_ISN, addr);
        }
    }

    private final void emitCALL() {
        Object o1 = operands.get(0);
        if (o1 instanceof Register) {
            stream.writeCALL(getRegister(((Register) o1).name));
        } else if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = (Label) labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeCALL(lab);
        } else if (o1 instanceof Address) {
            Address ind = (Address) o1;
            if (ind.reg != null && ind.sreg != null) {
                throw new IllegalArgumentException("Scaled is not supported for call ");
            } else if (ind.reg != null && ind.sreg == null) {
                stream.writeCALL(getRegister(ind.getImg()), ind.disp);
            } else if (ind.reg == null && ind.sreg != null) {
                stream.writeCALL(getRegister(ind.sreg), ind.scale, ind.disp);
            } else if (ind.reg == null && ind.sreg == null) {
                throw new IllegalArgumentException("Absolute is not supported for call ");
            } else {
                throw new IllegalArgumentException("Unknown indirect: " + ind);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emitCLD() {
        stream.writeCLD();
    }

    private final void emitCLI() {
        stream.writeCLI();
    }

    private final void emitCLTS() {
        stream.writeCLTS();
    }

    private final void emitCMP() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeCMP(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeCMP_Const(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeCMP(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case RA_ADDR:
                ind = getAddress(1);
                stream.writeCMP_MEM(getReg(0), ind.disp);
                break;
            case RG_ADDR:
                ind = getAddress(1);
                stream.writeCMP(getReg(0), (SR) X86Register.getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeCMP(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeCMP_Const(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AC_ADDR:
                ind = getAddress(0);
                stream.writeCMP_MEM(operandSize, ind.disp, getInt(1));
                break;
            case GC_ADDR:
                ind = getAddress(0);
                stream.writeCMP_Const(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(CMP_ISN, addr);
        }
    }

    private final void emitCPUID() {
        stream.writeCPUID();
    }

    private final void emitDEC() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeDEC(getReg(0));
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeDEC(operandSize, getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(DEC_ISN, addr);
        }
    }

    private final void emitDIV() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeDIV_EAX(getReg(0));
                break;
            default:
                reportAddressingError(DIV_ISN, addr);
        }
    }

    private final void emitFLDCW() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeFLDCW(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(FLDCW_ISN, addr);
        }
    }

    private final void emitFNINIT() {
        stream.writeFNINIT();
    }

    private final void emitFNSAVE() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeFNSAVE(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(FNSAVE_ISN, addr);
        }
    }

    private final void emitFRSTOR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeFRSTOR(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(FRSTOR_ISN, addr);
        }
    }

    private final void emitFSTCW() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeFSTCW(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(FSTCW_ISN, addr);
        }
    }

    private final void emitFXRSTOR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeFXRSTOR(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(FXRSTOR_ISN, addr);
        }
    }

    private final void emitFXSAVE() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeFXSAVE(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(FXSAVE_ISN, addr);
        }
    }

    private final void emitHLT() {
        stream.writeHLT();
    }

    private final void emitIN() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg2 = getReg(1);
                if (reg2 != X86Register.DX) {
                    throw new IllegalArgumentException("Invalid second operand for IN: " + reg2);
                }
                GPR reg1 = getReg(0);
                if (reg1 == X86Register.AL) {
                    stream.writeIN(X86Constants.BITS8);
                } else if (reg1 == X86Register.AX) {
                    stream.writeIN(X86Constants.BITS16);
                } else if (reg1 == X86Register.EAX) {
                    stream.writeIN(X86Constants.BITS32);
                } else {
                    throw new IllegalArgumentException("Invalid first operand for IN: " + reg1);
                }
                break;
            case RC_ADDR:
                reg1 = getReg(0);
                if (reg1 == X86Register.AL) {
                    stream.writeIN(X86Constants.BITS8, getInt(1));
                } else if (reg1 == X86Register.AX) {
                    stream.writeIN(X86Constants.BITS16, getInt(1));
                } else if (reg1 == X86Register.EAX) {
                    stream.writeIN(X86Constants.BITS32, getInt(1));
                } else {
                    throw new IllegalArgumentException("Invalid first operand for IN: " + reg1);
                }
                break;
            default:
                reportAddressingError(IN_ISN, addr);
        }
    }

    private final void emitINC() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeINC(getReg(0));
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeINC(operandSize, getRegister(ind.getImg()), ind.disp);
                break;
            case A_ADDR:
                stream.writeINC(operandSize, getAddress(0).disp);
                break;
            case S_ADDR:
                ind = getAddress(0);
                stream.writeINC(operandSize, getRegister(ind.getImg()), getRegister(ind.sreg), ind.scale, ind.disp);
                break;
            case G_ADDR:
                ind = getAddress(0);
                stream.writeINC(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(INC_ISN, addr);
        }
    }

    private final void emitINT() {
        Object o1 = operands.get(0);
        Integer val = null;
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            val = (Integer) constants.get(id);
        } else if (o1 instanceof Integer) {
            val = (Integer) o1;
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }

        if (val != null) {
            stream.writeINT(val.intValue());
        } else {
            throw new IllegalArgumentException("Missing operand for INT");
        }
    }

    private final void emitIRET() {
        stream.writeIRET();
    }

    private final void emitJCC(int jumpCode) {
        Object o1 = operands.get(0);
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = (Label) labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJCC(lab, jumpCode);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emitJECXZ() {
        Object o1 = operands.get(0);
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = (Label) labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJECXZ(lab);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emitJMP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Register) {
            stream.writeJMP(getRegister(((Register) o1).name));
        } else if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = (Label) labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJMP(lab);
        } else if (o1 instanceof Address) {
            Address addr = (Address) o1;
            stream.writeJMP(operandSize, addr.scale, addr.disp);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emitLDMXCSR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeLDMXCSR(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(LDMXCSR_ISN, addr);
        }
    }

    private final void emitLEA() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeLEA(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case RS_ADDR:
                ind = getAddress(1);
                stream.writeLEA(getReg(0), getRegister(ind.getImg()), getRegister(ind.sreg), ind.scale, ind.disp);
                break;
            case RZ_ADDR:
                ind = getAddress(1);
                stream.writeLEA(getReg(0), getRegister(ind.sreg), ind.scale, ind.disp);
                break;
            default:
                reportAddressingError(LEA_ISN, addr);
        }
    }

    private final void emitLGDT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case A_ADDR:
                stream.writeLGDT(getAddress(0).disp);
                break;
            default:
                reportAddressingError(LGDT_ISN, addr);
        }
    }

    private final void emitLIDT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case A_ADDR:
                stream.writeLIDT(getAddress(0).disp);
                break;
            default:
                reportAddressingError(LIDT_ISN, addr);
        }
    }

    private final void emitLMSW() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeLMSW(getReg(0));
                break;
            default:
                reportAddressingError(LMSW_ISN, addr);
        }
    }

    private final void emitLODSW() {
        stream.writeLODSW();
    }

    private final void emitLOOP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = (Label) labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            try {
                stream.writeLOOP(lab);
            } catch (UnresolvedObjectRefException x) {
                x.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emitLTR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeLTR(getReg(0));
                break;
            default:
                reportAddressingError(LTR_ISN, addr);
        }
    }

    private final void emitMOV() {
        if (operands.size() == 2 &&
            operands.get(0) instanceof Register &&
            operands.get(1) instanceof Identifier) {
            stream.writeMOV_Const(getRegister(((Register) operands.get(0)).name),
                new Label(((Identifier) operands.get(1)).name));
            return;
        }
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register r1 = X86Register.getRegister(((Register) args[0]).name);
                X86Register r2 = X86Register.getRegister(((Register) args[1]).name);
                if (r1 instanceof GPR && r2 instanceof GPR) {
                    int s1 = r1.getSize();
                    int s2 = r2.getSize();
                    if (s1 != s2) {
                        throw new IllegalArgumentException("Incompatible register pair: " + r1 + "," + r2);
                    }
                    stream.writeMOV(s1, (GPR) r1, (GPR) r2);
                } else if (r1 instanceof CRX && r2 instanceof GPR) {
                    stream.writeMOV((CRX) r1, (GPR) r2);
                } else if (r1 instanceof GPR && r2 instanceof CRX) {
                    stream.writeMOV((GPR) r1, (CRX) r2);
                } else if (r1 instanceof SR && r2 instanceof GPR) {
                    stream.writeMOV((SR) r1, (GPR) r2);
                } else if (r1 instanceof GPR && r2 instanceof SR) {
                    stream.writeMOV((GPR) r1, (SR) r2);
                } else {
                    throw new IllegalArgumentException("Invalid register usage: mov " + r1 + "," + r2);
                }
                break;
            case RC_ADDR:
                stream.writeMOV_Const(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                GPR r = getReg(0);
                Address ind = getAddress(1);
                stream.writeMOV(r.getSize(), r, getRegister(ind.getImg()), ind.disp);
                break;
            case RA_ADDR:
                stream.writeMOV(getReg(0), getAddress(1).disp);
                break;
            case RG_ADDR:
                ind = getAddress(1);
                stream.writeMOV(getReg(0), (SR) X86Register.getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeMOV(operandSize, getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeMOV_Const(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AR_ADDR:
                stream.writeMOV(getAddress(0).disp, getReg(1));
                break;
            case AC_ADDR:
                stream.writeMOV_Const(operandSize, getAddress(0).disp, getInt(1));
                break;
            case SR_ADDR:
                ind = getAddress(0);
                stream.writeMOV(operandSize, getRegister(ind.getImg()), getRegister(ind.sreg),
                    ind.scale, ind.disp, getReg(1));
                break;
            case GR_ADDR:
                ind = getAddress(0);
                stream.writeMOV((SR) X86Register.getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case GC_ADDR:
                ind = getAddress(0);
                stream.writeMOV_Const(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(MOV_ISN, addr);
        }
    }

    private final void emitMOVSB() {
        stream.writeMOVSB();
    }

    private final void emitMOVSW() {
        stream.writeMOVSW();
    }

    private final void emitMOVSD() {
        stream.writeMOVSD();
    }

    private final void emitMOVZX() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg2 = getReg(1);
                stream.writeMOVZX(getReg(0), reg2, reg2.getSize());
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeMOVZX(getReg(0), getRegister(ind.getImg()), ind.disp, operandSize);
                break;
            default:
                reportAddressingError(MOVZX_ISN, addr);
        }
    }

    private final void emitNEG() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeNEG(getReg(0));
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeNEG(operandSize, getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(NEG_ISN, addr);
        }
    }

    private final void emitNOP() {
        stream.writeNOP();
    }

    private final void emitOR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeOR(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeOR(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeOR(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeOR(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeOR(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AC_ADDR:
                stream.writeOR(operandSize, getAddress(0).disp, getInt(1));
                break;
            case GC_ADDR:
                ind = getAddress(0);
                stream.writeOR(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(OR_ISN, addr);
        }
    }

    private final void emitOUT() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg1 = getReg(0);
                if (reg1 != X86Register.DX) {
                    throw new IllegalArgumentException("Invalid second operand for OUT: " + reg1);
                }
                GPR reg2 = getReg(1);
                if (reg2 == X86Register.AL) {
                    stream.writeOUT(X86Constants.BITS8);
                } else if (reg2 == X86Register.AX) {
                    stream.writeOUT(X86Constants.BITS16);
                } else if (reg2 == X86Register.EAX) {
                    stream.writeOUT(X86Constants.BITS32);
                } else {
                    throw new IllegalArgumentException("Invalid first operand for OUT: " + reg2);
                }
                break;
            case CR_ADDR:
                reg2 = getReg(1);
                if (reg2 == X86Register.AL) {
                    stream.writeOUT(X86Constants.BITS8, getInt(0));
                } else if (reg2 == X86Register.AX) {
                    stream.writeOUT(X86Constants.BITS16, getInt(0));
                } else if (reg2 == X86Register.EAX) {
                    stream.writeOUT(X86Constants.BITS32, getInt(0));
                } else {
                    throw new IllegalArgumentException("Invalid first operand for OUT: " + reg2);
                }
                break;
            default:
                reportAddressingError(OUT_ISN, addr);
        }
    }

    private final void emitPOP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                X86Register r1 = X86Register.getRegister(((Register) args[0]).name);
                if (r1 instanceof GPR) {
                    stream.writePOP((GPR) r1);
                } else if (r1 instanceof SR) {
                    stream.writePOP((SR) r1);
                } else {
                    throw new IllegalArgumentException("Invalid register usage: pop " + r1);
                }
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writePOP(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(POP_ISN, addr);
        }
    }

    private final void emitPOPA() {
        stream.writePOPA();
    }

    private final void emitPOPF() {
        stream.writePOPF();
    }

    private final void emitPUSH() {
        if (operands.size() == 1 && operands.get(0) instanceof Identifier) {
            stream.writePUSH_Const(new Label(((Identifier) operands.get(0)).name));
            return;
        }
        int addr = getAddressingMode(1);
        switch (addr) {
            case C_ADDR:
                stream.writePUSH(getInt(0));
                break;
            case R_ADDR:
                X86Register r1 = X86Register.getRegister(((Register) args[0]).name);
                if (r1 instanceof GPR) {
                    stream.writePUSH((GPR) r1);
                } else if (r1 instanceof SR) {
                    stream.writePUSH((SR) r1);
                } else {
                    throw new IllegalArgumentException("Invalid register usage: push " + r1);
                }
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writePUSH(getRegister(ind.getImg()), ind.disp);
                break;
            case G_ADDR:
                ind = getAddress(0);
                r1 = X86Register.getRegister(ind.getImg());
                stream.writePUSH((SR) r1, ind.disp);
                break;
            default:
                reportAddressingError(PUSH_ISN, addr);
        }
    }

    private final void emitPUSHA() {
        stream.writePUSHA();
    }

    private final void emitPUSHF() {
        stream.writePUSHF();
    }

    private final void emitRDMSR() {
        stream.writeRDMSR();
    }

    private final void emitRET() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case N_ADDR:
                stream.writeRET();
                break;
            case C_ADDR:
                stream.writeRET(getInt(0));
                break;
            default:
                reportAddressingError(RET_ISN, addr);
        }
    }

    private final void emitSHL() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RC_ADDR:
                stream.writeSHL(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Address ind = getAddress(0);
                stream.writeSHL(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SHL_ISN, addr);
        }
    }

    private final void emitSHR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RC_ADDR:
                stream.writeSHR(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Address ind = getAddress(0);
                stream.writeSHR(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SHR_ISN, addr);
        }
    }

    private final void emitSTD() {
        stream.writeSTD();
    }

    private final void emitSTI() {
        stream.writeSTI();
    }

    private final void emitSTMXCSR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeSTMXCSR(getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(STMXCSR_ISN, addr);
        }
    }

    private final void emitSTOSB() {
        stream.writeSTOSB();
    }

    private final void emitSTOSD() {
        stream.writeSTOSD();
    }

    private final void emitSTOSW() {
        stream.writeSTOSW();
    }

    private final void emitSUB() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeSUB(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeSUB(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeSUB(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeSUB(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeSUB(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AR_ADDR:
                stream.writeSUB(getAddress(0).disp, getReg(1));
                break;
            default:
                reportAddressingError(SUB_ISN, addr);
        }
    }

    private final void emitTEST() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeTEST(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeTEST(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Address ind = getAddress(0);
                stream.writeTEST(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            case AC_ADDR:
                stream.writeTEST(operandSize, getAddress(0).disp, getInt(1));
                break;
            case GC_ADDR:
                ind = getAddress(0);
                stream.writeTEST(operandSize, (SR) X86Register.getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(TEST_ISN, addr);
        }
    }

    private final void emitWRMSR() {
        stream.writeWRMSR();
    }

    private final void emitXCHG() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeXCHG(getReg(0), getReg(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeXCHG(getRegister(ind.getImg()), ind.disp, getReg(0));
                break;
            case RA_ADDR:
                stream.writeXCHG(getAddress(1).disp, getReg(0));
                break;
            case RG_ADDR:
                ind = getAddress(1);
                stream.writeXCHG((SR) X86Register.getRegister(ind.getImg()), ind.disp, getReg(0));
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeXCHG(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case AR_ADDR:
                stream.writeXCHG(getAddress(0).disp, getReg(1));
                break;
            case GR_ADDR:
                ind = getAddress(0);
                stream.writeXCHG((SR) X86Register.getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            default:
                reportAddressingError(XCHG_ISN, addr);
        }
    }

    private final void emitXOR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeXOR(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeXOR(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeXOR(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeXOR(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getAddress(0);
                stream.writeXOR(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(XOR_ISN, addr);
        }
    }
}
