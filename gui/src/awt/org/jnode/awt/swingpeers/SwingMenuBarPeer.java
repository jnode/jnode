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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.peer.MenuBarPeer;

/**
 * AWT menu bar peer implemented as a {@link javax.swing.JMenuBar}.
 * @author Levente S\u00e1ntha
 */

final class SwingMenuBarPeer extends SwingMenuComponentPeer<MenuBar, JMenuBar>
        implements MenuBarPeer {

    public SwingMenuBarPeer(SwingToolkit toolkit, MenuBar menuBar) {
        super(toolkit, menuBar, new JMenuBar());
        int mc = menuBar.getMenuCount();
        for(int i = 0; i < mc; i++){
            try{
                Menu m = menuBar.getMenu(i);
                JMenu jm = new JMenu(m.getLabel());
                jComponent.add(jm);
            }catch(Exception e){
                System.out.println("menu count: " + mc);
                System.out.println("menu index: " + i);
                e.printStackTrace();
            }
        }
    }

    public void delMenu(int index) {
        jComponent.remove(index);
    }

    public void addHelpMenu(Menu m) {
        //TODO implement it
    }
}
