/*
 * $Id$
 */
package org.jnode.assembler;

/**
 * Interface that contains methods that can be used during the creation
 * of the boot image, but not at runtime.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface BootImageNativeStream {

	/**
	 * Write a reference to the given object
	 * @param object
	 */
	public void writeObjectRef(Object object);

}
