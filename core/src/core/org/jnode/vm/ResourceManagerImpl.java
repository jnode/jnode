/*
 * $Id$
 */
package org.jnode.vm;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.MemoryScanner;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.system.ResourcePermission;
import org.jnode.system.SimpleResourceOwner;

/**
 * Default implementation of ResourceManager.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class ResourceManagerImpl implements ResourceManager {
	
    private final ResourcePermission IOPORTS_PERM = new ResourcePermission("ioports");
    private final ResourcePermission MEMSCAN_PERM = new ResourcePermission("memoryScanner");
    private final MemoryScanner memScan;
    
	/**
	 * Hidden constructor.
	 */
	private ResourceManagerImpl() {
	    memScan = new MemoryScannerImpl();
	}
	
	protected static ResourceManager initialize() {
		try {
			final Address kernelStart = Unsafe.getKernelStart(); 
			final Address kernelEnd = Unsafe.getKernelEnd();
			final long kernelSize = Address.distance(kernelStart, kernelEnd); 
			MemoryResourceImpl.claimMemoryResource(new SimpleResourceOwner("kernel"), kernelStart, kernelSize, MEMMODE_NORMAL);

			final Address bootHeapStart = Unsafe.getBootHeapStart(); 
			final Address bootHeapEnd = Unsafe.getBootHeapEnd();
			final long bootHeapSize = Address.distance(bootHeapStart, bootHeapEnd); 
			MemoryResourceImpl.claimMemoryResource(new SimpleResourceOwner("bootheap"), bootHeapStart, bootHeapSize, MEMMODE_NORMAL);

			ResourceManager rm = new ResourceManagerImpl();
			InitialNaming.bind(NAME, rm);
			return rm;
		} catch (NamingException ex) {
			throw new Error("Cannot initialize ResourceManager");
		} catch (ResourceNotFreeException ex) {
			throw new Error("Claim kernel memory");
		}
	}

	/**
	 * Claim a range of IO ports
	 * @param owner
	 * @param startPort
	 * @param length
	 * @return The claimed resource
	 * @throws ResourceNotFreeException
	 */
	public IOResource claimIOResource(ResourceOwner owner, int startPort, int length)
	throws ResourceNotFreeException {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(IOPORTS_PERM);
	    }
		return IOResourceImpl.claimIOResource(owner, startPort, length);
	}

	/**
	 * Claim a memory region
	 * @param owner
	 * @param start
	 * @param size
	 * @param mode
	 * @return The claimed resource
	 * @throws ResourceNotFreeException
	 */
	public MemoryResource claimMemoryResource(ResourceOwner owner, Address start, long size, int mode) 
	throws ResourceNotFreeException {
		return MemoryResourceImpl.claimMemoryResource(owner, start, size, mode);
	}
	
	/**
	 * Register an interrupt handler for a given irq number.
	 * 
	 * @param owner
	 * @param irq
	 * @param handler
	 * @param shared
	 * @return True is the handler was set, false if there was already a handler for the given irq
	 *         number set.
	 * @throws ResourceNotFreeException
	 */
	public IRQResource claimIRQ(ResourceOwner owner, int irq, IRQHandler handler, boolean shared) 
	throws ResourceNotFreeException {
		return Unsafe.getCurrentProcessor().getIRQManager().claimIRQ(owner, irq, handler, shared);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given byte-array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(byte[] data) {
		return new MemoryResourceImpl(data, data.length, 1);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(char[] data) {
		return new MemoryResourceImpl(data, data.length, 2);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(short[] data) {
		return new MemoryResourceImpl(data, data.length, 2);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(int[] data) {
		return new MemoryResourceImpl(data, data.length, 4);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(long[] data) {
		return new MemoryResourceImpl(data, data.length, 8);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(float[] data) {
		return new MemoryResourceImpl(data, data.length, 4);
	}
	
	/**
	 * Create a MemoryResource wrapper around a given array.
	 * @param data
	 * @return The claimed resource
	 */
	public MemoryResource asMemoryResource(double[] data) {
		return new MemoryResourceImpl(data, data.length, 8);
	}
	
	/** 
	 * Gets the memory scanner.
	 * This method will requires a ResourcePermission("memoryScanner"). 
	 */
	public MemoryScanner getMemoryScanner() {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(MEMSCAN_PERM);
	    }
	    return memScan;
	}
}
