/*
 * $Id$
 */
package org.jnode.test;

import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ViewMethodTest {

    public static void main(String[] args) throws ClassNotFoundException {
        final String className = args[ 0];
        final String mname = (args.length > 1) ? args[ 1] : null;

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        final VmType cls = cl.loadClass(className).getVmClass();

        final int cnt = cls.getNoDeclaredMethods();
        for (int i = 0; i < cnt; i++) {
            final VmMethod method = cls.getDeclaredMethod(i);
            if ((mname == null) || method.getName().equals(mname)) {
                System.out.println("OptL: " + method.getNativeCodeOptLevel());
                System.out.println("Code: " + method.getDefaultCompiledCode());
            }
        }

    }
}
