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
 
package org.jnode.test.gui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingTest {

	public static void main(String[] args) {
		try {
			//UIDefaults defs = UIManager.getDefaults();
			//System.out.println("Defs=" + defs);
			
			//KeyStroke.getKeyStroke(' ');
			//AWTKeyStroke ks = AWTKeyStroke.getAWTKeyStroke("SPACE");
			//System.out.println("ks=" + ks);
			//JPanel pan = new JPanel();
            JFrame frame = new JFrame("JFrame test");
            frame.setLocation(100, 100);
            frame.setSize(400, 400);
            frame.getContentPane().add(new JButton("JButton test"), BorderLayout.NORTH);
            JTabbedPane tabs = new JTabbedPane();
            tabs.add("Tab1", new JButton("JButton1"));
            tabs.add("Tab2", new JButton("JButton2"));
            tabs.add("Tab3", new JButton("JButton3"));
            //frame.getContentPane().add(tabs, BorderLayout.CENTER);
//            JMenuBar mb = new JMenuBar();
//            JMenu menu = new JMenu("JMenu test");
//            JMenuItem mi = new JMenuItem("JMenuItem test");
//            mb.add(menu);
//            menu.add(mi);
//            frame.setJMenuBar(mb);
            frame.validate();
            frame.show();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
