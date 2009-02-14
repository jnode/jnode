package org.jnode.pluginlist;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
class RootNode extends DefaultMutableTreeNode implements Syncable {
    public RootNode(PluginListModel model) {
        super(model);
        for (int i = 0; i < model.size(); i++)
            add(new ProjectNode(model.getProject(i)));
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    public void sync() {
        PluginListModel model = (PluginListModel) getUserObject();
        if (model.size() != getChildCount()) {
            removeAllChildren();
            for (int i = 0; i < model.size(); i++)
                add(new ProjectNode(model.getProject(i)));
        }
        for (int i = 0; i < getChildCount(); i++)
            ((ProjectNode) getChildAt(i)).sync();
    }
}
