/*
 * $Id$
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