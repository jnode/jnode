/**
 * $Id$
 */
package org.jnode.jnasm.preprocessor;

import java.util.HashMap;
import java.util.Collection;
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

/**
 * @author Levente S\u00e1ntha
 */
public abstract class Preprocessor {
    static HashMap multiMacros = new HashMap();
    static HashMap singleMacros = new HashMap();
    static HashSet localLabels = new HashSet();
    boolean substitute = true;

    public static void main(String[] argv) throws Exception{
        newInstance(System.in).print(new OutputStreamWriter(System.out));
        //System.err.println("MULTI LINE MACROS:\n" + multiMacros);
        //System.err.println("SINGLE LINE MACROS:\n" + singleMacros);
    }

    public static Preprocessor newInstance(InputStream in){
        return new JNAsmPP(in);
    }

    public static Preprocessor newInstance(Reader reader){
        return new JNAsmPP(reader);
    }

    String processFile(String file){
        StringWriter sw = new StringWriter();
        try{
            newInstance(new FileInputStream(file)).print(sw);
        }catch(FileNotFoundException e){
            System.err.println(e.getMessage());
        }
        sw.flush();
        return sw.toString();
    }

    String processString(String str){
        StringWriter sw = new StringWriter();
        newInstance(new StringReader(str)).print(sw);
        sw.flush();
        return sw.toString();
    }


    public void print(Writer w){
        try{
            PrintWriter pw = new PrintWriter(w);
            Token t = jnasmppInput();
            while (t != null) {
                  print(t, pw);
                  t = t.next;
            }
            pw.flush();
        } catch (ParseException pe){
            pe.printStackTrace();
            System.exit(-1);
        }
    }

    void print(Token t, PrintWriter ostr) {
        Token tt = t.specialToken;
        if (tt != null) {
          while (tt.specialToken != null) tt = tt.specialToken;
          while (tt != null) {
            ostr.print(tt.image);
            tt = tt.next;
          }
        }
        ostr.print(t.image);
    }

    void clearTokens(Token start, Token end){
        for(Token t = start; t != end; t = t.next){
            if(t.kind != JNAsmPPConstants.LINE_END){
                t.image = "";
            }
        }
        if(end.kind != JNAsmPPConstants.LINE_END) end.image = "";
    }

    void clearTokens(Token start){
        for(Token t = start; t != null && t.kind != JNAsmPPConstants.LINE_END; t = t.next){
            t.image = "";
        }
    }

    void singleLineMacroCall(Token t){
        if(substitute){
            String s = (String)singleMacros.get(t.image);
            if(s != null) t.image = s;
        }
    }

    void multiLineMacroCall(Token nameToken, Collection params){
        if(substitute){
            String name = nameToken.image.trim();
            Macro macro = (Macro)multiMacros.get(name);
            if(macro != null){
                String[] sparams = new String[0];
                if( params != null ){
                    sparams = (String[]) params.toArray(new String[params.size()]);
                }
                clearTokens(nameToken);
                String expansion = macro.expand(sparams);
                if(expansion != null){
                    //System.err.println(macro.toString() + " exp " + expansion);
                    nameToken.image = processString(expansion);
                    //System.err.println(" exp proc " + nameToken.image);
                }
            }
        }
    }

    abstract Token jnasmppInput() throws ParseException ;
}
