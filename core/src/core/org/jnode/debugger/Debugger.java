/*
 * $Id$
 */
package org.jnode.debugger;

import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.KeyboardEvent;
import org.jnode.driver.input.KeyboardListener;
import org.jnode.driver.input.SystemTriggerListener;
import org.jnode.system.event.SystemEvent;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmThread;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Debugger implements SystemTriggerListener, KeyboardListener,
        PrivilegedAction {

    private boolean enabled;

    private TreeMap threads;

    private KeyboardEvent event;

    private Iterator threadIterator;

    private int index;

    private static final int ST_ALL = 1;

    private static final int ST_RUNNING = 2;

    private static final int ST_WAITING = 3;

    /**
     * @see org.jnode.driver.input.SystemTriggerListener#systemTrigger(org.jnode.system.event.SystemEvent)
     */
    public void systemTrigger(SystemEvent event) {
        event.consume();
        if (!enabled) {
            reset();
            setPreferredListener();
            VmSystem.getOut().println("[Debugger ('h' for help)]");
        }
        enabled = !enabled;
    }

    /**
     * @see org.jnode.driver.input.KeyboardListener#keyPressed(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyPressed(KeyboardEvent event) {
        if (enabled) {
            this.event = event;
            AccessController.doPrivileged(this);
            event.consume();
        }
    }

    /**
     * @see org.jnode.driver.input.KeyboardListener#keyReleased(org.jnode.driver.input.KeyboardEvent)
     */
    public void keyReleased(KeyboardEvent event) {
        if (enabled) {
            // Do nothing
            event.consume();
        }
    }

    /**
     * Perform the actual debugger actions.
     * 
     * @see java.security.PrivilegedAction#run()
     */
    public Object run() {
        final PrintStream out = VmSystem.getOut();
        switch (event.getKeyChar()) {
        case 't':
            showThreadList(out, ST_ALL, "[All threads]");
            break;
        case 'r':
            showThreadList(out, ST_RUNNING, "[Running threads]");
            break;
        case 'w':
            showThreadList(out, ST_WAITING, "[Waiting threads]");
            break;
        case 'n':
            showNextThread(out);
            break;
        case 'h':
        case '?':
        case 'H':
            help(out);
        }
        return null;
    }

    private void showThreadList(PrintStream out, int state, String title) {
        out.println(title);
        threads = getAllThreads(state);
        for (Iterator i = threads.values().iterator(); i.hasNext();) {
            final Thread t = (Thread) i.next();
            showThreadHeading(out, t);
            out.print(", ");
        }
        out.println();
        threadIterator = threads.values().iterator();
        index = 1;
    }

    private void showNextThread(PrintStream out) {
        if ((threadIterator != null) && threadIterator.hasNext()) {
            out.println("[Thread " + index + "/" + threads.size() + "]");
            showThread(out, (Thread) threadIterator.next());
            index++;
        } else {
            out.println("No more threads in list");
            threads = null;
            threadIterator = null;
        }
    }

    private void showThreadHeading(PrintStream out, Thread thread) {
        out.print(thread.getName() + ", " + thread.getPriority() + ", "
                + thread.getVmThread().getThreadStateName());
    }

    private void showThread(PrintStream out, Thread thread) {
        showThreadHeading(out, thread);
        out.println();
        final Object[] trace = VmSystem.getStackTrace(thread.getVmThread());
        final int traceLen = Math.min(trace.length, 10);
        for (int k = 0; k < traceLen; k++) {
            out.println(trace[ k]);
        }
        out.println();
    }

    private TreeMap getAllThreads(int state) {
        final TreeMap map = new TreeMap();
        ThreadGroup grp = Thread.currentThread().getThreadGroup();
        while (grp.getParent() != null) {
            grp = grp.getParent();
        }
        getThreads(map, grp, state);
        return map;
    }

    private void getThreads(TreeMap map, ThreadGroup grp, int state) {
        final int max = grp.activeCount() * 2;
        final Thread[] ts = new Thread[ max];
        grp.enumerate(ts);
        for (int i = 0; i < max; i++) {
            final Thread t = ts[ i];
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
        final ThreadGroup[] tgs = new ThreadGroup[ gmax];
        grp.enumerate(tgs);
        for (int i = 0; i < gmax; i++) {
            final ThreadGroup tg = tgs[ i];
            if (tg != null) {
                getThreads(map, tg, state);
            }
        }
    }

    private void help(PrintStream out) {
        out.println("Usage:");
        out.println("t         - List all threads");
        out.println("r         - List all running threads");
        out.println("w         - List all waiting threads");
        out.println("n         - Next thread in list");
        out.println("Alt-SysRq - Quit debugger");
    }

    private void setPreferredListener() {
        final KeyboardListener l = this;
        AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                try {
                    final Collection devs = DeviceUtils
                            .getDevicesByAPI(KeyboardAPI.class);
                    for (Iterator i = devs.iterator(); i.hasNext();) {
                        final Device dev = (Device) i.next();
                        final KeyboardAPI api = (KeyboardAPI) dev
                                .getAPI(KeyboardAPI.class);
                        api.setPreferredListener(l);
                    }
                } catch (ApiNotFoundException ex) {
                    // Ignore
                }
                return null;
            }
        });
    }

    private void reset() {
        threads = null;
        threadIterator = null;
    }
}
