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
	
	/**
	 * Initialize this instance.
	 * @param shape
	 */
	public GlyphRenderer(Shape shape) {
		final Master master = createMaster(shape);
		this.sumAreaTable = new SummedAreaTable(master.bits, master.width, master.height);		
	}
	
	/**
	 * Create a raster for the given at a given font-size.
	 * @param fontSize
	 * @return
	 */
	public Raster createGlyphRaster(int fontSize) {
		final double scale = (double)sumAreaTable.getHeight() / (double)fontSize;
		final int width = (int)(sumAreaTable.getWidth() / scale);

		final ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
		final int[] nBits = { 8 };
		final ComponentColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE,
                DataBuffer.TYPE_BYTE);
		final WritableRaster raster = cm.createCompatibleWritableRaster(width, fontSize);
		
		final int si = (int)scale;
		for (int y = 0; y < fontSize; y++) {
			final int ypos = (int)(y * scale);
			for (int x = 0; x < width; x++) {
				final int xpos = (int)(x * scale);
				final float v = sumAreaTable.getIntensity(xpos, ypos, si, si) * 255;
				
				//System.out.println("(" + x + "," + y + ") " + v + ", si " + si);
				raster.setSample(x, y, 0, v);
			}
		}
		return raster;			
	}
	
	/**
	 * Creates a master shape of the letter.
	 * @param shape
	 * @return
	 */
	private final Master createMaster(Shape shape) {
		final Area area = new Area(shape);
		final double scale = MASTER_HEIGHT / area.getBounds().getHeight();

		area.transform(AffineTransform.getScaleInstance(scale, scale));
		Rectangle bounds = area.getBounds();
		area.transform(AffineTransform.getTranslateInstance(-bounds.getMinX(), -bounds.getMinY()));
		bounds = area.getBounds();
		
		final int width = (int)(bounds.getMaxX() + 0.5);
		final int height = (int)(bounds.getMaxY() + 0.5);
		
		System.out.println("width=" + width + ", height=" + height);
		
		final BitSet bits = new BitSet(width * height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (area.contains(x, y)) {
					bits.set(y * width + x);
				}
			}
		}

		return new Master(bits, width, height);
	}
	
	private static class Master {
	    public BitSet bits;
	    public int width;
	    public int height;
	    
	    
        /**
         * @param bits
         * @param width
         * @param height
         */
        public Master(BitSet bits, int width, int height) {
            super();
            this.bits = bits;
            this.width = width;
            this.height = height;
        }
	}
}
