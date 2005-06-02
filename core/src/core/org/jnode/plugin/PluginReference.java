/*
 * $Id$
 */
package org.jnode.plugin;

/**
 * Class the contains a full reference to a plugin, containing its
 * id and its version.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class PluginReference implements Comparable<PluginReference> {

    private final String id;

    private final String version;

    /**
     * @param id
     * @param version
     */
    public PluginReference(String id, String version) {
        this.id = id;
        this.version = version;
    }

    /**
     * Gets the id of the plugin.
     * 
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Gets the version of the plugin.
     * 
     * @return Returns the version.
     */
    public final String getVersion() {
        return version;
    }
       
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof PluginReference) {
            return (compareTo((PluginReference)obj) == 0);
        } else {
            return false;
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return id.hashCode() ^ version.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return id + ", " + version;
    }

    /**
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(PluginReference o) {
        int rc = id.compareTo(o.id);
        if (rc == 0) {
            rc = version.compareTo(o.version);
        }
        return rc;
    }
}
