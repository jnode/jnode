/*
 * $Id$
 */
package org.jnode.shell.def;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.shell.ShellManager;
import org.jnode.shell.alias.AliasManager;
import org.jnode.shell.alias.def.DefaultAliasManager;

/**
 * Service used to create and bind the system alias manager.
 * @author epr
 */
public class ShellPlugin extends Plugin {

	/**
	 * Initialize a new instance
	 * @param descriptor
	 */
	public ShellPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * Start this plugin
	 */
	protected void startPlugin() throws PluginException {
		try {
			final AliasManager aliasMgr = new DefaultAliasManager(getDescriptor().getExtensionPoint("aliases"));
			final ShellManager shellMgr = new DefaultShellManager();
			InitialNaming.bind(AliasManager.NAME, aliasMgr);
			InitialNaming.bind(ShellManager.NAME, shellMgr);
		} catch (NamingException ex) {
			throw new PluginException("Cannot bind shell component", ex);
		}
	}

	/**
	 * Stop this plugin
	 */
	protected void stopPlugin() throws PluginException {
		InitialNaming.unbind(ShellManager.NAME);
		InitialNaming.unbind(AliasManager.NAME);
	}
}
