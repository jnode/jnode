/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2004 JNode.org
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

import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.jnode.vm.classmgr.VmClassType;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.memmgr.HeapHelper;
import org.vmmagic.pragma.Uninterruptible;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class HeapHelperImpl extends HeapHelper implements Uninterruptible {

    private final int flagsOffset;

    private final int tibOffset;

    /**
     * Initialize this instance.
     * 
     * @param arch
     */
    public HeapHelperImpl(VmArchitecture arch) {
        if (Vm.getVm() != null) { throw new SecurityException(
                "Cannot instantiate HeapHelpImpl at runtime"); }
        final int refSize = arch.getReferenceSize();
        flagsOffset = ObjectLayout.FLAGS_SLOT * refSize;
        tibOffset = ObjectLayout.TIB_SLOT * refSize;
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#addressToLong(org.jnode.vm.Address)
     */
    public final long addressToLong(VmAddress a) {
        return Unsafe.addressToLong(a);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#allocateBlock(int)
     */
    public final VmAddress allocateBlock(int size) {
        return MemoryBlockManager.allocateBlock(size);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#clear(org.jnode.vm.Address, int)
     */
    public final void clear(VmAddress dst, int size) {
        Unsafe.clear(dst, size);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#copy(org.jnode.vm.Address,
     *      org.jnode.vm.Address, int)
     */
    public final void copy(VmAddress src, VmAddress dst, int size) {
        Unsafe.copy(src, dst, size);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getBootHeapEnd()
     */
    public final VmAddress getBootHeapEnd() {
        return Unsafe.getBootHeapEnd();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getBootHeapStart()
     */
    public final VmAddress getBootHeapStart() {
        return Unsafe.getBootHeapStart();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getByte(java.lang.Object, int)
     */
    public final byte getByte(Object src, int offset) {
        return Unsafe.getByte(src, offset);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getInt(java.lang.Object, int)
     */
    public final int getInt(Object src, int offset) {
        return Unsafe.getInt(src, offset);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getObject(java.lang.Object, int)
     */
    public final Object getObject(Object src, int offset) {
        return Unsafe.getObject(src, offset);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getAddress(Object, int)
     */
    public final VmAddress getAddress(Object src, int offset) {
        return Unsafe.getAddress(src, offset);
    }

    /**
     * Gets the color of the given object.
     * 
     * @param src
     * @return @see org.jnode.vm.classmgr.ObjectFlags#GC_BLACK
     * @see org.jnode.vm.classmgr.ObjectFlags#GC_GREY
     * @see org.jnode.vm.classmgr.ObjectFlags#GC_WHITE
     * @see org.jnode.vm.classmgr.ObjectFlags#GC_YELLOW
     */
    public final int getObjectColor(Object src) {
        return Unsafe.getObjectFlags(src) & ObjectFlags.GC_COLOUR_MASK;
    }

    /**
     * Gets the flags of the given object.
     */
    public final int getObjectFlags(Object src) {
        return Unsafe.getObjectFlags(src);
    }

    /**
     * Has the given object been finalized.
     * 
     * @param src
     * @return
     */
    public final boolean isFinalized(Object src) {
        return ((Unsafe.getObjectFlags(src) & ObjectFlags.STATUS_FINALIZED) != 0);
    }

    /**
     * Mark the given object as finalized.
     * 
     * @param dst
     */
    public final void setFinalized(Object dst) {
        final VmAddress addr = Unsafe.add(Unsafe.addressOf(dst), flagsOffset);
        int oldValue;
        int newValue;
        do {
            oldValue = Unsafe.getInt(dst, flagsOffset);
            if ((oldValue & ObjectFlags.STATUS_FINALIZED) != 0) { return; }
            newValue = oldValue | ObjectFlags.STATUS_FINALIZED;
        } while (!Unsafe.atomicCompareAndSwap(addr, oldValue, newValue));
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getStack(org.jnode.vm.VmThread)
     */
    public final Object getStack(VmThread thread) {
        return thread.getStack();
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getTib(java.lang.Object)
     */
    public final Object getTib(Object object) {
        return Unsafe.getObject(object, tibOffset);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#getVmClass(java.lang.Object)
     */
    public final VmClassType getVmClass(Object object) {
        return Unsafe.getVmClass(object);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#setByte(java.lang.Object, int, byte)
     */
    public final void setByte(Object dst, int offset, byte value) {
        Unsafe.setByte(dst, offset, value);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#setInt(java.lang.Object, int, int)
     */
    public final void setInt(Object dst, int offset, int value) {
        Unsafe.setInt(dst, offset, value);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#setObject(java.lang.Object, int,
     *      java.lang.Object)
     */
    public final void setObject(Object dst, int offset, Object value) {
        Unsafe.setObject(dst, offset, value);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#unsafeSetObjectFlags(Object, int)
     */
    public final void unsafeSetObjectFlags(Object dst, int flags) {
        Unsafe.setObjectFlags(dst, flags);
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
        final VmAddress addr = Unsafe.add(Unsafe.addressOf(dst), flagsOffset);
        int oldValue;
        int newValue;
        do {
            oldValue = Unsafe.getInt(dst, flagsOffset);
            if ((oldValue & ObjectFlags.GC_COLOUR_MASK) != oldColor) { return false; }
            newValue = (oldValue & ~ObjectFlags.GC_COLOUR_MASK) | newColor;
        } while (!Unsafe.atomicCompareAndSwap(addr, oldValue, newValue));
        return true;
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#add(org.jnode.vm.Address, int)
     */
    public final VmAddress add(VmAddress address, int offset) {
        return Unsafe.add(address, offset);
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#addressOf(java.lang.Object)
     */
    public final VmAddress addressOf(Object object) {
        return Unsafe.addressOf(object);
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#addressOf32(java.lang.Object)
     */
    public final int addressOf32(Object object) {
        return Unsafe.addressToInt(Unsafe.addressOf(object));
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#addressOf64(java.lang.Object)
     */
    public final long addressOf64(Object object) {
        return Unsafe.addressToLong(Unsafe.addressOf(object));
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#addressOfArrayData(java.lang.Object)
     */
    public final VmAddress addressOfArrayData(Object array) {
        return VmAddress.addressOfArrayData(array);
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#objectAt(org.jnode.vm.Address)
     */
    public final Object objectAt(VmAddress ptr) {
        return Unsafe.objectAt(ptr);
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#objectAt32(int)
     */
    public final Object objectAt32(int ptr) {
        return Unsafe.objectAt(Unsafe.intToAddress(ptr));
    }

    /**
     * @see org.jnode.assembler.ObjectResolver#objectAt64(long)
     */
    public final Object objectAt64(long ptr) {
        return Unsafe.objectAt(Unsafe.longToAddress(ptr));
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
     * @see org.jnode.vm.memmgr.HeapHelper#getInflatedMonitor(java.lang.Object,
     *      org.jnode.vm.VmArchitecture)
     */
    public final Monitor getInflatedMonitor(Object object, VmArchitecture arch) {
        return MonitorManager.getInflatedMonitor(object, arch);
    }

    /**
     * @see org.jnode.vm.memmgr.HeapHelper#die(java.lang.String)
     */
    public final void die(String msg) {
        try {
            Unsafe.getCurrentProcessor().getArchitecture().getStackReader()
                    .debugStackTrace();
        } finally {
            Unsafe.die(msg);
        }
    }

    /**
     * Stop and block all threads (on all processors) on a GC safe point. Only
     * the calling thread (the GC thread) will continue.
     */
    public final void stopThreadsAtSafePoint() {
        Unsafe.getCurrentProcessor().disableReschedule();
    }

    /**
     * Unblock all threads (on all processors). This method is called after a
     * call a call to {@link #stopThreadsAtSafePoint()}.
     */
    public void restartThreads() {
        Unsafe.getCurrentProcessor().enableReschedule();
    }
}