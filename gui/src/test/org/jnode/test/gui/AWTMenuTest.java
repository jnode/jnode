/*
 * $Id$
 *
 * Copyright (C) 2003-2012 JNode.org
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

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@SuppressWarnings("serial")
public class AWTMenuTest extends AWTTest {

    /**
     * @param title
     */
    public AWTMenuTest(String title) {
        super(title);
        MenuBar mb = new MenuBar();
        mb.add(new Menu("Window"));
        setMenuBar(mb);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            final AWTMenuTest wnd = new AWTMenuTest("AWTTest");
            wnd.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
