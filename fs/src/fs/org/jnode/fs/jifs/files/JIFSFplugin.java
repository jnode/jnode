/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 
package org.jnode.fs.jifs.files;

import javax.naming.NameNotFoundException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;
import org.jnode.plugin.PluginManager;
import org.jnode.plugin.PluginPrerequisite;

/**
 * File, which contains information about the plugin with the same name.
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSFplugin extends JIFSFile {

    public JIFSFplugin() {
        return;
    }

    /**
     * Creates a file, which contains information about a Plugin.
     * 
     * @param name Name of this file <u>and</u> name of the plugin, whose
     *            information is stored in this file.
     * @param parent Parent FSEntry, in this case it is an instance of
     *            JIFSDplugins.
     */
    public JIFSFplugin(String name, FSDirectory parent) {
        super(name, parent);
        refresh();
    }

    public void refresh() {
        super.refresh();
        try {
            final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
            final PluginDescriptor descr = mgr.getRegistry().getPluginDescriptor(name);
            if (descr != null) {
                addStringln("Name:");
                addStringln("\t" + descr.getId());
                addStringln("Provider:");
                addStringln("\t" + descr.getProviderName());
                addStringln("State :");

                try {
                    if (descr.getPlugin().isActive()) {
                        addStringln("\tactive");
                    } else {
                        addStringln("\tinactive");
                    }
                } catch (PluginException PE) {
                    System.err.println(PE);
                }

                addStringln("Prerequisites:");
                PluginPrerequisite[] allPreqs = descr.getPrerequisites();
                PluginPrerequisite current;
                for (int i = 0; i < allPreqs.length; i++) {
                    current = allPreqs[i];
                    addStringln("\t" + current.getPluginId() + "\t\t" + current.getPluginVersion());
                }
            } else {
                isvalid = false;
            }
        } catch (NameNotFoundException N) {
            System.err.println(N);
        }
    }
}
