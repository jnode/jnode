/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;
import org.jnode.util.Queue;
import org.jnode.util.QueueProcessor;
import org.jnode.util.QueueProcessorThread;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ConfigurationProcessor implements QueueProcessor {

    private final Queue queue = new Queue();

    private final Logger log = Logger.getLogger(getClass());

    private QueueProcessorThread thread;

    /**
     * Apply the configuration on the given device. This is an asynchronous
     * call.
     * 
     * @param device
     * @param config
     */
    public void apply(Device device, NetDeviceConfig config,
            boolean waitUntilReady) {
        final QueueEntry entry = new QueueEntry(device, config);
        queue.add(entry);
        if (waitUntilReady) {
            entry.waitUntilReady();
        }
    }

    public void start() {
        thread = new QueueProcessorThread("Net configuration processor", queue,
                this);
        thread.start();
    }

    public void stop() {
        thread.stopProcessor();
    }

    /**
     * @see org.jnode.util.QueueProcessor#process(java.lang.Object)
     */
    public void process(Object object) {
        final QueueEntry entry = (QueueEntry) object;
        entry.apply();
    }

    private class QueueEntry {
        private final Device device;

        private final NetDeviceConfig config;

        private boolean ready = false;

        /**
         * @param device
         * @param config
         */
        public QueueEntry(final Device device, final NetDeviceConfig config) {
            super();
            this.device = device;
            this.config = config;
        }

        public synchronized void apply() {
            try {
                config.apply(device);
            } catch (NetworkException ex) {
                log.error("Cannot configure device " + device.getId(), ex);
            } finally {
                ready = true;
                this.notifyAll();
            }
        }

        public synchronized void waitUntilReady() {
            while (!ready) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    // Ignore
                }
            }
        }
    }
}