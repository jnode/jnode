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
 
package org.jnode.vm.compiler.ir.quad;

import org.jnode.util.BootableHashMap;
import org.jnode.vm.compiler.ir.CodeGenerator;
import org.jnode.vm.compiler.ir.IRBasicBlock;
import org.jnode.vm.compiler.ir.Operand;
import org.jnode.vm.compiler.ir.Variable;

/**
 * @author Madhu Siddalingaiah
 *
 * Represents an intermediate intruction, commonly called a Quad
 * in the literature.
 * 
 */
public abstract class Quad {
	private int address;
	private boolean deadCode;
	private IRBasicBlock basicBlock;

	public Quad(int address, IRBasicBlock block) {
		this.address = address;
		this.basicBlock = block;
		this.deadCode = false;
	}

	public Operand getOperand(int varIndex) {
		return basicBlock.getVariables()[varIndex];
	}

	/**
	 * Gets the bytecode address for this operation
	 * 
	 * @return bytecode address for this operation
	 */
	public int getAddress() {
		return address;
	}

	/**
	 * @param i
	 */
	public void setAddress(int i) {
		address = i;
	}

	/**
	 * Gets the operand defined by this operation (left side of assignment)
	 * 
	 * @return defined operand or null if none
	 */
	public abstract Operand getDefinedOp();

	/**
	 * Gets all operands used by this operation (right side of assignment)
	 * 
	 * @return array of referenced operands or null if none
	 */
	public abstract Operand[] getReferencedOps();

	/**
	 * Gets all operands that interfere in this operation
	 * This useful in graph coloring register allocators
	 * 
	 * @return array of operands that interfere with each other or null if none
	 */
	public Operand[] getIFOperands() {
		return null;
	}

	/**
	 * @return
	 */
	public boolean isDeadCode() {
		return deadCode;
	}

	/**
	 * @param dead
	 */
	public void setDeadCode(boolean dead) {
		deadCode = dead;
	}

	/**
	 * @return
	 */
	public IRBasicBlock getBasicBlock() {
		return basicBlock;
	}

	public void computeLiveness(BootableHashMap liveVariables) {
		Operand[] refs = getReferencedOps();
		if (refs != null) {
			int n = refs.length;
			for (int i=0; i<n; i+=1) {
				if (refs[i] instanceof Variable) {
					Variable v = (Variable) refs[i];
					v.setLastUseAddress(getAddress());
					liveVariables.put(v, v);
				}
			}
		}
	}

	/**
	 * Performs basic optimizations such as constant folding and
	 * copy propagation. In most cases, subclasses can simplify
	 * operands, e.g.:
	 * 
	 * <code>refs[0] = refs[0].simplify();</code>
	 */
	public abstract void doPass2();
	public abstract void generateCode(CodeGenerator cg);
}
