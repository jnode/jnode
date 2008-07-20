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

package org.jnode.awt.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
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
import java.awt.image.renderable.RenderableImage;
import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.driver.video.Surface;
import sun.awt.image.ToolkitImage;

/**
 * @deprecated
 * @author epr
 * @author Levente S\u00e1ntha
 */
public abstract class AbstractSurfaceGraphics extends AbstractGraphics {

    private final Surface surface;
    private static final Logger log = Logger.getLogger(AbstractSurfaceGraphics.class);
    private int mode = Surface.PAINT_MODE;

    /**
     * @param src
     */
    public AbstractSurfaceGraphics(AbstractSurfaceGraphics src) {
        super(src);
        this.surface = src.surface;
    }

    /**
     * @param surface
     * @param width
     * @param height
     */
    public AbstractSurfaceGraphics(Surface surface, int width, int height) {
        super(width, height);
        this.surface = surface;
    }

    /**
     * @param x
     * @param y
     * @param width
     * @param height
     * @param dx
     * @param dy
     * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        surface.copyArea(x, y, width, height, dx, dy);
    }

    /**
     * @param shape
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    public final void draw(Shape shape) {
        surface.draw(shape, clip, transform, getColor(), mode);
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param bgcolor
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, int x, int y, Color bgcolor, ImageObserver observer) {
        try {
            if (transform != null) {
                Point p = new Point(x, y);
                transform.transform(p, p);
                x = p.x;
                y = p.y;
            }
            final Raster raster = getCompatibleRaster(image);
            Rectangle r = getClipBounds();
            int w = Math.min(raster.getWidth(), r.width);
            int h = Math.min(raster.getHeight(), r.height);
            surface.drawCompatibleRaster(raster, 0, 0, x, y, w, h, bgcolor);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, int x, int y, ImageObserver observer) {
        try {
            if (transform != null) {
                Point p = new Point(x, y);
                transform.transform(p, p);
                x = p.x;
                y = p.y;
            }
            final Raster raster = getCompatibleRaster(image);
            Rectangle r = getClipBounds();
            int w = Math.min(raster.getWidth(), r.width);
            int h = Math.min(raster.getHeight(), r.height);
            surface.drawCompatibleRaster(raster, 0, 0, x, y, w, h, null);
            return true;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @param bgcolor
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int,
     *      java.awt.Color, java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, int x, int y, int width, int height, Color bgcolor,
                                   ImageObserver observer) {
        return drawImage(
            new ToolkitImage(new FilteredImageSource(image.getSource(), new AreaAveragingScaleFilter(width, height))),
            x,
            y, bgcolor, observer);
    }

    /**
     * @param image
     * @param x
     * @param y
     * @param width
     * @param height
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, int x, int y, int width, int height, ImageObserver observer) {
        return drawImage(
            new ToolkitImage(new FilteredImageSource(image.getSource(), new AreaAveragingScaleFilter(width, height))),
            x,
            y, observer);
    }

    /**
     * @param image
     * @param dx1
     * @param dy1
     * @param dx2
     * @param dy2
     * @param sx1
     * @param sy1
     * @param sx2
     * @param sy2
     * @param bgColor
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int,
     *      java.awt.Color, java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                                   Color bgColor, ImageObserver observer) {
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
        final Image imageArea = new ToolkitImage(new FilteredImageSource(image.getSource(),
            new CropImageFilter(xImage, yImage, widthImage, heightImage)));
        if (bgColor == null) {
            return drawImage(imageArea, xDest, yDest, widthDest, heightDest, observer);
        } else {
            return drawImage(imageArea, xDest, yDest, widthDest, heightDest, bgColor, observer);
        }
    }

    /**
     * @param image
     * @param dx1
     * @param dy1
     * @param dx2
     * @param dy2
     * @param sx1
     * @param sy1
     * @param sx2
     * @param sy2
     * @param observer
     * @return boolean
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int,
     *      java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
                                   ImageObserver observer) {
        return drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
    }

    /**
     * @param image
     * @param op
     * @param x
     * @param y
     * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
     */
    public final void drawImage(BufferedImage image, BufferedImageOp op, int x, int y) {
        final BufferedImage dstImage = op.createCompatibleDestImage(image, surface.getColorModel());
        drawImage(op.filter(image, dstImage), x, y, null);
    }

    /**
     * @param image
     * @param xform
     * @param obs
     * @return boolean
     * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    public final boolean drawImage(Image image, AffineTransform xform, ImageObserver obs) {
        log.debug("JnodeGraphics: drawImage");
        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @param image
     * @param xform
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage,
     *      java.awt.geom.AffineTransform)
     */
    public final void drawRenderableImage(RenderableImage image, AffineTransform xform) {
        drawRenderedImage(image.createDefaultRendering(), xform);
    }

    /**
     * @param image
     * @param xform
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
     */
    public final void drawRenderedImage(RenderedImage image, AffineTransform xform) {
        // TODO Auto-generated method stub

    }

    /**
     * @param shape
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    public final void fill(Shape shape) {
        surface.fill(shape, clip, transform, getColor(), mode);
    }

    /**
     * @see java.awt.Graphics#setPaintMode()
     */
    public void setPaintMode() {
        super.setPaintMode();
        mode = Surface.PAINT_MODE;
    }

    /**
     * @param color
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    public void setXORMode(Color color) {
        super.setXORMode(color);
        mode = Surface.XOR_MODE;
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
            final PixelGrabber grabber =
                new PixelGrabber(image, 0, 0, image.getWidth(null), image.getHeight(null), true);
            if (grabber.grabPixels()) {
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
                log.error("Unimplemented raster conversion (required: " + model + " + available: " + dst_model);
                return raster;
            }
        else {
            log.error("Unimplemented raster conversion (required: " + model + " + available: " + dst_model);
            return raster;
        }

        return dst_raster;
    }

    /**
     * @param text
     * @param x
     * @param y
     * @see java.awt.Graphics#drawString(java.lang.String,int,int)
     */
    public void drawString(String text, int x, int y) {
        try {
            //System.out.println("drawText():" + text);
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

                tk.getFontManager().drawText(surface, this.clip, this.transform, text, font, x, y, getColor());
            }
        } catch (Throwable t) {
            log.error("error in drawString", t);
        }
    }
}
