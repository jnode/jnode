/*
 * $Id$
 */
package org.jnode.driver.video.vga;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import org.jnode.awt.image.JNodeBufferedImage;
import org.jnode.driver.video.FrameBufferConfiguration;

/**
 * @author epr
 */
public class VGAConfiguration extends FrameBufferConfiguration {

	private final IndexColorModel colorModel;

	/**
	 * @param width
	 * @param height
	 * @param colorModel
	 */
	public VGAConfiguration(int width, int height, IndexColorModel colorModel) {
		super(width, height, colorModel);
		this.colorModel = colorModel;
	}

	/**
	 * Returns a BufferedImage that supports the specified transparency 
	 * and has a data layout and color model compatible with this device. 
	 * This method has nothing to do with memory-mapping a device. 
	 * The returned BufferedImage has a layout and color model that 
	 * can be optimally blitted to this device. 
	 * @see java.awt.Transparency#BITMASK
	 * @see java.awt.Transparency#OPAQUE
	 * @see java.awt.Transparency#TRANSLUCENT
	 */
	public JNodeBufferedImage createCompatibleImage(int w, int h, int transparency) {
		return new JNodeBufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
	}
}
