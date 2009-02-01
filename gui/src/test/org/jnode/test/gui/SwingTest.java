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

import java.awt.BorderLayout;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SwingTest extends JFrame {

    private final JButton north;
    private final JButton south;

    public SwingTest(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().setDoubleBuffered(false);
        setLocation(100, 100);
        setSize(400, 400);
        //getContentPane().add(north = new JButton("JButton north"), BorderLayout.NORTH);
        URL url = getClass().getClassLoader().getResource("JNode_logo_trans.png");
        if (url == null) {
            System.out.println("Unable to load image: getRecource(\"JNode_logo_trans.png\")");
            north = new JButton("JButton north");
        } else {
            north = new JButton("JButton north", new ImageIcon(url));
        }
        getContentPane().add(north, BorderLayout.NORTH);
        getContentPane().add(new JTextArea("JTextArea test"), BorderLayout.CENTER);
        getContentPane().add(south = new JButton("JButton south"), BorderLayout.SOUTH);
        north.requestFocus();
    }

    public void dumpInfo() {
        System.out.println("frame.size:                 " + getSize());
        System.out.println("frame.insets:               " + getInsets());
//        System.out.println("frame.peer.insets:         " + ((FramePeer)getPeer()).getInsets());
        System.out.println("frame.contentpane.bounds:   " + getContentPane().getBounds());
        System.out.println("north.bounds                " + north.getBounds());
        System.out.println("south.bounds                " + south.getBounds());
    }

    public static void main(String[] args) {
        try {
            SwingTest frame = new SwingTest("JFrame test");
            frame.validate();
            frame.setVisible(true);
            frame.dumpInfo();
            Thread.sleep(10000);
            frame.dumpInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
