package org.jnode.pluginlist;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
class PluginNode extends DefaultMutableTreeNode implements Syncable {
    public PluginNode(Plugin p) {
        super(p);
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public void sync() {
        //nothing to do
    }
}
