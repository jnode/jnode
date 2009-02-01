/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2009 JNode.org
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
final class LicenseEntry implements Comparable<LicenseEntry> {

    private final String name;

    private final String url;

    public static final LicenseEntry UNKNOWN = new LicenseEntry("-", null);

    /**
     * @param name
     * @param url
     */
    public LicenseEntry(String name, String url) {
        this.name = name;
        this.url = url;
    }

    /**
     * @return Returns the name.
     */
    public final String getName() {
        return name;
    }

    /**
     * @return Returns the url.
     */
    public final String getUrl() {
        return url;
    }

    public String toHtmlString() {
        if ((name != null) && (url != null)) {
            return "<a href='" + url + "'>" + name + "</a>";
        } else {
            return name;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(T)
     */
    public int compareTo(LicenseEntry o) {
        int rc = name.compareTo(o.name);
        if (rc == 0) {
            if ((url != null) && (o.url != null)) {
                rc = url.compareTo(o.url);
            } else if ((url == null) && (o.url != null)) {
                rc = -1;
            } else if ((url != null) && (o.url == null)) {
                rc = 1;
            }
        }
        return rc;
    }
}
