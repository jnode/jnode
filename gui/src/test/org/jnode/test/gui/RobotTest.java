package org.jnode.test.gui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JTextField;

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
        final Frame f = new Frame();
        final JButton b = new JButton("Click me");
        final JTextField tf = new JTextField();
        final RobotTest t = new RobotTest();
        f.add(tf, BorderLayout.NORTH);
        f.add(t, BorderLayout.CENTER);
        f.add(b, BorderLayout.SOUTH);
        f.pack();
        f.setLocation(0,0);
        f.setVisible(true);
        
        Robot r = new Robot();
        r.setAutoDelay(50);
        r.delay(1000);
//        image = r.createScreenCapture(new Rectangle(0,0,200,200));
        t.repaint();
//        for(int i = 0; i < 400; i++){
//            r.mouseMove(i, i);
//        }

        b.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        tf.setText("Clicked !");
                    }                    
                });
        
        moveToCenterOfComponent(r, b);
        r.mousePress(InputEvent.BUTTON1_MASK);
        r.mouseRelease(InputEvent.BUTTON1_MASK);
                
        Point p = f.getLocationOnScreen();
        p.translate(f.getWidth()/2, 5);
        r.mouseMove((int) p.getX(), (int) p.getY());
        r.mousePress(InputEvent.BUTTON1_MASK);
        for(int i = 0; i < 100; i++)
        {
            r.mouseMove((int) p.getX()+i, (int) p.getY()+i);
        }
        r.mouseRelease(InputEvent.BUTTON1_MASK);
    }
    
    private static final void moveToCenterOfComponent(Robot r, Component c)
    {
        Point p = c.getLocationOnScreen();
        p.translate(c.getWidth()/2, c.getHeight()/2);
        r.mouseMove((int) p.getX(), (int) p.getY());        
    }
}
