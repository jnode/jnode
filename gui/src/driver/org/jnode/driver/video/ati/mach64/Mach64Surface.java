/*
 * $Id$
 */
package org.jnode.driver.video.ati.mach64;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

import org.jnode.driver.video.util.AbstractSurface;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Mach64Surface extends AbstractSurface {
	
	/**
	 * @param width
	 * @param height
	 */
	public Mach64Surface(int width, int height) {
		super(width, height);
		// TODO Auto-generated constructor stub
	}
	
	protected int convertColor(Color color) {
		// TODO Auto-generated method stub
		return 0;
	}

	protected void drawPixel(int x, int y, int color, int mode) {
		// TODO Auto-generated method stub

	}

	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX,
			int srcY, int dstX, int dstY, int width, int height, Color color) {
		// TODO Auto-generated method stub

	}

	public void drawCompatibleRaster(Raster raster, int srcX, int srcY,
			int dstX, int dstY, int width, int height, Color bgColor) {
		// TODO Auto-generated method stub

	}

	public ColorModel getColorModel() {
		// TODO Auto-generated method stub
		return null;
	}
}
