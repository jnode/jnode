/**
 * $Id$  
 */
package org.jnode.jnasm.assembler.x86;

import org.jnode.jnasm.assembler.AssemblerModule;
import org.jnode.jnasm.assembler.Token;
import org.jnode.jnasm.assembler.Indirect;
import org.jnode.jnasm.assembler.Assembler;
import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
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

    private static final int DISP = 3;
    private static final int DISP_MASK = ((2 << (DISP - 1)) - 1);

    private static final int N_ADDR = NUL_ARG;
    private static final int C_ADDR = CON_ARG;
    private static final int R_ADDR = REG_ARG;
    private static final int RR_ADDR = REG_ARG | REG_ARG << DISP;
    private static final int RC_ADDR = REG_ARG | CON_ARG << DISP;
    private static final int RE_ADDR = REG_ARG | REL_ARG << DISP;
    private static final int RA_ADDR = REG_ARG | ABS_ARG << DISP;
    private static final int E_ADDR = REL_ARG;
    private static final int ER_ADDR = REL_ARG | REG_ARG << DISP;
    private static final int EC_ADDR = REL_ARG | CON_ARG << DISP;
    private static final int AC_ADDR = ABS_ARG | CON_ARG << DISP;

    private static final String[] ARG_TYPES = {"noargument","constant","register","relative", "absolute", "scaled", "simplescaled"};

    private final Object[] args = new Object[3];



    private int getAddressingMode(int maxArgs) {
        int ret = N_ADDR;
        if (maxArgs > 3) {
            throw new Error("Invalid number of arguments: " + maxArgs);
        }

        for (int i = 0; i < maxArgs; i++) {
            try {
                if(operands == null) break;
                
                Object o = operands.get(i);
                if (o == null) break;

                if (o instanceof Integer) {
                    ret |= CON_ARG << DISP * i;
                } else if (o instanceof Token && Assembler.isIdent((Token) o)) {
                    ret |= REG_ARG << DISP * i;
                } else if (o instanceof Indirect) {
                    Indirect ind = (Indirect) o;
                    if(ind.reg != null && ind.sreg != null){
                        ret |= SCL_ARG << DISP * i;
                    } else if(ind.reg != null && ind.sreg == null){
                        ret |= REL_ARG << DISP * i;
                    } else if(ind.reg == null && ind.sreg != null){
                        ret |= ZSC_ARG << DISP * i;
                    } else if(ind.reg == null && ind.sreg == null){
                        ret |= ABS_ARG << DISP * i;
                    } else {
                        throw new IllegalArgumentException("Unknown indirect: " + ind);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown operand: " + o);
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
        return getRegister((Token) args[i]);
    }

    private final Indirect getInd(int i) {
        return (Indirect) args[i];
    }


    public static final int ADC_ISN = 0;
    public static final int ADD_ISN = ADC_ISN + 1;
    public static final int AND_ISN = ADD_ISN + 1;
    public static final int CALL_ISN = AND_ISN + 1;
    public static final int CMP_ISN = CALL_ISN + 1;
    public static final int DEC_ISN = CMP_ISN + 1;
    public static final int INC_ISN = DEC_ISN + 1;
    public static final int JA_ISN = INC_ISN + 1;
    public static final int JAE_ISN = JA_ISN + 1;
    public static final int JB_ISN = JAE_ISN + 1;
    public static final int JE_ISN = JB_ISN + 1;
    public static final int JL_ISN = JE_ISN + 1;
    public static final int JLE_ISN = JL_ISN + 1;
    public static final int JMP_ISN = JLE_ISN + 1;
    public static final int JNE_ISN = JMP_ISN + 1;
    public static final int JNZ_ISN = JNE_ISN + 1;
    public static final int JZ_ISN = JNZ_ISN + 1;
    public static final int LEA_ISN = JZ_ISN + 1;
    public static final int LOOP_ISN = LEA_ISN + 1;
    public static final int MOV_ISN = LOOP_ISN + 1;
    public static final int NEG_ISN = MOV_ISN + 1;
    public static final int NOP_ISN = NEG_ISN + 1;
    public static final int OR_ISN = NOP_ISN + 1;
    public static final int POP_ISN = OR_ISN + 1;
    public static final int POPA_ISN = POP_ISN + 1;
    public static final int PUSH_ISN = POPA_ISN + 1;
    public static final int PUSHA_ISN = PUSH_ISN + 1;
    public static final int RET_ISN = PUSHA_ISN + 1;
    public static final int SHL_ISN = RET_ISN + 1;
    public static final int SHR_ISN = SHL_ISN + 1;
    public static final int SUB_ISN = SHR_ISN + 1;
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
            case AND_ISN:
                emmitAND();
                break;
            case CALL_ISN:
                emmitCALL();
                break;
            case CMP_ISN:
                emmitCMP();
                break;
            case DEC_ISN:
                emmitDEC();
                break;
            case INC_ISN:
                emmitINC();
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
            case LEA_ISN:
                emmitLEA();
                break;
            case LOOP_ISN:
                emmitLOOP();
                break;
            case MOV_ISN:
                emmitMOV();
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
            case POP_ISN:
                emmitPOP();
                break;
            case POPA_ISN:
                emmitPOPA();
                break;
            case PUSH_ISN:
                emmitPUSH();
                break;
            case PUSHA_ISN:
                emmitPUSHA();
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

    private void emmitADC() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeADC(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeADC(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeADC(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeADC(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeADC(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(ADC_ISN, addr);
        }
    }

    private void emmitADD() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeADD(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeADD(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeADD(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeADD(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeADD(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(ADD_ISN, addr);
        }
    }

    private void emmitAND() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeAND(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeAND(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeAND(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeAND(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeAND(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(AND_ISN, addr);
        }
    }

    private void emmitCALL() {
        Object o1 = operands.get(0);
        if (o1 instanceof Token) {
            Token t1 = (Token) o1;
            if (Assembler.isIdent(t1)) {
                Label lab = (Label) labels.get(t1.image);
                lab = (lab == null) ? new Label(t1.image) : lab;
                stream.writeCALL(lab);
            } else {
                throw new IllegalArgumentException("Unknown operand: " + t1.image);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emmitCMP() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeCMP(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeCMP_Const(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeCMP(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case RA_ADDR:
                ind = getInd(1);
                stream.writeCMP_MEM(getReg(0), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeCMP(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeCMP_Const(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            case AC_ADDR:
                ind = getInd(0);
                stream.writeCMP_MEM(operandSize, ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(CMP_ISN, addr);
        }
    }

    private void emmitDEC() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeDEC(getReg(0));
                break;
            case E_ADDR:
                Indirect ind = getInd(0);
                stream.writeDEC(operandSize, getRegister(ind.reg), ind.disp);
                break;
            default:
                reportAddressingError(DEC_ISN, addr);
        }
    }

    private void emmitINC() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeINC(getReg(0));
                break;
            case E_ADDR:
                Indirect ind = getInd(0);
                stream.writeINC(operandSize, getRegister(ind.reg), ind.disp);
                break;
            default:
                reportAddressingError(INC_ISN, addr);
        }
    }

    private void emmitJMP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Token) {
            Token t1 = (Token) o1;
            if (Assembler.isIdent(t1)) {
                Label lab = (Label) labels.get(t1.image);
                lab = (lab == null) ? new Label(t1.image) : lab;
                stream.writeJMP(lab);
            } else {
                throw new IllegalArgumentException("Unknown operand: " + t1.image);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emmitJCC(int jumpCode) {
        Object o1 = operands.get(0);
        if (o1 instanceof Token) {
            Token t1 = (Token) o1;
            if (Assembler.isIdent(t1)) {
                Label lab = (Label) labels.get(t1.image);
                lab = (lab == null) ? new Label(t1.image) : lab;
                stream.writeJCC(lab, jumpCode);
            } else {
                throw new IllegalArgumentException("Unknown operand: " + t1.image);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emmitLEA() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeLEA(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            default:
                reportAddressingError(LEA_ISN, addr);
        }
    }

    private void emmitLOOP() {
        Object o1 = operands.get(0);
        if (o1 instanceof Token) {
            Token t1 = (Token) o1;
            if (Assembler.isIdent(t1)) {
                Label lab = (Label) labels.get(t1.image);
                lab = (lab == null) ? new Label(t1.image) : lab;
                try{
                    stream.writeLOOP(lab);
                }catch(UnresolvedObjectRefException x){
                    x.printStackTrace();
                }
            } else {
                throw new IllegalArgumentException("Unknown operand: " + t1.image);
            }
        } else {
            throw new IllegalArgumentException("Unknown operand: " + o1);
        }
    }

    private void emmitMOV() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeMOV(operandSize, getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeMOV_Const(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeMOV(operandSize, getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeMOV(operandSize, getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeMOV_Const(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(MOV_ISN, addr);
        }
    }

    private void emmitNEG() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writeNEG(getReg(0));
                break;
            case E_ADDR:
                Indirect ind = getInd(0);
                stream.writeNEG(operandSize, getRegister(ind.reg), ind.disp);
                break;
            default:
                reportAddressingError(NEG_ISN, addr);
        }
    }

    private void emmitNOP() {
        stream.writeNOP();
    }

    private void emmitOR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeOR(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeOR(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeOR(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeOR(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeOR(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(OR_ISN, addr);
        }
    }

    private void emmitPOP() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case R_ADDR:
                stream.writePOP(getReg(0));
                break;
            case E_ADDR:
                Indirect ind = getInd(0);
                stream.writePOP(getRegister(ind.reg), ind.disp);
                break;
            default:
                reportAddressingError(POP_ISN, addr);
        }
    }

    private void emmitPOPA() {
        stream.writePOPA();
    }

    private void emmitPUSH() {
        int addr = getAddressingMode(1);
        switch (addr) {
            case C_ADDR:
                stream.writePUSH(getInt(0));
                break;
            case R_ADDR:
                stream.writePUSH(getReg(0));
                break;
            case E_ADDR:
                Indirect ind = getInd(0);
                stream.writePUSH(getRegister(ind.reg), ind.disp);
                break;
            default:
                reportAddressingError(PUSH_ISN, addr);
        }
    }

    private void emmitPUSHA() {
        stream.writePUSHA();
    }

    private void emmitRET() {
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

    private void emmitSHL() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RC_ADDR:
                stream.writeSHL(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Indirect ind = getInd(0);
                stream.writeSHL(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SHL_ISN, addr);
        }
    }

    private void emmitSHR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RC_ADDR:
                stream.writeSHR(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Indirect ind = getInd(0);
                stream.writeSHR(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SHR_ISN, addr);
        }
    }

    private void emmitSUB() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeSUB(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeSUB(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeSUB(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeSUB(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeSUB(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(SUB_ISN, addr);
        }
    }

    private void emmitTEST() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeTEST(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeTEST(getReg(0), getInt(1));
                break;
            case EC_ADDR:
                Indirect ind = getInd(0);
                stream.writeTEST(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(TEST_ISN, addr);
        }
    }

    private void emmitXCHG() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeXCHG(getReg(0), getReg(1));
                break;
            case ER_ADDR:
                Indirect ind = getInd(0);
                stream.writeXCHG(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            default:
                reportAddressingError(XCHG_ISN, addr);
        }
    }

    private void emmitXOR() {
        int addr = getAddressingMode(2);
        switch (addr) {
            case RR_ADDR:
                stream.writeXOR(getReg(0), getReg(1));
                break;
            case RC_ADDR:
                stream.writeXOR(getReg(0), getInt(1));
                break;
            case RE_ADDR:
                Indirect ind = getInd(1);
                stream.writeXOR(getReg(0), getRegister(ind.reg), ind.disp);
                break;
            case ER_ADDR:
                ind = getInd(0);
                stream.writeXOR(getRegister(ind.reg), ind.disp, getReg(1));
                break;
            case EC_ADDR:
                ind = getInd(0);
                stream.writeXOR(operandSize, getRegister(ind.reg), ind.disp, getInt(1));
                break;
            default:
                reportAddressingError(XOR_ISN, addr);
        }
    }

    private void reportAddressingError(int instruction, int addressing){
        String err = "";
        int ad = addressing;
        do {
            err += " " + ARG_TYPES[ad & DISP_MASK];
            ad >>= DISP;
        } while(ad != 0);

        throw new IllegalArgumentException("Unknown addressing mode " + addressing + " (" + err + " ) for " + MNEMONICS[instruction]);
    }

    static final X86Register.GPR getRegister(Token t) {
        return X86Register.getGPR(t.image);
    }
}
