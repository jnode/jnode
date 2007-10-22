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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.ImageObserver;
import java.net.URL;

/**
 * @author epr
 */
public class ImageTest {
    
    final static String IMAGE_PATH = "/org/zaval/lw/rs/img/tree.gif";

    static class TestComponent extends Component implements ImageObserver {

        private final Image img;

        public TestComponent() {
            super();
            URL url = ClassLoader.getSystemResource(IMAGE_PATH);
            if (url == null) {
                System.out.println("Trying to load " + IMAGE_PATH + ": bad url found.");
                System.exit(-1);
            }
            img = Toolkit.getDefaultToolkit().createImage(url);
        }

        public void paint(Graphics g) {
            System.out.println("Paint called");
            super.paint(g);

            g.setColor(Color.green);
            g.drawRect(0, 0, getWidth(), getHeight());
            g.drawImage(img, 1, 1, this);
        }

        /**
         * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
         */
        public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
            System.out.println("imageUpdate " + flags + "," + x + "," + y + "," + w + "," + h);
            return super.imageUpdate(img, flags, x, y, w, h);
        }

    }

    public static void main(String[] args) throws Exception {
        final boolean loadOnly = (args.length > 0) && args[0].equalsIgnoreCase("loadOnly");

        if (!loadOnly) {
            final Frame wnd = new Frame("Waiting for input from System.in stream");
            try {
                wnd.setSize(600, 400);
                wnd.add(new TestComponent());
                wnd.setVisible(true);

                //Thread.sleep(5000);
                System.in.read();
            } finally {
                wnd.dispose();
            }
        }
    }
}
