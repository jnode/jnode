/*
 * $Id$
 */
package org.jnode.vm;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import org.jnode.system.ResourceManager;
import org.jnode.util.BootableArrayList;
import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.HotMethodManager;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.pragma.NoInlinePragma;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Vm extends VmSystemObject implements Statistics {

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
	/** The list of all system processors */
	private final List processors;
	/** All statistics */
	private transient HashMap statistics;
	/** Should assertions be verified? */
	public static final boolean VerifyAssertions = true;

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
		this.processors = new BootableArrayList();
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
     * Returns the number of available processors currently available to the
     * virtual machine. This number may change over time; so a multi-processor
     * program want to poll this to determine maximal resource usage.
     *
     * @return the number of processors available, at least 1
     */
    public final int availableProcessors() {
        return processors.size();
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
			out.println("JNode VM " + vm.getVersion());
			vm.dumpStatistics(out);
			vm.getStatics().dumpStatistics(out);
			if (vm.hotMethodManager != null) {
				vm.hotMethodManager.dumpStatistics(out);
			}
			vm.heapManager.dumpStatistics(out);
			final SecurityManager sm = System.getSecurityManager();
			out.println("Security manager: " + sm);
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
    
    /**
     * Find all processors in the system and start them.
     */
    final void initializeProcessors(ResourceManager rm) {
        // Add the current (bootstrap) processor
        addProcessor(Unsafe.getCurrentProcessor());
        // Let the architecture find the processors
        arch.initializeProcessors(rm);
        // Show some info
        final int cnt = processors.size();
        if (cnt == 1) {
            System.out.println("Detected 1 processor");
        } else {
            System.out.println("Detected " + cnt + " processors");            
        }
    }
    
    /**
     * Add a discovered CPU.
     * @param cpu
     */
    final void addProcessor(VmProcessor cpu) {
        processors.add(cpu);
    }

    /**
     * Gets of create a counter with a given name.
     * @param name
     * @return The counter
     */
    public synchronized Counter getCounter(String name) {
        Counter cnt = (Counter)getStatistic(name);
        if (cnt == null) {
            cnt = new Counter(name, name);
            addStatistic(name, cnt);
        }
        return cnt;
    }
    
    private Statistic getStatistic(String name) {        
        if (statistics != null) {
            return (Statistic)statistics.get(name);
        } else {
            return null;
        }
    }
    
    private void addStatistic(String name, Statistic stat) {
        if (statistics == null) {
            statistics = new HashMap();
        }
        statistics.put(name, stat);
    }
    
    /**
     * @see org.jnode.util.Statistics#getStatistics()
     */
    public synchronized Statistic[] getStatistics() {
        if (statistics != null) {
            return (Statistic[])statistics.values().toArray(new Statistic[statistics.size()]);
        } else {
            return new Statistic[0];
        }
    }
    
    public void dumpStatistics(PrintStream out) {
        if (statistics != null) {
            final Statistic[] stats = getStatistics();
            for (int i = 0; i < stats.length; i++) {
                out.println(stats[i]);
            }        
        }
    }

    /**
     * Assert the given value to be true.
     * @param value
     */
    public static void _assert(boolean value) {
        if (!value) {
            assertionFailed(null, null);
        }
    }
    
    /**
     * Assert the given value to be true.
     * @param value
     */
    public static void _assert(boolean value, String msg) {
        if (!value) {
            assertionFailed(msg, null);
        }
    }
    
    /**
     * Assert the given value to be true.
     * @param value
     */
    public static void _assert(boolean value, String msg, String msg2) {
        if (!value) {
            assertionFailed(msg, msg2);
        }
    }
    
    /**
     * Throw an AssertionError with the given messages.
     * @param msg
     * @param msg2
     * @throws NoInlinePragma
     */
    private static void assertionFailed(String msg, String msg2) 
    throws NoInlinePragma {
        if ((msg == null) && (msg2 == null)) {
            msg = "Assertion failed";
        } else if (msg2 != null) {
            msg = msg + ": " + msg2;
        }
        throw new AssertionError(msg);
    }
}
