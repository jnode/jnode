/*
 * $Id$
 *
 * JNode.org
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

import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.peer.MenuItemPeer;
import javax.swing.JMenuItem;

/**
 * AWT menu item peer implemented as a {@link javax.swing.JMenuItem}.
 *
 * @author Levente S\u00e1ntha
 */

final class SwingMenuItemPeer extends
    SwingBaseMenuItemPeer<MenuItem, JMenuItem> implements MenuItemPeer {

    public SwingMenuItemPeer(SwingToolkit toolkit, final MenuItem menuItem) {
        super(toolkit, menuItem, new JMenuItem());
        jComponent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                event.setSource(menuItem);
                menuItem.dispatchEvent(event);
            }
        });
    }
}
