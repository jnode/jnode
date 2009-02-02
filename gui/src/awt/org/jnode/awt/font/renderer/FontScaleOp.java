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
 
package org.jnode.awt.font.renderer;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.ImagingOpException;
import java.awt.image.Kernel;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class FontScaleOp implements BufferedImageOp, RasterOp {

    private final int dstHeight;

    private final int dstWidth;

    private final Kernel kernel;

    public FontScaleOp(int dstWidth, int dstHeight, Kernel kernel) {
        this.dstHeight = dstHeight;
        this.dstWidth = dstWidth;
        this.kernel = kernel;
    }

    /**
     * @see java.awt.image.BufferedImageOp#createCompatibleDestImage(java.awt.image.BufferedImage,
     *      java.awt.image.ColorModel)
     */
    public BufferedImage createCompatibleDestImage(BufferedImage src,
                                                   ColorModel dstCM) {
        return new BufferedImage(dstWidth, dstHeight,
            BufferedImage.TYPE_BYTE_GRAY);
    }

    /**
     * @see java.awt.image.RasterOp#createCompatibleDestRaster(java.awt.image.Raster)
     */
    public WritableRaster createCompatibleDestRaster(Raster src) {
        SampleModel sm = new ComponentSampleModel(DataBuffer.TYPE_BYTE,
            dstWidth, dstHeight, 0, 0, new int[]{0});
        return Raster.createWritableRaster(sm, sm.createDataBuffer(),
            new Point(0, 0));
    }

    /**
     * @see java.awt.image.BufferedImageOp#filter(java.awt.image.BufferedImage,
     *      java.awt.image.BufferedImage)
     */
    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (src == dst) {
            throw new IllegalArgumentException("src == dst");
        }

        if (dst == null) {
            dst = createCompatibleDestImage(src, src.getColorModel());
        }

        filter(src.getRaster(), dst.getRaster());

        return dst;
    }

    /**
     * @see java.awt.image.RasterOp#filter(java.awt.image.Raster,
     *      java.awt.image.WritableRaster)
     */
    public WritableRaster filter(Raster src, WritableRaster dest) {
        if (src == dest) {
            throw new IllegalArgumentException("src == dest");
        }
        if ((src.getWidth() < kernel.getWidth()) || (src.getHeight() < kernel.getHeight())) {
            throw new ImagingOpException("src.bounds < kernel.bounds");
        }

        if (dest == null) {
            dest = createCompatibleDestRaster(src);
        }

        final int srcWidth = src.getWidth();
        final int srcHeight = src.getHeight();
        final double sx = srcWidth / (double) dstWidth;
        final double sy = srcHeight / (double) dstHeight;
        final int kw = kernel.getWidth();
        final int kh = kernel.getHeight();
        final float[] kData = kernel.getKernelData(null);

        final float[] tmp = new float[kw * kh];
        final int tmpLength = tmp.length;

        for (int y = 0; y < dstHeight; y++) {
            final int srcY = (int) (y * sy);
            for (int x = 0; x < dstWidth; x++) {

                final int srcX = (int) (x * sx);
                getSamples(src, srcWidth, srcHeight, srcX, srcY, kw, kh, tmp);

                float v = 0.0f;
                for (int i = 0; i < tmpLength; i++) {
                    v += tmp[i] * kData[i];
                }

                dest.setSample(x, y, 0, v);
            }
        }

        return dest;
    }

    private final void getSamples(Raster src, int srcWidth, int srcHeight,
                                  int x, int y, int w, int h, float[] data) {
        if ((x < 0) || (x + w > srcWidth) || (y < 0) || (y + h > srcHeight)) {
            // Somewhere out of bounds
            Arrays.fill(data, 0.0f);
            for (int yy = y; yy < y + h; yy++) {
                if ((yy >= 0) && (yy < srcHeight)) {
                    for (int xx = x; xx < x + w; xx++) {
                        if ((xx >= 0) && (xx < srcWidth)) {
                            float v = src.getSampleFloat(xx, yy, 0);
                            data[(yy - y) * w + (xx - x)] = v;
                        }
                    }
                }
            }
        } else {
            // All samples visible
            src.getSamples(x, y, w, h, 0, data);
        }
    }

    /**
     * @see java.awt.image.BufferedImageOp#getBounds2D(java.awt.image.BufferedImage)
     */
    public Rectangle2D getBounds2D(BufferedImage src) {
        return new Rectangle(0, 0, dstWidth, dstHeight);
    }

    /**
     * @see java.awt.image.RasterOp#getBounds2D(java.awt.image.Raster)
     */
    public Rectangle2D getBounds2D(Raster src) {
        return new Rectangle(0, 0, dstWidth, dstHeight);
    }

    /**
     * @see java.awt.image.RasterOp#getPoint2D(java.awt.geom.Point2D,
     *      java.awt.geom.Point2D)
     */
    public Point2D getPoint2D(Point2D srcPoint, Point2D destPoint) {
        return srcPoint;
    }

    /**
     * @see java.awt.image.RasterOp#getRenderingHints()
     */
    public RenderingHints getRenderingHints() {
        return null;
    }
}
