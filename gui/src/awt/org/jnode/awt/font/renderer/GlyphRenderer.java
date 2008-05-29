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

package org.jnode.awt.font.renderer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.BitSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GlyphRenderer {

    private final double MASTER_HEIGHT = 128.0;

    private final SummedAreaTable sumAreaTable;

    private final double minX;

    private final double minY;

    private static final String BITS_NAME = GlyphRenderer.class.getName() + "#BITS";

    /**
     * Initialize this instance.
     *
     * @param shape
     */
    public GlyphRenderer(RenderContext ctx, Shape shape, double ascent) {
        final Master master = createMaster(ctx, shape, ascent);
        this.minX = master.minX;
        this.minY = master.minY;
        this.sumAreaTable = SummedAreaTable.create(master.bits, master.width,
            master.height);
    }

    /**
     * Create a raster that can be used in {@link #createGlyphRaster(double)}.
     *
     * @param width
     * @param height
     * @return
     */
    public static final WritableRaster createRaster(int width, int height) {
        final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        final int[] nBits = {8};
        final ComponentColorModel cm = new ComponentColorModel(cs, nBits,
            false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return cm.createCompatibleWritableRaster(width, height);
    }

    /**
     * Create a raster for the given at a given font-size.
     *
     * @param fontSize
     * @return The size of the created raster.
     */
    public final Dimension createGlyphRaster(WritableRaster dst, double fontSize) {
        final double scale = MASTER_HEIGHT / fontSize;
        final int height = (int) ((((double) sumAreaTable.getHeight()) / scale) + 0.5);
        final int width = (int) ((((double) sumAreaTable.getWidth()) / scale) + 0.5);

        if (dst.getWidth() < width) {
            throw new IllegalArgumentException("Raster width (" + dst.getWidth() + ") is too small (" + width + ")");
        }
        if (dst.getHeight() < height) {
            throw new IllegalArgumentException("Raster height (" + dst.getHeight() + ") is too small (" + height + ")");
        }

        final int si = (int) (scale + 0.5);
        int ypos = (height - 1) * si;
        for (int y = height - 1; y >= 0; y--) {
            int xpos = (width - 1) * si;
            for (int x = width - 1; x >= 0; x--) {
                final int v = sumAreaTable.getIntensity8b(xpos, ypos, si, si);
                dst.setSample(x, y, 0, v);
                xpos -= si;
            }
            ypos -= si;
        }
        return new Dimension(width, height);
    }

    /**
     * Gets the minX, minY location that was removed when the glyph was rendered
     * into the raster.
     *
     * @param fontSize
     * @return
     */
    public Point2D getMinLocation(double fontSize) {
        final double scale = MASTER_HEIGHT / fontSize;
        return new Point2D.Double(minX / scale, -minY / scale);
    }

    /**
     * Creates a master shape of the letter.
     *
     * @param shape
     * @return
     */
    private final Master createMaster(RenderContext ctx, Shape shape,
                                      double ascent) {
        final Area area = new Area(shape);
        final double scale = MASTER_HEIGHT / ascent;

        area.transform(AffineTransform.getScaleInstance(scale, scale));
        final Rectangle bounds = area.getBounds();
        // System.out.println("createMaster bounds " + bounds);
        // area.transform(AffineTransform.getTranslateInstance(-bounds.getMinX(),
        // -bounds.getMinY()));
        // bounds = area.getBounds();

        final int minX = (int) (bounds.getMinX() - 0.5);
        final int maxX = (int) (bounds.getMaxX() + 0.5);
        final int minY = (int) (bounds.getMinY() - 0.5);
        final int maxY = (int) (bounds.getMaxY() + 0.5);
        final int width = maxX - minX;
        final int height = maxY - minY;

        BitSet bits = (BitSet) ctx.getObject(BITS_NAME);
        if (bits == null) {
            bits = new BitSet(width * height);
            ctx.setObject(BITS_NAME, bits);
        } else {
            bits.clear();
        }
        int ofs = 0;
        for (int y = maxY; y > minY; y--) {
            for (int x = minX; x < maxX; x++) {
                if (area.contains(x, y)) {
                    bits.set(ofs);
                }
                ofs++;
            }
        }

        return new Master(bits, width, height, minX, minY);
    }

    public static class Master {
        public final BitSet bits;

        public final int width;

        public final int height;

        public final int minX;

        public final int minY;

        /**
         * @param bits
         * @param width
         * @param height
         */
        public Master(BitSet bits, int width, int height, int minX, int minY) {
            super();
            this.bits = bits;
            this.width = width;
            this.height = height;
            this.minX = minX;
            this.minY = minY;
        }
    }
}
