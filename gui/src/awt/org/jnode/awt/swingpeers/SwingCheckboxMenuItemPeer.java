/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.CheckboxMenuItem;
import java.awt.event.PaintEvent;
import java.awt.peer.CheckboxMenuItemPeer;

import javax.swing.JCheckBoxMenuItem;

/**
 * AWT checkbox menu item peer implemented as a
 * {@link javax.swing.JCheckBoxMenuItem}.
 */

class SwingCheckboxMenuItemPeer extends JCheckBoxMenuItem implements
        CheckboxMenuItemPeer {

    //
    // Construction
    //

    public SwingCheckboxMenuItemPeer(CheckboxMenuItem checkBoxMenuItem) {
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