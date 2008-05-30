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
final class NetDeviceEventProcessor implements QueueProcessor<NetDeviceEvent> {

    /**
     * Event listeners
     */
    private ArrayList<NetDeviceListener> listeners = new ArrayList<NetDeviceListener>();

    /**
     * Cached array of listeners
     */
    private NetDeviceListener[] listenerCache;

    /**
     * Event queue
     */
    private final Queue<NetDeviceEvent> eventQueue = new Queue<NetDeviceEvent>();

    /**
     * The thread that will dispatch the events to the listeners
     */
    private QueueProcessorThread<NetDeviceEvent> thread;

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
            thread = new QueueProcessorThread<NetDeviceEvent>("NetDeviceEventProcessor",
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
    public void process(NetDeviceEvent event) throws Exception {
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
            for (int i = 0; i < max; i++) {
                listeners[i].processEvent(event);
            }
        }
    }
}
