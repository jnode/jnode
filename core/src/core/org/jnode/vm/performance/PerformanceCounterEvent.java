/*
 * $Id$
 */
package org.jnode.vm.performance;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PerformanceCounterEvent implements
        Comparable<PerformanceCounterEvent> {

    /** Identifier of this event */
    private final String id;

    /**
     * Initialize this instance.
     * 
     * @param id
     */
    protected PerformanceCounterEvent(String id) {
        this.id = id;
    }

    /**
     * Gets the language independent identifier of this event.
     * 
     * @return
     */
    public final String getId() {
        return id;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PerformanceCounterEvent) {
            return (compareTo((PerformanceCounterEvent)obj) == 0);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return id;
    }

    /**
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(PerformanceCounterEvent o) {
        return id.compareTo(o.id);
    }
}
