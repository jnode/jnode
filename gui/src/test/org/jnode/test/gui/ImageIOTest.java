/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * @author Levente S\u00e1ntha
 */
public class ImageIOTest {
    public static void main(String[] argv) throws Exception {
        if (argv.length == 0) {
            System.out.println("org.jnode.test.gui.ImageIOTest <image file>");
        } else {
            final BufferedImage bi = ImageIO.read(new File(argv[0]));

            if (bi == null) {
                System.out.println("image loading failed");
            } else {
                JFrame f = new JFrame("ImageTest") {
                    public void paint(Graphics g) {
                        g.drawImage(bi, 0, 0, this);
                    }
                };
                f.setSize(400, 400);
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                f.setVisible(true);
            }
        }
    }
}
