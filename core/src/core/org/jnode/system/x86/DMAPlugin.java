/*
 * $Id$
 */
package org.jnode.system.x86;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.system.DMAException;
import org.jnode.system.DMAManager;
import org.jnode.system.DMAResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.SimpleResourceOwner;
import org.jnode.vm.Address;

/**
 * DMA service for X86 architecture.
 * @author epr
 */
public class DMAPlugin extends Plugin implements DMAManager {

	/** All channels */
	private final X86DMAChannel[] channels;
	private DMA dma;
	
	/**
	 * Create a new instance
	 * @param descriptor
	 */
	public DMAPlugin(PluginDescriptor descriptor) {
		super(descriptor);
		channels = new X86DMAChannel[DMA.MAX];
		channels[4] = new X86DMAChannel(this, new SimpleResourceOwner("cascade"), 4);
	}

	/**
	 * @param owner
	 * @param dmanr
	 * @see org.jnode.system.DMAManager#claimDMAChannel(ResourceOwner, int)
	 * @return The claimed resource
	 * @throws IllegalArgumentException
	 * @throws ResourceNotFreeException
	 */
	public synchronized DMAResource claimDMAChannel(ResourceOwner owner, int dmanr)
	throws IllegalArgumentException, ResourceNotFreeException {
		if ((dmanr < 0) || (dmanr >= DMA.MAX)) {
			throw new IllegalArgumentException("Invalid dmanr " + dmanr);
		}
		
		if (channels[dmanr] != null) {
			throw new ResourceNotFreeException("DMA channel " + dmanr + " is in use");	
		}
		channels[dmanr] = new X86DMAChannel(this, owner, dmanr);
		return channels[dmanr];
	}

	/**
	 * Do the setup for a given channel
	 * @param dmanr
	 * @param memory
	 * @param length
	 * @param mode
	 * @throws DMAException
	 * @throws IllegalArgumentException
	 */	
	protected synchronized void setup(int dmanr, MemoryResource memory, int length, int mode) 
	throws DMAException, IllegalArgumentException {
		
		if (length > memory.getSize()) {
			throw new IllegalArgumentException("Length > memory.size");
		}
		final Address addr = memory.getAddress();
		dma.test(dmanr, addr, length);
		
		// Mask the channel
		dma.disable(dmanr);
		// Clear any datatransfers that are currently executing
		dma.clearFF(dmanr);
		
		// Set the mode
		dma.setMode(dmanr, mode);
		
		// Set the address+page
		dma.setAddress(dmanr, addr);
		// Set the transfer length
		dma.setLength(dmanr, length);
	}
	
	/**
	 * Enable the datatransfer of this channel. This may only be called
	 * after a succesful call to setup.
	 * @param dmanr
	 * @throws DMAException
	 */
	protected synchronized void enable(int dmanr) 
	throws DMAException {
		dma.enable(dmanr);
	}

	/**
	 * Gets the remaining length for this channel
	 * @param dmanr
	 * @return the remaining length
	 * @throws DMAException
	 */
	protected synchronized int getLength(int dmanr)
	throws DMAException {
		return dma.getLength(dmanr);	
	}
	
	/**
	 * Release the given channel
	 * @param channel
	 */
	protected synchronized void release(X86DMAChannel channel) {
		channels[channel.getDmaNr()] = null;
	}

	/**
	 * Start the plugin
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
		try {
			dma = new DMA();
			InitialNaming.bind(NAME, this);
		} catch (DMAException ex) {
			throw new PluginException("Cannot initialize DMA", ex);
		} catch (NamingException ex) {
			throw new PluginException("Cannot bind DMAService", ex);
		}
	}

	/**
	 * Stop the plugin
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		InitialNaming.unbind(NAME);
		dma.release();
		dma = null;
	}
}
