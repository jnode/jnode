/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 * 
 * IntItems are items with type INT
 */

final class IntItem extends WordItem implements X86CompilerConstants {

	private int value;

	final void initialize(int kind, int offsetToFP, Register reg, int value) {
		super.initialize(kind, reg, offsetToFP);
		this.value = value;
	}

	IntItem(ItemFactory factory) {
	    super(factory);
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
	 */
	protected WordItem cloneConstant() {
		return factory.createIConst(getValue());
	}

	/**
	 * Get the JVM type of this item
	 * 
	 * @return the JVM type
	 */
	int getType() {
		return JvmType.INT;
	}

	int getValue() {
	    if (Vm.VerifyAssertions) Vm._assert(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
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
		if (value != 0) {
			os.writeMOV_Const(reg, value);
		} else {
			os.writeXOR(reg, reg);
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
		os.writeFISTP32(reg, disp);
	}

	/**
	 * Push my constant on the stack using the given os.
	 * 
	 * @param os
	 */
	protected void pushConstant(EmitterContext ec, AbstractX86Stream os) {
		os.writePUSH(value);
	}

	/**
	 * Push the given memory location on the FPU stack.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
		os.writeFILD32(reg, disp);
	}
}