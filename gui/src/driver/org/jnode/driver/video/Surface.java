/*
 * $Id$
 */
package org.jnode.driver.video;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

/**
 * A Surface is an area (of the screen or of an imagebuffer) that can
 * be painted on.
 * 
 * @author epr
 */
public interface Surface {

	public static final int PAINT_MODE = 0x00;
	public static final int XOR_MODE = 0x01;

	/**
	 * Draw the given shape
	 * @param shape
	 * @param color
	 * @param mode
	 */
	public void draw(Shape shape, AffineTransform tx, Color color, int mode);

	/**
	 * Fill the given shape with the given color
	 * @param shape
	 * @param color
	 * @param mode
	 */
	public void fill(Shape shape, AffineTransform tx, Color color, int mode);

	/**
	 * Draw an raster to this surface.
	 * The given raster is compatible with the color model of this surface.
	 * 
	 * @param raster
	 * @param srcX The upper left x coordinate within the raster
	 * @param srcY The upper left y coordinate within the raster
	 * @param dstX The upper left destination x coordinate 
	 * @param dstY The upper left destination y coordinate
	 * @param width
	 * @param height
	 * @param bgColor The background color to use for transparent pixels. If null, no transparent pixels are unmodified on the destination
	 */
	public void drawCompatibleRaster(Raster raster, int srcX, int srcY, int dstX, int dstY, int width, int height, Color bgColor);

	/**
	 * Gets the color model of this surface
	 */
	public ColorModel getColorModel();

	/**
	 * Close this surface
	 */
	public void close();
}
