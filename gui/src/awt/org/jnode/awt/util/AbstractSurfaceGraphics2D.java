/*
 * $Id: AbstractSurfaceGraphics.java 2680 2006-08-31 17:24:04Z lsantha $
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

import gnu.java.awt.java2d.AbstractGraphics2D;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.renderable.RenderableImage;
import java.io.WriteAbortedException;

import org.apache.log4j.Logger;
import org.jnode.awt.JNodeToolkit;
import org.jnode.awt.image.JNodeImage;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;

/**
 * @author epr
 */
public abstract class AbstractSurfaceGraphics2D extends AbstractGraphics2D {

	private AbstractSurface surface;
	private static final Logger log = Logger.getLogger(AbstractSurfaceGraphics.class);
	private int mode = Surface.PAINT_MODE;

	private int width;
	private int height;
	
	/**
	 * @param src
	 */
	public AbstractSurfaceGraphics2D(AbstractSurfaceGraphics2D src) {
		super();
		this.surface = src.surface;
		this.width = src.width;
		this.height = src.height;
	}

	/**
	 * @param surface
	 * @param width
	 * @param height
	 */
	public AbstractSurfaceGraphics2D(AbstractSurface surface, int width, int height) {
		super();
		this.surface = surface;
		this.width = width;
		this.height = height;
	}

	
	public Object clone() {
		AbstractSurfaceGraphics2D copy = (AbstractSurfaceGraphics2D) super.clone();
		
		copy.surface = surface;
		copy.width = width;
		copy.height = height;
		
		return copy;
	}

	/**
	 * @see java.awt.Graphics#setPaintMode()
	 */
	public final void setPaintMode() {
		super.setPaintMode();
		mode = Surface.PAINT_MODE;
	}

	/**
	 * @param color
	 * @see java.awt.Graphics#setXORMode(java.awt.Color)
	 */
	public final void setXORMode(Color color) {
		super.setXORMode(color);
		mode = Surface.XOR_MODE;
	}
	
	/**
	 * @param image
	 * @param x
	 * @param y
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	protected final boolean rawDrawImage(Image image, int x, int y, ImageObserver observer) {
		try {
            if(transform != null) {
                Point p = new Point(x, y);
                transform.transform(p,p);
                x = p.x; y = p.y;
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
     * @param text
     * @param x
     * @param y
     * @see java.awt.Graphics#drawString(String,int,int)
     */
    public void rawDrawString(String text, int x, int y) {
    	try {

    		//System.out.println("drawText():" + text);
    		final Font font = getFont();
    		if (font != null) {
    			JNodeToolkit tk = ((JNodeToolkit) Toolkit.getDefaultToolkit());
    			if(tk == null) { System.err.println("Toolkit is null"); return; }
    			if(tk.getFontManager() == null) { System.err.println("FontManager is null"); return; }
    			// fontmanager == org.jnode.awt.font.def.DefaultFontManager
    			tk.getFontManager().drawText(surface, getClip(), this.transform, text, font, x, y, getColor());
    		}
    	} catch(Throwable t) {
    		log.error("error in drawString", t);
    	}
    }



    protected void rawDrawLine(int x0, int y0, int x1, int y1) {
		surface.drawLine(x0, y0, x1, y1, getColor().getRGB(), this.mode);
	}

	protected void rawClearRect(int x, int y, int w, int h) {
		surface.fillRect(x, y, w, h, 0, this.mode);
	}
	
	protected void rawFillRect(int x, int y, int w, int h) {
		surface.fillRect(x, y, w, h, getColor().getRGB(), this.mode);
	}


    protected void rawCopyArea(int x, int y, int w, int h, int dx, int dy) {
		surface.copyArea(x, y, w, h, dx, dy);
	}

    /**
     * Notifies the backend that the raster has changed in the specified
     * rectangular area. The raster that is provided in this method is always
     * the same as the one returned in {@link #getDestinationRaster}.
     * Backends that reflect changes to this raster directly don't need to do
     * anything here.
     *
     * @param raster the updated raster, identical to the raster returned
     *               by {@link #getDestinationRaster()}
     * @param x      the upper left corner of the updated region, X coordinate
     * @param y      the upper lef corner of the updated region, Y coordinate
     * @param w      the width of the updated region
     * @param h      the height of the updated region
     */
	protected void rawUpdateRaster(Raster raster, int x, int y, int w, int h) {

		System.out.println("rawUpdateRaster(...)");
		surface.drawCompatibleRaster(raster, x, y, 0, 0, w, h, null);
	}

	
	// HELPER Methods:
	

	/**
	 * Gets the Raster of a given image.
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
			final PixelGrabber grabber = new PixelGrabber(image, 0, 0, -1, -1, true);
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

        if(dst_model instanceof DirectColorModel)
            if(model instanceof ComponentColorModel){
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++)
                        dst_raster.setPixel(x, y, raster.getPixel(x, y, samples));
            } else if(model instanceof IndexColorModel){
                final IndexColorModel icm = (IndexColorModel) model;
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++){
                        int sample = raster.getSample(x, y, 0);
                        samples[0] = icm.getRed(sample);
                        samples[1] = icm.getGreen(sample);
                        samples[2] = icm.getBlue(sample);
                        samples[3] = icm.getAlpha(sample);
                        dst_raster.setPixel(x, y, samples);
                    }
            } else {
                log.error("Unimplemented raster conversion");
                return raster;
            }
        else {
            log.error("Unimplemented raster conversion");
            return raster;
        }

        return dst_raster;
	}
}
