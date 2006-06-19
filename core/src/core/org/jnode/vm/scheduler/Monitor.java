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
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author epr
 */
@NoFieldAlignments
@Uninterruptible
@MagicPermission
public final class Monitor {

    /** Number of locks on this monitor THIS FIELD MUST BE THE FIRST!! */
    private int lockCount;

    /** Mutex gaurding access to the structures of this Monitor */
    private final ProcessorLock mutex;

    /** Thread that owns the monitor */
    private VmThread owner;

    /** Thread queue for monitorenter/exit */
    private final VmThreadScheduleQueue enterQueue;

    /** Thread queue for wait/notify/notifyAll */
    private final VmThreadWaitQueue notifyQueue;
    
    private final Object object;

    /**
     * Create a new instance
     */
    public Monitor(Object object) {
        this.lockCount = 0;
        this.owner = null;
        this.object = object;
        this.mutex = new ProcessorLock();
        this.enterQueue = new VmThreadScheduleQueue("mon-enter");
        this.notifyQueue = new VmThreadWaitQueue("mon-notify");
    }

    /**
     * Create a new instance that has already been locked.
     * 
     * @param owner
     * @param lockCount
     */
    Monitor(VmThread owner, int lockCount, Object object) {
        this.owner = owner;
        this.lockCount = lockCount;
        if (lockCount < 1) {
            throw new IllegalArgumentException("LockCount must be >= 1");
        }
        this.object = object;
        this.mutex = new ProcessorLock();
        this.enterQueue = new VmThreadScheduleQueue("mon-enter");
        this.notifyQueue = new VmThreadWaitQueue("mon-notify");
    }

    /**
     * Initialize this monitor. Only called from MonitorManager.
     * 
     * @param owner
     * @param lockcount
     */
    final void initialize(VmThread owner, int lockcount) {
        this.owner = owner;
        this.lockCount = lockcount;
    }

    /**
     * Enter the given monitor. This method will block until the monitor is
     * locked by the current thread.
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
        while (true) {
            // Lock this monitor first
            mutex.lock();
            
            // Try to claim this monitor
            if (getLCAddress().attempt(0, 1)) {
                // Success, we now own this monitor
                this.owner = VmMagic.currentProcessor().currentThread;
                mutex.unlock();
                return;
            } else {
                // Get current thread
                final VmThread current = VmMagic.currentProcessor().currentThread;
                // Claim the lock for this monitor
                VmMagic.currentProcessor().disableReschedule(false, null);
                current.goSleepByMonitor(this, object, null, VmThread.WAITING_ENTER);
                enterQueue.enqueue(current, false);
                // Release the monitor lock
                mutex.unlock();
                VmMagic.currentProcessor().suspend(false, null);
                // When we return here, another thread has given up
                // this monitor.
            }
        }
    }

    /**
     * Giveup this monitor.
     * 
     * @throws UninterruptiblePragma
     */
    public final void exit() {
        String exMsg = null;
        
        mutex.lock();
        if (owner != VmMagic.currentProcessor().currentThread) {
            exMsg = "Current thread is not the owner of this monitor";
            mutex.unlock();
        } else if (lockCount <= 0) {
            lockCount = 0;
            exMsg = "Monitor is not locked";
            mutex.unlock();
        } else if (lockCount > 1) {
            // Monitor is locked by current thread, decrement lockcount
            lockCount--;
            mutex.unlock();
        } else {
            // Monitor is locked by current thread and will decrement to 0.
            VmMagic.currentProcessor().disableReschedule(false, null);
            owner = null;
            lockCount = 0;
            
            final VmThread t = this.enterQueue.dequeue();
            VmMagic.currentProcessor().enableReschedule(false, null);
            mutex.unlock();
            
            if (t != null) {
                t.wakeupFromEnterQueue(this);
            }
        }
        if (exMsg != null) {
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
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
     * @throws UninterruptiblePragma
     * @throws InterruptedException
     */
    public final void Wait(long timeout) throws InterruptedException {
        final VmThread current = VmMagic.currentProcessor().getCurrentThread();
        // final int id = current.getId();
        String exMsg = null;

        // Prepare allocations
        final VmThreadProxy proxy;
        final int waitState;
        if (timeout > 0) {
            waitState = VmThread.WAITING_NOTIFY_TIMEOUT;
            proxy = new VmThreadProxy(current, VmSystem
                    .currentKernelMillis()
                    + timeout);
        } else {
            waitState = VmThread.WAITING_NOTIFY;
            proxy = new VmThreadProxy(current);
        }

        // Claim the scheduler and the mutex
        VmMagic.currentProcessor().disableReschedule(true, mutex);
        if (owner != current) {
            VmMagic.currentProcessor().enableReschedule(true, mutex);
            exMsg = "Current thread is not the owner of this monitor";
        } else if (lockCount == 0) {
            VmMagic.currentProcessor().enableReschedule(true, mutex);
            exMsg = "Monitor is not locked";
        } else {
            final int oldLockCount = lockCount;
            current.goSleepByMonitor(this, object, proxy, waitState);
            notifyQueue.enqueue(proxy);
            // If there is a timeout, also add the current thread to the
            // sleep queue.
            if (timeout > 0) {
                VmMagic.currentProcessor().getScheduler().addToWakeupQueue(
                        proxy);
            }
            owner = null;
            lockCount = 0;
            
            final VmThread t = this.enterQueue.dequeue();
            if (t != null) {
                t.wakeupFromEnterQueue(this);
            }
            
            VmMagic.currentProcessor().suspend(true, mutex);
            // When we return here, we have been notified or there
            // was a timeout.

            if (!current.isRunning()) {
                Unsafe.debug("Back from wait, but state != running");
                Unsafe.debug("state=");
                Unsafe.debug(current.getThreadState());
                Unsafe.die("Wait");
            }

            // We got back here from the enter queue, but we still have to
            // reclaim the lock.
            enter();
            
            // Restore lock and lock counter
            mutex.lock();
            this.lockCount = oldLockCount;
            mutex.unlock();
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
     * @throws UninterruptiblePragma
     */
    public final void NotifyAll() {
        Notify(true);
    }

    /**
     * Notify the first or all waiting threads on this monitor.
     * 
     * @param all
     * @throws UninterruptiblePragma
     */
    final void Notify(boolean all) {
        String exMsg = null;
        
        mutex.lock();
        if (owner != VmMagic.currentProcessor().currentThread) {
            exMsg = "Current thread is not the owner of this monitor";
        } else if (lockCount == 0) {
            exMsg = "Monitor is not locked";
        } else {
            // Move thread(s) to enter queue
            VmMagic.currentProcessor().disableReschedule(false, null);
            VmThread t = notifyQueue.dequeue();
            while (t != null) {
                t.wakeupFromNotifyQueue(this);
                enterQueue.enqueue(t, false);
                if (!all) {
                    break;
                }
                t = notifyQueue.dequeue();
            }
            VmMagic.currentProcessor().enableReschedule(false, null);
        }
        mutex.unlock();
        
        if (exMsg != null) {
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
        }
    }

    /**
     * Is the given thread owner of this monitor?
     * 
     * @param thread
     * @return boolean
     * @throws UninterruptiblePragma
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
     * @throws UninterruptiblePragma
     */
    @Inline
    final boolean isLocked() {
        return (lockCount > 0);
    }

    /**
     * Move the given thread to the enter queue of this monitor.
     * 
     * @param thread
     */
    final void addToEnterQueue(VmThread thread) {
        mutex.lock();
        this.enterQueue.enqueue(thread, false);
        mutex.unlock();
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
}
