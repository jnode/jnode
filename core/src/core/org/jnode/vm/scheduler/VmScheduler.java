/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
 
package org.jnode.vm.scheduler;

import org.jnode.annotation.Inline;
import org.jnode.annotation.Internal;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.Uninterruptible;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.VmSystem;

/**
 * Thread scheduler. This scheduler is used by all processors in the system, so
 * all access to data structures are protected by processor locks.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmScheduler {

    /**
     * Reference to current architecture.
     */
    private final VmArchitecture architecture;

    /**
     * Lock for the allThreadsQueue.
     */
    private final ProcessorLock allThreadsLock;

    /**
     * Queue holding all threads.
     */
    private final VmThreadQueue.AllThreadsQueue allThreadsQueue;

    /**
     * My ready queue.
     */
    private final VmThreadQueue.ScheduleQueue readyQueue;

    /**
     * My sleep queue.
     */
    private final VmThreadQueue.SleepQueue sleepQueue;

    /**
     * Lock used to protect the ready and sleep queue.
     */
    private final ProcessorLock queueLock;

    /**
     * Default constructor.
     */
    public VmScheduler(VmArchitecture architecture) {
        this.architecture = architecture;
        this.allThreadsLock = new ProcessorLock();
        this.allThreadsQueue = new VmThreadQueue.AllThreadsQueue("scheduler-all");

        this.queueLock = new ProcessorLock();
        this.readyQueue = new VmThreadQueue.ScheduleQueue("scheduler-ready");
        this.sleepQueue = new VmThreadQueue.SleepQueue("scheduler-sleep");
    }

    /**
     * Call the visitor for all live threads.
     *
     * @param visitor
     */
    @Uninterruptible
    final VmThread getThreadById(int id) {
        // final SpinLock lock = vm.allThreadsLock;
        VmThreadQueueEntry e = this.allThreadsQueue.first;
        while (e != null) {
            if (e.thread.getId() == id) {
                return e.thread;
            }
            e = e.next;
        }
        return null;
    }

    /**
     * Register a thread in the list of all live threads.
     *
     * @param thread
     */
    final void registerThread(VmThread thread) {
        if (Vm.isWritingImage()) {
            allThreadsQueue.add(thread, "Vm");
        } else {
            allThreadsLock.lock();
            try {
                allThreadsQueue.add(thread, "Vm");
            } finally {
                allThreadsLock.unlock();
            }
        }
    }

    /**
     * Remove the given thread from the list of all threads.
     *
     * @param thread
     */
    final void unregisterThread(VmThread thread) {
        allThreadsLock.lock();
        try {
            allThreadsQueue.remove(thread);
            //todo recent change, more testing needed
            //remove the thread from readyQueue and sleepQueue too
            readyQueue.remove(thread);
            sleepQueue.remove(thread);
        } finally {
            allThreadsLock.unlock();
        }
    }

    /**
     * Call the visitor for all live threads.
     *
     * @param visitor
     */
    @Internal
    public final boolean visitAllThreads(VmThreadVisitor visitor) {
        allThreadsLock.lock();
        try {
            return allThreadsQueue.visit(visitor);
        } finally {
            allThreadsLock.unlock();
        }
    }

    /**
     * Add the given thread to the ready queue to the scheduler and remove it
     * from the sleep queue (if it still was on the sleep queue).
     *
     * @param thread
     * @param ignorePriority If true, the thread is always added to the back of the list,
     *                       regarding its priority.
     * @param caller
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final void addToReadyQueue(VmThread thread, boolean ignorePriority,
                               String caller) {
        try {
            // Get access to queues
            queueLock.lock();

            if (thread.isRunning() || thread.isYielding()) {
                sleepQueue.remove(thread);
                readyQueue.add(thread, ignorePriority, caller);
            } else {
                Unsafe
                    .debug("Thread must be in running state to add to ready queue, not ");
                Unsafe.debug(thread.getThreadState());
                architecture.getStackReader().debugStackTrace();
                Unsafe.die("addToReadyQueue");
            }
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Add the given thread to the sleep queue to this scheduler.
     *
     * @param thread
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     */
    @Uninterruptible
    final void addToSleepQueue(VmThread thread) {
        try {
            // Get access to queues
            queueLock.lock();

            sleepQueue.add(thread, null);
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Gets the first thread from the ready queue. If such a thread is
     * available, it is removed from the ready queue.
     *
     * @return
     */
    @KernelSpace
    @Uninterruptible
    final VmThread popFirstReadyThread() {
        try {
            // Get access to queues
            queueLock.lock();

            final VmThread newThread = readyQueue.first(VmMagic.currentProcessor());
            if (newThread != null) {
                readyQueue.remove(newThread);
                return newThread;
            }
            return null;
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Gets the first thread from the sleep queue that is ready to be woken up.
     * If such a thread is available, it is removed from the sleep queue.
     *
     * @return
     */
    @KernelSpace
    @Uninterruptible
    final VmThread popFirstSleepingThread() {
        try {
            // Get access to queues
            queueLock.lock();

            final VmThread newThread = sleepQueue.first(VmMagic.currentProcessor());
            if (newThread != null) {
                final long curTime = VmSystem.currentKernelMillis();
                if (newThread.canWakeup(curTime)) {
                    sleepQueue.remove(newThread);
                    return newThread;
                }
            }
            return null;
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Dump the state of the scheduler to the unsafe debug stream.
     */
    @KernelSpace
    @Uninterruptible
    final void dump() {
        try {
            // Get access to queues
            queueLock.lock();

            readyQueue.dump(false, null);
            sleepQueue.dump(false, null);
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Lock the queues for access by the current processor.
     */
    @Inline
    @Uninterruptible
    final void lock() {
        queueLock.lock();
    }

    /**
     * Unlock the queues.
     */
    @Inline
    @Uninterruptible
    final void unlock() {
        queueLock.unlock();
    }

    /**
     * @return The queue containing all ready threads.
     */
    @Inline
    final VmThreadQueue.ScheduleQueue getReadyQueue() {
        return readyQueue;
    }

    /**
     * @return The queue containing all sleeping threads.
     */
    @Inline
    final VmThreadQueue.SleepQueue getSleepQueue() {
        return sleepQueue;
    }

    /**
     * @return The queue containing all threads.
     */
    @Inline
    final VmThreadQueue.AllThreadsQueue getAllThreadsQueue() {
        return allThreadsQueue;
    }

    /**
     * @return The {@link VmStackReader} from the current architecture.
     */
    @Inline
    final VmStackReader getStackReader() {
        return architecture.getStackReader();
    }
}
