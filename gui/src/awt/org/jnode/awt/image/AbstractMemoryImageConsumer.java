/*
 * $Id$
 */
package org.jnode.awt.image;

import java.awt.Dimension;
import java.awt.Rectangle;

import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class AbstractMemoryImageConsumer extends AbstractImageConsumer {

	protected final MemoryResource target;
	protected final int targetWidth;
	protected final int targetHeight;
	protected final int destX;
	protected final int destY;
	protected final int destWidth;
	protected final int destHeight;
	protected final int bytesPerLine;

	/**
	 * @param target
	 * @param targetDimension
	 * @param dest
	 * @param bytesPerLine
	 */
	public AbstractMemoryImageConsumer(MemoryResource target, Dimension targetDimension, Rectangle dest, int bytesPerLine) {
		this.target = target;
		this.targetWidth = targetDimension.width;
		this.targetHeight = targetDimension.height;
		this.destX = dest.x;
		this.destY = dest.y;
		this.destWidth = dest.width;
		this.destHeight = dest.height;
		this.bytesPerLine = bytesPerLine;
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
	public AbstractMemoryImageConsumer(MemoryResource target, int targetW, int targetH, int destX, int destY, int destW, int destH, int bytesPerLine) {
		this.target = target;
		this.targetWidth = targetW;
		this.targetHeight = targetH;
		this.destX = destX;
		this.destY = destY;
		this.destWidth = destW;
		this.destHeight = destH;
		this.bytesPerLine = bytesPerLine;
	}
	
}
