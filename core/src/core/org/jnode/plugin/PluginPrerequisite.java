/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 
package org.jnode.plugin;

/**
 * Descriptor of plugin prerequisites.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PluginPrerequisite {
	
	/**
	 * Gets the identifier of the plugin that is required
	 * @return The id
	 */
	public String getPluginId();

	/**
	 * Gets the version of the plugin that is required.
	 * If not specified, this version is equal to the version of the
	 * declaring plugin.
	 * @return The version
	 */
	public String getPluginVersion();

	/**
	 * Gets the descriptor of the plugin in which this element was declared.
	 * @return The descriptor
	 */
	public PluginDescriptor getDeclaringPluginDescriptor();
}
