/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import gnu.java.security.action.SetPropertyAction;

import java.security.AccessController;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class SwingPeersPlugin extends Plugin {

    private static final String TOOLKIT = "org.jnode.awt.swingpeers.SwingToolkit";
    
    /**
     * @param descriptor
     */
    public SwingPeersPlugin(PluginDescriptor descriptor) {
        super(descriptor);
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    protected void startPlugin() throws PluginException {
        AccessController.doPrivileged(new SetPropertyAction("awt.toolkit", TOOLKIT));
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    protected void stopPlugin() throws PluginException {
    }
}
