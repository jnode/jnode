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
 
package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Levente S\u00e1ntha
 */
public class AWTMenuBuilderTest {
    public static void main(String[] argv) {
        final List<Menu> menuList = new ArrayList<Menu>();
        final Frame f = new Frame("AWT Menu Builder Test");
        final MenuBar mb = new MenuBar();
        f.setMenuBar(mb);

        Menu m = new Menu("Menu " + (menuList.size() + 1));
        menuList.add(m);
        mb.add(m);

        Button addMenu = new Button("Add menu");
        addMenu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Menu m = new Menu("Menu " + (menuList.size() + 1));
                menuList.add(m);
                mb.add(m);
            }
        });
        final PopupMenu popup = new PopupMenu("Popup");
        f.add(popup);
        f.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    popup.show(f, event.getX(), event.getY());
                }
            }
        });
        f.add(addMenu, BorderLayout.NORTH);
        Button addItem = new Button("Add item");
        addItem.addActionListener(new ActionListener() {
            int cnt = 1;

            public void actionPerformed(ActionEvent event) {
                for (Menu m : menuList) {
                    m.add(new MenuItem("Item " + cnt));
                }
                popup.add(new MenuItem("Item " + cnt));
                cnt++;
            }
        });
        f.add(addItem, BorderLayout.WEST);
        Button addSeparator = new Button("Add separator");
        addSeparator.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                for (Menu m : menuList) {
                    m.addSeparator();
                }
                popup.addSeparator();
            }
        });
        f.add(addSeparator, BorderLayout.EAST);
        Button removeItem = new Button("Remove item");
        removeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                for (Menu m : menuList) {
                    if (m.getItemCount() > 0)
                        m.remove(0);
                }
                if (popup.getItemCount() > 0)
                    popup.remove(0);
            }
        });
        f.add(removeItem, BorderLayout.SOUTH);
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                f.dispose();
            }
        });
        f.setSize(300, 300);
        f.setVisible(true);
    }
}
