/*
 * $Id$
 */
package org.jnode.jnasm.assembler.x86;

import org.jnode.jnasm.assembler.InstructionUtils;
import org.jnode.assembler.Label;

import java.util.List;
import java.util.Map;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class MMX extends AbstractX86Module {

    public static final int EMMS_ISN = 0;
    /*public static final int MOVD_ISN = EMMS_ISN + 1;
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
    */
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
            default:
                throw new Error("Invalid instruction binding " + key.intValue() + " for " + mnemonic);

        }
        return true;
    }

    private void emmitEMMS() {
        stream.writeEMMS();
    }
}
