/**
 * $Id$
 */
package org.jnode.vm.compiler;

import java.util.Iterator;

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream;
import org.jnode.assembler.ObjectResolver;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.vm.Address;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.bytecode.BasicBlock;
import org.jnode.vm.bytecode.BytecodeParser;
import org.jnode.vm.bytecode.ControlFlowGraph;
import org.jnode.vm.classmgr.AbstractVmClassLoader;
import org.jnode.vm.classmgr.VmAddressMap;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmCompiledExceptionHandler;
import org.jnode.vm.classmgr.VmConstClass;
import org.jnode.vm.classmgr.VmMethod;

/**
 * Abstract native code compiler.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class NativeCodeCompiler extends VmSystemObject {

	/**
	 * Compile the given method during bootstrapping
	 * 
	 * @param method
	 * @param os
	 * @param level
	 *            Optimization level
	 */
	public final void compileBootstrap(VmMethod method, NativeStream os, int level) {
		int start = os.getLength();
		final CompiledMethod cm;
		final boolean abstractM = method.isAbstract();
		if (abstractM) {
			if (method.isStatic()) {
				throw new Error("Abstract & static");
			}
			if (method.isNative()) {
				throw new Error("Abstract & native");
			}
			cm = doCompileAbstract(method, os, level, true);
			if (cm == null) {
				return;
			}
		} else {
			cm = doCompile(method, os, level, true);
		}
		int end = os.getLength();

		final Address nativeCode = (Address) cm.getCodeStart().getObject();
		final VmCompiledExceptionHandler[] eTable;
		final Address defExHandler;
		final VmByteCode bc;
		final VmAddressMap aTable = cm.getAddressTable();

		if (!(method.isNative() || abstractM)) {
			final NativeStream.ObjectRef defExHRef = cm.getDefExceptionHandler();
			if (defExHRef != null) {
				defExHandler = (Address) defExHRef.getObject();
			} else {
				defExHandler = null;
			}
			bc = method.getBytecode();
			final CompiledExceptionHandler[] ceh = cm.getExceptionHandlers();
			if (ceh != null) {
				eTable = new VmCompiledExceptionHandler[ceh.length];
				for (int i = 0; i < ceh.length; i++) {

					final VmConstClass catchType = bc.getExceptionHandler(i).getCatchType();
					final Address startPtr = (Address) ceh[i].getStartPc().getObject();
					final Address endPtr = (Address) ceh[i].getEndPc().getObject();
					final Address handler = (Address) ceh[i].getHandler().getObject();
					eTable[i] = new VmCompiledExceptionHandler(catchType, startPtr, endPtr, handler);
				}
			} else {
				eTable = null;
			}
		} else {
			eTable = null;
			defExHandler = null;
			bc = null;
		}

		method.setCompiledCode(new VmCompiledCode(bc, nativeCode, null, end - start, eTable, defExHandler, aTable), level);
	}

	/**
	 * Compile the given method during runtime.
	 * 
	 * @param method
	 * @param resolver
	 * @param allocator
	 * @param level
	 *            Optimization level
	 * @param os
	 *            The native stream, can be null
	 */
	public void compileRuntime(VmMethod method, ObjectResolver resolver, int level, NativeStream os) {

		if (method.isNative()) {
			throw new IllegalArgumentException("Cannot compile native methods");
		}

		//long start = System.currentTimeMillis();

		//System.out.println("Compiling " + method);

		if (os == null) {
			os = createNativeStream(resolver);
		}
		final CompiledMethod cm;
		if (method.isAbstract()) {
			cm = doCompileAbstract(method, os, level, false);
			if (cm == null) {
				return;
			}
		} else {
			cm = doCompile(method, os, level, false);
		}

		try {
			final int startOffset = cm.getCodeStart().getOffset();
			final int size = cm.getCodeEnd().getOffset() - startOffset;
			final byte[] code = new byte[size];
			System.arraycopy(os.getBytes(), startOffset, code, 0, size);
			final Address codePtr = resolver.addressOfArrayData(code);

			final NativeStream.ObjectRef defExHRef = cm.getDefExceptionHandler();
			final Address defExHandler;
			if (defExHRef != null) {
				defExHandler = resolver.add(codePtr, cm.getDefExceptionHandler().getOffset() - startOffset);
			} else {
				defExHandler = null;
			}
			final VmCompiledExceptionHandler[] eTable;
			final VmAddressMap aTable = cm.getAddressTable();

			final VmByteCode bc = method.getBytecode();
			final CompiledExceptionHandler[] ceh = cm.getExceptionHandlers();
			if (ceh != null) {
				eTable = new VmCompiledExceptionHandler[ceh.length];
				for (int i = 0; i < ceh.length; i++) {

					final VmConstClass catchType = bc.getExceptionHandler(i).getCatchType();
					final Address startPtr = Address.add(codePtr, ceh[i].getStartPc().getOffset() - startOffset);
					final Address endPtr = Address.add(codePtr, ceh[i].getEndPc().getOffset() - startOffset);
					final Address handler = Address.add(codePtr, ceh[i].getHandler().getOffset() - startOffset);

					eTable[i] = new VmCompiledExceptionHandler(catchType, startPtr, endPtr, handler);
				}
			} else {
				eTable = null;
			}

			method.setCompiledCode(new VmCompiledCode(bc, codePtr, code, size, eTable, defExHandler, aTable), level);

			// For debugging only
			//System.out.println("Code: " + NumberUtils.hex(code));
			//System.out.println();
			//End of debugging only
		} catch (UnresolvedObjectRefException ex) {
			throw new CompileError(ex);
		}

		if (os.hasUnresolvedObjectRefs()) {
			throw new CompileError("Unresolved labels after compile!");
		}
	}

	/**
	 * Create a native stream for the current architecture.
	 * 
	 * @param resolver
	 * @return NativeStream
	 */
	public abstract NativeStream createNativeStream(ObjectResolver resolver);

	/**
	 * Compile the given method into the given stream.
	 * 
	 * @param method
	 * @param outputStream
	 * @param level
	 *            Optimization level
	 * @param isBootstrap
	 * @return The compiled method
	 */
	protected CompiledMethod doCompile(VmMethod method, NativeStream os, int level, boolean isBootstrap) {
		final CompiledMethod cm = new CompiledMethod(level);
		if (method.isNative()) {
			Object label = new Label(method.getMangledName());
			cm.setCodeStart(os.getObjectRef(label));
		} else {
			// Create the visitor
			final CompilerBytecodeVisitor bcv = createBytecodeVisitor(method, cm, os, level, isBootstrap);
			// Get the bytecode
			final VmByteCode bc = method.getBytecode();
			// Create the control flow graph
			ControlFlowGraph cfg = (ControlFlowGraph) bc.getCompilerData();
			if (cfg == null) {
				cfg = new ControlFlowGraph(bc);
				bc.setCompilerData(cfg);
			}
			// Compile the code 1 basic block at a time
			bcv.startMethod(method);
			for (Iterator i = cfg.basicBlockIterator(); i.hasNext();) {
				final BasicBlock bb = (BasicBlock) i.next();
				bcv.startBasicBlock(bb);
				BytecodeParser.parse(bc, bcv, bb.getStartPC(), bb.getEndPC(), false);
				bcv.endBasicBlock();
			}
			bcv.endMethod();
		}

		return cm;
	}

	/**
	 * Compile the given abstract method into the given stream.
	 * 
	 * @param method
	 * @param outputStream
	 * @param level
	 *            Optimization level
	 * @param isBootstrap
	 * @return The compiled method
	 */
	protected abstract CompiledMethod doCompileAbstract(VmMethod method, NativeStream os, int level, boolean isBootstrap);

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
	protected abstract CompilerBytecodeVisitor createBytecodeVisitor(VmMethod method, CompiledMethod cm, NativeStream os, int level, boolean isBootstrap);

	/**
	 * Initialize this compiler
	 * 
	 * @param loader
	 */
	public abstract void initialize(AbstractVmClassLoader loader);

	/**
	 * Dump compiler statistics to System.out
	 */
	public abstract void dumpStatistics();
}
