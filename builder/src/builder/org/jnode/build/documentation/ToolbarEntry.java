/*
 * $Id$
 */
package org.jnode.build.documentation;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class ToolbarEntry {

    private final String title;

    private final String href;

    /**
     * @param title
     * @param href
     */
    public ToolbarEntry(String title, String href) {
        this.title = title;
        this.href = href;
    }

    /**
     * @return Returns the href.
     */
    public final String getHref() {
        return href;
    }

    /**
     * @return Returns the title.
     */
    public final String getTitle() {
        return title;
    }
}
