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
 
package org.jnode.plugin.model;

import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

/**
 * @author epr
 */
final class EmptyPlugin extends Plugin {

	/**
	 * Initialize a new instance
	 * @param descriptor
	 */
	public EmptyPlugin(PluginDescriptor descriptor) {
		super(descriptor);
	}

	/**
	 * @see org.jnode.plugin.Plugin#startPlugin()
	 */
	protected void startPlugin() throws PluginException {
		// Do nothing
	}

	/**
	 * @see org.jnode.plugin.Plugin#stopPlugin()
	 */
	protected void stopPlugin() throws PluginException {
		// Do nothing
	}
}
