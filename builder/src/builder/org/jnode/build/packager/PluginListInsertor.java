/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.build.packager;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.jnode.build.PluginList;
import org.jnode.plugin.PluginException;

/**
 * Task that insert the user plugins into a plugin list 
 * without actually modifying the plugin list files.
 * 
 * @author fabien
 *
 */
public class PluginListInsertor extends PackagerTask {
   
    /**
     * Main method of the task.
     * 
     * @param list
     * @throws MalformedURLException
     * @throws PluginException
     */
    public void insertInto(final PluginList list) throws MalformedURLException, PluginException {
        if (isEnabled()) {
            for (String pluginId : readPluginIds(list.getName())) {
                log("Adding user plugin " + pluginId, Project.MSG_INFO);            
                list.addPlugin(pluginId);
            }
        }
    }

    /**
     * Read the user plugins ids from the properties file
     * @param pluginListName
     * @return
     */
    private List<String> readPluginIds(String pluginListName) {
        List<String> pluginIds = new ArrayList<String>();
        
        final Properties properties = getProperties();
        final String targetName = properties.getProperty(PLUGIN_LIST_NAME, null); 
        if (targetName == null) {
            log("property " + PLUGIN_LIST_NAME + " not specified in " +
                    getPropertiesFile().getAbsolutePath(), Project.MSG_ERR);
        } else {
            if (targetName.equals(pluginListName)) {
                final String ids = properties.getProperty(USER_PLUGIN_IDS, null);                
                if ((ids == null) || ids.trim().isEmpty()) {
                    log("property " + USER_PLUGIN_IDS + " not specified in " +
                            getPropertiesFile().getAbsolutePath(), Project.MSG_ERR);
                } else {
                    for (String pluginId : ids.split(",")) {
                        pluginIds.add(pluginId);
                    }
                }
            }
        }
        
        return pluginIds;
    }
}
