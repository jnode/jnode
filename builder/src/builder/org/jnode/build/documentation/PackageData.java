/*
 * $Id$
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
