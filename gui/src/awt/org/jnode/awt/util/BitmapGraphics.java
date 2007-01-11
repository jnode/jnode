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
 
package org.jnode.awt.util;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.*;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jnode.driver.video.Surface;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.MultiMediaMemoryResource;
import org.jnode.system.ResourceManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public abstract class BitmapGraphics {

    static final class BitmapGraphics16bpp extends BitmapGraphics {

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
        }

        /**
         * @see org.jnode.awt.util.BitmapGraphics#doCopyArea(int, int, int, int,
         *      int, int)
         */
        protected void doCopyArea(int x, int y, int width, int height, int dx,
                int dy) {
            log.error("Not implemented");
        }

        protected final void doDrawImage(Raster src, int srcX, int srcY,
                int dstX, int dstY, int width, int height) {
            // TODO Implement me
            log.error("Not implemented");
        }

        /**
         * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
         *      int, int, int, int, int, int, int)
         */
        protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
                int dstX, int dstY, int width, int height, int color) {
            // TODO Implement me
            log.error("Not implemented");
        }

        protected final void doDrawImage(Raster src, int srcX, int srcY,
                int dstX, int dstY, int width, int height, int bgColor) {
            // TODO Implement me
            log.error("Not implemented");
        }

        public int doGetPixel(int x, int y) {
            // TODO Implement me
            log.error("Not implemented");
            return 0;
        }

        public int[] doGetPixels(Rectangle r) {
            // TODO Implement me
            log.error("Not implemented");
            return new int[0];
        }

        protected final void doDrawLine(int x, int y, int w, int color, int mode) {
            final int ofs = offset + (y * bytesPerLine) + (x << 1);
            if (mode == Surface.PAINT_MODE) {
                mem.setShort(ofs, (short) color, w);
            } else {
                mem.xorShort(ofs, (short) color, w);
            }
        }

        protected final void doDrawPixels(int x, int y, int count, int color,
                int mode) {
            final int ofs = offset + (y * bytesPerLine) + (x << 1);
            if (mode == Surface.PAINT_MODE) {
                mem.setShort(ofs, (short) color, count);
            } else {
                mem.xorShort(ofs, (short) color, count);
            }
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
        public BitmapGraphics24bpp(MemoryResource mem, int width, int height,
                int offset, int bytesPerLine) {
            super(mem, width, height, offset, bytesPerLine);
        }

        /**
         * @see org.jnode.awt.util.BitmapGraphics#doCopyArea(int, int, int, int,
         *      int, int)
         */
        protected void doCopyArea(int x, int y, int width, int height, int dx,
                int dy) {
            log.error("Not implemented");
        }

        protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                int dstY, int width, int height) {
            // TODO Implement me
            log.error("Not implemented");
        }

        protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                int dstY, int width, int height, int bgColor) {
            // TODO Implement me
            log.error("Not implemented");
        }

        /**
         * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
         *      int, int, int, int, int, int, int)
         */
        protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
                int dstX, int dstY, int width, int height, int color) {
            // TODO Implement me
            log.error("Not implemented");
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
            for(int i = 0; i < w; i++)
                for(int j = 0; j < h; j++){
                    final int ofs = offset + ((j + y) * bytesPerLine) + ((i + x) * 3);
                    ret[j*w + i] = mem.getInt(ofs);
                }
            return ret;
        }

        protected void doDrawLine(int x, int y, int w, int color, int mode) {
            final int ofs = offset + (y * bytesPerLine) + (x * 3);
            if (mode == Surface.PAINT_MODE) {
                mem.setInt24(ofs, color, w);
            } else {
                mem.xorInt24(ofs, color, w);
            }
        }

        protected void doDrawPixels(int x, int y, int count, int color, int mode) {
            final int ofs = offset + (y * bytesPerLine) + (x * 3);
            if (mode == Surface.PAINT_MODE) {
                mem.setInt24(ofs, color, count);
            } else {
                mem.xorInt24(ofs, color, count);
            }
        }
    }

    static final class BitmapGraphics32bpp extends BitmapGraphics {

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
        }


        /**
         * @see org.jnode.awt.util.BitmapGraphics#doCopyArea(int, int, int, int,
         *      int, int)
         */
        protected void doCopyArea(int srcX, int srcY, int width, int height,
                int dstX, int dstY) {
            for (int row = 0; row < height; row++) {
                final int srcOfs = offset + ((srcY + row) * bytesPerLine)
                        + (srcX << 2);
                final int dstOfs = offset + ((dstY + row) * bytesPerLine)
                        + (dstX << 2);
                mem.copy(srcOfs, dstOfs, width << 2);
            }
        }

        protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                int dstY, int width, int height) {
            final int[] buf = getPixelBuffer(width);
            for (int row = 0; row < height; row++) {
                final int ofs = offset + ((dstY + row) * bytesPerLine) + (dstX << 2);
                src.getDataElements(srcX, srcY + row, width, 1, buf);
                if(transparency == Transparency.TRANSLUCENT)
                    mem.setARGB32bpp(buf, 0, ofs, width);
                else
                    mem.setInts(buf, 0, ofs, width);
            }
        }

        protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                int dstY, int width, int height, int bgColor) {
            final int[] buf = new int[width];
            for (int row = 0; row < height; row++) {
                final int ofs = offset + ((dstY + row) * bytesPerLine)
                        + (dstX << 2);
                src.getDataElements(srcX, srcY + row, width, 1, buf);
                mem.setInts(buf, 0, ofs, width);
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

            color &= 0x00FFFFFF;
            final int ofsX = dstX << 2;

            for (int row = height - 1; row >= 0; row--) {
                final int ofs = offset + ((dstY + row) * bytesPerLine) + ofsX;
                raster.getDataElements(srcX, srcY + row, width, 1, buf);
                for (int i = width - 1; i >= 0; i--) {
                    final int alpha = (buf[i] & 0xFF);
                    pixels[i] = (alpha << 24) | color;
                }
                mem.setARGB32bpp(pixels, 0, ofs, width);
            }
        }

        protected void doDrawLine(int x, int y, int w, int color, int mode) {
            // System.out.println("doDrawLine" + x + "," + y + "," + w + "," +
            // color + "," + mode);
            final int ofs = offset + (y * bytesPerLine) + (x << 2);
            if (mode == Surface.PAINT_MODE) {
                mem.setInt(ofs, color, w);
            } else {
                mem.xorInt(ofs, color, w);
            }
        }

        protected final void doDrawPixels(int x, int y, int count, int color,
                int mode) {
            final int ofs = offset + (y * bytesPerLine) + (x << 2);
            // System.out.println("ofs=" + ofs);
            if (mode == Surface.PAINT_MODE) {
                mem.setInt(ofs, color, count);
            } else {
                mem.xorInt(ofs, color, count);
            }
        }

        public final int doGetPixel(int x, int y) {
            return mem.getInt(offset + (y * bytesPerLine) + (x << 2));
        }

        public final int[] doGetPixels(Rectangle r) {
            int x = r.x;
            int y = r.y;
            int w = r.width;
            int h = r.height;
            int[] ret = new int[w*h];
            for(int j = 0; j < h; j++) {
                mem.getInts(offset + ((j + y) * bytesPerLine) + ( x << 2), ret, j*w, w);
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
    }

    static final class BitmapGraphics8bpp extends BitmapGraphics {

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
        }

        /**
         * @see org.jnode.awt.util.BitmapGraphics#doCopyArea(int, int, int, int,
         *      int, int)
         */
        protected void doCopyArea(int srcX, int srcY, int width, int height, int dstX,
                int dstY) {
            for (int row = 0; row < height; row++) {
                final int srcOfs = offset + ((srcY + row) * bytesPerLine)
                        + (srcX * bytesPerLine);
                final int dstOfs = offset + ((dstY + row) * bytesPerLine)
                        + (dstX * bytesPerLine);
                mem.copy(srcOfs, dstOfs, width);
            }

        }

        protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                int dstY, int width, int height) {
            final byte[] buf = new byte[width];
            for (int row = 0; row < height; row++) {
                final int ofs = offset + ((dstY + row) * bytesPerLine) + (dstX * bytesPerLine);
                src.getDataElements(srcX, srcY + row, width, 1, buf);
                mem.setBytes(buf, 0, ofs, width);
            }
        }

        protected void doDrawImage(Raster src, int srcX, int srcY, int dstX,
                int dstY, int width, int height, int bgColor) {
            final byte[] buf = new byte[width];
            for (int row = 0; row < height; row++) {
                final int ofs = offset + ((dstY + row) * bytesPerLine) + (dstX * bytesPerLine);
                src.getDataElements(srcX, srcY + row, width, 1, buf);
                mem.setBytes(buf, 0, ofs, width);
            }
        }

        /**
         * @see org.jnode.awt.util.BitmapGraphics#doDrawAlphaRaster(java.awt.image.Raster,
         *      int, int, int, int, int, int, int)
         */
        protected void doDrawAlphaRaster(Raster raster, int srcX, int srcY,
                int dstX, int dstY, int width, int height, int color) {
            final byte[] buf = new byte[width];
            //TODO this method is still buggy, the "new" font render does not work with it.
            color &= 0x00FFFFFF;
            final int ofsX = dstX << 2;

            for (int row = height - 1; row >= 0; row--) {
                final int ofs = offset + ((dstY + row) * bytesPerLine) + ofsX;
                raster.getDataElements(srcX, srcY + row, width, 1, buf);
                for (int i = width - 1; i >= 0; i--) {
                    final int alpha = (buf[i] & 0xFF);
                    buf[i] = (byte) ((alpha ^ (color & 0xFF) ^ ((color >> 8) & 0xFF) ^ ((color >> 16) & 0xFF)));
                }
                mem.setBytes(buf, 0, ofs, width);
            }
       }

        public int doGetPixel(int x, int y) {
            // TODO Implement me
            log.error("Not implemented");
            return 0;
        }

        public int[] doGetPixels(Rectangle r) {
            // TODO Implement me
            log.error("Not implemented");
            return new int[0];
        }

        protected void doDrawLine(int x, int y, int w, int color, int mode) {
            final int ofs = offset + (y * bytesPerLine) + x;
            if (mode == Surface.PAINT_MODE) {
                mem.setByte(ofs, (byte) color, w);
            } else {
                mem.xorByte(ofs, (byte) color, w);
            }
        }

        protected void doDrawPixels(int x, int y, int count, int color, int mode) {
            final int ofs = offset + (y * bytesPerLine) + x;
            if (mode == Surface.PAINT_MODE) {
                mem.setByte(ofs, (byte) color, count);
            } else {
                mem.xorByte(ofs, (byte) color, count);
            }
        }
    }

    /**
     * Create a new instance for 16 bits/pixel layout
     * 
     * @param mem
     * @param width
     * @param height
     * @param bytesPerLine
     * @param offset
     * @return The created instance
     */
    public static BitmapGraphics create16bppInstance(MemoryResource mem,
            int width, int height, int bytesPerLine, int offset) {
        return new BitmapGraphics16bpp(mem, width, height, offset, bytesPerLine);
    }

    /**
     * Create a new instance for 24 bits/pixel layout
     * 
     * @param mem
     * @param width
     * @param height
     * @param bytesPerLine
     * @param offset
     * @return The created instance
     */
    public static BitmapGraphics create24bppInstance(MemoryResource mem,
            int width, int height, int bytesPerLine, int offset) {
        return new BitmapGraphics24bpp(mem, width, height, offset, bytesPerLine);
    }

    /**
     * Create a new instance for 32 bits/pixel layout
     * 
     * @param mem
     * @param width
     * @param height
     * @param bytesPerLine
     * @param offset
     * @return The created instance
     */
    public static BitmapGraphics create32bppInstance(MemoryResource mem,
            int width, int height, int bytesPerLine, int offset) {
        return new BitmapGraphics32bpp(mem, width, height, offset, bytesPerLine, Transparency.OPAQUE);
    }

    /**
     * Create a new instance for 32 bits/pixel layout
     *
     * @param mem
     * @param width
     * @param height
     * @param bytesPerLine
     * @param offset
     * @return The created instance
     */
    public static BitmapGraphics create32bppInstance(MemoryResource mem,
            int width, int height, int bytesPerLine, int offset, int transparency) {
        return new BitmapGraphics32bpp(mem, width, height, offset, bytesPerLine, transparency);
    }
    /**
     * Create a new instance for 8 bits/pixel layout
     * 
     * @param mem
     * @param width
     * @param height
     * @param bytesPerLine
     * @param offset
     * @return The created instance
     */
    public static BitmapGraphics create8bppInstance(MemoryResource mem,
            int width, int height, int bytesPerLine, int offset) {
        return new BitmapGraphics8bpp(mem, width, height, offset, bytesPerLine);
    }

    /**
     * Create a new instance for a given DataBuffer
     * 
     * @param dataBuffer
     * @param width
     * @param height
     * @param bytesPerLine
     * @return The created instance
     */
    public static BitmapGraphics createInstance(DataBuffer dataBuffer,
            int width, int height, int bytesPerLine, int transparency) {
        final ResourceManager rm;
        try {
            rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
        } catch (NamingException ex) {
            throw new RuntimeException("Cannot find ResourceManager", ex);
        }
        final int dbOffset = dataBuffer.getOffset();
        switch (dataBuffer.getDataType()) {
        case DataBuffer.TYPE_BYTE: {
            final byte[] data = ((DataBufferByte) dataBuffer).getData();
            return new BitmapGraphics8bpp(rm.asMemoryResource(data), width,
                    height, dbOffset, bytesPerLine);
        }
        case DataBuffer.TYPE_SHORT: {
            final short[] data = ((DataBufferShort) dataBuffer).getData();
            return new BitmapGraphics16bpp(rm.asMemoryResource(data), width,
                    height, dbOffset * 2, bytesPerLine);
        }
        case DataBuffer.TYPE_USHORT: {
            final short[] data = ((DataBufferUShort) dataBuffer).getData();
            return new BitmapGraphics16bpp(rm.asMemoryResource(data), width,
                    height, dbOffset * 2, bytesPerLine);
        }
        case DataBuffer.TYPE_INT: {
            final int[] data = ((DataBufferInt) dataBuffer).getData();
            return new BitmapGraphics32bpp(rm.asMemoryResource(data), width,
                    height, dbOffset * 4, bytesPerLine, transparency);
        }
        default: {
            throw new RuntimeException("Unimplemented databuffer type "
                    + dataBuffer.getDataType());
        }
        }
    }

    protected final int bytesPerLine;

    protected final int height;

    /** My logger */
    protected final Logger log = Logger.getLogger(getClass());

    protected final MultiMediaMemoryResource mem;

    /** Offset of first pixel in mem (in bytes) */
    protected final int offset;

    protected final int width;

    /**
     * Create a new instance
     * 
     * @param mem
     * @param width
     * @param height
     * @param offset
     * @param bytesPerLine
     */
    protected BitmapGraphics(MemoryResource mem, int width, int height,
            int offset, int bytesPerLine) {
        this.mem = mem.asMultiMediaMemoryResource();
        this.offset = offset;
        this.bytesPerLine = bytesPerLine;
        this.width = width;
        this.height = height;
    }

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
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    protected abstract void doCopyArea(int x, int y, int width, int height,
            int dx, int dy);

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

    /**
     * Return the pixel value at the specified location.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return the pixel value
     */
    public abstract int doGetPixel(int x, int y);

    /**
     * Return the pixels value from the specified region.
     *
     * @param r a rectangular region
     * @return the pixel values
     */
    public abstract int[] doGetPixels(Rectangle r);

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

    protected abstract void doDrawAlphaRaster(Raster raster, int srcX,
            int srcY, int dstX, int dstY, int width, int height, int color);

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
}
