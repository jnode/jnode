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

import org.jnode.driver.video.util.Curves;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

/**
 * @author epr
 */
public class QuadCurveTest {

    public static void main(String[] args)
            throws Exception {
        Frame wnd = new Frame();
        try {
            wnd.setSize(600, 400);
            wnd.add(new TestComponent());
            wnd.setVisible(true);

            Thread.sleep(5000);

            wnd.setVisible(false);
        } finally {
            wnd.dispose();
        }
    }

    static class TestComponent extends Component {

        public void paint(Graphics g) {
            System.out.println("Paint called");
            paintComponent(g);
        }

        private void paintComponent(Graphics g) {
            final int w = getWidth();
            final int h = getHeight();
            g.setColor(Color.CYAN);
            final AffineTransform tx = new AffineTransform();
            tx.translate(50, h / 2);
            tx.scale(0.25, -0.25);
            final GeneralPath gp = new GeneralPath();
            gp.moveTo(w, 0);
            gp.quadTo(w, h, 0, h);
            gp.closePath();
            final Graphics2D g2d = (Graphics2D) g;
            g2d.draw(gp);
            g.setColor(Color.RED);
            drawQuadCurve(g, w, 0, w, h, 0, h);
        }

        protected void drawQuadCurve(Graphics g, double x0, double y0, double x1, double y1, double x2, double y2) {
            double[] points = new double[62];
            Curves.calculateQuadCurve(x0, y0, x1, y1, x2, y2, points);
            for (int i = 0; i < points.length - 2; i += 2) {
                g.drawLine((int) points[i], (int) points[i + 1], (int) points[i + 2], (int) points[i + 3]);
            }
        }

    }
}
