/*
 * $Id$
 */
package org.jnode.test.gui;

import org.jnode.driver.video.util.Curves;

/**
 * @author epr
 */
public class CurvesTest {

	public static void main(String[] args) {
		
		double[] points = new double[2 * 20];
		Curves.calculateCubicCurve(0, 0, 20, 0, 100, 50, 100, 100, points);
		for (int i = 0; i < points.length; i += 2) {
			System.out.println("" + i + "\t" + points[i] + "\t" + points[i+1]);
		}
	}
}
