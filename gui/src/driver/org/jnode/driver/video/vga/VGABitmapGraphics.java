/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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

package org.jnode.driver.video.vga;

import java.awt.Rectangle;
import java.awt.image.Raster;

import org.jnode.awt.util.AbstractBitmapGraphics;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VGABitmapGraphics extends AbstractBitmapGraphics {

    private final StandardVGAIO vgaIO;

    /**
     * @param vga
     * @param vgaIO
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    public VGABitmapGraphics(StandardVGA vga, StandardVGAIO vgaIO, int width, int height,
                             int offset, int bytesPerLine) {
        super(vga.getVgaMem(), width, height, offset, bytesPerLine);
        this.vgaIO = vgaIO;
    }

    protected void doCopyArea(int x, int y, int width, int height, int dx, int dy) {
        // TODO Implement me
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width,
                               int height) {
        vgaIO.setGRAF(1, 0);
        vgaIO.setGRAF(8, 0xFF);

        // round up the result of width/8
        int pWidth = width >> 3; // (width >> 3) == (width / 8)
        if ((width & 7) != 0) { // (width & 7) == (width % 8)
            pWidth++;
        }

        final byte[] plane0 = new byte[pWidth];
        final byte[] plane1 = new byte[pWidth];
        final byte[] plane2 = new byte[pWidth];
        final byte[] plane3 = new byte[pWidth];
        final byte[] buf = new byte[width];
        for (int row = 0; row < height; row++) {
            final int y = dstY + row;
            src.getDataElements(srcX, srcY + row, width, 1, buf);
            for (int col = 0; col < width; col++) {
                final int bit = getBit(dstX + col);
                final int pixel = buf[col];
                final int i = (col >> 3);
                if ((pixel & 0x01) != 0) {
                    plane0[i] |= bit;
                }
                if ((pixel & 0x02) != 0) {
                    plane1[i] |= bit;
                }
                if ((pixel & 0x04) != 0) {
                    plane2[i] |= bit;
                }
                if ((pixel & 0x08) != 0) {
                    plane3[i] |= bit;
                }
            }

            final int dstOfs = getOffset(dstX, y);

            vgaIO.setSEQ(2, 1); // plane 0
            vgaIO.setGRAF(4, 0);
            mem.setBytes(plane0, 0, dstOfs, pWidth);

            vgaIO.setSEQ(2, 2); // plane 1
            vgaIO.setGRAF(4, 1);
            mem.setBytes(plane1, 0, dstOfs, pWidth);

            vgaIO.setSEQ(2, 4); // plane 2
            vgaIO.setGRAF(4, 2);
            mem.setBytes(plane2, 0, dstOfs, pWidth);

            vgaIO.setSEQ(2, 8); // plane 3
            vgaIO.setGRAF(4, 3);
            mem.setBytes(plane3, 0, dstOfs, pWidth);

            for (int col = 0; col < pWidth; col++) {
                plane0[col] = 0;
                plane1[col] = 0;
                plane2[col] = 0;
                plane3[col] = 0;
            }
        }
        vgaIO.setSEQ(2, 0x0F); // restore
        vgaIO.setGRAF(1, 0x0F);
    }

    protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width,
                               int height, int bgColor) {
        //TODO use bgColor parameter
        doDrawImage(src, srcX, srcY, dstX, dstY, width, height);
    }

    protected void doDrawLine(int x, int y, int lineWidth, int color, int mode) {
        final int ofsY = y * 80;
        lineWidth = Math.min(this.width - x, lineWidth);
        while (lineWidth > 0) {
            if (((x & 7) == 0) && (lineWidth >= 8)) {
                final int count = lineWidth >> 3;
                final int bits = count << 3;
                final int offset = ofsY + (x >> 3);
                vgaIO.setGRAF(8, 0xFF);
                mem.orByte(offset, (byte) 0xFF, count);
                lineWidth -= bits;
                x += bits;
            } else {
                final int offset = ofsY + (x >> 3);
                vgaIO.setGRAF(8, getBit(x));
                mem.orByte(offset, (byte) 0xFF, 1);
                lineWidth--;
                x++;
            }
        }
    }

    protected void doDrawPixels(int x, int y, int count, int color, int mode) {
        //TODO (do)DrawLine/(do)doDrawPixels appear to be duplicates at higher level => remove one
        doDrawLine(x, y, count, color, mode);
    }

    protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY, int dstX, int dstY,
                                     int width, int height, int color) {
        //TODO should we support alpha with only a fixed set of 16 colors ?
        doDrawImage(raster, srcX, srcY, dstX, dstY, width, height, color);
    }

    public int doGetPixel(int x, int y) {
        // TODO Implement me
        return 0;
    }

    public int[] doGetPixels(Rectangle r) {
        final int[] result = new int[r.width * r.height];
        final int xmax = r.x + r.width;
        final int ymax = r.y + r.height;
        int i = 0;
        for (int y = r.y; y < ymax; y++) {
            for (int x = r.x; x < xmax; x++) {
                result[i++] = doGetPixel(x, y);
            }
        }
        return result;
    }

    @Override
    protected int getBytesForWidth(int width) {
        return (width * bytesPerLine) / this.width;
    }

    @Override
    protected int getOffset(int x, int y) {
        return y * 80 + (x >> 3);
    }

    private int getBit(int x) {
        return 0x80 >> (x & 7);
    }
}
