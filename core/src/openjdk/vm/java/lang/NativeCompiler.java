/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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
 
package java.lang;

import org.jnode.vm.classmgr.VmType;

/**
 * @author Levente S\u00e1ntha
 */
public class NativeCompiler {
    /**
     * @see Compiler#initialize()
     */
    private static void initialize(){
        //todo
    }

    /**
     * @see Compiler#registerNatives()
     */
    private static void registerNatives(){
        //todo
    }

    /**
     * @see Compiler@compileClass
     */
    public static boolean compileClass(Class<?> clazz){
        //todo implement it
        return false;
    }

    /**
     * @see Compiler#compileClasses(String)
     */
    public static boolean compileClasses(String string){
        //todo implement it
        return false;
    }

    private static final String err_no_class = "Class '%s' not found%n";
    private static final String fmt_out = "Compiling %d methods took %dms%n";

    /**
     * @see Compiler#command(Object)
     */
    public static Object command(Object any) {
        if (!(any instanceof String[])) {
            return "Nothing to do.";
        }

        String[] args = (String[]) any;
        if (args.length == 0)
            return "Nothing to do.";

        String className = args[0];
        int level = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        boolean test = args.length > 2 && Boolean.parseBoolean(args[2]);
        String methodName = args.length > 3 ? args[3] : null;
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final Class<?> cls;
        try {
            cls = cl.loadClass(className);
            final VmType<?> type = VmType.fromClass((Class<?>) cls);
            if (!type.isCpRefsResolved()) {
                type.resolveCpRefs();
            }
            final long start = System.currentTimeMillis();
            final int count;
            if (methodName == null) {
                count = type.compileRuntime(level, test);
            } else {
                count = type.compileRuntime(methodName, level, test);
            }
            final long end = System.currentTimeMillis();
            return String.format(fmt_out, count, (end - start));
        } catch (ClassNotFoundException ex) {
            return String.format(err_no_class, className);
        }
    }

    /**
     * @see Compiler#enable()
     */
    public static void enable(){
        //todo
    }

    /**
     * @see Compiler#disable()
     */
    public static void disable(){
        //todo
    }
}
