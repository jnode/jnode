/*
 * $Id$
 */
package org.jnode.vm.x86.compiler;

import org.jnode.assembler.Label;
import org.jnode.assembler.UnresolvedObjectRefException;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.assembler.x86.X86Constants;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.classmgr.Modifier;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.compiler.Symbol;
import org.jnode.vm.compiler.SymbolResolver;

/**
 * Helpers class used by the X86 compilers.
 * 
 * @author epr
 */
public class X86CompilerHelper implements X86CompilerConstants {

	public final Label VM_PATCH_MOV_EAX_IMM32 = new Label("vm_patch_MOV_EAX_IMM32");
	public final Label VM_PATCH_NOP = new Label("vm_patch_NOP");
	public final Label VM_ATHROW = new Label("vm_athrow");
	public final Label VM_ATHROW_NOTRACE = new Label("vm_athrow_notrace");
	public final Label VM_INVOKE = new Label("vm_invoke");
	public final int VM_INVOKE_SYMIDX = 4;
	public final Label VM_INTERPRETER = new Label("vm_interpreter");
	public final int VM_INTERPRETER_SYMIDX = 5;
	
	private final AbstractX86Stream os;
	private VmMethod method;
	//private final X86CompilerContext context;

	/**
	 * Create a new instance
	 * @param context
	 */
	public X86CompilerHelper(AbstractX86Stream os, X86CompilerContext context) {
		this.os = os;
		//this.context = context;
	}

	/**
	 * Gets the method that is currently being compiled.
	 * @return method
	 */
	public VmMethod getMethod() {
		return method;
	}

	/**
	 * Sets the method that is currently being compiled.
	 * @param method
	 */
	public void setMethod(VmMethod method) {
		this.method = method;
	}

	/**
	 * Create a method relative label to a given bytecode address.
	 * @param address
	 * @return The created label
	 */
	public Label getInstrLabel(int address) {
		return new Label(method.toString() + "_bci_" + address);
	}

	/**
	 * Create a method relative label
	 * @param postFix
	 * @return The created label
	 */
	public Label genLabel(String postFix) {
		return new Label(method.toString() + "_" + postFix);
	}

	/**
	 * Emit code to invoke a method, where the reference to the VmMethod
	 * instance is in register EAX.
	 * @param signature
	 */
	public void invokeJavaMethod(String signature) {
		os.writeCALL(VM_INVOKE);
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
	 * @param method
	 */
	public void invokeJavaMethod(VmMethod method) {
		os.writeMOV_Const(Register.EAX, method);
		invokeJavaMethod(method.getSignature());
	}
	
	public Symbol[] loadSymbols(SymbolResolver resolver) throws UnresolvedObjectRefException {
		final Symbol[] list = new Symbol[6];
		String name;
		int i = 0;

		name = VM_PATCH_MOV_EAX_IMM32.toString();
		list[i++] = new Symbol(name, resolver.getSymbolAddress(name));
		name = VM_PATCH_NOP.toString();
		list[i++] = new Symbol(name, resolver.getSymbolAddress(name));
		name = VM_ATHROW.toString();
		list[i++] = new Symbol(name, resolver.getSymbolAddress(name));
		name = VM_ATHROW_NOTRACE.toString();
		list[i++] = new Symbol(name, resolver.getSymbolAddress(name));
		name = VM_INVOKE.toString();
		list[i++] = new Symbol(name, resolver.getSymbolAddress(name));
		name = VM_INTERPRETER.toString();
		list[i++] = new Symbol(name, resolver.getSymbolAddress(name));
		return list;
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
	 * @param method
	 * @return true if code was written, false otherwise
	 */
	public boolean writeClassInitialize(VmMethod method, X86CompilerContext context) {
		// Only for static methods (non <clinit>)
		if (method.isStatic() && !method.isInitializer()) {
			// Only when class is not initialize
			final VmType cls = method.getDeclaringClass();
			if (!cls.isInitialized()) {
				// Save eax
				os.writePUSH(Register.EAX);
				os.writePUSH(Register.EDX);
				// Do the is initialized test
				os.writeMOV_Const(Register.EAX, cls);
				os.writeMOV(INTSIZE, Register.EAX, Register.EAX, context.getVmTypeModifiers().getOffset());
				os.writeTEST_EAX(Modifier.ACC_INITIALIZED);
				final Label afterInit = new Label(method.getMangledName() + "$$after-classinit");
				os.writeJCC(afterInit, X86Constants.JNZ);
				// Call cls.initialize
				os.writePUSH(cls);
				invokeJavaMethod(context.getVmTypeInitialize());
				os.setObjectRef(afterInit);
				// Restore eax
				os.writePOP(Register.EDX);
				os.writePOP(Register.EAX);
				return true;
			}
		}
		return false;
	}
}
