/*
 * Created on Nov 23, 2003
 *
 * mailto:madhu@madhu.com
 */
package org.jnode.vm.compiler.ir;

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
	private AssignOP assignOP;

	// TODO compute liveness by updating lastUseOP when it is used by
	// later instructions
	/*
	 * The operation where this variable is last used
	 */
	private IOP lastUseOP;

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

	public Operand propagate() {
		if (assignOP instanceof VariableRefAssignOP) {
			return ((VariableRefAssignOP) assignOP).getRHS();
		}
		if (assignOP instanceof ConstantRefAssignOP) {
			return ((ConstantRefAssignOP) assignOP).getRHS();
		}
		return this;
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
	 * @return
	 */
	public AssignOP getAssignOP() {
		return assignOP;
	}

	/**
	 * @return
	 */
	public IOP getLastUseOP() {
		return lastUseOP;
	}

	/**
	 * @param assignOP
	 */
	public void setAssignOP(AssignOP assignOP) {
		this.assignOP = assignOP;
	}

	/**
	 * @param iop
	 */
	public void setLastUseOP(IOP iop) {
		lastUseOP = iop;
	}
}
