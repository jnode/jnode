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

package org.jnode.vm.x86.compiler;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
import org.jnode.assembler.x86.X86Register.GPR64;
import org.jnode.vm.JvmType;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmStaticsEntry;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.classmgr.VmTypeState;
import org.jnode.vm.memmgr.VmWriteBarrier;
import org.jnode.vm.x86.X86CpuID;
import org.vmmagic.pragma.PrivilegedActionPragma;
import org.vmmagic.unboxed.Address;

/**
 * Helpers class used by the X86 compilers.
 * 
 * @author epr
 * @author patrik_reali
 */
public class X86CompilerHelper implements X86CompilerConstants {

	private final X86CompilerContext context;

	private VmMethod method;

	private String labelPrefix;

	private String instrLabelPrefix;

	private final boolean isBootstrap;

	private final Label jumpTableLabel;

	private final Address jumpTableAddress;

	private final boolean haveCMOV;

	private Label[] addressLabels;

	private final boolean debug = Vm.getVm().isDebugMode();

	private final AbstractX86StackManager stackMgr;

	private final X86Assembler os;

	/**
	 * Create a new instance
	 * 
	 * @param context
	 */
	public X86CompilerHelper(X86Assembler os, AbstractX86StackManager stackMgr,
			X86CompilerContext context, boolean isBootstrap)
			throws PrivilegedActionPragma {
		this.os = os;
		this.context = context;
		this.stackMgr = stackMgr;
		this.isBootstrap = isBootstrap;
		if (isBootstrap) {
			jumpTableLabel = new Label(X86JumpTable.JUMPTABLE_NAME);
			jumpTableAddress = null;
		} else {
			jumpTableLabel = null;
			jumpTableAddress = Unsafe.getJumpTable();
		}
		final X86CpuID cpuId = (X86CpuID) os.getCPUID();
		haveCMOV = cpuId.hasFeature(X86CpuID.FEAT_CMOV);
	}

	/**
	 * Gets the method that is currently being compiled.
	 * 
	 * @return method
	 */
	public final VmMethod getMethod() {
		return method;
	}

	/**
	 * Sets the method that is currently being compiled.
	 * 
	 * @param method
	 */
	public final void setMethod(VmMethod method) {
		this.method = method;
		this.labelPrefix = method.toString() + "_";
		this.instrLabelPrefix = labelPrefix + "_bci_";
		this.addressLabels = new Label[method.getBytecodeSize()];
	}

	/**
	 */
	public void startInlinedMethod(VmMethod inlinedMethod, Label curInstrLabel) {
		this.labelPrefix = curInstrLabel + "_" + inlinedMethod + "_";
		this.instrLabelPrefix = labelPrefix + "_bci_";
		this.addressLabels = new Label[inlinedMethod.getBytecodeSize()];
	}

	/**
	 * Create a method relative label to a given bytecode address.
	 * 
	 * @param address
	 * @return The created label
	 */
	public final Label getInstrLabel(int address) {
		Label l = addressLabels[address];
		if (l == null) {
			l = new Label(instrLabelPrefix + address);
			addressLabels[address] = l;
		}
		return l;
	}

	/**
	 * Create a method relative label
	 * 
	 * @param postFix
	 * @return The created label
	 */
	public final Label genLabel(String postFix) {
		return new Label(labelPrefix + postFix);
	}

	/**
	 * Write code to call the address found at the given offset in the system
	 * jumptable.
	 * 
	 * @param offset
	 * @see X86JumpTable
	 */
	public final void writeJumpTableCALL(int offset) {
		if (isBootstrap) {
			os.writeCALL(jumpTableLabel, offset, false);
		} else {
			os.writeCALL(jumpTableAddress, offset, true);
		}
	}

	/**
	 * Write code to jump to the address found at the given offset in the system
	 * jumptable.
	 * 
	 * @param offset
	 * @see X86JumpTable
	 */
	public final void writeJumpTableJMP(int offset) {
		if (isBootstrap) {
			os.writeJMP(jumpTableLabel, offset, false);
		} else {
			os.writeJMP(jumpTableAddress, offset, true);
		}
	}

	/**
	 * Emit code to invoke a method, where the reference to the VmMethod
	 * instance is in register EAX.
	 * 
	 * @param signature
	 */
	public final void invokeJavaMethod(String signature) {
		final int offset = context.getVmMethodNativeCodeField().getOffset();
		if (os.isCode32()) {
			os.writeCALL(X86Register.EAX, offset);
		} else {
			os.writeCALL(X86Register.RAX, offset);
		}
		pushReturnValue(signature);
	}

	/**
	 * Emit code to push the returncode of the given method signature.
	 * 
	 * @param signature
	 */
	public final void pushReturnValue(String signature) {
		final int returnType = JvmType.getReturnType(signature);
		assertCondition(
				signature.endsWith("V") == (returnType == JvmType.VOID),
				"Return type");
		// System.out.println("Return type: " + returnType + "\t" + signature);
		switch (returnType) {
		case JvmType.VOID:
			// No return value
			break;
		case JvmType.DOUBLE:
		case JvmType.LONG:
			// Wide return value
			if (os.isCode32()) {
				stackMgr.writePUSH64(returnType, X86Register.EAX,
						X86Register.EDX);
			} else {
				stackMgr.writePUSH64(returnType, X86Register.RAX);
			}
			break;
		case JvmType.REFERENCE:
			if (os.isCode32()) {
				stackMgr.writePUSH(returnType, X86Register.EAX);
			} else {
				stackMgr.writePUSH(returnType, X86Register.RAX);
			}
			break;
		default:
			// int/float return value
			stackMgr.writePUSH(returnType, X86Register.EAX);
		}
	}

	/**
	 * Emit code to invoke a java method
	 * 
	 * @param method
	 */
	public final void invokeJavaMethod(VmMethod method) {
		if (false) {
			os.writeMOV(context.ADDRSIZE, context.AAX, context.STATICS,
					getStaticsOffset(method));
		} else {
			os.writeMOV_Const(context.AAX, method);
		}
		invokeJavaMethod(method.getSignature());
	}

	/**
	 * Insert a yieldpoint into the code
	 */
	public final void writeYieldPoint(Object curInstrLabel) {
		if (method.getThreadSwitchIndicatorMask() != 0) {
			final Label doneLabel = new Label(curInstrLabel + "noYP");
			final int offset = context.getVmThreadSwitchIndicatorOffset();
			final int flag = VmProcessor.TSI_SWITCH_REQUESTED;
			if (os.isCode32()) {
				os.writePrefix(X86Constants.FS_PREFIX);
				os.writeCMP_MEM(BITS32, offset, flag);
			} else {
				os.writeCMP_Const(BITS32, PROCESSOR64, offset, flag);
			}
			os.writeJCC(doneLabel, X86Constants.JNE);
			os.writeINT(X86CompilerConstants.YIELDPOINT_INTNO);
			os.setObjectRef(doneLabel);
		}
	}

	/**
	 * Write class initialization code
	 * 
	 * @param method
	 * @param methodReg
	 *            Register that holds the method reference before this method is
	 *            called.
	 * @return true if code was written, false otherwise
	 */
	public final boolean writeClassInitialize(VmMethod method, GPR methodReg) {
		// Only for static methods (non <clinit>)
		if (method.isStatic() && !method.isInitializer()) {
			// Only when class is not initialize
			final VmType cls = method.getDeclaringClass();
			if (!cls.isInitialized()) {
				final GPR aax = context.AAX;
				final int size = os.getMode().getSize();

				// Save eax
				os.writePUSH(aax);
				// Do the is initialized test
				// Move method.declaringClass -> EAX
				os.writeMOV(size, aax, methodReg, context
						.getVmMemberDeclaringClassField().getOffset());
				// Test declaringClass.modifiers
				os.writeTEST(BITS32, aax, context.getVmTypeState().getOffset(),
						VmTypeState.ST_INITIALIZED);
				final Label afterInit = new Label(method.getMangledName()
						+ "$$after-classinit");
				os.writeJCC(afterInit, X86Constants.JNZ);
				// Call cls.initialize
				os.writePUSH(aax);
				invokeJavaMethod(context.getVmTypeInitialize());
				os.setObjectRef(afterInit);
				// Restore eax
				os.writePOP(aax);
				return true;
			}
		}
		return false;
	}

	public final void writeClassInitialize(Label curInstrLabel, GPR classReg,
			VmType cls) {
		if (!cls.isInitialized()) {
			// Test declaringClass.modifiers
			os.writeTEST(BITS32, classReg,
					context.getVmTypeState().getOffset(),
					VmTypeState.ST_INITIALIZED);
			final Label afterInit = new Label(curInstrLabel
					+ "$$after-classinit-ex");
			os.writeJCC(afterInit, X86Constants.JNZ);
			if (os.isCode32()) {
				os.writePUSHA();
			} else {
				os.writePUSH(X86Register.RAX);
				os.writePUSH(X86Register.RBX);
				os.writePUSH(X86Register.RCX);
				os.writePUSH(X86Register.RDX);
				os.writePUSH(X86Register.RSI);
			}
			// Call cls.initialize
			os.writePUSH(classReg);
			invokeJavaMethod(context.getVmTypeInitialize());
			if (os.isCode32()) {
				os.writePOPA();
			} else {
				os.writePOP(X86Register.RSI);
				os.writePOP(X86Register.RDX);
				os.writePOP(X86Register.RCX);
				os.writePOP(X86Register.RBX);
				os.writePOP(X86Register.RAX);
			}
			// Set label
			os.setObjectRef(afterInit);
		}
	}

	/**
	 * Write method counter increment code.
	 * 
	 * @param methodReg
	 *            Register that holds the method reference before this method is
	 *            called.
	 */
	public final void writeIncInvocationCount(GPR methodReg) {
		final int offset = context.getVmMethodInvocationCountField()
				.getOffset();
		os.writeINC(BITS32, methodReg, offset);
	}

	/**
	 * Write stack overflow test code.
	 * 
	 * @param method
	 */
	public final void writeStackOverflowTest(VmMethod method_) {
		// cmp esp,STACKEND
		// jg vm_invoke_testStackOverflowDone
		// vm_invoke_testStackOverflow:
		// int 0x31
		// vm_invoke_testStackOverflowDone:
		final int offset = context.getVmProcessorStackEnd().getOffset();
		final Label doneLabel = new Label(labelPrefix + "$$stackof-done");
		if (os.isCode32()) {
			os.writePrefix(X86Constants.FS_PREFIX);
			os.writeCMP_MEM(X86Register.ESP, offset);
		} else {
			os.writeCMP(X86Register.RSP, PROCESSOR64, offset);
		}
		os.writeJCC(doneLabel, X86Constants.JG);
		os.writeINT(0x31);
		os.setObjectRef(doneLabel);
	}

	/**
	 * Write staticTable load code. After the code (generated by this method) is
	 * executed, the STATICS register contains the reference to the statics
	 * table.
	 */
	public final void writeLoadSTATICS(Label curInstrLabel, String labelPrefix,
			boolean isTestOnly) {
		final int offset = context.getVmProcessorStaticsTable().getOffset();
		if (isTestOnly) {
			if (debug) {
				final Label ok = new Label(curInstrLabel + labelPrefix
						+ "$$ediok");
				if (os.isCode32()) {
					os.writePrefix(X86Constants.FS_PREFIX);
					os.writeCMP_MEM(context.STATICS, offset);
				} else {
					os.writeCMP(context.STATICS, PROCESSOR64, offset);
				}
				os.writeJCC(ok, X86Constants.JE);
				os.writeINT(0x88);
				os.setObjectRef(ok);
			}
		} else {
			if (os.isCode32()) {
				os.writeXOR(context.STATICS, context.STATICS);
				os.writePrefix(X86Constants.FS_PREFIX);
				os.writeMOV(INTSIZE, context.STATICS, context.STATICS, offset);
			} else {
				os.writeMOV(BITS64, context.STATICS, PROCESSOR64, offset);
			}
		}
	}

	/**
	 * Is class initialization code needed for the given method.
	 * 
	 * @param method
	 * @return true if class init code is needed, false otherwise.
	 */
	public static boolean isClassInitializeNeeded(VmMethod method) {
		// Only for static methods (non <clinit>)
		if (method.isStatic() && !method.isInitializer()) {
			// Only when class is not initialize
			final VmType cls = method.getDeclaringClass();
			if (!cls.isInitialized()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Do we need a write barrier
	 * 
	 * @return True/false
	 */
	public final boolean needsWriteBarrier() {
		return (context.getWriteBarrier() != null);
	}

	/**
	 * Write code to call the arrayStoreWriteBarrier.
	 * 
	 * @param refReg
	 * @param indexReg
	 * @param valueReg
	 */
	public final void writeArrayStoreWriteBarrier(GPR refReg, GPR indexReg,
			GPR valueReg, GPR scratchReg) {
		final VmWriteBarrier wb = context.getWriteBarrier();
		if (wb != null) {
			os.writeMOV_Const(scratchReg, wb);
			os.writePUSH(scratchReg);
			os.writePUSH(refReg);
			os.writePUSH(indexReg);
			os.writePUSH(valueReg);
			invokeJavaMethod(context.getArrayStoreWriteBarrier());
		}
	}

	/**
	 * Write code to call the putfieldWriteBarrier.
	 * 
	 * @param field
	 * @param refReg
	 * @param valueReg
	 */
	public final void writePutfieldWriteBarrier(VmInstanceField field,
			GPR refReg, GPR valueReg, GPR scratchReg) {
		if (field.isObjectRef()) {
			final VmWriteBarrier wb = context.getWriteBarrier();
			if (wb != null) {
				os.writeMOV_Const(scratchReg, wb);
				os.writePUSH(scratchReg);
				os.writePUSH(refReg);
				os.writePUSH(field.getOffset());
				os.writePUSH(valueReg);
				invokeJavaMethod(context.getPutfieldWriteBarrier());
			}
		}
	}

	/**
	 * Write code to call the putstaticWriteBarrier.
	 * 
	 * @param field
	 * @param valueReg
	 */
	public final void writePutstaticWriteBarrier(VmStaticField field,
			GPR valueReg, GPR scratchReg) {
		if (field.isObjectRef()) {
			final VmWriteBarrier wb = context.getWriteBarrier();
			if (wb != null) {
				os.writeMOV_Const(scratchReg, wb);
				os.writePUSH(scratchReg);
				os.writePUSH(field.getStaticsIndex());
				os.writePUSH(valueReg);
				invokeJavaMethod(context.getPutstaticWriteBarrier());
			}
		}
	}

	/**
	 * Is CMOVxx support bu the current cpu.
	 * 
	 * @return Returns the haveCMOV.
	 */
	public final boolean haveCMOV() {
		return this.haveCMOV;
	}

	/**
	 * Write code to load the given statics table entry into the given register.
	 * 
	 * @param curInstrLabel
	 * @param dst
	 * @param entry
	 */
	public final void writeGetStaticsEntry(Label curInstrLabel, GPR dst,
			VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "gs", true);
		os.writeMOV(INTSIZE, dst, context.STATICS, getStaticsOffset(entry));
	}

	/**
	 * Write code to load the given statics table entry onto the FPU stack.
	 * 
	 * @param curInstrLabel
	 * @param entry
	 * @param is32bit
	 *            If true, a 32-bit load is performed, otherwise a 64-bit load.
	 */
	public final void writeGetStaticsEntryToFPU(Label curInstrLabel,
			VmStaticsEntry entry, boolean is32bit) {
		writeLoadSTATICS(curInstrLabel, "gs", true);
		final int staticsIdx = getStaticsOffset(entry);
		if (is32bit) {
			os.writeFLD32(context.STATICS, staticsIdx);
		} else {
			os.writeFLD64(context.STATICS, staticsIdx);
		}
	}

	/**
	 * Write code to push the given statics table entry to the stack
	 * 
	 * @param curInstrLabel
	 * @param entry
	 */
	/* Patrik, added to push without requiring allocation of a register */
	public final void writePushStaticsEntry(Label curInstrLabel,
			VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "gs", true);
		os.writePUSH(context.STATICS, getStaticsOffset(entry));
	}

	/**
	 * Write code to load the given 64-bit statics table entry into the given
	 * 32-bit registers.
	 * 
	 * @param curInstrLabel
	 * @param lsbDst
	 * @param msbReg
	 * @param entry
	 */
	public final void writeGetStaticsEntry64(Label curInstrLabel, GPR lsbDst,
			GPR msbReg, VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "gs64", true);
		final int staticsOfs = getStaticsOffset(entry);
		os.writeMOV(INTSIZE, msbReg, context.STATICS, staticsOfs + 4); // MSB
		os.writeMOV(INTSIZE, lsbDst, context.STATICS, staticsOfs + 0); // LSB
	}

	/**
	 * Write code to load the given 64-bit statics table entry into the given
	 * 64-bit register.
	 * 
	 * @param curInstrLabel
	 * @param dstReg
	 * @param entry
	 */
	public final void writeGetStaticsEntry64(Label curInstrLabel, GPR64 dstReg,
			VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "gs64", true);
		os.writeMOV(BITS64, dstReg, context.STATICS, getStaticsOffset(entry));
	}

	/**
	 * Write code to store the given statics table entry into the given
	 * register.
	 * 
	 * @param curInstrLabel
	 * @param src
	 * @param entry
	 */
	public final void writePutStaticsEntry(Label curInstrLabel, GPR src,
			VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "ps", true);
		os.writeMOV(INTSIZE, context.STATICS, getStaticsOffset(entry), src);
	}

	/**
	 * Write code to store the given 64-bit statics table entry into the given
	 * 32-bit registers.
	 * 
	 * @param curInstrLabel
	 * @param lsbSrc
	 * @param msbSrc
	 * @param entry
	 */
	public final void writePutStaticsEntry64(Label curInstrLabel, GPR lsbSrc,
			GPR msbSrc, VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "ps64", true);
		final int staticsOfs = getStaticsOffset(entry);
		os.writeMOV(BITS32, context.STATICS, staticsOfs + 4, msbSrc); // MSB
		os.writeMOV(BITS32, context.STATICS, staticsOfs + 0, lsbSrc); // LSB
	}

	/**
	 * Write code to store the given 64-bit statics table entry into the given
	 * 64-bit register.
	 * 
	 * @param curInstrLabel
	 * @param srcReg
	 * @param entry
	 */
	public final void writePutStaticsEntry64(Label curInstrLabel, GPR64 srcReg,
			VmStaticsEntry entry) {
		writeLoadSTATICS(curInstrLabel, "ps64", true);
		os.writeMOV(BITS64, context.STATICS, getStaticsOffset(entry), srcReg);
	}

	/**
	 * Gets the offset from the beginning of the statics table (context.STATICS)
	 * to the given entry.
	 * 
	 * @param entry
	 * @return The byte offset from context.STATICS to the entry.
	 */
	public final int getStaticsOffset(VmStaticsEntry entry) {
		if (os.isCode32()) {
			return (VmArray.DATA_OFFSET * 4) + (entry.getStaticsIndex() << 2);			
		} else {
			return (VmArray.DATA_OFFSET * 8) + (entry.getStaticsIndex() << 2);						
		}
	}

	public static void assertCondition(boolean condition, String msg) {
		if (!condition) {
			throw new InternalError("Assertion failed: " + msg);
		}
	}
}
