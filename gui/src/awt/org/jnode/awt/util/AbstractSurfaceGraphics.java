/*
 * $Id$
 */
package org.jnode.awt.util;

import java.awt.Color;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
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
import java.awt.image.renderable.RenderableImage;

import org.apache.log4j.Logger;
import org.jnode.awt.image.JNodeImage;
import org.jnode.driver.video.Surface;

/**
 * @author epr
 */
public abstract class AbstractSurfaceGraphics extends AbstractGraphics {

	private final Surface surface;
	private final Logger log = Logger.getLogger(getClass());
	private int mode = Surface.PAINT_MODE;

	/**
	 * @param src
	 */
	public AbstractSurfaceGraphics(AbstractSurfaceGraphics src) {
		super(src);
		this.surface = src.surface;
	}

	/**
	 * @param surface
	 * @param width
	 * @param height
	 */
	public AbstractSurfaceGraphics(Surface surface, int width, int height) {
		super(width, height);
		this.surface = surface;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param dx
	 * @param dy
	 * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param shape
	 * @see java.awt.Graphics2D#draw(java.awt.Shape)
	 */
	public final void draw(Shape shape) {
		surface.draw(shape, clip, transform, getColor(), mode);
	}

	/**
	 * @param image
	 * @param x
	 * @param y
	 * @param bgcolor
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, int x, int y, Color bgcolor, ImageObserver observer) {
		try {
			final Raster raster = getCompatibleRaster(image);
			surface.drawCompatibleRaster(raster, 0, 0, x, y, raster.getWidth(), raster.getHeight(), bgcolor);
			return true;
		} catch (InterruptedException ex) {
			return false;
		}
	}

	/**
	 * @param image
	 * @param x
	 * @param y
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, int x, int y, ImageObserver observer) {
		try {
			final Raster raster = getCompatibleRaster(image);
			surface.drawCompatibleRaster(raster, 0, 0, x, y, raster.getWidth(), raster.getHeight(), null);
			return true;
		} catch (InterruptedException ex) {
			return false;
		}
	}

	/**
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param bgcolor
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
		return drawImage(new JNodeImage(new FilteredImageSource(image.getSource(), new AreaAveragingScaleFilter(width, height))), x, y, bgcolor, observer);
	}

	/**
	 * @param image
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, int x, int y, int width, int height, ImageObserver observer) {
		return drawImage(new JNodeImage(new FilteredImageSource(image.getSource(), new AreaAveragingScaleFilter(width, height))), x, y, observer);
	}

	/**
	 * @param image
	 * @param dx1
	 * @param dy1
	 * @param dx2
	 * @param dy2
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 * @param bgColor
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bgColor, ImageObserver observer) {
		if (dx1 == dx2 || dy1 == dy2) {
			return true;
		}
		if (sx1 == sx2 || sy1 == sy2) {
			return true;
		}

		final int widthImage;
		final int heightImage;
		final int xImage;
		final int yImage;
		if (sx2 > sx1) {
			widthImage = sx2 - sx1 + 1;
			xImage = sx1;
		} else {
			widthImage = sx1 - sx2 + 1;
			xImage = sx2;
		}

		if (sy2 > sy1) {
			heightImage = sy2 - sy1 + 1;
			yImage = sy1;
		} else {
			heightImage = sy1 - sy2 + 1;
			yImage = sy2;
		}

		final int widthDest;
		final int heightDest;
		final int xDest;
		final int yDest;
		if (dx2 > dx1) {
			widthDest = dx2 - dx1 + 1;
			xDest = dx1;
		} else {
			widthDest = dx1 - dx2 + 1;
			xDest = dx2;
		}

		if (dy2 > dy1) {
			heightDest = dy2 - dy1 + 1;
			yDest = dy1;
		} else {
			heightDest = dy1 - dy2 + 1;
			yDest = dy2;
		}

		// Extract the image with a CropImageFilter
		final Image imageArea = new JNodeImage(new FilteredImageSource(image.getSource(), new CropImageFilter(xImage, yImage, widthImage, heightImage)));
		if (bgColor == null) {
			return drawImage(imageArea, xDest, yDest, widthDest, heightDest, observer);
		} else {
			return drawImage(imageArea, xDest, yDest, widthDest, heightDest, bgColor, observer);
		}
	}

	/**
	 * @param image
	 * @param dx1
	 * @param dy1
	 * @param dx2
	 * @param dy2
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 * @param observer
	 * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
		return drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
	}

	/**
	 * @param image
	 * @param op
	 * @param x
	 * @param y
	 * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
	 */
	public final void drawImage(BufferedImage image, BufferedImageOp op, int x, int y) {
		final BufferedImage dstImage = op.createCompatibleDestImage(image, surface.getColorModel());
		drawImage(op.filter(image, dstImage), x, y, null);
	}

	/**
	 * @param image
	 * @param xform
	 * @param obs
	 * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
	 * @return boolean
	 */
	public final boolean drawImage(Image image, AffineTransform xform, ImageObserver obs) {
		log.debug("JnodeGraphics: drawImage");
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * @param image
	 * @param xform
	 * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
	 */
	public final void drawRenderableImage(RenderableImage image, AffineTransform xform) {
		drawRenderedImage(image.createDefaultRendering(), xform);
	}

	/**
	 * @param image
	 * @param xform
	 * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
	 */
	public final void drawRenderedImage(RenderedImage image, AffineTransform xform) {
		// TODO Auto-generated method stub

	}

	/**
	 * @param shape
	 * @see java.awt.Graphics2D#fill(java.awt.Shape)
	 */
	public final void fill(Shape shape) {
		surface.fill(shape, clip, transform, getColor(), mode);
	}

	/**
	 * @see java.awt.Graphics#setPaintMode()
	 */
	public void setPaintMode() {
		super.setPaintMode();
		mode = Surface.PAINT_MODE;
	}

	/**
	 * @param color
	 * @see java.awt.Graphics#setXORMode(java.awt.Color)
	 */
	public void setXORMode(Color color) {
		super.setXORMode(color);
		mode = Surface.XOR_MODE;
	}

	/**
	 * Gets the Raster of a given image.
	 * @param image
	 * @return Raster
	 * @throws InterruptedException
	 */
	private Raster getCompatibleRaster(Image image) throws InterruptedException {
		final ColorModel dstModel = surface.getColorModel();
		if (image instanceof BufferedImage) {
			// We have a direct raster
			final Raster raster = ((BufferedImage) image).getRaster();
			if (dstModel.isCompatibleRaster(raster)) {
				// Raster is compatible, return without changes
				return raster;
			} else {
				// Convert it into a compatible raster
				return createCompatibleRaster(raster);
			}
		} else if (image instanceof RenderedImage) {
				// We have a direct raster
				final Raster raster = ((RenderedImage) image).getData();
				if (dstModel.isCompatibleRaster(raster)) {
					// Raster is compatible, return without changes
					return raster;
				} else {
					// Convert it into a compatible raster
					return createCompatibleRaster(raster);
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
	 * @return
	 */
	private Raster createCompatibleRaster(Raster raster) {
		// TODO Implement raster conversion
		log.warn("Unimplemented raster conversion in AbstractSurfaceGraphics");
		return raster;
	}
}
