/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.Address;

/**
 * @author epr
 */
public final class VmCompiledCode extends AbstractCode {

	/** The bytecode, this compiled code is derived from */
	private final VmByteCode bytecode;
	/** Address of native code of this method */
	private final Address nativeCode;
	/** Size in bytes of native code */
	private int nativeCodeSize1;
	/** Address of the default exception handler (only for compiled methods) */
	private final Address defaultExceptionHandler;
	/** Compiled code of this method */
	private final Object compiledCode1;
	/** Exception handler table */
	private final VmCompiledExceptionHandler[] eTable;
	/** Mapping between PC's and addresses */
	private final VmAddressMap addressTable;
	
	/**
	 * Create a new instance
	 * @param bytecode
	 * @param nativeCode
	 * @param compiledCode
	 * @param size
	 * @param eTable
	 * @param defaultExceptionHandler
	 * @param addressTable
	 */
	public VmCompiledCode(VmByteCode bytecode, Address nativeCode, Object compiledCode, int size, VmCompiledExceptionHandler[] eTable, Address defaultExceptionHandler, VmAddressMap addressTable) {
		this.bytecode = bytecode;
		this.nativeCode = nativeCode;
		this.compiledCode1 = compiledCode;
		this.eTable = eTable;
		this.nativeCodeSize1 = size;
		this.defaultExceptionHandler = defaultExceptionHandler;
		this.addressTable = addressTable;
		if (bytecode != null) {
			bytecode.lock();
		}
		addressTable.lock();
	}
	
	/**
	 * Returns the defaultExceptionHandler.
	 * @return Object
	 */
	public Address getDefaultExceptionHandler() {
		return defaultExceptionHandler;
	}
	
	/**
	 * Gets the length of the native code in bytes.
	 * @return the length
	 */
	public int getSize() {
		return nativeCodeSize1;
	}

	/**
	 * Get the number of exception handlers
	 * @return the number of exception handlers
	 */
	public int getNoExceptionHandlers() {
		return (eTable == null) ? 0 : eTable.length;
	}

	/**
	 * Get the handler PC of the exception handler at a given index
	 * @param index
	 * @return The handler
	 */
	public VmCompiledExceptionHandler getExceptionHandler(int index) {
		if (eTable != null) {
			return eTable[index];
		} else {
			throw new IndexOutOfBoundsException("eTable is null; index " + index);
		}
	}
	
	/**
	 * Gets the linenumber of a given address.
	 * @param address
	 * @return The linenumber for the given pc, or -1 is not found.
	 */
	public int getLineNr(Address address) {
		if (this.bytecode != null) {
			final int offset = (int)Address.distance(nativeCode, address);
			final int pc = addressTable.findPC(offset);
			return bytecode.getLineNr(pc);
			//return offset;
		}
		return -1;
	}
	
	/**
	 * Gets the address of the start of the native code.
	 * @return The address
	 */
	final Address getNativeCode() {
		return nativeCode;
	}
	
	final Object getCompiledCode() {
		return compiledCode1;
	}
	
	/**
	 * Does this method contain the given address?
	 * @param codePtr
	 * @return boolean
	 */
	public boolean contains(Address codePtr) {
		final int cmpStart = Address.compare(codePtr, nativeCode);
		final int cmpEnd = Address.compare(codePtr, Address.add(nativeCode, nativeCodeSize1));
		return ((cmpStart >= 0) && (cmpEnd < 0));
	}
}
