package org.jnode.test.gui;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Levente S\u00e1ntha
 */
public class JTreeTest {
    public static void main(String[] argv) {
        JFrame f = new JFrame();
        f.setSize(400, 400);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("A");
        root.add(new DefaultMutableTreeNode("B"));
        root.add(new DefaultMutableTreeNode("C"));
        f.add(new JScrollPane(new JTree(root)));
        /*
        f.add(new JScrollPane(new JTree(new TreeModel() {
            public Object getRoot() {
                return 1;
            }

            public Object getChild(Object parent, int index) {
                return index == 0 ? ((Integer) parent) * 2 : ((Integer) parent) * 2 + 1;
            }

            public int getChildCount(Object parent) {
                return 2;
            }

            public boolean isLeaf(Object node) {
                return false;
            }

            public int getIndexOfChild(Object parent, Object child) {
                return ((Integer) child) / 2 == 0 ? 0 : 1;
            }

            public void addTreeModelListener(TreeModelListener listener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void removeTreeModelListener(TreeModelListener listener) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void valueForPathChanged(TreePath path, Object newvalue) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        })));
        */
        f.setVisible(true);
    }
}
