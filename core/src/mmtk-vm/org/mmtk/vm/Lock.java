/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Common Public License (CPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/cpl1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */

 
package org.mmtk.vm;

import org.jnode.vm.scheduler.ProcessorLock;

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
