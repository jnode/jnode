/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmAddress;
import org.vmmagic.unboxed.Address;

/**
 * @author epr
 */
public class VmCompiledExceptionHandler extends AbstractExceptionHandler {

	private final VmAddress handler;
	private final VmAddress startPtr;
	private final VmAddress endPtr;

	/**
	 * Create a new instance
	 * @param catchType
	 * @param start
	 * @param end
	 * @param handler
	 */
	public VmCompiledExceptionHandler(VmConstClass catchType, VmAddress start, VmAddress end, VmAddress handler) {
		super(catchType);
		this.startPtr = start;
		this.endPtr = end;
		this.handler = handler;
	}

	/**
	 * Returns the endPtr.
	 * @return Object
	 */
	public VmAddress getEnd() {
		return endPtr;
	}

	/**
	 * Returns the handler.
	 * @return Object
	 */
	public VmAddress getHandler() {
		return handler;
	}

	/**
	 * Returns the startPtr.
	 * @return Object
	 */
	public VmAddress getStart() {
		return startPtr;
	}
	
	/**
	 * Is the given address between start and end.
	 * @param address
	 * @return True if address is between start and end, false otherwise
	 */
	public boolean isInScope(Address address) {
		final Address start = Address.fromAddress(startPtr);
		final Address end = Address.fromAddress(endPtr);
		
		return address.GE(start) && address.LT(end);
	}
}
