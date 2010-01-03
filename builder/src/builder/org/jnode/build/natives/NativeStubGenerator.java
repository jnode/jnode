/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.build.natives;

import java.io.File;
import java.io.PrintWriter;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;
import org.apache.bcel.util.ClassLoaderRepository;

/**
 * Simple tool for generating JNode speciffic "native" stubs for classes with native methods.
 * The first argument is the fully qualified name of the class to process, which is on the classpath.
 * The second argument is the root directory of the source tree where the generated stub is saved,
 * if missing the stub is printed to System.out.
 *
 * @author Levente S\u00e1ntha
 */
public class NativeStubGenerator {
    /*
    TODO: generate imports, improve the structure
    */
    public static void main(String[] argv) throws Exception {
        if (argv.length < 1) {
            System.err.println("Mandatory class name argument is missing.");
            return;
        }
        ClassLoaderRepository clr = new ClassLoaderRepository(NativeStubGenerator.class.getClassLoader());
        File f = null;
        if (argv.length > 1) {
            f = new File(argv[1]);
            if (!(f.exists() && f.isDirectory())) {
                System.err.println("Invalid output directory: " + argv[1]);
                return;
            }
        }
        JavaClass jc;
        try {
            jc = clr.loadClass(argv[0]);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + argv[0]);
            return;
        }
        String pk = jc.getPackageName();
        String cn = jc.getClassName();
        int p = cn.lastIndexOf('.');
        if (p > -1) cn = cn.substring(p + 1);
        String fn = "/" + pk.replace('.', '/') + "/";
        PrintWriter pw;
        if (f != null) {
            f = new File(f, fn);
            f.mkdirs();
            f = new File(f, "Native" + cn + ".java");
            if (f.exists()) {
                System.out.println("File already exists: " + f);
                return;
            }
            pw = new PrintWriter(f);
        } else {
            pw = new PrintWriter(System.out);
        }
        pw.println("package " + pk + ";");
        pw.println();
        pw.println("/**");
        pw.println(" * @see " + jc.getClassName());
        //pw.println(" * @author Levente S\\u00e1ntha");
        pw.println(" */");
        pw.println("class Native" + cn + " {");
        Method[] ms = jc.getMethods();
        for (Method m : ms) {
            if (m.isNative()) {
                String args = "";
                String types = "";
                int i = 1;
                for (Type t : m.getArgumentTypes()) {
                    String s = t.toString();
                    p = s.lastIndexOf('.');
                    if (p > -1) s = s.substring(p + 1);
                    args += ", " + s.replace('$', '.') + " arg" + (i++);
                    types += ", " + t.toString().replace('$', '.');
                }
                if (args.length() > 0 && m.isStatic()) args = args.substring(2);
                if (types.length() > 0) types = types.substring(2);
                pw.println("    /**");
                pw.println("     * @see " + jc.getClassName() + "#" + m.getName() + "(" + types + ")");
                pw.println("     */");
                String ret = m.getReturnType().toString();
                p = ret.lastIndexOf('.');
                if (p > -1) ret = ret.substring(p + 1);
                ret = ret.replace('$', '.');
                pw.println("    private static " + ret + " " + m.getName() + "(" +
                    (!m.isStatic() ? cn + " instance" : "") + args + ") {");
                pw.println("        //todo implement it");
                if (ret.equals("boolean"))
                    pw.println("        return false;");
                else if (ret.equals("int") || ret.equals("long") || ret.equals("float") ||
                    ret.equals("double") || ret.equals("byte") || ret.equals("short") || ret.equals("char"))
                    pw.println("        return 0;");
                else if (!ret.equals("void"))
                    pw.println("        return null;");
                pw.println("    }");
            }
        }
        pw.println("}");
        pw.flush();
        pw.close();
    }
}
