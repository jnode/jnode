package org.jnode.vm.scheduler;

import org.jnode.vm.annotation.Uninterruptible;

/**
 * Queue for all threads.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@Uninterruptible
final class VmAllThreadsQueue extends VmThreadQueueEntryQueue {

    /**
     * Initialize this instance.
     */
    public VmAllThreadsQueue(String name) {
        super(name);
    }

    final void add(VmThread thread) {
        first = addToQueue(first, thread.allThreadsEntry, true);
    }

    final void remove(VmThread thread) {
        first = removeFromQueue(first, thread.allThreadsEntry);
    }
}