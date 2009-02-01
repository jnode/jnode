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

import java.awt.image.IndexColorModel;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class ColorModelTest {
    public static void main(String[] args) {
        run(8, 256);
        System.out
            .println("Expecting IllegalArgumentException; attempting to pass more than 16 bits to IndexColorModel()");
        try {
            run(32, 256);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught IllegalArgumentException as expected");
        }
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
