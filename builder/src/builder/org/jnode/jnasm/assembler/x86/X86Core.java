/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
import org.jnode.jnasm.assembler.Instruction;
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
    public static final int BTS_ISN = AND_ISN + 1;
    public static final int CALL_ISN = BTS_ISN + 1;
    public static final int CDQ_ISN = CALL_ISN + 1;
    public static final int CLD_ISN = CDQ_ISN + 1;
    public static final int CLI_ISN = CLD_ISN + 1;
    public static final int CLTS_ISN = CLI_ISN + 1;
    public static final int CMP_ISN = CLTS_ISN + 1;
    public static final int CMPXCHG_ISN = CMP_ISN + 1;
    public static final int CPUID_ISN = CMPXCHG_ISN + 1;
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
    public static final int IDIV_ISN = HLT_ISN + 1;
    public static final int IMUL_ISN = IDIV_ISN + 1;
    public static final int IN_ISN = IMUL_ISN + 1;
    public static final int INC_ISN = IN_ISN + 1;
    public static final int INT_ISN = INC_ISN + 1;
    public static final int IRET_ISN = INT_ISN + 1;
    public static final int IRETQ_ISN = IRET_ISN + 1;
    public static final int JA_ISN = IRETQ_ISN + 1;
    public static final int JAE_ISN = JA_ISN + 1;
    public static final int JB_ISN = JAE_ISN + 1;
    public static final int JBE_ISN = JB_ISN + 1;
    public static final int JE_ISN = JBE_ISN + 1;
    public static final int JG_ISN = JE_ISN + 1;
    public static final int JGE_ISN = JG_ISN + 1;
    public static final int JL_ISN = JGE_ISN + 1;
    public static final int JLE_ISN = JL_ISN + 1;
    public static final int JMP_ISN = JLE_ISN + 1;
    public static final int JNA_ISN = JMP_ISN + 1;
    public static final int JNE_ISN = JNA_ISN + 1;
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
    public static final int MOVSX_ISN = MOVSD_ISN + 1;
    public static final int MOVZX_ISN = MOVSX_ISN + 1;
    public static final int MUL_ISN = MOVZX_ISN + 1;
    public static final int NEG_ISN = MUL_ISN + 1;
    public static final int NOP_ISN = NEG_ISN + 1;
    public static final int NOT_ISN = NOP_ISN + 1;
    public static final int OR_ISN = NOT_ISN + 1;
    public static final int OUT_ISN = OR_ISN + 1;
    public static final int POP_ISN = OUT_ISN + 1;
    public static final int POPA_ISN = POP_ISN + 1;
    public static final int POPF_ISN = POPA_ISN + 1;
    public static final int PUSH_ISN = POPF_ISN + 1;
    public static final int PUSHA_ISN = PUSH_ISN + 1;
    public static final int PUSHF_ISN = PUSHA_ISN + 1;
    public static final int RDMSR_ISN = PUSHF_ISN + 1;
    public static final int RDTSC_ISN = RDMSR_ISN + 1;
    public static final int RET_ISN = RDTSC_ISN + 1;
    public static final int SBB_ISN = RET_ISN + 1;
    public static final int SETE_ISN = SBB_ISN + 1;
    public static final int SAHF_ISN = SETE_ISN + 1;
    public static final int SAL_ISN = SAHF_ISN + 1;
    public static final int SAR_ISN = SAL_ISN + 1;
    public static final int SHL_ISN = SAR_ISN + 1;
    public static final int SETA_ISN = SHL_ISN + 1;
    public static final int SETAE_ISN = SETA_ISN + 1;
    public static final int SETB_ISN = SETAE_ISN + 1;
    public static final int SETBE_ISN = SETB_ISN + 1;
    public static final int SETNE_ISN = SETBE_ISN + 1;
    public static final int SHLD_ISN = SETNE_ISN + 1;
    public static final int SHR_ISN = SHLD_ISN + 1;
    public static final int SHRD_ISN = SHR_ISN + 1;
    public static final int STD_ISN = SHRD_ISN + 1;
    public static final int STI_ISN = STD_ISN + 1;
    public static final int STMXCSR_ISN = STI_ISN + 1;
    public static final int STOSB_ISN = STMXCSR_ISN + 1;
    public static final int STOSD_ISN = STOSB_ISN + 1;
    public static final int STOSQ_ISN = STOSD_ISN + 1;
    public static final int STOSW_ISN = STOSQ_ISN + 1;
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

    public boolean emit(String mnemonic, List<Object> operands, int operandSize, Instruction instruction) {
        this.instruction = instruction;
        this.operands = operands;
        this.operandSize = operandSize;

        Integer key = INSTRUCTION_MAP.get(mnemonic);

        if (key == null) return false;

        switch (key) {
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
            case BTS_ISN:
                emitBTS();
                break;
            case CALL_ISN:
                emitCALL();
                break;
            case CDQ_ISN:
                emitCDQ();
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
            case CMPXCHG_ISN:
                emitCMPXCHG();
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
            case IDIV_ISN:
                emitIDIV();
                break;
            case IMUL_ISN:
                emitIMUL();
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
            case IRETQ_ISN:
                emitIRETQ();
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
            case JBE_ISN:
                emitJCC(X86Assembler.JBE);
                break;
            case JE_ISN:
                emitJCC(X86Assembler.JE);
                break;
            case JECXZ_ISN:
                emitJECXZ();
                break;
            case JG_ISN:
                emitJCC(X86Assembler.JG);
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
            case JNA_ISN:
                emitJCC(X86Assembler.JNA);
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
            case MOVSX_ISN:
                emitMOVSX();
                break;
            case MOVZX_ISN:
                emitMOVZX();
                break;
            case MUL_ISN:
                emitMUL();
                break;
            case NEG_ISN:
                emitNEG();
                break;
            case NOP_ISN:
                emitNOP();
                break;
            case NOT_ISN:
                emitNOT();
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
            case RDTSC_ISN:
                emitRDTSC();
                break;
            case RET_ISN:
                emitRET();
                break;
            case SBB_ISN:
                emitSBB();
                break;
            case SETE_ISN:
                emitSETE();
                break;
            case SAHF_ISN:
                emitSAHF();
                break;
            case SAL_ISN:
                emitSAL();
                break;
            case SAR_ISN:
                emitSAR();
                break;
            case SHL_ISN:
                emitSHL();
                break;
            case SETA_ISN:
                emitSETA();
                break;
            case SETAE_ISN:
                emitSETAE();
                break;
            case SETB_ISN:
                emitSETB();
                break;
            case SETBE_ISN:
                emitSETBE();
                break;
            case SETNE_ISN:
                emitSETNE();
                break;
            case SHLD_ISN:
                emitSHLD();
                break;
            case SHR_ISN:
                emitSHR();
                break;
            case SHRD_ISN:
                emitSHRD();
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
            case STOSQ_ISN:
                emitSTOSQ();
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
                throw new Error("Invalid instruction binding " + key + " for " + mnemonic);
        }

        return true;
    }

    private void emitADC() {
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

    private void emitADD() {
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

    private void emitALIGN() {
        Object o1 = operands.get(0);
        if (o1 instanceof Integer) {
            stream.align((Integer) o1);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }

    }

    private void emitAND() {
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

    private void emitBTS() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RC_ADDR:
                stream.writeBTS(getReg(0), getInt(1));
                break;
            default:
                reportAddressingError(BTS_ISN, addr);
        }
    }

    private void emitCALL() {
        Object o1 = operands.get(0);
        if (o1 instanceof Register) {
            stream.writeCALL(getRegister(((Register) o1).name));
        } else if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeCALL(lab);
        } else if (o1 instanceof Address) {
            Address ind = (Address) o1;
            if (ind.reg != null && ind.sreg != null) {
                throw new IllegalArgumentException("Scaled is not supported for call ");
            } else if (ind.reg != null) {
                if ("far".equals(this.instruction.getJumpType())) {
                    stream.writeCALL_FAR(getRegister(ind.getImg()), ind.disp);
                } else {
                    stream.writeCALL(getRegister(ind.getImg()), ind.disp);
                }
            } else if (ind.sreg != null) {
                stream.writeCALL(getRegister(ind.sreg), ind.scale, ind.disp);
            } else {
                throw new IllegalArgumentException("Absolute is not supported for call ");
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emitCDQ() {
        stream.writeCDQ(operandSize);
    }

    private void emitCLD() {
        stream.writeCLD();
    }

    private void emitCLI() {
        stream.writeCLI();
    }

    private void emitCLTS() {
        stream.writeCLTS();
    }

    private void emitCMP() {
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

    private void emitCMPXCHG() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case ER_ADDR:
                Address ind = getAddress(0);
                //prefix is already written
                stream.writeCMPXCHG_EAX(getRegister(ind.getImg()), ind.disp, getReg(1), false);
                break;
            default:
                reportAddressingError(CMPXCHG_ISN, addr);
        }
    }

    private void emitCPUID() {
        stream.writeCPUID();
    }

    private void emitDEC() {
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

    private void emitDIV() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeDIV_EAX(getReg(0));
                break;
            default:
                reportAddressingError(DIV_ISN, addr);
        }
    }

    private void emitFLDCW() {
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

    private void emitFNINIT() {
        stream.writeFNINIT();
    }

    private void emitFNSAVE() {
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

    private void emitFRSTOR() {
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

    private void emitFSTCW() {
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

    private void emitFXRSTOR() {
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

    private void emitFXSAVE() {
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

    private void emitHLT() {
        stream.writeHLT();
    }

    private void emitIDIV() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                GPR reg = getReg(0);
                stream.writeIDIV_EAX(reg);
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeIDIV_EAX(operandSize, getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(IDIV_ISN, addr);
        }
    }

    private void emitIMUL() {
        int addr = getAddressingMode(3);
        switch (addr) {
            case R_ADDR:
                GPR reg = getReg(0);
                stream.writeIMUL_EAX(reg);
                break;
            case RR_ADDR:
                GPR reg1 = getReg(0);
                GPR reg2 = getReg(1);
                stream.writeIMUL(reg1, reg2);
                break;
            case RE_ADDR:
                reg = getReg(0);
                Address ind = getAddress(1);
                stream.writeIMUL(reg, getRegister(ind.getImg()), ind.disp);
                break;
            case RRC_ADDR:
                reg1 = getReg(0);
                reg2 = getReg(1);
                int imm = getInt(2);
                stream.writeIMUL_3(reg1, reg2, imm);
                break;
            case REC_ADDR:
                reg = getReg(0);
                ind = getAddress(0);
                imm = getInt(2);
                stream.writeIMUL_3(reg, getRegister(ind.getImg()), ind.disp, imm);
                break;
            default:
                reportAddressingError(IMUL_ISN, addr);
        }
    }

    private void emitIN() {
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

    private void emitINC() {
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

    private void emitINT() {
        Object o1 = operands.get(0);
        Integer val;
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            val = constants.get(id);
        } else if (o1 instanceof Integer) {
            val = (Integer) o1;
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }

        if (val != null) {
            stream.writeINT(val);
        } else {
            throw new IllegalArgumentException("Missing operand for INT");
        }
    }

    private void emitIRET() {
        stream.writeIRET();
    }

    private void emitIRETQ() {
        stream.writeIRETQ();
    }

    private void emitJCC(int jumpCode) {
        Object o1 = operands.get(0);
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJCC(lab, jumpCode);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emitJECXZ() {
        Object o1 = operands.get(0);
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJECXZ(lab);
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emitJMP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Register) {
            stream.writeJMP(getRegister(((Register) o1).name));
        } else if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = labels.get(id);
            lab = (lab == null) ? new Label(id) : lab;
            stream.writeJMP(lab);
        } else if (o1 instanceof Address) {
            Address addr = (Address) o1;
            if (addr.reg != null) {
                stream.writeJMP(getRegister(addr.reg), addr.disp);
            } else {
                stream.writeJMP(operandSize, addr.scale, addr.disp);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emitLDMXCSR() {
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

    private void emitLEA() {
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

    private void emitLGDT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case A_ADDR:
                stream.writeLGDT(getAddress(0).disp);
                break;
            default:
                reportAddressingError(LGDT_ISN, addr);
        }
    }

    private void emitLIDT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case A_ADDR:
                stream.writeLIDT(getAddress(0).disp);
                break;
            default:
                reportAddressingError(LIDT_ISN, addr);
        }
    }

    private void emitLMSW() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeLMSW(getReg(0));
                break;
            default:
                reportAddressingError(LMSW_ISN, addr);
        }
    }

    private void emitLODSW() {
        stream.writeLODSW();
    }

    private void emitLOOP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Identifier) {
            String id = ((Identifier) o1).name;
            Label lab = labels.get(id);
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

    private void emitLTR() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeLTR(getReg(0));
                break;
            default:
                reportAddressingError(LTR_ISN, addr);
        }
    }

    private void emitMOV() {
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
            case RS_ADDR:
                ind = getAddress(1);
                stream.writeMOV(operandSize, getReg(0), getRegister(ind.getImg()), getRegister(ind.sreg), ind.scale,
                    ind.disp);
                break;
            case ER_ADDR:
                ind = getAddress(0);
                int oSize = operandSize;
                if (oSize > getReg(1).getSize()) {
                    oSize = getReg(1).getSize();
                }
                stream.writeMOV(oSize, getRegister(ind.getImg()), ind.disp, getReg(1));
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

    private void emitMOVSB() {
        stream.writeMOVSB();
    }

    private void emitMOVSW() {
        stream.writeMOVSW();
    }

    private void emitMOVSD() {
        stream.writeMOVSD();
    }

    private void emitMOVSX() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg2 = getReg(1);
                stream.writeMOVSX(getReg(0), reg2, operandSize);
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeMOVSX(getReg(0), getRegister(ind.getImg()), ind.disp, operandSize);
                break;
            default:
                reportAddressingError(MOVSX_ISN, addr);
        }
    }

    private void emitMOVZX() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg2 = getReg(1);
                int os = operandSize == X86Constants.BITS32 ? reg2.getSize() : operandSize;
                stream.writeMOVZX(getReg(0), reg2, os);
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeMOVZX(getReg(0), getRegister(ind.getImg()), ind.disp, operandSize);
                break;
            default:
                reportAddressingError(MOVZX_ISN, addr);
        }
    }

    private void emitMUL() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeMUL_EAX(getReg(0));
                break;
            default:
                reportAddressingError(MUL_ISN, addr);
        }
    }

    private void emitNEG() {
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

    private void emitNOP() {
        stream.writeNOP();
    }

    private void emitNOT() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeNOT(getReg(0));
                break;
            case E_ADDR:
                Address ind = getAddress(0);
                stream.writeNOT(operandSize, getRegister(ind.getImg()), ind.disp);
                break;
            default:
                reportAddressingError(NOT_ISN, addr);
        }
    }

    private void emitOR() {
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

    private void emitOUT() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg1 = getReg(0);
                if (reg1 != X86Register.DX) {
                    throw new IllegalArgumentException("Invalid second operand for OUT: " + reg1 +
                        ", must be " + X86Register.DX);
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

    private void emitPOP() {
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

    private void emitPOPA() {
        stream.writePOPA();
    }

    private void emitPOPF() {
        stream.writePOPF();
    }

    private void emitPUSH() {
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

    private void emitPUSHA() {
        stream.writePUSHA();
    }

    private void emitPUSHF() {
        stream.writePUSHF();
    }

    private void emitRDMSR() {
        stream.writeRDMSR();
    }

    private void emitRDTSC() {
        stream.writeRDTSC();
    }

    private void emitRET() {
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

    private void emitSBB() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeSBB(getReg(0), getReg(1));
                break;
            case RE_ADDR:
                Address ind = getAddress(1);
                stream.writeSBB(getReg(0), getRegister(ind.getImg()), ind.disp);
                break;
            case RC_ADDR:
                stream.writeSBB(getReg(0), getInt(1));
                break;
            default:
                reportAddressingError(SBB_ISN, addr);
        }
    }

    private void emitSETE() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeSETCC(getReg(0), X86Constants.JE);
                break;
            default:
                reportAddressingError(SETE_ISN, addr);
        }
    }

    private void emitSAHF() {
        stream.writeSAHF();
    }

    private void emitSAL() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg = getReg(1);
                if (reg.equals(X86Register.CL)) {
                    stream.writeSAL_CL(getReg(0));
                } else {
                    throw new IllegalArgumentException("Invalid second operand for SAL: " + reg +
                        ", must be " + X86Register.CL);
                }
                break;
            case RC_ADDR:
                stream.writeSAL(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Address ind = getAddress(0);
                stream.writeSAL(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SAL_ISN, addr);
        }
    }

    private void emitSAR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg = getReg(1);
                if (reg.equals(X86Register.CL)) {
                    stream.writeSAR_CL(getReg(0));
                } else {
                    throw new IllegalArgumentException("Invalid second operand for SAR: " + reg +
                        ", must be " + X86Register.CL);
                }
                break;
            case RC_ADDR:
                stream.writeSAR(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Address ind = getAddress(0);
                stream.writeSAR(operandSize, getRegister(ind.getImg()), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SAR_ISN, addr);
        }
    }

    private void emitSHL() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg = getReg(1);
                if (reg.equals(X86Register.CL)) {
                    stream.writeSHL_CL(getReg(0));
                } else {
                    throw new IllegalArgumentException("Invalid second operand for SHL: " + reg +
                        ", must be " + X86Register.CL);
                }
                break;
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

    private void emitSETA() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeSETCC(getReg(0), X86Constants.JA);
                break;
            default:
                reportAddressingError(SETA_ISN, addr);
        }
    }

    private void emitSETAE() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeSETCC(getReg(0), X86Constants.JAE);
                break;
            default:
                reportAddressingError(SETAE_ISN, addr);
        }
    }

    private void emitSETB() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeSETCC(getReg(0), X86Constants.JB);
                break;
            default:
                reportAddressingError(SETB_ISN, addr);
        }
    }

    private void emitSETBE() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeSETCC(getReg(0), X86Constants.JBE);
                break;
            default:
                reportAddressingError(SETBE_ISN, addr);
        }
    }

    private void emitSETNE() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeSETCC(getReg(0), X86Constants.JNE);
                break;
            default:
                reportAddressingError(SETNE_ISN, addr);
        }
    }

    private void emitSHLD() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeSHLD_CL(getReg(0), getReg(1));
                break;
            default:
                reportAddressingError(SHLD_ISN, addr);
        }
    }

    private void emitSHR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                GPR reg = getReg(1);
                if (reg.equals(X86Register.CL)) {
                    stream.writeSHR_CL(getReg(0));
                } else {
                    throw new IllegalArgumentException("Invalid second operand for SHR: " + reg +
                        ", must be " + X86Register.CL);
                }
                break;
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

    private void emitSHRD() {
        int addr = getAddressingMode(3);
        switch (addr) {
            case RRR_ADDR:
                GPR reg = getReg(2);
                if (reg.equals(X86Register.CL)) {
                    stream.writeSHRD_CL(getReg(0), getReg(1));
                } else {
                    throw new IllegalArgumentException("Invalid second operand for SHRD: " + reg +
                        ", must be " + X86Register.CL);
                }
                break;
            default:
                reportAddressingError(SHRD_ISN, addr);
        }
    }

    private void emitSTD() {
        stream.writeSTD();
    }

    private void emitSTI() {
        stream.writeSTI();
    }

    private void emitSTMXCSR() {
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

    private void emitSTOSB() {
        stream.writeSTOSB();
    }

    private void emitSTOSD() {
        stream.writeSTOSD();
    }

    private void emitSTOSQ() {
        stream.writeSTOSQ();
    }

    private void emitSTOSW() {
        stream.writeSTOSW();
    }

    private void emitSUB() {
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

    private void emitTEST() {
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

    private void emitWRMSR() {
        stream.writeWRMSR();
    }

    private void emitXCHG() {
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

    private void emitXOR() {
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
