/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import java.awt.AWTEvent;
import java.awt.MenuItem;
import java.awt.event.PaintEvent;
import java.awt.peer.MenuItemPeer;

/**
 * AWT menu item peer implemented as a {@link javax.swing.JMenuItem}.
 * @author Levente Sántha
 */

class SwingMenuItemPeer extends SwingMenuComponentPeer implements MenuItemPeer {

    //
    // Construction
    //

    public SwingMenuItemPeer(SwingToolkit toolkit, MenuItem menuItem) {
        super(toolkit, menuItem, new JMenuItem());
    }

    public SwingMenuItemPeer(SwingToolkit toolkit, MenuItem menuItem, JComponent jComponent) {
        super(toolkit, menuItem, jComponent);
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

    public void disable() {
        jComponent.setEnabled(false);
    }

    public void enable() {
        jComponent.setEnabled(true);
    }

    public void setEnabled(boolean enabled) {
        jComponent.setEnabled(enabled);
    }

    public void setLabel(String text) {
        ((JMenuItem)jComponent).setText(text);
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    // Private
}