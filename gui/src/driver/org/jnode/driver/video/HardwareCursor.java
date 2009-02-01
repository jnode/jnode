/*
 * $Id$
 *
 * JNode.org
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

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class HardwareCursor {

    /** The images */
    private final HashMap<String, HardwareCursorImage> imageMap;

    /**
     * Initialize this instance.
     * 
     * @param images
     */
    public HardwareCursor(HardwareCursorImage[] images) {
        this.imageMap = new HashMap<String, HardwareCursorImage>();
        for (int i = 0; i < images.length; i++) {
            final HardwareCursorImage img = images[i];
            imageMap.put(getKey(img.getWidth(), img.getHeight()), img);
        }
    }

    /**
     * Gets the best cursor image for the given size.
     * 
     * @param width
     * @param height
     * @return HardwareCursorImage
     */
    public HardwareCursorImage getImage(int width, int height) {
        final String key = getKey(width, height);
        HardwareCursorImage img = imageMap.get(key);
        if (img == null) {
            if (width > 20) {
                img = scaleImage(getImage(width / 2, height / 2));
                if (img != null) {
                    imageMap.put(key, img);
                }
            }
        }
        return img;
    }

    /**
     * Create an iterator over all images.
     * 
     * @return An iterator of {@link HardwareCursor}instances.
     */
    public Iterator iterator() {
        return imageMap.values().iterator();
    }

    /**
     * Create a key for use in imageMap.
     * 
     * @param width
     * @param height
     * @return
     */
    private String getKey(int width, int height) {
        return "" + width + "-" + height;
    }

    /**
     * Scale the given image by a factor of 2.
     * 
     * @param src
     * @return HardwareCursorImage
     */
    private HardwareCursorImage scaleImage(HardwareCursorImage src) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        final int nw = w * 2;
        final int nh = h * 2;

        final int[] argb = src.getImage();
        final int[] nargb = new int[nw * nh];

        for (int y = 0; y < h; y++) {
            final int ny = y * 2;
            for (int x = 0; x < w; x++) {
                final int nx = x * 2;
                final int v = argb[y * w + x];
                nargb[ny * nw + nx] = v;
                nargb[(ny + 1) * nw + nx] = v;
                nargb[ny * nw + nx + 1] = v;
                nargb[(ny + 1) * nw + nx + 1] = v;
            }
        }

        return new HardwareCursorImage(nw, nh, nargb, src.getHotSpotX() * 2, src.getHotSpotY() * 2);
    }
}
