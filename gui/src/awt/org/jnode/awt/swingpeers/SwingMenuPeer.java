/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.PaintEvent;
import java.awt.peer.MenuPeer;

import javax.swing.JMenu;

/**
 * AWT menu peer implemented as a {@link javax.swing.JMenu}.
 */

class SwingMenuPeer extends JMenu implements MenuPeer {

    //
    // Construction
    //

    public SwingMenuPeer(Menu menu) {
        super();
    }

    //
    // MenuPeer
    //

    public void addItem(MenuItem item) {
    }

    public void delItem(int index) {
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