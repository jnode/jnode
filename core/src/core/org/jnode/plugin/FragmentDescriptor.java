/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface FragmentDescriptor extends PluginDescriptor {

    /**
     * Gets the identifier of the plugin to which this fragment belongs.
     * @return
     */
    public String getPluginId();

    /**
     * Gets the version of the plugin to which this fragment belongs.
     * @return
     */
    public String getPluginVersion();
}
