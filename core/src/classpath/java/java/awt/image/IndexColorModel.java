/* IndexColorModel.java -- Java class for interpreting Pixel objects
   Copyright (C) 1999 Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.awt.image;

/**
 *
 * @author C. Brian Jones (cbj@gnu.org) 
 */
public class IndexColorModel extends ColorModel {
	
	private int map_size;
	//private boolean opaque;
	private int trans = -1;
	private int[] rgb;
	private static final int LOOKUP_CACHE_SIZE = 32;
	private final int[] lookupCache = new int[LOOKUP_CACHE_SIZE*2];

	/**
	 * Each array much contain <code>size</code> elements.  For each 
	 * array, the i-th color is described by reds[i], greens[i], 
	 * blues[i], alphas[i], unless alphas is not specified, then all the 
	 * colors are opaque except for the transparent color. 
	 *
	 * @param bits the number of bits needed to represent <code>size</code> colors
	 * @param size the number of colors in the color map
	 * @param reds the red component of all colors
	 * @param greens the green component of all colors
	 * @param blues the blue component of all colors
	 */
	public IndexColorModel(int bits, int size, byte[] reds, byte[] greens, byte[] blues) {
		this(bits, size, reds, greens, blues, null);
	}

	/**
	 * Each array much contain <code>size</code> elements.  For each 
	 * array, the i-th color is described by reds[i], greens[i], 
	 * blues[i], alphas[i], unless alphas is not specified, then all the 
	 * colors are opaque except for the transparent color. 
	 *
	 * @param bits the number of bits needed to represent <code>size</code> colors
	 * @param size the number of colors in the color map
	 * @param reds the red component of all colors
	 * @param greens the green component of all colors
	 * @param blues the blue component of all colors
	 * @param trans the index of the transparent color
	 */
	public IndexColorModel(int bits, int size, byte[] reds, byte[] greens, byte[] blues, int trans) {
		this(bits, size, reds, greens, blues, null);
		this.trans = trans;
	}

	/**
	 * Each array much contain <code>size</code> elements.  For each 
	 * array, the i-th color is described by reds[i], greens[i], 
	 * blues[i], alphas[i], unless alphas is not specified, then all the 
	 * colors are opaque except for the transparent color. 
	 *
	 * @param bits the number of bits needed to represent <code>size</code> colors
	 * @param size the number of colors in the color map
	 * @param reds the red component of all colors
	 * @param greens the green component of all colors
	 * @param blues the blue component of all colors
	 * @param alphas the alpha component of all colors
	 */
	public IndexColorModel(int bits, int size, byte[] reds, byte[] greens, byte[] blues, byte[] alphas) {
		super(bits);
		map_size = size;

		rgb = new int[size];
		if (alphas == null) {
			for (int i = 0; i < size; i++) {
				rgb[i] = 0xff000000 | ((reds[i] & 0xff) << 16) | ((greens[i] & 0xff) << 8) | (blues[i] & 0xff);
			}
		} else {
			for (int i = 0; i < size; i++) {
				rgb[i] = ((alphas[i] & 0xff) << 24 | ((reds[i] & 0xff) << 16) | ((greens[i] & 0xff) << 8) | (blues[i] & 0xff));
			}
		}
	}

	/**
	 * Each array much contain <code>size</code> elements.  For each 
	 * array, the i-th color is described by reds[i], greens[i], 
	 * blues[i], alphas[i], unless alphas is not specified, then all the 
	 * colors are opaque except for the transparent color. 
	 *
	 * @param bits the number of bits needed to represent <code>size</code> colors
	 * @param size the number of colors in the color map
	 * @param cmap packed color components
	 * @param start the offset of the first color component in <code>cmap</code>
	 * @param hasAlpha <code>cmap</code> has alpha values
	 */
	public IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasAlpha) {
		this(bits, size, cmap, start, hasAlpha, -1);
	}

	/**
	 * Each array much contain <code>size</code> elements.  For each 
	 * array, the i-th color is described by reds[i], greens[i], 
	 * blues[i], alphas[i], unless alphas is not specified, then all the 
	 * colors are opaque except for the transparent color. 
	 *
	 * @param bits the number of bits needed to represent <code>size</code> colors
	 * @param size the number of colors in the color map
	 * @param cmap packed color components
	 * @param start the offset of the first color component in <code>cmap</code>
	 * @param hasAlpha <code>cmap</code> has alpha values
	 * @param trans the index of the transparent color
	 */
	public IndexColorModel(int bits, int size, byte[] cmap, int start, boolean hasAlpha, int trans) {
		super(bits);
		map_size = size;
		this.trans = trans;
	}

	public final int getMapSize() {
		return map_size;
	}

	/**
	 * Get the index of the transparent color in this color model
	 */
	public final int getTransparentPixel() {
		return trans;
	}

	/**
	 * <br>
	 */
	public final void getReds(byte[] r) {
		getComponents(r, 2);
	}

	/**
	 * <br>
	 */
	public final void getGreens(byte[] g) {
		getComponents(g, 1);
	}

	/**
	 * <br>
	 */
	public final void getBlues(byte[] b) {
		getComponents(b, 0);
	}

	/**
	 * <br>
	 */
	public final void getAlphas(byte[] a) {
		getComponents(a, 3);
	}

	private void getComponents(byte[] c, int ci) {
		int i, max = (map_size < c.length) ? map_size : c.length;
		for (i = 0; i < max; i++) {
			c[i] = (byte) (rgb[i] >> (ci * 8));
		}
	}

	/**
	 * Get the red component of the given pixel.
	 * <br>
	 */
	public final int getRed(int pixel) {
		if (pixel < map_size) {
			return ((rgb[pixel] >> 16) & 0xFF);
		} else {
			return 0;
		}
	}

	/**
	 * Get the green component of the given pixel.
	 * <br>
	 */
	public final int getGreen(int pixel) {
		if (pixel < map_size) {
			return ((rgb[pixel] >> 8) & 0xFF);
		} else {
			return 0;
		}
	}

	/**
	 * Get the blue component of the given pixel.
	 * <br>
	 */
	public final int getBlue(int pixel) {
		if (pixel < map_size) {
			return (rgb[pixel] & 0xFF);
		} else {
			return 0;
		}
	}

	/**
	 * Get the alpha component of the given pixel.
	 * <br>
	 */
	public final int getAlpha(int pixel) {
		if (pixel < map_size) {
			return ((rgb[pixel] >> 24) & 0xFF);
		} else {
			return 0;
		}
	}

	/**
	 * Get the RGB color value of the given pixel using the default
	 * RGB color model. 
	 * <br>
	 *
	 * @param pixel a pixel value
	 */
	public final int getRGB(int pixel) {
		if (pixel < map_size)
			return rgb[pixel];
		return 0;
	}

	/**
	 * @see java.awt.image.ColorModel#createCompatibleSampleModel(int, int)
	 */
	public SampleModel createCompatibleSampleModel(int w, int h) {
		final int[] off = new int[1];
		off[0] = 0;
		if (pixel_bits == 1 || pixel_bits == 2 || pixel_bits == 4) {
			return new MultiPixelPackedSampleModel(transferType, w, h, pixel_bits);
		} else {
			return new ComponentSampleModel(transferType, w, h, 1, w, off);
		}
	}

	/**
	 * @see java.awt.image.ColorModel#getDataElements(int, java.lang.Object)
	 */
	public Object getDataElements(int rgb, Object pixel) {
		int index = findInCache(rgb);
		if (index < 0) {
			index = getClosestColorIndex(rgb);
			putInCache(rgb, index);
		}
		return indexToArray(pixel, index);
	}
	
	/**
	 * Find the index of a given RGB value in the lookup cache.
	 * @param rgb
	 * @return -1 if not found, the index otherwise
	 */
	private final synchronized int findInCache(int rgb) {
		for (int i = 0; i < LOOKUP_CACHE_SIZE; i +=2) {
			if (lookupCache[i] == rgb) {
				return lookupCache[i+1];
			}
		}
		return -1;
	}
	
	/**
	 * Put a reverse lookup into the lookup cache
	 * @param rgb
	 * @param index
	 */
	private final synchronized void putInCache(int rgb, int index) {
		System.arraycopy(lookupCache, 0, lookupCache, 2, LOOKUP_CACHE_SIZE-2);
		lookupCache[0] = rgb;
		lookupCache[1] = index;
	}

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
	private final int getClosestColorIndex(int ARGB) {
		final int a = (ARGB >> 24) & 0xFF;
		if (a == 0) {
			final int transPixel = getTransparentPixel();
			return transPixel != -1 ? transPixel : 0;
		}

		final int r = (ARGB >> 16) & 0xFF;
		final int g = (ARGB >> 8) & 0xFF;
		final int b = ARGB & 0xFF;
		final int colorsCount = getMapSize();
		int colorIndex = 0;
		int minDistance = Integer.MAX_VALUE;
		for (int i = 0; i < colorsCount; i++) {
			final int aDif = a - getAlpha(i);
			final int rDif = r - getRed(i);
			final int gDif = g - getGreen(i);
			final int bDif = b - getBlue(i);
			final int distance = aDif * aDif + rDif * rDif + gDif * gDif + bDif * bDif;
			if (distance < minDistance) {
				minDistance = distance;
				colorIndex = i;
			}
		}

		return colorIndex;
	}

	/**
	 * Set an index into an byte, short or int array.
	 * @param array
	 * @param index
	 */
	private Object indexToArray(Object array, int index) {
		switch (transferType) {
			case DataBuffer.TYPE_INT :
				int[] intObj;
				if (array == null) {
					array = intObj = new int[1];
				} else {
					intObj = (int[]) array;
				}
				intObj[0] = index;
				break;
			case DataBuffer.TYPE_BYTE :
				byte[] byteObj;
				if (array == null) {
					array = byteObj = new byte[1];
				} else {
					byteObj = (byte[]) array;
				}
				byteObj[0] = (byte) index;
				break;
			case DataBuffer.TYPE_USHORT :
				short[] shortObj;
				if (array == null) {
					array = shortObj = new short[1];
				} else {
					shortObj = (short[]) array;
				}
				shortObj[0] = (short) index;
				break;
			default :
				throw new UnsupportedOperationException("This method has not been " + "implemented for transferType " + transferType);
		}
		return array;
	}

}
