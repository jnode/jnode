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
 
package org.jnode.jnasm.assembler;

import org.jnode.assembler.NativeStream;

import java.util.List;
import java.util.Map;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
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

    public PseudoInstructions(final Map labels, final Map constants) {
        super(labels, constants);
    }

    public void setNativeStream(NativeStream stream) {
        this.stream = stream;
    }

    public boolean emmit(String mnemonic, List operands, int operandSize) {
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
