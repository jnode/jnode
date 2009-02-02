/*
 * $Id$
 *
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
 
package org.jnode.vm.x86.compiler.stub;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.CompilerBytecodeVisitor;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.compiler.GCMapIterator;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.AbstractX86Compiler;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class X86StubCompiler extends AbstractX86Compiler {

    /**
     * Compile the given method into the given stream.
     *
     * @param method
     * @param nos
     * @param level       Optimization level
     * @param isBootstrap
     * @return The compiled method
     */
    protected CompiledMethod doCompile(VmMethod method, NativeStream nos, int level, boolean isBootstrap) {
        final CompiledMethod cm = new CompiledMethod(level);
        if (method.isNative()) {
            Object label = new Label(method.getMangledName());
            cm.setCodeStart(nos.getObjectRef(label));
        } else {
            final X86Assembler os = (X86Assembler) nos;
            final EntryPoints context = getEntryPoints();
            // Create the helper
            final X86CompilerHelper ih = new X86CompilerHelper(os, null, context, isBootstrap);
            // Start an "object"
            final NativeStream.ObjectInfo objectInfo = os.startObject(context.getVmMethodCodeClass());
            // Start the code creation
            cm.setCodeStart(os.setObjectRef(new Label(method.getMangledName() + "$$start")));

            // Setup call to {@link VmMethod#recompileMethod(int, int)}
            final VmType<?> declClass = method.getDeclaringClass();
            os.writePUSH(declClass.getSharedStaticsIndex());
            os.writePUSH(declClass.indexOf(method));
            final int recompileStatOfs = ih.getSharedStaticsOffset(context.getRecompileMethod());
            os.writeCALL(ih.STATICS, recompileStatOfs);

            // Emit jump to the newly compiled code.
            final int methodStatOfs = ih.getSharedStaticsOffset(method);
            os.writeJMP(ih.STATICS, methodStatOfs);

            // Close the "object"
            objectInfo.markEnd();
            // The end
            cm.setCodeEnd(os.setObjectRef(new Label(method.getMangledName() + "$$end")));

            // Test for unresolved objectrefs
            if (!isBootstrap && os.hasUnresolvedObjectRefs()) {
                throw new Error("There are unresolved objectrefs " + os.getUnresolvedObjectRefs());
            }
        }

        return cm;
    }

    /**
     * Create the visitor that converts bytecodes into native code.
     *
     * @param method
     * @param cm
     * @param os
     * @param level
     * @param isBootstrap
     * @return The new bytecode visitor.
     */
    protected CompilerBytecodeVisitor createBytecodeVisitor(VmMethod method, CompiledMethod cm, NativeStream os,
                                                            int level, boolean isBootstrap) {
        return null;
    }

    /**
     * Create a native stream for the current architecture.
     *
     * @param resolver
     * @return NativeStream
     */
    public NativeStream createNativeStream(ObjectResolver resolver) {
        X86BinaryAssembler os =
            new X86BinaryAssembler((X86CpuID) VmProcessor.current().getCPUID(), getMode(), 0, 16, 64, 8);
        os.setResolver(resolver);
        return os;
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#getMagic()
     */
    public final int getMagic() {
        return STUB_COMPILER_MAGIC;
    }

    /**
     * Gets the name of this compiler.
     *
     * @return
     */
    public String getName() {
        return "X86-Stub";
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#createGCMapIterator()
     */
    @Override
    public GCMapIterator createGCMapIterator() {
        return new EmptyGCMapIterator();
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#getCompilerPackages()
     */
    @Override
    public String[] getCompilerPackages() {
        return new String[]{"org.jnode.vm.x86.compiler", "org.jnode.vm.x86.compiler.stub"};
    }
}
