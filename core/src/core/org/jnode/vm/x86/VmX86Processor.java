/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmThread;

/**
 * Processor implementation for the X86 architecture.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class VmX86Processor extends VmProcessor {
	
	/** The IRQ counters */
	private final int[] irqCount = new int[16];

	/**
	 * @param id
	 */
	public VmX86Processor(int id) {
		super(id, VmX86Architecture.INSTANCE);
	}

	/**
	 * Gets the IRQ counters array. 
	 * @return int[]
	 */
	protected final int[] getIrqCounters() {
		return irqCount;
	}
	
	/**
	 * Create a new thread
	 * @return The new thread
	 */
	protected VmThread createThread() {
		return new VmX86Thread();
	}
	
	/**
	 * Create a new thread
	 * @param javaThread
	 * @return The new thread
	 */
	public VmThread createThread(Thread javaThread) {
		return new VmX86Thread(javaThread);
	}
}
