/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.system.x86;

import org.jnode.system.DMAException;
import org.jnode.system.DMAResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.Resource;
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

    /**
     * @see org.jnode.system.Resource#getParent()
     */
    public Resource getParent() {
        return null;
    }
}
