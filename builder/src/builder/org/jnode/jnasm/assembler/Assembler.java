/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.jnasm.assembler.x86.X86Support;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class Assembler {
    public static final int UNDEFINED_INT = 989898989;
    private static final String PARSER_CLASS = "org.jnode.jnasm.assembler.gen.JNAsm";
    public static final boolean THROW = false;
    protected static final Object UNDEFINED = new String("UNDEFIEND");
    protected final List<Instruction> instructions = new ArrayList<Instruction>();
    private final Map<String, Integer> constants = new HashMap<String, Integer>();
    private final Map<String, Label> labels = new HashMap<String, Label>();
    private int pass = 0;
    protected final HardwareSupport hwSupport;
    private final PseudoInstructions pseudo;
    protected Instruction crtIns;

    public static Assembler newInstance(InputStream in) {
        try {
            Class clazz = Class.forName(PARSER_CLASS);
            Constructor cons = clazz.getConstructor(new Class[]{InputStream.class});
            return (Assembler) cons.newInstance(new Object[]{in});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Assembler newInstance(Reader reader) {
        try {
            Class clazz = Class.forName("org.jnode.jnasm.assembler.gen.JNAsm");
            Constructor cons = clazz.getConstructor(new Class[]{Reader.class});
            return (Assembler) cons.newInstance(new Object[]{reader});
        } catch (Exception e) {
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
        pseudo = new PseudoInstructions(labels, constants);
        hwSupport = new X86Support(this, instructions, labels, constants);
    }

    public PseudoInstructions getPseudo() {
        return pseudo;
    }

    public void performTwoPasses(Reader reader, OutputStream out) throws Exception {
        StringWriter sw = new StringWriter();
        char[] buf = new char[1024];
        for (int count; (count = reader.read(buf)) > -1; sw.write(buf, 0, count)) ;
        sw.flush();
        sw.close();
        String data = sw.toString();

        //1st pass
        ReInit(new StringReader(data));
        setPass(1);
        jnasmInput();
        assemble(0);

        //2nd pass
        setPass(2);
        instructions.clear();
        ReInit(new StringReader(data));
        jnasmInput();
        emit(out);
    }

    public void performTwoPasses(Reader reader, NativeStream asm) throws Exception {
        StringWriter sw = new StringWriter();
        char[] buf = new char[1024];
        for (int count; (count = reader.read(buf)) > -1; sw.write(buf, 0, count)) ;
        sw.flush();
        sw.close();
        String data = sw.toString();

        new RandomAccessFile("jnode.lst", "rw").write(data.getBytes());

        //1st pass
        ReInit(new StringReader(data));
        setPass(1);
        jnasmInput();
        assemble((int) asm.getBaseAddr());

        //2nd pass
        setPass(2);
        instructions.clear();
        ReInit(new StringReader(data));
        jnasmInput();
        emit(asm);

        asm.writeTo(new FileOutputStream("jnode.out"));
    }

    public void assemble(int baseAddress) {
        hwSupport.assemble(baseAddress);
    }

    public void assemble(NativeStream out) {
        hwSupport.assemble(out);
    }

    public void setPass(int pass) {
        this.pass = pass;
        hwSupport.setPass(pass);
    }

    public abstract void jnasmInput() throws Exception;

    public abstract void ReInit(Reader stream);

    public void emit(OutputStream out) throws IOException {
        assemble(0);
        hwSupport.writeTo(out);
    }

    public void emit(NativeStream out) throws IOException {
        assemble(out);
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
                if (pass == 2) {
                    //x.printStackTrace();
                    System.out.println(x.getMessage());
                    throw new UndefinedConstantException(name);
                }
                return Integer.MAX_VALUE;
            }
        }
        return i.intValue();
    }

    protected final void setSizeInfo(String size) {
        crtIns.setSizeInfo(size);
    }

    public static class UndefinedConstantException extends RuntimeException {
        private String constant;

        public UndefinedConstantException(String constant) {
            super(constant);
            this.constant = constant;
        }

        public String getConstant() {
            return constant;
        }
    }
}
