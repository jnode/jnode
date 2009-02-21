/*
 * Copyright 2004-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package sun.tools.jconsole.inspector;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import sun.tools.jconsole.Resources;
import sun.tools.jconsole.inspector.XNodeInfo.Type;

@SuppressWarnings("serial")
public class XTreeRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(
                tree, value, selected, expanded, leaf, row, hasFocus);
        Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
        if (userObject instanceof XNodeInfo) {
            XNodeInfo node = (XNodeInfo) userObject;
            setToolTipText(node.getToolTipText());
            switch (node.getType()) {
                case MBEAN:
                    XMBean xmbean = (XMBean) node.getData();
                    setIcon((ImageIcon) xmbean.getIcon());
                    break;
                case NONMBEAN:
                    break;
                case ATTRIBUTES:
                case OPERATIONS:
                case NOTIFICATIONS:
                    setIcon(null);
                    break;
                case ATTRIBUTE:
                case OPERATION:
                case NOTIFICATION:
                    setIcon(null);
                    break;
            }
        } else {
            setToolTipText(null);
        }
        return this;
    }
}
