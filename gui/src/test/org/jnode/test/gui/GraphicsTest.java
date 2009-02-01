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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;

/**
 * @author Levente S?ntha
 */
public class GraphicsTest {
    public static void main(String[] args)
        throws Exception {
        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            wnd.add(new TestComponent());
            wnd.show();

            Thread.sleep(5000);

            wnd.hide();
        } finally {
            wnd.dispose();
        }
    }

    static class TestComponent extends Component {

        public void paint(Graphics g) {
            g.setColor(Color.RED);
            g.translate(10, 10);
            g.drawRect(0, 0, 100, 100);
            g.translate(10, 20);
            g.drawRect(0, 0, 100, 100);
            g.translate(20, 10);
            g.drawRect(0, 0, 100, 100);
        }
    }
}
