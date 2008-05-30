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

package org.jnode.debugger;

import java.io.PrintStream;
import java.util.Map;
import org.jnode.driver.input.KeyboardEvent;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class DebugState {

    private final DebugState parent;
    private final String name;

    /**
     * Initialize this instance.
     *
     * @param parent
     */
    public DebugState(String name, DebugState parent) {
        this.name = name;
        this.parent = parent;
    }

    /**
     * Print this state.
     *
     * @param out
     */
    public abstract void print(PrintStream out);

    /**
     * Fill the given map with usage information for this state.
     *
     * @param map keychar - message
     */
    public void fillHelp(Map<String, String> map) {
        if (parent != null) {
            parent.fillHelp(map);
        }
    }

    /**
     * Process this given event and return the new state.
     * Call event.consume() when the event has been processed.
     *
     * @param event
     * @return The new state
     */
    public abstract DebugState process(KeyboardEvent event);

    /**
     * @return Returns the parent.
     */
    public final DebugState getParent() {
        return this.parent;
    }

    public final String getStateTrace() {
        if (parent != null) {
            return parent.getStateTrace() + " - " + name;
        } else {
            return name;
        }
    }
}
