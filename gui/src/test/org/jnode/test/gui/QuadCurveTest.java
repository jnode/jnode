/*
 * $Id$
 */
package org.jnode.test.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

import org.jnode.driver.video.util.Curves;

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
			wnd.show();

			Thread.sleep(5000);

			wnd.hide();
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
			for (int i = 0; i < points.length-2; i+= 2) {		
				g.drawLine((int)points[i], (int)points[i+1], (int)points[i+2], (int)points[i+3]);
			}
		}

	}
}
