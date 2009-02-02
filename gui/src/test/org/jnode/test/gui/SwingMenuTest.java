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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingMenuTest extends SwingTest {

    /**
     * @param title
     */
    public SwingMenuTest(String title) {
        super(title);
        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("JMenu test");
        JMenuItem mi = new JMenuItem("JMenuItem test");
        mb.add(menu);
        menu.add(mi);
        setJMenuBar(mb);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            SwingMenuTest frame = new SwingMenuTest("JFrame test");
            frame.validate();
            frame.setVisible(true);
            frame.dumpInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
