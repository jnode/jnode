/*
 * $Id$
 */
package org.jnode.vm.x86.compiler.l1a;

import org.jnode.assembler.x86.AbstractX86Stream;
import org.jnode.assembler.x86.Register;
import org.jnode.vm.JvmType;
import org.jnode.vm.x86.compiler.X86CompilerConstants;

/**
 * @author Patrik Reali
 * 
 * IntItems are items with type INT
 */

final class IntItem extends WordItem implements X86CompilerConstants {

	/**
	 * Create a constant item
	 * 
	 * @param value
	 * @return
	 */
	static IntItem createConst(int value) {
		return new IntItem(Kind.CONSTANT, null, value, 0);
	}

	/**
	 * Create an item that is on the FPU stack.
	 * 
	 * @return
	 */
	static IntItem createFPUStack() {
		return new IntItem(Kind.FPUSTACK, null, 0, 0);
	}

	/**
	 * Create a local variable item
	 * 
	 * @param offsetToFP
	 * @return
	 */
	static IntItem createLocal(int offsetToFP) {
		return new IntItem(Kind.LOCAL, null, 0, offsetToFP);
	}

	/**
	 * Create a register item
	 * 
	 * @param reg
	 * @return
	 */
	static IntItem createReg(Register reg) {
		return new IntItem(Kind.REGISTER, reg, 0, 0);
	}

	/**
	 * Create an item that is on the stack
	 * 
	 * @return
	 */
	static IntItem createStack() {
		return new IntItem(Kind.STACK, null, 0, 0);
	}

	private final int value;

	private IntItem(int kind, Register reg, int value, int local) {
		super(kind, reg, local);
		this.value = value;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
	 */
	protected WordItem cloneConstant() {
		return createConst(getValue());
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
		assertCondition(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
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