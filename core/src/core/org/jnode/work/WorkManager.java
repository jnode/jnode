/*
 * $Id$
 */
package org.jnode.work;



/**
 * Manager of small asynchronous bits of work.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface WorkManager {

    /**
     * Name used to bind this manager in the InitialNaming namespace.
     */
    public static final Class NAME = WorkManager.class;
    
    /**
     * Add a bit of work to the qork queue.
     * @param work
     */
    public void add(Work work);
    
    /**
     * Gets the number of entries in the work queue.
     * @return
     */
    public int queueSize();
    
    /**
     * Is the work queue empty.
     * @return
     */
    public boolean isEmpty();
}
