/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
 * This local variable must have a value at indices into the code
 * array in the interval [startPC, startPC + length].
 * @author epr
 */
public class VmLocalVariable extends VmSystemObject {
	
	/** Start of the value value range. */
	private final char startPC;
	/** Length of the value value range. */
	private final char length;
	/** Name of the variable */
	private final String name;
	/** Type descriptor of the variable */
	private final String signature;
	/** Local variable index (on the stack) of the variable */
	private final char index;
	
	/**
	 * Create a new instance
	 * @param startPC
	 * @param length
	 * @param name
	 * @param signature
	 * @param index
	 */
	public VmLocalVariable(char startPC, char length, String name, String signature, char index) {
		this.startPC = startPC;
		this.length = length;
		this.name = name;
		this.signature = signature;
		this.index = index;
	}
	

	/**
	 * @return The index of this variable
	 */
	public char getIndex() {
		return this.index;
	}

	/**
	 * @return The length from startPc where this variable is valid
	 * @see #getStartPC()
	 */
	public char getLength() {
		return this.length;
	}

	/**
	 * @return The name of this variable
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The signature of this variable
	 */
	public String getSignature() {
		return this.signature;
	}

	/**
	 * @return The start PC where this variable is valid
	 */
	public char getStartPC() {
		return this.startPC;
	}

}
