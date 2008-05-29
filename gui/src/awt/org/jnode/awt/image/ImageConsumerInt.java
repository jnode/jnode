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

package org.jnode.awt.image;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;

/**
 * Image consumer that copies pixels of 32-bit to the destination.
 *
 * @author epr
 */
public class ImageConsumerInt extends AbstractMemoryImageConsumer {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(ImageConsumerInt.class);

    /**
     * @param target
     * @param targetDimension
     * @param dest
     * @param bytesPerLine
     */
    public ImageConsumerInt(MemoryResource target, Dimension targetDimension, Rectangle dest, int bytesPerLine) {
        super(target, targetDimension, dest, bytesPerLine);
    }

    /**
     * @param target
     * @param targetW
     * @param targetH
     * @param destX
     * @param destY
     * @param destW
     * @param destH
     * @param bytesPerLine
     */
    public ImageConsumerInt(MemoryResource target, int targetW, int targetH, int destX, int destY, int destW, int destH,
                            int bytesPerLine) {
        super(target, targetW, targetH, destX, destY, destW, destH, bytesPerLine);
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @param model
     * @param pixels
     * @param offset
     * @param scansize
     * @see java.awt.image.ImageConsumer#setPixels(int, int, int, int, java.awt.image.ColorModel, byte[], int, int)
     */
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int offset, int scansize) {
        log.debug("setPixel byte[] " + x + "," + y + "," + w + "," + h);
        w = Math.max(0, Math.min(w, targetWidth - (destX + x)));
        h = Math.max(0, Math.min(h, targetHeight - (destY + y)));
        for (int row = 0; row < h; row++) {
            // Offset within pixels
            final int rowOfs = (row * scansize) + offset;
            // Offset in target
            final int dstPtr = ((y + destY + row) * bytesPerLine) + ((destX + x) << 2);

            for (int col = 0; col < w; col++) {
                final int bytePixel = pixels[rowOfs + col];
                final int intPixel;
                if (model != null) {
                    intPixel = model.getRGB(bytePixel);
                } else {
                    intPixel = bytePixel;
                }
                target.setInt(dstPtr + (col << 2), intPixel);
            }
        }
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @param model
     * @param pixels
     * @param offset
     * @param scansize
     * @see java.awt.image.ImageConsumer#setPixels(int, int, int, int, java.awt.image.ColorModel, int[], int, int)
     */
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int offset, int scansize) {
        log.debug("setPixel int[] " + x + "," + y + "," + w + "," + h);
        w = Math.max(0, Math.min(w, targetWidth - (destX + x)));
        h = Math.max(0, Math.min(h, targetHeight - (destY + y)));
        log.debug("w=" + w + ", h=" + h);
        for (int row = 0; row < h; row++) {
            // Offset within pixels
            final int rowOfs = (row * scansize) + offset;
            // Offset in target
            final int dstPtr = ((y + destY + row) * bytesPerLine) + ((destX + x) << 2);
            target.setInts(pixels, rowOfs, dstPtr, w);
        }
    }

}
