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
 
package org.jnode.vm.x86.compiler.l1b;

import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.CompilerBytecodeVisitor;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.compiler.GCMapIterator;
import org.jnode.vm.compiler.InlineBytecodeVisitor;
import org.jnode.vm.compiler.OptimizingBytecodeVisitor;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.AbstractX86Compiler;

/**
 * Native code compiler for the Intel x86 architecture.
 * <p/>
 * <pre>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * HIGH VALUES first-arg ... last-arg old EIP old EBP magic method local 0 ... local n calculation stack LOW VALUES
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <h1>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 *     long entries
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * </h1>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 *       PUSH: MSB, LSB POP: LSB, MSB
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * <p/>
 * </pre>
 */
public final class X86Level1BCompiler extends AbstractX86Compiler {

    /**
     * Should this compiler try to inline methods?
     */
    private final boolean inlineMethods = true;
    private final MagicHelper magicHelper = new MagicHelper();

    /**
     * Initialize this instance.
     */
    public X86Level1BCompiler() {
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
    protected CompilerBytecodeVisitor createBytecodeVisitor(VmMethod method,
                                                            CompiledMethod cm, NativeStream os, int level,
                                                            boolean isBootstrap) {
        final InlineBytecodeVisitor cbv;
        final EntryPoints entryPoints = getEntryPoints();
        cbv = new X86BytecodeVisitor(os, cm, isBootstrap, entryPoints, magicHelper, getTypeSizeInfo());
        if (inlineMethods /*&& ((X86Assembler)os).isCode32()*/) {
            final VmClassLoader loader = method.getDeclaringClass().getLoader();
            return new OptimizingBytecodeVisitor(entryPoints, cbv, loader);
        } else {
            return cbv;
        }
    }

    /**
     * Create a native stream for the current architecture.
     *
     * @param resolver
     * @return NativeStream
     */
    public NativeStream createNativeStream(ObjectResolver resolver) {
        X86CpuID cpuid = (X86CpuID) VmProcessor.current().getCPUID();
        X86BinaryAssembler os = new X86BinaryAssembler(cpuid, getMode(), 0);
        os.setResolver(resolver);
        return os;
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#getMagic()
     */
    public final int getMagic() {
        return L1B_COMPILER_MAGIC;
    }

    /**
     * Gets the name of this compiler.
     *
     * @return The name of this compiler
     */
    public String getName() {
        return "X86-L1B";
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#createGCMapIterator()
     */
    @Override
    public GCMapIterator createGCMapIterator() {
        return new X86GCMapIterator();
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#getCompilerPackages()
     */
    @Override
    public String[] getCompilerPackages() {
        return new String[]{"org.jnode.vm.x86.compiler", "org.jnode.vm.x86.compiler.l1b"};
    }
}
