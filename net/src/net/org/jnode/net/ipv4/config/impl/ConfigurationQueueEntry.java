/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import org.jnode.driver.Device;
import org.jnode.driver.net.NetworkException;

final class ConfigurationQueueEntry {
    private final Device device;

    private final NetDeviceConfig config;

    private boolean ready = false;

    /**
     * @param device
     * @param config
     */
    public ConfigurationQueueEntry(final Device device,
            final NetDeviceConfig config) {
        super();
        this.device = device;
        this.config = config;
    }

    public synchronized void apply() {
        try {
            config.apply(device);
        } catch (NetworkException ex) {
            ConfigurationProcessor.log.error("Cannot configure device "
                    + device.getId(), ex);
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