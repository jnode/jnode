/*
 * $Id$
 */
package org.jnode.shell.help.def;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.shell.help.Help;

/**
 * Service used to create and bind the system help.
 * @author qades
 */
public class SystemHelpPlugin extends Plugin {

	/**
	 * Initialize a new instance
	 * @param descriptor
	 */
	public SystemHelpPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 */
	protected void startPlugin() throws PluginException {
		try {
			final Help help = new DefaultHelp();
			InitialNaming.bind(Help.NAME, help);
		} catch (NamingException ex) {
			throw new PluginException("Cannot bind system help", ex);
		}
	}

	/**
	 * Stop this plugin
	 */
	protected void stopPlugin() throws PluginException {
		InitialNaming.unbind(Help.NAME);
	}
}
