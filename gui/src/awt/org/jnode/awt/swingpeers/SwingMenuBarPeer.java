/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.event.PaintEvent;
import java.awt.peer.MenuBarPeer;

import javax.swing.JMenuBar;

/**
 * AWT menu bar peer implemented as a {@link javax.swing.JMenuBar}.
 */

class SwingMenuBarPeer extends JMenuBar implements MenuBarPeer {

    //
    // Construction
    //

    public SwingMenuBarPeer(MenuBar menuBar) {
        super();
    }

    //
    // MenuBarPeer
    //

    public void addMenu(Menu m) {
    }

    public void delMenu(int index) {
    }

    public void addHelpMenu(Menu m) {
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