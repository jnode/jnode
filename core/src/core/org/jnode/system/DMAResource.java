/*
 * $Id$
 */
package org.jnode.system;


/**
 * Direct Memory Access resource.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface DMAResource extends Resource {
	
	/** I/O to memory */
	public static final int MODE_READ = 1;
	/** Memory to I/O */
	public static final int MODE_WRITE = 2;
	
	/**
	 * Prepare this channel for a data transfer.
	 * @param address
	 * @param length
	 * @param mode
	 * @throws IllegalArgumentException
	 * @throws DMAException
	 */
	public void setup(MemoryResource address, int length, int mode)
	throws IllegalArgumentException, DMAException;
	
	/**
	 * Enable the datatransfer of this channel. This may only be called
	 * after a succesful call to setup.
	 * @throws DMAException
	 */
	public void enable() 
	throws DMAException;

	/**
	 * Gets the remaining length for this channel
	 * @return The remaining length
	 * @throws DMAException
	 */
	public int getLength()
	throws DMAException;
}
