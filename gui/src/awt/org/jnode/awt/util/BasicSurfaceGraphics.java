/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.awt.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.CropImageFilter;
import java.awt.image.DirectColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.text.AttributedCharacterIterator;
import org.jnode.awt.JNodeToolkit;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;
import sun.awt.image.ToolkitImage;

/**
 * @author Levente S\u00e1ntha
 */
public class BasicSurfaceGraphics extends BasicGraphics {
    private final AbstractSurface surface;
    private int mode = Surface.PAINT_MODE;

    protected BasicSurfaceGraphics(AbstractSurface surface) {
        this.surface = surface;
    }

    protected BasicSurfaceGraphics(BasicSurfaceGraphics g) {
        super(g);
        this.surface = g.surface;
        this.mode = g.mode;
    }

    /**
     * Clears the specified rectangle by filling it with the background
     * color of the current drawing surface. This operation does not
     * use the current paint mode.
     * <p/>
     * Beginning with Java&nbsp;1.1, the background color
     * of offscreen images may be system dependent. Applications should
     * use <code>setColor</code> followed by <code>fillRect</code> to
     * ensure that an offscreen image is cleared to a specific color.
     *
     * @param x      the <i>x</i> coordinate of the rectangle to clear.
     * @param y      the <i>y</i> coordinate of the rectangle to clear.
     * @param width  the width of the rectangle to clear.
     * @param height the height of the rectangle to clear.
     * @see java.awt.Graphics#fillRect(int, int, int, int)
     * @see java.awt.Graphics#drawRect
     * @see java.awt.Graphics#setColor(java.awt.Color)
     * @see java.awt.Graphics#setPaintMode
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    public void clearRect(int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        _transform(r);
        if (clip != null)
            r = clip.intersection(r);

        surface.fillRect(r.x, r.y, r.width, r.height, Color.BLACK.getRGB(), Surface.PAINT_MODE);
        surface.update(r.x, r.y, r.width, r.height);
    }

    /**
     * Copies an area of the component by a distance specified by
     * <code>dx</code> and <code>dy</code>. From the point specified
     * by <code>x</code> and <code>y</code>, this method
     * copies downwards and to the right.  To copy an area of the
     * component to the left or upwards, specify a negative value for
     * <code>dx</code> or <code>dy</code>.
     * If a portion of the source rectangle lies outside the bounds
     * of the component, or is obscured by another window or component,
     * <code>copyArea</code> will be unable to copy the associated
     * pixels. The area that is omitted can be refreshed by calling
     * the component's <code>paint</code> method.
     *
     * @param x      the <i>x</i> coordinate of the source rectangle.
     * @param y      the <i>y</i> coordinate of the source rectangle.
     * @param width  the width of the source rectangle.
     * @param height the height of the source rectangle.
     * @param dx     the horizontal distance to copy the pixels.
     * @param dy     the vertical distance to copy the pixels.
     */
    public void copyArea(final int x, final int y, final int width, final int height,
                         final int dx, final int dy) {
        //source area
        Rectangle sr = new Rectangle(x, y, width, height);
        _transform(sr);
        if (clip != null)
            sr = clip.intersection(sr);

        //destination area
        Rectangle dr = new Rectangle(x + dx, y + dy, width, height);
        _transform(dr);
        if (clip != null)
            dr = clip.intersection(dr);

        final int w = Math.min(sr.width, dr.width);
        final int h = Math.min(sr.height, dr.height);

        if (dr.x != sr.x + dx) sr.x = dr.x - dx;
        if (dr.y != sr.y + dy) sr.y = dr.y - dy;


        surface.copyArea(sr.x, sr.y, w, h, dx, dy);
    }

    /**
     * Draws the outline of a circular or elliptical arc
     * covering the specified rectangle.
     * <p/>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees, using the current color.
     * Angles are interpreted such that 0&nbsp;degrees
     * is at the 3&nbsp;o'clock position.
     * A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * <p/>
     * The center of the arc is the center of the rectangle whose origin
     * is (<i>x</i>,&nbsp;<i>y</i>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * <p/>
     * The resulting arc covers an area
     * <code>width&nbsp;+&nbsp;1</code> pixels wide
     * by <code>height&nbsp;+&nbsp;1</code> pixels tall.
     * <p/>
     * The angles are specified relative to the non-square extents of
     * the bounding rectangle such that 45 degrees always falls on the
     * line from the center of the ellipse to the upper right corner of
     * the bounding rectangle. As a result, if the bounding rectangle is
     * noticeably longer in one axis than the other, the angles to the
     * start and end of the arc segment will be skewed farther along the
     * longer axis of the bounds.
     *
     * @param x          the <i>x</i> coordinate of the
     *                   upper-left corner of the arc to be drawn.
     * @param y          the <i>y</i>  coordinate of the
     *                   upper-left corner of the arc to be drawn.
     * @param width      the width of the arc to be drawn.
     * @param height     the height of the arc to be drawn.
     * @param startAngle the beginning angle.
     * @param arcAngle   the angular extent of the arc,
     *                   relative to the start angle.
     * @see java.awt.Graphics#fillArc
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        //todo implement it
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.drawArc() not implemented\n");
    }

    /**
     * Draws as much of the specified area of the specified image as is
     * currently available, scaling it on the fly to fit inside the
     * specified area of the destination drawable surface. Transparent pixels
     * do not affect whatever pixels are already there.
     * <p/>
     * This method returns immediately in all cases, even if the
     * image area to be drawn has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that loads the image notifies
     * the specified image observer.
     * <p/>
     * This method always uses the unscaled version of the image
     * to render the scaled rectangle and performs the required
     * scaling on the fly. It does not use a cached, scaled version
     * of the image for this operation. Scaling of the image from source
     * to destination is performed such that the first coordinate
     * of the source rectangle is mapped to the first coordinate of
     * the destination rectangle, and the second source coordinate is
     * mapped to the second destination coordinate. The subimage is
     * scaled and flipped as needed to preserve those mappings.
     *
     * @param img      the specified image to be drawn. This method does
     *                 nothing if <code>img</code> is null.
     * @param dx1      the <i>x</i> coordinate of the first corner of the
     *                 destination rectangle.
     * @param dy1      the <i>y</i> coordinate of the first corner of the
     *                 destination rectangle.
     * @param dx2      the <i>x</i> coordinate of the second corner of the
     *                 destination rectangle.
     * @param dy2      the <i>y</i> coordinate of the second corner of the
     *                 destination rectangle.
     * @param sx1      the <i>x</i> coordinate of the first corner of the
     *                 source rectangle.
     * @param sy1      the <i>y</i> coordinate of the first corner of the
     *                 source rectangle.
     * @param sx2      the <i>x</i> coordinate of the second corner of the
     *                 source rectangle.
     * @param sy2      the <i>y</i> coordinate of the second corner of the
     *                 source rectangle.
     * @param observer object to be notified as more of the image is
     *                 scaled and converted.
     * @return <code>false</code> if the image pixels are still changing;
     *         <code>true</code> otherwise.
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since JDK1.1
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                             ImageObserver observer) {

        return img == null || drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
    }

    /**
     * Draws as much of the specified area of the specified image as is
     * currently available, scaling it on the fly to fit inside the
     * specified area of the destination drawable surface.
     * <p/>
     * Transparent pixels are drawn in the specified background color.
     * This operation is equivalent to filling a rectangle of the
     * width and height of the specified image with the given color and then
     * drawing the image on top of it, but possibly more efficient.
     * <p/>
     * This method returns immediately in all cases, even if the
     * image area to be drawn has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that loads the image notifies
     * the specified image observer.
     * <p/>
     * This method always uses the unscaled version of the image
     * to render the scaled rectangle and performs the required
     * scaling on the fly. It does not use a cached, scaled version
     * of the image for this operation. Scaling of the image from source
     * to destination is performed such that the first coordinate
     * of the source rectangle is mapped to the first coordinate of
     * the destination rectangle, and the second source coordinate is
     * mapped to the second destination coordinate. The subimage is
     * scaled and flipped as needed to preserve those mappings.
     *
     * @param img      the specified image to be drawn. This method does
     *                 nothing if <code>img</code> is null.
     * @param dx1      the <i>x</i> coordinate of the first corner of the
     *                 destination rectangle.
     * @param dy1      the <i>y</i> coordinate of the first corner of the
     *                 destination rectangle.
     * @param dx2      the <i>x</i> coordinate of the second corner of the
     *                 destination rectangle.
     * @param dy2      the <i>y</i> coordinate of the second corner of the
     *                 destination rectangle.
     * @param sx1      the <i>x</i> coordinate of the first corner of the
     *                 source rectangle.
     * @param sy1      the <i>y</i> coordinate of the first corner of the
     *                 source rectangle.
     * @param sx2      the <i>x</i> coordinate of the second corner of the
     *                 source rectangle.
     * @param sy2      the <i>y</i> coordinate of the second corner of the
     *                 source rectangle.
     * @param bgcolor  the background color to paint under the
     *                 non-opaque portions of the image.
     * @param observer object to be notified as more of the image is
     *                 scaled and converted.
     * @return <code>false</code> if the image pixels are still changing;
     *         <code>true</code> otherwise.
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     * @since JDK1.1
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor, ImageObserver observer) {
        if (img == null)
            return true;

        if (dx1 == dx2 || dy1 == dy2 || sx1 == sx2 || sy1 == sy2)
            return true;


        final int widthImage;
        final int heightImage;
        final int xImage;
        final int yImage;
        if (sx2 > sx1) {
            widthImage = sx2 - sx1 + 1;
            xImage = sx1;
        } else {
            widthImage = sx1 - sx2 + 1;
            xImage = sx2;
        }

        if (sy2 > sy1) {
            heightImage = sy2 - sy1 + 1;
            yImage = sy1;
        } else {
            heightImage = sy1 - sy2 + 1;
            yImage = sy2;
        }

        final int widthDest;
        final int heightDest;
        final int xDest;
        final int yDest;
        if (dx2 > dx1) {
            widthDest = dx2 - dx1 + 1;
            xDest = dx1;
        } else {
            widthDest = dx1 - dx2 + 1;
            xDest = dx2;
        }

        if (dy2 > dy1) {
            heightDest = dy2 - dy1 + 1;
            yDest = dy1;
        } else {
            heightDest = dy1 - dy2 + 1;
            yDest = dy2;
        }


        final Image imageArea;
        if (img.getWidth(observer) == widthImage && img.getHeight(observer) == heightImage) {
            imageArea = img;
        } else {
            // Extract the image with a CropImageFilter
            imageArea = new ToolkitImage(new FilteredImageSource(img.getSource(),
                new CropImageFilter(xImage, yImage, widthImage, heightImage)));
        }

        if (widthImage == widthDest && heightImage == heightDest) {
            if (bgcolor == null) {
                return drawImage(imageArea, xDest, yDest, observer);
            } else {
                return drawImage(imageArea, xDest, yDest, bgcolor, observer);
            }
        } else {
            if (bgcolor == null) {
                return drawImage(imageArea, xDest, yDest, widthDest, heightDest, observer);
            } else {
                return drawImage(imageArea, xDest, yDest, widthDest, heightDest, bgcolor, observer);
            }
        }
    }

    /*
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                             Color bgcolor, ImageObserver observer) {
        if (img == null) return true;

        if (dx1 == dx2 || dy1 == dy2) {
            return true;
        }
        if (sx1 == sx2 || sy1 == sy2) {
            return true;
        }

        final int widthImage;
        final int heightImage;
        final int xImage;
        final int yImage;
        if (sx2 > sx1) {
            widthImage = sx2 - sx1 + 1;
            xImage = sx1;
        } else {
            widthImage = sx1 - sx2 + 1;
            xImage = sx2;
        }

        if (sy2 > sy1) {
            heightImage = sy2 - sy1 + 1;
            yImage = sy1;
        } else {
            heightImage = sy1 - sy2 + 1;
            yImage = sy2;
        }

        final int widthDest;
        final int heightDest;
        final int xDest;
        final int yDest;
        if (dx2 > dx1) {
            widthDest = dx2 - dx1 + 1;
            xDest = dx1;
        } else {
            widthDest = dx1 - dx2 + 1;
            xDest = dx2;
        }

        if (dy2 > dy1) {
            heightDest = dy2 - dy1 + 1;
            yDest = dy1;
        } else {
            heightDest = dy1 - dy2 + 1;
            yDest = dy2;
        }

        // Extract the image with a CropImageFilter
        final Image imageArea = new ToolkitImage(new FilteredImageSource(img.getSource(),
            new CropImageFilter(xImage, yImage, widthImage, heightImage)));

        if (bgcolor == null) {
            return drawImage(imageArea, xDest, yDest, widthDest, heightDest, observer);
        } else {
            return drawImage(imageArea, xDest, yDest, widthDest, heightDest, bgcolor, observer);
        }
    }
     */
    /**
     * Draws as much of the specified image as is currently available.
     * The image is drawn with its top-left corner at
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate
     * space.  Transparent pixels are drawn in the specified
     * background color.
     * <p/>
     * This operation is equivalent to filling a rectangle of the
     * width and height of the specified image with the given color and then
     * drawing the image on top of it, but possibly more efficient.
     * <p/>
     * This method returns immediately in all cases, even if the
     * complete image has not yet been loaded, and it has not been dithered
     * and converted for the current output device.
     * <p/>
     * If the image has completely loaded and its pixels are
     * no longer being changed, then
     * <code>drawImage</code> returns <code>true</code>.
     * Otherwise, <code>drawImage</code> returns <code>false</code>
     * and as more of
     * the image becomes available
     * or it is time to draw another frame of animation,
     * the process that loads the image notifies
     * the specified image observer.
     *
     * @param img      the specified image to be drawn. This method does
     *                 nothing if <code>img</code> is null.
     * @param x        the <i>x</i> coordinate.
     * @param y        the <i>y</i> coordinate.
     * @param bgcolor  the background color to paint under the
     *                 non-opaque portions of the image.
     * @param observer object to be notified as more of
     *                 the image is converted.
     * @return <code>false</code> if the image pixels are still changing;
     *         <code>true</code> otherwise.
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        if (img == null) return true;
        try {
            Raster rast = getCompatibleRaster(img);
            Rectangle r = new Rectangle(x, y, rast.getWidth(), rast.getHeight());
            _transform(r);
            final int tx = r.x, ty = r.y;
            if (clip != null)
                r = clip.intersection(r);

            if (!r.isEmpty()) {
                surface.drawCompatibleRaster(rast, r.x - tx, r.y - ty, r.x, r.y, r.width, r.height, bgcolor);
                surface.update(r.x, r.y, r.width, r.height);
            }
            return true;
        } catch (InterruptedException ie) {
            return false;
        }
    }

    /**
     * Draws as much of the specified image as is currently available.
     * The image is drawn with its top-left corner at
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate
     * space. Transparent pixels in the image do not affect whatever
     * pixels are already there.
     * <p/>
     * This method returns immediately in all cases, even if the
     * complete image has not yet been loaded, and it has not been dithered
     * and converted for the current output device.
     * <p/>
     * If the image has completely loaded and its pixels are
     * no longer being changed, then
     * <code>drawImage</code> returns <code>true</code>.
     * Otherwise, <code>drawImage</code> returns <code>false</code>
     * and as more of
     * the image becomes available
     * or it is time to draw another frame of animation,
     * the process that loads the image notifies
     * the specified image observer.
     *
     * @param img      the specified image to be drawn. This method does
     *                 nothing if <code>img</code> is null.
     * @param x        the <i>x</i> coordinate.
     * @param y        the <i>y</i> coordinate.
     * @param observer object to be notified as more of
     *                 the image is converted.
     * @return <code>false</code> if the image pixels are still changing;
     *         <code>true</code> otherwise.
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        if (img == null) return true;
        try {
            Raster rast = getCompatibleRaster(img);
            Rectangle r = new Rectangle(x, y, rast.getWidth(), rast.getHeight());
            _transform(r);
            final int tx = r.x, ty = r.y;
            if (clip != null)
                r = clip.intersection(r);

            if (!r.isEmpty()) {
                surface.drawCompatibleRaster(rast, r.x - tx, r.y - ty, r.x, r.y, r.width, r.height, null);
                surface.update(r.x, r.y, r.width, r.height);
            }
            return true;
        } catch (InterruptedException ie) {
            return false;
        }
    }

    /**
     * Draws as much of the specified image as has already been scaled
     * to fit inside the specified rectangle.
     * <p/>
     * The image is drawn inside the specified rectangle of this
     * graphics context's coordinate space, and is scaled if
     * necessary. Transparent pixels are drawn in the specified
     * background color.
     * This operation is equivalent to filling a rectangle of the
     * width and height of the specified image with the given color and then
     * drawing the image on top of it, but possibly more efficient.
     * <p/>
     * This method returns immediately in all cases, even if the
     * entire image has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that loads the image notifies
     * the specified image observer.
     * <p/>
     * A scaled version of an image will not necessarily be
     * available immediately just because an unscaled version of the
     * image has been constructed for this output device.  Each size of
     * the image may be cached separately and generated from the original
     * data in a separate image production sequence.
     *
     * @param img      the specified image to be drawn. This method does
     *                 nothing if <code>img</code> is null.
     * @param x        the <i>x</i> coordinate.
     * @param y        the <i>y</i> coordinate.
     * @param width    the width of the rectangle.
     * @param height   the height of the rectangle.
     * @param bgcolor  the background color to paint under the
     *                 non-opaque portions of the image.
     * @param observer object to be notified as more of
     *                 the image is converted.
     * @return <code>false</code> if the image pixels are still changing;
     *         <code>true</code> otherwise.
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        if (img == null)
            return true;

        if (img.getWidth(observer) == width && img.getHeight(observer) == height)
            return drawImage(img, x, y, bgcolor, observer);

        return drawImage(new ToolkitImage(new FilteredImageSource(img.getSource(),
            new AreaAveragingScaleFilter(width, height))), x, y, bgcolor, observer);
    }

    /**
     * Draws as much of the specified image as has already been scaled
     * to fit inside the specified rectangle.
     * <p/>
     * The image is drawn inside the specified rectangle of this
     * graphics context's coordinate space, and is scaled if
     * necessary. Transparent pixels do not affect whatever pixels
     * are already there.
     * <p/>
     * This method returns immediately in all cases, even if the
     * entire image has not yet been scaled, dithered, and converted
     * for the current output device.
     * If the current output representation is not yet complete, then
     * <code>drawImage</code> returns <code>false</code>. As more of
     * the image becomes available, the process that loads the image notifies
     * the image observer by calling its <code>imageUpdate</code> method.
     * <p/>
     * A scaled version of an image will not necessarily be
     * available immediately just because an unscaled version of the
     * image has been constructed for this output device.  Each size of
     * the image may be cached separately and generated from the original
     * data in a separate image production sequence.
     *
     * @param img      the specified image to be drawn. This method does
     *                 nothing if <code>img</code> is null.
     * @param x        the <i>x</i> coordinate.
     * @param y        the <i>y</i> coordinate.
     * @param width    the width of the rectangle.
     * @param height   the height of the rectangle.
     * @param observer object to be notified as more of
     *                 the image is converted.
     * @return <code>false</code> if the image pixels are still changing;
     *         <code>true</code> otherwise.
     * @see java.awt.Image
     * @see java.awt.image.ImageObserver
     * @see java.awt.image.ImageObserver#imageUpdate(java.awt.Image, int, int, int, int, int)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        if (img == null)
            return true;

        if (img.getWidth(observer) == width && img.getHeight(observer) == height)
            return drawImage(img, x, y, observer);

        return drawImage(new ToolkitImage(new FilteredImageSource(img.getSource(),
            new AreaAveragingScaleFilter(width, height))), x, y, observer);
    }

    /**
     * Draws a line, using the current color, between the points
     * <code>(x1,&nbsp;y1)</code> and <code>(x2,&nbsp;y2)</code>
     * in this graphics context's coordinate system.
     *
     * @param x1 the first point's <i>x</i> coordinate.
     * @param y1 the first point's <i>y</i> coordinate.
     * @param x2 the second point's <i>x</i> coordinate.
     * @param y2 the second point's <i>y</i> coordinate.
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        x1 += origin.x;
        y1 += origin.y;
        x2 += origin.x;
        y2 += origin.y;

        if (clip == null || (clip.contains(x1, y1) && clip.contains(x2, y2))) {
            surface.drawLine(x1, y1, x2, y2, color.getRGB(), mode);
        } else {
            //todo optimize clipping
            //copute clipped line coordinates with the Liang-Barsjky alogorithm
            drawLineClipped(x1, x2, y1, y2);
        }

        if (x1 == x2) {
            if (y1 == y2)
                surface.update(x1, y1, 2, 2);
            else if (y1 < y2)
                surface.update(x1, y1, 2, y2 - y1 + 2);
            else
                surface.update(x1, y2, 2, y1 - y2 + 2);

        } else if (y1 == y2) {
            if (x1 < x2)
                surface.update(x1, y1, x2 - x1 + 2, 2);
            else
                surface.update(x2, y1, x1 - x2 + 2, 2);
        } else {
            if (x1 < x2 && y1 < y2)
                surface.update(x1, y1, x2 - x1 + 2, y2 - y1 + 2);
            else if (x1 < x2 && y1 > y2)
                surface.update(x1, y2, x2 - x1 + 2, y1 - y2 + 2);
            else if (x1 > x2 && y1 < y2)
                surface.update(x2, y1, x1 - x2 + 2, y2 - y1 + 2);
            else if (x1 > x2 && y1 > y2)
                surface.update(x2, y2, x1 - x2 + 2, y1 - y2 + 2);
        }
    }

    private void drawLineClipped(int x1, int x2, int y1, int y2) {
        final int incx, incy;
        int countx, county;

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

        drawPixelClipped(x1, y1);
        if (sizex >= sizey) {
            int y = sizex >> 1;
            final int loops = sizex;
            for (int i = 0; i < loops; i++) {
                y += sizey;
                if (y >= sizex) {
                    y -= sizex;
                    county += incy;
                }
                countx += incx;
                drawPixelClipped(countx, county);
            }
        } else {
            int x = sizey >> 1;
            final int loops = sizey;
            for (int i = 0; i < loops; i++) {
                x += sizex;
                if (x >= sizey) {
                    x -= sizey;
                    countx += incx;
                }
                county += incy;
                drawPixelClipped(countx, county);
            }
        }
    }

    private void drawPixelClipped(int x, int y) {
        if (clip == null || clip.contains(x, y))
            surface.drawPixel(x, y, color.getRGB(), mode);
    }


    /**
     * Draws the outline of an oval.
     * The result is a circle or ellipse that fits within the
     * rectangle specified by the <code>x</code>, <code>y</code>,
     * <code>width</code>, and <code>height</code> arguments.
     * <p/>
     * The oval covers an area that is
     * <code>width&nbsp;+&nbsp;1</code> pixels wide
     * and <code>height&nbsp;+&nbsp;1</code> pixels tall.
     *
     * @param x      the <i>x</i> coordinate of the upper left
     *               corner of the oval to be drawn.
     * @param y      the <i>y</i> coordinate of the upper left
     *               corner of the oval to be drawn.
     * @param width  the width of the oval to be drawn.
     * @param height the height of the oval to be drawn.
     * @see java.awt.Graphics#fillOval
     */
    public void drawOval(int x, int y, int width, int height) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.drawOval() Not implemented\n");
    }

    /**
     * Draws a closed polygon defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * <p/>
     * This method draws the polygon defined by <code>nPoint</code> line
     * segments, where the first <code>nPoint&nbsp;-&nbsp;1</code>
     * line segments are line segments from
     * <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * to <code>(xPoints[i],&nbsp;yPoints[i])</code>, for
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>.
     * The figure is automatically closed by drawing a line connecting
     * the final point to the first point, if those points are different.
     *
     * @param xPoints a an array of <code>x</code> coordinates.
     * @param yPoints a an array of <code>y</code> coordinates.
     * @param nPoints a the total number of points.
     * @see java.awt.Graphics#fillPolygon
     * @see java.awt.Graphics#drawPolyline
     */
    public void drawPolygon(int xPoints[], int yPoints[], int nPoints) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.drawPlygon() Not implemented\n");
    }

    /**
     * Draws a sequence of connected lines defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * Each pair of (<i>x</i>,&nbsp;<i>y</i>) coordinates defines a point.
     * The figure is not closed if the first point
     * differs from the last point.
     *
     * @param xPoints an array of <i>x</i> points
     * @param yPoints an array of <i>y</i> points
     * @param nPoints the total number of points
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     * @since JDK1.1
     */
    public void drawPolyline(int xPoints[], int yPoints[], int nPoints) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.drawPolyLine() Not implemented\n");
    }

    /**
     * Draws an outlined round-cornered rectangle using this graphics
     * context's current color. The left and right edges of the rectangle
     * are at <code>x</code> and <code>x&nbsp;+&nbsp;width</code>,
     * respectively. The top and bottom edges of the rectangle are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height</code>.
     *
     * @param x         the <i>x</i> coordinate of the rectangle to be drawn.
     * @param y         the <i>y</i> coordinate of the rectangle to be drawn.
     * @param width     the width of the rectangle to be drawn.
     * @param height    the height of the rectangle to be drawn.
     * @param arcWidth  the horizontal diameter of the arc
     *                  at the four corners.
     * @param arcHeight the vertical diameter of the arc
     *                  at the four corners.
     * @see java.awt.Graphics#fillRoundRect
     */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.drawRoundRect() Not implemented\n");
    }

    /**
     * Renders the text of the specified iterator applying its attributes
     * in accordance with the specification of the
     * {@link java.awt.font.TextAttribute TextAttribute} class.
     * <p/>
     * The baseline of the leftmost character is at position
     * (<i>x</i>,&nbsp;<i>y</i>) in this graphics context's coordinate system.
     *
     * @param iterator the iterator whose text is to be drawn
     * @param x        the <i>x</i> coordinate.
     * @param y        the <i>y</i> coordinate.
     * @throws NullPointerException if <code>iterator</code> is
     *                              <code>null</code>.
     * @see java.awt.Graphics#drawBytes
     * @see java.awt.Graphics#drawChars
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.drawString() Not implemented\n");
    }

    /**
     * Draws the text given by the specified string, using this
     * graphics context's current font and color. The baseline of the
     * leftmost character is at position (<i>x</i>,&nbsp;<i>y</i>) in this
     * graphics context's coordinate system.
     *
     * @param str the string to be drawn.
     * @param x   the <i>x</i> coordinate.
     * @param y   the <i>y</i> coordinate.
     * @throws NullPointerException if <code>str</code> is <code>null</code>.
     * @see java.awt.Graphics#drawBytes
     * @see java.awt.Graphics#drawChars
     */
    public void drawString(String str, final int x, final int y) {
        int xx = x + origin.x;
        int yy = y + origin.y;
        try {
            final Font font = getFont();
            if (font != null) {
                JNodeToolkit tk = ((JNodeToolkit) Toolkit.getDefaultToolkit());
                if (tk == null) {
                    System.err.println("Toolkit is null");
                    return;
                }
                if (tk.getFontManager() == null) {
                    System.err.println("FontManager is null");
                    return;
                }

                tk.getFontManager().drawText(surface, this.clip, null, str, font, xx, yy, getColor());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Fills a circular or elliptical arc covering the specified rectangle.
     * <p/>
     * The resulting arc begins at <code>startAngle</code> and extends
     * for <code>arcAngle</code> degrees.
     * Angles are interpreted such that 0&nbsp;degrees
     * is at the 3&nbsp;o'clock position.
     * A positive value indicates a counter-clockwise rotation
     * while a negative value indicates a clockwise rotation.
     * <p/>
     * The center of the arc is the center of the rectangle whose origin
     * is (<i>x</i>,&nbsp;<i>y</i>) and whose size is specified by the
     * <code>width</code> and <code>height</code> arguments.
     * <p/>
     * The resulting arc covers an area
     * <code>width&nbsp;+&nbsp;1</code> pixels wide
     * by <code>height&nbsp;+&nbsp;1</code> pixels tall.
     * <p/>
     * The angles are specified relative to the non-square extents of
     * the bounding rectangle such that 45 degrees always falls on the
     * line from the center of the ellipse to the upper right corner of
     * the bounding rectangle. As a result, if the bounding rectangle is
     * noticeably longer in one axis than the other, the angles to the
     * start and end of the arc segment will be skewed farther along the
     * longer axis of the bounds.
     *
     * @param x          the <i>x</i> coordinate of the
     *                   upper-left corner of the arc to be filled.
     * @param y          the <i>y</i>  coordinate of the
     *                   upper-left corner of the arc to be filled.
     * @param width      the width of the arc to be filled.
     * @param height     the height of the arc to be filled.
     * @param startAngle the beginning angle.
     * @param arcAngle   the angular extent of the arc,
     *                   relative to the start angle.
     * @see java.awt.Graphics#drawArc
     */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.fillArc() Not implemented\n");
    }

    /**
     * Fills an oval bounded by the specified rectangle with the
     * current color.
     *
     * @param x      the <i>x</i> coordinate of the upper left corner
     *               of the oval to be filled.
     * @param y      the <i>y</i> coordinate of the upper left corner
     *               of the oval to be filled.
     * @param width  the width of the oval to be filled.
     * @param height the height of the oval to be filled.
     * @see java.awt.Graphics#drawOval
     */
    public void fillOval(int x, int y, int width, int height) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.fillOval() Not implemented\n");
    }

    /**
     * Fills a closed polygon defined by
     * arrays of <i>x</i> and <i>y</i> coordinates.
     * <p/>
     * This method draws the polygon defined by <code>nPoint</code> line
     * segments, where the first <code>nPoint&nbsp;-&nbsp;1</code>
     * line segments are line segments from
     * <code>(xPoints[i&nbsp;-&nbsp;1],&nbsp;yPoints[i&nbsp;-&nbsp;1])</code>
     * to <code>(xPoints[i],&nbsp;yPoints[i])</code>, for
     * 1&nbsp;&le;&nbsp;<i>i</i>&nbsp;&le;&nbsp;<code>nPoints</code>.
     * The figure is automatically closed by drawing a line connecting
     * the final point to the first point, if those points are different.
     * <p/>
     * The area inside the polygon is defined using an
     * even-odd fill rule, also known as the alternating rule.
     *
     * @param xPoints a an array of <code>x</code> coordinates.
     * @param yPoints a an array of <code>y</code> coordinates.
     * @param nPoints a the total number of points.
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    public void fillPolygon(int xPoints[], int yPoints[], int nPoints) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.fillPolygon Not implemented\n");
    }

    /**
     * Fills the specified rectangle.
     * The left and right edges of the rectangle are at
     * <code>x</code> and <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>.
     * The top and bottom edges are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>.
     * The resulting rectangle covers an area
     * <code>width</code> pixels wide by
     * <code>height</code> pixels tall.
     * The rectangle is filled using the graphics context's current color.
     *
     * @param x      the <i>x</i> coordinate
     *               of the rectangle to be filled.
     * @param y      the <i>y</i> coordinate
     *               of the rectangle to be filled.
     * @param width  the width of the rectangle to be filled.
     * @param height the height of the rectangle to be filled.
     * @see java.awt.Graphics#clearRect
     * @see java.awt.Graphics#drawRect
     */
    public void fillRect(int x, int y, int width, int height) {
        Rectangle r = new Rectangle(x, y, width, height);
        _transform(r);
        if (clip != null)
            r = clip.intersection(r);
        try {
            surface.fillRect(r.x, r.y, r.width, r.height, color.getRGB(), Surface.PAINT_MODE);
            surface.update(r.x, r.y, r.width, r.height);
        } catch (Exception ex) {
            org.jnode.vm.Unsafe.debugStackTrace(ex);
        }
    }

    /**
     * Fills the specified rounded corner rectangle with the current color.
     * The left and right edges of the rectangle
     * are at <code>x</code> and <code>x&nbsp;+&nbsp;width&nbsp;-&nbsp;1</code>,
     * respectively. The top and bottom edges of the rectangle are at
     * <code>y</code> and <code>y&nbsp;+&nbsp;height&nbsp;-&nbsp;1</code>.
     *
     * @param x         the <i>x</i> coordinate of the rectangle to be filled.
     * @param y         the <i>y</i> coordinate of the rectangle to be filled.
     * @param width     the width of the rectangle to be filled.
     * @param height    the height of the rectangle to be filled.
     * @param arcWidth  the horizontal diameter
     *                  of the arc at the four corners.
     * @param arcHeight the vertical diameter
     *                  of the arc at the four corners.
     * @see java.awt.Graphics#drawRoundRect
     */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        //todo
        org.jnode.vm.Unsafe.debug("SimpleSurfaceGraphics.fillRoundRect() Not implemented\n");
    }

    /**
     * Creates a new <code>Graphics</code> object that is
     * a copy of this <code>Graphics</code> object.
     *
     * @return a new graphics context that is a copy of
     *         this graphics context.
     */
    public Graphics create() {
        return new BasicSurfaceGraphics(this);
    }

    /**
     * Gets the Raster of a given image.
     *
     * @param image
     * @return Raster
     * @throws InterruptedException
     */
    private Raster getCompatibleRaster(Image image) throws InterruptedException {
        final ColorModel dstModel = surface.getColorModel();
        if (image instanceof BufferedImage) {
            final BufferedImage b_image = (BufferedImage) image;
            // We have a direct raster
            final Raster raster = b_image.getRaster();
            if (dstModel.isCompatibleRaster(raster)) {
                // Raster is compatible, return without changes
                return raster;
            } else {
                // Convert it into a compatible raster
                return createCompatibleRaster(raster, b_image.getColorModel());
            }
        } else if (image instanceof RenderedImage) {
            final RenderedImage r_image = (RenderedImage) image;
            // We have a direct raster
            final Raster raster = r_image.getData();
            if (dstModel.isCompatibleRaster(raster)) {
                // Raster is compatible, return without changes
                return raster;
            } else {
                // Convert it into a compatible raster
                return createCompatibleRaster(raster, r_image.getColorModel());
            }
        } else {
            // Convert it to a raster
            final PixelGrabber grabber = new PixelGrabber(image, 0, 0, image.getWidth(null),
                image.getHeight(null), true);
            if (grabber.grabPixels(10000)) {
                final int w = grabber.getWidth();
                final int h = grabber.getHeight();
                final WritableRaster raster = dstModel.createCompatibleWritableRaster(w, h);
                final int[] pixels = (int[]) grabber.getPixels();
                Object dataElems = null;
                for (int y = 0; y < h; y++) {
                    final int ofsY = y * w;
                    for (int x = 0; x < w; x++) {
                        final int rgb = pixels[ofsY + x];
                        dataElems = dstModel.getDataElements(rgb, dataElems);
                        raster.setDataElements(x, y, dataElems);
                    }
                }
                return raster;
            } else {
                throw new IllegalArgumentException("Cannot grab pixels");
            }
        }
    }

    /**
     * Create a raster that is compatible with the surface and contains
     * data derived from the given raster.
     *
     * @param raster
     * @return the new raster
     */
    private Raster createCompatibleRaster(Raster raster, ColorModel model) {

        //todo optimize
        final ColorModel dst_model = surface.getColorModel();
        final int[] samples = new int[4];
        final int w = raster.getWidth();
        final int h = raster.getHeight();
        final WritableRaster dst_raster = dst_model.createCompatibleWritableRaster(w, h);

        if (dst_model instanceof DirectColorModel)
            if (model instanceof DirectColorModel) {
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++)
                        dst_raster.setPixel(x, y, raster.getPixel(x, y, samples));
            } else if (model instanceof ComponentColorModel) {
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++)
                        dst_raster.setPixel(x, y, raster.getPixel(x, y, samples));
            } else if (model instanceof IndexColorModel) {
                final IndexColorModel icm = (IndexColorModel) model;
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++) {
                        int sample = raster.getSample(x, y, 0);
                        samples[0] = icm.getRed(sample);
                        samples[1] = icm.getGreen(sample);
                        samples[2] = icm.getBlue(sample);
                        samples[3] = icm.getAlpha(sample);
                        dst_raster.setPixel(x, y, samples);
                    }
            } else {
                //log.error("Unimplemented raster conversion (required: " + model + " + available: " + dst_model);
                return raster;
            }
        else {
            //log.error("Unimplemented raster conversion (required: " + model + " + available: " + dst_model);
            return raster;
        }

        return dst_raster;
    }
}
