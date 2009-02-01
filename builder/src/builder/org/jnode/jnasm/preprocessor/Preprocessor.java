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
 
package org.jnode.jnasm.preprocessor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.jnode.build.AsmSourceInfo;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class Preprocessor {
    private static final String PARSER_CLASS = "org.jnode.jnasm.preprocessor.gen.JNAsmPP";
    private static FileResolver fileResolver;
    protected static HashMap multiMacros = new HashMap();
    protected static HashMap singleMacros = new HashMap();
    protected static HashSet localLabels = new HashSet();
    protected boolean substitute = true;

    public static void main(String[] argv) throws Exception {
        Preprocessor p = newInstance(System.in);
        p.defineSymbol("BITS32", "");
        p.defineSymbol("JNODE_VERSION", "1");
        p.setFileResolver(new FileResolver(null));
        p.print(new OutputStreamWriter(System.out));
        //System.err.println("MULTI LINE MACROS:\n" + multiMacros);
        //System.err.println("SINGLE LINE MACROS:\n" + singleMacros);
    }

    public static Preprocessor newInstance(AsmSourceInfo sourceInfo, Map<String, String> symbolMappings) {
        try {
            File mainFile = sourceInfo.getSrcFile();
            Preprocessor preprocessor = newInstance(new BufferedInputStream(new FileInputStream(mainFile)));
            preprocessor.setFileResolver(new FileResolver(sourceInfo.includeDirs()));
            Preprocessor.singleMacros.putAll(symbolMappings);
            return preprocessor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Preprocessor newInstance(InputStream in) {
        try {
            Class clazz = Class.forName(PARSER_CLASS);
            Constructor cons = clazz.getConstructor(new Class[]{InputStream.class});
            Preprocessor preprocessor = (Preprocessor) cons.newInstance(new Object[]{in});
            return preprocessor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Preprocessor newInstance(Reader reader) {
        try {
            Class clazz = Class.forName(PARSER_CLASS);
            Constructor cons = clazz.getConstructor(new Class[]{Reader.class});
            Preprocessor preprocessor = (Preprocessor) cons.newInstance(new Object[]{reader});
            return preprocessor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String processFile(String file) {
        StringWriter sw = new StringWriter();
        sw.write(";start include " + file + "\n");
        try {
            newInstance(new FileInputStream(fileResolver.resolveFile(file))).print(sw);
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
        sw.write(";end include " + file + "\n");
        sw.flush();
        return sw.toString();
    }

    protected String processString(String str) {
        StringWriter sw = new StringWriter();
        newInstance(new StringReader(str)).print(sw);
        sw.flush();
        return sw.toString();
    }


    public void print(Writer w) {
        try {
            StringWriter sw = new StringWriter();
            jnasmppInput(new PrintWriter(sw));
            ReInit(new StringReader(sw.toString()));
            jnasmppInput(new PrintWriter(w));
        } catch (Exception pe) {
            pe.printStackTrace();
            System.exit(-1);
        }
    }

    public void setFileResolver(FileResolver fileResolver) {
        this.fileResolver = fileResolver;
    }

    public void defineSymbol(String name, String definition) {
        singleMacros.put(name, definition);
    }

    public abstract void jnasmppInput(PrintWriter pw) throws Exception;

    public abstract void ReInit(InputStream stream);

    public abstract void ReInit(Reader stream);
}
