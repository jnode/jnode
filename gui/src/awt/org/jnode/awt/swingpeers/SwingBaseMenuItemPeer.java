/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
        setLabel(menuItem.getLabel());
    }

    // Events

    public void handleEvent(AWTEvent e) {
        // System.err.println(e);
    }

    public void coalescePaintEvent(PaintEvent e) {
        System.err.println(e);
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
