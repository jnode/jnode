/*
 * $Id$
 */
package org.jnode.awt;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.awt.Transparency;
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
		return createCompatibleImage(w, h, Transparency.OPAQUE);
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

}
