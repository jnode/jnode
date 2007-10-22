/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import java.awt.BorderLayout;

/**
 * @author Levente S\u00e1ntha
 */
public class JInternalFrameTest {
    public static void main(String[] argv){
        JFrame f = new JFrame("Test");
        JDesktopPane dt = new JDesktopPane();
        f.getContentPane().add(dt, BorderLayout.CENTER);
        JInternalFrame ifr = new JInternalFrame("IF1");
        ifr.setLocation(0,0);
        ifr.setSize(150,150);
        ifr.setResizable(true);
        ifr.setClosable(true);
        ifr.setMaximizable(true);
        dt.add(ifr);
        ifr.setVisible(true);

        JInternalFrame ifr2 = new JInternalFrame("IF2");
        ifr2.setLocation(20,20);
        ifr2.setSize(150,150);
        ifr2.setResizable(true);
        ifr2.setClosable(true);
        ifr2.setMaximizable(true);
        dt.add(ifr2);
        ifr2.setVisible(true);

        JInternalFrame ifr3 = new JInternalFrame("IF3");
        ifr3.setLocation(40,40);
        ifr3.setSize(150,150);
        ifr3.setResizable(true);
        ifr3.setClosable(true);
        ifr3.setMaximizable(true);
        dt.add(ifr3);
        ifr3.setVisible(true);

        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLocation(0,0);
        f.setSize(300,300);
        f.setVisible(true);
    }
}
