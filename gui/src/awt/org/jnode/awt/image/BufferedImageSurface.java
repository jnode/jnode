/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.awt.image;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;

/**
 * @author Levente S\u00e1ntha
 */
public class BufferedImageSurface extends AbstractSurface {

    private final BufferedImage sImage;
    private final BitmapGraphics bitmapGraphics;
    private final ColorModel model;

    public BufferedImageSurface(BufferedImage image) {
        super(image.getWidth(), image.getHeight());
        this.model = image.getColorModel();
        this.sImage = image;
        final Raster raster = image.getRaster();
        final DataBuffer dataBuffer = raster.getDataBuffer();
        final SampleModel sampleModel = raster.getSampleModel();
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            final SinglePixelPackedSampleModel sppSM = (SinglePixelPackedSampleModel) sampleModel;
            final int dataType = dataBuffer.getDataType();
            final int dataTypeSize = DataBuffer.getDataTypeSize(dataType);
            this.bitmapGraphics = BitmapGraphics.createInstance(dataBuffer, width, height,
                sppSM.getScanlineStride() * dataTypeSize / 8, model.getTransparency());
        } else {
            this.bitmapGraphics = null;
        }
    }

    /**
     * @param color
     * @return The color value
     */
    protected int convertColor(Color color) {
        // TODO Make dependent on BufferedImage type
        return color.getRGB();
    }

    /**
     * Draw a pixel
     *
     * @param x
     * @param y
     * @param color
     * @param mode
     */
    public void drawPixel(int x, int y, int color, int mode) {
        if (bitmapGraphics != null) {
            bitmapGraphics.drawPixels(x, y, 1, color, mode);
        } else {
            if (mode == Surface.PAINT_MODE) {
                sImage.setRGB(x, y, color);
            } else {
                sImage.setRGB(x, y, sImage.getRGB(x, y) ^ color);
            }
        }
    }

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int, int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        bitmapGraphics.copyArea(x, y, width, height, dx, dy);
    }

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX
     * @param srcY
     * @param x       The upper left x coordinate
     * @param y       The upper left y coordinate
     * @param w
     * @param h
     * @param bgColor
     */
    public void drawCompatibleRaster(Raster src, int srcX, int srcY, int x, int y, int w, int h, Color bgColor) {
        if (bitmapGraphics != null) {
            if (bgColor != null) {
                bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h, convertColor(bgColor));
            } else {
                bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h);
            }
        } else {
            final Object pData;
            final WritableRaster r = sImage.getRaster();
            pData = src.getDataElements(srcX, srcY, w, h, null);
            r.setDataElements(x, y, w, h, pData);
        }
    }

    /**
     * Draw a line
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param color
     * @param mode
     */
    public void drawLine(int x1, int y1, int x2, int y2, int color, int mode) {
        if ((y1 == y2) && (bitmapGraphics != null)) {
            bitmapGraphics.drawLine(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, color, mode);
        } else {
            super.drawLine(x1, y1, x2, y2, color, mode);
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
    @Override
    public void fillRect(int x, int y, int w, int h, int color, int mode) {
        super.fillRect(x, y, w, h, color, mode);
        //bitmapGraphics.fillRect(x, y, w, h, color, mode);
    }

    /**
     * @return the color model
     * @see org.jnode.driver.video.Surface#getColorModel()
     */
    public ColorModel getColorModel() {
        return model;
    }

    /**
     * @see org.jnode.driver.video.Surface#drawAlphaRaster(java.awt.image.Raster, java.awt.geom.AffineTransform,
     *      int, int, int, int, int, int, java.awt.Color)
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY,
                                int dstX, int dstY, int width, int height, Color color) {
        if (bitmapGraphics != null) {
            bitmapGraphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY, width, height, convertColor(color));
        } else {
            throw new AWTError("Not implemented");
        }
    }

    @Override
    public int getRGBPixel(final int x, final int y) {
        if (0 <= x && x < bitmapGraphics.getWidth() && 0 <= y && y < bitmapGraphics.getHeight()) {
            return bitmapGraphics.doGetPixel(x, y);
        } else {
            //todo investigate this case
            return 0;
        }
    }

    @Override
    public int[] getRGBPixels(Rectangle region) {
        return bitmapGraphics.doGetPixels(region);
    }
}
