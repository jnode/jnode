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

package org.jnode.awt.geom;

import java.awt.geom.Rectangle2D;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class PolyLine {

    /**
     * Does the polyline described by the given line points contain
     * (x,y)
     *
     * @param linePoints
     * @param x
     * @param y
     * @return True if the point is contained, false otherwise
     */
    public static boolean contains(double[] linePoints, double x, double y) {
        int wind = 0;
        final int cnt = linePoints.length;
        double lastX = linePoints[cnt - 2];
        double lastY = linePoints[cnt - 1];
        int oldquad = whichquad(lastX, lastY, x, y); /* get starting angle */
        for (int i = 0; i < cnt; i += 2) { /* for each point in the polygon */
            final double thisX = linePoints[i];
            final double thisY = linePoints[i + 1];
            final int newquad = whichquad(thisX, thisY, x, y);
            if (oldquad != newquad) { /* adjust wind */
                /*
                     * use mod 4 comparsions to see if we have
                     * advanced or backed up one quadrant
                     */
                if (((oldquad + 1) & 3) == newquad)
                    wind++;
                else if (((newquad + 1) & 3) == oldquad) {
                    wind--;
                } else {
                    /*
                         * upper left to lower right, or
                         * upper right to lower left. Determine
                         * direction of winding  by intersection
                         *  with x==0.
                         */
                    double a = lastY - thisY;
                    a *= (x - lastX);
                    double b = lastX - thisX;
                    a += lastY * b;
                    b *= y;
                    if (a > b) {
                        wind += 2;
                    } else {
                        wind -= 2;
                    }
                    //System.out.println("x=" + x + "\ty=" + y + "\ta=" + a + "\tb=" + b + "\twind=" + wind);
                }
            }
            lastX = thisX;
            lastY = thisY;
            oldquad = newquad;
        }
        return (wind != 0); /* non zero means point in poly */

    }

    /**
     * Gets the bounding box of a PolyLine.
     *
     * @param linePoints
     * @return The calculated bounding box
     */
    public static Rectangle2D getBounds(double[] linePoints) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        final int cnt = linePoints.length;
        for (int i = 0; i < cnt; i += 2) {
            final double x = linePoints[i];
            final double y = linePoints[i + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        final double w = (maxX - minX) + 1;
        final double h = (maxY - minY) + 1;
        return new Rectangle2D.Double(minX, minY, w, h);
    }

    /**
     * Figure out which quadrent pt is in with respect to orig
     *
     * @param x
     * @param y
     * @param origX
     * @param origY
     * @return The quadrant
     */
    private static int whichquad(double x, double y, double origX, double origY) {
        final int quad;
        if (x < origX) {
            if (y < origY) {
                quad = 2;
            } else {
                quad = 1;
            }
        } else {
            if (y < origY) {
                quad = 3;
            } else {
                quad = 0;
            }
        }
        return quad;
    }
}
