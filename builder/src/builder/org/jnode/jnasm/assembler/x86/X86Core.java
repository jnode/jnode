/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.jnasm.assembler.x86;

import org.jnode.jnasm.assembler.AssemblerModule;
import org.jnode.jnasm.assembler.Address;
import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.jnasm.assembler.Register;
import org.jnode.jnasm.assembler.Identifier;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register.CRX;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.SR;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.UnresolvedObjectRefException;

import java.util.Map;
import java.util.List;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class X86Core extends AssemblerModule {
    private static final int NUL_ARG = 0;
    private static final int CON_ARG = 1;
    private static final int REG_ARG = 2;
    private static final int REL_ARG = 3;
    private static final int ABS_ARG = 4;
    private static final int SCL_ARG = 5;
    private static final int ZSC_ARG = 6;
    private static final int SEG_ARG = 7;

    private static final String[] ARG_TYPES = {"noargument", "constant", "register", "relative", "absolute", "scaled", "simplescaled", "segment"};

    private static final int DISP = 3;
    private static final int DISP_MASK = ((2 << (DISP - 1)) - 1);

    private static final int N_ADDR = NUL_ARG;
    private static final int C_ADDR = CON_ARG;
    private static final int R_ADDR = REG_ARG;
    private static final int RR_ADDR = REG_ARG | REG_ARG << DISP;
    private static final int RC_ADDR = REG_ARG | CON_ARG << DISP;
    private static final int RE_ADDR = REG_ARG | REL_ARG << DISP;
    private static final int RA_ADDR = REG_ARG | ABS_ARG << DISP;
    private static final int RS_ADDR = REG_ARG | SCL_ARG << DISP;
    private static final int RZ_ADDR = REG_ARG | ZSC_ARG << DISP;
    private static final int CR_ADDR = CON_ARG | REG_ARG << DISP;
    private static final int E_ADDR = REL_ARG;
    private static final int ER_ADDR = REL_ARG | REG_ARG << DISP;
    private static final int EC_ADDR = REL_ARG | CON_ARG << DISP;
    private static final int A_ADDR = ABS_ARG;
    private static final int AC_ADDR = ABS_ARG | CON_ARG << DISP;
    private static final int AR_ADDR = ABS_ARG | REG_ARG << DISP;
    private static final int S_ADDR = SCL_ARG;
    private static final int SR_ADDR = SCL_ARG | REG_ARG << DISP;

    private final Object[] args = new Object[3];


    private int getAddressingMode(int maxArgs) {
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
                    if (ind.segment){
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

    private final int getInt(int i) {
        return ((Integer) args[i]).intValue();
    }

    private final X86Register.GPR getReg(int i) {
        return getRegister(((Register) args[i]).name);
    }

    private final Address getAddress(int i) {
        return (Address) args[i];
    }


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
    public static final int JL_ISN = JE_ISN + 1;
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
    public static final int RET_ISN = PUSHF_ISN + 1;
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
    public static final int XCHG_ISN = TEST_ISN + 1;
    public static final int XOR_ISN = XCHG_ISN + 1;


    protected static final Map INSTRUCTION_MAP;
    private static final String[] MNEMONICS;

    static {
        Map map = InstructionUtils.getInstructionMap(X86Core.class);
        String[] mnemonics = InstructionUtils.getMnemonicArray(map);
        INSTRUCTION_MAP = map;
        MNEMONICS = mnemonics;
    }

    private List operands;
    private int operandSize;
    private X86Assembler stream;

    public X86Core(final Map labels) {
        super(labels);
    }

    public void setNativeStream(NativeStream stream) {
        this.stream = (X86Assembler) stream;
    }

    public boolean emmit(String mnemonic, List operands, int operandSize) {
        this.operands = operands;
        this.operandSize = operandSize;

        Integer key = (Integer) INSTRUCTION_MAP.get(mnemonic);

        if (key == null) return false;

        switch (key.intValue()) {
            case ADC_ISN:
                emmitADC();
                break;
            case ADD_ISN:
                emmitADD();
                break;
            case ALIGN_ISN:
                emmitALIGN();
                break;
            case AND_ISN:
                emmitAND();
                break;
            case CALL_ISN:
                emmitCALL();
                break;
            case CLD_ISN:
                emmitCLD();
                break;
            case CLI_ISN:
                emmitCLI();
                break;
            case CLTS_ISN:
                emmitCLTS();
                break;
            case CMP_ISN:
                emmitCMP();
                break;
            case CPUID_ISN:
                emmitCPUID();
                break;
            case DEC_ISN:
                emmitDEC();
                break;
            case DIV_ISN:
                emmitDIV();
                break;
            case FLDCW_ISN:
                emmitFLDCW();
                break;
            case FNINIT_ISN:
                emmitFNINIT();
                break;
            case FNSAVE_ISN:
                emmitFNSAVE();
                break;
            case FRSTOR_ISN:
                emmitFRSTOR();
                break;
            case FSTCW_ISN:
                emmitFSTCW();
                break;
            case FXRSTOR_ISN:
                emmitFXRSTOR();
                break;
            case FXSAVE_ISN:
                emmitFXSAVE();
                break;
            case HLT_ISN:
                emmitHLT();
                break;
            case IN_ISN:
                emmitIN();
                break;
            case INC_ISN:
                emmitINC();
                break;
            case INT_ISN:
                emmitINT();
                break;
            case IRET_ISN:
                emmitIRET();
                break;
            case JA_ISN:
                emmitJCC(X86Assembler.JA);
            case JAE_ISN:
                emmitJCC(X86Assembler.JAE);
            case JB_ISN:
                emmitJCC(X86Assembler.JB);
                break;
            case JE_ISN:
                emmitJCC(X86Assembler.JE);
                break;
//TODO the X86BinaryAssembler support for this is buggy, cannot handle forward jumps.
            case JECXZ_ISN:
                emmitJECXZ();
                break;
            case JL_ISN:
                emmitJCC(X86Assembler.JL);
                break;
            case JLE_ISN:
                emmitJCC(X86Assembler.JLE);
                break;
            case JMP_ISN:
                emmitJMP();
                break;
            case JNE_ISN:
                emmitJCC(X86Assembler.JNE);
                break;
            case JNZ_ISN:
                emmitJCC(X86Assembler.JNZ);
                break;
            case JZ_ISN:
                emmitJCC(X86Assembler.JZ);
                break;
            case LDMXCSR_ISN:
                emmitLDMXCSR();
                break;
            case LEA_ISN:
                emmitLEA();
                break;
            case LGDT_ISN:
                emmitLGDT();
                break;
            case LIDT_ISN:
                emmitLIDT();
                break;
            case LMSW_ISN:
                emmitLMSW();
                break;
            case LODSW_ISN:
                emmitLODSW();
                break;
            case LOOP_ISN:
                emmitLOOP();
                break;
            case LTR_ISN:
                emmitLTR();
                break;
            case MOV_ISN:
                emmitMOV();
                break;
            case MOVSB_ISN:
                emmitMOVSB();
                break;
            case MOVSW_ISN:
                emmitMOVSW();
                break;
            case MOVSD_ISN:
                emmitMOVSD();
                break;
            case MOVZX_ISN:
                emmitMOVZX();
                break;
            case NEG_ISN:
                emmitNEG();
                break;
            case NOP_ISN:
                emmitNOP();
                break;
            case OR_ISN:
                emmitOR();
                break;
            case OUT_ISN:
                emmitOUT();
                break;
            case POP_ISN:
                emmitPOP();
                break;
            case POPA_ISN:
                emmitPOPA();
                break;
            case POPF_ISN:
                emmitPOPF();
                break;
            case PUSH_ISN:
                emmitPUSH();
                break;
            case PUSHA_ISN:
                emmitPUSHA();
                break;
            case PUSHF_ISN:
                emmitPUSHF();
                break;
            case RET_ISN:
                emmitRET();
                break;
            case SHL_ISN:
                emmitSHL();
                break;
            case SHR_ISN:
                emmitSHR();
                break;
            case STD_ISN:
                emmitSTD();
                break;
            case STI_ISN:
                emmitSTI();
                break;
            case STMXCSR_ISN:
                emmitSTMXCSR();
                break;
            case STOSB_ISN:
                emmitSTOSB();
                break;
            case STOSD_ISN:
                emmitSTOSD();
                break;
            case STOSW_ISN:
                emmitSTOSW();
                break;
            case SUB_ISN:
                emmitSUB();
                break;
            case TEST_ISN:
                emmitTEST();
                break;
            case XCHG_ISN:
                emmitXCHG();
                break;
            case XOR_ISN:
                emmitXOR();
                break;
            default:
                throw new Error("Invalid instruction binding " + key.intValue() + " for " + mnemonic);
        }

        return true;
    }

    private final void emmitADC() {
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

    private final void emmitADD() {
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
            default:
                reportAddressingError(ADD_ISN, addr);
        }
    }

    private final void emmitALIGN() {
        Object o1 = operands.get(0);
        if (o1 instanceof Integer) {
            stream.align(((Integer)o1).intValue());
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }

    }

    private final void emmitAND() {
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
            default:
                reportAddressingError(AND_ISN, addr);
        }
    }

    private final void emmitCALL() {
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
                throw new IllegalArgumentException("Simple scaled is not supported for call ");
            } else if (ind.reg == null && ind.sreg == null) {
                throw new IllegalArgumentException("Absolute is not supported for call ");
            } else {
                throw new IllegalArgumentException("Unknown indirect: " + ind);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emmitCLD() {
        stream.writeCLD();
    }

    private final void emmitCLI() {
        stream.writeCLI();
    }

    private final void emmitCLTS() {
        stream.writeCLTS();
    }

    private final void emmitCMP() {
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
            default:
                reportAddressingError(CMP_ISN, addr);
        }
    }

    private final void emmitCPUID() {
        stream.writeCPUID();
    }

    private final void emmitDEC() {
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

    private final void emmitDIV() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeDIV_EAX(getReg(0));
                break;
            default:
                reportAddressingError(DIV_ISN, addr);
        }
    }

    private final void emmitFLDCW() {
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

    private final void emmitFNINIT() {
        stream.writeFNINIT();
    }

    private final void emmitFNSAVE() {
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

    private final void emmitFRSTOR() {
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

    private final void emmitFSTCW() {
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

    private final void emmitFXRSTOR() {
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

    private final void emmitFXSAVE() {
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

    private final void emmitHLT() {
        stream.writeHLT();
    }

    private final void emmitIN() {
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

    private final void emmitINC() {
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
            default:
                reportAddressingError(INC_ISN, addr);
        }
    }

    private final void emmitINT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case C_ADDR:
                stream.writeINT(getInt(0));
                break;
            default:
                reportAddressingError(INT_ISN, addr);
        }
    }

    private final void emmitIRET() {
        stream.writeIRET();
    }

    private final void emmitJCC(int jumpCode) {
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

    private final void emmitJECXZ() {
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

    private final void emmitJMP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Register) {
            stream.writeJMP(getRegister(((Register) o1).name));
        } else if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = (Label) labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJMP(lab);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private final void emmitLDMXCSR() {
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

    private final void emmitLEA() {
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

    private final void emmitLGDT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case A_ADDR:
                stream.writeLGDT(getAddress(0).disp);
                break;
            default:
                reportAddressingError(LGDT_ISN, addr);
        }
    }

    private final void emmitLIDT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case A_ADDR:
                stream.writeLIDT(getAddress(0).disp);
                break;
            default:
                reportAddressingError(LIDT_ISN, addr);
        }
    }

    private final void emmitLMSW() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeLMSW(getReg(0));
                break;
            default:
                reportAddressingError(LMSW_ISN, addr);
        }
    }

    private final void emmitLODSW() {
        stream.writeLODSW();
    }

    private final void emmitLOOP() {
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

    private final void emmitLTR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeLTR(getReg(0));
                break;
            default:
                reportAddressingError(LTR_ISN, addr);
        }
    }

    private final void emmitMOV() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                X86Register r1 = X86Register.getRegister(((Register) args[0]).name);
                X86Register r2 = X86Register.getRegister(((Register) args[1]).name);
                if (r1 instanceof GPR && r2 instanceof GPR) {
                    int s1 = r1.getSize();
                    int s2 = r2.getSize();
                    if(s1 != s2){
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
            default:
                reportAddressingError(MOV_ISN, addr);
        }
    }

    private final void emmitMOVSB() {
        stream.writeMOVSB();
    }

    private final void emmitMOVSW() {
        stream.writeMOVSW();
    }

    private final void emmitMOVSD() {
        stream.writeMOVSD();
    }

    private final void emmitMOVZX() {
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

    private final void emmitNEG() {
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

    private final void emmitNOP() {
        stream.writeNOP();
    }

    private final void emmitOR() {
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
            default:
                reportAddressingError(OR_ISN, addr);
        }
    }

    private final void emmitOUT() {
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

    private final void emmitPOP() {
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

    private final void emmitPOPA() {
        stream.writePOPA();
    }

    private final void emmitPOPF() {
        stream.writePOPF();
    }

    private final void emmitPUSH() {
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
            default:
                reportAddressingError(PUSH_ISN, addr);
        }
    }

    private final void emmitPUSHA() {
        stream.writePUSHA();
    }

    private final void emmitPUSHF() {
        stream.writePUSHF();
    }

    private final void emmitRET() {
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

    private final void emmitSHL() {
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

    private final void emmitSHR() {
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

    private final void emmitSTD() {
        stream.writeSTD();
    }

    private final void emmitSTI() {
        stream.writeSTI();
    }

    private final void emmitSTMXCSR() {
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

    private final void emmitSTOSB() {
        stream.writeSTOSB();
    }

    private final void emmitSTOSD() {
        stream.writeSTOSD();
    }

    private final void emmitSTOSW() {
        stream.writeSTOSW();
    }

    private final void emmitSUB() {
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

    private final void emmitTEST() {
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
            default:
                reportAddressingError(TEST_ISN, addr);
        }
    }

    private final void emmitXCHG() {
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
            case ER_ADDR:
                ind = getAddress(0);
                stream.writeXCHG(getRegister(ind.getImg()), ind.disp, getReg(1));
                break;
            case AR_ADDR:
                stream.writeXCHG(getAddress(0).disp, getReg(1));
                break;
            default:
                reportAddressingError(XCHG_ISN, addr);
        }
    }

    private final void emmitXOR() {
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

    private final void reportAddressingError(int instruction, int addressing) {
        String err = "";
        int ad = addressing;
        do {
            err += " " + ARG_TYPES[ad & DISP_MASK];
            ad >>= DISP;
        } while (ad != 0);

        throw new IllegalArgumentException("Unknown addressing mode " + addressing + " (" + err + " ) for " + MNEMONICS[instruction]);
    }

    static final X86Register.GPR getRegister(String name) {
        return X86Register.getGPR(name);
    }
}
