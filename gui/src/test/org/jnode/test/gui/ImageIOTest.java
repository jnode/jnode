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
