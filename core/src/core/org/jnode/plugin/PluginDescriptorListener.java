/*
 * $Id$
 */
package org.jnode.plugin;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginDescriptorListener {

    /**
     * Notify that the given plugin has been started.
     * @param descriptor
     */
    public void pluginStarted(PluginDescriptor descriptor);

    /**
     * Notify that the given plugin is going to stop.
     * @param descriptor
     */
    public void pluginStop(PluginDescriptor descriptor);

}
