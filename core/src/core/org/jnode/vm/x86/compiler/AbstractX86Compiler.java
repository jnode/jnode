/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.vm.x86.compiler;

import java.io.Writer;
import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86TextAssembler;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.PrivilegedActionPragma;
import org.jnode.vm.classmgr.TypeSizeInfo;
import org.jnode.vm.classmgr.VmClassLoader;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.x86.VmX86Architecture;
import org.jnode.vm.x86.X86CpuID;
import org.vmmagic.unboxed.Address;

/**
 * Abstract native code compiler for the Intel X86 architecture.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class AbstractX86Compiler extends NativeCodeCompiler implements
    X86CompilerConstants {

    private EntryPoints context;

    private X86Constants.Mode mode;

    private TypeSizeInfo typeSizeInfo;

    private final ThreadLocal nativeStreamHolder = new ThreadLocal();

    /**
     * Initialize this compiler
     *
     * @param loader
     */
    public final void initialize(VmClassLoader loader) {
        if (context == null) {
            context = new EntryPoints(loader, Vm.getHeapManager(), getMagic());
            mode = ((VmX86Architecture) loader.getArchitecture()).getMode();
            typeSizeInfo = loader.getArchitecture().getTypeSizeInfo();
        }
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#createNativeStream(org.jnode.assembler.ObjectResolver)
     */
    public NativeStream createNativeStream(ObjectResolver resolver) {
        X86BinaryAssembler os;
        os = (X86BinaryAssembler) nativeStreamHolder.get();
        if (os == null) {
            os = new X86BinaryAssembler((X86CpuID) VmProcessor.current()
                .getCPUID(), mode, 0);
            os.setResolver(resolver);
            nativeStreamHolder.set(os);
        }
        return os;
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#doCompileAbstract(org.jnode.vm.classmgr.VmMethod,
     *      org.jnode.assembler.NativeStream, int, boolean)
     */
    @PrivilegedActionPragma
    protected final CompiledMethod doCompileAbstract(VmMethod method,
                                                     NativeStream nos, int level, boolean isBootstrap) {
        if (isBootstrap) {
            // System.out.println("Abstraxct method " + method);
            final CompiledMethod cm = new CompiledMethod(level);
            final X86Assembler os = (X86Assembler) nos;
            // Create the helper
            final X86CompilerHelper helper = new X86CompilerHelper(os, null,
                context, isBootstrap);
            // Start an "object"
            final NativeStream.ObjectInfo objectInfo = os.startObject(context
                .getVmMethodCodeClass());
            // Start the code creation
            cm.setCodeStart(os.setObjectRef(new Label(method.getMangledName()
                + "$$abstract-start")));
            // Call abstract method error method
            helper.writeJumpTableJMP(X86JumpTable.VM_INVOKE_ABSTRACT_IDX);
            // Close the "object"
            objectInfo.markEnd();
            // The end
            cm.setCodeEnd(os.setObjectRef(new Label(method.getMangledName()
                + "$$abstract-end")));

            return cm;
        } else {
            // Set the address of the abstract method code
            final Address errorAddr = Unsafe
                .getJumpTableEntry(X86JumpTable.VM_INVOKE_ABSTRACT_IDX);
            final VmCompiledCode code = Vm.getCompiledMethods()
                .createCompiledCode(null, method, this, null,
                    errorAddr.toAddress(), null, 0, null, null, null);
            method.addCompiledCode(code, level);
            return null;
        }
    }

    /**
     * @return Returns the context.
     */
    protected final EntryPoints getEntryPoints() {
        return this.context;
    }

    /**
     * Gets the operating mode.
     *
     * @return
     */
    protected final X86Constants.Mode getMode() {
        return mode;
    }

    public void disassemble(VmMethod method, ObjectResolver resolver,
                            int level, Writer writer) {

        if (method.isNative()) {
            System.out.println(method + " is native");
            return;
        }

        if (method.isAbstract()) {
            System.out.println(method + " is abstract");
            return;
        }

        X86TextAssembler tos = new X86TextAssembler(writer, (X86CpuID) VmProcessor.current().getCPUID(), mode);

        doCompile(method, tos, level, false);
        try {
            tos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @see org.jnode.vm.compiler.NativeCodeCompiler#dumpStatistics()
     */
    public void dumpStatistics() {
    }

    /**
     * Gets the type size information.
     *
     * @return
     */
    public final TypeSizeInfo getTypeSizeInfo() {
        return typeSizeInfo;
    }
}
