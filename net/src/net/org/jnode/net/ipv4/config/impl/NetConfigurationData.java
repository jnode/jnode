/*
 * $Id$
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
