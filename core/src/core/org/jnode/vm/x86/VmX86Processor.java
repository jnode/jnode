/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.vm.CpuID;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmThread;
import org.jnode.vm.classmgr.VmStatics;

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
	public VmX86Processor(int id, VmX86Architecture arch, VmStatics statics, X86CpuID cpuId) {
		super(id, arch, statics);
		if (cpuId != null) {
			setCPUID(cpuId);
		}
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
	
	/**
	 * Load the CPU id.
	 * @return CpuID
	 */
	protected CpuID loadCPUID(int[] id) {
		return new X86CpuID(id);
	}
}
