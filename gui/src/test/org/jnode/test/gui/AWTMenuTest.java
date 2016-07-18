/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SuppressWarnings("serial")
public class AWTMenuTest extends AWTTest {

    public AWTMenuTest(String title) {
        super(title);
        MenuBar mb = new MenuBar();
        Menu file = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        file.add(exit);
        mb.add(file);
        Menu window = new Menu("Window");
        MenuItem test = new MenuItem("Test");
        window.add(test);
        mb.add(window);
        setMenuBar(mb);
    }

    public static void main(String[] args) {
        try {
            final AWTMenuTest wnd = new AWTMenuTest("AWTTest");
            wnd.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
