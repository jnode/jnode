package org.jnode.test.gui;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JComponent;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.Graphics;

/**
 * @author Levente S\u00e1ntha
 */
public class ImageIOTest {
    public static void main(String[] argv) throws Exception{
        final BufferedImage bi = ImageIO.read(new File(argv[0]));

        if(bi == null){
            System.out.println("image loading failed");
        } else {
            JFrame f = new JFrame("ImageTest");
            f.add(new JComponent(){
                public void paint(Graphics g) {
                    super.paint(g);
                    g.drawImage(bi, 0, 0, this);
                }
            });
            f.setSize(400,400);
            f.setVisible(true);
        }
    }
}
