/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;
import org.jnode.assembler.Label;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.Address;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.Modifier;
import org.jnode.vm.classmgr.VmArray;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmStaticField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;
/**
 * Helpers class used by the X86 compilers.
 * 
 * @author epr
 */
public class X86CompilerHelper extends X86StackManager implements X86CompilerConstants {
	private VmMethod method;
	private final boolean isBootstrap;
	private final Label jumpTableLabel;
	private final Address jumpTableAddress;
	private final boolean haveCMOV;
	/**
	 * Create a new instance
	 * 
	 * @param context
	 */
	public X86CompilerHelper(AbstractX86Stream os, X86CompilerContext context, boolean isBootstrap) {
		super(os);
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
	}
	/**
	 * Create a method relative label to a given bytecode address.
	 * 
	 * @param address
	 * @return The created label
	 */
	public final Label getInstrLabel(int address) {
		return new Label(method.toString() + "_bci_" + address);
	}
	/**
	 * Create a method relative label
	 * 
	 * @param postFix
	 * @return The created label
	 */
	public final Label genLabel(String postFix) {
		return new Label(method.toString() + "_" + postFix);
	}
	/**
	 * Write code to call the address found at the given offset in the system jumptable.
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
	 * Write code to jump to the address found at the given offset in the system jumptable.
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
	 * Emit code to invoke a method, where the reference to the VmMethod instance is in register EAX.
	 * 
	 * @param signature
	 */
	public final void invokeJavaMethod(String signature, X86CompilerContext context) {
		os.writeCALL(Register.EAX, context.getVmMethodNativeCodeField().getOffset());
		//writeJumpTableCALL(X86JumpTable.VM_INVOKE_OFS);
		final char ch = signature.charAt(signature.length() - 1);
		if (ch == 'V') {
			/** No return value */
		} else if ((ch == 'J') || (ch == 'D')) {
			/** Wide return value */
			writePUSH64(Register.EAX, Register.EDX);
		} else {
			/** Normal return value */
			writePUSH(Register.EAX);
		}
	}
	/**
	 * Emit code to invoke a java method
	 * 
	 * @param method
	 */
	public final void invokeJavaMethod(VmMethod method, X86CompilerContext context) {
		os.writeMOV_Const(Register.EAX, method);
		invokeJavaMethod(method.getSignature(), context);
	}
	/**
	 * Insert a yieldpoint into the code
	 */
	public final void writeYieldPoint(Object curInstrLabel, X86CompilerContext context) {
		if (method.getThreadSwitchIndicatorMask() != 0) {
			final Label doneLabel = new Label(curInstrLabel + "noYP");
			os.writePrefix(X86Constants.FS_PREFIX);
			os.writeCMP_MEM(context.getVmThreadSwitchIndicatorOffset(), VmProcessor.TSI_SWITCH_REQUESTED);
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
	 *            Register that holds the method reference before this method is called.
	 * @return true if code was written, false otherwise
	 */
	public final boolean writeClassInitialize(VmMethod method, Register methodReg, Register scratch, X86CompilerContext context) {
		// Only for static methods (non <clinit>)
		if (method.isStatic() && !method.isInitializer()) {
			// Only when class is not initialize
			final VmType cls = method.getDeclaringClass();
			if (!cls.isInitialized()) {
				// Save eax
				writePUSH(EAX);
				// Do the is initialized test
				// Move method.declaringClass -> scratch
				os.writeMOV(INTSIZE, scratch, methodReg, context.getVmMemberDeclaringClassField().getOffset());
				// Move declaringClass.modifiers -> EAX
				os.writeMOV(INTSIZE, EAX, scratch, context.getVmTypeModifiers().getOffset());
				os.writeTEST_EAX(Modifier.ACC_INITIALIZED);
				final Label afterInit = new Label(method.getMangledName() + "$$after-classinit");
				os.writeJCC(afterInit, X86Constants.JNZ);
				// Call cls.initialize
				os.writeMOV(INTSIZE, EAX, scratch);
				writePUSH(EAX);
				invokeJavaMethod(context.getVmTypeInitialize(), context);
				os.setObjectRef(afterInit);
				// Restore eax
				writePOP(EAX);
				return true;
			}
		}
		return false;
	}
	/**
	 * Write method counter increment code.
	 * 
	 * @param methodReg
	 *            Register that holds the method reference before this method is called.
	 */
	public final void writeIncInvocationCount(Register methodReg, X86CompilerContext context) {
		final int offset = context.getVmMethodInvocationCountField().getOffset();
		os.writeADD(methodReg, offset, 1);
	}
	/**
	 * Write stack overflow test code.
	 * 
	 * @param method
	 * @param context
	 */
	public final void writeStackOverflowTest(VmMethod method, X86CompilerContext context) {
		//cmp esp,STACKEND
		//jg vm_invoke_testStackOverflowDone
		//vm_invoke_testStackOverflow:
		//int 0x31
		//vm_invoke_testStackOverflowDone:
		final int offset = context.getVmProcessorStackEnd().getOffset();
		final Label doneLabel = new Label(method + "$$stackof-done");
		os.writePrefix(X86Constants.FS_PREFIX);
		os.writeCMP_MEM(Register.ESP, offset);
		os.writeJCC(doneLabel, X86Constants.JG);
		os.writeINT(0x31);
		os.setObjectRef(doneLabel);
	}
	/**
	 * Write staticTable load code. After the code (generated by this method) is executed, the STATICS register contains the reference to the statics table.
	 * 
	 * @param context
	 */
	public final void writeLoadSTATICS(X86CompilerContext context, Label labelPrefix, boolean isTestOnly) {
		final int offset = context.getVmProcessorStaticsTable().getOffset();
		if (isTestOnly) {
			/*
			 * final Label ok = new Label(labelPrefix + "$$ediok"); os.writePrefix(X86Constants.FS_PREFIX); os.writeCMP_MEM(STATICS, offset); os.writeJCC(ok, X86Constants.JE); os.writeINT(0x88);
			 */
		} else {
			os.writeXOR(STATICS, STATICS);
			os.writePrefix(X86Constants.FS_PREFIX);
			os.writeMOV(INTSIZE, STATICS, STATICS, offset);
		}
	}
	/**
	 * Write code to get an entry out of the constant pool of the declaring class of the current method.
	 * 
	 * @param dst
	 *            Destination register
	 * @param methodReg
	 *            Register that holds a reference to the current method
	 * @param cpIdx
	 *            Index in the constant pool
	 * @param context
	 *            The compiler context
	 */
	public final void writeGetCPEntry(Register dst, Register methodReg, int cpIdx, X86CompilerContext context, int slotSize) {
		// First get the declaring class
		final int declaringClassOffset = context.getVmMemberDeclaringClassField().getOffset();
		os.writeMOV(INTSIZE, dst, methodReg, declaringClassOffset);
		// Now get VmType.cp
		final int vmTypeCpOffset = context.getVmTypeCp().getOffset();
		os.writeMOV(INTSIZE, dst, dst, vmTypeCpOffset);
		// Now get the VmCP.cp
		final int vmCPCpOffset = context.getVmCPCp().getOffset();
		os.writeMOV(INTSIZE, dst, dst, vmCPCpOffset);
		// Now get the cp[cpIdx]
		os.writeMOV(INTSIZE, dst, dst, (VmArray.DATA_OFFSET + cpIdx) * slotSize);
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
	 * Write code to call the arrayStoreWriteBarrier.
	 * 
	 * @param refReg
	 * @param indexReg
	 * @param valueReg
	 */
	public final void writeArrayStoreWriteBarrier(X86CompilerContext context, Register refReg, Register indexReg, Register valueReg) {
		writePUSH(refReg);
		writePUSH(indexReg);
		writePUSH(valueReg);
		invokeJavaMethod(context.getArrayStoreWriteBarrier(), context);
	}
	/**
	 * Write code to call the putfieldWriteBarrier.
	 * 
	 * @param field
	 * @param refReg
	 * @param valueReg
	 */
	public final void writePutfieldWriteBarrier(X86CompilerContext context, VmInstanceField field, Register refReg, Register valueReg) {
		if (field.isObjectRef()) {
			writePUSH(refReg);
			writePUSH(field.getOffset());
			writePUSH(valueReg);
			invokeJavaMethod(context.getPutfieldWriteBarrier(), context);
		}
	}
	/**
	 * Write code to call the putstaticWriteBarrier.
	 * 
	 * @param field
	 * @param valueReg
	 */
	public final void writePutstaticWriteBarrier(X86CompilerContext context, VmStaticField field, Register valueReg) {
		if (field.isObjectRef()) {
			writePUSH(field.getStaticsIndex());
			writePUSH(valueReg);
			invokeJavaMethod(context.getPutstaticWriteBarrier(), context);
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
}
