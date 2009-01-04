/*
 * $Id$
 */
package java.lang;

import java.io.PrintStream;
import java.io.InputStream;
import java.util.Properties;
import org.jnode.vm.VmSystem;
import gnu.classpath.SystemProperties;


/**
 * @author Levente S\u00e1ntha
 */
public class NativeSystem {


    private static void registerNatives() {
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
