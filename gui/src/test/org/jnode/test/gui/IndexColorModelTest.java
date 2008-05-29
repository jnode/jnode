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

import java.awt.image.IndexColorModel;

/**
 * @author epr
 */
public class IndexColorModelTest {

    public static void main(String[] args) {

        final byte[] r = {0, 63, (byte) 128, (byte) 168, (byte) 255};
        final byte[] g = {63, (byte) 128, (byte) 168, (byte) 255, 0};
        final byte[] b = {(byte) 128, (byte) 168, (byte) 255, 0, 63};

        final IndexColorModel model = new IndexColorModel(8, r.length, r, g, b);

        for (int i = 0; i < r.length; i++) {
            System.out.println("i=" + i);
            System.out.println("r: " + (r[i] & 0xFF) + ", " + model.getRed(i));
            System.out.println("g: " + (g[i] & 0xFF) + ", " + model.getGreen(i));
            System.out.println("b: " + (b[i] & 0xFF) + ", " + model.getBlue(i));
            System.out.println("rgb: " + Integer.toHexString(model.getRGB(i)));
        }
    }
}
