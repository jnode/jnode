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
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.x86.X86CpuID;

/**
 * Helpers class used by the X86 compilers.
 * 
 * @author epr
 */
public class X86CompilerHelper implements X86CompilerConstants {

	private final AbstractX86Stream os;
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
		this.os = os;
		this.isBootstrap = isBootstrap;
		if (isBootstrap) {
			jumpTableLabel = new Label(X86JumpTable.JUMPTABLE_NAME);
			jumpTableAddress = null;
		} else {
			jumpTableLabel = null;
			jumpTableAddress = Unsafe.getJumpTable();
		}
		final X86CpuID cpuId = (X86CpuID)os.getCPUID();
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
	 * Emit code to invoke a method, where the reference to the VmMethod instance is in register
	 * EAX.
	 * 
	 * @param signature
	 */
	public final void invokeJavaMethod(String signature) {
		writeJumpTableCALL(X86JumpTable.VM_INVOKE_OFS);
		char ch = signature.charAt(signature.length() - 1);
		if (ch == 'V') {
			/** No return value */
		} else if ((ch == 'J') || (ch == 'D')) {
			/** Wide return value */
			os.writePUSH(Register.EDX);
			os.writePUSH(Register.EAX);
		} else {
			/** Normal return value */
			os.writePUSH(Register.EAX);
		}
	}

	/**
	 * Emit code to invoke a java method
	 * 
	 * @param method
	 */
	public final void invokeJavaMethod(VmMethod method) {
		os.writeMOV_Const(Register.EAX, method);
		invokeJavaMethod(method.getSignature());
	}

	/**
	 * Insert a yieldpoint into the code
	 */
	public final void yieldPoint(Object curInstrLabel, X86CompilerContext context) {
		if (method.getThreadSwitchIndicatorMask() != 0) {
			final Object doneLabel = new Label(curInstrLabel + "noYP");
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
	 * @param methodReg Register that holds the method reference before 
	 * this method is called.
	 * @return true if code was written, false otherwise
	 */
	public final boolean writeClassInitialize(VmMethod method, Register methodReg, X86CompilerContext context) {
		// Only for static methods (non <clinit>)
		if (method.isStatic() && !method.isInitializer()) {
			// Only when class is not initialize
			final VmType cls = method.getDeclaringClass();
			if (!cls.isInitialized()) {
				// Save eax
				os.writePUSH(Register.EAX);
				// Do the is initialized test
				//os.writeMOV_Const(Register.EAX, cls);
				os.writeMOV(INTSIZE, Register.EAX, methodReg, context.getVmMemberDeclaringClassField().getOffset());
				
				os.writeMOV(INTSIZE, Register.EAX, Register.EAX, context.getVmTypeModifiers().getOffset());
				os.writeTEST_EAX(Modifier.ACC_INITIALIZED);
				final Label afterInit = new Label(method.getMangledName() + "$$after-classinit");
				os.writeJCC(afterInit, X86Constants.JNZ);
				// Call cls.initialize
				os.writePUSH(cls);
				invokeJavaMethod(context.getVmTypeInitialize());
				os.setObjectRef(afterInit);
				// Restore eax
				os.writePOP(Register.EAX);
				return true;
			}
		}
		return false;
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
	 * Is CMOVxx support bu the current cpu.
	 * @return Returns the haveCMOV.
	 */
	public final boolean haveCMOV() {
		return this.haveCMOV;
	}

}
