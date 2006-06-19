package org.jnode.vm.scheduler;

import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Uninterruptible;

/**
 * Queue used by the scheduler and monitors.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Uninterruptible
final class VmThreadScheduleQueue extends VmThreadQueueEntryQueue {

    /**
     * Initialize this instance.
     */
    public VmThreadScheduleQueue(String name) {
        super(name);
    }

    @KernelSpace
    final void enqueue(VmThread thread, boolean ignorePriority) {
        first = addToQueue(first, thread.queueEntry, ignorePriority);
    }

    @KernelSpace
    final VmThread dequeue() {
        final VmThreadQueueEntry t = this.first;
        if (t != null) {
            // We found a thread to dequeue
            this.first = t.next;
            t.next = null;
            t.resetQueue(this);
            return t.thread;
        }
        return null;
    }
}