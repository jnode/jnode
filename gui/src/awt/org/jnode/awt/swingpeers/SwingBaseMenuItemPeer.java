/*
 * $Id$
 */
package org.jnode.awt.swingpeers;

import java.awt.AWTEvent;
import java.awt.MenuItem;
import java.awt.event.PaintEvent;
import java.awt.peer.MenuItemPeer;

import javax.swing.JComponent;
import javax.swing.JMenuItem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
abstract class SwingBaseMenuItemPeer<awtT extends MenuItem, peerT extends JComponent>
        extends SwingMenuComponentPeer<awtT, peerT> implements
        MenuItemPeer {

    //
    // Construction
    //

    public SwingBaseMenuItemPeer(SwingToolkit toolkit, awtT menuItem, peerT peer) {
        super(toolkit, menuItem, peer);
    }

    // public SwingMenuItemPeer(SwingToolkit toolkit, MenuItem menuItem,
    // JComponent jComponent) {
    // super(toolkit, menuItem, jComponent);
    // }
    //
    //
    // ComponentPeer
    //

    // Events

    public void handleEvent(AWTEvent e) {
        // System.err.println(e);
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
        ((JMenuItem) jComponent).setText(text);
    }
    // /////////////////////////////////////////////////////////////////////////////////////
    // Private
}
