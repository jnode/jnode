/*
 * $Id$
 */
package org.jnode.vm.compiler;

import org.jnode.vm.VmSystemObject;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class CompiledIMT extends VmSystemObject {

	/**
	 * Gets the address of the IMT code table.
	 * 
	 * @return
	 */
	public abstract Object getIMTAddress();

}