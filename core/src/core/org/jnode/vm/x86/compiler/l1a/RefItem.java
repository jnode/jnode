/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.Label;
import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.classmgr.VmConstString;
import org.jnode.vm.x86.compiler.X86CompilerConstants;
import org.jnode.vm.x86.compiler.X86CompilerHelper;

/**
 * @author Patrik Reali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class RefItem extends WordItem implements X86CompilerConstants {

	static RefItem createConst(VmConstString value) {
		return new RefItem(Kind.CONSTANT, null, value, 0);
	}

	static RefItem createLocal(int offsetToFP) {
		return new RefItem(Kind.LOCAL, null, null, offsetToFP);
	}

	static RefItem createRegister(Register reg) {
		return new RefItem(Kind.REGISTER, reg, null, 0);
	}

	static RefItem createStack() {
		return new RefItem(Kind.STACK, null, null, 0);
	}

	// generate unique labels for writeStatics (should use current label)
	private long labelCounter;

	private VmConstString value;

	/**
	 * @param kind
	 * @param reg
	 * @param val
	 * @param offsetToFP
	 */
	private RefItem(int kind, Register reg, VmConstString val, int offsetToFP) {
		super(kind, reg, offsetToFP);
		this.value = val;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
	 */
	protected Item cloneConstant() {
		return createConst(getValue());
	}

	/**
	 * Get the JVM type of this item
	 * 
	 * @return the JVM type
	 */
	int getType() {
		return JvmType.REFERENCE;
	}

	/**
	 * Gets the value of this reference. Item must have a CONSTANT kind.
	 * 
	 * @return
	 */
	VmConstString getValue() {
		assertCondition(getKind() == Kind.CONSTANT, "kind == Kind.CONSTANT");
		return value;
	}

	/**
	 * Load my constant to the given os.
	 * 
	 * @param os
	 * @param reg
	 */
	protected void loadToConstant(EmitterContext ec, AbstractX86Stream os,
			Register reg) {
		if (value == null) {
			os.writeMOV_Const(reg, value);
		} else {
			X86CompilerHelper helper = ec.getHelper();
			Label l = new Label(Long.toString(labelCounter++));
			helper.writeGetStaticsEntry(l, reg, value);
		}
	}

	/**
	 * Pop the top of the FPU stack into the given memory location.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void popFromFPU(AbstractX86Stream os, Register reg, int disp) {
		notImplemented();
	}

	/**
	 * Push my constant on the stack using the given os.
	 * 
	 * @param os
	 */
	protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
		if (value == null) {
			os.writePUSH_Const(null);
		} else {
			X86CompilerHelper helper = ec.getHelper();
			Label l = new Label(Long.toString(labelCounter++));
			helper.writePushStaticsEntry(l, value);
		}
	}

	/**
	 * Push the given memory location on the FPU stack.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
		notImplemented();
	}

}