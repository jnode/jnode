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

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import org.jnode.awt.util.AbstractSurfaceGraphics;

/**
 * @deprecated 
 * @author epr
 */
public class JNodeBufferedImageGraphics extends AbstractSurfaceGraphics {

    private final BufferedImage image;

    /**
     * @param src
     */
    public JNodeBufferedImageGraphics(JNodeBufferedImageGraphics src) {
        super(src);
        this.image = src.image;
    }

    /**
     * @param image
     */
    public JNodeBufferedImageGraphics(BufferedImage image) {
        super(new BufferedImageSurface(image), image.getWidth(), image.getHeight());
        this.image = image;
    }

    /**
     * @return The graphics
     * @see java.awt.Graphics#create()
     */
    public Graphics create() {
        return new JNodeBufferedImageGraphics(this);
    }

    /**
     * @return The configuration
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the color model of this Graphics object.
     *
     * @return the color model of this Graphics object
     */
    protected ColorModel getColorModel() {
        return image.getColorModel();
    }

    /**
     * Returns a WritableRaster that is used by this class to perform the
     * rendering in. It is not necessary that the target surface immediately
     * reflects changes in the raster. Updates to the raster are notified via
     * {@link #updateRaster}.
     *
     * @return the destination raster
     */
    protected WritableRaster getDestinationRaster() {
        return image.getRaster();
    }
}
