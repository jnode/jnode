/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;

/**
 *
 */
class PluginTreePane extends JPanel {
    
    private static final long serialVersionUID = 1L;
    JTree tree;

    PluginTreePane(final PluginListModel model) {
        tree = new JTree(new RootNode(model)) {
            private static final long serialVersionUID = 1L;
            @Override
            public String getToolTipText(MouseEvent event) {
                TreePath path = tree.getClosestPathForLocation(event.getX(), event.getY());
                Object o = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                if (o instanceof Plugin) {
                    return model.getTooltipText((Plugin) o);
                }
                return null;
            }
        };
        tree.setToolTipText("");
        tree.setRootVisible(false);
        setLayout(new BorderLayout());
        add(new JScrollPane(tree), BorderLayout.CENTER);
    }

}
