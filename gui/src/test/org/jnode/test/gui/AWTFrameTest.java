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
 
package org.jnode.test.gui;

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Levente S\u00e1ntha
 */

public class AWTFrameTest {
    public static void main(String[] args) {
        final Frame f = new Frame("Frame test");
        f.setSize(200, 200);
        f.setLocation(50, 50);
        final Frame f2 = new Frame("Test");
        f2.setSize(100, 100);
        f2.setLocation(350, 200);
        f.setLayout(new GridLayout(5, 1));

        ActionListener close = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        };

        Button show = new Button("show");
        show.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(true);
            }
        });
        //f.add(show);
        Button hide = new Button("hide");
        hide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(false);
            }
        });
        //f.add(hide);
        Button back = new Button("back");
        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toBack();
            }
        });
        //f.add(back);
        Button front = new Button("front");
        front.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toFront();
            }
        });
        //f.add(front);

        MenuBar mb = new MenuBar();
        Menu window = new Menu("Window");
        mb.add(window);
        MenuItem show_mi = new MenuItem("show");
        show_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(true);
            }
        });
        window.add(show_mi);
        MenuItem hide_mi = new MenuItem("hide");
        hide_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(false);
            }
        });
        window.add(hide_mi);
        MenuItem back_mi = new MenuItem("back");
        back_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toBack();
            }
        });
        window.add(back_mi);
        MenuItem front_mi = new MenuItem("front");
        front_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toFront();
            }
        });
        window.add(front_mi);
        window.addSeparator();
        MenuItem close_mi = new MenuItem("close");
        close_mi.addActionListener(close);
        window.add(close_mi);

        //popup menu
        final PopupMenu p_window = new PopupMenu("Window");
        MenuItem p_show_mi = new MenuItem("show");
        p_show_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(true);
            }
        });
        p_window.add(p_show_mi);
        MenuItem p_hide_mi = new MenuItem("hide");
        p_hide_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.setVisible(false);
            }
        });
        p_window.add(p_hide_mi);
        MenuItem p_back_mi = new MenuItem("back");
        p_back_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toBack();
            }
        });
        p_window.add(p_back_mi);
        MenuItem p_front_mi = new MenuItem("front");
        p_front_mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                f2.toFront();
            }
        });
        p_window.add(p_front_mi);
        p_window.addSeparator();
        MenuItem p_close_mi = new MenuItem("close");
        p_close_mi.addActionListener(close);
        p_window.add(p_close_mi);

        f.add(p_window);
        f.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                p_window.show(f, event.getX(), event.getY());
            }
        });

        f.setMenuBar(mb);
        f.validate();
        f.setVisible(true);
    }
}
