/*
 * $Id$
 */
package org.jnode.driver.textscreen;

/**
 * A ScrollableScreen has more lines then an actual (device)
 * screen and maps a visible portion of its screen onto the 
 * actual (device)screen.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface ScrollableTextScreen extends TextScreen {
    
    /**
     * Scroll a given number of rows up.
     * @param rows
     */
    public void scrollUp(int rows);
    
    /**
     * Scroll a given number of rows down.
     * @param rows
     */
    public void scrollDown(int rows);
}
