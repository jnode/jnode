/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.List;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.Shape;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import javax.swing.JCheckBox;

//import org.apache.log4j.Logger;

/**
 * @author epr
 */
public class AWTTest {

    static class TestComponent extends Component {

        /** My logger */
//		private final Logger log = Logger.getLogger(getClass());
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

    public static void main(String[] args) throws InterruptedException {
        boolean useDoubleBuffer = (args.length > 0) && args[0].equals("buffer");
        
        final Frame wnd = new Frame("AWTTest");
        try {
        	//wnd.setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
            wnd.setSize(600, 400);
            wnd.setLayout(new BorderLayout());
            //wnd.setLocation(75, 50);
            wnd.setBackground(Color.GREEN);
            
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
                        wnd.add(new Button(String.valueOf(i)), BorderLayout.WEST);
                        break;
                    }
                    case 1:{
                        wnd.add(new Button(String.valueOf(i)), BorderLayout.SOUTH);
                        break;
                    }
                    case 2:{
                        wnd.add(new Button(String.valueOf(i)), BorderLayout.EAST);
                        break;
                    }
                    default:
                        b.setLabel(String.valueOf(i));
                    	wnd.setVisible(false);
                    }
                    i++;
                    wnd.validate();
                }
            });
            wnd.add(b, BorderLayout.CENTER);
            b.setBackground(Color.YELLOW);
            
            final Button b2 = new Button("Left");
            final Scrollbar sb = new Scrollbar(Scrollbar.HORIZONTAL);
            final JCheckBox cb1 = new JCheckBox("Right");

            wnd.add(b2, BorderLayout.WEST);
            b2.setBackground(Color.RED);
            b2.addActionListener(new ActionListener() {
            	public void actionPerformed(ActionEvent e) {
            		System.out.println("Action on b2");
            		if (sb.getValue() + sb.getBlockIncrement() <= sb.getMaximum()) {
            			sb.setValue(sb.getValue() + sb.getBlockIncrement());
            		} else {
            			Frame f2 = new Frame("New frame");
            			f2.setSize(200, 100);
            			f2.show();
            		}
            	}
            });
            
            wnd.add(cb1, BorderLayout.EAST);
            cb1.setBackground(Color.WHITE);
                    
            final TextField tf = new TextField();
            tf.setText("Let's type here");
            wnd.add(tf, BorderLayout.SOUTH);
            
            System.out.println(wnd.getFont().getName());
            System.out.println(wnd.getFont().getClass().getName());
            System.out.println(wnd.getFontMetrics(wnd.getFont()).getClass().getName());
            
            wnd.show();

//            while (wnd.isVisible()) {
//            	Thread.sleep(500);
//            }
            

//            Font f = wnd.getFont();
//            System.out.println(f.getName());
//            wnd.getFontMetrics(f);

//            for (int i = 0; i < 30; i++) {
//            	wnd.setLocation(wnd.getX() + 5, wnd.getY() + 4);
//            	if ((i % 10) == 0) {
//            		cb1.setState(!cb1.getState());
//            		Thread.sleep(2500);
//            	} else {
//            		if ((i % 5) == 0) {
//            			// Intended mixing of width & height, just for the fun of the test
//            			wnd.setSize(wnd.getHeight(), wnd.getWidth());
//            		}
//            		Thread.sleep(100);
//            	}
//            }
            
//            Thread.sleep(5000);

//            wnd.hide();
        }catch(Throwable t){
            t.printStackTrace();
        } finally {
//            wnd.dispose();
        }
    }
}
