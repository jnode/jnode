/*
 * $Id$
 */
package org.jnode.vm.scheduler;

import org.jnode.vm.LoadCompileService;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.pragma.UninterruptiblePragma;

/**
 * Thread scheduler. This scheduler is used by all processors in the system, so
 * all access to data structures are protected by processor locks.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public final class VmScheduler {

    /** Reference to current architecture */
    private final VmArchitecture architecture;

    /** Lock for the allThreadsQueue */
    private final ProcessorLock allThreadsLock;

    /** Queue holding all threads */
    private final VmAllThreadsQueue allThreadsQueue;

    /** My ready queue */
    private final VmThreadScheduleQueue readyQueue;

    /** My wakeup queue */
    private final VmThreadWakeupQueue wakeupQueue;

    /** Lock used to protect the ready and sleep queue */
    private final ProcessorLock queueLock;

    /**
     * Default constructor.
     */
    public VmScheduler(VmArchitecture architecture) {
        this.architecture = architecture;
        this.allThreadsLock = new ProcessorLock();
        this.allThreadsQueue = new VmAllThreadsQueue("scheduler-all");

        this.queueLock = new ProcessorLock();
        this.readyQueue = new VmThreadScheduleQueue("scheduler-ready");
        this.wakeupQueue = new VmThreadWakeupQueue("scheduler-wakeup");
    }

    /**
     * Call the visitor for all live threads.
     * 
     * @param visitor
     */
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
            allThreadsQueue.add(thread);
        } else {
            allThreadsLock.lock();
            try {
                allThreadsQueue.add(thread);
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
                Unsafe.debug(e.thread.getThreadStateName());
                Unsafe.debug(" p0x");
                Unsafe.debug(e.thread.priority);
                Unsafe.debug(" wf:");

                Object wf = e.thread.waitForObject;
                if (wf == null) {
                    Unsafe.debug("null");
                } else {
                    Unsafe.debug(VmMagic.getObjectType(wf).getName());
                }
                Unsafe.debug("\n");
                if (dumpStack && (stackReader != null)) {
                    stackReader.debugStackTrace(e.thread);
                    Unsafe.debug("\n");
                    Unsafe.debug("\n");
                }
            }
            e = e.next;
        }
    }

    /**
     * Dump the status of this queue on Unsafe.debug.
     */
    @KernelSpace
    final void dumpAllThreads(boolean dumpStack, VmStackReader stackReader) {
        VmThreadQueueEntry e = allThreadsQueue.first;
        while (e != null) {
            Unsafe.debug(e.thread.getName());
            Unsafe.debug(" id0x");
            Unsafe.debug(e.thread.getId());
            Unsafe.debug(" s0x");
            Unsafe.debug(e.thread.getThreadStateName());
            Unsafe.debug(" p0x");
            Unsafe.debug(e.thread.priority);
            Unsafe.debug(" wf:");

            Object wf = e.thread.waitForObject;
            if (wf == null) {
                Unsafe.debug("null");
            } else {
                Unsafe.debug(VmMagic.getObjectType(wf).getName());
            }
            Unsafe.debug("\n");
            if (dumpStack && (stackReader != null)) {
                stackReader.debugStackTrace(e.thread);
                Unsafe.debug("\n");
                Unsafe.debug("\n");
            }
            e = e.next;
        }
    }

    /**
     * Add the given thread to the ready queue to the scheduler and remove it
     * from the sleep queue (if it still was on the sleep queue)
     * 
     * @param thread
     * @param ignorePriority
     *            If true, the thread is always added to the back of the list,
     *            regarding its priority.
     * @param caller
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final void addToReadyQueue(VmThread thread, boolean ignorePriority) {
        try {
            // Get access to queues
            queueLock.lock();

            if (thread.isRunning() || thread.isYielding()) {
                readyQueue.enqueue(thread, ignorePriority);
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
     * Add the given thread to the wakeup queue of this scheduler. The thread
     * must have a preset proxy.
     * 
     * @param thread
     */
    @Uninterruptible
    final void addToWakeupQueue(VmThreadProxy proxy) {
        try {
            // Get access to queues
            queueLock.lock();

            wakeupQueue.enqueue(proxy);
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Gets the first ready to run thread.
     * 
     * Also process the wakeup queue.
     * 
     * @return
     */
    @KernelSpace
    @Uninterruptible
    final VmThread schedule() {
        try {
            // Get access to queues
            queueLock.lock();

            // Process the wakeup queue
            if (wakeupQueue.isReady()) {
                VmThread t = wakeupQueue.dequeue();
                while (t != null) {
                    t.wakeupFromWakeupQueue(this);
                    t = wakeupQueue.dequeue();
                }
            }

            // Get first ready to run thread.
            return readyQueue.dequeue();
        } finally {
            // Release access to queues
            queueLock.unlock();
        }

    }

    /**
     * Dump the state of the scheduler to the unsafe debug stream.
     * 
     */
    @KernelSpace
    @Uninterruptible
    final void dump() {
        try {
            // Get access to queues
            queueLock.lock();

            readyQueue.dump(false, null);
            wakeupQueue.dump(false, null);
        } finally {
            // Release access to queues
            queueLock.unlock();
        }
    }

    /**
     * Process all waiting KDB commands.
     * 
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
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    @KernelSpace
    private final void processKdbInput(int input) {
        switch ((char) input) {
        case '?':
        case 'h':
            Unsafe.debug("Commands:\n");
            Unsafe.debug("a   Print all threads\n");
            Unsafe.debug("A   Print stacktraces of all threads\n");
            Unsafe.debug("l   Show Load/Compile queue\n");
            Unsafe.debug("p   Ping\n");
            Unsafe.debug("q   Print thread queues\n");
            Unsafe.debug("r   Print stacktraces of ready-queue\n");
            Unsafe.debug("t   Print current thread\n");
            Unsafe.debug("v   Verify thread\n");
            Unsafe.debug("w   Print waiting threads\n");
            Unsafe.debug("W   Print stacktraces of waiting threads\n");
            break;
        case 'a':
            Unsafe.debug("<threads: ");
            Unsafe.debug("\n");
            dumpAllThreads(false, null);
            Unsafe.debug("/>\n");
            break;
        case 'A':
            Unsafe.debug("<threads: ");
            Unsafe.debug("\n");
            dumpAllThreads(true, architecture.getStackReader());
            Unsafe.debug("/>\n");
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
            wakeupQueue.dump(false, null);
            Unsafe.debug("/>\n");
        }
            break;
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
        }
            break;
        case 'T': {
            final VmThread currentThread = VmMagic.currentProcessor().currentThread;
            Unsafe.debug("<currentthread name='");
            Unsafe.debug(currentThread.getName());
            Unsafe.debug("' state='");
            Unsafe.debug(currentThread.getThreadStateName());
            architecture.getStackReader().debugStackTrace(currentThread);
            Unsafe.debug("'/>\n");
        }
            break;
        case '#':
            Unsafe.debug("Halt for ever\n");
            while (true)
                ;

            // default:
            // Unsafe.debug(input);
        }
    }

    /**
     * Lock the queues for access by the current processor and lock the other
     * lock. The scheduler lock is claimed first.
     */
    @Inline
    @Uninterruptible
    final void lock(ProcessorLock otherLock) {
        if (otherLock == null) {
            queueLock.lock();
        } else {
            ProcessorLock.lock(queueLock, otherLock);
        }
    }

    /**
     * Unlock the queues
     * 
     */
    @Inline
    @Uninterruptible
    final void unlock(ProcessorLock otherLock) {
        if (otherLock != null) {
            otherLock.unlock();
        }
        queueLock.unlock();
    }
}
