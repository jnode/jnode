/*
 * $Id$
 *
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
 
package org.jnode.vm.memmgr;

import org.jnode.vm.ObjectVisitor;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.scheduler.Monitor;
import org.vmmagic.pragma.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class HeapHelper implements Uninterruptible {

    /**
     * Mark the given object as finalized.
     *
     * @param src
     */
    public abstract void setFinalized(Object src);

    /**
     * Change the color of the given object from oldColor to newColor.
     *
     * @param dst
     * @param oldColor
     * @param newColor
     * @return True if the color was changed, false if the current color of the object was not equal to oldColor.
     */
    public abstract boolean atomicChangeObjectColor(Object dst, int oldColor, int newColor);

    public abstract void copy(Address src, Address dst, Extent size);

    public abstract void clear(Address dst, int size);

    public abstract void clear(Address dst, Extent size);

    public abstract Address allocateBlock(Extent size);

    /**
     * Gets the start address of the boot image
     */
    public abstract Address getBootImageStart();

    /**
     * End the end address of the boot image
     */
    public abstract Address getBootImageEnd();

    /**
     * Gets the start address of the boot heap
     */
    public abstract Address getBootHeapStart();

    /**
     * End the end address of the boot heap
     */
    public abstract Address getBootHeapEnd();

    /**
     * Gets the amount of memory available to the memory manager
     */
    public abstract Extent getHeapSize();

    public abstract void invokeFinalizer(VmMethod finalizer, Object object);

    public abstract void die(String msg);

    /**
     * Gets the inflated monitor of an object (if any).
     *
     * @param object
     * @param arch
     * @return The inflated monitor of the given object, or null if the given object has no
     *         inflated monitor.
     */
    public abstract Monitor getInflatedMonitor(Object object, VmArchitecture arch);

    /**
     * Stop and block all threads (on all processors) on a GC safe point.
     * Only the calling thread (the GC thread) will continue.
     */
    public abstract void stopThreadsAtSafePoint();

    /**
     * Unblock all threads (on all processors).
     * This method is called after a call a call to {@link #stopThreadsAtSafePoint()}.
     */
    public abstract void restartThreads();

    /**
     * Visit all roots of the object tree.
     *
     * @param visitor
     */
    public abstract void visitAllRoots(ObjectVisitor visitor, VmHeapManager heapManager);

    /**
     * Boot the architecture object.
     *
     * @param emptyMMap If true all page mappings in the AVAILABLE region
     *                  are removed.
     */
    public abstract void bootArchitecture(boolean emptyMMap);
}
