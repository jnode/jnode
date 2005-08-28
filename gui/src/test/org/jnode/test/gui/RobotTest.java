package org.jnode.test.gui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Frame;
import java.awt.BorderLayout;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * @author Levente S\u00e1ntha
 */
public class RobotTest extends Canvas {
    private static BufferedImage image;
    public RobotTest(){
        setBackground(Color.BLACK);
    }

    public Dimension getPreferredSize() {
        return new Dimension(400,400);
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 100, 100);
        g.setColor(Color.GREEN);
        g.fillRect(100,100,100,100);
        g.setColor(Color.YELLOW);
        g.fillRect(0,100,100,100);
        g.setColor(Color.RED);
        g.fillRect(100,0, 100,100);
        if(image != null){
            g.drawImage(image, 200,200, this);
        }
    }

    public static void main(String[] argv) throws AWTException {
        Frame f = new Frame();
        RobotTest t = new RobotTest();
        f.add(t, BorderLayout.CENTER);
        f.pack();
        f.setLocation(0,0);
        f.setVisible(true);
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.delay(1000);
        image = r.createScreenCapture(new Rectangle(0,0,200,200));
        t.repaint();
        for(int i = 0; i < 400; i++){
            r.mouseMove(i, i);
        }
    }
}
