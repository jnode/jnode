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

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Levente S\u00e1ntha
 */
public abstract class BitmapGraphics {

    abstract public int getWidth();

    abstract public int getHeight();

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    public abstract void copyArea(int srcX, int srcY, int w,
                                  int h, int dstX, int dstY);

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
     * @param srcX The upper left x coordinate of the source
     * @param srcY The upper left y coordinate of the source
     * @param dstX The upper left x coordinate of the destination
     * @param dstY The upper left y coordinate of the destination
     * @param w
     * @param h
     */
    public abstract void drawImage(Raster src, int srcX, int srcY,
                                   int dstX, int dstY, int w, int h);

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX    The upper left x coordinate of the source
     * @param srcY    The upper left y coordinate of the source
     * @param dstX    The upper left x coordinate of the destination
     * @param dstY    The upper left y coordinate of the destination
     * @param w
     * @param h
     * @param bgColor The color to use for transparent pixels
     */
    public abstract void drawImage(Raster src, int srcX, int srcY,
                                   int dstX, int dstY, int w, int h, int bgColor);

    /**
     * Draw an raster of alpha values using a given color onto to this surface.
     * The given raster is a 1 band gray type raster.
     *
     * @param raster
     * @param srcX   The upper left x coordinate within the raster
     * @param srcY   The upper left y coordinate within the raster
     * @param dstX   The upper left destination x coordinate
     * @param dstY   The upper left destination y coordinate
     * @param w
     * @param h
     * @param color  The color to use.
     */
    public abstract void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX,
                                         int srcY, int dstX, int dstY, int w, int h,
                                         int color);

    /**
     * Draw a line at location x,y that is w long using the given color.
     *
     * @param x
     * @param y
     * @param w
     * @param color
     * @param mode
     * @see org.jnode.driver.video.Surface#PAINT_MODE
     * @see org.jnode.driver.video.Surface#XOR_MODE
     */
    public abstract void drawLine(int x, int y, int w,
                                  int color, int mode);

    /**
     * Draw a pixel at location x,y using the given color.
     *
     * @param x
     * @param y
     * @param color
     * @param mode
     * @see org.jnode.driver.video.Surface#PAINT_MODE
     * @see org.jnode.driver.video.Surface#XOR_MODE
     */
    public abstract void drawPixels(int x, int y, int count,
                                    int color, int mode);


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
                                                     int width, int height, int bytesPerLine, int offset,
                                                     int transparency) {
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
                throw new RuntimeException("Unimplemented databuffer type " + dataBuffer.getDataType());
            }
        }
    }

}
