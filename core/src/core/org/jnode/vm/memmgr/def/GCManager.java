/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.memmgr.GCStatistics;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Word;

/**
 * @author epr
 */
@MagicPermission
final class GCManager extends VmSystemObject implements Uninterruptible {

    /**
     * The heap manager
     */
    private final DefaultHeapManager heapManager;

    /**
     * The mark stack
     */
    private final GCStack markStack;

    /**
     * An object visitor used for marking
     */
    private final GCMarkVisitor markVisitor;

    /**
     * An object visitor used for sweeping
     */
    private final GCSweepVisitor sweepVisitor;

    /**
     * An object visitor used for setting objects to GC colour white
     */
    private final GCSetWhiteVisitor setWhiteVisitor;

    /**
     * The object visitor that verifies the correctness of the object tree
     */
    private final GCVerifyVisitor verifyVisitor;

    /**
     * My statistics
     */
    private final DefGCStatistics stats;

    /**
     * The low level helper
     */
    private final HeapHelper helper;

    /**
     * The write barrier
     */
    private final DefaultWriteBarrier writeBarrier;

    /**
     * Debug mode?
     */
    private final boolean debug;

    /**
     * Create a new instance
     */
    public GCManager(DefaultHeapManager heapManager, VmArchitecture arch) {
        this.debug = true || Vm.getVm().isDebugMode();
        this.heapManager = heapManager;
        this.writeBarrier = (DefaultWriteBarrier) heapManager.getWriteBarrier();
        this.helper = heapManager.getHelper();
        this.markStack = new GCStack();
        this.markVisitor = new GCMarkVisitor(heapManager, arch, markStack);
        this.setWhiteVisitor = new GCSetWhiteVisitor(heapManager);
        this.verifyVisitor = new GCVerifyVisitor(heapManager, arch);
        this.sweepVisitor = new GCSweepVisitor(heapManager);
        this.stats = new DefGCStatistics();
    }

    /**
     * Do a garbage collection cycle.
     */
    final void gc() {
        // Prepare
        final VmBootHeap bootHeap = heapManager.getBootHeap();
        final VmDefaultHeap firstHeap = heapManager.getHeapList();
        stats.lastGCTime = System.currentTimeMillis();

        final boolean locking = (writeBarrier != null);
        final boolean verbose = (heapManager.getHeapFlags() & VmHeapManager.TRACE_BASIC) != 0;
        helper.stopThreadsAtSafePoint();
        heapManager.setGcActive(true);
        try {
            // Pre-GC verification
            if (debug) {
                if (false) {
                    // Turn back to true in case of problems in the GC.
                    if (verbose) {
                        heapManager.debug("<preverify/>");
                    }
                    verify(bootHeap, firstHeap);
                }
            }
            
            // Mark
            //helper.stopThreadsAtSafePoint();
            //heapManager.setGcActive(true);
            try {
                if (verbose) {
                    heapManager.debug("<mark/>");
                }
                markHeap(bootHeap, firstHeap, locking);
            } finally {
                //heapManager.setGcActive(false);
                //helper.restartThreads();
            }

            // Sweep
            if (verbose) {
                heapManager.debug("<sweep/>");
            }
            sweep(firstHeap);

            // Cleanup
            if (verbose) {
                heapManager.debug("<cleanup/>");
            }
            cleanup(bootHeap, firstHeap);

            // Verification
            if (debug) {
                if (false) {
                    // Turn back to true in case of problems in the GC.
                    if (verbose) {
                        heapManager.debug("<verify/>");
                    }
                    verify(bootHeap, firstHeap);
                }
            }
        } finally {
            heapManager.setGcActive(false);
            heapManager.resetCurrentHeap();
            helper.restartThreads();
        }

        // Start the finalization process
        heapManager.triggerFinalization();
    }

    /**
     * Mark all live objects in the heap.
     *
     * @param bootHeap
     * @param firstHeap
     */
    private final void markHeap(VmBootHeap bootHeap, VmDefaultHeap firstHeap, boolean locking) {

        if (writeBarrier != null) {
            writeBarrier.setActive(true);
        }

        final long startTime = VmSystem.currentKernelMillis();
        stats.lastMarkIterations = 0;
        long markedObjects = 0;
        boolean firstIteration = true;
        boolean wbChanged = false;
        do {
            // Do an iteration reset
            stats.lastMarkIterations++;
            markStack.reset();
            if (writeBarrier != null) {
                writeBarrier.resetChanged();
            }
            markVisitor.reset();
            markVisitor.setRootSet(true);
            // Mark all roots
            helper.visitAllRoots(markVisitor, heapManager);
//            statics.walk(markVisitor, resolver);
//            helper.visitAllThreads(threadMarkVisitor);
            // Mark every object in the rootset
//            bootHeap.walk(markVisitor, locking, 0, 0);
            if (!firstIteration) {
                // If there was an overflow in the last iteration,
                // we must also walk through the other heap to visit
                // all grey objects, since we must still mark
                // their children.
                markVisitor.setRootSet(false);
                bootHeap.walk(markVisitor, locking, Word.zero(), Word.zero());
                VmDefaultHeap heap = firstHeap;
                while ((heap != null) && (!markStack.isOverflow())) {
                    heap.walk(markVisitor, locking, Word.zero(), Word.zero());
                    heap = heap.getNext();
                }
            }
            // Test for an endless loop
            if ((markVisitor.getMarkedObjects() == 0) && markStack.isOverflow()) {
                // Oops... an endless loop
                Unsafe.debug("Endless loop in markHeap.... going to die");
                helper.die("GCManager.markHeap");
            }
            // Do some cleanup
            markedObjects += markVisitor.getMarkedObjects();
            firstIteration = false;
            if (writeBarrier != null) {
                wbChanged = writeBarrier.isChanged();
            }
        } while (markStack.isOverflow() || wbChanged);
        final long endTime = VmSystem.currentKernelMillis();
        stats.lastMarkDuration = endTime - startTime;
        stats.lastMarkedObjects = markedObjects;

        if (writeBarrier != null) {
            writeBarrier.setActive(false);
        }
    }

    /**
     * Sweep all heaps for dead objects.
     *
     * @param firstHeap
     */
    private void sweep(VmDefaultHeap firstHeap) {
        final long startTime = VmSystem.currentKernelMillis();
        VmDefaultHeap heap = firstHeap;
        while (heap != null) {
            heap.lock();
            sweepVisitor.setCurrentHeap(heap);
            heap.walk(sweepVisitor, false, Word.zero(), Word.zero());
            heap.unlock();
            heap = heap.getNext();
        }
        final long endTime = VmSystem.currentKernelMillis();
        stats.lastSweepDuration = endTime - startTime;
    }

    /**
     * Mark all objects white, so a next GC action is valid
     *
     * @param bootHeap
     * @param firstHeap
     */
    private void cleanup(VmBootHeap bootHeap, VmDefaultHeap firstHeap) {
        final long startTime = VmSystem.currentKernelMillis();
        bootHeap.walk(setWhiteVisitor, true, Word.zero(), Word.zero());
        VmDefaultHeap heap = firstHeap;
        while (heap != null) {
            heap.defragment();
            //heap.walk(setWhiteVisitor, locking);
            heap = heap.getNext();
        }
        final long endTime = VmSystem.currentKernelMillis();
        stats.lastCleanupDuration = endTime - startTime;
    }

    /**
     * Verify all heaps.
     *
     * @param bootHeap
     * @param firstHeap
     */
    private void verify(VmBootHeap bootHeap, VmDefaultHeap firstHeap) {
        final long startTime = VmSystem.currentKernelMillis();
        final Word zero = Word.zero();
        verifyVisitor.reset();
        bootHeap.walk(verifyVisitor, true, zero, zero);
        VmDefaultHeap heap = firstHeap;
        while (heap != null) {
            heap.walk(verifyVisitor, true, zero, zero);
            heap = heap.getNext();
        }
        final int errorCount = verifyVisitor.getErrorCount();
        if (errorCount > 0) {
            Unsafe.debug(errorCount);
            Unsafe.debug(" verify errors. ");
            helper.die("Corrupted heap");
        }
        final long endTime = VmSystem.currentKernelMillis();
        stats.lastVerifyDuration = endTime - startTime;
    }

    public GCStatistics getStatistics() {
        return stats;
    }
}
