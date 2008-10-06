/*
 * $Id: LinkMessageImpl.java 4595 2008-10-02 13:24:26Z crawley $
 */
package org.jnode.vm.isolate;

import javax.isolate.LinkMessage;

/**
 * Base class for all types of LinkMessage implementation classes.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class LinkMessageImpl extends LinkMessage {

    private boolean received = false;

    /**
     * Clone this message in the current isolate.
     *
     * @return
     */
    abstract LinkMessageImpl cloneMessage();

    /**
     * Block the current thread, until this message has its received flag set.
     */
    final void waitUntilReceived() throws InterruptedException {
        if (!received) {
            synchronized (this) {
                while (!received) {
                    wait();
                }
            }
        }
    }

    /**
     * Mark this message as received and notify all waiting threads.
     */
    final synchronized void notifyReceived() {
        this.received = true;
        notifyAll();
    }
}
