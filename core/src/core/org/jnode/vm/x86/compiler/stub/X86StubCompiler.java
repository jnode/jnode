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
 
package org.jnode.vm.x86.compiler.stub;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.CompilerBytecodeVisitor;
import org.jnode.vm.compiler.EntryPoints;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.AbstractX86Compiler;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86JumpTable;
import org.vmmagic.pragma.PrivilegedActionPragma;
import org.vmmagic.unboxed.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86StubCompiler extends AbstractX86Compiler {

	/**
	 * Compile the given method during runtime.
	 * 
	 * @param method
	 * @param resolver
	 * @param level
	 *            Optimization level
	 * @param os
	 *            The native stream, can be null
	 */
	public void compileRuntime(VmMethod method, ObjectResolver resolver, int level, NativeStream os) throws PrivilegedActionPragma {
		if (method.isAbstract()) {
			super.compileRuntime(method, resolver, level, os);
		} else if (X86CompilerHelper.isClassInitializeNeeded(method)) {
			// Create the code to initialize and call the interpreter
			super.compileRuntime(method, resolver, level, os);
		} else {
			// Only set the code address of the interpreter
			final Address intrAddr = Unsafe.getJumpTableEntry(X86JumpTable.VM_INVOKE_METHOD_AFTER_RECOMPILE_IDX);
			final VmCompiledCode code = Vm.getCompiledMethods().createCompiledCode(null, method, this, null, intrAddr.toAddress(), null, 0, null, null, null);
			method.addCompiledCode(code, level);
		}
	}

	/**
	 * Compile the given method into the given stream.
	 * 
	 * @param method
	 * @param nos
	 * @param level
	 *            Optimization level
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
			final CCompilerHelper ih = new CCompilerHelper(os, null, context, isBootstrap);
			// Start an "object"
			final NativeStream.ObjectInfo objectInfo = os.startObject(context.getVmMethodCodeClass());
			// Start the code creation
			cm.setCodeStart(os.setObjectRef(new Label(method.getMangledName() + "$$start")));
			// Initialize the class
			ih.writeClassInitialize(method);
			// Call the interpreter
			ih.emitInvokeMethodAtferRecompile();
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
	protected CompilerBytecodeVisitor createBytecodeVisitor(VmMethod method, CompiledMethod cm, NativeStream os, int level, boolean isBootstrap) {
		return null;
	}

	/**
	 * Create a native stream for the current architecture.
	 * 
	 * @param resolver
	 * @return NativeStream
	 */
	public NativeStream createNativeStream(ObjectResolver resolver) {
		X86BinaryAssembler os = new X86BinaryAssembler((X86CpuID)Unsafe.getCurrentProcessor().getCPUID(), getMode(), 0, 16, 64, 8);
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
     * @return
     */
    public String getName() {
        return "X86-Stub";
    }
}
