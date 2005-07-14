/*
 * $Id$
 */
package org.jnode.vm.performance;

import java.util.Set;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class PerformanceCounters {

    /**
     * Gets all performance counter events that are available on the 
     * current processor.
     * 
     * @return
     */
    public abstract Set<PerformanceCounterEvent> getAvailableEvents();
    
    /**
     * Gets the performance counter event with the given id, if that is 
     * available on thecurrent processor.
     * 
     * @return The event, or null if not found.
     */
    public abstract PerformanceCounterEvent getAvailableEvent(String id);
    
    /**
     * Gets the performance counter event with the given id, if that is 
     * available on thecurrent processor.
     * 
     * @return The event, or null if not found.
     */
    public final PerformanceCounterEvent getAvailableEvent(PresetEvent preset) {
        return getAvailableEvent(preset.name());
    }
    
    /**
     * Gets the number of performance counters that are available on the
     * current processor.
     * @return
     */
    public abstract int getMaximumCounters();
    
    /**
     * Start performance counters for the given events on the current
     * thread.
     * The number of events must be equal to or less then the maximum number of 
     * available counters. See {@link #getMaximumCounters()}.
     * 
     * Any counters that have been started before are stopped.
     * 
     * @param events
     */
    public abstract void startCounters(PerformanceCounterEvent[] events)
    throws IllegalArgumentException;
    
    /**
     * Stop all counters that have been started with {@link #startCounters(PerformanceCounterEvent[])}.
     * If no counters have been started, this method returns directly.
     */
    public abstract void stopCounters();
    
    /**
     * Gets the counter values of the counters that have been started using
     * {@link #startCounters(PerformanceCounterEvent[])}.
     * 
     * @param counters
     *            The destination of the counter values. The array uses the same
     *            indexes as the events array in
     *            {@link #startCounters(PerformanceCounterEvent[])}.
     */
    public abstract void getCounterValues(long[] counters);
    
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Events: " + getAvailableEvents() + ", max counters: " + getMaximumCounters();
    }
}
