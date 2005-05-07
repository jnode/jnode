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
 
package org.jnode.jnasm;

import org.jnode.jnasm.preprocessor.Preprocessor;
import org.jnode.jnasm.preprocessor.FileResolver;
import org.jnode.jnasm.assembler.Assembler;
import org.jnode.assembler.NativeStream;
import org.jnode.build.AsmSourceInfo;

import java.io.StringWriter;
import java.io.StringReader;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Levente S\u00e1ntha (lsantha@users.sourceforge.net)
 */
public class JNAsm {
    public static void main(String[] argv) throws Exception{
        Preprocessor preprocessor = Preprocessor.newInstance(System.in);
        preprocessor.defineSymbol("BITS32","");
        preprocessor.setFileResolver(new FileResolver(null));
        StringWriter sw = new StringWriter();
        preprocessor.print(sw);
        sw.flush();
        sw.close();
        StringReader sr = new StringReader(sw.toString());
        Assembler assembler = Assembler.newInstance(sr);
        FileOutputStream out = new FileOutputStream("out");
        assembler.performTwoPasses(sr, out);
        out.flush();
        out.close();
    }

    public static void assembler(NativeStream asm, AsmSourceInfo sourceInfo) throws Exception{
        Map<String,String> symbols = new HashMap<String, String>();
        symbols.put("BITS32","");
        symbols.put("JNODE_VERSION", "1");
        Preprocessor preprocessor = Preprocessor.newInstance(sourceInfo, symbols);
        StringWriter sw = new StringWriter();
        preprocessor.print(sw);
        sw.flush();
        sw.close();
        StringReader sr = new StringReader(sw.toString());
        Assembler assembler = Assembler.newInstance(sr);
        assembler.performTwoPasses(sr, asm);
    }
}
