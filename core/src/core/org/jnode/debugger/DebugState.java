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
public abstract class DebugState {
    
    private final DebugState parent;
    private final String name;
    
    /**
     * Initialize this instance.
     * @param parent
     */
    public DebugState(String name, DebugState parent) {
        this.name = name;
        this.parent = parent;
    }
    
    /**
     * Print this state.
     * @param out
     */
    public abstract void print(PrintStream out);
    
    /**
     * Fill the given map with usage information for this state.
     * @param map keychar - message
     */
    public void fillHelp(Map map) {
        if (parent != null) {
            parent.fillHelp(map);
        }
    }
    
    /**
     * Process this given event and return the new state.
     * Call event.consume() when the event has been processed.
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
