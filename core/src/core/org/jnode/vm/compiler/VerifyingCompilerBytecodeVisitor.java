/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.compiler;

import org.jnode.vm.classmgr.VmConstIMethodRef;
import org.jnode.vm.classmgr.VmConstMethodRef;
import org.jnode.vm.classmgr.VmMethod;

/**
 * Verifier
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VerifyingCompilerBytecodeVisitor<T extends CompilerBytecodeVisitor>
    extends DelegatingCompilerBytecodeVisitor<T> {

    private VmMethod currentMethod;
    private boolean currentKernelSpace;
    private boolean currentUninterruptible;

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
        this.currentUninterruptible = method.isUninterruptible();
        super.startMethod(method);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor
     * #visit_invokeinterface(org.jnode.vm.classmgr.VmConstIMethodRef, int)
     */
    @Override
    public void visit_invokeinterface(VmConstIMethodRef methodRef, int count) {
        verifyInvoke(methodRef);
        super.visit_invokeinterface(methodRef, count);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor
     * #visit_invokespecial(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    @Override
    public void visit_invokespecial(VmConstMethodRef methodRef) {
        verifyInvoke(methodRef);
        super.visit_invokespecial(methodRef);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor
     * #visit_invokestatic(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    @Override
    public void visit_invokestatic(VmConstMethodRef methodRef) {
        verifyInvoke(methodRef);
        super.visit_invokestatic(methodRef);
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor
     * #visit_invokevirtual(org.jnode.vm.classmgr.VmConstMethodRef)
     */
    @Override
    public void visit_invokevirtual(VmConstMethodRef methodRef) {
        verifyInvoke(methodRef);
        super.visit_invokevirtual(methodRef);
    }


    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_monitorenter()
     */
    @Override
    public void visit_monitorenter() {
        verifyMonitor();
        super.visit_monitorenter();
    }

    /**
     * @see org.jnode.vm.compiler.DelegatingCompilerBytecodeVisitor#visit_monitorexit()
     */
    @Override
    public void visit_monitorexit() {
        verifyMonitor();
        super.visit_monitorexit();
    }

    /**
     * Verify the invoke of the given method from the current method.
     *
     * @param methodRef
     */
    protected final void verifyInvoke(VmConstMethodRef methodRef) {
        if (currentKernelSpace || currentUninterruptible) {
            // May only call methods with kernelspace pragma.
            methodRef.resolve(currentMethod.getDeclaringClass().getLoader());

            final VmMethod callee = methodRef.getResolvedVmMethod();
            if (currentKernelSpace) {
                if (!callee.hasKernelSpacePragma()) {
                    // throw new ClassFormatError("Method '" + currentMethod +
                    // "' calls method outside KernelSpace: " + callee);
                    System.out.println("Method calls method outside KernelSpace:\n\tcaller: " +
                        currentMethod.getFullName() + "\n\tcallee: " +
                        callee.getFullName());
                }
            }
            if (currentUninterruptible) {
                if (!callee.isUninterruptible()) {
                    if (currentMethod.getDeclaringClass().getName().startsWith("org.jnode.vm.schedule")) {
                        // throw new ClassFormatError("Method '" + currentMethod +
                        // "' calls interruptible method: " + callee);
                        System.out.println("Method calls interruptible method:\n\tcaller: " +
                            currentMethod.getFullName() + "\n\tcallee: " +
                            callee.getFullName());
                    }
                }
            }
            if (callee.isSynchronized()) {
                //throw new ClassFormatError("Method '" + currentMethod + "' calls synchronized method: " + callee);
                System.out.println(
                    "Method '" + currentMethod.getFullName() + "' calls synchronized method: " + callee.getFullName());
            }
        }
    }

    /**
     * Verify the monitor usage of the current method.
     */
    protected final void verifyMonitor() {
        if (currentKernelSpace) {
            //throw new ClassFormatError("Method '" + currentMethod + "' uses monitors");
            System.out.println("Method '" + currentMethod.getFullName() + "' uses monitors");
        }
    }
}
