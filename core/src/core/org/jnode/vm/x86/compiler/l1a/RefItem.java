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

	// generate unique labels for writeStatics (should use current label)
	private long labelCounter;

	private VmConstString value;

	/**
	 * Initialize a blank item
	 */
	RefItem(ItemFactory factory) {
	    super(factory);
	}

	final void initialize(int kind, int offsetToFP, Register reg, VmConstString val) {
		super.initialize(kind, reg, offsetToFP);
		this.value = val;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
	 */
	protected WordItem cloneConstant() {
		return factory.createAConst(getValue());
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
			os.writeXOR(reg, reg);
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