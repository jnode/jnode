package org.jnode.pluginlist;

/**
 *
 */
interface PluginListModel {
    PluginRepository getRepository();

    Project getProject(int index);

    int size();

    String getTooltipText(Plugin o);
}
