/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;

/**
 * @author Patrik Reali
 */
final class DoubleItem extends DoubleWordItem {

	private double value;

	/**
	 * Initialize a blank item.
	 */
	DoubleItem(ItemFactory factory) {
	    super(factory);
	}
	
	/**
	 * @param kind
	 * @param offsetToFP
	 * @param value
	 */
	final void initialize(int kind, int offsetToFP, Register lsb, Register msb,
			double value) {
		super.initialize(kind, offsetToFP, lsb, msb);
		this.value = value;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.DoubleWordItem#cloneConstant()
	 */
	protected DoubleWordItem cloneConstant() {
		return factory.createDConst(getValue());
	}

	/**
	 * Get the JVM type of this item
	 * 
	 * @return the JVM type
	 */
	final int getType() {
		return JvmType.DOUBLE;
	}

	/**
	 * Gets the constant value.
	 * 
	 * @return
	 */
	double getValue() {
	    if (Vm.VerifyAssertions) Vm._assert(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
		return value;
	}

	/**
	 * Load my constant to the given os.
	 * 
	 * @param os
	 * @param lsb
	 * @param msb
	 */
	protected final void loadToConstant(EmitterContext ec,
			AbstractX86Stream os, Register lsb, Register msb) {
		final long lvalue = Double.doubleToLongBits(value);
		final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
		final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);

		os.writeMOV_Const(lsb, lsbv);
		os.writeMOV_Const(msb, msbv);
	}

	/**
	 * Pop the top of the FPU stack into the given memory location.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void popFromFPU(AbstractX86Stream os, Register reg, int disp) {
		os.writeFSTP64(reg, disp);
	}

	/**
	 * Push my constant on the stack using the given os.
	 * 
	 * @param os
	 */
	protected final void pushConstant(EmitterContext ec, AbstractX86Stream os) {
		final long lvalue = Double.doubleToLongBits(value);
		final int lsbv = (int) (lvalue & 0xFFFFFFFFL);
		final int msbv = (int) ((lvalue >>> 32) & 0xFFFFFFFFL);
		os.writePUSH(msbv);
		os.writePUSH(lsbv);
	}

	/**
	 * Push the given memory location on the FPU stack.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void pushToFPU(AbstractX86Stream os, Register reg, int disp) {
		os.writeFLD64(reg, disp);
	}
}