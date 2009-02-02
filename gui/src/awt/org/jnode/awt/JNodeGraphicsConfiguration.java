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
 
package org.jnode.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import org.jnode.driver.video.FrameBufferConfiguration;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class JNodeGraphicsConfiguration extends GraphicsConfiguration {

    private final JNodeFrameBufferDevice device;
    private final Rectangle bounds;
    private final ColorModel colorModel;
    private final FrameBufferConfiguration config;

    public JNodeGraphicsConfiguration(JNodeFrameBufferDevice device, FrameBufferConfiguration config) {
        this.device = device;
        this.bounds = new Rectangle(config.getScreenWidth(), config.getScreenHeight());
        this.colorModel = config.getColorModel();
        this.config = config;
    }

    /**
     * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int, int)
     */
    public BufferedImage createCompatibleImage(int w, int h, int transparency) {
        return config.createCompatibleImage(w, h, transparency);
    }

    /**
     * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
     */
    public BufferedImage createCompatibleImage(int w, int h) {
        return createCompatibleImage(w, h, config.getColorModel().getTransparency());
    }

    /**
     * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int, int)
     */
    public VolatileImage createCompatibleVolatileImage(int w, int h) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return The bounds
     * @see java.awt.GraphicsConfiguration#getBounds()
     */
    public Rectangle getBounds() {
        return (Rectangle) bounds.clone();
    }

    /**
     * @return The color model
     * @see java.awt.GraphicsConfiguration#getColorModel()
     */
    public ColorModel getColorModel() {
        return colorModel;
    }

    /**
     * @see java.awt.GraphicsConfiguration#getColorModel(int)
     */
    public ColorModel getColorModel(int transparency) {
        // TODO review this, normally transparency should be respected        
        return colorModel;
    }

    /**
     * @see java.awt.GraphicsConfiguration#getDefaultTransform()
     */
    public AffineTransform getDefaultTransform() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see java.awt.GraphicsConfiguration#getDevice()
     */
    public GraphicsDevice getDevice() {
        return device;
    }

    /**
     * @see java.awt.GraphicsConfiguration#getNormalizingTransform()
     */
    public AffineTransform getNormalizingTransform() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return The framebuffer configuration which is wrapped in this GraphicsConfiguration.
     */
    public FrameBufferConfiguration getConfig() {
        return this.config;
    }

    public String toString() {
        return bounds.width + "x" + bounds.height + "/" + colorModel.getPixelSize();
    }

    @Override
    public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency) {
        // TODO Auto-generated method stub
        return null;
    }
}
