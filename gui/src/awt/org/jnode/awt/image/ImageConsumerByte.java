/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
import org.jnode.system.MemoryResource;

/**
 * Image consumer that copies pixels of 8-bit to the destination.
 *
 * @author epr
 */
public class ImageConsumerByte extends AbstractMemoryImageConsumer {

    /**
     * @param target
     * @param targetDimension
     * @param dest
     * @param bytesPerLine
     */
    public ImageConsumerByte(MemoryResource target, Dimension targetDimension, Rectangle dest, int bytesPerLine) {
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
    public ImageConsumerByte(MemoryResource target, int targetW, int targetH, int destX, int destY, int destW,
                             int destH, int bytesPerLine) {
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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

}
