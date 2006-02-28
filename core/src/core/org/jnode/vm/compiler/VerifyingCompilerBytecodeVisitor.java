/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmMethod;

/**
 * Verifier
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VerifyingCompilerBytecodeVisitor<T extends CompilerBytecodeVisitor>
        extends DelegatingCompilerBytecodeVisitor<T> {

    private VmMethod currentMethod;
    private boolean currentKernelSpace;
    
    /**
     * @param delegate
     */
    public VerifyingCompilerBytecodeVisitor(T delegate) {
        super(delegate);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#startMethod(org.jnode.vm.classmgr.VmMethod)
     */
    @Override
    public void startMethod(VmMethod method) {
        this.currentMethod = method;
        this.currentKernelSpace = method.hasKernelSpacePragma();
        super.startMethod(method);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
     */
    @Override
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        verifyInvoke(methodRef);
        super.visit_invokeinterface(methodRef, count);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    @Override
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        verifyInvoke(methodRef);
        super.visit_invokespecial(methodRef);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    @Override
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        verifyInvoke(methodRef);
        super.visit_invokestatic(methodRef);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    @Override
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        verifyInvoke(methodRef);
        super.visit_invokevirtual(methodRef);
    }

    /**
     * Verify the invoke of the given method from the current method.
     * @param methodRef
     */
    protected final void verifyInvoke(VmConstMethodRef methodRef) {
        if (currentKernelSpace) {
            // May only call methods with kernelspace pragma.
            methodRef.resolve(currentMethod.getDeclaringClass().getLoader());
            
            final VmMethod callee = methodRef.getResolvedVmMethod();
            if (!callee.hasKernelSpacePragma()) {
                //throw new ClassFormatError("Method '" + currentMethod + "' calls method outside KernelSpace: " + callee);
                System.out.println("Method '" + currentMethod.getFullName() + "' calls method outside KernelSpace: " + callee.getFullName());
            }
        }
    }
}
