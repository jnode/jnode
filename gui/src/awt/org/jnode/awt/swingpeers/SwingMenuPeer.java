/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.peer.MenuPeer;
import javax.swing.JMenu;

/**
 * AWT menu peer implemented as a {@link javax.swing.JMenu}.
 *
 * @author Levente S\u00e1ntha
 */

final class SwingMenuPeer extends SwingBaseMenuPeer<Menu, JMenu> implements MenuPeer {

    @SuppressWarnings("deprecation")
    public SwingMenuPeer(SwingToolkit toolkit, Menu menu) {
        super(toolkit, menu, new JMenu());
        int item_count = menu.getItemCount();
        for (int i = 0; i < item_count; i++) {
            MenuItem menu_item = menu.getItem(i);
            menu_item.addNotify();
            jComponent.add(((SwingMenuComponentPeer) menu_item.getPeer()).jComponent);
        }
    }

    @SuppressWarnings("deprecation")
    public void addItem(MenuItem item) {
        //the current awt way of adding a separator appears to be adding an item with "-" as the label
        if ("-".equals(item.getLabel())) {
            addSeparator();
        } else {
            item.addNotify();
            jComponent.add(((SwingBaseMenuItemPeer) item.getPeer()).jComponent);
        }
    }

    public void delItem(int index) {
        jComponent.remove(index);
    }

    public void addSeparator() {
        jComponent.addSeparator();
    }
}
