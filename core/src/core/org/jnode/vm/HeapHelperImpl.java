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
 
package org.jnode.vm;

import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.memmgr.HeapHelper;
import org.jnode.vm.memmgr.VmHeapManager;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Word;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class HeapHelperImpl extends HeapHelper implements Uninterruptible {

    private static final class ThreadRootVisitor extends VmThreadVisitor {

        private VmHeapManager heapManager;

        private ObjectVisitor visitor;

        public final void initialize(ObjectVisitor visitor,
                VmHeapManager heapManager) {
            this.visitor = visitor;
            this.heapManager = heapManager;
        }

        public boolean visit(VmThread thread) {
            return thread.visit(visitor, heapManager);
        }
    }

    private final int flagsOffset;

    private final ThreadRootVisitor threadRootVisitor;

    /**
     * Initialize this instance.
     * 
     * @param arch
     */
    public HeapHelperImpl(VmArchitecture arch) {
        if (Vm.getVm() != null) {
            throw new SecurityException(
                    "Cannot instantiate HeapHelpImpl at runtime");
        }
        final int refSize = arch.getReferenceSize();
        flagsOffset = ObjectLayout.FLAGS_SLOT * refSize;
        this.threadRootVisitor = new ThreadRootVisitor();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#allocateBlock(Extent)
     */
    public final Address allocateBlock(Extent size) {
        return MemoryBlockManager.allocateBlock(size);
    }

    /**
     * Change the color of the given object from oldColor to newColor.
     * 
     * @param dst
     * @param oldColor
     * @param newColor
     * @return True if the color was changed, false if the current color of the
     *         object was not equal to oldColor.
     */
    public boolean atomicChangeObjectColor(Object dst, int oldColor,
            int newColor) {
        final Address addr = ObjectReference.fromObject(dst).toAddress().add(
                flagsOffset);
        int oldValue;
        int newValue;
        do {
            oldValue = addr.prepareInt();
            if ((oldValue & ObjectFlags.GC_COLOUR_MASK) != oldColor) {
                return false;
            }
            newValue = (oldValue & ~ObjectFlags.GC_COLOUR_MASK) | newColor;
        } while (!addr.attempt(oldValue, newValue));
        return true;
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#clear(Address, Extent)
     */
    public final void clear(Address dst, Extent size) {
        Unsafe.clear(dst, size);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#clear(Address, int)
     */
    public final void clear(Address dst, int size) {
        Unsafe.clear(dst, Extent.fromIntSignExtend(size));
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#copy(Address, Address, int)
     */
    public final void copy(Address src, Address dst, Extent size) {
        Unsafe.copy(src, dst, size);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#die(java.lang.String)
     */
    public final void die(String msg) {
        try {
            VmProcessor.current().getArchitecture().getStackReader()
                    .debugStackTrace();
        } finally {
            Unsafe.die(msg);
        }
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getBootHeapEnd()
     */
    public final Address getBootHeapEnd() {
        return Unsafe.getBootHeapEnd();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getBootHeapStart()
     */
    public final Address getBootHeapStart() {
        return Unsafe.getBootHeapStart();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getBootImageEnd()
     */
    public final Address getBootImageEnd() {
        return Unsafe.getBootHeapEnd();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getBootImageStart()
     */
    public final Address getBootImageStart() {
        return Unsafe.getKernelStart();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getHeapSize()
     */
    public Extent getHeapSize() {
        final Word end = Unsafe.getMemoryEnd().toWord();
        final Word start = Unsafe.getMemoryStart().toWord();
        return end.sub(start).toExtent();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getInflatedMonitor(java.lang.Object,
     *      org.jnode.vm.VmArchitecture)
     */
    public final Monitor getInflatedMonitor(Object object, VmArchitecture arch) {
        return MonitorManager.getInflatedMonitor(object, arch);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#invokeFinalizer(org.jnode.vm.classmgr.VmMethod,
     *      java.lang.Object)
     */
    public final void invokeFinalizer(VmMethod finalizer, Object object) {
        Unsafe.pushObject(object);
        Unsafe.invokeVoid(finalizer);
    }

    /**
     * Unblock all threads (on all processors). This method is called after a
     * call a call to {@link #stopThreadsAtSafePoint()}.
     */
    public void restartThreads() {
        VmProcessor.current().enableReschedule();
    }

    /**
     * Mark the given object as finalized.
     * 
     * @param dst
     */
    public final void setFinalized(Object dst) {
        final Address addr = ObjectReference.fromObject(dst).toAddress().add(
                flagsOffset);
        int oldValue;
        int newValue;
        do {
            oldValue = addr.prepareInt();
            if ((oldValue & ObjectFlags.STATUS_FINALIZED) != 0) {
                return;
            }
            newValue = oldValue | ObjectFlags.STATUS_FINALIZED;
        } while (!addr.attempt(oldValue, newValue));
        // } while (!Unsafe.atomicCompareAndSwap(addr, oldValue, newValue));
    }

    /**
     * Stop and block all threads (on all processors) on a GC safe point. Only
     * the calling thread (the GC thread) will continue.
     */
    public final void stopThreadsAtSafePoint() {
        VmProcessor.current().disableReschedule();
    }

    /**
     * Visit all roots of the object tree.
     * 
     * @param visitor
     */
    public void visitAllRoots(ObjectVisitor visitor, VmHeapManager heapManager) {
        final Vm vm = Vm.getVm();
        if (!vm.getSharedStatics().walk(visitor)) {
            return;
        }
        if (!VmProcessor.current().getIsolatedStatics().walk(visitor)) {
            return;
        }
        threadRootVisitor.initialize(visitor, heapManager);
        Vm.visitAllThreads(threadRootVisitor);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#bootArchitecture(boolean)
     */
    public final void bootArchitecture(boolean emptyMMap) 
    throws UninterruptiblePragma {
        Vm.getArch().boot(emptyMMap);
    }
}
