/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
