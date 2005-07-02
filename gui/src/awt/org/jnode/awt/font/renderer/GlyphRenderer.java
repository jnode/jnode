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
 
package org.jnode.awt.font.renderer;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.BitSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GlyphRenderer {

    private final double MASTER_HEIGHT = 128.0;
	private final SummedAreaTable sumAreaTable;
    private final Master master;
    private final double minX;
    private final double minY;
	
	/**
	 * Initialize this instance.
	 * @param shape
	 */
	public GlyphRenderer(Shape shape, double ascent) {
		final Master master = createMaster(shape, ascent);
        this.master = master;
        this.minX = master.minX;
        this.minY = master.minY;
		this.sumAreaTable = new SummedAreaTable(master.bits, master.width, master.height);		
	}
	
	/**
	 * Create a raster for the given at a given font-size.
	 * @param fontSize
	 * @return
	 */
	public Raster createGlyphRaster(double fontSize) {
	    final double scale = MASTER_HEIGHT / fontSize;
        final int height = (int)(sumAreaTable.getHeight() / scale);
		final int width = (int)(sumAreaTable.getWidth() / scale);

		final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		final int[] nBits = { 8 };
		final ComponentColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
		final WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		
		final int si = (int)scale;
		for (int y = 0; y < height; y++) {
			final int ypos = (int)(y * scale);
			for (int x = 0; x < width; x++) {
				final int xpos = (int)(x * scale);
				final float v = sumAreaTable.getIntensity(xpos, ypos, si, si) * 255;
				raster.setSample(x, y, 0, v);
			}
		}
		return raster;			
	}
    
    /**
     * Gets the minX, minY location that was removed when the
     * glyph was rendered into the raster.
     * @param fontSize
     * @return
     */
    public Point2D getMinLocation(double fontSize) {
        final double scale = MASTER_HEIGHT / fontSize;
        return new Point2D.Double(minX / scale, -minY / scale);
    }
	
	/**
	 * Creates a master shape of the letter.
	 * @param shape
	 * @return
	 */
	private final Master createMaster(Shape shape, double ascent) {
		final Area area = new Area(shape);
		final double scale = MASTER_HEIGHT / ascent;

		area.transform(AffineTransform.getScaleInstance(scale, scale));
		Rectangle bounds = area.getBounds();
//        System.out.println("createMaster bounds " + bounds);
		//area.transform(AffineTransform.getTranslateInstance(-bounds.getMinX(), -bounds.getMinY()));
		//bounds = area.getBounds();
		
        final int minX = (int)(bounds.getMinX() - 0.5);
        final int maxX = (int)(bounds.getMaxX() + 0.5);
        final int minY = (int)(bounds.getMinY() - 0.5);
        final int maxY = (int)(bounds.getMaxY() + 0.5);
		final int width = maxX - minX;
		final int height = maxY - minY;
		
//        System.out.println("createMaster wxh " + width + 'x' + height);

//        System.out.println("min/max X " + minX + "/" + maxX);
//        System.out.println("min/max Y " + minY + "/" + maxY);
//		System.out.println("width=" + width + ", height=" + height);
		
		final BitSet bits = new BitSet(width * height);
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

    /**
     * @return Returns the master.
     */
    public final Master getMaster() {
        return master;
    }
}
