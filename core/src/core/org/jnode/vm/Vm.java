/*
 * $Id$
 */
package org.jnode.vm;

import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.HotMethodManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Vm extends VmSystemObject {

	/** The single instance */
	private static Vm instance;
	/** Are will in bootimage building phase? */
	private transient boolean bootstrap;
	/** The current architecture */
	private final VmArchitecture arch;
	/** The heap manager */
	private final VmHeapManager heapManager;
	/** The boot heap */
	private final VmBootHeap bootHeap;
	/** The hot method manager */
	private HotMethodManager hotMethodManager;

	/**
	 * Initialize a new instance
	 * 
	 * @param arch
	 */
	public Vm(VmArchitecture arch, VmHeapManager heapManager) {
		instance = this; 
		this.bootstrap = true;
		this.arch = arch;
		this.heapManager = heapManager;
		this.bootHeap = new VmBootHeap();
	}

	/**
	 * @return Returns the bootstrap.
	 */
	public final boolean isBootstrap() {
		return this.bootstrap;
	}

	/**
	 * @return Returns the arch.
	 */
	public final VmArchitecture getArch() {
		return this.arch;
	}

	/**
	 * @return Returns the instance.
	 */
	public static final Vm getVm() {
		return instance;
	}

	/**
	 * @return Returns the bootHeap.
	 */
	public final VmBootHeap getBootHeap() {
		return this.bootHeap;
	}
	
	/**
	 * @return Returns the heapManager.
	 */
	public final VmHeapManager getHeapManager() {
		return this.heapManager;
	}
	
	final void startHotMethodManager() {
		final VmStatics statics = Unsafe.getCurrentProcessor().getStatics();
		this.hotMethodManager = new HotMethodManager(arch, statics);
		hotMethodManager.start();
	}

	public static void main(String[] args) {
		final Vm vm = getVm();
		if ((vm != null) && !vm.isBootstrap()) {
			Unsafe.getCurrentProcessor().getStatics().dumpStatistics();
			if (vm.hotMethodManager != null) {
				vm.hotMethodManager.dumpStatistics();
			}
		}
	}
}
