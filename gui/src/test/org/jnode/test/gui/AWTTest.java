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

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.List;
import java.awt.Scrollbar;
import java.awt.Shape;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

//import org.apache.log4j.Logger;

/**
 * @author epr
 */
public class AWTTest extends Frame {

    static class TestComponent extends Component {

        /** My logger */
//		private static final Logger log = Logger.getLogger(TestComponent.class);
        private final boolean useDoubleBuffer;

        public TestComponent(boolean useDoubleBuffer) {
            this.useDoubleBuffer = useDoubleBuffer;
        }

        public void paint(Graphics g) {
            System.out.println("Paint called");

            if (useDoubleBuffer) {
                final int w = getWidth();
                final int h = getHeight();
                System.out.println("Size=" + w + "," + h);
                final Image buffer = createImage(w, h);
                Graphics gb = buffer.getGraphics();
                paintComponent(gb);
                g.drawImage(buffer, 0, 0, null);
            } else {
                paintComponent(g);
            }
        }

        private void paintComponent(Graphics g) {
            g.setColor(Color.red);
            g.drawLine(0, 0, 50, 50);

            if (g instanceof Graphics2D) {
                final Graphics2D g2d = (Graphics2D) g;
                g2d.draw(new Ellipse2D.Double(50, 50, 100, 100));

                g2d.setColor(Color.yellow);
                g2d.fill(new Ellipse2D.Double(150, 150, 70, 70));
            }

//			Color[] colors = new Color[] { Color.BLACK, Color.RED, Color.CYAN, Color.BLUE, Color.GREEN, Color.GRAY };
            Color[] colors = new Color[]{Color.black, Color.red, Color.cyan, Color.blue, Color.green, Color.gray};

            //g.setColor(Color.GREEN);
            //draw(g, new Ellipse2D.Double(40, 40, 300, 300));

            g.setColor(Color.cyan);
            draw(g, createEllipse(42, 42, 296, 296), colors);
        }

        private Shape createEllipse(float x, float y, float w, float h) {
            final GeneralPath path = new GeneralPath();
            final float hw = w / 2.0f;
            final float hh = h / 2.0f;
            //System.out.println("w=" + w + " hw=" + hw);
            /*
            final double angle = Math.PI / 4.0;
            final double a = 1.0 - Math.cos(angle);
            final double b = Math.tan(angle);
            final double c = Math.sqrt(1.0 + b * b) - 1 + a;
            final float cv = (float)(4.0 / 3.0 * a * b / c);
            */
            final float cv = 0.5522848f;
            path.moveTo(x + hw, y);
            // Top right
            path.curveTo(x + hw + hw * cv, y, x + w, y + hh - hh * cv, x + w, y + hh);
            // Bottom right
            path.curveTo(x + w, y + hh + hh * cv, x + hw + hw * cv, y + h, x + hw, y + h);
            // Bottom left
            path.curveTo(x + hh - hh * cv, y + h, x, y + hh + hh * cv, x, y + hh);
            // Top left
            path.curveTo(x, y + hh - hh * cv, x + hh - hh * cv, y, x + hw, y);
            return path;
        }

        protected void draw(Graphics g, Shape shape, Color[] colors) {
            final float[] coords = new float[6];
            final PathIterator i = shape.getPathIterator(null);
            int moveX = 0;
            int moveY = 0;
            int curX = 0;
            int curY = 0;
            int x;
            int y;
            int loop = 0;
            while (!i.isDone()) {
                g.setColor(colors[loop++]);
                final int type = i.currentSegment(coords);
                switch (type) {
                    case PathIterator.SEG_MOVETO:
                        curX = moveX = (int) coords[0];
                        curY = moveY = (int) coords[1];
                        break;
                    case PathIterator.SEG_LINETO:
                        x = (int) coords[0];
                        y = (int) coords[1];
                        g.drawLine(curX, curY, x, y);
                        curX = x;
                        curY = y;
                        break;
                    case PathIterator.SEG_CLOSE:
                        g.drawLine(moveX, moveY, curX, curY);
                        break;
                    case PathIterator.SEG_CUBICTO:
                        drawCurve(g, curX, curY, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                        curX = (int) coords[4];
                        curY = (int) coords[5];
                        break;
                    case PathIterator.SEG_QUADTO:
//						log.debug("QUADTO not implemented yet");
                        // Not implemented yet
                        break;
                }
                i.next();
            }
        }

        protected void drawCurve(Graphics g, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3) {
            double old_x = x0;
            double old_y = y0;
            for (double t = 0.0; t <= 1.0; t += 0.05) {
                double f0 = (1 - t) * (1 - t) * (1 - t);
                double f1 = 3 * t * (1 - t) * (1 - t);
                double f2 = 3 * t * t * (1 - t);
                double f3 = t * t * t;
                double new_x = f0 * x0 + f1 * x1 + f2 * x2 + f3 * x3;
                double new_y = f0 * y0 + f1 * y1 + f2 * y2 + f3 * y3;
                g.drawLine((int) old_x, (int) old_y, (int) new_x, (int) new_y);
                old_x = new_x;
                old_y = new_y;
            }
            g.drawLine((int) old_x, (int) old_y, (int) x3, (int) y3);
        }

    }
    
    public AWTTest(String title) {
        super(title);
        //wnd.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
        setSize(600, 400);
        setLayout(new BorderLayout());
        //wnd.setLocation(75, 50);
        setBackground(Color.GREEN);
        
        final List l = new List();
        l.add("Item 1");
        l.add("Item 2");
        l.add("Item 3");
        //wnd.add(l, BorderLayout.NORTH);
        
        final Button b = new Button("Hello world");
        b.addActionListener(new ActionListener() {
            int i =0;
            public void actionPerformed(ActionEvent e) {
                System.out.println("Action performed " + e);
                switch(i){
                case 0:{
                    add(new Button(String.valueOf(i)), BorderLayout.WEST);
                    break;
                }
                case 1:{
                    add(new Button(String.valueOf(i)), BorderLayout.SOUTH);
                    break;
                }
                case 2:{
                    add(new Button(String.valueOf(i)), BorderLayout.EAST);
                    break;
                }
                default:
                    b.setLabel(String.valueOf(i));
                    setVisible(false);
                }
                i++;
                invalidate();
            }
        });
        add(b, BorderLayout.CENTER);
        b.setBackground(Color.YELLOW);
        
        final Button b2 = new Button("Left");
        final Scrollbar sb = new Scrollbar(Scrollbar.HORIZONTAL);
        final Checkbox cb1 = new Checkbox("Right");

        add(b2, BorderLayout.WEST);
        b2.setBackground(Color.RED);
        b2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Action on b2");
                if (sb.getValue() + sb.getBlockIncrement() <= sb.getMaximum()) {
                    sb.setValue(sb.getValue() + sb.getBlockIncrement());
                } else {
                    Frame f2 = new Frame("New frame");
                    f2.setSize(200, 100);
                    f2.setVisible(true);
                }
            }
        });
        
        add(cb1, BorderLayout.EAST);
        cb1.setBackground(Color.WHITE);
                
        final TextField tf = new TextField();
        tf.setText("Let's type here");
        add(tf, BorderLayout.SOUTH);
        
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
    }

    public static void main(String[] args) throws InterruptedException {
        boolean useDoubleBuffer = (args.length > 0) && args[0].equals("buffer");
        
        try {
            final AWTTest wnd = new AWTTest("AWTTest");
            wnd.setVisible(true);
        }catch(Throwable t){
            t.printStackTrace();
        }
    }
}
