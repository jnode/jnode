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

package org.jnode.driver.video;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/**
 * @author epr
 */
public abstract class FrameBufferConfiguration {

    private final int width;
    private final int height;
    private final ColorModel colorModel;

    /**
     * Initialize this instance.
     * 
     * @param width
     * @param height
     * @param colorModel
     */
    public FrameBufferConfiguration(int width, int height, ColorModel colorModel) {
        this.colorModel = colorModel;
        this.height = height;
        this.width = width;
    }

    /**
     * Gets the width of the screen in pixels
     */
    public int getScreenWidth() {
        return width;
    }

    /**
     * Gets the height of the screen in pixels
     */
    public int getScreenHeight() {
        return height;
    }

    /**
     * Gets the color model
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * Returns a BufferedImage that supports the specified transparency and has
     * a data layout and color model compatible with this device. This method
     * has nothing to do with memory-mapping a device. The returned
     * BufferedImage has a layout and color model that can be optimally blitted
     * to this device.
     * 
     * @see java.awt.Transparency#BITMASK
     * @see java.awt.Transparency#OPAQUE
     * @see java.awt.Transparency#TRANSLUCENT
     */
    public abstract BufferedImage createCompatibleImage(int w, int h, int transparency);
}
