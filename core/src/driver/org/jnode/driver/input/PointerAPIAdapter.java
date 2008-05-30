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
     * Add a pointer listener
     *
     * @param l
     */
    public synchronized void addPointerListener(PointerListener l) {
        listeners.add(l);
    }

    /**
     * Remove a pointer listener
     *
     * @param l
     */
    public synchronized void removePointerListener(PointerListener l) {
        listeners.remove(l);
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
