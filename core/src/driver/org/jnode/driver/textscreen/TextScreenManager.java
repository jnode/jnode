/*
 * $Id$
 */
package org.jnode.driver.textscreen;

/**
 * Manager of TextScreen's.
 * If a system has no support for TextScreen's, this manager
 * is not available.
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface TextScreenManager {
    
    /** The name used to bind this manager in the InitialNaming namespace. */
    public static final Class NAME = TextScreenManager.class;
    
    /**
     * Gets the system wide text screen.
     */
    public TextScreen getSystemScreen();
}
