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
import java.lang.reflect.Constructor;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class Assembler {
    private static final String PARSER_CLASS = "org.jnode.jnasm.assembler.gen.JNAsm";
    public static final boolean THROW = false;
    protected static final Object UNDEFINED = new String("UNDEFIEND");
    protected final List instructions = new ArrayList();
    private final Map constants = new HashMap();
    private final Map labels = new HashMap();
    private int pass = 0;
    protected final HardwareSupport hwSupport;
    private final PseudoInstructions pseudo;
    protected Instruction crtIns;
    
    public static Assembler newInstance(InputStream in){
        try{
            Class clazz = Class.forName(PARSER_CLASS);
            Constructor cons = clazz.getConstructor(new Class[]{InputStream.class});
            return (Assembler) cons.newInstance(new Object[]{in});
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Assembler newInstance(Reader reader){
        try{
            Class clazz = Class.forName("org.jnode.jnasm.assembler.gen.JNAsm");
            Constructor cons = clazz.getConstructor(new Class[]{Reader.class});
            return (Assembler) cons.newInstance(new Object[]{reader});
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

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

    public abstract void jnasmInput() throws Exception;

    public abstract void ReInit(Reader stream);

    public void emmit(OutputStream out) throws IOException{
        assemble();
        hwSupport.writeTo(out);
    }

    public void putConstant(String name, int value) {
        if (constants.get(name) != null && pass == 1)
            throw new IllegalArgumentException("Constant already defined: " + name);

        constants.put(name, new Integer(value));
    }

    protected int getConstant(String name, int line) {
        Integer i = (Integer) constants.get(name);
        try {
            if (i == null)
                throw new IllegalArgumentException("Undefined constant at line " + line + ": " + name);
        } catch (RuntimeException x) {
            if (THROW) {
                throw x;
            } else {
                if(pass == 2){
                    //x.printStackTrace();
                    System.out.println(x.getMessage());
                }
                return 0;
            }
        }
        return i.intValue();
    }

    protected final void setSizeInfo(String size){
        crtIns.setSizeInfo(size);
    }
}
