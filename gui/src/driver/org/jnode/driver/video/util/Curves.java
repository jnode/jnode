/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.jnode.driver.video.util;

/**
 * Utility class for calculating various types of curves.
 * 
 * @author Ewout Prangsma
 */
public class Curves {

	/**
	 * Calculate the line points that make up the cubic curve 
	 * described by (x0,y0)-(x3,y3)
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param points The resulting points
	 */
	public static void calculateCubicCurve(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double points[]) {
		
		final int length = points.length;
		final int steps = (length / 2) - 1;
		final double incr = 1.0 / steps;

		int i = 0;
		double t = 0.0;
		for (int step = 0; step < steps; step++) {
			double f0 = (1 - t) * (1 - t) * (1 - t);
			double f1 = 3 * t * (1 - t) * (1 - t);
			double f2 = 3 * t * t * (1 - t);
			double f3 = t * t * t;
			double new_x = f0 * x0 + f1 * x1 + f2 * x2 + f3 * x3;
			double new_y = f0 * y0 + f1 * y1 + f2 * y2 + f3 * y3;
			points[i++] = new_x;
			points[i++] = new_y;
			t += incr;
		}
		points[i++] = x3;
		points[i++] = y3;
	}

	/**
	 * Calculate the line points that make up the quadratic parametric curve
	 * described by (x0,y0)-(x2,y2)
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param points The resulting points
	 */
	public static void calculateQuadCurve(double x0, double y0, double x1, double y1, double x2, double y2, double points[]) {
		
		final int length = points.length;
		final int steps = (length / 2) - 1;
		final double incr = 1.0 / steps;

		int i = 0;
		double t = 0.0;
		for (int step = 0; step < steps; step++) {
			double f0 = (1 - t) * (1 - t);
			double f1 = 2 * t * (1 - t);
			double f2 = t * t;
			double new_x = f0 * x0 + f1 * x1 + f2 * x2;
			double new_y = f0 * y0 + f1 * y1 + f2 * y2;
			points[i++] = new_x;
			points[i++] = new_y;
			t += incr;
		}
		points[i++] = x2;
		points[i++] = y2;
	}

}
