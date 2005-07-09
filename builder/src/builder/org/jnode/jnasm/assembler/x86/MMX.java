/*
 * $Id$
 */
package org.jnode.jnasm.assembler.x86;

import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.jnasm.assembler.Address;
import org.jnode.jnasm.assembler.Register;
import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Register;

import java.util.List;
import java.util.Map;

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
                emmitEMMS();
                break;
            case MOVD_ISN:
                emmitMOVD();
                break;
            case MOVQ_ISN:
                emmitMOVQ();
                break;
            case PACKUSWB_ISN:
                emmitPACKUSWB();
                break;
            case PADDW_ISN:
                emmitPADDW();
                break;
            case PAND_ISN:
                emmitPAND();
                break;
            case PCMPGTW_ISN:
                emmitPCMPGTW();
                break;
            case PMULLW_ISN:
                emmitPMULLW();
                break;
            case PSHUFW_ISN:
                emmitPSHUFW();
                break;
            case PSRLW_ISN:
                emmitPSRLW();
                break;
            case PSUBW_ISN:
                emmitPSUBW();
                break;
            case PUNPCKLBW_ISN:
                emmitPUNPCKLBW();
                break;
            case PXOR_ISN:
                emmitPXOR();
                break;
            default:
                throw new Error("Invalid instruction binding " + key.intValue() + " for " + mnemonic);

        }
        return true;
    }

    private void emmitEMMS() {
            stream.writeEMMS();
    }

    private void emmitMOVD() {
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

    private void emmitMOVQ() {
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

    private void emmitPACKUSWB() {
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

    private void emmitPADDW() {
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

    private void emmitPAND() {
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

    private void emmitPCMPGTW() {
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

    private void emmitPMULLW() {
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

    private void emmitPSHUFW() {
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

    private void emmitPSRLW() {
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

    private void emmitPSUBW() {
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

    private void emmitPUNPCKLBW() {
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

    private void emmitPXOR() {
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
