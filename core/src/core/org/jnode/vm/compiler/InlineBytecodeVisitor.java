/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class InlineBytecodeVisitor extends CompilerBytecodeVisitor {

    /**
     * An inlined method will follow next.
     * @param inlinedMethod
     * @param newMaxLocals The new maxlocals count. 
     */
    public abstract void startInlinedMethod(VmMethod inlinedMethod, int newMaxLocals);
    
    /**
     * An inlined method has ended.
     * @param previousMethod The method we're continuing with
     */
    public abstract void endInlinedMethod(VmMethod previousMethod);
    
    /**
     * Leave the values on the stack and jump to the end of the inlined method.
     */
    public abstract void visit_inlinedReturn();
    
    /**
     * Can inlinedMethod be inlined in the caller method.
     * @param inlinedMethod
     * @param caller
     * @return
     */
    public abstract boolean canInline(VmMethod inlinedMethod, VmMethod caller);
    
}
