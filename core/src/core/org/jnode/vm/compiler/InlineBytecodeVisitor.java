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
     * An inlined method header will follow next.
     * @param inlinedMethod
     * @param newMaxLocals The new maxlocals count. 
     */
    public abstract void startInlinedMethodHeader(VmMethod inlinedMethod, int newMaxLocals);
    
    /**
     * An inlined method code will follow next.
     * @param inlinedMethod
     * @param newMaxLocals The new maxlocals count. 
     */
    public abstract void startInlinedMethodCode(VmMethod inlinedMethod, int newMaxLocals);
    
    /**
     * An inlined method has ended.
     * @param previousMethod The method we're continuing with
     */
    public abstract void endInlinedMethod(VmMethod previousMethod);
    
    /**
     * Leave the values on the stack and jump to the end of the inlined method.
     */
    public abstract void visit_inlinedReturn();
}
