
/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class LiveRange implements Comparable {
	private Variable variable;
	private int assignAddress;
	private int lastUseAddress;
	// TODO this needs to be a more specific class, location can be a
	// register or a local variable
	private Object location;

	/**
	 * 
	 */
	public LiveRange(Variable v) {
		this.variable = v;
		this.assignAddress = v.getAssignAddress();
		this.lastUseAddress = v.getLastUseAddress();
	}

	public boolean interferesWith(LiveRange other) {
		return lastUseAddress > other.getAssignAddress() ||
			other.lastUseAddress > assignAddress;
	}

	public Variable getVariable() {
		return variable;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object obj) {
		LiveRange other = (LiveRange) obj;
		return assignAddress - other.getVariable().getAssignAddress();
	}
	
	public String toString() {
		String leader = variable.toString() + ": " +
			assignAddress + "-" + lastUseAddress;
		if (location == null) {
			return leader;
		}
		return leader + " (" + location + ")";
	}

	/**
	 * @return
	 */
	public int getAssignAddress() {
		return assignAddress;
	}

	/**
	 * @return
	 */
	public int getLastUseAddress() {
		return lastUseAddress;
	}

	/**
	 * @return
	 */
	public Object getLocation() {
		return location;
	}

	/**
	 * @param string
	 */
	public void setLocation(Object string) {
		location = string;
	}
}
