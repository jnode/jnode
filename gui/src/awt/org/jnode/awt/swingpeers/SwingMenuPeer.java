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

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.peer.MenuPeer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;

/**
 * AWT menu peer implemented as a {@link javax.swing.JMenu}.
 * @author Levente S\u00e1ntha
 */

final class SwingMenuPeer extends SwingBaseMenuPeer<Menu, JMenu> implements MenuPeer {

    public SwingMenuPeer(SwingToolkit toolkit, Menu menu) {
        super(toolkit, menu, new JMenu());
    }

    public void addItem(MenuItem item) {
        Action action = new AbstractAction(){
            public void actionPerformed(ActionEvent e) {
                //TODO implement it
            }
        };
        action.putValue(Action.NAME, item.getLabel());
        jComponent.add(action);
    }

    public void delItem(int index) {
        jComponent.remove(index);
    }
}
