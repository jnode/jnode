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

import org.jnode.assembler.x86.X86Assembler;
import org.jnode.assembler.x86.X86Register;
import org.jnode.assembler.x86.X86Register.GPR;
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
	final void initialize(int kind, int offsetToFP, X86Register.GPR lsb, X86Register.GPR msb, X86Register.XMM xmm,
			double value) {
		super.initialize(kind, offsetToFP, lsb, msb, xmm);
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
			X86Assembler os, GPR lsb, GPR msb) {
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
	protected void popFromFPU(X86Assembler os, GPR reg, int disp) {
		os.writeFSTP64(reg, disp);
	}

	/**
	 * Push my constant on the stack using the given os.
	 * 
	 * @param os
	 */
	protected final void pushConstant(EmitterContext ec, X86Assembler os) {
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
	protected void pushToFPU(X86Assembler os, GPR reg, int disp) {
		os.writeFLD64(reg, disp);
	}
}
