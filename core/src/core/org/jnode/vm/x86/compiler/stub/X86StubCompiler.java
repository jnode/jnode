/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.stub;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.X86Stream;
import org.jnode.vm.Address;
import org.jnode.vm.Unsafe;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.compiler.CompilerBytecodeVisitor;
import org.jnode.vm.x86.X86CpuID;
import org.jnode.vm.x86.compiler.AbstractX86Compiler;
import org.jnode.vm.x86.compiler.X86CompilerContext;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86JumpTable;


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
	public void compileRuntime(VmMethod method, ObjectResolver resolver, int level, NativeStream os) {
		if (method.isAbstract()) {
			super.compileRuntime(method, resolver, level, os);
		} else if (X86CompilerHelper.isClassInitializeNeeded(method)) {
			// Create the code to initialize and call the interpreter
			super.compileRuntime(method, resolver, level, os);
		} else {
			// Only set the code address of the interpreter
			final Address intrAddr = X86JumpTable.getJumpTableEntry(X86JumpTable.VM_INVOKE_METHOD_AFTER_RECOMPILE_OFS);
			final VmCompiledCode code = new VmCompiledCode(this, null, intrAddr, null, 0, null, null, null);
			method.setCompiledCode(code, level);
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
			final AbstractX86Stream os = (AbstractX86Stream) nos;
			final X86CompilerContext context = getContext();
			// Create the helper
			final CCompilerHelper ih = new CCompilerHelper(os, context, isBootstrap);
			// Start an "object"
			final NativeStream.ObjectInfo objectInfo = os.startObject(context.getVmMethodCodeClass());
			// Start the code creation
			cm.setCodeStart(os.setObjectRef(new Label(method.getMangledName() + "$$start")));
			// Initialize the class
			ih.writeClassInitialize(method, EAX, ECX);
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
		X86Stream os = new X86Stream((X86CpuID)Unsafe.getCurrentProcessor().getCPUID(), 0, 16, 64, 8);
		os.setResolver(resolver);
		return os;
	}

}
