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

import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Event;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.peer.PopupMenuPeer;

/**
 * AWT popup menu peer implemented as a {@link javax.swing.JPopupMenu}.
 * @author Levente S\u00e1ntha
 */

final class SwingPopupMenuPeer extends SwingBaseMenuPeer<PopupMenu, JPopupMenu>
        implements PopupMenuPeer {

    @SuppressWarnings("deprecation")
    public SwingPopupMenuPeer(SwingToolkit toolkit, PopupMenu popupMenu) {
        super(toolkit, popupMenu, new JPopupMenu());
        int item_count = popupMenu.getItemCount();
        for(int i = 0; i < item_count; i++){
            MenuItem menu_item = popupMenu.getItem(i);
            menu_item.addNotify();
            jComponent.add(((SwingMenuComponentPeer)menu_item.getPeer()).jComponent);
        }
    }

    public void setLabel(String text) {
        ((JPopupMenu) jComponent).setLabel(text);
    }

    public void show(Event e) {
        //TODO implement it
    }

    @SuppressWarnings("deprecation")
    public void show(Component component, int x, int y) {
        ((JPopupMenu)jComponent).show(((SwingComponentPeer) component.getPeer()).peerComponent, x, y);
    }

    @SuppressWarnings("deprecation")
    public void addItem(MenuItem item) {
        item.addNotify();
        jComponent.add(((SwingMenuItemPeer)item.getPeer()).jComponent);
    }

    public void delItem(int index) {
        jComponent.remove(index);
    }

    public void addSeparator() {
        jComponent.addSeparator();
    }
}
