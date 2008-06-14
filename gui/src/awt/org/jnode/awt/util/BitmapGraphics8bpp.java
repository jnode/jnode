/**
 *
 */
package org.jnode.awt.util;

import java.awt.Rectangle;
import java.awt.image.Raster;
import org.jnode.driver.video.Surface;
import org.jnode.system.MemoryResource;

final class BitmapGraphics8bpp extends AbstractBitmapGraphics {

    /**
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    public BitmapGraphics8bpp(MemoryResource mem, int width, int height,
                              int offset, int bytesPerLine) {
        super(mem, width, height, offset, bytesPerLine);
        //Unsafe.debug("creating BitmapGraphics8bpp");
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                               int dstY, int width, int height) {
        final byte[] buf = new byte[width];
        int dstOfs = getOffset(dstX, dstY);

        final int maxRow = srcY + height;
        for (int row = srcY; row < maxRow; row++) {
            src.getDataElements(srcX, row, width, 1, buf);
            mem.setBytes(buf, 0, dstOfs, width);

            dstOfs += bytesPerLine;
        }
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                               int dstY, int width, int height, int bgColor) {

        // TODO implement me
        doDrawImage(src, srcX, srcY, dstX, dstY, width, height);
    }

    /**
     * @see org.jnode.awt.util.AbstractBitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
     * int, int, int, int, int, int, int)
     */
    protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
                                     int dstX, int dstY, int width, int height, int color) {

        final byte[] buf = new byte[width];
        //TODO this method is still buggy, the "new" font render does not work with it.
        color &= 0x00FFFFFF;
        int dstOfs = getOffset(dstX, dstY);

        for (int row = height - 1; row >= 0; row--) {
            raster.getDataElements(srcX, srcY + row, width, 1, buf);

            for (int i = width - 1; i >= 0; i--) {
                final int alpha = (buf[i] & 0xFF);
                buf[i] = (byte) ((alpha ^ (color & 0xFF) ^ ((color >> 8) & 0xFF) ^ ((color >> 16) & 0xFF)));
            }
            mem.setBytes(buf, 0, dstOfs, width);

            dstOfs += bytesPerLine;
        }
    }

    public int doGetPixel(int x, int y) {
        // TODO Implement me
        log.error("Not implemented");
        org.jnode.vm.Unsafe.debug("doGetPixel Not implemented");
        return 0;
    }

    public int[] doGetPixels(Rectangle r) {
        // TODO Implement me
        log.error("Not implemented");
        org.jnode.vm.Unsafe.debug("doGetPixels Not implemented");
        return new int[0];
    }

    protected void doDrawLine(int x, int y, int w, int color, int mode) {
        final int ofs = getOffset(x, y);

        if (mode == Surface.PAINT_MODE) {
            mem.setByte(ofs, (byte) color, w);
        } else {
            mem.xorByte(ofs, (byte) color, w);
        }
    }

    protected void doDrawPixels(int x, int y, int count, int color, int mode) {
        final int ofs = getOffset(x, y);

        if (mode == Surface.PAINT_MODE) {
            mem.setByte(ofs, (byte) color, count);
        } else {
            mem.xorByte(ofs, (byte) color, count);
        }
    }

    @Override
    protected int getOffset(int x, int y) {
        return offset + (y * bytesPerLine) + x;
    }

    @Override
    protected int getBytesForWidth(int width) {
        return width;
    }
}
