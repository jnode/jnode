/*
 * $Id$
 */
package org.jnode.net.ipv4.config.impl;

import javax.naming.NamingException;

import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.naming.InitialNaming;
import org.jnode.net.ipv4.config.IPv4ConfigurationService;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.work.Work;
import org.jnode.work.WorkUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class IPv4ConfigurationPlugin extends Plugin {

    private NetConfigurationData config;
    private ConfigurationServiceImpl service;
    private NetDeviceMonitor monitor;
    private DeviceManager devMan;
    private ConfigurationProcessor processor;
    
    /**
     * @param descriptor
     */
    public IPv4ConfigurationPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }    
    
    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        this.processor = new ConfigurationProcessor();
        this.config = (NetConfigurationData)getDescriptor().getConfiguration();
        this.service = new ConfigurationServiceImpl(processor, config);
        this.monitor = new NetDeviceMonitor(processor, config);
        try {
            InitialNaming.bind(IPv4ConfigurationService.NAME, service);
            devMan = DeviceUtils.getDeviceManager();
            devMan.addListener(monitor);
            WorkUtils.add(new Work("Network device configuration") {
                public void execute() {
                    monitor.configureDevices(devMan);
                }
                });
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
        processor.start();
    }
    
    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        processor.stop();
        devMan.removeListener(monitor);
        InitialNaming.unbind(IPv4ConfigurationService.NAME);
        this.monitor = null;
        this.service = null;
        this.devMan = null;
        this.processor = null;
    }    
}
