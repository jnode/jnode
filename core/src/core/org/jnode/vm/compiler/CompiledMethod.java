/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.assembler.NativeStream;
import org.jnode.vm.classmgr.VmAddressMap;
import org.jnode.vm.classmgr.VmMethod;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class CompiledMethod {
	
	private NativeStream.ObjectRef codeStart;
	private NativeStream.ObjectRef codeEnd;
	private NativeStream.ObjectRef defExceptionHandler;
	private CompiledExceptionHandler[] exceptionHandlers;
	private final VmAddressMap addressTable;
	private final int optLevel;
	
	/**
	 * Initialize this instance
	 */
	public CompiledMethod(int optLevel) {
		this.addressTable = new VmAddressMap();
		this.optLevel = optLevel;
	}
	
	/**
	 * @return NativeStream.ObjectRef
	 */
	public NativeStream.ObjectRef getCodeEnd() {
		return codeEnd;
	}

	/**
	 * @return NativeStream.ObjectRef
	 */
	public NativeStream.ObjectRef getCodeStart() {
		return codeStart;
	}

	/**
	 * @return CompiledExceptionHandler[]
	 */
	public CompiledExceptionHandler[] getExceptionHandlers() {
		return exceptionHandlers;
	}

	/**
	 * Sets the codeEnd.
	 * @param codeEnd The codeEnd to set
	 */
	public void setCodeEnd(NativeStream.ObjectRef codeEnd) {
		this.codeEnd = codeEnd;
	}

	/**
	 * Sets the codeStart.
	 * @param codeStart The codeStart to set
	 */
	public void setCodeStart(NativeStream.ObjectRef codeStart) {
		this.codeStart = codeStart;
	}

	/**
	 * Sets the exceptionHandlers.
	 * @param exceptionHandlers The exceptionHandlers to set
	 */
	public void setExceptionHandlers(CompiledExceptionHandler[] exceptionHandlers) {
		this.exceptionHandlers = exceptionHandlers;
	}

	/**
	 * @return NativeStream.ObjectRef
	 */
	public NativeStream.ObjectRef getDefExceptionHandler() {
		return defExceptionHandler;
	}

	/**
	 * Sets the defExceptionHandler.
	 * @param defExceptionHandler The defExceptionHandler to set
	 */
	public void setDefExceptionHandler(
		NativeStream.ObjectRef defExceptionHandler) {
		this.defExceptionHandler = defExceptionHandler;
	}
	
	/**
	 * Add an address-pc mapping
	 * @param pc
	 * @param offset
	 */
	public final void add(VmMethod method, int pc, int offset) {
		addressTable.add(method, pc, offset);
	}
	
	/**
	 * Gets the mapping between address and PC
	 * @return Address map
	 */
	public VmAddressMap getAddressTable() {
		return addressTable;
	}

	/**
	 * Gets the optimization level used to create this compiled method.
	 * @return Returns the optimization level.
	 */
	final int getOptimizationLevel() {
		return this.optLevel;
	}

}
