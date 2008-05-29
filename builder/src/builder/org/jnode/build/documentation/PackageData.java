/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.build.documentation;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class PackageData implements Comparable<PackageData> {

    private final String packageName;
    private final PluginData plugin;

    /**
     * @param name
     * @param plugin
     */
    public PackageData(String name, PluginData plugin) {
        // TODO Auto-generated constructor stub
        packageName = name;
        this.plugin = plugin;
    }

    /**
     * @return Returns the packageName.
     */
    public final String getPackageName() {
        return packageName;
    }

    /**
     * @return Returns the plugin.
     */
    public final PluginData getPlugin() {
        return plugin;
    }

    /**
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(PackageData o) {
        return this.packageName.compareTo(o.packageName);
    }
}
