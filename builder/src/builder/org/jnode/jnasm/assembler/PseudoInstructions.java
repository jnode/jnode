/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import org.jnode.assembler.NativeStream;

import java.util.List;
import java.util.Map;

/**
 * @author Levente S\u00e1ntha
 */
public class PseudoInstructions extends AssemblerModule {
    protected static final Map INSTRUCTION_MAP;
    private static final String[] MNEMONICS;
    public static final int DB_ISN = 0;
    public static final int DW_ISN = DB_ISN + 1;
    public static final int DD_ISN = DW_ISN + 1;
    public static final int RESB_ISN = DD_ISN + 1;
    public static final int RESD_ISN = RESB_ISN + 1;

    static {
        Map map = InstructionUtils.getInstructionMap(PseudoInstructions.class);
        String[] mnemonics = InstructionUtils.getMnemonicArray(map);
        INSTRUCTION_MAP = map;
        MNEMONICS = mnemonics;
    }

    private List operands;
    private NativeStream stream;

    public PseudoInstructions(final Map labels) {
        super(labels);
    }

    public void setNativeStream(NativeStream stream) {
        this.stream = stream;
    }

    public boolean emmit(String mnemonic, List operands) {
        this.operands = operands;

        Integer key = (Integer) INSTRUCTION_MAP.get(mnemonic);

        if (key == null) return false;

        switch (key.intValue()) {
            case DB_ISN:
                emmitDB();
                break;
            case DW_ISN:
                emmitDW();
                break;
            case DD_ISN:
                emmitDD();
                break;
            case RESB_ISN:
                emmitRESB();
                break;
            case RESD_ISN:
                emmitRESD();
                break;
            default:
                throw new Error("Invalid instruction binding " + key.intValue() + " for " + mnemonic);
        }

        return true;
    }


    private void emmitDB() {
        int ln = operands.size();
        for (int i = 0; i < ln; i++) {
            Object o = (Object) operands.get(i);
            if (o instanceof Integer) {
                stream.write8(((Integer) o).intValue());
            } else if (o instanceof String) {
                byte[] bytes = ((String) o).getBytes();
                int bln = bytes.length;
                for (int j = 0; j < bln; j++) {
                    stream.write8(bytes[j]);
                }
            } else {
                System.out.println("unkown data: " + o);
            }
        }
    }

    private void emmitDW() {
        int ln = operands.size();
        for (int i = 0; i < ln; i++) {
            Object o = (Object) operands.get(i);
            if (o instanceof Integer) {
                stream.write16(((Integer) o).intValue());
            } else if (o instanceof String) {
                byte[] bytes = ((String) o).getBytes();
                int bln = bytes.length;
                for (int j = 0; j < bln; j++) {
                    stream.write8(bytes[j]);
                }
                if (bln % 2 == 1)
                    stream.write8(0);
            } else {
                System.out.println("unkown data: " + o);
            }
        }
    }

    private void emmitDD() {
        int ln = operands.size();
        for (int i = 0; i < ln; i++) {
            Object o = (Object) operands.get(i);
            if (o instanceof Integer) {
                stream.write32(((Integer) o).intValue());
            } else if (o instanceof String) {
                byte[] bytes = ((String) o).getBytes();
                int bln = bytes.length;
                for (int j = 0; j < bln; j++) {
                    stream.write8(bytes[j]);
                }
                bln = (4 - bln % 4) % 4;
                for (int j = 0; j < bln; j++) {
                    stream.write8(0);
                }
            } else {
                System.out.println("unkown data: " + o);
            }
        }
    }

    private void emmitRESB() {
        for ( int i = ((Integer) operands.get(0)).intValue(); i-- > 0; stream.write8(0) );
    }

    private void emmitRESD() {
        for ( int i = 4 * ((Integer) operands.get(0)).intValue(); i-- > 0; stream.write8(0) );
    }
}
