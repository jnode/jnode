/**
 *
 */
package org.jnode.awt.util;

import java.awt.Rectangle;
import java.awt.image.Raster;
import org.jnode.driver.video.Surface;
import org.jnode.system.MemoryResource;
import org.jnode.vm.Unsafe;

final class BitmapGraphics16bpp extends AbstractBitmapGraphics {

    /**
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    public BitmapGraphics16bpp(MemoryResource mem, int width, int height,
                               int offset, int bytesPerLine) {
        super(mem, width, height, offset, bytesPerLine);
        Unsafe.debug("created BitmapGraphics16bpp");
    }

    @Override
    protected int getOffset(int x, int y) {
        return offset + (y * bytesPerLine) + (x << 1);
    }

    protected final void doDrawImage(Raster src, int srcX, int srcY,
                                     int dstX, int dstY, int width, int height) {
        Unsafe.debug("BitmapGraphics16bpp: doDrawImage");

        int dstOfs = getOffset(dstX, dstY);

        final int[] buf = new int[width];
        for (int row = 0; row < height; row++) {
            src.getDataElements(srcX, srcY + row, width, 1, buf);

            int idxLine = dstOfs;
            for (int i = 0; i < width; i++) {
                final int color = buf[i];
                final int r = color >> 24;
                final int g = (color >> 16) & 0xFF;
                final int b = (color >> 8) & 0xFF;
                final int v = ((r >> 3) << 11) | ((g >> 3) << 6) | (b >> 2);
                mem.setShort(idxLine, (short) v);

                idxLine += 2;
            }

            dstOfs += bytesPerLine;
        }
        log.error("Wrong image colors in BitmapGraphics.doDrawImage!");
    }

    /**
     * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
     *      int, int, int, int, int, int, int)
     */
    protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
                                     int dstX, int dstY, int width, int height, int color) {
        // TODO Implement me
        log.error("Not implemented");
        Unsafe.debug("BitmapGraphics16bpp: doDrawAlphaRaster");

        doDrawImage(raster, srcX, srcY, dstX, dstY, width, height);
    }

    protected final void doDrawImage(Raster src, int srcX, int srcY,
                                     int dstX, int dstY, int width, int height, int bgColor) {
        // TODO Check if it's API conform to disregard bgColor.. 32bit impl does ignore it
        Unsafe.debug("BitmapGraphics16bpp: doDrawImage");
        log.error("Not implemented");
        doDrawImage(src, srcX, srcY, dstX, dstY, width, height);
    }

    public int doGetPixel(int x, int y) {
        Unsafe.debug("BitmapGraphics16bpp: doGetPixel");
        return mem.getShort(offset + (y * bytesPerLine) + (x << 1));
    }

    public int[] doGetPixels(Rectangle r) {
        Unsafe.debug("BitmapGraphics16bpp: doGetPixels");
        int x = r.x;
        int y = r.y;
        int w = r.width;
        int h = r.height;
        int[] result = new int[w * h];
        int ofs = getOffset(x, y);

        int idxResult = 0;
        for (int i = 0; i < h; i++) {

            int idxLine = ofs;
            for (int j = 0; j < w; j++) {
                result[idxResult] = 0xFFFF & mem.getShort(idxLine);

                idxLine += 2;
                idxResult++;
            }

            ofs += bytesPerLine;
        }
        return result;
    }

    protected final void doDrawLine(int x, int y, int w, int color, int mode) {
        Unsafe.debug("BitmapGraphics16bpp: doDrawLine");
        final int ofs = getOffset(x, y);

        if (mode == Surface.PAINT_MODE) {
            mem.setShort(ofs, (short) color, w);
        } else {
            mem.xorShort(ofs, (short) color, w);
        }
    }

    protected final void doDrawPixels(int x, int y, int count, int color,
                                      int mode) {
        Unsafe.debug("BitmapGraphics16bpp: doDrawPixels");
        final int ofs = getOffset(x, y);

        if (mode == Surface.PAINT_MODE) {
            mem.setShort(ofs, (short) color, count);
        } else {
            mem.xorShort(ofs, (short) color, count);
        }
    }

    @Override
    protected int getBytesForWidth(int width) {
        return width << 1;
    }
}
