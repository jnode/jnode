/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.debugger;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.vm.scheduler.VmThread;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ThreadListState extends DebugState {

    public static final int ST_ALL = 0;

    public static final int ST_RUNNING = 1;

    public static final int ST_WAITING = 2;

    private static final String[] STATE_NAMES = {"threads", "running-threads", "waiting-threads"};

    private final Map<String, Thread> threads;

    private Iterator<Thread> threadIterator;

    private int index;

    /**
     * @param parent
     */
    public ThreadListState(DebugState parent, int threadState) {
        super(STATE_NAMES[threadState], parent);
        threads = getAllThreads(threadState);
        reset();
    }

    public void print(PrintStream out) {
        for (Iterator<Thread> i = threads.values().iterator(); i.hasNext();) {
            final Thread t = (Thread) i.next();
            DebuggerUtils.showThreadHeading(out, t);
            if (i.hasNext()) {
                out.print(", ");
            }
        }
        out.println();
    }

    /**
     * Fill the given map with usage information for this state.
     *
     * @param map keychar - message
     */
    public void fillHelp(Map<String, String> map) {
        super.fillHelp(map);
        map.put("n", "Next thread");
        map.put("r", "Reset the list");
    }

    /**
     * @see org.jnode.debugger.DebugState#process(KeyboardEvent)
     */
    public DebugState process(KeyboardEvent event) {
        DebugState newState = this;

        switch (event.getKeyChar()) {
            case 'n':
                if (!threadIterator.hasNext()) {
                    reset();
                }
                if (threadIterator.hasNext()) {
                    final Thread t = (Thread) threadIterator.next();
                    newState = new ThreadState(this, t, index++);
                }
                break;
            case 'r':
                reset();
                break;
            default:
                return this;
        }
        event.consume();
        return newState;
    }

    private void reset() {
        threadIterator = threads.values().iterator();
        index = 1;
    }

    private Map<String, Thread> getAllThreads(int state) {
        final TreeMap<String, Thread> map = new TreeMap<String, Thread>();
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }
        getThreads(map, grp, state);
        return map;
    }

    private void getThreads(Map<String, Thread> map, ThreadGroup grp, int state) {
        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[max];
        grp.enumerate(ts);
        for (int i = 0; i < max; i++) {
            final Thread t = ts[i];
            if (t != null) {
                final VmThread vmThread = t.getVmThread();
                final boolean add;
                switch (state) {
                    case ST_ALL:
                        add = true;
                        break;
                    case ST_RUNNING:
                        add = vmThread.isRunning();
                        break;
                    case ST_WAITING:
                        add = vmThread.isWaiting();
                        break;
                    default:
                        add = false;
                }
                if (add) {
                    map.put(t.getName(), t);
                }
            }
        }

        final int gmax = grp.activeGroupCount() * 2;
        final ThreadGroup[] tgs = new ThreadGroup[gmax];
        grp.enumerate(tgs);
        for (int i = 0; i < gmax; i++) {
            final ThreadGroup tg = tgs[i];
            if (tg != null) {
                getThreads(map, tg, state);
            }
        }
    }

    public int size() {
        return threads.size();
    }
}
