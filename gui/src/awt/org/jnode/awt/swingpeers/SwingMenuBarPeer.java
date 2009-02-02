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
import java.awt.MenuBar;
import java.awt.peer.MenuBarPeer;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 * AWT menu bar peer implemented as a {@link javax.swing.JMenuBar}.
 *
 * @author Levente S\u00e1ntha
 */

final class SwingMenuBarPeer extends SwingMenuComponentPeer<MenuBar, JMenuBar>
    implements MenuBarPeer {

    @SuppressWarnings("deprecation")
    public SwingMenuBarPeer(SwingToolkit toolkit, MenuBar menuBar) {
        super(toolkit, menuBar, new JMenuBar());
        int mc = menuBar.getMenuCount();


        Menu help_menu = menuBar.getHelpMenu();
        if (help_menu != null) {
            mc--;
            //TODO provide a workaround for the smissing swing feature, help menu
            /*
            help_menu.addNotify();
            jComponent.setHelpMenu(((SwingMenuPeer) help_menu.getPeer()).jComponent);
            */
        }
        for (int i = 0; i < mc; i++) {
            Menu menu = menuBar.getMenu(i);
            menu.addNotify();
            jComponent.add(((SwingMenuPeer) menu.getPeer()).jComponent);
        }
        //TODO a better workaround than this
        if (help_menu != null) {
            help_menu.addNotify();
            jComponent.add(((SwingMenuPeer) help_menu.getPeer()).jComponent);
        }
    }

    public void delMenu(int index) {
        jComponent.remove(index);
        jComponent.revalidate();
        jComponent.repaint();
    }

    @SuppressWarnings("deprecation")
    public void addHelpMenu(Menu helpMenu) {
        //TODO provide a workaround for the smissing swing feature, help menu
        helpMenu.addNotify();
        //jComponent.setHelpMenu(((SwingMenuPeer) helpMenu.getPeer()).jComponent);
        jComponent.add(((SwingMenuPeer) helpMenu.getPeer()).jComponent);
    }

    @SuppressWarnings("deprecation")
    public void addMenu(Menu menu) {
        menu.addNotify();
        jComponent.add((JMenu) ((SwingMenuPeer) menu.getPeer()).jComponent);
        jComponent.revalidate();
        jComponent.repaint();
    }
}
