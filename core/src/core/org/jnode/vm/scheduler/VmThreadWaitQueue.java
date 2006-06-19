package org.jnode.vm.scheduler;

import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * Queue for all threads that have a wakeup time set.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Uninterruptible
final class VmThreadWaitQueue extends VmThreadProxyQueue {

    private VmThreadProxy tail;

    /**
     * Initialize this instance.
     */
    public VmThreadWaitQueue(String name) {
        super(name);
    }

    /**
     * Add the given proxy to this thread.
     * 
     * @param p
     */
    protected void enqueue(VmThreadProxy p) {
        p.setQueue(this);
        if (head == null) {
            head = p;
        } else {
            tail.waitNext = p;
        }
        tail = p;
    }

    /**
     * Gets the first thread that is ready to be woken up. The returned thread
     * is removed from this queue.
     */
    @KernelSpace
    final VmThread dequeue() {
        while (head != null) {
            final VmThreadProxy p = head;
            p.resetQueue(this);
            head = head.waitNext;
            if (head == null) {
                tail = null;
            }
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
            p = p.waitNext;
        }
        return true;
    }       
}