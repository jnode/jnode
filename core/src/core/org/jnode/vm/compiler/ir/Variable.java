/*
 * $Id$
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

import org.jnode.vm.compiler.ir.quad.AssignQuad;

/**
 * @author Madhu Siddalingaiah
 *
 */
public abstract class Variable extends Operand implements Cloneable {
	private int index;
	private int ssaValue;
	private Location location;

	/*
	 * The operation where this variable is assigned
	 */
	private AssignQuad assignQuad;

	/*
	 * The address where this variable is last used
	 */
	private int lastUseAddress;

	public Variable(int type, int index) {
		super(type);
		this.index = index;
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
	
	/**
	 * @param i
	 */
	public void setSSAValue(int i) {
		ssaValue = i;
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
		return assignQuad.getLHSLiveAddress();
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
		Operand op = assignQuad.propagate(this);
		return op;
	}
	
	/**
	 * @return
	 */
	public Location getLocation() {
		return this.location;
	}

	/**
	 * @param loc
	 */
	public void setLocation(Location loc) {
		this.location = loc;
	}

	/* (non-Javadoc)
	 * @see org.jnode.vm.compiler.ir.Operand#getAddressingMode()
	 */
	public int getAddressingMode() {
		if (location instanceof StackLocation) {
			return Operand.MODE_STACK;
		} else if (location instanceof RegisterLocation) {
			return Operand.MODE_REGISTER;
		} else {
			throw new IllegalArgumentException("Undefined location: " + toString());
		}
	}

	public boolean equals(Object other) {
		if (other instanceof Variable) {
			Variable v = (Variable) other;
			return index == v.getIndex() &&
				ssaValue == v.getSSAValue();
		}
		return false;
	}
}
