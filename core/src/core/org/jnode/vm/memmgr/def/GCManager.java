/*
 * $Id$
 */
package org.jnode.vm.memmgr.def;

import org.jnode.assembler.ObjectResolver;
import org.jnode.vm.Uninterruptible;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.memmgr.HeapHelper;

/**
 * @author epr
 */
final class GCManager extends VmSystemObject implements Uninterruptible {

    /** The heap manager */
    private final DefaultHeapManager heapManager;

    /** The mark stack */
    private final GCStack markStack;

    /** An object visitor used for marking */
    private final GCMarkVisitor markVisitor;

    /** An object visitor used for setting objects to GC colour white */
    private final GCSetWhiteVisitor setWhiteVisitor;

    /** The object visitor that verifies the correctness of the object tree */
    private final GCVerifyVisitor verifyVisitor;

    /** My statistics */
    private final GCStatistics stats;

    /** The object resolver */
    private final ObjectResolver resolver;

    /** The statics table */
    private final VmStatics statics;

    /** The low level helper */
    private final HeapHelper helper;

    /** The write barrier */
    private final DefaultWriteBarrier writeBarrier;

    /** Debug mode? */
    private final boolean debug;

    /**
     * Create a new instance
     */
    public GCManager(DefaultHeapManager heapManager, VmArchitecture arch,
            VmStatics statics) {
        this.debug = Vm.getVm().isDebugMode();
        this.heapManager = heapManager;
        this.writeBarrier = (DefaultWriteBarrier) heapManager.getWriteBarrier();
        this.helper = heapManager.getHelper();
        this.markStack = new GCStack();
        this.markVisitor = new GCMarkVisitor(heapManager, arch, markStack);
        this.setWhiteVisitor = new GCSetWhiteVisitor(heapManager);
        this.verifyVisitor = new GCVerifyVisitor(heapManager, arch);
        this.stats = new GCStatistics();
        this.statics = statics;
        this.resolver = new Unsafe.UnsafeObjectResolver();
    }

    /**
     * Do a garbage collection cycle.
     */
    final void gc() {
        // Prepare
        final VmBootHeap bootHeap = heapManager.getBootHeap();
        final VmAbstractHeap firstHeap = heapManager.getFirstHeap();
        stats.lastGCTime = System.currentTimeMillis();

        // Mark
        helper.stopThreadsAtSafePoint();
        heapManager.setGcActive(true);
        try {
            Unsafe.debug("<mark/>");
            if (writeBarrier != null) {
                writeBarrier.setActive(true);
            }
            markHeap(bootHeap, firstHeap);
            if (writeBarrier != null) {
                writeBarrier.setActive(false);
            }

            // Sweep
            Unsafe.debug("<sweep/>");
            sweep(firstHeap);

            // Cleanup
            Unsafe.debug("<cleanup/>");
            cleanup(bootHeap, firstHeap);

            // Verification
            if (debug) {
                Unsafe.debug("<verify/>");
                verify(bootHeap, firstHeap);
            }
        } finally {
            heapManager.setGcActive(false);
            heapManager.resetCurrentHeap();
            helper.restartThreads();
        }

        Unsafe.debug("</gc free=");
        Unsafe.debug(heapManager.getFreeMemory());
        Unsafe.debug("/>");

        if (debug) {
            System.out.println(stats);
        }
    }

    /**
     * Mark all live objects in the heap.
     * 
     * @param bootHeap
     * @param firstHeap
     */
    private final void markHeap(VmBootHeap bootHeap, VmAbstractHeap firstHeap) {
        final long startTime = VmSystem.currentKernelMillis();
        long markedObjects = 0;
        boolean firstIteration = true;
        boolean wbChanged = false;
        do {
            // Do an iteration reset
            markStack.reset();
            if (writeBarrier != null) {
                writeBarrier.resetChanged();
            }
            markVisitor.reset();
            markVisitor.setRootSet(true);
            statics.walk(markVisitor, resolver);
            // Mark every object in the rootset
            bootHeap.walk(markVisitor);
            if (!firstIteration) {
                // If there was an overflow in the last iteration,
                // we must also walk through the other heap to visit
                // all grey objects, since we must still mark
                // their children.
                markVisitor.setRootSet(false);
                VmAbstractHeap heap = firstHeap;
                while ((heap != null) && (!markStack.isOverflow())) {
                    heap.walk(markVisitor);
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
    }

    /**
     * Sweep all heaps for dead objects.
     * 
     * @param firstHeap
     */
    private void sweep(VmAbstractHeap firstHeap) {
        final long startTime = VmSystem.currentKernelMillis();
        VmAbstractHeap heap = firstHeap;
        long freedBytes = 0;
        while (heap != null) {
            freedBytes += heap.collect();
            heap = heap.getNext();
        }
        final long endTime = VmSystem.currentKernelMillis();
        stats.lastSweepDuration = endTime - startTime;
        stats.lastFreedBytes = freedBytes;
    }

    /**
     * Mark all objects white, so a next GC action is valid
     * 
     * @param bootHeap
     * @param firstHeap
     */
    private void cleanup(VmBootHeap bootHeap, VmAbstractHeap firstHeap) {
        bootHeap.walk(setWhiteVisitor);
        VmAbstractHeap heap = firstHeap;
        while (heap != null) {
            heap.walk(setWhiteVisitor);
            heap = heap.getNext();
        }
    }

    /**
     * Verify all heaps.
     * 
     * @param bootHeap
     * @param firstHeap
     */
    private void verify(VmBootHeap bootHeap, VmAbstractHeap firstHeap) {
        verifyVisitor.reset();
        bootHeap.walk(verifyVisitor);
        VmAbstractHeap heap = firstHeap;
        while (heap != null) {
            heap.walk(verifyVisitor);
            heap = heap.getNext();
        }
        final int errorCount = verifyVisitor.getErrorCount();
        if (errorCount > 0) {
            Unsafe.debug(errorCount);
            Unsafe.debug(" verify errors. ");
            helper.die("Corrupted heap");
        }
    }
}
