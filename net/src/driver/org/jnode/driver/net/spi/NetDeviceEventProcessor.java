/*
 * $Id$
 */
package org.jnode.driver.net.spi;

import java.util.ArrayList;

import org.jnode.driver.net.NetDeviceEvent;
import org.jnode.driver.net.NetDeviceListener;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class NetDeviceEventProcessor implements QueueProcessor {

    /** Event listeners */
    private ArrayList listeners = new ArrayList();

    /** Cached array of listeners */
    private NetDeviceListener[] listenerCache;

    /** Event queue */
    private final Queue eventQueue = new Queue();

    /** The thread that will dispatch the events to the listeners */
    private QueueProcessorThread thread;

    /**
     * Does this processor have any listeners.
     * 
     * @return
     */
    final synchronized boolean isEmpty() {
        return listeners.isEmpty();
    }

    /**
     * @see org.jnode.driver.net.NetDeviceAPI#addEventListener(org.jnode.driver.net.NetDeviceListener)
     */
    final synchronized void addEventListener(NetDeviceListener listener) {
        listeners.add(listener);
        this.listenerCache = null;
        if (thread == null) {
            thread = new QueueProcessorThread("NetDeviceEventProcessor",
                    eventQueue, this);
            thread.start();
        }
    }

    /**
     * @see org.jnode.driver.net.NetDeviceAPI#removeEventListener(org.jnode.driver.net.NetDeviceListener)
     */
    final synchronized void removeEventListener(NetDeviceListener listener) {
        listeners.remove(listener);
        this.listenerCache = null;
        if (listeners.isEmpty() && (thread != null)) {
            thread.stopProcessor();
            thread = null;
        }
    }

    /**
     * Post an event that will be fired (on another thread) to the listeners.
     * 
     * @param event
     */
    final void postEvent(NetDeviceEvent event) {
        if (thread != null) {
            eventQueue.add(event);
        }
    }

    /**
     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
     */
    public void process(Object object) throws Exception {
        NetDeviceListener[] listeners = this.listenerCache;
        if (listeners == null) {
            synchronized (this) {
                final int size = this.listeners.size();
                if (size > 0) {
                    listeners = (NetDeviceListener[]) this.listeners
                            .toArray(new NetDeviceListener[size]);
                    this.listenerCache = listeners;
                }
            }
        }
        if (listeners != null) {
            final int max = listeners.length;
            final NetDeviceEvent event = (NetDeviceEvent) object;
            for (int i = 0; i < max; i++) {
                listeners[i].processEvent(event);
            }
        }
    }
}
