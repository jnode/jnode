/*
 * $Id$
 */
package org.jnode.awt.util;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.driver.video.Surface;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class BitmapGraphics {

	/** My logger */
	protected final Logger log = Logger.getLogger(getClass());
	protected final MemoryResource mem;
	/** Offset of first pixel in mem (in bytes) */
	protected final int offset;
	protected final int bytesPerLine;
	protected final int width;
	protected final int height;

	/**
	 * Create a new instance
	 * @param mem
	 * @param width
	 * @param height
	 * @param offset
	 * @param bytesPerLine
	 */
	protected BitmapGraphics(MemoryResource mem, int width, int height, int offset, int bytesPerLine) {
		this.mem = mem;
		this.offset = offset;
		this.bytesPerLine = bytesPerLine;
		this.width = width;
		this.height = height;
	}

	/**
	 * Create a new instance for 8 bits/pixel layout
	 * @param mem
	 * @param width
	 * @param height
	 * @param bytesPerLine
	 * @param offset
	 * @return The created instance
	 */
	public static BitmapGraphics create8bppInstance(MemoryResource mem, int width, int height, int bytesPerLine, int offset) {
		return new BitmapGraphics8bpp(mem, width, height, offset, bytesPerLine);
	}

	/**
	 * Create a new instance for 16 bits/pixel layout
	 * @param mem
	 * @param width
	 * @param height
	 * @param bytesPerLine
	 * @param offset
	 * @return The created instance
	 */
	public static BitmapGraphics create16bppInstance(MemoryResource mem, int width, int height, int bytesPerLine, int offset) {
		return new BitmapGraphics16bpp(mem, width, height, offset, bytesPerLine);
	}

	/**
	 * Create a new instance for 24 bits/pixel layout
	 * @param mem
	 * @param width
	 * @param height
	 * @param bytesPerLine
	 * @param offset
	 * @return The created instance
	 */
	public static BitmapGraphics create24bppInstance(MemoryResource mem, int width, int height, int bytesPerLine, int offset) {
		return new BitmapGraphics24bpp(mem, width, height, offset, bytesPerLine);
	}

	/**
	 * Create a new instance for 32 bits/pixel layout
	 * @param mem
	 * @param width
	 * @param height
	 * @param bytesPerLine
	 * @param offset
	 * @return The created instance
	 */
	public static BitmapGraphics create32bppInstance(MemoryResource mem, int width, int height, int bytesPerLine, int offset) {
		return new BitmapGraphics32bpp(mem, width, height, offset, bytesPerLine);
	}

	/**
	 * Create a new instance for a given DataBuffer
	 * @param dataBuffer
	 * @param width
	 * @param height
	 * @param bytesPerLine
	 * @return The created instance
	 */
	public static BitmapGraphics createInstance(DataBuffer dataBuffer, int width, int height, int bytesPerLine) {
		final ResourceManager rm;
		try {
			rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
		} catch (NamingException ex) {
			throw new RuntimeException("Cannot find ResourceManager", ex);
		}
		final int dbOffset = dataBuffer.getOffset();
		switch (dataBuffer.getDataType()) {
			case DataBuffer.TYPE_BYTE :
				{
					final byte[] data = ((DataBufferByte) dataBuffer).getData();
					return new BitmapGraphics8bpp(rm.asMemoryResource(data), width, height, dbOffset, bytesPerLine);
				}
			case DataBuffer.TYPE_SHORT :
				{
					final short[] data = ((DataBufferShort) dataBuffer).getData();
					return new BitmapGraphics16bpp(rm.asMemoryResource(data), width, height, dbOffset * 2, bytesPerLine);
				}
			case DataBuffer.TYPE_USHORT :
				{
					final short[] data = ((DataBufferUShort) dataBuffer).getData();
					return new BitmapGraphics16bpp(rm.asMemoryResource(data), width, height, dbOffset * 2, bytesPerLine);
				}
			case DataBuffer.TYPE_INT :
				{
					final int[] data = ((DataBufferInt) dataBuffer).getData();
					return new BitmapGraphics32bpp(rm.asMemoryResource(data), width, height, dbOffset * 4, bytesPerLine);
				}
			default :
				{
					throw new RuntimeException("Unimplemented databuffer type " + dataBuffer.getDataType());
				}
		}
	}

	/**
	 * Draw a pixel at location x,y using the given color.
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
			log.error("Index out of bounds: x=" + x + ", y=" + y + ", width=" + width + ", height=" + height);
		}
	}

	/**
	 * Draw a line at location x,y that is w long using the given color.
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
			log.error("Index out of bounds: x=" + x + ", y=" + y + ", w=" + w + ", width=" + width + ", height=" + height);
		}
	}

	/**
	 * Draw an image to this surface
	 * @param src
	 * @param srcX The upper left x coordinate of the source
	 * @param srcY The upper left y coordinate of the source
	 * @param dstX The upper left x coordinate of the destination
	 * @param dstY The upper left y coordinate of the destination
	 * @param w
	 * @param h
	 */
	public final void drawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int w, int h) {
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
	 * @param src
	 * @param srcX The upper left x coordinate of the source
	 * @param srcY The upper left y coordinate of the source
	 * @param dstX The upper left x coordinate of the destination
	 * @param dstY The upper left y coordinate of the destination
	 * @param w
	 * @param h
	 * @param bgColor The color to use for transparent pixels
	 */
	public final void drawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int w, int h, int bgColor) {
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
	 * Draw a number of pixels at location x,y using the given color.
	 * @param x
	 * @param y
	 * @param count
	 * @param color
	 * @param mode
	 * @see Surface#PAINT_MODE
	 * @see Surface#XOR_MODE
	 */
	protected abstract void doDrawPixels(int x, int y, int count, int color, int mode);

	/**
	 * Draw a line at location x,y that is w long using the given color.
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
	 * Draw an image to this surface
	 * @param src
	 * @param srcX The upper left x coordinate of the source
	 * @param srcY The upper left y coordinate of the source
	 * @param dstX The upper left x coordinate of the destination
	 * @param dstY The upper left y coordinate of the destination
	 * @param width
	 * @param height
	 */
	protected abstract void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height);

	/**
	 * Draw an image to this surface
	 * @param src
	 * @param srcX The upper left x coordinate of the source
	 * @param srcY The upper left y coordinate of the source
	 * @param dstX The upper left x coordinate of the destination
	 * @param dstY The upper left y coordinate of the destination
	 * @param width
	 * @param height
	 * @param bgColor The color to use for transparent pixels
	 */
	protected abstract void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height, int bgColor);

	static final class BitmapGraphics8bpp extends BitmapGraphics {

		/**
		 * @param mem
		 * @param width
		 * @param height
		 * @param offset
		 * @param bytesPerLine
		 */
		public BitmapGraphics8bpp(MemoryResource mem, int width, int height, int offset, int bytesPerLine) {
			super(mem, width, height, offset, bytesPerLine);
		}

		protected void doDrawPixels(int x, int y, int count, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + x;
			if (mode == Surface.PAINT_MODE) {
				mem.setByte(ofs, (byte) color, count);
			} else {
				mem.xorByte(ofs, (byte) color, count);
			}
		}

		protected void doDrawLine(int x, int y, int w, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + x;
			if (mode == Surface.PAINT_MODE) {
				mem.setByte(ofs, (byte) color, w);
			} else {
				mem.xorByte(ofs, (byte) color, w);
			}
		}

		protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height) {
			// TODO Implement me
			log.error("Not implemented");
		}

		protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height, int bgColor) {
			// TODO Implement me
			log.error("Not implemented");
		}
	}

	static final class BitmapGraphics16bpp extends BitmapGraphics {

		/**
		 * @param mem
		 * @param width
		 * @param height
		 * @param offset
		 * @param bytesPerLine
		 */
		public BitmapGraphics16bpp(MemoryResource mem, int width, int height, int offset, int bytesPerLine) {
			super(mem, width, height, offset, bytesPerLine);
		}

		protected final void doDrawPixels(int x, int y, int count, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + (x << 1);
			if (mode == Surface.PAINT_MODE) {
				mem.setShort(ofs, (short) color, count);
			} else {
				mem.xorShort(ofs, (short) color, count);
			}
		}

		protected final void doDrawLine(int x, int y, int w, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + (x << 1);
			if (mode == Surface.PAINT_MODE) {
				mem.setShort(ofs, (short) color, w);
			} else {
				mem.xorShort(ofs, (short) color, w);
			}
		}

		protected final void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height) {
			// TODO Implement me
			log.error("Not implemented");
		}

		protected final void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height, int bgColor) {
			// TODO Implement me
			log.error("Not implemented");
		}
	}

	static final class BitmapGraphics24bpp extends BitmapGraphics {

		/**
		 * @param mem
		 * @param width
		 * @param height
		 * @param offset
		 * @param bytesPerLine
		 */
		public BitmapGraphics24bpp(MemoryResource mem, int width, int height, int offset, int bytesPerLine) {
			super(mem, width, height, offset, bytesPerLine);
		}

		protected void doDrawPixels(int x, int y, int count, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + (x * 3);
			if (mode == Surface.PAINT_MODE) {
				mem.setInt24(ofs, color, count);
			} else {
				mem.xorInt24(ofs, color, count);
			}
		}

		protected void doDrawLine(int x, int y, int w, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + (x * 3);
			if (mode == Surface.PAINT_MODE) {
				mem.setInt24(ofs, color, w);
			} else {
				mem.xorInt24(ofs, color, w);
			}
		}

		protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height) {
			// TODO Implement me
			log.error("Not implemented");
		}

		protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height, int bgColor) {
			// TODO Implement me
			log.error("Not implemented");
		}
	}

	static final class BitmapGraphics32bpp extends BitmapGraphics {

		/**
		 * @param mem
		 * @param width
		 * @param height
		 * @param offset
		 * @param bytesPerLine
		 */
		public BitmapGraphics32bpp(MemoryResource mem, int width, int height, int offset, int bytesPerLine) {
			super(mem, width, height, offset, bytesPerLine);
		}

		protected final void doDrawPixels(int x, int y, int count, int color, int mode) {
			final int ofs = offset + (y * bytesPerLine) + (x << 2);
			//System.out.println("ofs=" + ofs);
			if (mode == Surface.PAINT_MODE) {
				mem.setInt(ofs, color, count);
			} else {
				mem.xorInt(ofs, color, count);
			}
		}

		protected void doDrawLine(int x, int y, int w, int color, int mode) {
			//System.out.println("doDrawLine" + x + "," + y + "," + w + "," + color + "," + mode);
			final int ofs = offset + (y * bytesPerLine) + (x << 2);
			if (mode == Surface.PAINT_MODE) {
				mem.setInt(ofs, color, w);
			} else {
				mem.xorInt(ofs, color, w);
			}
		}

		protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height) {
			final int[] buf = new int[width];
			for (int row = 0; row < height; row++) {
				final int ofs = offset + ((dstY + row) * bytesPerLine) + (dstX << 2);
				src.getDataElements(srcX, srcY + row, width, 1, buf);
				mem.setInts(buf, 0, ofs, width);
			}
		}

		protected void doDrawImage(Raster src, int srcX, int srcY, int dstX, int dstY, int width, int height, int bgColor) {
			final int[] buf = new int[width];
			for (int row = 0; row < height; row++) {
				final int ofs = offset + ((dstY + row) * bytesPerLine) + (dstX << 2);
				src.getDataElements(srcX, srcY + row, width, 1, buf);
				mem.setInts(buf, 0, ofs, width);
			}
		}
	}
}
