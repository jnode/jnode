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
    JTree tree;

    PluginTreePane(final PluginListModel model) {
        tree = new JTree(new RootNode(model)) {
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
