package org.jnode.test.gui;

import java.awt.Image;

public class Imageutil {

    public static Image getImage() {
        // ---
        String file = "D:\\java\\spirit.jpg";
//	Image img1 = Toolkit.getDefaultToolkit().getImage( file );
        javax.swing.ImageIcon ic = new javax.swing.ImageIcon(file);
        Image img1 = ic.getImage();


        return img1;
    }
}
