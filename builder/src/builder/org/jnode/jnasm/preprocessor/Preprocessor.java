/**
 * $Id$
 */
package org.jnode.jnasm.preprocessor;

import java.util.HashMap;
import java.util.HashSet;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.Reader;
import java.lang.reflect.Constructor;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public abstract class Preprocessor {
    private static final String PARSER_CLASS = "org.jnode.jnasm.preprocessor.gen.JNAsmPP";
    protected static HashMap multiMacros = new HashMap();
    protected static HashMap singleMacros = new HashMap();
    protected static HashSet localLabels = new HashSet();
    protected boolean substitute = true;

    public static void main(String[] argv) throws Exception{
        newInstance(System.in).print(new OutputStreamWriter(System.out));
        //System.err.println("MULTI LINE MACROS:\n" + multiMacros);
        //System.err.println("SINGLE LINE MACROS:\n" + singleMacros);
    }

    public static Preprocessor newInstance(InputStream in){
        try{
            singleMacros.put("BITS32", "");
            Class clazz = Class.forName(PARSER_CLASS);
            Constructor cons = clazz.getConstructor(new Class[]{InputStream.class});
            return (Preprocessor) cons.newInstance(new Object[]{in});
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Preprocessor newInstance(Reader reader){
        try{
            singleMacros.put("BITS32", "");
            Class clazz = Class.forName(PARSER_CLASS);
            Constructor cons = clazz.getConstructor(new Class[]{Reader.class});
            return (Preprocessor) cons.newInstance(new Object[]{reader});
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    protected String processFile(String file){
        StringWriter sw = new StringWriter();
        sw.write(";start include " + file + "\n");
        try{
            newInstance(new FileInputStream(file)).print(sw);
        }catch(FileNotFoundException e){
            System.err.println(e.getMessage());
        }
        sw.write(";end include " + file + "\n");
        sw.flush();
        return sw.toString();
    }

    protected String processString(String str){
        StringWriter sw = new StringWriter();
        newInstance(new StringReader(str)).print(sw);
        sw.flush();
        return sw.toString();
    }


    public void print(Writer w){
        try{
            jnasmppInput(new PrintWriter(w));
        } catch (Exception pe){
            pe.printStackTrace();
            System.exit(-1);
        }
    }

    public abstract void jnasmppInput(PrintWriter pw) throws Exception ;
}
