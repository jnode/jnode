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
 
package java.lang;

import java.io.PrintStream;
import java.io.InputStream;
import java.util.Properties;
import org.jnode.vm.VmSystem;
import org.jnode.util.SystemInputStream;
import gnu.classpath.SystemProperties;


/**
 * @author Levente S\u00e1ntha
 */
public class NativeSystem {


    private static void registerNatives() {
    }

    private static InputStream makeSystemIn0() {
        return SystemInputStream.getInstance();
    }

    private static PrintStream makeSystemOut0() {
        return VmSystem.getSystemOut();
    }



    private static void setIn0(InputStream in) {
        VmSystem.setIn(in);
    }

    private static void setOut0(PrintStream out) {
        VmSystem.setOut(out);
    }

    private static void setErr0(PrintStream err) {
        VmSystem.setErr(err);
    }

    /**
     * @see System#currentTimeMillis()
     */
    private static long currentTimeMillis() {
        return VmSystem.currentTimeMillis();
    }

    /**
     * @see System#nanoTime()
     */
    private static long nanoTime() {
        return VmSystem.nanoTime();
    }

    /**
     * @see System#arraycopy(Object, int, Object, int, int)
     */
    private static void arraycopy(Object src, int srcPos,
                                  Object dest, int destPos, int length) {
        VmSystem.arrayCopy(src, srcPos, dest, destPos, length);
    }

    /**
     * @see System#identityHashCode(Object)
     */
    private static int identityHashCode(Object x) {
        return VmSystem.getHashCode(x);
    }

    private static Properties initProperties(Properties props) {
        Properties sys_prop = SystemProperties.getProperties();
        props.putAll(sys_prop);
        return props;
    }

    /**
     * @see System#mapLibraryName(String)
     */
    private static String mapLibraryName(String libname) {
        return libname;
    }
}
