/*
 * $Id$
 */
package org.jnode.awt.font.renderer;

import java.util.BitSet;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class SummedAreaTable {

	private final int height;

	private final int[] table;

	private final int width;

	/**
	 * Initialize this instance from a given 1 banded raster.
	 * @param src
	 */
	public SummedAreaTable(BitSet master, int width, int height) {
		this.width = width;
		this.height = height;
		this.table = new int[width * height];

		createTable(master);
	}

	/**
	 * @return Returns the height.
	 */
	public final int getHeight() {
		return height;
	}

	/**
	 * @return Returns the width.
	 */
	public final int getWidth() {
		return width;
	}

	/**
	 * Gets the sum at the given position.
	 * @param x
	 * @param y
	 * @return
	 */
	public final int getSum(int x, int y) {
		if ((x < 0) || (x >= width)) {
			throw new IllegalArgumentException("x");
		}
		if ((y < 0) || (y >= height)) {
			throw new IllegalArgumentException("y");
		}
		return table[y * width + x];
	}
	
	/**
	 * Gets the intensity of the area described by the parameters.
	 * The intensity if the sum of the given area, divided by the number of 
	 * elements in the area.
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public final float getIntensity(int x, int y, int w, int h) {
		if (x < 0) {
			w += x;
			x = 0;
		}
		if (y < 0) {
			h += y;
			y = 0;
		}
		if (x + w > width) {
			w = Math.max(0, width - x);
		}
		if (y + h > height) {
			h = Math.max(0, height - y);
		}
		if ((w <= 0) || (h <= 0)) {
			return 0.0f;
		}
		final int x2 = x + w;
		final int y2 = y + h;
		
		final int sum_xy = getSum(x, y);
		final int sum_x2y2 = getSum(x2, y2);
		final int sum_xy2 = getSum(x, y2);
		final int sum_x2y = getSum(x2, y);
		
		return ((float)(sum_x2y2 + sum_xy - (sum_xy2 + sum_x2y))) / ((float)(w * h));		
	}

	/**
	 * Create the summed area table from the given source.
	 * 
	 * @param src
	 */
	private final void createTable(BitSet master) {
		for (int y = 0; y < height; y++) {
			final int yOfs = y * width;
			for (int x = 0; x < width; x++) {
				int sum;
				// Start with the sum directly above me
				if (y > 0) {
					sum = table[(y - 1) * width + x];
				} else {
					sum = 0;
				}
				// Add the sum of the values left of me
				for (int i = 0; i <= x; i++) {
				    if (master.get(y * width + i)) {
				        sum += 1;
				    }
				}
				table[yOfs + x] = sum;
			}
		}
	}
}
