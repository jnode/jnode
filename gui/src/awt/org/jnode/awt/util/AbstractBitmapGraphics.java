/**
 * 
 */
package org.jnode.awt.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.Raster;

import org.apache.log4j.Logger;
import org.jnode.driver.video.Surface;
import org.jnode.system.MemoryResource;
import org.jnode.system.MultiMediaMemoryResource;

public abstract class AbstractBitmapGraphics extends BitmapGraphics  
{
    protected final int bytesPerLine;

    protected final int height;

    /** My logger */
    protected static final Logger log = Logger.getLogger(AbstractBitmapGraphics.class);

    protected final MultiMediaMemoryResource mem;

    /** Offset of first pixel in mem (in bytes) */
    protected final int offset;

    protected final int width;

    public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
    
    /**
     * Create a new instance
     * 
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    protected AbstractBitmapGraphics(MemoryResource mem, int width, int height,
            int offset, int bytesPerLine) {
        this.mem = mem.asMultiMediaMemoryResource();
        this.offset = offset;
        this.bytesPerLine = bytesPerLine;
        this.width = width;
        this.height = height;
    }
    
    abstract protected int getOffset(int x, int y);
    

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    public final void copyArea(int srcX, int srcY, int w, int h, int dstX,
            int dstY) {
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doCopyArea(srcX, srcY, w, h, dstX, dstY);
            }
        }
        // TODO Auto-generated method stub

    }
    
    /**
     * Draw an image to this surface
     * 
     * @param src
     * @param srcX
     *            The upper left x coordinate of the source
     * @param srcY
     *            The upper left y coordinate of the source
     * @param dstX
     *            The upper left x coordinate of the destination
     * @param dstY
     *            The upper left y coordinate of the destination
     * @param w
     * @param h
     */
    public final void drawImage(Raster src, int srcX, int srcY, int dstX,
            int dstY, int w, int h) {
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doDrawImage(src, srcX, srcY, dstX, dstY, w, h);
            }
        }
    }

    /**
     * Draw an image to this surface
     * 
     * @param src
     * @param srcX
     *            The upper left x coordinate of the source
     * @param srcY
     *            The upper left y coordinate of the source
     * @param dstX
     *            The upper left x coordinate of the destination
     * @param dstY
     *            The upper left y coordinate of the destination
     * @param w
     * @param h
     * @param bgColor
     *            The color to use for transparent pixels
     */
    public final void drawImage(Raster src, int srcX, int srcY, int dstX,
            int dstY, int w, int h, int bgColor) {
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doDrawImage(src, srcX, srcY, dstX, dstY, w, h, bgColor);
            }
        }
    }

    /**
     * Draw an raster of alpha values using a given color onto to this surface.
     * The given raster is a 1 band gray type raster.
     * 
     * @param raster
     * @param srcX
     *            The upper left x coordinate within the raster
     * @param srcY
     *            The upper left y coordinate within the raster
     * @param dstX
     *            The upper left destination x coordinate
     * @param dstY
     *            The upper left destination y coordinate
     * @param w
     * @param h
     * @param color
     *            The color to use.
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX,
            int srcY, int dstX, int dstY, int w, int h, int color) {
        if (tx != null) {
            Point2D dst = tx.transform(new Point2D.Float(dstX, dstY), null);
            dstX = (int) dst.getX();
            dstY = (int) dst.getY();
        }
        if ((dstY < this.height) && (dstX < this.width)) {
            if (dstX < 0) {
                srcX -= dstX;
                w += dstX;
                dstX = 0;
            }
            if (dstY < 0) {
                srcY -= dstY;
                h += dstY;
                dstY = 0;
            }
            w = Math.min(w, width - dstX);
            h = Math.min(h, height - dstY);
            if ((w > 0) && (h > 0)) {
                doDrawAlphaRaster(raster, srcX, srcY, dstX, dstY, w, h, color);
            }
        }
    }
    
    /**
     * Draw a line at location x,y that is w long using the given color.
     * 
     * @param x
     * @param y
     * @param w
     * @param color
     * @param mode
     * @see Surface#PAINT_MODE
     * @see Surface#XOR_MODE
     */
    public final void drawLine(int x, int y, int w, int color, int mode) {
        try {
            if ((y >= 0) && (y < height) && (x < width)) {
                if (x < 0) {
                    w += x;
                    x = 0;
                }
                w = Math.min(w, width - x);
                if (w > 0) {
                    doDrawLine(x, y, w, color, mode);
                }
            }
        } catch (IndexOutOfBoundsException ex) {
            log.error("Index out of bounds: x=" + x + ", y=" + y + ", w=" + w
                    + ", width=" + width + ", height=" + height);
        }
    }

    /**
     * Draw a pixel at location x,y using the given color.
     * 
     * @param x
     * @param y
     * @param color
     * @param mode
     * @see Surface#PAINT_MODE
     * @see Surface#XOR_MODE
     */
    public final void drawPixels(int x, int y, int count, int color, int mode) {
        try {
            if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
                doDrawPixels(x, y, count, color, mode);
            }
        } catch (IndexOutOfBoundsException ex) {
            log.error("Index out of bounds: x=" + x + ", y=" + y + ", width="
                    + width + ", height=" + height);
        }
    }	    
    
    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    protected void doCopyArea(int srcX, int srcY, int width, int height,
            int dstX, int dstY) {
        int srcOfs = getOffset(srcX, srcY);
        int dstOfs = getOffset(dstX, dstY);
        final int bytesForWidth = getBytesForWidth(width);
        
        for (int row = 0; row < height; row++) {
            mem.copy(srcOfs, dstOfs, bytesForWidth);
            srcOfs += bytesPerLine;
            dstOfs += bytesPerLine;
        }
    }
    
    abstract protected int getBytesForWidth(int width);

	protected abstract void doDrawAlphaRaster(Raster raster, int srcX,
            int srcY, int dstX, int dstY, int width, int height, int color);
    
    /**
     * Draw an image to this surface
     * 
     * @param src
     * @param srcX
     *            The upper left x coordinate of the source
     * @param srcY
     *            The upper left y coordinate of the source
     * @param dstX
     *            The upper left x coordinate of the destination
     * @param dstY
     *            The upper left y coordinate of the destination
     * @param width
     * @param height
     */
    protected abstract void doDrawImage(Raster src, int srcX, int srcY,
            int dstX, int dstY, int width, int height);

    /**
     * Draw an image to this surface
     * 
     * @param src
     * @param srcX
     *            The upper left x coordinate of the source
     * @param srcY
     *            The upper left y coordinate of the source
     * @param dstX
     *            The upper left x coordinate of the destination
     * @param dstY
     *            The upper left y coordinate of the destination
     * @param width
     * @param height
     * @param bgColor
     *            The color to use for transparent pixels
     */
    protected abstract void doDrawImage(Raster src, int srcX, int srcY,
            int dstX, int dstY, int width, int height, int bgColor);	
    
    /**
     * Draw a line at location x,y that is w long using the given color.
     * 
     * @param x
     * @param y
     * @param w
     * @param color
     * @param mode
     * @see Surface#PAINT_MODE
     * @see Surface#XOR_MODE
     */
    protected abstract void doDrawLine(int x, int y, int w, int color, int mode);

    /**
     * Draw a number of pixels at location x,y using the given color.
     * 
     * @param x
     * @param y
     * @param count
     * @param color
     * @param mode
     * @see Surface#PAINT_MODE
     * @see Surface#XOR_MODE
     */
    protected abstract void doDrawPixels(int x, int y, int count, int color,
            int mode);
    
}