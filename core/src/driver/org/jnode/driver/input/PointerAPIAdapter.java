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

package org.jnode.driver.input;

import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PointerAPIAdapter implements PointerAPI {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(PointerAPIAdapter.class);

    /**
     * All listeners
     */
    private final ArrayList<PointerListener> listeners = new ArrayList<PointerListener>();

    /**
     * Add a pointer listener.
     *
     * @param listener the pointer listener to be added
     */
    public synchronized void addPointerListener(PointerListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a pointer listener.
     *
     * @param listener the pointer listener to be removed
     */
    public synchronized void removePointerListener(PointerListener listener) {
        listeners.remove(listener);
    }

    public synchronized void setPreferredListener(PointerListener l) {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SET_PREFERRED_LISTENER_PERMISSION);
        }
        if (listeners.remove(l)) {
            listeners.add(0, l);
        }
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
     * @param event the event to be fired
     */
    public synchronized void fireEvent(PointerEvent event) {
        for (PointerListener l : listeners) {
            try {
                l.pointerStateChanged(event);
            } catch (Throwable ex) {
                log.error("Exception in PointerListener", ex);
            }
            if (event.isConsumed()) {
                break;
            }
        }
    }
}
