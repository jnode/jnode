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
 
package org.jnode.jnasm.assembler;

import java.util.List;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class PseudoInstructions extends AssemblerModule {
    protected static final Map<String, Integer> INSTRUCTION_MAP;
    @SuppressWarnings("unused")
    private static final String[] MNEMONICS;
    public static final int BITS_ISN = 0;
    public static final int DB_ISN = BITS_ISN + 1;
    public static final int DW_ISN = DB_ISN + 1;
    public static final int DD_ISN = DW_ISN + 1;
    public static final int DQ_ISN = DD_ISN + 1;
    public static final int RESB_ISN = DQ_ISN + 1;
    public static final int RESD_ISN = RESB_ISN + 1;

    static {
        Map<String, Integer> map = InstructionUtils.getInstructionMap(PseudoInstructions.class);
        String[] mnemonics = InstructionUtils.getMnemonicArray(map);
        INSTRUCTION_MAP = map;
        MNEMONICS = mnemonics;
    }

    private List<Object> operands;
    private NativeStream stream;

    public PseudoInstructions(Map<String, Label> labels, Map<String, Integer> constants) {
        super(labels, constants);
    }

    public void setNativeStream(NativeStream stream) {
        this.stream = stream;
    }

    public boolean emit(String mnemonic, List<Object> operands, int operandSize, Instruction instruction) {
        this.operands = operands;

        Integer key = INSTRUCTION_MAP.get(mnemonic);

        if (key == null) return false;

        switch (key) {
            case BITS_ISN:
                emitBITS();
                break;
            case DB_ISN:
                emitDB();
                break;
            case DW_ISN:
                emitDW();
                break;
            case DD_ISN:
                emitDD();
                break;
            case DQ_ISN:
                emitDQ();
                break;
            case RESB_ISN:
                emitRESB();
                break;
            case RESD_ISN:
                emitRESD();
                break;
            default:
                throw new Error("Invalid instruction binding " + key + " for " + mnemonic);
        }

        return true;
    }

    private void emitBITS() {
        //do nothing for now
    }

    private void emitDB() {
        for (Object o : operands) {
            if (o instanceof Integer) {
                stream.write8((Integer) o);
            } else if (o instanceof String) {
                byte[] bytes = ((String) o).getBytes();
                for (byte aByte : bytes) {
                    stream.write8(aByte);
                }
            } else {
                throw new IllegalArgumentException("Unknown data: " + o);
            }
        }
    }

    private void emitDW() {
        for (Object o : operands) {
            if (o instanceof Integer) {
                stream.write16((Integer) o);
            } else if (o instanceof String) {
                byte[] bytes = ((String) o).getBytes();
                int bln = bytes.length;
                for (byte aByte : bytes) {
                    stream.write8(aByte);
                }
                if (bln % 2 == 1)
                    stream.write8(0);
            } else {
                throw new IllegalArgumentException("Unknown data: " + o);
            }
        }
    }

    private void emitDD() {
        for (Object o : operands) {
            if (o instanceof Integer) {
                stream.write32((Integer) o);
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
            } else if (o instanceof Identifier) {
                stream.writeObjectRef(new Label(((Identifier) o).name));
            } else {
                throw new IllegalArgumentException("Unknown data: " + o);
            }
        }
    }

    private void emitDQ() {
        for (Object o : operands) {
            if (o instanceof Integer) {
                stream.write64((Integer) o);
            } else if (o instanceof Long) {
                stream.write64((Long) o);
            } else if (o instanceof String) {
                byte[] bytes = ((String) o).getBytes();
                int bln = bytes.length;
                for (int j = 0; j < bln; j++) {
                    stream.write8(bytes[j]);
                }
                bln = (8 - bln % 8) % 8;
                for (int j = 0; j < bln; j++) {
                    stream.write8(0);
                }
            } else if (o instanceof Identifier) {
                stream.writeObjectRef(new Label(((Identifier) o).name));
            } else {
                throw new IllegalArgumentException("Unknown data: " + o);
            }
        }
    }

    private void emitRESB() {
        int i = (Integer) operands.get(0);
        while (i-- > 0) {
            stream.write8(0);
        }
    }

    private void emitRESD() {
        int i = 4 * (Integer) operands.get(0);
        while (i-- > 0) {
            stream.write8(0);
        }
    }
}
