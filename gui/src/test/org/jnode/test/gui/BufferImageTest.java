/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.image.BufferedImage;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class BufferImageTest {

    public static void main(String[] args) {
        BufferedImage img = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);
        System.out.println("numBands=" + img.getRaster().getSampleModel().getNumBands());
    }
}
