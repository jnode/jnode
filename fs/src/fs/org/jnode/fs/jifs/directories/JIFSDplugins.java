/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.fs.jifs.directories;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.naming.NameNotFoundException;

import org.jnode.fs.FSDirectory;
import org.jnode.fs.FSEntry;
import org.jnode.fs.jifs.JIFSDirectory;
import org.jnode.fs.jifs.JIFSFile;
import org.jnode.fs.jifs.files.JIFSFplugin;
import org.jnode.naming.InitialNaming;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginManager;


/**
 * <description>
 * 
 * @author Andreas H\u00e4nel
 */
public class JIFSDplugins extends JIFSDirectory {

    public JIFSDplugins() throws IOException {
        super("plugins");
        refresh();
    }

    public JIFSDplugins(FSDirectory parent) throws IOException {
        this();
        setParent(parent);
    }

    public void refresh() {
        // this has to be improved
        // just add new ones and delete old ones
        // now it does delete all files and (re)create all ones
        super.clear();
        final ArrayList<String> rows = new ArrayList<String>();
        try {
            final PluginManager mgr = InitialNaming.lookup(PluginManager.NAME);
            for (PluginDescriptor descr : mgr.getRegistry()) {
                rows.add(descr.getId());
            }
            Collections.sort(rows);
            for (Object row : rows) {
                final JIFSFile F = new JIFSFplugin(row.toString(), this);
                addFSE(F);
            }
        } catch (NameNotFoundException N) {
            System.err.println(N);
        }
    }

    public FSEntry getEntry(String name) {
        return super.getEntry(name);
    }
}
