/*
 * $Id: $
 */
package org.jnode.vm.scheduler;

import org.jnode.vm.Unsafe;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

@Uninterruptible
final class VmThreadProxy extends ProcessorLock {

    private VmThread thread;

    /** Next pointer used on the wait queue */
    VmThreadProxy waitNext;
    /** Queue this proxy is on */
    private VmThreadWaitQueue waitQueue;

    /** Next pointer used on the wakeup queue */
    VmThreadProxy wakeupNext;
    /** Queue this proxy is on */
    private VmThreadWakeupQueue wakeupQueue;

    /** Wakeup cycle */
    final long wakeupTime;

    /**
     * Create a new instance.
     * 
     * @param thread
     */
    public VmThreadProxy(VmThread thread) {
        this(thread, 0);
    }

    /**
     * Create a new instance.
     * 
     * @param thread
     */
    public VmThreadProxy(VmThread thread, long wakeupTime) {
        this.thread = thread;
        this.wakeupTime = wakeupTime;
    }

    /**
     * Remove the thread from this proxy.
     * 
     * @return Null if the thread has already been removed.
     */
    @KernelSpace
    public final VmThread unproxy() {
        if (thread == null) {
            return null;
        }
        lock();
        final VmThread t = this.thread;
        this.thread = null;
        if (t != null) {
            t.proxy = null;
        }
        unlock();
        return t;
    }
    
    /**
     * Visit the given visitor with my thread (if any).
     * @param visitor
     * @return
     */
    final boolean visit(VmThreadVisitor visitor) {
        final VmThread t = this.thread;
        if (t != null) {
            return visitor.visit(t);
        } else {
            return true;
        }
    }
    
    /**
     * Set the queue this entry is used on.
     * @param queue
     */
    @Inline
    final void setQueue(VmThreadWaitQueue queue) {
        if (this.waitQueue != null) {
            Unsafe.debug("Entry is already on queue ");
            Unsafe.debug(this.waitQueue.name);
            Unsafe.debug(" when asked to add to ");
            Unsafe.debug(queue.name);
            Unsafe.debug(" thread state ");
            Unsafe.debug(thread.getThreadStateName());
            Unsafe.die("VmThreadProxy.setQueue(WaitQueue)");
        }
        this.waitQueue = queue;        
    }

    /**
     * Set the queue this entry is used on.
     * @param queue
     */
    @Inline
    @KernelSpace
    final void resetQueue(VmThreadWaitQueue queue) {
        if (this.waitQueue != queue) {
            Unsafe.debug("Entry not on queue ");
            Unsafe.debug(queue.name);
            Unsafe.debug(" but ");
            Unsafe.debug((this.waitQueue != null) ? this.waitQueue.name : "null");
            Unsafe.die("VmThreadProxy.setQueue(WaitQueue)");
        }
        this.waitQueue = null;        
    }
    
    /**
     * Set the queue this entry is used on.
     * @param queue
     */
    @Inline
    final void setQueue(VmThreadWakeupQueue queue) {
        if (this.wakeupQueue != null) {
            Unsafe.debug("Entry is already on queue ");
            Unsafe.debug(this.wakeupQueue.name);
            Unsafe.debug(" when asked to add to ");
            Unsafe.debug(queue.name);
            Unsafe.debug(" thread state ");
            Unsafe.debug(thread.getThreadStateName());
            Unsafe.die("VmThreadProxy.setQueue(WakeupQueue)");
        }
        this.wakeupQueue = queue;        
    }

    /**
     * Set the queue this entry is used on.
     * @param queue
     */
    @Inline
    @KernelSpace
    final void resetQueue(VmThreadWakeupQueue queue) {
        if (this.wakeupQueue != queue) {
            Unsafe.debug("Entry not on queue ");
            Unsafe.debug(queue.name);
            Unsafe.debug(" but ");
            Unsafe.debug((this.wakeupQueue != null) ? this.wakeupQueue.name : "null");
            Unsafe.die("VmThreadProxy.setQueue(WakeupQueue)");
        }
        this.wakeupQueue = null;        
    }
}
