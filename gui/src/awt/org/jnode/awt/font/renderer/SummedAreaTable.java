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
 
package org.jnode.awt.font.renderer;

import java.util.BitSet;
import org.jnode.vm.Vm;
import org.jnode.vm.annotation.Inline;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public abstract class SummedAreaTable {

    private final int height;

    private final int width;

    /**
     * Initialize this instance from a given 1 banded raster.
     *
     * @param src
     */
    protected SummedAreaTable(int width, int height) {
        this.width = width;
        this.height = height;
        Vm.getVm().getCounter(getClass().getName()).inc();
    }

    /**
     * Initialize this instance from a given 1 banded raster.
     *
     * @param src
     */
    public static SummedAreaTable create(BitSet master, int width, int height) {
        final int size = width * height;
        final SummedAreaTable tbl;
        if (size <= Character.MAX_VALUE) {
            tbl = new Char(width, height);
        } else {
            tbl = new Int(width, height);
        }
        tbl.createTable(master);
        return tbl;
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
     * Gets a value out of the table at a given offset.
     *
     * @param offset
     * @return
     */
    protected abstract int get(int offset);

    /**
     * Sets a value in the table at a given offset.
     *
     * @param offset
     */
    protected abstract void set(int offset, int value);

    /**
     * Gets the sum at the given position.
     *
     * @param x
     * @param y
     * @return
     */
    @Inline
    public final int getSum(int x, int y) {
        if ((x < 0) || (x >= width)) {
            throw new IllegalArgumentException("x " + x + " " + width);
        }
        if ((y < 0) || (y >= height)) {
            throw new IllegalArgumentException("y " + y + " " + height);
        }
        return get(y * width + x);
    }

    /**
     * Gets the intensity of the area described by the parameters.
     * The intensity is the sum of the given area, divided by the number of
     * elements in the area.
     *
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
        final int x2 = x + w - 1;
        final int y2 = y + h - 1;

        final int sum_xy = getSum(x, y);
        final int sum_x2y2 = getSum(x2, y2);
        final int sum_xy2 = getSum(x, y2);
        final int sum_x2y = getSum(x2, y);

        return ((float) (sum_x2y2 + sum_xy - (sum_xy2 + sum_x2y))) / ((float) (w * h));
    }

    /**
     * Gets the intensity of the area described by the parameters.
     * The intensity if the sum of the given area, divided by the number of
     * elements in the area.
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return The intensity as a value between 0 and 0xFF
     */
    public final int getIntensity8b(int x, int y, int w, int h) {
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
            return 0;
        }
        final int x2 = x + w - 1;
        final int y2 = y + h - 1;

        final int sum_xy = getSum(x, y);
        final int sum_x2y2 = getSum(x2, y2);
        final int sum_xy2 = getSum(x, y2);
        final int sum_x2y = getSum(x2, y);

        final int sum = (sum_x2y2 + sum_xy - (sum_xy2 + sum_x2y));
        return ((sum << 8) / (w * h)) & 0xFF;
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
                    sum = get((y - 1) * width + x);
                } else {
                    sum = 0;
                }
                // Add the sum of the values left of me
                for (int i = 0; i <= x; i++) {
                    if (master.get(yOfs + i)) {
                        sum += 1;
                    }
                }
                set(yOfs + x, sum);
            }
        }
    }

    /**
     * Implementation of the summed area table using a char[] table.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private static final class Char extends SummedAreaTable {

        private final char[] table;

        public Char(int width, int height) {
            super(width, height);
            this.table = new char[width * height];
        }

        /**
         * @see org.jnode.awt.font.renderer.SummedAreaTable#get(int)
         */
        @Override
        protected final int get(int offset) {
            return table[offset];
        }

        /**
         * @see org.jnode.awt.font.renderer.SummedAreaTable#set(int, int)
         */
        @Override
        protected final void set(int offset, int value) {
            table[offset] = (char) value;
        }
    }

    /**
     * Implementation of the summed area table using an int[] table.
     *
     * @author Ewout Prangsma (epr@users.sourceforge.net)
     */
    private static final class Int extends SummedAreaTable {

        private final int[] table;

        public Int(int width, int height) {
            super(width, height);
            this.table = new int[width * height];
        }

        /**
         * @see org.jnode.awt.font.renderer.SummedAreaTable#get(int)
         */
        @Override
        protected final int get(int offset) {
            return table[offset];
        }

        /**
         * @see org.jnode.awt.font.renderer.SummedAreaTable#set(int, int)
         */
        @Override
        protected final void set(int offset, int value) {
            table[offset] = value;
        }
    }
}
