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
 
package org.jnode.driver.video;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HardwareCursorImage {

    /** White */
    private static final int W = 0xFFFFFFFF;
    /** Black */
    private static final int B = 0xFF000000;
    /** Transparent */
    private static final int T = 0x00000000;

    private final int width;
    private final int height;
    private final int[] image;
    private final int hotSpotX;
    private final int hotSpotY;

    /**
     * Initialize this instance.
     * 
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
     * Initialize this instance by parsing the pixels from a given String.
     * 
     * @param width
     * @param height
     * @param image
     * @param hotSpotX
     * @param hotSpotY
     */
    public HardwareCursorImage(int width, int height, String image, int hotSpotX, int hotSpotY) {
        if (image.length() != width * height) {
            throw new IllegalArgumentException("Invalid image length");
        }
        this.width = width;
        this.height = height;
        this.image = parse(image);
        this.hotSpotX = hotSpotX;
        this.hotSpotY = hotSpotY;
    }

    private int[] parse(String image) {
        int[] res = new int[image.length()];
        int i = 0;
        for (char c : image.toCharArray()) {
            switch (c) {
                case 'T':
                    res[i++] = T;
                    break;
                case 'B':
                    res[i++] = B;
                    break;
                case 'W':
                    res[i++] = W;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown pixel color indicator '" + c + "'");
            }
        }
        return res;
    }

    /**
     * Gets the argb image.
     * 
     * @return int[]
     */
    public int[] getImage() {
        return image;
    }

    /**
     * Gets the hotspot X coordinate
     * 
     * @return int
     */
    public int getHotSpotX() {
        return hotSpotX;
    }

    /**
     * Gets the hotspot Y coordinate
     * 
     * @return int
     */
    public int getHotSpotY() {
        return hotSpotY;
    }

    /**
     * Gets the height of the image
     * 
     * @return Returns the height.
     */
    public final int getHeight() {
        return this.height;
    }

    /**
     * Gets the width of the image
     * 
     * @return Returns the width.
     */
    public final int getWidth() {
        return this.width;
    }

}
