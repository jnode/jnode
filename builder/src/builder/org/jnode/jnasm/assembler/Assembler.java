/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.Label;
import org.jnode.vm.x86.X86CpuID;

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
    List instructions = new ArrayList();
    private Map constants = new HashMap();
    private Map labels = new HashMap();

    public static void main(String[] argv) throws Exception{
        Assembler jnasm = newInstance(System.in);
        jnasm.jnasmInput();
        FileOutputStream out = new FileOutputStream("out");
        jnasm.emit(out);
        out.flush();
        out.close();
    }

    public abstract void jnasmInput() throws ParseException;

    public static Assembler newInstance(InputStream in){
        return new JNAsm(in);
    }

    public static Assembler newInstance(Reader reader){
        return new JNAsm(reader);
    }

    public void emit(OutputStream out) {
        X86CpuID cpuId = X86CpuID.createID("pentium");
        X86Assembler asm = new X86BinaryAssembler(cpuId, X86Constants.Mode.CODE32, 0);
        for (Iterator it = instructions.iterator(); it.hasNext();) {
            Instruction ins = (Instruction) it.next();
            String label = ins.getLabel();
            if(label != null){
                if(labels.get(label) != null )
                    throw new IllegalArgumentException("Label already defined: " + label);

                try{
                    Label lab = new Label(label);
                    labels.put(label, lab);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            String mnemo = ins.getMnemonic();
            if(mnemo != null){
                emit(ins.getMnemonic(), ins.getOperands(), asm);
            }
        }
        try {
            asm.writeTo(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void emit(String mnemonic, List operands, X86Assembler asm) {
        Token t1, t2;
        Object o1, o2;
        if ("mov".equals(mnemonic)) {
            o1 = operands.get(0);
            o2 = operands.get(1);
            if (o1 instanceof Token && isIdent(t1 = (Token) o1)) {
                if (o2 instanceof Token) {
                    t2 = (Token) o2;
                    if (isIdent(t2)) {
                        asm.writeMOV(X86Constants.BITS32, getRegister(t1), getRegister(t2));
                    } else {
                        System.out.println("unkown operand: " + t2.image);
                    }
                } else if (o2 instanceof Integer) {
                    asm.writeMOV_Const(getRegister(t1), ((Integer) o2).intValue());
                } else if (o2 instanceof Indirect) {
                    Indirect ind = (Indirect) o2;
                    asm.writeMOV(X86Constants.BITS32, getRegister(t1), getRegister(ind.reg), ind.disp);
                } else {
                    System.out.println("unkown operand: " + o2);
                }
            } else if (o1 instanceof Indirect) {
                Indirect ind = (Indirect) o1;
                if (o2 instanceof Token) {
                    t2 = (Token) o2;
                    if (isIdent(t2)) {
                        asm.writeMOV(X86Constants.BITS32, getRegister(ind.reg), ind.disp, getRegister(t2));
                    } else {
                        System.out.println("unkown operand: " + t2.image);
                    }
                } else if (o2 instanceof Integer) {
                    asm.writeMOV_Const(X86Constants.BITS32, getRegister(ind.reg), ind.disp, ((Integer) o2).intValue());
                } else {
                    System.out.println("unkown operand: " + o2);
                }
            } else {
                System.out.println("unkown operand: " + o1);
            }
        } else if ("push".equals(mnemonic)) {
            o1 = operands.get(0);
            if (o1 instanceof Token) {
                t1 = (Token) o1;
                if (isIdent(t1)) {
                    asm.writePUSH(getRegister(t1));
                } else {
                    System.out.println("unkown operand: " + t1.image);
                }
            } else if (o1 instanceof Integer) {
                asm.writePUSH(((Integer) o1).intValue());
            } else if (o1 instanceof Indirect) {
                Indirect ind = (Indirect) o1;
                asm.writePUSH(getRegister(ind.reg), ind.disp);
            } else {
                System.out.println("unkown operand: " + o1);
            }
        } else if ("pop".equals(mnemonic)) {
            o1 = operands.get(0);
            if (o1 instanceof Token) {
                t1 = (Token) o1;
                if (isIdent(t1)) {
                    asm.writePOP(getRegister(t1));
                } else {
                    System.out.println("unkown operand: " + t1.image);
                }
            } else if (o1 instanceof Indirect) {
                Indirect ind = (Indirect) o1;
                asm.writePOP(getRegister(ind.reg), ind.disp);
            } else {
                System.out.println("unkown operand: " + o1);
            }
        } else if ("jmp".equals(mnemonic)) {
            o1 = operands.get(0);
            if (o1 instanceof Token) {
                t1 = (Token) o1;
                if (isIdent(t1)) {
                    Label lab = (Label) labels.get(t1.image);
                    lab = (lab == null) ? new Label(t1.image) : lab;
                    asm.writeJMP(lab);
                } else {
                    System.out.println("unkown operand: " + t1.image);
                }
            } else {
                System.out.println("unkown operand: " + o1);
            }
        } else {
            throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
    }

    static final boolean isIdent(Token t) {
        return t.kind == JNAsmConstants.IDENT || t.kind == JNAsmConstants.REGISTER;
    }

    static final boolean isNumber(Token t) {
        int k = t.kind;
        return k == JNAsmConstants.DECNUMBER ||
                k == JNAsmConstants.BINNUMBER ||
                k == JNAsmConstants.OCTNUMBER ||
                k == JNAsmConstants.HEXNUMBER;
    }

    static final X86Register.GPR getRegister(Token t) {
        return X86Register.getGPR(t.image);
    }

    static final int getNumber(Token t) {
        int ret;
        String s = t.image;
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
                ret = Integer.parseInt(s.substring(2), 16);
                break;

            default:
                throw new IllegalArgumentException("Unkown number type: " + t.kind);

        }
        return ret;
    }

    void putConstant(String name, int value){
        if(constants.get(name) != null)
            throw new IllegalArgumentException("Constant already defined: " + name);

        constants.put(name, new Integer(value));
    }

    int getConstant(String name){
        Integer i = (Integer) constants.get(name);
        if( i == null)
            throw new IllegalArgumentException("Undefined constant: " + name);

        return i.intValue();
    }
}
