/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.vm.Unsafe;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.CompilerBytecodeVisitor;
import org.jnode.vm.compiler.InlineBytecodeVisitor;
import org.jnode.vm.compiler.InliningBytecodeVisitor;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.AbstractX86Compiler;

/**
 * Native code compiler for the Intel x86 architecture.
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *      
 *       HIGH VALUES first-arg ... last-arg old EIP old EBP magic method local 0 ... local n calculation stack LOW VALUES
 *        
 *      
 *     
 *    
 *   
 *  
 * <h1>
 * 
 *  
 *   
 *    
 *     long entries
 *     
 *    
 *   
 *  
 * </h1>
 * 
 *  
 *   
 *    
 *     
 *       PUSH: MSB, LSB POP: LSB, MSB
 *      
 *     
 *    
 *   
 *  
 * </pre>
 */
public class X86Level1ACompiler extends AbstractX86Compiler {

    /** Should this compiler try to inline methods? */
    private final boolean inlineMethods = true;
    private final MagicHelper magicHelper = new MagicHelper();

    /**
     * Initialize this instance.
     */
    public X86Level1ACompiler() {
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
            CompiledMethod cm, NativeStream os, int level, boolean isBootstrap) {
        final InlineBytecodeVisitor cbv;
        cbv = new X86BytecodeVisitor(os, cm, isBootstrap, getContext(), magicHelper, getTypeSizeInfo());
        if (inlineMethods && ((X86Assembler)os).isCode32()) {
            final VmClassLoader loader = method.getDeclaringClass().getLoader();
            return new InliningBytecodeVisitor(cbv, loader);
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
        X86CpuID cpuid = (X86CpuID) Unsafe.getCurrentProcessor().getCPUID();
        X86BinaryAssembler os = new X86BinaryAssembler(cpuid, getMode(), 0);
        os.setResolver(resolver);
        return os;
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#getMagic()
     */
    public final int getMagic() {
        return L1A_COMPILER_MAGIC;
    }

    /**
     * Gets the name of this compiler.
     * 
     * @return The name of this compiler
     */
    public String getName() {
        return "X86-L1A";
    }
}
