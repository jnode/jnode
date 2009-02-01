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
 
package org.jnode.awt.util;

import java.awt.image.IndexColorModel;

/**
 * @author epr
 */
public class AwtUtils {

    /**
     * Returns the index of the closest color of <code>ARGB</code>
     * in the indexed color model <code>colorModel</code>.
     *
     * @param colorModel an indexed color model.
     * @param ARGB       a color coded in the default color model.
     * @return if alpha chanel == 0, returns the index returned by <code>getTransparentPixel ()</code>
     *         on <code>colorModel</code>. If this index is -1, 0 is returned.
     *         The returned color index is the index of the color with the smallest distance between the
     *         given ARGB color and the colors of the color model.
     * @since PJA2.3
     */
    public static int getClosestColorIndex(IndexColorModel colorModel, int ARGB) {
        final int a = (ARGB >> 24) & 0xFF;
        if (a == 0) {
            final int transPixel = colorModel.getTransparentPixel();
            return transPixel != -1 ? transPixel : 0;
        }

        final int r = (ARGB >> 16) & 0xFF;
        final int g = (ARGB >> 8) & 0xFF;
        final int b = ARGB & 0xFF;
        final int colorsCount = colorModel.getMapSize();
        int colorIndex = 0;
        int minDistance = Integer.MAX_VALUE;
        for (int i = 0; i < colorsCount; i++) {
            final int aDif = a - colorModel.getAlpha(i);
            final int rDif = r - colorModel.getRed(i);
            final int gDif = g - colorModel.getGreen(i);
            final int bDif = b - colorModel.getBlue(i);
            final int distance = aDif * aDif + rDif * rDif + gDif * gDif + bDif * bDif;
            if (distance < minDistance) {
                minDistance = distance;
                colorIndex = i;
            }
        }

        return colorIndex;
    }

}
