/*
 * $Id$
 */
package org.jnode.vm.classmgr;

/**
 * A constant object that needs to be resolved.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class VmResolvableConstObject extends VmConstObject {

    private boolean resolved = false;
    
    public VmResolvableConstObject() {
    }
    
    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     * @param clc
     */
    public void resolve(VmClassLoader clc) {
        if (!resolved) {
            doResolve(clc);
            resolved = true;
        }
    }
    
    /**
     * Returns the resolved.
     * @return boolean
     */
    public boolean isResolved() {
        return resolved;
    }

    /**
     * Resolve the references of this constant to loaded VmXxx objects.
     * @param clc
     */
    protected abstract void doResolve(VmClassLoader clc);
}
