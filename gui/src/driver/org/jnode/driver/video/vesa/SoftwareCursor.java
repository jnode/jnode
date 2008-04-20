package org.jnode.driver.video.vesa;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.Raster;

import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;
import org.jnode.driver.video.Surface;
import org.jnode.vm.Unsafe;

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
		try
		{
			final HardwareCursorImage cursImage = cursor.getImage(16, 16); 
	
			if(this.cursorImage != cursImage)
			{
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
		catch(Throwable t)
		{
			Unsafe.debugStackTrace();
			Unsafe.debug("\nerror in setCursorImage ("+t.getClass().getName()+") "+t.getMessage()+"\n");
		}
	}

	public void setCursorPosition(int x, int y) {		
		try
		{
			if((this.cursorX  != x) || (this.cursorY != y))
			{
				hideCursor();
				this.cursorX = x - cursorImage.getHotSpotX();
				this.cursorY = y - cursorImage.getHotSpotY();
				showCursor();
			}
		}
		catch(Throwable t)
		{
			Unsafe.debugStackTrace();
			Unsafe.debug("\nerror in setCursorPosition ("+t.getClass().getName()+") "+t.getMessage()+"\n");
		}
	}

	public void setCursorVisible(boolean visible) {
		try		
		{
			if(this.cursorVisible != visible)
			{
				this.cursorVisible = visible;
				
				if(visible)
				{
					showCursor();					
				}
				else
				{
					hideCursor();
				}
			}
		}
		catch(Throwable t)
		{
			Unsafe.debugStackTrace();
			Unsafe.debug("\nerror in setCursorVisible ("+t.getClass().getName()+") "+t.getMessage()+"\n");
		}
	}

	private boolean intersectsCursor(int x, int y, int width, int height)
	{
		boolean intersects = false;
		
		if(cursorVisible && (width > 0) && (height > 0))
		{
			Rectangle cursorArea = new Rectangle(cursorX, cursorY, cursorImage.getWidth(), cursorImage.getHeight());
			Rectangle screenArea = new Rectangle(x, y, width, height);
			intersects = cursorArea.intersects(screenArea);
		}
		
		return intersects;
	}
	
	private void showCursor()
	{
		if((cursorImage != null) && (screenBackup != null))
		{
//			screenBackup = graphics.doGetPixels(new Rectangle(cursorX, cursorY, cursorImage.getWidth(), cursorImage.getHeight())); 
			int index = 0;
			for(int y = cursorY ; y < (cursorY + cursorImage.getHeight()) ; y++)
			{
				for(int x = cursorX ; x < (cursorX + cursorImage.getWidth()) ; x++)
				{
					screenBackup[index] = graphics.doGetPixel(x, y);
					index++;
				}				
			}		
			
			putPixels(cursorImage.getImage(), screenBackup);		
		}
	}
	
	private void hideCursor()
	{
		if((cursorImage != null) && (screenBackup != null))
		{
			putPixels(screenBackup, null);
		}
	}
	
	private void putPixels(int[] pixels, int[] background)
	{
		final int maxY = Math.min(cursorY + cursorImage.getHeight(), graphics.getHeight());
		final int maxX = Math.min(cursorX + cursorImage.getWidth(), graphics.getWidth());
		
		int index = 0;
		for(int y = cursorY ; y < maxY ; y++)
		{
			for(int x = cursorX ; x < maxX ; x++)
			{
				int color;
				if(background == null)
				{
					color = pixels[index];
				}
				else
				{
					final int c = pixels[index];
					final boolean isTransparent = (c == 0); 
					color = isTransparent ? background[index] : c;
				}
				
				graphics.drawPixels(x, y, 1, color, Surface.PAINT_MODE);
				index++;
				
				if(index >= pixels.length)
				{
					return;
				}
			}				
		}		
	}
}

