/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

package org.jnode.vm.facade;

import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;

/**
 * Interface with a VM thread.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
public interface VmThread {

    /**
     * Visit all objects on the stack and register state of this thread.
     *
     * @param visitor
     * @param heapManager
     * @return {@code true} if the last visit returned {@code true} or no visit was
     * made, {@code false} otherwise.
     */
    boolean accept(ObjectVisitor visitor, VmHeapManager heapManager);

    /**
     * Gets the identifier of this thread.
     *
     * @return The id
     */
    int getId();

    /**
     * Get the Thread to which this VmThread belongs.
     *
     * @return The java thread
     */
    Thread asThread();

    /**
     * Detect a deadlock on this thread.
     * When the parameter <code>deadLockCycle</code> is null, the deadlock (if any) is traced by Unsafe.debug.
     * When the parameter <code>deadLockCycle</code> is not null, it will first be cleared and filled with involved
     * threads when this thread is part of a deadlock cycle.
     *
     * @param deadLockCycle   The list of thread involved in a deadlock. It might be null.
     * @param concurrentLocks If true, the search will include concurrent locks (defined by looking at
     *                        {@link java.util.concurrent.locks.AbstractOwnableSynchronizer#exclusiveOwnerThread} and
     *                        {@link java.lang.Thread#parkBlocker} fields.
     *                        <p/>
     *                        Look at method <code>find_deadlocks_at_safepoint</code> in hotspot sources (src/share/vm/services/threadService.cpp)
     *                        for further details.
     */
    void detectDeadlock(List<VmThread> deadLockCycle, boolean concurrentLocks);

    /**
     * @param lockedMonitors
     * @param lockedSynchronizers
     * @param maxDepth                   The maximal depth of the stack dump (-1 means entire stack; 0 means no stacktrace).
     * @param useCache                   If true, cachedOwnableSynchronizers is used instead of iterating over all heaps to search AbstractOwnableSynchronizer.
     * @param cachedOwnableSynchronizers A cache of AbstractOwnableSynchronizers.
     * @return
     */
    ThreadInfo getThreadInfo(boolean lockedMonitors, boolean lockedSynchronizers, int maxDepth,
                             boolean useCache, List<AbstractOwnableSynchronizer> cachedOwnableSynchronizers);
}
