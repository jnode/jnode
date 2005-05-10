/*
 * $Id$
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
