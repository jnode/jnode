/*
 * $Id$
 */
package org.jnode.util;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class StopWatch {
    
    private long startTime;
    private long stopTime;
    
    public StopWatch() {
        start();
    }
    
    public final void start() {
        this.stopTime = 0;
        this.startTime = System.currentTimeMillis();
    }
    
    public final void stop() {
        this.stopTime = System.currentTimeMillis();        
    }
    
    public final long getElapsedTimed() {
        if (stopTime != 0) {
            return stopTime - startTime;
        } else {
            return System.currentTimeMillis() - startTime;
        }
    }
        
    public final boolean isElapsedLongerThen(long ms) {
        return getElapsedTimed() > ms;
    }
    
    public String toString() {
        return String.valueOf(getElapsedTimed()) + "ms";
    }
}
