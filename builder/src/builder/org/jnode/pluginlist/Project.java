package org.jnode.pluginlist;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
class Project implements Comparable {
    private String name;
    private List<Plugin> pluginList = new ArrayList<Plugin>();

    public Project(String name) {
        this.name = name;
    }

    void addPlugin(Plugin plugin) {
        pluginList.add(plugin);
    }

    List<Plugin> plugins() {
        return Collections.unmodifiableList(pluginList);
    }

    @Override
    public String toString() {
        return name;
    }

    public Plugin getPlugin(int index) {
        return pluginList.get(index);
    }

    public int size() {
        return pluginList.size();
    }

    void sort() {
        Collections.sort(pluginList);
    }

    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Object o) {
        return name.compareTo(((Project) o).name);
    }

    public void remove(Plugin plu) {
        pluginList.remove(plu);
    }
}
