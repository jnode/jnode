/*
 * $Id$
 */
package org.jnode.vm.compiler.ir;

/**
 * @author Madhu Siddalingaiah
 * 
 */
public class StackLocation extends Location {
	private int displacement;

	/**
	 * @param displacement
	 */
	public StackLocation(int displacement) {
		super("local" + displacement);
		this.displacement = displacement;
	}
	
	public StackLocation() {
		this(0);
	}
	
	public void setDisplacement(int displacement) {
		this.displacement = displacement;
	}

	/**
	 * 
	 */
	public int getDisplacement() {
		return displacement;
	}
}
