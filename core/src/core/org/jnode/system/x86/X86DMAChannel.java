/*
 * $Id$
 */
package org.jnode.system.x86;

import org.jnode.system.DMAException;
import org.jnode.system.DMAResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceOwner;

/**
 * @author epr
 */
public class X86DMAChannel implements DMAResource, DMAConstants {
	
	/** The parent */
	private final DMAPlugin service;
	/** The owner of this channel */
	private final ResourceOwner owner;
	/** Channel nr */
	private final int dmanr;
	
	public X86DMAChannel(DMAPlugin service, ResourceOwner owner, int dmanr) {
		this.service = service;
		this.owner = owner;
		this.dmanr = dmanr;
	}

	/**
	 * @param address
	 * @param length
	 * @param mode
	 * @see org.jnode.system.DMAResource#setup(MemoryResource, int, int)
	 * @throws IllegalArgumentException
	 * @throws DMAException
	 */
	public void setup(MemoryResource address, int length, int mode) 
	throws IllegalArgumentException, DMAException {
		final int x86Mode;
		switch (mode) {
			case MODE_READ: x86Mode = DMA_MODE_READ; break;
			case MODE_WRITE: x86Mode = DMA_MODE_WRITE; break;
			default: throw new IllegalArgumentException("Invalid mode");
		}
		service.setup(dmanr, address, length, x86Mode);
	}

	/**
	 * Enable the datatransfer of this channel. This may only be called
	 * after a succesful call to setup.
	 * @throws DMAException
	 */
	public void enable() 
	throws DMAException {
		service.enable(dmanr);
	}

	/**
	 * Gets the remaining length for this channel
	 * @return the remaining length
	 * @throws DMAException
	 */
	public int getLength()
	throws DMAException {
		return service.getLength(dmanr);	
	}
	
	/**
	 * @see org.jnode.system.Resource#getOwner()
	 * @return the owner of this resource
	 */
	public ResourceOwner getOwner() {
		return owner;
	}

	/**
	 * @see org.jnode.system.Resource#release()
	 */
	public void release() {
		service.release(this);
	}
	
	/**
	 * The DMA number of this channel
	 * @return the DMA number
	 */
	public int getDmaNr() {
		return dmanr;
	}

}
