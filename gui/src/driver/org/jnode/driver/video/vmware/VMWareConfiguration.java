/*
 * $Id$
 */
package org.jnode.driver.video.vmware;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

import org.jnode.awt.image.JNodeBufferedImage;
import org.jnode.driver.video.FrameBufferConfiguration;

/**
 * @author epr
 */
public class VMWareConfiguration extends FrameBufferConfiguration {

	/**
	 * @param width
	 * @param height
	 * @param colorModel
	 */
	public VMWareConfiguration(int width, int height, ColorModel colorModel) {
		super(width, height, colorModel);
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
		return new JNodeBufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	}
}
