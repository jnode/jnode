/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

import java.util.HashMap;

import org.jnode.driver.Device;
import org.jnode.plugin.PluginConfiguration;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NetConfigurationData extends PluginConfiguration {
    
    /** Map bewteen device id and NetDeviceConfig */
    private final HashMap deviceConfigs = new HashMap();
    
    /** Is no configuration is set, use DHCP automatically? */
    private boolean autoConfigureUsingDhcp = false;

    /**
     * Set the configuration data for the given device.
     * @param device
     * @param config
     */
    public void setConfiguration(Device device, NetDeviceConfig config) {
        deviceConfigs.put(device.getId(), config);
    }
    
    /**
     * Gets the configuration data for the device, or null if not found.
     * @return
     */
    public NetDeviceConfig getConfiguration(Device device) {
        final NetDeviceConfig cfg = (NetDeviceConfig)deviceConfigs.get(device.getId());
        if ((cfg == null) && autoConfigureUsingDhcp) {
            return new NetDhcpConfig();
        } else {
            return cfg;
        }
    }    
    
    /**
     * @return Returns the autoConfigureUsingDhcp.
     */
    public boolean isAutoConfigureUsingDhcp() {
        return this.autoConfigureUsingDhcp;
    }
    
    /**
     * @param autoConfigureUsingDhcp The autoConfigureUsingDhcp to set.
     */
    public void setAutoConfigureUsingDhcp(boolean autoConfigureUsingDhcp) {
        this.autoConfigureUsingDhcp = autoConfigureUsingDhcp;
    }
}
