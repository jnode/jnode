/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 
package org.jnode.pluginlist;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
class ProjectNode extends DefaultMutableTreeNode implements Syncable {
    private static final long serialVersionUID = 1L;
    public ProjectNode(Project project) {
        super(project);
        for (Plugin p : project.plugins())
            add(new PluginNode(p));
    }

    public void sync() {
        Project project = (Project) getUserObject();
        if (project.size() != getChildCount()) {
            removeAllChildren();
            for (Plugin p : project.plugins())
                add(new PluginNode(p));
        }

        for (int i = 0; i < getChildCount(); i++)
            ((PluginNode) getChildAt(i)).sync();
    }
}
