/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
            return (compareTo((PluginReference) obj) == 0);
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
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(PluginReference o) {
        int rc = id.compareTo(o.id);
        if (rc == 0) {
            rc = version.compareTo(o.version);
        }
        return rc;
    }
}
