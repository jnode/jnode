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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

import org.jnode.driver.video.Surface;

/**
 * Abstract and generic implementation of a Surface.
 * 
 * @author epr
 */
public abstract class AbstractSurface implements Surface {

    protected int width;
    protected int height;

    private double[] curvesData = new double[200];

    public AbstractSurface(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @see org.jnode.driver.video.Surface#close()
     */
    public void close() {
        // Nothing to do here
    }

    /**
     * @see org.jnode.driver.video.Surface#draw(Shape, Shape, AffineTransform,
     *      Color, int)
     */
    public void draw(Shape shape, Shape clip, AffineTransform tx, Color color, int mode) {
        draw(shape, clip, tx, convertColor(color), mode);
    }

    /**
     * Fill the given shape with the given color
     * 
     * @param shape The shape to fill
     * @param clip The clipping area, can be null
     * @param trans The transformation to be applied to shape &amp; clip.
     * @param color
     * @param mode
     */
    public void fill(Shape shape, Shape clip, AffineTransform trans, Color color, int mode) {
        final int c = convertColor(color);

        final double tx;
        final double ty;

        final Shape txShape;
        final Shape txClip;
        if (trans == null) {
            tx = 0.0;
            ty = 0.0;
            txShape = shape;
            txClip = clip;
        } else if (trans.getType() == AffineTransform.TYPE_IDENTITY) {
            tx = 0.0;
            ty = 0.0;
            txShape = shape;
            txClip = clip;
        } else if (trans.getType() == AffineTransform.TYPE_TRANSLATION) {
            tx = trans.getTranslateX();
            ty = trans.getTranslateY();
            txShape = shape;
            txClip = clip;
        } else {
            tx = 0.0;
            ty = 0.0;
            final GeneralPath gp = new GeneralPath();
            gp.append(shape.getPathIterator(trans), false);
            txShape = gp;
            if (clip != null) {
                final GeneralPath gpClip = new GeneralPath();
                gp.append(clip.getPathIterator(trans), false);
                txClip = gpClip;
            } else {
                txClip = null;
            }
        }

        Rectangle bounds = txShape.getBounds();
        if (txShape instanceof Rectangle2D) {
            if (txClip != null) {
                bounds = bounds.createIntersection(txClip.getBounds2D()).getBounds();
            }
            if ((bounds.width > 0) && (bounds.height > 0)) {
                bounds.translate((int) tx, (int) ty);
                fillRect(bounds.x, bounds.y, bounds.width, bounds.height, c, mode);
            }
        } else {
            // todo optimize for the ellipse
            for (int row = 0; row < bounds.height; row++) {
                final int y = bounds.y + row;
                for (int col = 0; col < bounds.width; col++) {
                    final int x = bounds.x + col;
                    if (txShape.contains(x, y)) {
                        if ((txClip == null) || txClip.contains(x, y)) {
                            drawPixel((int) (tx + x), (int) (ty + y), c, mode);
                        }
                    }
                }
            }
        }
    }

    /**
     * Fill a rectangle with the given color.
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     * @param color
     * @param mode
     */
    public void fillRect(int x, int y, int w, int h, int color, int mode) {
        for (int row = 0; row < h; row++) {
            drawLine(x, y + row, x + w, y + row, color, mode);
        }
    }

    /**
     * Set a pixel at the given location
     * 
     * @param x
     * @param y
     * @param color
     * @param mode
     */
    protected abstract void drawPixel(int x, int y, int color, int mode);

    /**
     * Draw a line between (x1,y1) and (x2,y2)
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param color
     * @param mode
     */
    public void drawLine(int x1, int y1, int x2, int y2, int color, int mode) {
        final int incx, incy;
        int countx, county;

        // log.debug("AS.drawLine " + x1 + "," + y1 + "," + x2 + ","+ y2);

        final int sizex = Math.abs(x2 - x1);
        final int sizey = Math.abs(y2 - y1);
        if (x1 > x2) {
            incx = -1;
        } else {
            incx = 1;
        }
        if (y1 > y2) {
            incy = -1;
        } else {
            incy = 1;
        }
        countx = x1;
        county = y1;

        drawPixel(x1, y1, color, mode);
        if (sizex >= sizey) {
            int y = sizex >> 1;
            final int loops = sizex - 1;
            for (int i = 0; i < loops; i++) {
                y += sizey;
                if (y >= sizex) {
                    y -= sizex;
                    county += incy;
                }
                countx += incx;
                drawPixel(countx, county, color, mode);
            }
        } else {
            int x = sizey >> 1;
            final int loops = sizey - 1;
            for (int i = 0; i < loops; i++) {
                x += sizex;
                if (x >= sizey) {
                    x -= sizey;
                    countx += incx;
                }
                county += incy;
                drawPixel(countx, county, color, mode);
            }
        }
    }

    /**
     * Draw a bezier curve
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param color
     * @param mode
     */
    protected void drawCurve(double x0, double y0, double x1, double y1, double x2, double y2,
            double x3, double y3, int color, int mode) {
        // final double[] points = new double[42];
        // Curves.calculateCubicCurve(x0, y0, x1, y1, x2, y2, x3, y3, points);
        int length = Curves.calculateCubicCurveOpt2(x0, y0, x1, y1, x2, y2, x3, y3, curvesData);
        for (int i = 0; i < length - 2; i += 2) {
            drawLine((int) curvesData[i], (int) curvesData[i + 1], (int) curvesData[i + 2],
                    (int) curvesData[i + 3], color, mode);
        }
    }

    /**
     * Draw a quadratic parametric curve
     * 
     * @param x0
     * @param y0
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param color
     * @param mode
     */
    protected void drawQuadCurve(double x0, double y0, double x1, double y1, double x2, double y2,
            int color, int mode) {
        // final double[] points = new double[42];
        // Curves.calculateQuadCurve(x0, y0, x1, y1, x2, y2, points);
        int length = Curves.calculateQuadCurveOpt2(x0, y0, x1, y1, x2, y2, curvesData);
        for (int i = 0; i < length - 2; i += 2) {
            drawLine((int) curvesData[i], (int) curvesData[i + 1], (int) curvesData[i + 2],
                    (int) curvesData[i + 3], color, mode);
        }
    }

    /**
     * Draw the given shape
     * 
     * @param shape
     * @param clip
     * @param color
     * @param mode
     */
    protected final void draw(Shape shape, Shape clip, AffineTransform trans, int color, int mode) {

        // log.debug("Draw " + shape + ", " + trans);

        final double[] coords = new double[6];
        double moveX = 0;
        double moveY = 0;
        double curX = 0;
        double curY = 0;

        final double tx;
        final double ty;

        if (clip != null) {
            // Very rough for now, but let's see
            if (!clip.contains(shape.getBounds2D())) {
                // TODO fix clipping
                // return;
            }
        }

        final PathIterator i;
        if (trans == null) {
            tx = 0.0;
            ty = 0.0;
            i = shape.getPathIterator(null);
        } else if (trans.getType() == AffineTransform.TYPE_IDENTITY) {
            tx = 0.0;
            ty = 0.0;
            i = shape.getPathIterator(null);
        } else if (trans.getType() == AffineTransform.TYPE_TRANSLATION) {
            tx = trans.getTranslateX();
            ty = trans.getTranslateY();
            i = shape.getPathIterator(null);
        } else {
            tx = 0.0;
            ty = 0.0;
            i = shape.getPathIterator(trans);
        }

        // log.debug("Draw");

        while (!i.isDone()) {
            final int type = i.currentSegment(coords);
            int x;
            int y;
            // log.debug("Segment type " + type);
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    curX = moveX = coords[0];
                    curY = moveY = coords[1];
                    break;
                case PathIterator.SEG_LINETO:
                    x = (int) coords[0];
                    y = (int) coords[1];
                    drawLine((int) (curX + tx), (int) (curY + ty), (int) (x + tx), (int) (y + ty),
                            color, mode);
                    curX = x;
                    curY = y;
                    break;
                case PathIterator.SEG_CLOSE:
                    drawLine((int) (moveX + tx), (int) (moveY + ty), (int) (curX + tx),
                            (int) (curY + ty), color, mode);
                    break;
                case PathIterator.SEG_CUBICTO:
                    // log.debug("CUBICTO ");
                    drawCurve(curX + tx, curY + ty, coords[0] + tx, coords[1] + ty, coords[2] + tx,
                            coords[3] + ty, coords[4] + tx, coords[5] + ty, color, mode);
                    curX = (int) coords[4];
                    curY = (int) coords[5];
                    break;
                case PathIterator.SEG_QUADTO:
                    drawQuadCurve(curX + tx, curY + ty, coords[0] + tx, coords[1] + ty, coords[2] +
                            tx, coords[3] + ty, color, mode);
                    curX = (int) coords[2];
                    curY = (int) coords[3];
                    break;
            }
            i.next();
        }
        // log.debug("Draw done");
    }

    /**
     * Convert the given color to an int representation
     * 
     * @param color
     */
    protected abstract int convertColor(Color color);

    /**
     * @see org.jnode.driver.video.Surface#getRGBPixel(int, int)
     */
    public int getRGBPixel(int x, int y) {
        throw new RuntimeException("getRGBPixel(int,int) is not implemented for this video driver");
    }

    /**
     * Sets the color of the pixel at the specified screen coordinates.
     * 
     * @param x the x coordinate
     * @param y the y coordinate
     * @param color the new color of the pixel
     */
    public void setRGBPixel(int x, int y, int color) {
        drawPixel(x, y, color, PAINT_MODE);
    }

    /**
     * @see org.jnode.driver.video.Surface#getRGBPixels(java.awt.Rectangle)
     */
    public int[] getRGBPixels(Rectangle region) {
        throw new RuntimeException(
                "getRGBPixels(Rectangle) is not implemented for this video driver");
    }

    protected void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * @return The height of this surface
     */
    protected final int getHeight() {
        return this.height;
    }

    /**
     * @return The width of this surface
     */
    protected final int getWidth() {
        return this.width;
    }

    protected final Rectangle getBounds(Shape shape, AffineTransform trans) {
        if (trans == null) {
            return shape.getBounds();
        } else if (trans.getType() == AffineTransform.TYPE_IDENTITY) {
            return shape.getBounds();
        } else if (trans.getType() == AffineTransform.TYPE_TRANSLATION) {
            Rectangle b = shape.getBounds();
            b.x += trans.getTranslateX();
            b.y += trans.getTranslateY();
            return b;
        } else {
            final GeneralPath gp = new GeneralPath();
            gp.append(shape.getPathIterator(trans), false);
            return gp.getBounds();
        }
    }

    public void update(int x, int y, int width, int height) {
        // do nothing
    }
}
