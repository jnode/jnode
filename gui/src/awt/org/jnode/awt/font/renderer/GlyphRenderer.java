/*
 * $Id$
 */
package org.jnode.awt.font.renderer;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class GlyphRenderer {

	private final SummedAreaTable sumAreaTable;
	
	/**
	 * Initialize this instance.
	 * @param shape
	 */
	public GlyphRenderer(Shape shape) {
		final Raster master = createMaster(shape);
		this.sumAreaTable = new SummedAreaTable(master);		
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
				
				System.out.println("(" + x + "," + y + ") " + v + ", si " + si);
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
	private final Raster createMaster(Shape shape) {
		final Rectangle bounds = shape.getBounds();
		final int width = (int)(bounds.getMaxX() + 0.5);
		final int height = (int)(bounds.getMaxY() + 0.5);
		
        final WritableRaster raster;
        raster = Raster.createPackedRaster(DataBuffer.TYPE_BYTE,
                                           width, height, 1, 1, null);
	
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (shape.contains(x, y)) {
					raster.setSample(x, y, 0, 1);
				}
			}
		}
		return raster;
	}
}
