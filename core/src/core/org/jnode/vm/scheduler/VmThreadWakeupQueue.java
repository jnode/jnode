package org.jnode.vm.scheduler;

import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * Queue for all threads that have a wakeup time set.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Uninterruptible
final class VmThreadWakeupQueue extends VmThreadProxyQueue {

    /**
     * Initialize this instance.
     */
    public VmThreadWakeupQueue(String name) {
        super(name);
    }

    /**
     * Is a proxy ready to be dequeued.
     * @return
     */
    @KernelSpace
    final boolean isReady() {
        final VmThreadProxy p = head;
        return ((p != null) && (p.wakeupTime <= VmSystem.currentKernelMillis()));
    }
    
    /**
     * Add the given proxy to this thread.
     * 
     * @param p
     */
    protected void enqueue(VmThreadProxy p) {
        VmThreadProxy previous = null;
        VmThreadProxy current = head;
        p.setQueue(this);
        while (current != null && current.wakeupTime <= p.wakeupTime) {
            // skip proxies with earlier wakeupTimes
            previous = current;
            current = current.wakeupNext;
        }
        // insert p
        if (previous == null) {
            head = p;
        } else {
            previous.wakeupNext = p;
        }
        p.wakeupNext = current;
    }

    /**
     * Gets the first thread that is ready to be woken up. The returned thread
     * is removed from this queue.
     */
    @KernelSpace
    final VmThread dequeue() {
        final long currentTime = VmSystem.currentKernelMillis();
        while (head != null) {
            final VmThreadProxy p = head;
            if ((currentTime < p.wakeupTime)) {
                return null;
            }
            p.resetQueue(this);
            head = head.wakeupNext;
            p.wakeupNext = null;
            final VmThread t = p.unproxy();
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    /**
     * @see org.jnode.vm.scheduler.VmThreadQueue#visit(org.jnode.vm.scheduler.VmThreadVisitor)
     */
    @Override
    public boolean visit(VmThreadVisitor visitor) {
        VmThreadProxy p = head;
        while (p != null) {
            if (!p.visit(visitor)) {
                return false;
            }
            p = p.wakeupNext;
        }
        return true;
    }       
}