/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

import org.jnode.vm.compiler.ir.quad.AssignQuad;
import org.jnode.vm.compiler.ir.quad.VariableRefAssignQuad;

/**
 * @author Madhu Siddalingaiah
 *
 */
public abstract class Variable extends Operand implements Cloneable {
	private int index;
	private int ssaValue;

	/*
	 * The operation where this variable is assigned
	 */
	private AssignQuad assignQuad;

	/*
	 * The address where this variable is last used
	 */
	private int lastUseAddress;

	public Variable(int type, int index, int ssaValue) {
		super(type);
		this.index = index;
		this.ssaValue = ssaValue;
	}

	/**
	 * @param type
	 */
	public Variable(int type, int index) {
		this(type, index, 1000*index);
	}

	public void doSSA() {
		ssaValue += 1;
	}

	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		return hashCode() == other.hashCode();
	}

	public int hashCode() {
		return ssaValue;
	}

	/**
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @return
	 */
	public int getSSAValue() {
		return ssaValue;
	}
	
	public abstract Object clone();

	/**
	 * Returns the AssignQuad where this variable was last assigned.
	 * 
	 * @return
	 */
	public AssignQuad getAssignQuad() {
		return assignQuad;
	}

	/**
	 * @return
	 */
	public int getLastUseAddress() {
		return lastUseAddress;
	}

	/**
	 * @param assignQuad
	 */
	public void setAssignQuad(AssignQuad assignQuad) {
		this.assignQuad = assignQuad;
	}

	public int getAssignAddress() {
		if (assignQuad == null) {
			return 0;
		}
		// Add one so this live range starts just after this operation.
		// This way live range interference computation is simplified.
		return this.assignQuad.getLHSLiveAddress();
	}

	/**
	 * @param address
	 */
	public void setLastUseAddress(int address) {
		if (address > lastUseAddress) {
			lastUseAddress = address;
		}
	}

	public Operand simplify() {
		return assignQuad.propagate(this);
	}
	
	public Variable simplifyCopy() {
		if (assignQuad instanceof VariableRefAssignQuad) {
			VariableRefAssignQuad va = (VariableRefAssignQuad) assignQuad;
			Operand rhs = va.getRHS();
			if (rhs instanceof Variable) {
				return (Variable) rhs;
			}
		}
		assignQuad.setDeadCode(false);
		return this;
	}
}
