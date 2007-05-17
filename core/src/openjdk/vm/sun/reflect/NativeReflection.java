/*
 * $Id$
 */
package sun.reflect;

import org.jnode.vm.VmSystem;

/**
 * @see sun.reflect.Reflection
 * @author Levente Sántha
 */
class NativeReflection {
    /**
     *
     * @param realFramesToSkip
     * @return
     * @see Reflection#getCallerClass(int)
     */
    static Class getCallerClass(int realFramesToSkip){
        //todo test it
        return VmSystem.getClassContext()[realFramesToSkip];
    }

    /**
     *
     * @param c
     * @return
     * @see Reflection#getClassAccessFlags(Class)
     */
    static int getClassAccessFlags(Class c) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
}

