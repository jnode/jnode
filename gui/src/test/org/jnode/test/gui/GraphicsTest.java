/**
 * $Id$  
 */
package org.jnode.test.gui;

import org.jnode.driver.video.util.Curves;

import java.awt.Frame;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * @author Levente S?ntha
 */
public class GraphicsTest {
    public static void main(String[] args)
            throws Exception {
        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            wnd.add(new TestComponent());
            wnd.show();

            Thread.sleep(5000);

            wnd.hide();
        } finally {
            wnd.dispose();
        }
    }

    static class TestComponent extends Component {

        public void paint(Graphics g) {
            g.setColor(Color.RED);
            g.translate(10, 10);
            g.drawRect(0, 0, 100,100);
            g.translate(10, 20);
            g.drawRect(0, 0, 100,100);
            g.translate(20, 10);
            g.drawRect(0, 0, 100,100);
        }
    }
}
