/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.work;

import java.util.LinkedList;
import java.util.List;

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

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(WorkPlugin.class);

    /**
     * Queue of work items
     */
    private final Queue<Work> queue = new Queue<Work>();

    /**
     * Queue processor threads
     */
    private final List<QueueProcessorThread<Work>> threads;

    /**
     * Number of workers started initially.
     */
    private final int workerCount = 2; //8

    /**
     * Counter used for worker thread names
     */
    private int counter = 1;

    /**
     * Number of work items added
     */
    private int workCounter;

    /**
     * Number of work items started
     */
    private int workStartCounter;

    /**
     * Number of work items ended
     */
    private int workEndCounter;

    /**
     * @param descriptor
     */
    public WorkPlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.threads = new LinkedList<QueueProcessorThread<Work>>();
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected final void startPlugin() throws PluginException {
        for (int i = 0; i < workerCount; i++) {
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
        for (QueueProcessorThread<Work> t : threads) {
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
        final QueueProcessorThread<Work> t = new QueueProcessorThread<Work>("worker-"
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

    private int getFreeProcessors() {
        return workStartCounter - workEndCounter;
    }

    class WorkProcessor implements QueueProcessor<Work> {

        /**
         * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
         */
        public void process(Work work) throws Exception {
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
