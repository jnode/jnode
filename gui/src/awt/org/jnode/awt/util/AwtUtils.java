/*
 * $Id$
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
	 * @param  colorModel an indexed color model.
	 * @param  ARGB       a color coded in the default color model.
	 * @return if alpha chanel == 0, returns the index returned by <code>getTransparentPixel ()</code>
	 *         on <code>colorModel</code>. If this index is -1, 0 is returned.
	 *         The returned color index is the index of the color with the smallest distance between the 
	 *         given ARGB color and the colors of the color model.
	 * @since  PJA2.3
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
