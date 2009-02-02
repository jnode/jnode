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
 
package org.jnode.vm.scheduler;

import org.jnode.vm.LoadCompileService;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * Thread scheduler. This scheduler is used by all processors in the system, so
 * all access to data structures are protected by processor locks.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmScheduler {

    /**
     * Reference to current architecture
     */
    private final VmArchitecture architecture;

    /**
     * Lock for the allThreadsQueue
     */
    private final ProcessorLock allThreadsLock;

    /**
     * Queue holding all threads
     */
    private final VmThreadQueue.AllThreadsQueue allThreadsQueue;

    /**
     * My ready queue
     */
    private final VmThreadQueue.ScheduleQueue readyQueue;

    /**
     * My sleep queue
     */
    private final VmThreadQueue.SleepQueue sleepQueue;

    /**
     * Lock used to protect the ready and sleep queue
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
     * Dump the status of this queue on Unsafe.debug.
     */
    @KernelSpace
    final void verifyThreads() {
        VmThreadQueueEntry e = allThreadsQueue.first;
        while (e != null) {
            e.thread.verifyState();
            e = e.next;
        }
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
     * Dump the status of this queue on Unsafe.debug.
     */
    @KernelSpace
    final void dumpWaitingThreads(boolean dumpStack, VmStackReader stackReader) {
        VmThreadQueueEntry e = allThreadsQueue.first;
        while (e != null) {
            if (e.thread.isWaiting()) {
                Unsafe.debug(e.thread.getName());
                Unsafe.debug(" id0x");
                Unsafe.debug(e.thread.getId());
                Unsafe.debug(" s0x");
                Unsafe.debug(e.thread.getThreadState());
                Unsafe.debug(" p0x");
                Unsafe.debug(e.thread.priority);
                Unsafe.debug(" wf:");
                VmThread waitFor = e.thread.getWaitForThread();
                Unsafe.debug((waitFor != null) ? waitFor.getName() : "none");
                Unsafe.debug("\n");
                if (dumpStack && (stackReader != null)) {
                    stackReader.debugStackTrace(e.thread);
                    Unsafe.debug("\n");
                }
            }
            e = e.next;
        }
    }

    /**
     * Add the given thread to the ready queue to the scheduler and remove it
     * from the sleep queue (if it still was on the sleep queue)
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
     * Process all waiting KDB commands.
     */
    @Uninterruptible
    @KernelSpace
    @Inline
    final void processAllKdbInput() {
        int ch;
        while ((ch = Unsafe.readKdbInput()) >= 0) {
            processKdbInput(ch);
        }
    }

    /**
     * Process the input from the kernel debugger.
     *
     * @param input
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     */
    @Uninterruptible
    @KernelSpace
    private final void processKdbInput(int input) {
        switch ((char) input) {
            case '?':
            case 'h':
                Unsafe.debug("Commands:\n");
                Unsafe.debug("l   Show Load/Compile queue\n");
                Unsafe.debug("p   Ping\n");
                Unsafe.debug("q   Print thread queues\n");
                Unsafe.debug("r   Print stacktraces of ready-queue\n");
                Unsafe.debug("t   Print current thread\n");
                Unsafe.debug("v   Verify thread\n");
                Unsafe.debug("w   Print waiting threads\n");
                Unsafe.debug("W   Print stacktraces of waiting threads\n");
                break;
            case 'l':
                Unsafe.debug("<load-compile-service: ");
                Unsafe.debug("\n");
                LoadCompileService.showInfo();
                Unsafe.debug("/>\n");
                break;
            case 'p':
                Unsafe.debug("<ping/>");
                break;
            case 'q': {
                final VmThread currentThread = VmMagic.currentProcessor().currentThread;
                Unsafe.debug("<queues: current-thread name='");
                Unsafe.debug(currentThread.getName());
                Unsafe.debug("' state='");
                Unsafe.debug(currentThread.getThreadStateName());
                Unsafe.debug("\n");
                readyQueue.dump(false, null);
                sleepQueue.dump(false, null);
                Unsafe.debug("/>\n");
                break;
            }
            case 'r':
                Unsafe.debug("<traces: ");
                Unsafe.debug("\n");
                readyQueue.dump(true, architecture.getStackReader());
                Unsafe.debug("/>\n");
                break;
            case 'v':
                Unsafe.debug("<verify: ");
                Unsafe.debug("\n");
                verifyThreads();
                Unsafe.debug("/>\n");
                break;
            case 'w':
                Unsafe.debug("<waiting: ");
                Unsafe.debug("\n");
                dumpWaitingThreads(false, null);
                Unsafe.debug("/>\n");
                break;
            case 'W':
                Unsafe.debug("<waiting: ");
                Unsafe.debug("\n");
                dumpWaitingThreads(true, architecture.getStackReader());
                Unsafe.debug("/>\n");
                break;
            case 't': {
                final VmThread currentThread = VmMagic.currentProcessor().currentThread;
                Unsafe.debug("<currentthread name='");
                Unsafe.debug(currentThread.getName());
                Unsafe.debug("' state='");
                Unsafe.debug(currentThread.getThreadStateName());
                Unsafe.debug("'/>\n");
                break;
            }
            case 'T': {
                final VmThread currentThread = VmMagic.currentProcessor().currentThread;
                Unsafe.debug("<currentthread name='");
                Unsafe.debug(currentThread.getName());
                Unsafe.debug("' state='");
                Unsafe.debug(currentThread.getThreadStateName());
                architecture.getStackReader().debugStackTrace(currentThread);
                Unsafe.debug("'/>\n");
                break;
            }
            case '#':
                Unsafe.debug("Halt for ever\n");
                while (true)
                    ;

                // default:
                // Unsafe.debug(input);
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
     * Unlock the queues
     */
    @Inline
    @Uninterruptible
    final void unlock() {
        queueLock.unlock();
    }
}
