/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VmSpecialMethod extends VmStaticMethod {

    /**
     * @param name
     * @param signature
     * @param modifiers
     * @param declaringClass
     */
    VmSpecialMethod(String name, String signature, int modifiers,
            VmType declaringClass) {
        super(name, signature, modifiers | Modifier.ACC_SPECIAL,
                declaringClass);
    }

}
