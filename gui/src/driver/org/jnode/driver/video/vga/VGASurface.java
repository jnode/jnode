/*
 * $Id$
 */
package org.jnode.driver.video.vga;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

import org.jnode.awt.util.AwtUtils;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.driver.video.vgahw.*;
import org.jnode.system.ResourceNotFreeException;

/**
 * @author epr
 */
public class VGASurface extends AbstractSurface implements Surface, VgaConstants {

	private final VGADriver driver;
	private final StandardVGA vga;
	private final StandardVGAIO vgaIO;
	private final IndexColorModel model;
	private final BitmapGraphics bitmapGraphics;

	public VGASurface(VGADriver driver) throws ResourceNotFreeException {
		super(640, 480);
		this.vga = new StandardVGA(driver.getDevice(), VGADriver.COLOR_MODEL);
		this.vgaIO = new StandardVGAIO(driver.getDevice(), vga.getVgaMem());
		this.driver = driver;
		this.model = VGADriver.COLOR_MODEL;
		this.bitmapGraphics = new VGABitmapGraphics(vga, vgaIO, width, height, 0, width / 8);
	}

	/**
	 * Open the given configuration
	 * @param config
	 */
	public synchronized void open(FrameBufferConfiguration config) {
		vga.startService(vgaIO);
	}

	/**
	 * Close the VGA surface
	 */
	public synchronized void close() {
		vgaIO.setGRAF(3, LOGOP_NONE);
		vga.stopService(vgaIO);
		vga.release();
		vgaIO.release();
		driver.close(this);
	}

	/**
	 * Draw the given shape
	 * @param shape
	 * @param color
	 * @param mode
	 */
	public synchronized void draw(Shape shape, AffineTransform tx, Color color, int mode) {
		vgaIO.setGRAF(0, convertColor(color));
		vgaIO.setGRAF(3, (mode == PAINT_MODE) ? LOGOP_NONE : LOGOP_XOR);
		draw(shape, tx, 0, mode);
	}

	/**
	 * Draw an image to this surface
	 * @param src
	 * @param srcX
	 * @param srcY
	 * @param x The upper left x coordinate 
	 * @param y The upper left y coordinate
	 * @param w
	 * @param h
	 * @param bgColor
	 */
	public void drawCompatibleRaster(Raster src, int srcX, int srcY, int x, int y, int w, int h, Color bgColor) {
		if (bgColor != null) {
			bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h, convertColor(bgColor));
		} else {
			bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h);
		}
	}

	/**
	 * Unused in this class
	 * @see org.jnode.driver.video.util.AbstractSurface#convertColor(java.awt.Color)
	 */
	protected final int convertColor(Color color) {
		return AwtUtils.getClosestColorIndex(model, color.getRGB());
	}

	/**
	 * @see org.jnode.driver.video.util.AbstractSurface#drawPixel(int, int, int, int)
	 */
	protected void drawPixel(int x, int y, int color, int mode) {
		bitmapGraphics.drawPixel(x, y, color, mode);
	}
	/**
	 * @see org.jnode.driver.video.util.AbstractSurface#drawLine(int, int, int, int, int, int)
	 */
	protected void drawLine(int x1, int y1, int x2, int y2, int color, int mode) {
		if (y1 == y2) {
			bitmapGraphics.drawLine(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, color, mode);
		} else {
			super.drawLine(x1, y1, x2, y2, color, mode);
		}
	}

	/**
	 * @see org.jnode.driver.video.Surface#getColorModel()
	 */
	public ColorModel getColorModel() {
		return model;
	}

}
