/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.driver.input;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ByteChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;

import javax.naming.NameNotFoundException;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.naming.InitialNaming;
import org.jnode.util.SystemInputStream;

/**
 * @author epr
 */
public abstract class AbstractKeyboardDriver extends AbstractInputDriver<KeyboardEvent> implements
    KeyboardAPI, SystemTriggerAPI {

    ByteChannel channel;

    KeyboardInterpreter kbInterpreter;

    private InputStream kis;

    private final ArrayList<SystemTriggerListener> stListeners = new ArrayList<SystemTriggerListener>();

    public final void addKeyboardListener(KeyboardListener l) {
        super.addListener(l);
    }

    public final void removeKeyboardListener(KeyboardListener l) {
        super.removeListener(l);
    }

    /**
     * Claim to be the preferred listener.
     * The given listener must have been added by addKeyboardListener.
     * If there is a security manager, this method will call
     * <code>checkPermission(new DriverPermission("setPreferredListener"))</code>.
     *
     * @param l
     */
    public synchronized void setPreferredListener(KeyboardListener l) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SET_PREFERRED_LISTENER_PERMISSION);
        }
        super.setPreferredListener(l);
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
        try {
            KeyboardLayoutManager mgr = InitialNaming.lookup(KeyboardLayoutManager.NAME);
            this.kbInterpreter = mgr.createDefaultKeyboardInterpreter();
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find the keyboard layout manager", ex);
        } catch (KeyboardInterpreterException ex) {
            throw new DriverException();
        }

        final Device dev = getDevice();
        final String id = dev.getId();
        startDispatcher(id);
        dev.registerAPI(KeyboardAPI.class, this);
        dev.registerAPI(SystemTriggerAPI.class, this);

//        // If no inputstream has been defined, create and set one.
//        kis = null;
//        if (channel != null) {
//            kis = new KeyboardInputStream(this);
//        }
//        final InputStream systemIn = kis;
//        AccessController.doPrivileged(new PrivilegedAction() {
//            public Object run() {
//                SystemInputStream.getInstance().initialize(systemIn);
//                return null;
//            }                
//        });
    }

    /**
     * Gets the byte channel. This is implementation specific
     *
     * @return The byte channel
     */
    protected abstract ByteChannel getChannel();

    /**
     * Stop the keyboard device.
     */
    protected synchronized void stopDevice() throws DriverException {
        final Device dev = getDevice();
        dev.unregisterAPI(KeyboardAPI.class);
        dev.unregisterAPI(SystemTriggerAPI.class);
        if (System.in == kis) {
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    SystemInputStream.getInstance().releaseSystemIn();
                    return null;
                }
            });
        }
        stopDispatcher();

        try {
            channel.close();
        } catch (IOException ex) {
            System.err.println("Error closing Keyboard channel: "
                + ex.toString());
        }
    }

    /**
     * Send a given keyboard event to the given listener.
     *
     * @param l
     * @param event
     */
    @Override
    protected void sendEvent(SystemListener<KeyboardEvent> l, KeyboardEvent event) {
        KeyboardListener kl = (KeyboardListener) l;
        if (event.isKeyPressed()) {
            kl.keyPressed(event);
        } else if (event.isKeyReleased()) {
            kl.keyReleased(event);
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

    protected KeyboardEvent handleScancode(byte scancode) {
        KeyboardEvent event = kbInterpreter.interpretScancode(scancode & 0xff);
        if (event != null) {
            if ((event.getKeyCode() == KeyEvent.VK_PRINTSCREEN) &&
                event.isControlDown() &&
                event.isShiftDown() &&
                event.isKeyPressed()) {
                dispatchSystemTriggerEvent(event);
            }
        }
        return event;
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
     * @param kbInterpreter The kbInterpreter to set
     */
    public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {
        if (kbInterpreter == null) {
            throw new IllegalArgumentException(
                "kbInterpreter==null");
        }
        this.kbInterpreter = kbInterpreter;
    }
}
