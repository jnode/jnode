/*
 * $Id$
 */
package org.jnode.debugger;

import java.io.PrintStream;
import java.util.Map;

import org.jnode.driver.input.KeyboardEvent;
import org.jnode.vm.VmSystem;


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
        final int max = ((ThreadListState)getParent()).size();
        out.println("[Thread " + index + "/" + max + "]");
        showThread(out, thread);
    }

    /**
     * Fill the given map with usage information for this state.
     * 
     * @param map
     *            keychar - message
     */
    public void fillHelp(Map map) {
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
        final Object[] trace = VmSystem.getStackTrace(thread.getVmThread());
        final int traceLen = Math.min(trace.length, 10);
        for (int k = 0; k < traceLen; k++) {
            out.println(trace[ k]);
        }
        out.println();
    }
}
