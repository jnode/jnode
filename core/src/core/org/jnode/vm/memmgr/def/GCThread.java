/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.system.BootLog;
import org.jnode.vm.Monitor;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GCThread extends Thread {

    /** The manager */
    private final GCManager manager;

    /** Monitor for synchronizing access to my fields */
    private final Monitor heapMonitor;

    /** Is a GC run requested? */
    private boolean runNeeded;

    /** Is the GC currently active */
    private boolean gcActive;

    /**
     * Initialize this instance.
     * 
     * @param manager
     */
    public GCThread(GCManager manager, Monitor heapMonitor) {
        super("gc-thread");
        this.manager = manager;
        this.heapMonitor = heapMonitor;
    }

    /**
     * Trigger a GC run.
     * 
     * @param waitToFinish
     *            If true, block until the run is ready, if false, return
     *            immediately.
     */
    public final void trigger(boolean waitToFinish) {
        if (runNeeded && !waitToFinish) {
            return;
        }
        heapMonitor.enter();
        try {
            runNeeded = true;
            heapMonitor.NotifyAll();
            if (waitToFinish) {
                while (runNeeded || gcActive) {
                    try {
                        heapMonitor.Wait(0L);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
            }
        } finally {
            heapMonitor.exit();
        }
    }

    /**
     * Continue to GC.
     * 
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        while (true) {
            try {
                heapMonitor.enter();
                try {
                    while (!runNeeded) {
                        heapMonitor.Wait(0L);
                    }
                    gcActive = true;
                    runNeeded = false;
                    
                    manager.gc();
                    gcActive = false;
                    heapMonitor.NotifyAll();
                } finally {
                    heapMonitor.exit();
                }
            } catch (Throwable ex) {
                try {
                    BootLog.error("Error in GCThread", ex);
                } catch (Throwable ex2) {
                    // Ignore
                }
            }
        }
    }
}
 