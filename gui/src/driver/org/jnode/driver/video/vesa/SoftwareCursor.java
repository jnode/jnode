package org.jnode.driver.video.vesa;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;

import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;
import org.jnode.driver.video.Surface;

public class SoftwareCursor extends BitmapGraphics implements HardwareCursorAPI {
	private BitmapGraphics graphics;
	private HardwareCursorImage cursorImage;
	private int cursorX = -1;
	private int cursorY = -1;
	private boolean cursorVisible = false;
	private int[] screenBackup;
	
    public SoftwareCursor(BitmapGraphics graphics) {
    	setBitmapGraphics(graphics);
    }

    public void setBitmapGraphics(BitmapGraphics graphics)
    {
    	if(this.graphics != graphics)
    	{
	        this.graphics = graphics;
	        cursorX = Math.min(graphics.getWidth() -1, cursorX);
	        cursorY = Math.min(graphics.getHeight() -1, cursorY);
	        
	        //TODO test when screen resolution is changing    		
    		if(cursorVisible)
    		{
    			showCursor();
    		}
    	}
    }
    
	@Override
	public void copyArea(int srcX, int srcY, int w, int h, int dstX,
			int dstY) {

		//TODO don't take cursor pixels for the source
		
		final boolean intersects = intersectsCursor(dstX, dstY, w, h);
		if(intersects)
		{
			hideCursor();
		}
		
		graphics.copyArea(srcX, srcY, w, h, dstX, dstY);
		
		if(intersects)
		{
			showCursor();			
		}		
	}

	@Override
	public int doGetPixel(int x, int y) {
		//TODO don't take cursor pixels
		return graphics.doGetPixel(x, y);
	}

	@Override
	public int[] doGetPixels(Rectangle r) {
		//TODO don't take cursor pixels
		return graphics.doGetPixels(r);
	}

	@Override
	public void drawAlphaRaster(Raster raster, AffineTransform tx,
			int srcX, int srcY, int dstX, int dstY, int w, int h, int color) {
		
		final boolean intersects = intersectsCursor(dstX, dstY, w, h);
		if(intersects)
		{
			hideCursor();
		}
		
		graphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY, w, h, color);
		
		if(intersects)
		{
			showCursor();			
		}		
	}

	@Override
	public void drawImage(Raster src, int srcX, int srcY, int dstX,
			int dstY, int w, int h) {
		final boolean intersects = intersectsCursor(dstX, dstY, w, h);
		if(intersects)
		{
			hideCursor();
		}
		
		graphics.drawImage(src, srcX, srcY, dstX, dstY, w, h);
		
		if(intersects)
		{
			showCursor();			
		}		
	}

	@Override
	public void drawImage(Raster src, int srcX, int srcY, int dstX,
			int dstY, int w, int h, int bgColor) {
		final boolean intersects = intersectsCursor(dstX, dstY, w, h);
		if(intersects)
		{
			hideCursor();
		}
		
		graphics.drawImage(src, srcX, srcY, dstX, dstY, w, h, bgColor);
		
		if(intersects)
		{
			showCursor();			
		}		
	}

	@Override
	public void drawLine(int x, int y, int w, int color, int mode) {
		final boolean intersects = intersectsCursor(x, y, w, 1);
		if(intersects)
		{
			hideCursor();
		}
		
		graphics.drawLine(x, y, w, color, mode);
		
		if(intersects)
		{
			showCursor();			
		}		
	}

	@Override
	public void drawPixels(int x, int y, int count, int color, int mode) {
		final boolean intersects = intersectsCursor(x, y, count, 1);
		if(intersects)
		{
			hideCursor();
		}
		
		graphics.drawPixels(x, y, count, color, mode);
		
		if(intersects)
		{
			showCursor();			
		}		
	}

	public int getWidth()
	{
		return graphics.getWidth();
	}
	
	public int getHeight()
	{
		return graphics.getHeight();
	}

	public void setCursorImage(HardwareCursor cursor) {
		final HardwareCursorImage cursImage = cursor.getImage(16, 16); 

		if(this.cursorImage != cursImage)
		{			
			// we assume here that cursor has always the same size (that size by the call "cursor.getImage(w, h)" above)
			if(screenBackup == null)
			{
				screenBackup = new int[cursImage.getWidth() * cursImage.getHeight()];
			}
			else
			{
				hideCursor();				
			}
			
			this.cursorImage = cursImage;			
			showCursor();
		}
	}

	public void setCursorPosition(int x, int y) {		
		hideCursor();
		this.cursorX = x - cursorImage.getHotSpotX();
		this.cursorY = y - cursorImage.getHotSpotY();
		showCursor();
	}

	public void setCursorVisible(boolean visible) {
		this.cursorVisible = visible;
		showCursor();
	}
	
	private boolean intersectsCursor(int x, int y, int width, int height)
	{
		boolean intersects = false;
		
		if(cursorVisible && (width > 0) && (height > 0))
		{
			//TODO implement it
			intersects = false;
//			final int endCursorX = cursorX + cursorImage.getWidth(); 
//			final int endCursorY = cursorY + cursorImage.getHeight();
//			
//			if(((x + width) > cursorX) && ((y + height) > cursorY) &&
//			   (x < endCursorX) && (y < endCursorY))
//			{
//				// given area contains cursor area
//				intersects = true;
//			}
//			else
//			{
//				final int endX = x + width;
//				final int endY = y + height;
//				
//				intersects = isInside(x,    y,    cursorX, cursorY, endCursorX, endCursorY) ||
//							 isInside(x,    endY, cursorX, cursorY, endCursorX, endCursorY) ||
//							 isInside(endX, endY, cursorX, cursorY, endCursorX, endCursorY) ||
//							 isInside(endX, y,    cursorX, cursorY, endCursorX, endCursorY);
//			}
		}
		
		return intersects;
	}
	
	/**
	 * 
	 * @param value
	 * @param min inclusive
	 * @param max exclusive
	 * @return
	 */
	private boolean isInside(int value, int min, int max)
	{
		return (value >= min) && (value < max);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @param xmin inclusive
	 * @param ymin inclusive
	 * @param xmax exclusive
	 * @param ymax exclusive
	 * @return
	 */
	private boolean isInside(int x, int y, int xmin, int ymin, int xmax, int ymax)
	{
		return isInside(x, xmin, xmax) && isInside(y, ymin, ymax);
	}

	private void showCursor()
	{
		if((cursorImage != null) && (screenBackup != null))
		{
			int index = 0;
			for(int y = cursorY ; y < (cursorY + cursorImage.getHeight()) ; y++)
			{
				for(int x = cursorX ; x < (cursorX + cursorImage.getWidth()) ; x++)
				{
					screenBackup[index] = graphics.doGetPixel(x, y);
					index++;
				}				
			}		

			putPixels(cursorImage.getImage());		
		}
	}
	
	private void hideCursor()
	{
		if((cursorImage != null) && (screenBackup != null))
		{
			putPixels(screenBackup);
		}
	}
	
	private void putPixels(int[] pixels)
	{
		int index = 0;
		for(int y = cursorY ; y < (cursorY + cursorImage.getHeight()) ; y++)
		{
			for(int x = cursorX ; x < (cursorX + cursorImage.getWidth()) ; x++)
			{
				graphics.drawPixels(x, y, 1, pixels[index], Surface.PAINT_MODE);
				index++;
			}				
		}		
	}
}

