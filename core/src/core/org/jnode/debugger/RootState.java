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
public class RootState extends DebugState {

    public RootState() {
        super("debugger", null);
    }

    /**
     * @see org.jnode.debugger.DebugState#print(java.io.PrintStream)
     */
    public void print(PrintStream out) {
        out.println("[Debugger]");
    }

    /**
     * Fill the given map with usage information for this state.
     *
     * @param map keychar - message
     */
    public void fillHelp(Map<String, String> map) {
        super.fillHelp(map);
        map.put("t", "List all threads");
        map.put("r", "List all running threads");
        map.put("w", "List all waiting threads");
    }

    /**
     * @see org.jnode.debugger.DebugState#process(KeyboardEvent)
     */
    public DebugState process(KeyboardEvent event) {
        final DebugState newState;
        switch (event.getKeyChar()) {
            case 't':
                newState = new ThreadListState(this, ThreadListState.ST_ALL);
                break;
            case 'r':
                newState = new ThreadListState(this, ThreadListState.ST_RUNNING);
                break;
            case 'w':
                newState = new ThreadListState(this, ThreadListState.ST_WAITING);
                break;
            default:
                return this;
        }
        event.consume();
        return newState;
    }
}
