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

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class KeyboardAPIAdapter implements KeyboardAPI {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(KeyboardAPIAdapter.class);

    /**
     * All listeners
     */
    private final ArrayList<KeyboardListener> listeners = new ArrayList<KeyboardListener>();
    /**
     * The interpreter
     */
    private KeyboardInterpreter interpreter = null/*new KeyboardInterpreter()*/;

    /**
     * @see org.jnode.driver.input.KeyboardAPI#addKeyboardListener(org.jnode.driver.input.KeyboardListener)
     */
    public synchronized void addKeyboardListener(KeyboardListener l) {
        listeners.add(l);
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
        if (listeners.remove(l)) {
            listeners.add(0, l);
        }
    }

    /**
     * @see org.jnode.driver.input.KeyboardAPI#getKbInterpreter()
     */
    public KeyboardInterpreter getKbInterpreter() {
        return interpreter;
    }

    /**
     * @see org.jnode.driver.input.KeyboardAPI#removeKeyboardListener(org.jnode.driver.input.KeyboardListener)
     */
    public synchronized void removeKeyboardListener(KeyboardListener l) {
        listeners.remove(l);
    }

    /**
     * @see org.jnode.driver.input.KeyboardAPI#setKbInterpreter(org.jnode.driver.input.AbstractKeyboardInterpreter)
     */
    public void setKbInterpreter(KeyboardInterpreter kbInterpreter) {
        if (kbInterpreter == null) {
            throw new IllegalArgumentException("kbInterpreter==null");
        }
        this.interpreter = kbInterpreter;
    }

    /**
     * Remove all listeners.
     */
    public synchronized void clear() {
        listeners.clear();
    }

    /**
     * Fire a given pointer event to all known listeners.
     *
     * @param event
     */
    public synchronized void fireEvent(KeyboardEvent event) {
        if (event != null) {
            for (KeyboardListener l : listeners) {
                try {
                    if (event.isKeyPressed()) {
                        l.keyPressed(event);
                    } else if (event.isKeyReleased()) {
                        l.keyReleased(event);
                    }
                } catch (Throwable ex) {
                    log.error("Exception in KeyboardListener", ex);
                }
                if (event.isConsumed()) {
                    break;
                }
            }
        }
    }
}
