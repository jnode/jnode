/*
 * $Id$
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
