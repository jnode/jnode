/*
 * $Id$
 */
package org.jnode.debugger;

import java.util.Collection;
import java.util.Iterator;

import javax.naming.NameNotFoundException;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceListener;
import org.jnode.driver.DeviceManager;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.input.KeyboardAPI;
import org.jnode.driver.input.SystemTriggerAPI;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DebuggerPlugin extends Plugin implements DeviceListener {

    private final Debugger debugger = new Debugger();

    /**
     * @param descriptor
     */
    public DebuggerPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        try {
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            dm.addListener(this);
            final Collection devs = dm.getDevices();
            for (Iterator i = devs.iterator(); i.hasNext();) {
                addListeners((Device) i.next());
            }
        } catch (NameNotFoundException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
        try {
            final DeviceManager dm = DeviceUtils.getDeviceManager();
            dm.removeListener(this);
            final Collection devs = dm.getDevices();
            for (Iterator i = devs.iterator(); i.hasNext();) {
                removeListeners((Device) i.next());
            }
        } catch (NameNotFoundException ex) {
            throw new PluginException(ex);
        }
    }
    
    /**
     * @see org.jnode.driver.DeviceListener#deviceStarted(org.jnode.driver.Device)
     */
    public void deviceStarted(Device device) {
        addListeners(device);
    }
    
    /**
     * @see org.jnode.driver.DeviceListener#deviceStop(org.jnode.driver.Device)
     */
    public void deviceStop(Device device) {
        removeListeners(device);
    }
    
    private void addListeners(Device device) {
        if (device.implementsAPI(SystemTriggerAPI.class)) {
            try {
                final SystemTriggerAPI api = (SystemTriggerAPI) device
                        .getAPI(SystemTriggerAPI.class);
                api.addSystemTriggerListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }            
        }        
        if (device.implementsAPI(KeyboardAPI.class)) {
            try {
                final KeyboardAPI api = (KeyboardAPI) device
                        .getAPI(KeyboardAPI.class);
                api.addKeyboardListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }            
        }        
    }
    
    private void removeListeners(Device device) {
        if (device.implementsAPI(SystemTriggerAPI.class)) {
            try {
                final SystemTriggerAPI api = (SystemTriggerAPI) device
                        .getAPI(SystemTriggerAPI.class);
                api.removeSystemTriggerListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }            
        }        
        if (device.implementsAPI(KeyboardAPI.class)) {
            try {
                final KeyboardAPI api = (KeyboardAPI) device
                        .getAPI(KeyboardAPI.class);
                api.removeKeyboardListener(debugger);
            } catch (ApiNotFoundException ex) {
                // Ignore
            }            
        }        
    }
}
