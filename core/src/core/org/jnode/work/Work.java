/*
 * $Id$
 */
package org.jnode.work;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class Work {
    
    private final String name;

    /**
     * Initialize this instance.
     * @param name
     */
    public Work(String name) {
        this.name = name;
    }
    
    /**
     * Execute this bit of work.
     */
    public abstract void execute();

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
}
