/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @author epr
 */
public final class VmByteCode extends AbstractCode {

	/** The method i'm a part of */
	private final VmMethod method;
	/** The constant pool where indexes in my bytecode refer to */
	private final VmCP cp;
	/** #Local variables of this method */
	private int noLocals;
	/** Max. #slots taken by this method on the stack */
	private int maxStack;
	/** Bytecode of this method */
	private byte[] bytecode;
	/** Exception handler table */
	private VmInterpretedExceptionHandler[] eTable;
	/** Line number table */
	private VmLineNumberMap lnTable;
	/** Is this object in use bye a method? If so, no modifications are allowed. */
	//private boolean locked;
	/** Data used by the native code compilers */
	private Object compilerData;
	
	/**
	 * Create a new instance
	 * @param method
	 * @param bytecode
	 * @param noLocals
	 * @param maxStack
	 * @param eTable
	 * @param lnTable
	 */
	public VmByteCode(VmMethod method, byte[] bytecode, int noLocals, int maxStack, VmInterpretedExceptionHandler[] eTable, VmLineNumberMap lnTable) {
		this.method = method;
		this.cp = method.getDeclaringClass().getCP();
		this.bytecode = bytecode;
		this.noLocals = noLocals;
		this.maxStack = maxStack;
		this.eTable = eTable;
		this.lnTable = lnTable;
		//this.locked = false;
	}

	/**
	 * Gets the actual bytecode. 
	 * Do not change the contents of the given array!
	 * @return the code
	 */
	public byte[] getBytecode() {
		return bytecode;
	}
	
	/**
	 * Gets the length of the bytecode
	 * @return the length
	 */
	public int getLength() {
		return bytecode.length;
	}

	/**
	 * Gets the maximum stack size
	 * @return The maximum stack size
	 */
	public int getMaxStack() {
		return maxStack;
	}

	/**
	 * Gets the number of local variables
	 * @return the number of local variables
	 */
	public int getNoLocals() {
		return noLocals;
	}

	/**
	 * Get the number of exception handlers
	 * @return The number of exception handlers
	 */
	public int getNoExceptionHandlers() {
		return (eTable == null) ? 0 : eTable.length;
	}

	/**
	 * Get the handler PC of the exception handler at a given index
	 * @param index
	 * @return The exception handler
	 */
	public VmInterpretedExceptionHandler getExceptionHandler(int index) {
		if (eTable != null) {
			return eTable[index];
		} else {
			throw new IndexOutOfBoundsException("eTable is null; index " + index);
		}
	}
	
	/**
	 * Gets all exception handler as unmodifiable list of VmInterpretedExceptionHandler
	 * instances.
	 * @return The handlers
	 */
	public List getExceptionHandlers() {
		if (eTable == null) {
			return Collections.EMPTY_LIST;
		} else {
			return Arrays.asList(eTable);
		}
	}
	
	/**
	 * Gets the line number table, or null if no line number table exists
	 * for this bytecode.
	 * @return
	 */
	public VmLineNumberMap getLineNrs() {
		return lnTable;
	}
	
	/**
	 * Gets the linenumber of a given program counter.
	 * @param pc
	 * @return The linenumber for the given pc, or -1 is not found.
	 */
	public int getLineNr(int pc) {
		final VmLineNumberMap lnTable = this.lnTable;
		if (lnTable != null) {
			return lnTable.findLineNr(pc);
		} else {
			return -1;
		}
	}
	
	/**
	 * Lock this object.
	 * This will make future modifications on this object fail. 
	 */
	final void lock() {
		//this.locked = true;
	}
	
	/**
	 * Gets the constant pool, where indexes in this bytecode refer to
	 * @return The constant pool
	 */
	public VmCP getCP() {
		return cp;
	}
	
	/**
	 * Gets the method where this bytecode is a part of
	 * @return The method
	 */
	public VmMethod getMethod() {
		return method;
	}

	/**
	 * @return Returns the compilerData.
	 */
	public final Object getCompilerData() {
		return this.compilerData;
	}

	/**
	 * @param compilerData The compilerData to set.
	 */
	public final void setCompilerData(Object compilerData) {
		this.compilerData = compilerData;
	}

}
