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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Frame;
import java.awt.TextField;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class TextFieldTest extends Frame {

    public TextFieldTest() {
        super("TextFieldTest");
        final TextField tf = new TextField();
        final Button b = new Button();
        add(tf, BorderLayout.CENTER);
        add(b, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        try {
            final TextFieldTest wnd = new TextFieldTest();
            wnd.setSize(200, 100);
            wnd.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    wnd.dispose();
                }
            });
            wnd.setVisible(true);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
