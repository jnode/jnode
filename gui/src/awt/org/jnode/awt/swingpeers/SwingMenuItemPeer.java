/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.MenuItem;
import java.awt.event.PaintEvent;
import java.awt.peer.MenuItemPeer;

import javax.swing.JMenuItem;

/**
 * AWT menu item peer implemented as a {@link javax.swing.JMenuItem}.
 */

class SwingMenuItemPeer extends JMenuItem implements MenuItemPeer {

    //
    // Construction
    //

    public SwingMenuItemPeer(MenuItem menuItem) {
        super();
    }

    //
    // ComponentPeer
    //

    // Events

    public void handleEvent(AWTEvent e) {
        //System.err.println(e);
    }

    public void coalescePaintEvent(PaintEvent e) {
        System.err.println(e);
    }

    // Misc

    public void dispose() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // Private
}