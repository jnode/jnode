/**
 * $Id$
 */

package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.vm.Unsafe;
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
 *  HIGH VALUES first-arg ... last-arg old EIP old EBP magic method local 0 ... local n calculation stack LOW VALUES
 *   
 * <h1>long entries</h1>
 *  PUSH: MSB, LSB POP: LSB, MSB
 * </pre>
 */
public class X86Level1ACompiler extends AbstractX86Compiler {

    /** Should this compiler try to inline methods? */
    private final boolean inlineMethods = true;

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
        InlineBytecodeVisitor cbv = new X86BytecodeVisitor(os, cm, isBootstrap,
                getContext());
        if (inlineMethods) {
            return new InliningBytecodeVisitor(cbv, method.getDeclaringClass()
                    .getLoader());
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
        X86Stream os = new X86Stream((X86CpuID) Unsafe.getCurrentProcessor()
                .getCPUID(), 0);
        os.setResolver(resolver);
        return os;
    }
    
    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#getMagic()
     */
    public final int getMagic() {
        return L1_COMPILER_MAGIC;
    }
}
