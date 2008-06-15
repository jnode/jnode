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

package org.jnode.driver.video.util;

/**
 * Utility class for calculating various types of curves.
 * 
 * @author Ewout Prangsma
 * @author peda
 */
public class Curves {

    private static final int steps = 19;

    private static final int steps2 = steps * 2;

    private static final double subdiv_step = 1.0 / (steps + 1);
    private static final double subdiv_step2 = subdiv_step * subdiv_step;
    private static final double subdiv_step3 = subdiv_step2 * subdiv_step;

    private static final double subdiv_mul3 = 3.0 * subdiv_step;
    private static final double subdiv2_mul3 = 3.0 * subdiv_step2;
    private static final double subdiv2_mul6 = 6.0 * subdiv_step2;
    private static final double subdiv3_mul6 = 6.0 * subdiv_step3;

    private static final double subdiv_mul2 = subdiv_step * 2;
    private static final double subdiv2_mul2 = subdiv_step2 * 2;

    /**
     * Calculate the line points that make up the cubic curve described by
     * (x0,y0)-(x3,y3)
     * 
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
    public static int calculateCubicCurve(double x0, double y0, double x1, double y1, double x2,
            double y2, double x3, double y3, double points[]) {

        final int length = 42;
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

        return length;
    }

    /**
     * tried to eliminate the need of mult within the loop
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param points The resulting points
     * @return int number of points in points array
     */
    public static int calculateCubicCurveOpt2(double x0, double y0, double x1, double y1,
            double x2, double y2, double x3, double y3, double[] points) {

        /*
         * first derivative of the curve at point 0
         */
        final double dpx = x1 - x0;
        final double dpy = y1 - y0;

        /*
         * second derivative of the curve at point 0
         */
        final double ddpx = x0 - x1 * 2.0 + x2;
        final double ddpy = y0 - y1 * 2.0 + y2;

        /*
         * third derivative of the curve at point 0
         */
        final double dddpx = (x1 - x2) * 3.0 - x0 + x3;
        final double dddpy = (y1 - y2) * 3.0 - y0 + y3;

        double fx = x0;
        double fy = y0;

        double dfx = dpx * subdiv_mul3 + ddpx * subdiv2_mul3 + dddpx * subdiv_step3;
        double dfy = dpy * subdiv_mul3 + ddpy * subdiv2_mul3 + dddpy * subdiv_step3;

        double ddfx = ddpx * subdiv2_mul6 + dddpx * subdiv3_mul6;
        double ddfy = ddpy * subdiv2_mul6 + dddpy * subdiv3_mul6;

        final double dddfx = dddpx * subdiv3_mul6;
        final double dddfy = dddpy * subdiv3_mul6;

        points[0] = x0;
        points[1] = y0;

        for (int i = 1; i <= steps; i++) {
            fx += dfx;
            fy += dfy;
            dfx += ddfx;
            dfy += ddfy;
            ddfx += dddfx;
            ddfy += dddfy;

            points[i << 1] = fx;
            points[(i << 1) + 1] = fy;
        }

        points[steps2 + 2] = x3;
        points[steps2 + 3] = y3;

        return 42;
    }

    /**
     * Calculate the line points that make up the quadratic parametric curve
     * described by (x0,y0)-(x2,y2)
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param points The resulting points
     */
    public static int calculateQuadCurve(double x0, double y0, double x1, double y1, double x2,
            double y2, double points[]) {

        final int length = 42;
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

        return length;
    }

    /**
     * try to not do any mul within the loop
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param points
     * @param points The resulting points
     * @return int number of points in points array
     */
    public static int calculateQuadCurveOpt2(double x0, double y0, double x1, double y1, double x2,
            double y2, double points[]) {

        /*
         * first derivatives of the Curve at point 0
         */
        final double dpx = x1 - x0;
        final double dpy = y1 - y0;

        /*
         * second derivatives of the Curve at point 0
         */
        final double ddpx = x2 + x0 - 2 * x1;
        final double ddpy = y2 + y0 - 2 * y1;

        /*
         * the endpoints of the line
         */
        double fx = x0;
        double fy = y0;

        double dfx = subdiv_mul2 * dpx + subdiv_step2 * ddpx;
        double dfy = subdiv_mul2 * dpy + subdiv_step2 * ddpy;

        final double ddfx = ddpx * subdiv2_mul2;
        final double ddfy = ddpy * subdiv2_mul2;

        points[0] = x0;
        points[1] = y0;

        for (int i = 1; i <= steps; i++) {
            fx += dfx;
            fy += dfy;
            dfx += ddfx;
            dfy += ddfy;

            points[i << 1] = fx;
            points[(i << 1) + 1] = fy;
        }

        points[steps2 + 2] = x2;
        points[steps2 + 3] = y2;

        return 42;
    }

    /**
     * Subdivision by de'casteljau
     * 
     * This implementation may look nicer but it is much slower, so forget it
     * also I haven't been able to find a good estimation how many subdiv steps
     * we need
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param points The resulting points
     * 
     * public static GeneralPath calculateQuadCurveOpt1(double x0, double y0,
     * double x1, double y1, double x2, double y2) {
     * 
     * GeneralPath result = new GeneralPath(); result.moveTo((float) x0, (float)
     * y0); calculateQuadCurveRecursive(x0, y0, x1, y1, x2, y2, result);
     * 
     * return result; }
     * 
     * private static void calculateQuadCurveRecursive(double x0, double y0,
     * double x1, double y1, double x2, double y2, GeneralPath result) {
     *  // try to estimate if we have to do a further subdivision double dx =
     * x2-x0; double dy = y2-y0;
     *  /* double dist = Math.abs(((x1 - x2) * dy - (y1 - y2) * dx)); // if the
     * distance from point 1 to the line (0 to 2) is less then one pixel //
     * TODO: dist is not the distance as expected! if (dist < 2.0) {
     * result.addDoubles(x1, y1); result.addDoubles(x2, y2); return; }
     *  // this is an alternativ to the first aproximation, don't know which one
     * is faster or better yet :/ if ( ((dx*dx) + (dy*dy)) < 4 ) {
     * result.lineTo((float) x2, (float) y2); return; }
     *  // calculate mid-points // this is only fast if l1a compiler uses fscale
     * instead of fdiv!! double x01 = (x0 + x1) / 2; double y01 = (y0 + y1) / 2;
     * double x12 = (x1 + x2) / 2; double y12 = (y1 + y2) / 2;
     * 
     * double x012 = (x01 + x12) / 2; double y012 = (y01 + y12) / 2;
     * 
     * calculateQuadCurveRecursive(x0, y0, x01, y01, x012, y012, result);
     * calculateQuadCurveRecursive(x012, y012, x12, y12, x2, y2, result); }
     */
}
