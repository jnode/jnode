/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.system.BootLog;
import org.jnode.vm.Monitor;
import org.jnode.vm.classmgr.ObjectFlags;

/**
 * Thread used to invoke the {@link java.lang.Object#finalize()}method of all
 * objects that are about to be reclamed.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FinalizerThread extends Thread {

    /** The heap manager */
    private final DefaultHeapManager heapManager;

    /** Monitor for synchronizing access to my fields */
    private final Monitor monitor;

    /** The number of triggers received */
    private int triggerCount;
    /** The number of finalization runs performed */
    private int runCount;
    
    /** My visitor */
    private final FinalizerVisitor visitor;

    /**
     * Initialize this instance.
     * 
     * @param heapManager
     */
    public FinalizerThread(DefaultHeapManager heapManager) {
        super("finalizer-thread");
        this.heapManager = heapManager;
        this.monitor = new Monitor();
        this.visitor = new FinalizerVisitor(heapManager.getHelper());
    }

    /**
     * Trigger a GC run.
     * 
     * @param waitToFinish
     *            If true, block until the run is ready, if false, return
     *            immediately.
     */
    public final void trigger(boolean waitToFinish) {
        triggerCount++;
        monitor.enter();
        try {
            monitor.NotifyAll();
            if (waitToFinish) {
                while (triggerCount != runCount) {
                    try {
                        monitor.Wait(0L);
                    } catch (InterruptedException ex) {
                        // Ignore
                    }
                }
            }
        } finally {
            monitor.exit();
        }
    }

    /**
     * Continue to call runFinalization.
     * 
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        while (true) {
            try {
                monitor.enter();
                try {
                    while (triggerCount == runCount) {
                        monitor.Wait(0L);
                    }
                    runFinalization();
                    runCount++;
                    monitor.NotifyAll();
                } finally {
                    monitor.exit();
                }
            } catch (Throwable ex) {
                try {
                    BootLog.error("Error in FinalizerThread", ex);
                } catch (Throwable ex2) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Go through all heaps and run the finalize method of all objects that
     * are unreachable and still need finalization.
     */
    private final void runFinalization() {
        VmAbstractHeap heap = heapManager.getHeapList();
        while (heap != null) {
            visitor.setCurrentHeap(heap);
            heap.walk(visitor, true, ObjectFlags.GC_COLOUR_MASK, ObjectFlags.GC_YELLOW);
            heap = heap.getNext();
        }
    }
}
