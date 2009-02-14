package org.jnode.pluginlist;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 */
class ProjectNode extends DefaultMutableTreeNode implements Syncable {
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
