/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
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
