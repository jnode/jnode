/*
 * $Id$
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
	public ImageConsumerByte(MemoryResource target, int targetW, int targetH, int destX, int destY, int destW, int destH, int bytesPerLine) {
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
