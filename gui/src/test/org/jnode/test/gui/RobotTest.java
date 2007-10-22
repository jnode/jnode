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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;

/**
 * @author Levente S\u00e1ntha
 */
public class RobotTest extends JPanel {
    private static BufferedImage image;
    private static Color crtColor = Color.BLACK;
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
        g.setColor(crtColor);
        g.fillRect(25,225, 50,50);
    }

    public static void main(String[] argv) throws AWTException {
        final JFrame f = new JFrame();
        final JButton b = new JButton("Click me");
        final JTextField tf = new JTextField();
        final RobotTest t = new RobotTest();
        f.add(tf, BorderLayout.NORTH);
        f.add(t, BorderLayout.CENTER);
        f.add(b, BorderLayout.SOUTH);
        f.pack();
        f.setLocation(0,0);
        f.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                f.dispose();
            }
        });
        f.setVisible(true);

        final Robot r = new Robot();
        r.setAutoDelay(50);
        r.delay(1000);
        image = r.createScreenCapture(new Rectangle(0,0,200,200));
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
        t.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent event) {
                Point p = event.getPoint();
                SwingUtilities.convertPointToScreen(p, t);
                crtColor = r.getPixelColor(p.x, p.y);
                //Graphics g = t.getGraphics();
                //g.setColor(crtColor);
                //g.fillRect(25,225, 50,50);
                t.repaint();

            }
        });
    }

    private static final void moveToCenterOfComponent(Robot r, Component c)
    {
        Point p = c.getLocationOnScreen();
        p.translate(c.getWidth()/2, c.getHeight()/2);
        r.mouseMove((int) p.getX(), (int) p.getY());
    }
}
