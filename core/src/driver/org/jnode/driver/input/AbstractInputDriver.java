/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

package org.jnode.driver.input;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;

import org.jnode.driver.Driver;
import org.jnode.system.event.SystemEvent;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

abstract public class AbstractInputDriver<E extends SystemEvent> extends Driver
{
    final private ArrayList<SystemListener> listeners = new ArrayList<SystemListener>();

    private QueueProcessorThread<E> eventQueueThread;
    final private Queue<E> eventQueue = new Queue<E>();
    private InputDaemon daemon;

    final protected void startDispatcher(String id)
    {
        this.daemon = new InputDaemon(id + "-daemon");
        daemon.start();
        
        this.eventQueueThread = new QueueProcessorThread<E>(id + "-dispatcher",
                eventQueue, new SystemEventDispatcher());
        eventQueueThread.start();        
    }
    
    final protected void stopDispatcher()
    {
        if (eventQueueThread != null) {
            eventQueueThread.stopProcessor();
        }
        this.eventQueueThread = null;
        
        InputDaemon daemon = this.daemon;
        this.daemon = null;
        if (daemon != null) {
            daemon.interrupt();
        }        
    }
    
    class SystemEventDispatcher implements QueueProcessor<E> {

        /**
         * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
         */
        public void process(E event) throws Exception {
            for (SystemListener l : listeners) {
                sendEvent(l, event);
                if (event.isConsumed()) {
                    break;
                }
            }            
        }
    }
    
    abstract protected void sendEvent(SystemListener<E> l, E e); 
        
    /**
     * Add a pointer listener
     * 
     * @param l
     */
    final public synchronized void addListener(SystemListener<E> l) {
        listeners.add(l);
    }

    /**
     * Remove a pointer listener
     * 
     * @param l
     */
    final public synchronized void removeListener(SystemListener<E> l) {
        listeners.remove(l);
    }

    /**
     * Claim to be the preferred listener.
     * The given listener must have been added by addKeyboardListener.
     * <b>This is not checked by a security manager</b>
     * @param l
     */
    final protected synchronized void setPreferredListener(SystemListener<E> l) {
        if (listeners.remove(l)) {
            listeners.add(0, l);
        }
    }
    
    
    abstract protected E handleScancode(byte b);
    
    /**
     * Gets the byte channel. This is implementation specific
     * 
     * @return The byte channel
     */
    protected abstract ByteChannel getChannel();    
    
    /**
     * InputDaemon that translates scancodes to SystemEvents and dispatches those events.
     */
    class InputDaemon extends Thread {
    
        public InputDaemon(String name) {
            super(name);
        }

        public void run() {
            processChannel();
        }
        
        /**
         * Read scancodes from the input channel and dispatch them as events.
         */
        final void processChannel() {
            final ByteBuffer buf = ByteBuffer.allocate(1);
            final ByteChannel channel = getChannel();
            while ((channel != null) && channel.isOpen()) {
                try {
                    buf.rewind();
                    if (channel.read(buf) != 1) {
                        continue;
                    }
                    final byte scancode = buf.get(0);
                    E event = handleScancode(scancode);
                    if((event != null) && !event.isConsumed())
                    {
                        eventQueue.add(event);
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }        
    }    
}
