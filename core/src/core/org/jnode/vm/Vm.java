/*
 * $Id$
 */
package org.jnode.vm;

import java.io.PrintStream;

import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.HotMethodManager;
import org.jnode.vm.memmgr.VmHeapManager;

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
	/** The hot method manager */
	private HotMethodManager hotMethodManager;
	/** Set this boolean to turn the hot method manager on/off */
	private final boolean runHotMethodManager = false;
	/** Should this VM run in debug mode? */
	private final boolean debugMode;
	/** Version of the OS and VM */
	private final String version;
	/** The statics table */
	private final VmStatics statics;

	/**
	 * Initialize a new instance
	 * 
	 * @param arch
	 */
	public Vm(String version, VmArchitecture arch, VmHeapManager heapManager, VmStatics statics, boolean debugMode) {
		instance = this;
		this.version = version;
		this.debugMode = debugMode;
		this.bootstrap = true;
		this.arch = arch;
		this.heapManager = heapManager;
		this.statics = statics;
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
	 * @return Returns the heapManager.
	 */
	public final VmHeapManager getHeapManager() {
		return this.heapManager;
	}

	/**
	 * Start the hot method compiler.
	 *  
	 */
	final void startHotMethodManager() {
		if (runHotMethodManager) {
			this.hotMethodManager = new HotMethodManager(arch, statics);
			hotMethodManager.start();
		}
	}

	/**
	 * Show VM info.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final Vm vm = getVm();
		if ((vm != null) && !vm.isBootstrap()) {
			final PrintStream out = System.out;
			vm.getStatics().dumpStatistics(out);
			if (vm.hotMethodManager != null) {
				vm.hotMethodManager.dumpStatistics(out);
			}
			vm.heapManager.dumpStatistics(out);
		}
	}
	
    /**
     * Does this VM run in debug mode.
     * @return Returns the debugMode.
     */
    public final boolean isDebugMode() {
        return this.debugMode;
    }
    
    /**
     * @return Returns the statics.
     */
    public final VmStatics getStatics() {
        return this.statics;
    }
    
    /**
     * Gets the version of the current VM.
     * @return Returns the version.
     */
    public final String getVersion() {
        return this.version;
    }
}
