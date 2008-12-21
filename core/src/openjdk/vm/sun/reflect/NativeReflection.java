/*
 * $Id$
 */
package sun.reflect;

import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.VmType;

/**
 * @see sun.reflect.Reflection
 * @author Levente S\u00e1ntha
 */
class NativeReflection {
    /**
     *
     * @param realFramesToSkip
     * @return
     * @see Reflection#getCallerClass(int)
     */
    static Class getCallerClass(int realFramesToSkip){
        // using realFramesToSkip + 1 to skip VmSystem 
        return VmSystem.getRealClassContext()[realFramesToSkip + 1];
    }

    /**
     *
     * @param c
     * @return
     * @see Reflection#getClassAccessFlags(Class)
     */
    static int getClassAccessFlags(Class c) {
        return VmType.fromClass(c).getAccessFlags();
    }
}

