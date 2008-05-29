/**
 *
 */
package org.jnode.awt.util;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.Raster;
import org.jnode.driver.video.Surface;
import org.jnode.system.MemoryResource;
import org.jnode.vm.Unsafe;

final class BitmapGraphics32bpp extends AbstractBitmapGraphics {

    private int[] pixelBuffer;

    private byte[] alphaBuffer;

    protected final int transparency;

    /**
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     * @param transparency
     */
    public BitmapGraphics32bpp(MemoryResource mem, int width, int height,
                               int offset, int bytesPerLine, int transparency) {
        super(mem, width, height, offset, bytesPerLine);
        this.transparency = transparency;
        Unsafe.debug("creating BitmapGraphics32bpp");
    }

    @Override
    protected int getOffset(int x, int y) {
        return offset + (y * bytesPerLine) + (x << 2);
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                               int dstY, int width, int height) {
        int dstOfs = getOffset(dstX, dstY);
        int y = srcY;

        final int[] buf = getPixelBuffer(width);
        for (int row = 0; row < height; row++) {
            src.getDataElements(srcX, y, width, 1, buf);
            if (transparency == Transparency.TRANSLUCENT)
                mem.setARGB32bpp(buf, 0, dstOfs, width);
            else
                mem.setInts(buf, 0, dstOfs, width);

            dstOfs += bytesPerLine;
            y++;
        }
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                               int dstY, int width, int height, int bgColor) {
        final int[] buf = new int[width];
        int dstOfs = getOffset(dstX, dstY);
        int y = srcY;

        for (int row = 0; row < height; row++) {
            src.getDataElements(srcX, y, width, 1, buf);
            mem.setInts(buf, 0, dstOfs, width);

            dstOfs += bytesPerLine;
            y++;
        }
    }

    /**
     * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
     *      int, int, int, int, int, int, int)
     */
    protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
                                     int dstX, int dstY, int width, int height, int color) {
        final byte[] buf = getAlphaBuffer(width);
        final int[] pixels = getPixelBuffer(width);
        int dstOfs = getOffset(dstX, dstY);
        int y = srcY;

        color &= 0x00FFFFFF;

        for (int row = height - 1; row >= 0; row--) {
            raster.getDataElements(srcX, y, width, 1, buf);

            for (int i = width - 1; i >= 0; i--) {
                final int alpha = (buf[i] & 0xFF);
                pixels[i] = (alpha << 24) | color;
            }
            mem.setARGB32bpp(pixels, 0, dstOfs, width);

            dstOfs += bytesPerLine;
            y++;
        }
    }

    protected void doDrawLine(int x, int y, int w, int color, int mode) {
        // System.out.println("doDrawLine" + x + "," + y + "," + w + "," +
        // color + "," + mode);
        final int ofs = getOffset(x, y);
        if (mode == Surface.PAINT_MODE) {
            mem.setInt(ofs, color, w);
        } else {
            mem.xorInt(ofs, color, w);
        }
    }

    protected final void doDrawPixels(int x, int y, int count, int color,
                                      int mode) {
        final int ofs = getOffset(x, y);
        // System.out.println("ofs=" + ofs);
        if (mode == Surface.PAINT_MODE) {
            mem.setInt(ofs, color, count);
        } else {
            mem.xorInt(ofs, color, count);
        }
    }

    public final int doGetPixel(int x, int y) {
        return mem.getInt(getOffset(x, y));
    }

    public final int[] doGetPixels(Rectangle r) {
        int x = r.x;
        int y = r.y;
        int w = r.width;
        int h = r.height;

        int srcOfs = getOffset(x, y);
        int dstOfs = 0;

        int[] ret = new int[w * h];
        for (int j = 0; j < h; j++) {
            mem.getInts(srcOfs, ret, dstOfs, w);

            srcOfs += bytesPerLine;
            dstOfs += w;
        }
        return ret;
    }

    private final int[] getPixelBuffer(int width) {
        if ((pixelBuffer == null) || (pixelBuffer.length < width)) {
            pixelBuffer = new int[width];
        }
        return pixelBuffer;
    }

    private final byte[] getAlphaBuffer(int width) {
        if ((alphaBuffer == null) || (alphaBuffer.length < width)) {
            alphaBuffer = new byte[width];
        }
        return alphaBuffer;
    }

    @Override
    protected int getBytesForWidth(int width) {
        return width << 2;
    }
}
