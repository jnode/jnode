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
import java.awt.Color;
import java.awt.Frame;
import java.awt.Label;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * @author Levente S\u00e1ntha
 */
public class MouseEventTest {
    public static void main(String[] argv) {
        final Frame f = new Frame("Mouse event test");
        f.add(createLabel("WEST", Color.GREEN, true), BorderLayout.WEST);
        f.add(createLabel("EAST", Color.BLUE, true), BorderLayout.EAST);
        f.add(createLabel("NORTH", Color.RED, true), BorderLayout.NORTH);
        f.add(createLabel("CENTER", Color.WHITE, true), BorderLayout.CENTER);
        //f.add(createLabel("SOUTH", Color.MAGENTA, true), BorderLayout.SOUTH);
        JTextArea jta = new JTextArea("SOUTH\nThis is a\nscrollable textarea.\nUse mouse wheel\nto see all the text\n");
        f.add(new JScrollPane(jta), BorderLayout.CENTER);

        f.setSize(300, 300);
        f.setLocation(0, 0);
        f.validate();
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.dispose();
            }
        });
        f.setVisible(true);
    }

    private static Label createLabel(String name, Color color, boolean withListeners) {
        Label label = new Label(name);
        label.setAlignment(Label.CENTER);
        label.setName(name);
        label.setBackground(color);
        if (withListeners) {
            label.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent event) {
                    printEvent(event);
                }

                public void mousePressed(MouseEvent event) {
                    printEvent(event);
                }

                public void mouseReleased(MouseEvent event) {
                    printEvent(event);
                }

                public void mouseEntered(MouseEvent event) {
                    printEvent(event);
                }

                public void mouseExited(MouseEvent event) {
                    printEvent(event);
                }
            });
            label.addMouseMotionListener(new MouseMotionListener() {
                public void mouseDragged(MouseEvent event) {
                    printEvent(event);
                }

                public void mouseMoved(MouseEvent event) {
                    printEvent(event);
                }
            });
            label.addMouseWheelListener(new MouseWheelListener() {
                public void mouseWheelMoved(MouseWheelEvent event) {
                    printEvent(event);
                }
            });
        }
        return label;
    }

    private static void printEvent(MouseEvent event) {
        System.out.println(event);
    }
}
