/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
