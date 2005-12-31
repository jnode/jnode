/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 
package org.mmtk.vm;

import org.jnode.vm.ProcessorLock;

/**
 * Simple, fair locks with deadlock detection. The implementation mimics a
 * deli-counter and consists of two values: the ticket dispenser and the
 * now-serving display, both initially zero. Acquiring a lock involves grabbing
 * a ticket number from the dispenser using a fetchAndIncrement and waiting
 * until the ticket number equals the now-serving display. On release, the
 * now-serving display is also fetchAndIncremented. This implementation relies
 * on there being less than 1<<32 waiters.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Perry Cheng
 */
public final class Lock extends ProcessorLock {

    public static int verbose = 0; // show who is acquiring and releasing the
                                    // locks

    private final String name;
    
    public static void fullyBooted() {
    }

    /**
     * Initialize this instance.
     * @param str
     */
    public Lock(String str) {
        this.name = str;
    }

    // Try to acquire a lock and spin-wait until acquired.
    // (1) The isync at the end is important to prevent hardware instruction
    // re-ordering
    // from floating instruction below the acquire above the point of
    // acquisition.
    // (2) A deadlock is presumed to have occurred if the number of retries
    // exceeds MAX_RETRY.
    // (3) When a lock is acquired, the time of acquistion and the identity of
    // acquirer is recorded.
    //
    public final void acquire() {
        lock();
    }

    public void check(int w) {
        // TODO Understand me
    }

    // Release the lock by incrementing serving counter.
    // (1) The sync is needed to flush changes made while the lock is held and
    // also prevent
    // instructions floating into the critical section.
    // (2) When verbose, the amount of time the lock is ehld is printed.
    //
    public final void release() {
        unlock();
    }
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
}
