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
 
package org.jnode.awt;

import org.jnode.driver.video.FrameBufferConfiguration;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;

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
	 * @param w
	 * @param h
	 * @param transparency
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int, int)
	 * @return The image
	 */
	public BufferedImage createCompatibleImage(int w, int h, int transparency) {
		return config.createCompatibleImage(w, h, transparency);
	}

	/**
	 * @param w
	 * @param h
	 * @see java.awt.GraphicsConfiguration#createCompatibleImage(int, int)
	 * @return The image
	 */
	public BufferedImage createCompatibleImage(int w, int h) {
        return createCompatibleImage(w, h, config.getColorModel().getTransparency());
	}

	/**
	 * @param w
	 * @param h
	 * @see java.awt.GraphicsConfiguration#createCompatibleVolatileImage(int, int)
	 * @return The image
	 */
	public VolatileImage createCompatibleVolatileImage(int w, int h) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getBounds()
	 * @return The bounds
	 */
	public Rectangle getBounds() {
		return bounds;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getColorModel()
	 * @return The color model
	 */
	public ColorModel getColorModel() {
		return colorModel;
	}

	/**
	 * @param transparency
	 * @see java.awt.GraphicsConfiguration#getColorModel(int)
	 * @return The color model
	 */
	public ColorModel getColorModel(int transparency) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getDefaultTransform()
	 * @return The default transform
	 */
	public AffineTransform getDefaultTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getDevice()
	 * @return The device
	 */
	public GraphicsDevice getDevice() {
		return device;
	}

	/**
	 * @see java.awt.GraphicsConfiguration#getNormalizingTransform()
	 * @return The transform
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
    public VolatileImage createCompatibleVolatileImage(int width, int height, int transparency)
    {
        // TODO Auto-generated method stub
        return null;
    }
}
