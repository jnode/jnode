/*
 * $Id$
 */
package org.jnode.vm.classmgr;

import org.jnode.vm.VmSystemObject;

/**
 * @author epr
 */
public abstract class AbstractCode extends VmSystemObject {

	/**
	 * Get the number of exception handlers
	 * @return int
	 */
	public abstract int getNoExceptionHandlers();
}