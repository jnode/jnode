/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.Address;

/**
 * @author epr
 */
public class VmCompiledExceptionHandler extends AbstractExceptionHandler {

	private final Address handler;
	private final Address startPtr;
	private final Address endPtr;

	/**
	 * Create a new instance
	 * @param catchType
	 * @param start
	 * @param end
	 * @param handler
	 */
	public VmCompiledExceptionHandler(VmConstClass catchType, Address start, Address end, Address handler) {
		super(catchType);
		this.startPtr = start;
		this.endPtr = end;
		this.handler = handler;
	}

	/**
	 * Returns the endPtr.
	 * @return Object
	 */
	public Address getEnd() {
		return endPtr;
	}

	/**
	 * Returns the handler.
	 * @return Object
	 */
	public Address getHandler() {
		return handler;
	}

	/**
	 * Returns the startPtr.
	 * @return Object
	 */
	public Address getStart() {
		return startPtr;
	}
	
	/**
	 * Is the given address between start and end.
	 * @param address
	 * @return True if address is between start and end, false otherwise
	 */
	public boolean isInScope(Address address) {
		final int cmpStart = Address.compare(address, startPtr);
		final int cmpEnd = Address.compare(address, endPtr);
		return ((cmpStart >= 0) && (cmpEnd < 0));
	}
}
