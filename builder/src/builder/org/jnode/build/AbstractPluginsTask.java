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
 
package org.jnode.build;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;

import org.apache.tools.ant.Project;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginPrerequisite;
import org.jnode.plugin.PluginRegistry;
import org.jnode.plugin.model.PluginRegistryModel;

/**
 * @author epr
 */
public abstract class AbstractPluginsTask extends AbstractPluginTask {

	private File pluginListFile;
	private PluginList pluginList;
	/**
	 * Gets the pluginlist
	 * @return The list
	 * @throws PluginException
	 * @throws MalformedURLException
	 */
	protected PluginList getPluginList() 
	throws PluginException, MalformedURLException {
		if (pluginList == null) {
			pluginList = new PluginList(pluginListFile, pluginDir, targetArch);
		}
		return pluginList;
	}

	/**
	 * Get a pluginregistry containing the loaded plugins
	 * @return The registry
	 * @throws PluginException
	 * @throws MalformedURLException
	 */
	protected PluginRegistry getPluginRegistry() 
	throws PluginException, MalformedURLException {
		final PluginRegistry piRegistry;
		piRegistry = new PluginRegistryModel(getPluginList().getPluginList());
		return piRegistry;
	}

	/**
	 * @param file
	 */
	public void setPluginList(File file) {
		pluginListFile = file;
	}

	/**
	 * @return The plugin list file
	 */
	protected File getPluginListFile() {
		return pluginListFile;
	}
	
	/**
	 * Ensure that all plugin prerequisites are met.
	 * @param registry
	 * @throws BuildException
	 */
	protected void testPluginPrerequisites(PluginRegistry registry) 
	throws BuildException {
		
		for (Iterator i = registry.getDescriptorIterator(); i.hasNext(); ) {
			final PluginDescriptor descr = (PluginDescriptor)i.next();
			if (!descr.isSystemPlugin()) {
				log(descr.getId() +" is not a system plugin", Project.MSG_WARN);
			}
			final PluginPrerequisite[] prereqs = descr.getPrerequisites();
			for (int j = 0; j < prereqs.length; j++) {
				if (registry.getPluginDescriptor(prereqs[j].getPluginId()) == null) {
					throw new BuildException("Cannot find plugin " + prereqs[j].getPluginId() + ", which is required by " + descr.getId());
				}
			}
		}
	}

}
