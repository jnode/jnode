/*
 * $Id$
 */
package org.jnode.work;

import java.util.Iterator;
import java.util.LinkedList;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * Plugin that implements the {@link org.jnode.work.WorkManager}.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class WorkPlugin extends Plugin implements WorkManager {

    /** Queue of work items */
    private final Queue queue = new Queue();

    /** Queue processor threads */
    private final LinkedList threads;

    /** Counter used for worker thread names */
    private int counter = 1;
    
    /** Number of work items added */
    private int workCounter;
    
    /** Number of work items started */
    private int workStartCounter;
    
    /** Number of work items ended */
    private int workEndCounter;
    
    /**
     * @param descriptor
     */
    public WorkPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.threads = new LinkedList();
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected final void startPlugin() throws PluginException {
        final int n = 8;
        for (int i = 0; i < n; i++) {
            addWorker();
        }
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected final synchronized void stopPlugin() throws PluginException {
        InitialNaming.unbind(NAME);
        for (Iterator i = threads.iterator(); i.hasNext();) {
            QueueProcessorThread t = (QueueProcessorThread) i.next();
            t.stopProcessor();
        }
        threads.clear();
    }

    /**
     * @see org.jnode.plugin.Plugin#isStartFinished()
     */
    public final boolean isStartFinished() {
        return (workCounter == workEndCounter);
    }

    /**
     * @see org.jnode.work.WorkManager#add(org.jnode.work.Work)
     */
    public final synchronized void add(Work work) {
        workCounter++;
        queue.add(work);
    }

    /**
     * Gets the number of entries in the work queue.
     * 
     * @return
     */
    public final int queueSize() {
        return queue.size();
    }

    /**
     * Is the work queue empty.
     * 
     * @return
     */
    public final boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Add a worker thread.
     */
    private synchronized void addWorker() {
        final QueueProcessorThread t = new QueueProcessorThread("worker-"
                + counter, queue, new WorkProcessor());
        threads.add(t);
        counter++;
        t.start();
    }

    /**
     * Increment the workStartCounter.
     */
    final synchronized void incWorkStartCounter() {
        workStartCounter++;
    }

    /**
     * Increment the workEndCounter.
     */
    final synchronized void incWorkEndCounter() {
        workEndCounter++;
    }

    class WorkProcessor implements QueueProcessor {

        /**
         * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
         */
        public void process(Object object) throws Exception {
            final Work work = (Work) object;
            incWorkStartCounter();
            try {
                final Logger log = Logger.getLogger(work.getClass());
                if (log.isDebugEnabled()) {
                    log.debug("Start working on " + work);
                }
                work.execute();
                if (log.isDebugEnabled()) {
                    log.debug("Finished working on " + work);
                }
            } finally {
                incWorkEndCounter();
            }
        }
    }
}
