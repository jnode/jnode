/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 */
class Project implements Comparable<Object> {
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
