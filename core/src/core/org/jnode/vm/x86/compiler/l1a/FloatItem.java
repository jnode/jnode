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
import org.jnode.vm.JvmType;
import org.jnode.vm.Vm;

/**
 * @author Patrik Reali
 */
final class FloatItem extends WordItem {

	private float value;

	/**
	 * Initialize a blank item.
	 */
	FloatItem(ItemFactory factory) {
	    super(factory);
	}
	
	/**
	 * @param kind
	 * @param offsetToFP
	 * @param value
	 */
	final void initialize(int kind, int offsetToFP, X86Register reg, float value) {
		super.initialize(kind, reg, offsetToFP);
		this.value = value;
	}

	/**
	 * @see org.jnode.vm.x86.compiler.l1a.WordItem#cloneConstant()
	 */
	protected WordItem cloneConstant() {
		return factory.createFConst(getValue());
	}

	/**
	 * Get the JVM type of this item
	 * 
	 * @return the JVM type
	 */
	final int getType() {
		return JvmType.FLOAT;
	}

	/**
	 * Gets the constant value.
	 * 
	 * @return
	 */
	float getValue() {
	    if (Vm.VerifyAssertions) Vm._assert(kind == Kind.CONSTANT, "kind == Kind.CONSTANT");
		return value;
	}

	/**
	 * Load my constant to the given os.
	 * 
	 * @param os
	 * @param reg
	 */
	protected void loadToConstant(EmitterContext ec, X86Assembler os,
			X86Register reg) {
		os.writeMOV_Const(reg, Float.floatToIntBits(value));
	}

	/**
	 * Pop the top of the FPU stack into the given memory location.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void popFromFPU(X86Assembler os, X86Register reg, int disp) {
		os.writeFSTP32(reg, disp);
	}

	/**
	 * Push my constant on the stack using the given os.
	 * 
	 * @param os
	 */
	protected void pushConstant(EmitterContext ec, X86Assembler os) {
		os.writePUSH(Float.floatToIntBits(value));
	}

	/**
	 * Push the given memory location on the FPU stack.
	 * 
	 * @param os
	 * @param reg
	 * @param disp
	 */
	protected void pushToFPU(X86Assembler os, X86Register reg, int disp) {
		os.writeFLD32(reg, disp);
	}
}
