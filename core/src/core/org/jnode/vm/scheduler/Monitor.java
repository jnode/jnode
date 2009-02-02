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

import org.jnode.vm.Unsafe;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoFieldAlignments;
import org.jnode.vm.annotation.NoInline;
import org.jnode.vm.annotation.Uninterruptible;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author epr
 */
@NoFieldAlignments
@Uninterruptible
@MagicPermission
public final class Monitor {

    /**
     * Number of locks on this monitor THIS FIELD MUST BE THE FIRST!!
     */
    private int lockCount;

    /**
     * Lock counter of the monitor itself. THIS FIELD MUST BE THE SECOND!!
     */
    private int monitorLock;

    /**
     * Thread that owns the monitor
     */
    private VmThread owner;

    /**
     * Thread queue for monitorenter/exit
     */
    private final VmThreadQueue.ScheduleQueue enterQueue;

    /**
     * Thread queue for wait/notify/notifyAll
     */
    private final VmThreadQueue.ScheduleQueue notifyQueue;

    /**
     * The previous monitor in a thread bound monitor chain
     */
    private Monitor previous;

    /**
     * Create a new instance
     */
    public Monitor() {
        this.lockCount = 0;
        this.monitorLock = 0;
        this.owner = null;
        this.enterQueue = new VmThreadQueue.ScheduleQueue("mon-enter");
        this.notifyQueue = new VmThreadQueue.ScheduleQueue("mon-notify");
    }

    /**
     * Create a new instance that has already been locked.
     *
     * @param owner
     * @param lockCount
     */
    Monitor(VmThread owner, int lockCount) {
        this.monitorLock = 0;
        this.owner = owner;
        if (owner != null)
            addToOwner();
        this.lockCount = lockCount;
        if (lockCount < 1) {
            throw new IllegalArgumentException("LockCount must be >= 1");
        }
        this.enterQueue = new VmThreadQueue.ScheduleQueue("mon-enter");
        this.notifyQueue = new VmThreadQueue.ScheduleQueue("mon-notify");
    }

    /**
     * Initialize this monitor. Only called from MonitorManager.
     *
     * @param owner
     * @param lockcount
     */
    final void initialize(VmThread owner, int lockcount) {
        dropFromOwner();
        this.owner = owner;
        if (owner != null)
            addToOwner();
        this.lockCount = lockcount;
    }

    /**
     * Enter the given monitor. This method will block until the monitor is
     * locked by the current thread.
     *
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    @Inline
    public final void enter() {
        // Am I already owner of this lock?
        if (this.owner == VmProcessor.current().getCurrentThread()) {
            // Yes, already owner, just increment lock counter
            lockCount++;
        } else {
            // No yet owner, try to obtain the lock
            enterSlowPath();
        }
    }

    /**
     * Slow path of enter (current thread is not the owner). This is a seperate
     * method to control the inlining of the native code compiler.
     */
    @NoInline
    private final void enterSlowPath() {
        // No yet owner, try to obtain the lock
        boolean loop = true;
        final Address lcAddr = getLCAddress();
        while (loop) {
            // Get current thread
            final VmThread current = VmMagic.currentProcessor().getCurrentThread();
            // Try to claim this monitor
            if (lcAddr.attempt(0, 1)) {
                loop = false;
                dropFromOwner();
                this.owner = current;
                addToOwner();
            } else {
                // Claim the lock for this monitor
                lock();
                try {
                    VmMagic.currentProcessor().disableReschedule(true);
                    prepareWait(current, enterQueue, VmThread.WAITING_ENTER, "mon-enter");
                } finally {
                    unlock();
                }
                // Release the monitor lock
                VmMagic.currentProcessor().suspend(true);
                // When we return here, another thread has given up
                // this monitor.
            }
        }
    }

    /**
     * Giveup this monitor.
     *
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    public final void exit() {
        String exMsg = null;
        if (owner != VmProcessor.current().currentThread) {
            exMsg = "Current thread is not the owner of this monitor";
        } else if (lockCount <= 0) {
            lockCount = 0;
            exMsg = "Monitor is not locked";
        } else if (lockCount > 1) {
            // Monitor is locked by current thread, decrement lockcount
            lockCount--;
        } else {
            // Monitor is locked by current thread and will decrement to 0.
            lock();
            try {
                wakeupWaitingThreads(enterQueue, true);
                dropFromOwner();
                owner = null;
                lockCount = 0;
            } finally {
                unlock();
            }
        }
        if (exMsg != null) {
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
        }
    }

    /**
     * Giveup this monitor.
     * Called from VmThread on thread stop.
     *
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    public final void release(VmThread thread) {
        if (owner != thread) {
            //todo enable this debug message. How can this happen???
            //Unsafe.debug("Current thread is not the owner of this monitor\n");
            return;
        }

        if (lockCount <= 0) {
            //todo enable this debug message. How can this happen???
            //Unsafe.debug("Monitor is not locked\n");
            return;
        }

        // Monitor is locked by current thread, decrement lockcount
        lockCount = 0;
        lock();
        try {
            wakeupWaitingThreads(enterQueue, true);
            dropFromOwner();
            owner = null;
            lockCount = 0;
        } finally {
            unlock();
        }
    }

    /**
     * Causes current thread to wait until another thread invokes the notify()
     * method or the notifyAll() method for this monitor. In other words, this
     * method behaves exactly as if it simply performs the call wait(0). The
     * current thread must own this monitor. The thread releases ownership of
     * this monitor and waits until another thread notifies threads waiting on
     * this object's monitor to wake up either through a call to the notify
     * method or the notifyAll method. The thread then waits until it can
     * re-obtain ownership of the monitor and resumes execution. This method
     * should only be called by a thread that is the owner of this monitor. See
     * the notify method for a description of the ways in which a thread can
     * become the owner of a monitor.
     *
     * @param timeout
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     * @throws InterruptedException
     */
    public final void Wait(long timeout) throws InterruptedException {
        final VmThread current = VmMagic.currentProcessor().getCurrentThread();
        // final int id = current.getId();
        String exMsg = null;
        if (owner != current) {
            exMsg = "Current thread is not the owner of this monitor";
        } else if (lockCount == 0) {
            exMsg = "Monitor is not locked";
        } else {
            final int oldLockCount = lockCount;
            final int waitState = (timeout > 0) ? VmThread.WAITING_NOTIFY_TIMEOUT : VmThread.WAITING_NOTIFY;
            lock();
            try {
                prepareWait(current, notifyQueue, waitState, "mon-notify");
                VmMagic.currentProcessor().disableReschedule(true);
            } finally {
                unlock();
            }
            // If there is a timeout, also add the current thread to the
            // sleep queue.
            if (timeout > 0) {
                current.wakeupTime = VmSystem.currentKernelMillis() + timeout;
                VmMagic.currentProcessor().getScheduler().addToSleepQueue(current);
            }
            dropFromOwner();
            owner = null;
            lockCount = 0;
            wakeupWaitingThreads(enterQueue, true);
            VmMagic.currentProcessor().suspend(true);
            // When we return here, we have been notified or there
            // was a timeout.

            if (!current.isRunning()) {
                Unsafe.debug("Back from wait, but state != running");
                Unsafe.debug("state=");
                Unsafe.debug(current.getThreadState());
                Unsafe.die("Wait");
            }

            if (timeout > 0) {
                // Screen.debug("<backfromwait-"); Screen.debug(id);
                // Screen.debug("/>");
                // Remove the current thread from the notifyQueue.
                // There is no need to remove myself from the sleep queue,
                // because this is done either by the scheduler or
                // indirect by wakeupWaitingThreads.
                lock();
                try {
                    notifyQueue.remove(current);
                } finally {
                    unlock();
                }
            }
            enter();
            this.lockCount = oldLockCount;
        }
        if (exMsg != null) {
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
        }
        // Check for InterruptedException
        current.testAndClearInterruptStatus();
    }

    /**
     * Notify threads waiting on this monitor.
     *
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    public final void NotifyAll() {
        Notify(true);
    }

    /**
     * Notify the first or all waiting threads on this monitor.
     *
     * @param all
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    final void Notify(boolean all) {
        final VmProcessor proc = VmProcessor.current();
        final VmThread current = proc.getCurrentThread();
        String exMsg = null;
        if (owner != current) {
            exMsg = "Current thread is not the owner of this monitor";
        } else if (lockCount == 0) {
            exMsg = "Monitor is not locked";
        } else {
            lock();
            try {
                wakeupWaitingThreads(notifyQueue, all);
            } finally {
                unlock();
            }
        }
        if (exMsg != null) {
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
        }
    }

    /**
     * Notify the all waiting threads on this monitor. This method does not
     * require the current thread to be the owner of the monitor, nor is an
     * exception thrown if the monitor is not locked.
     *
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    final boolean unsynchronizedNotifyAll() {
        if (lockNoWait()) {
            try {
                wakeupWaitingThreads(notifyQueue, true);
            } finally {
                unlock();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Is the given thread owner of this monitor?
     *
     * @param thread
     * @return boolean
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    @Inline
    final boolean isOwner(VmThread thread) {
        return (owner == thread);
    }

    /**
     * Gets the owner of this monitor, or null if not owned.
     *
     * @return The owner of this monitor, or null if not owned.
     */
    @KernelSpace
    @Uninterruptible
    @Inline
    final VmThread getOwner() {
        return owner;
    }

    /**
     * Is this monitor locked?
     *
     * @return boolean
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    @Inline
    final boolean isLocked() {
        return (lockCount > 0);
    }

    /**
     * Prepare the given thread for a waiting state.
     *
     * @param thread
     * @param queue
     * @param queueName
     * @return The queue
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    private final void prepareWait(VmThread thread,
                                   VmThreadQueue.ScheduleQueue queue, int waitState, String queueName) {
        if (monitorLock != 1) {
            Unsafe.debug("MonitorLock not claimed");
            Unsafe.die("prepareWait");
        }
        thread.prepareWait(this, waitState);
        queue.add(thread, false, "mon.prepareWait");
    }

    /**
     * Notify a single thread. The thread is remove from the notifyQueue and its
     * <code>wakeupAfterMonitor</code> method is called.
     *
     * @param thread
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    @NoInline
    private final void notifyThread(VmThread thread) {
        final VmThreadQueue.ScheduleQueue eq = this.enterQueue;
        if (eq != null) {
            eq.remove(thread);
        }
        final VmThreadQueue.ScheduleQueue nq = this.notifyQueue;
        if (nq != null) {
            nq.remove(thread);
        }
        thread.wakeupAfterMonitor(this);
    }

    /**
     * The given thread is removed from the notifyQueue.
     *
     * @param thread
     */
    @KernelSpace
    @Uninterruptible
    final void removeThreadFromQueues(VmThread thread) {
        final VmThreadQueue.ScheduleQueue eq = this.enterQueue;
        if (eq != null) {
            eq.remove(thread);
        }
        final VmThreadQueue.ScheduleQueue nq = this.notifyQueue;
        if (nq != null) {
            nq.remove(thread);
        }
    }

    /**
     * Wakeup all waiting threads.
     *
     * @param queue
     * @param all
     * @throws org.vmmagic.pragma.UninterruptiblePragma
     *
     */
    @Inline
    private final void wakeupWaitingThreads(VmThreadQueue.ScheduleQueue queue, boolean all) {
        if (queue != null) {
            while (!queue.isEmpty()) {
                final VmThread thread = queue.first();
                notifyThread(thread);
                if (!all) {
                    break;
                }
            }
        }
    }

    /**
     * Gets the address of the lockCount variable. It is assumed that this
     * variable is at offset 0 within this object!
     *
     * @return The address of lockCount
     */
    @Inline
    private final Address getLCAddress() {
        return ObjectReference.fromObject(this).toAddress();
    }

    /**
     * Claim access to this monitor. A monitor may only be locked for a small
     * amount of time, since this method uses a spinlock.
     *
     * @see #unlock()
     * @see #monitorLock
     */
    @Inline
    private final void lock() {
        //final VmProcessor proc = VmProcessor.current();
        final Address mlAddr = ObjectReference.fromObject(this).toAddress().add(4);
        while (!mlAddr.attempt(0, 1)) {
            //proc.yield(true); // Yield breaks the Uninterruptible idea, so don't use it!
        }
    }

    /**
     * Claim access to this monitor. Return true on success, false on failure
     *
     * @see #unlock()
     * @see #monitorLock
     */
    @Inline
    private final boolean lockNoWait() {
        final Address mlAddr = ObjectReference.fromObject(this).toAddress().add(4);
        return mlAddr.attempt(0, 1);
    }

    /**
     * Release access to this monitor. A monitor may only be locked for a small
     * amount of time, since this method uses a spinlock.
     *
     * @see #lock()
     * @see #monitorLock
     */
    @Inline
    private final void unlock() {
        monitorLock = 0;
    }

    //monitor chaining to handle thread stop

    /**
     * Returns the monitor previously owned by the owner thread of this monitor.
     *
     * @return the previous monitor
     */
    Monitor getPrevious() {
        return previous;
    }

    @Inline
    private void addToOwner() {
        Monitor lom = owner.getLastOwnedMonitor();
        if (lom == null) {
            //the first monitor
            owner.setLastOwnedMonitor(this);
        } else {
            if (lom.owner != this.owner) {
                //todo error
                return;
            } else {
                if (lom == this) {
                    //no need to add it
                    return;
                } else {
                    //add it
                    this.previous = lom;
                    owner.setLastOwnedMonitor(this);
                }
            }
        }
    }

    @Inline
    private void dropFromOwner() {
        if (owner == null) {
            //error
            return;
        }

        Monitor lom = owner.getLastOwnedMonitor();
        if (lom == null)
            return;

        if (lom != this)
            return;

        owner.setLastOwnedMonitor(lom.previous);
        lom.previous = null;
    }
}
