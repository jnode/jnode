/*
 * $Id$
 */
package java.util;

import org.jnode.vm.VmSystem;

/**
 * @author Levente S\u00e1ntha
 */
class NativeResourceBundle {
    static Class[] getClassContext(){
        //skip the call to VmSystem.getRealClassContext() 
        Class[] context = VmSystem.getRealClassContext();
        Class[] ret = new Class[context.length - 2];
        System.arraycopy(context, 2, ret, 0, ret.length);

        return ret;
    }
}
