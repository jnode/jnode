/*
 * $Id$
 */
package org.jnode.driver.video;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HardwareCursorImage {
	
	private final int width;
	private final int height;
	private final int[] image;
	private final int hotSpotX;
	private final int hotSpotY;
	
	/**
	 * Initialize this instance.
	 * @param width
	 * @param height
	 * @param image
	 * @param hotSpotX
	 * @param hotSpotY
	 */
	public HardwareCursorImage(int width, int height, int[] image, int hotSpotX, int hotSpotY) {
		if (image.length != width * height) {
			throw new IllegalArgumentException("Invalid image length");
		}
		this.width = width;
		this.height = height;
		this.image = image;
		this.hotSpotX = hotSpotX;
		this.hotSpotY = hotSpotY;
	}

	/**
	 * Gets the argb image.
	 * @return int[]
	 */
	public int[] getImage() {
		return image;
	}
	
	/**
	 * Gets the hotspot X coordinate
	 * @return int
	 */
	public int getHotSpotX() {
		return hotSpotX;
	}
	
	/**
	 * Gets the hotspot Y coordinate
	 * @return int
	 */
	public int getHotSpotY() {
		return hotSpotY;
	}
	
	/**
	 * Gets the height of the image
	 * @return Returns the height.
	 */
	public final int getHeight() {
		return this.height;
	}

	/**
	 * Gets the width of the image
	 * @return Returns the width.
	 */
	public final int getWidth() {
		return this.width;
	}

}
