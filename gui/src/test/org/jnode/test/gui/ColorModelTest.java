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

import java.awt.image.IndexColorModel;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ColorModelTest {
    public static void main(String[] args) {
        run(8, 256);
        run(32, 256);
    }

    public static void run(int bits, int size) {
        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];

        IndexColorModel cm = new IndexColorModel(bits, size, r, g, b);

        System.out.println("Bits         = " + bits);
        System.out.println("Size         = " + size);
        System.out.println("cm.PixelSize = " + cm.getPixelSize());
        System.out.println("cm.MapSize   = " + cm.getMapSize());
    }
}
