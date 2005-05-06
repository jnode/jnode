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
import java.net.URL;

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

    private PluginList pluginList;

    private File pluginListFile;

    private PluginList systemPluginList;

    private File systemPluginListFile;

    /**
     * Gets the pluginlist
     * 
     * @return The list
     * @throws PluginException
     * @throws MalformedURLException
     */
    protected PluginList getPluginList() throws PluginException,
            MalformedURLException {
        if (pluginList == null) {
            pluginList = new PluginList(pluginListFile, pluginDir, targetArch);
        }
        return pluginList;
    }

    /**
     * @return The plugin list file
     */
    protected File getPluginListFile() {
        return pluginListFile;
    }

    /**
     * Get a pluginregistry containing the loaded plugins
     * 
     * @return The registry
     * @throws PluginException
     * @throws MalformedURLException
     */
    protected PluginRegistry getPluginRegistry() throws PluginException,
            MalformedURLException {
        final PluginRegistry piRegistry;
        final URL[] plugins = getPluginList().getPluginList();
        final URL[] systemPlugins = getSystemPluginList().getPluginList();
        final URL[] all = new URL[systemPlugins.length + plugins.length];
        System.arraycopy(systemPlugins, 0, all, 0, systemPlugins.length);
        System.arraycopy(plugins, 0, all, systemPlugins.length, plugins.length);        
        piRegistry = new PluginRegistryModel(all);
        return piRegistry;
    }

    /**
     * Gets the pluginlist
     * 
     * @return The list
     * @throws PluginException
     * @throws MalformedURLException
     */
    protected PluginList getSystemPluginList() throws PluginException,
            MalformedURLException {
        if (systemPluginList == null) {
            systemPluginList = new PluginList(systemPluginListFile, pluginDir,
                    targetArch);
        }
        return systemPluginList;
    }

    /**
     * @return Returns the systemPluginListFile.
     */
    public final File getSystemPluginListFile() {
        return systemPluginListFile;
    }

    /**
     * @param file
     */
    public void setPluginList(File file) {
        pluginListFile = file;
    }

    /**
     * @param systemPluginListFile
     *            The systemPluginListFile to set.
     */
    public final void setSystemPluginList(File systemPluginListFile) {
        this.systemPluginListFile = systemPluginListFile;
    }

    /**
     * Ensure that all plugin prerequisites are met.
     * 
     * @param registry
     * @throws BuildException
     */
    protected void testPluginPrerequisites(PluginRegistry registry)
            throws BuildException {

        for (PluginDescriptor descr : registry) {
            if (!descr.isSystemPlugin()) {
                log(descr.getId() + " is not a system plugin", Project.MSG_WARN);
            }
            final PluginPrerequisite[] prereqs = descr.getPrerequisites();
            for (int j = 0; j < prereqs.length; j++) {
                if (registry.getPluginDescriptor(prereqs[j].getPluginId()) == null) {
                    throw new BuildException("Cannot find plugin "
                            + prereqs[j].getPluginId()
                            + ", which is required by " + descr.getId());
                }
            }
        }
    }

}
