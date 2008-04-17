/**
 * 
 */
package org.jnode.awt.util;

import java.awt.Rectangle;
import java.awt.image.Raster;

import org.jnode.driver.video.Surface;
import org.jnode.system.MemoryResource;
import org.jnode.vm.Unsafe;

final class BitmapGraphics24bpp extends AbstractBitmapGraphics {

    /**
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    public BitmapGraphics24bpp(MemoryResource mem, int width, int height,
            int offset, int bytesPerLine) {
        super(mem, width, height, offset, bytesPerLine);
        Unsafe.debug("creating BitmapGraphics32bpp");                                                
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
            int dstY, int width, int height) {
        // TODO Implement me
        log.error("Not implemented");
        Unsafe.debug("doDrawImage Not implemented");
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
            int dstY, int width, int height, int bgColor) {
        // TODO Implement me
        log.error("Not implemented");
        Unsafe.debug("doDrawImage Not implemented");
    }

    /**
     * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
     *      int, int, int, int, int, int, int)
     */
    protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
            int dstX, int dstY, int width, int height, int color) {
        // TODO Implement me
        log.error("Not implemented");
        Unsafe.debug("doDrawImage Not implemented");
    }

    public int doGetPixel(int x, int y) {
        final int ofs = offset + (y * bytesPerLine) + (x * 3);
        return mem.getInt(ofs);
    }

    public int[] doGetPixels(Rectangle r) {
        int x = r.x;
        int y = r.y;
        int w = r.width;
        int h = r.height;
        int[] ret = new int[w*h];
        
        int resultIdx = 0;
        int ofs = getOffset(x, y);
        for(int i = 0; i < w; i++) {
        	
        	int idxLine = ofs;
            for(int j = 0; j < h; j++) {
                ret[resultIdx] = mem.getInt(ofs);
                
                resultIdx++;
                idxLine += 3;
            }
            
            ofs += bytesPerLine;
        }
        return ret;
    }

    protected void doDrawLine(int x, int y, int w, int color, int mode) {
        final int ofs = getOffset(x, y);
        
        if (mode == Surface.PAINT_MODE) {
            mem.setInt24(ofs, color, w);
        } else {
            mem.xorInt24(ofs, color, w);
        }
    }

    protected void doDrawPixels(int x, int y, int count, int color, int mode) {
        final int ofs = getOffset(x, y);
        
        if (mode == Surface.PAINT_MODE) {
            mem.setInt24(ofs, color, count);
        } else {
            mem.xorInt24(ofs, color, count);
        }
    }

	@Override
	protected int getOffset(int x, int y) {
		return offset + (y * bytesPerLine) + (x * 3);
	}
	
	@Override
	protected int getBytesForWidth(int width) {
		return width * 3;
	}		
}