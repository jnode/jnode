/*
 * $Id$
 */
package org.jnode.system;


/**
 * Interface of Manager or Direct Memory Access resources.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DMAManager {

	/** Name used to bind this service into the Initial Namespace */
	public static final String NAME = "system/DMAService";
	
	/**
	 * Claim a DMA channel identified by the given number.
	 * @param owner
	 * @param dmanr
	 * @return The claimed resource
	 * @throws IllegalArgumentException Invalid dmanr
	 * @throws ResourceNotFreeException Requested DMA channel is in use 
	 */
	public DMAResource claimDMAChannel(ResourceOwner owner, int dmanr)
	throws IllegalArgumentException, ResourceNotFreeException; 
	
	
}
