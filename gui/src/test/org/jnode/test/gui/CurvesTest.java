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
 
package org.jnode.test.gui;

import org.jnode.driver.video.util.Curves;

//import org.jnode.awt.geom.Curves;

/**
 * @author epr
 */
public class CurvesTest {

    public static void main(String[] args) {

        double[] points = new double[2 * 20];
        Curves.calculateCubicCurve(0, 0, 20, 0, 100, 50, 100, 100, points);
        for (int i = 0; i < points.length; i += 2) {
            System.out.println("" + i + "\t" + points[i] + "\t" + points[i + 1]);
        }
    }
}
