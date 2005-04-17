/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.vm;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;

import org.jnode.system.ResourceManager;
import org.jnode.util.BootableArrayList;
import org.jnode.util.Counter;
import org.jnode.util.Statistic;
import org.jnode.util.Statistics;
import org.jnode.vm.classmgr.CompiledCodeList;
import org.jnode.vm.classmgr.VmAtom;
import org.jnode.vm.classmgr.VmStatics;
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
	/** Set this boolean to turn the hot method manager on/off */
	private final boolean runHotMethodManager = false;
	/** Should this VM run in debug mode? */
	private final boolean debugMode;
	/** Version of the OS and VM */
	private final String version;
	/** The statics table */
	private final VmStatics statics;
	/** The list of all system processors */
	private final List<VmProcessor> processors;
	/** All statistics */
	private transient HashMap<String, Statistic> statistics;
	/** The atom manager */
	private final VmAtom.Manager atomManager;
    /** Lock for accessing the all threads list */
    private final SpinLock allThreadsLock;
    /** List of all threads */
    private final VmThreadQueue.AllThreadsQueue allThreads;
    /** List of all compiled methods */
    private final CompiledCodeList compiledMethods;
	
	/** Should assertions be verified? */
	public static final boolean VerifyAssertions = true;
	/** For assertion checking things that should never happen. */
	public static final boolean NOT_REACHED = false;

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
		this.processors = new BootableArrayList<VmProcessor>();
		this.atomManager = new VmAtom.Manager();
        this.allThreadsLock = new SpinLock();
        this.allThreads = new VmThreadQueue.AllThreadsQueue("all");
        this.compiledMethods = new CompiledCodeList();
	}

	/**
	 * @return Returns the bootstrap.
	 */
	public final boolean isBootstrap() {
		return this.bootstrap;
	}

	/**
	 * Is JNode currently running.
	 * @return true or false
	 */
	public static final boolean isRunningVm() {
		return ((instance != null) && !instance.bootstrap);
	}

	/**
	 * Is the bootimage being written?
	 * @return true or false.
	 */
	public static final boolean isWritingImage() {
		return ((instance == null) || instance.bootstrap);
	}

	/**
	 * Causes JNode to stop working with a given message.
	 * @param msg
	 */
	public static final void sysFail(String msg) {
		if (isRunningVm()) {
			Unsafe.die(msg);
		}
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
	public static final VmHeapManager getHeapManager() {
		return instance.heapManager;
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
			vm.heapManager.dumpStatistics(out);
			final SecurityManager sm = System.getSecurityManager();
			out.println("Security manager: " + sm);
			for (VmProcessor cpu : vm.processors) {
				out.println("Processor " + vm.processors.indexOf(cpu));
				cpu.dumpStatistics(out);
			}
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
            statistics = new HashMap<String, Statistic>();
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
    
	/**
	 * @return Returns the atomManager.
	 */
	public static final VmAtom.Manager getAtomManager() {
		return instance.atomManager;
	}
    
    /**
     * Register a thread in the list of all live threads.
     * @param thread
     */
    static final void registerThread(VmThread thread) {
        final Vm vm = getVm();
        final VmThreadQueue.AllThreadsQueue q = vm.allThreads;
        if (isWritingImage()) {
            q.add(thread, "Vm");            
        } else {
            final SpinLock lock = vm.allThreadsLock;
            lock.lock();
            try {
                q.add(thread, "Vm");
            } finally {
                lock.unlock();
            }
        }
    }
    
    /**
     * Remove the given thread from the list of all threads.
     * @param thread
     */
    static final void unregisterThread(VmThread thread) {
        final Vm vm = getVm();
        final SpinLock lock = vm.allThreadsLock;
        final VmThreadQueue.AllThreadsQueue q = vm.allThreads;
        lock.lock();
        try {
            q.remove(thread);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Call the visitor for all live threads.
     * @param visitor
     */
    static final boolean visitAllThreads(VmThreadVisitor visitor) {
        final Vm vm = getVm();
        final SpinLock lock = vm.allThreadsLock;
        final VmThreadQueue.AllThreadsQueue q = vm.allThreads;
        lock.lock();
        try {
            return q.visit(visitor);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Call the visitor for all live threads.
     * @param visitor
     */
    static final VmThread getThreadById(int id) {
        final Vm vm = getVm();
        final SpinLock lock = vm.allThreadsLock;
        final VmThreadQueue.AllThreadsQueue q = vm.allThreads;
        VmThreadQueueEntry e = q.first;
        while (e != null) {
            if (e.thread.getId() == id) {
                return e.thread;
            }
            e = e.next;
        }
        return null;
    }
    
    /**
     * Gets the list of compiled methods.
     * @return Returns the compiledMethods.
     */
    public static final CompiledCodeList getCompiledMethods() {
        return instance.compiledMethods;
    }
}
