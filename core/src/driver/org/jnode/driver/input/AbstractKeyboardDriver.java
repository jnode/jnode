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

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;

import org.jnode.driver.Device;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * @author epr
 */
public abstract class AbstractKeyboardDriver extends Driver implements
        KeyboardAPI, SystemTriggerAPI {

    final ByteBuffer buf = ByteBuffer.allocate(1);

    ByteChannel channel;

    KeyboardInterpreter kbInterpreter;

    private KeyboardDaemon daemon;

    private QueueProcessorThread<KeyboardEvent> eventQueueThread;

    private InputStream kis;

    private final ArrayList<KeyboardListener> kbListeners = new ArrayList<KeyboardListener>();

    private final ArrayList<SystemTriggerListener> stListeners = new ArrayList<SystemTriggerListener>();

    final Queue<KeyboardEvent> eventQueue = new Queue<KeyboardEvent>();

    /**
     * Add a keyboard listener
     * 
     * @param l
     */
    public synchronized void addKeyboardListener(KeyboardListener l) {
        kbListeners.add(l);
    }

	/**
	 * Claim to be the preferred listener.
	 * The given listener must have been added by addKeyboardListener.
	 * If there is a security manager, this method will call
	 * <code>checkPermission(new DriverPermission("setPreferredListener"))</code>.
	 * @param l
	 */
	public synchronized void setPreferredListener(KeyboardListener l) {
	    final SecurityManager sm = System.getSecurityManager();
	    if (sm != null) {
	        sm.checkPermission(SET_PREFERRED_LISTENER_PERMISSION);
	    }
	    if (kbListeners.remove(l)) {
	        kbListeners.add(0, l);
	    }
	}
	
    /**
     * Remove a keyboard listener
     * 
     * @param l
     */
    public synchronized void removeKeyboardListener(KeyboardListener l) {
        kbListeners.remove(l);
    }

    /**
     * Add a listener
     * 
     * @param l
     */
    public void addSystemTriggerListener(SystemTriggerListener l) {
        stListeners.add(l);
    }

    /**
     * Remove a listener
     * 
     * @param l
     */
    public void removeSystemTriggerListener(SystemTriggerListener l) {
        stListeners.remove(l);
    }

    /**
     * Start the keyboard device.
     */
    protected synchronized void startDevice() throws DriverException {
        this.channel = getChannel();
        this.kbInterpreter = createKeyboardInterpreter();

        final Device dev = getDevice();
        final String id = dev.getId();
        this.daemon = new KeyboardDaemon(id + "-daemon");
        daemon.start();
        this.eventQueueThread = new QueueProcessorThread<KeyboardEvent>(id + "-dispatcher",
                eventQueue, new KeyboardEventDispatcher());
        eventQueueThread.start();
        dev.registerAPI(KeyboardAPI.class, this);
        dev.registerAPI(SystemTriggerAPI.class, this);

        // If no inputstream has been defined, create and set one.
        if (System.in == null) {
            if (channel == null) {
                // even for keyboardless operation, we do need a System.in
                kis = new InputStream() {

                    public int read() {
                        return -1;
                    }
                };
            } else {
                kis = new KeyboardInputStream(this);
            }
            System.setIn(kis);
        }
    }

    /**
     * Gets the byte channel. This is implementation specific
     * 
     * @return The byte channel
     */
    protected abstract ByteChannel getChannel();

    /**
     * Create an interpreter for this keyboard device
     * 
     * @return The created interpreter
     */
    protected KeyboardInterpreter createKeyboardInterpreter() {
        return KeyboardInterpreterFactory.getDefaultKeyboardInterpreter();
    }

    /**
     * Stop the keyboard device.
     */
    protected synchronized void stopDevice() throws DriverException {
        final Device dev = getDevice();
        dev.unregisterAPI(KeyboardAPI.class);
        dev.unregisterAPI(SystemTriggerAPI.class);
        KeyboardDaemon daemon = this.daemon;
        if (System.in == kis) {
            System.setIn(null);
        }
        if (eventQueueThread != null) {
            eventQueueThread.stopProcessor();
        }
        this.eventQueueThread = null;
        if (daemon != null) {
            daemon.interrupt();
        }
        this.daemon = null;

        try {
            channel.close();
        } catch (IOException ex) {
            System.err.println("Error closing Keyboard channel: "
                    + ex.toString());
        }
    }

    /**
     * Dispatch a given keyboard event to all known listeners.
     * 
     * @param event
     */
    protected void dispatchEvent(KeyboardEvent event) {
        //Syslog.debug("Dispatching event to " + listeners.size());
        for (KeyboardListener l : kbListeners) {
            if (event.isKeyPressed()) {
                l.keyPressed(event);
            } else if (event.isKeyReleased()) {
                l.keyReleased(event);
            }
            if (event.isConsumed()) {
                break;
            }
        }
    }

    /**
     * Dispatch a given event to all known system trigger listeners.
     * 
     * @param event
     */
    protected void dispatchSystemTriggerEvent(KeyboardEvent event) {
        //Syslog.debug("Dispatching event to " + listeners.size());
        for (SystemTriggerListener l : stListeners) {
            l.systemTrigger(event);
        }
    }

    /**
     * KeyboardDaemon that translated scancodes to KeyboardEvents and
     * dispatches those events.
     * 
     * @author epr
     */
    class KeyboardDaemon extends Thread {

        public KeyboardDaemon(String name) {
            super(name);
        }

        public void run() {
            while ((channel != null) && channel.isOpen()) {
                try {
                    buf.rewind();
                    if (channel.read(buf) != 1) {
                        continue;
                    }
                    byte scancode = buf.get(0);
                    //Unsafe.debug("Interpreting " + (scancode & 0xff));
                    KeyboardEvent event = kbInterpreter
                            .interpretScancode(scancode & 0xff);
                    if (event != null) {
                        if ((event.getKeyCode() == KeyEvent.VK_PRINTSCREEN) && event.isKeyPressed() && event.isAltDown()) {
                            dispatchSystemTriggerEvent(event);
                        }
                        if (!event.isConsumed()) {
                            eventQueue.add(event);
                        }
                    }
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    class KeyboardEventDispatcher implements QueueProcessor<KeyboardEvent> {

        /**
         * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
         */
        public void process(KeyboardEvent event) throws Exception {
            dispatchEvent(event);
        }
    }

    /**
     * @return KeyboardInterpreter
     */
    public KeyboardInterpreter getKbInterpreter() {
        return kbInterpreter;
    }

    /**
     * Sets the kbInterpreter.
     * 
     * @param kbInterpreter
     *            The kbInterpreter to set
     */
    public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {
        if (kbInterpreter == null) { throw new IllegalArgumentException(
                "kbInterpreter==null"); }
        this.kbInterpreter = kbInterpreter;
    }
}
