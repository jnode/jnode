/*
 * $Id$
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
     * @param map keychar - message
     */
    public void fillHelp(Map map) {
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
        	case 't': newState = new ThreadListState(this, ThreadListState.ST_ALL); break;
        	case 'r': newState = new ThreadListState(this, ThreadListState.ST_RUNNING); break;
        	case 'w': newState = new ThreadListState(this, ThreadListState.ST_WAITING); break;
        	default: return this;
        }
        event.consume();
        return newState;
    }
}
