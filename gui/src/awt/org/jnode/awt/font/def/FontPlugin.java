/*
 * $Id$
 */
package org.jnode.awt.font.def;

import org.jnode.plugin.ExtensionPoint;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FontPlugin extends Plugin {

	private final DefaultFontManager fMgr;

	/**
	 * @param descriptor
	 */
	public FontPlugin(PluginDescriptor descriptor) {
		super(descriptor);
		final ExtensionPoint ep = descriptor.getExtensionPoint("providers");
		fMgr = new DefaultFontManager(ep);
	}

	/**
	 * Start this plugin
	 * 
	 * @throws PluginException
	 */
	protected void startPlugin() throws PluginException {
		fMgr.start();
	}

	/**
	 * Stop this plugin
	 * 
	 * @throws PluginException
	 */
	protected void stopPlugin() throws PluginException {
		fMgr.stop();
	}
}
