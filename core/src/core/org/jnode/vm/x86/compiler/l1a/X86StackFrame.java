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

import org.jnode.assembler.Label;
import org.jnode.assembler.NativeStream.ObjectRef;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86BinaryAssembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.vm.classmgr.VmByteCode;
import org.jnode.vm.classmgr.VmInterpretedExceptionHandler;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmMethodCode;
import org.jnode.vm.compiler.CompiledExceptionHandler;
import org.jnode.vm.compiler.CompiledMethod;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerContext;
import org.jnode.vm.x86.compiler.X86CompilerHelper;
import org.jnode.vm.x86.compiler.X86JumpTable;
import org.vmmagic.pragma.LoadStaticsPragma;

/**
 * Utility class for generating the X86 method stack frame
 * 
 * @author epr
 */
class X86StackFrame implements X86CompilerConstants {

	private final VmMethod method;

	private final X86Assembler os;

	private final X86CompilerHelper helper;

	private final X86CompilerContext context;

	private final CompiledMethod cm;

	private final VmByteCode bc;

	/** Label of the footer */
	private final Label footerLabel;

	/** Label at start of method init code */
	private final Label initLabel;

	/** Label at start of actual method code */
	private final Label startCodeLabel;

	private X86BinaryAssembler.ObjectInfo codeObject;

	private static final int EbpFrameRefOffset = 8;

	private static final int EbpMethodRefOffset = 0;

	/**
	 * Number of byte on the stack occupied by saved registers. See
	 * {@link #saveRegisters()}
	 */
	private static final int SAVED_REGISTERSPACE = 0 * 4;

	/**
	 * Create a new instance
	 * 
	 * @param os
	 * @param method
	 * @param context
	 * @param cm
	 */
	public X86StackFrame(X86Assembler os, X86CompilerHelper helper,
			VmMethod method, X86CompilerContext context, CompiledMethod cm) {
		this.os = os;
		this.helper = helper;
		this.method = method;
		this.context = context;
		this.cm = cm;
		this.bc = method.getBytecode();
		this.initLabel = helper.genLabel("$$init");
		this.startCodeLabel = helper.genLabel("$$code");
		this.footerLabel = helper.genLabel("$$footer");
	}

	/**
	 * Emit code to create the stack frame
	 * 
	 * @return The length of os at the start of the method code.
	 */
	public int emitHeader() {

		final VmMethodCode code = new VmMethodCode();
		final Label startLabel = helper.genLabel("$$start");
		codeObject = os.startObject(context.getVmMethodCodeClass());
		os.setObjectRef(code);
		cm.setCodeStart(os.setObjectRef(startLabel));
		final int rc = os.getLength();

		if (false) {
			// Debug only
			os.writeBreakPoint();
		}
		
		// Jump to init code
		os.writeJMP(initLabel);
		// Set startCode label
		os.setObjectRef(startCodeLabel);

		return rc;
	}

	/**
	 * Emit code to end the stack frame
	 */
	public void emitTrailer(int maxLocals) {
		final int argSlotCount = method.getArgSlotCount();
		final Label stackOverflowLabel = helper.genLabel("$$stack-overflow");

		// Begin footer
		// Now start the actual footer
		os.setObjectRef(footerLabel);

		/* Go restore the previous current frame */
		emitSynchronizationCode(context.getMonitorExitMethod());
		os.writeLEA(X86Register.ESP, X86Register.EBP, EbpFrameRefOffset);
		os.writePOP(X86Register.EBP);
		restoreRegisters();
		// Return
		if (argSlotCount > 0) {
			os.writeRET(argSlotCount * 4);
		} else {
			os.writeRET();
		}
		// End footer

		// Begin header
        // Init starts here
        os.setObjectRef(initLabel);
        
        // Test stack overflow        
        final int stackEndOffset = context.getVmProcessorStackEnd().getOffset();
        os.writePrefix(X86Constants.FS_PREFIX);
        os.writeCMP_MEM(X86Register.ESP, stackEndOffset);
        os.writeJCC(stackOverflowLabel, X86Constants.JLE);
		
		// Create class initialization code (if needed)
		helper.writeClassInitialize(method, X86Register.EAX);

		// Increment the invocation count
		helper.writeIncInvocationCount(X86Register.EAX);

		// Fixed framelayout
		saveRegisters();
		os.writePUSH(X86Register.EBP);
		os.writePUSH(context.getMagic());
		//os.writePUSH(0); // PC, which is only used in interpreted methods
		/** EAX MUST contain the VmMethod structure upon entry of the method */
		os.writePUSH(X86Register.EAX);
		os.writeMOV(INTSIZE, X86Register.EBP, X86Register.ESP);

		// Emit the code to create the locals
		final int noLocalVars = maxLocals - argSlotCount;
		// Create and clear all local variables
		if (noLocalVars > 0) {
			os.writeXOR(X86Register.EAX, X86Register.EAX);
			for (int i = 0; i < noLocalVars; i++) {
				os.writePUSH(X86Register.EAX);
			}
		}

		// Load the statics table reference
		if (method.canThrow(LoadStaticsPragma.class)) {
			helper.writeLoadSTATICS(helper.genLabel("$$edi"), "init", false);
		}

		/* Create the synchronization enter code */
		emitSynchronizationCode(context.getMonitorEnterMethod());
		
		// And jump back to the actual code start
		os.writeJMP(startCodeLabel);

		// Write stack overflow code
		os.setObjectRef(stackOverflowLabel);
        os.writeINT(0x31);
        // End header       

		// No set the exception start&endPtr's
		//final int noLocals = bc.getNoLocals();
		//final int noLocalVars = noLocals - noArgs;
		final int count = bc.getNoExceptionHandlers();
		CompiledExceptionHandler[] ceh = new CompiledExceptionHandler[count];
		for (int i = 0; i < count; i++) {
			final VmInterpretedExceptionHandler eh = bc.getExceptionHandler(i);
			final Label handlerLabel = helper.genLabel("$$ex-handler" + i);

			final ObjectRef handlerRef = os.setObjectRef(handlerLabel);

			/** Clear the calculation stack (only locals are left) */
			if (noLocalVars < 0) {
				System.out.println("@#@#@#@# noLocalVars = " + noLocalVars);
			}
			final int ofs = Math.max(0, noLocalVars) * 4;
			os.writeLEA(X86Register.ESP, X86Register.EBP, -ofs);
			/** Push the exception in EAX */
			os.writePUSH(X86Register.EAX);
			/** Goto the real handler */
			os.writeJMP(helper.getInstrLabel(eh.getHandlerPC()));

			ceh[i] = new CompiledExceptionHandler();
			ceh[i].setStartPc(os.getObjectRef(helper.getInstrLabel(eh
					.getStartPC())));
			ceh[i].setEndPc(os
					.getObjectRef(helper.getInstrLabel(eh.getEndPC())));
			ceh[i].setHandler(handlerRef);

		}
		cm.setExceptionHandlers(ceh);

		// Now create the default exception handler
		Label handlerLabel = helper.genLabel("$$def-ex-handler");
		cm.setDefExceptionHandler(os.setObjectRef(handlerLabel));
		emitSynchronizationCode(context.getMonitorExitMethod());
		os.writeLEA(X86Register.ESP, X86Register.EBP, EbpFrameRefOffset);
		os.writePOP(X86Register.EBP);
		restoreRegisters();
		/**
		 * Do not do a ret here, this way the return address will be used by
		 * vm_athrow as its return address
		 */
		helper.writeJumpTableJMP(X86JumpTable.VM_ATHROW_NOTRACE_OFS);
		//os.writeJMP(helper.VM_ATHROW_NOTRACE);

		codeObject.markEnd();
		cm.setCodeEnd(os.setObjectRef(helper.genLabel("$$end-code-object")));
	}

	/**
	 * Emit a jump to the exit code
	 */
	public void emitReturn() {
		os.writeJMP(footerLabel);
	}

	/**
	 * Gets the offset to EBP (current stack frame) for the local with the given
	 * index.
	 * 
	 * @param index
	 * @return int
	 */
	public final int getEbpOffset(int index) {
		int noArgs = method.getArgSlotCount();
		if (index < noArgs) {
			// Index refers to a method argument
			return ((noArgs - index + 1) * 4) + EbpFrameRefOffset
					+ SAVED_REGISTERSPACE;
		} else {
			// Index refers to a local variable
			return (index - noArgs + 1) * -4;
		}
	}

	/**
	 * Gets the offset to EBP (current stack frame) for the wide local with the
	 * given index.
	 * 
	 * @param index
	 * @return int
	 */
	public final int getWideEbpOffset(int index) {
		return getEbpOffset(index + 1);
	}

	private void emitSynchronizationCode(VmMethod monitorMethod) {
		if (method.isSynchronized()) {
			os.writePUSH(X86Register.EAX);
			os.writePUSH(X86Register.EDX);
			//System.out.println("synchr. " + method);
			if (method.isStatic()) {
				// Get declaring class
				final int declaringClassOffset = context
						.getVmMemberDeclaringClassField().getOffset();
				writeGetMethodRef(X86Register.EAX);
				os.writePUSH(X86Register.EAX, declaringClassOffset);
				//os.writePUSH(method.getDeclaringClass());
			} else {
				os.writePUSH(X86Register.EBP, getEbpOffset(0));
			}
			helper.invokeJavaMethod(monitorMethod);
			os.writePOP(X86Register.EDX);
			os.writePOP(X86Register.EAX);
		}
	}

	/**
	 * Push the method reference in the current stackframe onto the stack
	 */
	public final void writePushMethodRef() {
		os.writePUSH(X86Register.EBP, EbpMethodRefOffset);
	}

	/**
	 * Write code to copy the method reference into the dst register.
	 */
	public final void writeGetMethodRef(GPR dst) {
		os.writeMOV(INTSIZE, dst, X86Register.EBP, EbpMethodRefOffset);
	}

	/**
	 * Write code to save the callee saved registers.
	 * 
	 * @see #SAVED_REGISTERSPACE
	 * @see org.jnode.vm.x86.VmX86StackReader
	 */
	private final void saveRegisters() {
		//os.writePUSH(Register.EBX);
		//os.writePUSH(Register.EDI);
		//os.writePUSH(Register.ESI);
	}

	/**
	 * Write code to restore the callee saved registers.
	 * 
	 * @see #SAVED_REGISTERSPACE
	 * @see org.jnode.vm.x86.VmX86StackReader
	 */
	private final void restoreRegisters() {
		//os.writePOP(Register.ESI);
		//os.writePOP(Register.EDI);
		//os.writePOP(Register.EBX);
	}
}
