/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public abstract class AbstractExceptionHandler extends VmSystemObject {

	private final VmConstClass catchType;

	/**
	 * Create a new instance
	 * @param catchType
	 */
	public AbstractExceptionHandler(VmConstClass catchType) {
		this.catchType = catchType;
	}

	/**
	 * Gets the classreference of the exception class this exception handler
	 * can handle.
	 * @return VmConstClass
	 */
	public final VmConstClass getCatchType() {
		return catchType;
	}
}
