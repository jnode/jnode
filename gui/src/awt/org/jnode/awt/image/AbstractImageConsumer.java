/*
 * $Id$
 */
package org.jnode.awt.image;

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.util.Hashtable;

/**
 * @author epr
 */
public abstract class AbstractImageConsumer implements ImageConsumer {

	private ColorModel model;
	private int width;
	private int height;
	private Hashtable properties;

	public AbstractImageConsumer() {
	}

	/**
	 * @param status
	 * @see java.awt.image.ImageConsumer#imageComplete(int)
	 */
	public void imageComplete(int status) {
	}

	/**
	 * @param model
	 * @see java.awt.image.ImageConsumer#setColorModel(java.awt.image.ColorModel)
	 */
	public void setColorModel(ColorModel model) {
		this.model = model;
	}

	/**
	 * @param width
	 * @param height
	 * @see java.awt.image.ImageConsumer#setDimensions(int, int)
	 */
	public void setDimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * @param flags
	 * @see java.awt.image.ImageConsumer#setHints(int)
	 */
	public void setHints(int flags) {
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
	public abstract void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int offset, int scansize);

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
	public abstract void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int offset, int scansize);

	/**
	 * @param props
	 * @see java.awt.image.ImageConsumer#setProperties(java.util.Hashtable)
	 */
	public void setProperties(Hashtable props) {
		this.properties = props;
	}

	/**
	 * @return The height
	 */
	public final int getHeight() {
		return this.height;
	}

	/**
	 * @return The color model
	 */
	public final ColorModel getModel() {
		return this.model;
	}

	/**
	 * @return The properties
	 */
	public final Hashtable getProperties() {
		return this.properties;
	}

	/**
	 * @return The width
	 */
	public final int getWidth() {
		return this.width;
	}
}
