/*
 * $Id$
 */
package org.jnode.system;

/**
 * Hardware Interrupt resource.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface IRQResource extends Resource {
	
	/**
	 * Gets the IRQ number of this resource
	 * @return the IRQ number
	 */
	public int getIRQ();
	
	/**
	 * Is this a shared interrupt?
	 * @return boolean
	 */
	public boolean isShared();

}
