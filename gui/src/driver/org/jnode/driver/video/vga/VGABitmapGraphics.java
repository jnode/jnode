/*
 * $Id$
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
	public VGABitmapGraphics(StandardVGA vga, StandardVGAIO vgaIO, int width,
			int height, int offset, int bytesPerLine) {
		super(vga.getVgaMem(), width, height, offset, bytesPerLine);
		this.vgaIO = vgaIO;
	}

	private int divroundup(int num, int div) {
		if (num % div == 0) {
			return num / div;
		} else {
			return (num / div) + 1;
		}
	}

	/**
	 * @see org.jnode.awt.util.BitmapGraphics#doCopyArea(int, int, int, int,
	 *      int, int)
	 */
	protected void doCopyArea(int x, int y, int width, int height, int dx,
			int dy) {
		// TODO Implement me
	}

	/**
	 * @see org.jnode.awt.util.BitmapGraphics#doDrawImage(java.awt.image.Raster,
	 *      int, int, int, int, int, int)
	 */
	protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
			int dstY, int width, int height) {
		vgaIO.setGRAF(1, 0);
		vgaIO.setGRAF(8, 0xFF);

		final int pWidth = divroundup(width, 8);
		final byte[] plane0 = new byte[pWidth];
		final byte[] plane1 = new byte[pWidth];
		final byte[] plane2 = new byte[pWidth];
		final byte[] plane3 = new byte[pWidth];
		final byte[] buf = new byte[width];
		for (int row = 0; row < height; row++) {
			final int y = dstY + row;
			src.getDataElements(srcX, srcY + row, width, 1, buf);
			for (int col = 0; col < width; col++) {
				final int bit = 0x80 >> ((dstX + col) & 7);
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

			final int dstOfs = y * 80 + (dstX >> 3);

			vgaIO.setSEQ(2, 1); //plane 0
			vgaIO.setGRAF(4, 0);
			mem.setBytes(plane0, 0, dstOfs, pWidth);

			vgaIO.setSEQ(2, 2); //plane 1
			vgaIO.setGRAF(4, 1);
			mem.setBytes(plane1, 0, dstOfs, pWidth);

			vgaIO.setSEQ(2, 4); //plane 2
			vgaIO.setGRAF(4, 2);
			mem.setBytes(plane2, 0, dstOfs, pWidth);

			vgaIO.setSEQ(2, 8); //plane 3
			vgaIO.setGRAF(4, 3);
			mem.setBytes(plane3, 0, dstOfs, pWidth);

			for (int col = 0; col < pWidth; col++) {
				plane0[col] = 0;
				plane1[col] = 0;
				plane2[col] = 0;
				plane3[col] = 0;
			}
		}
		vgaIO.setSEQ(2, 0x0F); //restore
		vgaIO.setGRAF(1, 0x0F);
	}

	/**
	 * @see org.jnode.awt.util.BitmapGraphics#doDrawImage(java.awt.image.Raster,
	 *      int, int, int, int, int, int, int)
	 */
	protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
			int dstY, int width, int height, int bgColor) {
		doDrawImage(src, srcX, srcY, dstX, dstY, width, height);
	}

	/**
	 * @see org.jnode.awt.util.BitmapGraphics#doDrawLine(int, int, int, int,
	 *      int)
	 */
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
				final int bit = 0x80 >> (x & 7);
				final int offset = ofsY + (x >> 3);
				vgaIO.setGRAF(8, bit);
				mem.orByte(offset, (byte) 0xFF, 1);
				lineWidth--;
				x++;
			}
		}
	}

	/**
	 * @see org.jnode.awt.util.BitmapGraphics#doDrawPixels(int, int, int, int,
	 *      int)
	 */
	protected void doDrawPixels(int x, int y, int count, int color, int mode) {
		for (int i = 0; i < count; i++) {
			final int bit = 0x80 >> (x & 7);
			final int offset = y * 80 + (x >> 3);
			vgaIO.setGRAF(8, bit);
			mem.orByte(offset, (byte) 0xFF, 1);
			x++;
		}
	}
	
	
    /**
     * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster, int, int, int, int, int, int, int)
     */
    protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
            int dstX, int dstY, int width, int height, int color) {
        // TODO Implement me
    }

    public int doGetPixel(int x, int y) {
        // TODO Implement me
        return 0;
    }

    public int[] doGetPixels(Rectangle r) {
        // TODO Implement me
        return new int[0];
    }

	@Override
	protected int getBytesForWidth(int width) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int getOffset(int x, int y) {
		// TODO Auto-generated method stub
		return 0;
	}
}
