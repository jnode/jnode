/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.Label;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.jnasm.assembler.x86.Core;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class Assembler {
    private static final boolean THROW = false;
    final List instructions = new ArrayList();
    private final Map constants = new HashMap();
    private final Map labels = new HashMap();
    private final List modules = new ArrayList();

    public static void main(String[] argv) throws Exception {
        Assembler jnasm = newInstance(System.in);
        jnasm.jnasmInput();
        FileOutputStream out = new FileOutputStream("out");
        jnasm.emmit(out);
        out.flush();
        out.close();
    }

    protected Assembler() {
        modules.add(new Core(labels));
    }

    public abstract void jnasmInput() throws ParseException;

    public static Assembler newInstance(InputStream in) {
        return new JNAsm(in);
    }

    public static Assembler newInstance(Reader reader) {
        return new JNAsm(reader);
    }

    public void emmit(OutputStream out) {
        X86CpuID cpuId = X86CpuID.createID("pentium");
        X86Assembler asm = new X86BinaryAssembler(cpuId, X86Constants.Mode.CODE32, 0);
        for (Iterator it = instructions.iterator(); it.hasNext();) {
            Instruction ins = (Instruction) it.next();
            String label = ins.getLabel();
            if (label != null) {
                if (labels.get(label) != null)
                    throw new IllegalArgumentException("Label already defined: " + label);

                try {
                    Label lab = new Label(label);
                    labels.put(label, lab);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String mnemo = ins.getMnemonic();
            if (mnemo != null) {
                emmit(ins.getMnemonic(), ins.getOperands(), asm);
            }
        }
        try {
            asm.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void emmit(String mnemonic, List operands, X86Assembler asm) {
        for (int i = 0; i < modules.size(); i++) {
            if (((AssemblerModule) modules.get(i)).emmit(mnemonic, operands, asm)) {
                return;
            }
        }

        throw new RuntimeException("Unknown instruction: " + mnemonic);
    }

    public static final boolean isIdent(Token t) {
        return t.kind == JNAsmConstants.IDENT || t.kind == JNAsmConstants.REGISTER;
    }

    static final boolean isNumber(Token t) {
        int k = t.kind;
        return k == JNAsmConstants.DECNUMBER ||
                k == JNAsmConstants.BINNUMBER ||
                k == JNAsmConstants.OCTNUMBER ||
                k == JNAsmConstants.HEXNUMBER;
    }

    static final int getNumber(Token t) {
        int ret;
        String s = t.image;
        try {
            switch (t.kind) {
                case JNAsmConstants.DECNUMBER:
                    ret = Integer.parseInt(s);
                    break;

                case JNAsmConstants.BINNUMBER:
                    ret = Integer.parseInt(s.substring(0, s.length() - 1), 2);
                    break;

                case JNAsmConstants.OCTNUMBER:
                    ret = Integer.parseInt(s.substring(0, s.length() - 1), 8);
                    break;

                case JNAsmConstants.HEXNUMBER:
                    System.out.println("hextoken: " + s);
                    if (s.endsWith("h") || s.endsWith("H")) {
                        ret = Integer.parseInt(s.substring(0, s.length() - 1), 16);
                    } else {
                        ret = Integer.parseInt(s.substring(2), 16);
                    }
                    break;

                default:
                    throw new IllegalArgumentException("Unkown number type: " + t.kind);

            }
        } catch (RuntimeException x) {
            if (THROW) {
                throw x;
            } else {
                x.printStackTrace();
                return 0;
            }
        }
        return ret;
    }

    void putConstant(String name, int value) {
        if (constants.get(name) != null)
            throw new IllegalArgumentException("Constant already defined: " + name);

        constants.put(name, new Integer(value));
    }

    int getConstant(String name) {
        Integer i = (Integer) constants.get(name);
        try {
            if (i == null)
                throw new IllegalArgumentException("Undefined constant: " + name);
        } catch (RuntimeException x) {
            if (THROW) {
                throw x;
            } else {
                x.printStackTrace();
                return 0;
            }
        }
        return i.intValue();
    }
}
