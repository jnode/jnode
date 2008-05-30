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
import org.jnode.vm.scheduler.VmThread;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ThreadState extends DebugState {

    private final Thread thread;
    private final int index;

    /**
     * @param parent
     */
    public ThreadState(ThreadListState parent, Thread thread, int index) {
        super("thread", parent);
        this.thread = thread;
        this.index = index;
    }

    public void print(PrintStream out) {
        final int max = ((ThreadListState) getParent()).size();
        out.println("[Thread " + index + "/" + max + "]");
        showThread(out, thread);
    }

    /**
     * Fill the given map with usage information for this state.
     *
     * @param map keychar - message
     */
    public void fillHelp(Map<String, String> map) {
        super.fillHelp(map);
        map.put("i", "Interrupt this thread");
    }

    /**
     * @see org.jnode.debugger.DebugState#process(KeyboardEvent)
     */
    public DebugState process(KeyboardEvent event) {
        DebugState newState = this;

        switch (event.getKeyChar()) {
            case 'i':
                thread.interrupt();
                break;
            default:
                return this;
        }
        event.consume();
        return newState;
    }

    private void showThread(PrintStream out, Thread thread) {
        DebuggerUtils.showThreadHeading(out, thread);
        out.println();
        final Object[] trace = VmThread.getStackTrace(thread.getVmThread());
        final int traceLen = Math.min(trace.length, 10);
        for (int k = 0; k < traceLen; k++) {
            out.println(trace[k]);
        }
        out.println();
    }
}
