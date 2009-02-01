/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.vm.memmgr.def;

import org.jnode.system.BootLog;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.scheduler.Monitor;
import org.vmmagic.unboxed.Word;

/**
 * Thread used to invoke the {@link java.lang.Object#finalize()}method of all
 * objects that are about to be reclamed.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
final class FinalizerThread extends Thread {

    /**
     * The heap manager
     */
    private final DefaultHeapManager heapManager;

    /**
     * Monitor for synchronizing access to my fields
     */
    private final Monitor monitor;

    /**
     * The number of triggers received
     */
    private int triggerCount;
    /**
     * The number of finalization runs performed
     */
    private int runCount;

    /**
     * My visitor
     */
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
     * @param waitToFinish If true, block until the run is ready, if false, return
     *                     immediately.
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
        VmDefaultHeap heap = heapManager.getHeapList();
        final Word colorMask = Word.fromIntZeroExtend(ObjectFlags.GC_COLOUR_MASK);
        final Word yellow = Word.fromIntZeroExtend(ObjectFlags.GC_YELLOW);
        while (heap != null) {
            visitor.setCurrentHeap(heap);
            heap.walk(visitor, true, colorMask, yellow);
            heap = heap.getNext();
        }
    }
}
