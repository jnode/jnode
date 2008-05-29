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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import org.jnode.awt.geom.PolyLine;
import org.jnode.driver.video.util.Curves;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PolyLineTest {

    public static void main(String[] args) throws Exception {
        Frame wnd = new Frame();
        try {

            //final double[] linePoints = { 200, 50, 200, 100, 100, 100, 100, 75, 200, 50 };
            //final double[] linePoints = { 200, 50, 200, 100, 100, 150, 100, 100, 200, 50 };
            final double[] linePoints = new double[42];
            Curves.calculateQuadCurve(50, 200, 100, 100, 150, 300, linePoints);

            wnd.setSize(600, 400);
            wnd.add(new TestComponent(linePoints));
            wnd.setVisible(true);

            System.in.read();
            //Thread.sleep(5000);

            wnd.setVisible(false);
        } finally {
            wnd.dispose();
        }
    }

    static class TestComponent extends Component {

        private final double[] linePoints;

        public TestComponent(double[] linePoints) {
            super();
            this.linePoints = linePoints;
        }

        public void paint(Graphics g) {
            System.out.println("Paint called");
            paintComponent(g);
        }

        private void paintComponent(Graphics g) {

            g.setColor(Color.GREEN);
            final Rectangle2D bounds = PolyLine.getBounds(linePoints);
            for (int row = 0; row < bounds.getHeight(); row++) {
                for (int col = 0; col < bounds.getWidth(); col++) {
                    final int x = (int) bounds.getX() + col;
                    final int y = (int) bounds.getY() + row;
                    if (PolyLine.contains(linePoints, x, y)) {
                        g.drawLine(x, y, x, y);
                    }
                }
            }

            g.setColor(Color.RED);
            final int cnt = linePoints.length;
            for (int i = 0; i < cnt - 2; i += 2) {
                g.drawLine((int) linePoints[i], (int) linePoints[i + 1], (int) linePoints[i + 2], (int) linePoints[i +
                    3]);
            }
        }
    }
}
