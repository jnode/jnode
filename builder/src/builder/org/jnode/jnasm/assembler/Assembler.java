/**
 * $Id$  
 */
package org.jnode.jnasm.assembler;

import org.jnode.jnasm.assembler.x86.X86Support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.InputStreamReader;

/**
 * @author Levente S\u00e1ntha
 */
public abstract class Assembler {
    public static final boolean THROW = false;
    protected static final Object UNDEFINED = new String("UNDEFIEND");
    final List instructions = new ArrayList();
    private final Map constants = new HashMap();
    private final Map labels = new HashMap();
    private int pass = 0;
    private final HardwareSupport hwSupport;
    private final PseudoInstructions pseudo;

    public static void main(String[] argv) throws Exception {
        Assembler jnasm = newInstance(System.in);
        FileOutputStream out = new FileOutputStream("out");
        jnasm.performTwoPasses(new InputStreamReader(System.in), out);
        out.flush();
        out.close();
    }

    protected Assembler() {
        pseudo = new PseudoInstructions(labels);
        hwSupport = new X86Support(this, instructions, labels);
    }

    public PseudoInstructions getPseudo() {
        return pseudo;
    }

    public void performTwoPasses(Reader reader, OutputStream out) throws Exception{
        StringWriter sw = new StringWriter();
        char[] buf = new char[1024];
        for(int count; (count = reader.read(buf)) > -1; sw.write(buf, 0, count));
        sw.flush();
        sw.close();
        String data = sw.toString();

        //1st pass
        ReInit(new StringReader(data));
        setPass(1);
        jnasmInput();
        assemble();

        //2nd pass
        setPass(2);
        instructions.clear();
        ReInit(new StringReader(data));
        jnasmInput();
        emmit(out);
    }

    public void assemble() {
        hwSupport.assemble();
    }

    public void setPass(int pass) {
        this.pass = pass;
        hwSupport.setPass(pass);
    }

    public abstract void jnasmInput() throws ParseException;

    public abstract void ReInit(Reader stream);

    public static Assembler newInstance(InputStream in) {
        return new JNAsm(in);
    }

    public static Assembler newInstance(Reader reader) {
        return new JNAsm(reader);
    }

    public void emmit(OutputStream out) throws IOException{
        assemble();
        hwSupport.writeTo(out);
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
                    ret = (int) Long.parseLong(s);
                    break;

                case JNAsmConstants.BINNUMBER:
                    ret = (int) Long.parseLong(s.substring(0, s.length() - 1), 2);
                    break;

                case JNAsmConstants.OCTNUMBER:
                    ret = (int) Long.parseLong(s.substring(0, s.length() - 1), 8);
                    break;

                case JNAsmConstants.HEXNUMBER:
                    if (s.endsWith("h") || s.endsWith("H")) {
                        ret = (int) Long.parseLong(s.substring(0, s.length() - 1), 16);
                    } else {
                        ret = (int) Long.parseLong(s.substring(2), 16);
                    }
                    break;

                case JNAsmConstants.STRING:
                    s = s.substring(1, s.length() - 1);
                    byte[] buf = s.getBytes();
                    ret = 0;
                    int ln = Math.min(buf.length, 4);
                    for(int i = 0; i < ln; i++){
                        ret |= buf[i] << (i << 3);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Unkown number type: " + t.kind);

            }
        } catch (RuntimeException x) {
            if (THROW) {
                throw x;
            } else {
                //x.printStackTrace();
                System.err.println("Invaid int: " + x.getMessage());
                return 0;
            }
        }
        return ret;
    }

    public void putConstant(String name, int value) {
        if (constants.get(name) != null && pass == 1)
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
                //x.printStackTrace();
                if(pass == 2) System.err.println(x.getMessage());
                return 0;
            }
        }
        return i.intValue();
    }
}
