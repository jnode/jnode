/*
 * $Id$
 */
package org.jnode.awt;

import gnu.java.security.action.SetPropertyAction;

import java.awt.image.VMImageUtils;
import java.security.AccessController;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class AWTPlugin extends Plugin {

    private static final String TOOLKIT = "org.jnode.awt.peer.RawJNodeToolkit";
    //private static final String TOOLKIT = "org.jnode.awt.swingpeers.SwingToolkit";
    
	/**
	 * @param descriptor
	 */
	public AWTPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
	    AccessController.doPrivileged(new SetPropertyAction("awt.toolkit", TOOLKIT));
		VMImageUtils.setAPI(new VMImageAPIImpl(), this);
	}

	/**
	 * Stop this plugin
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		// GraphicsEnvironment.setLocalGraphicsEnvironment(null);
		VMImageUtils.resetAPI(this);
	}
}
