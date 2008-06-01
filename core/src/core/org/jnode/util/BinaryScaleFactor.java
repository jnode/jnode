/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.vm.annotation.SharedStatics;

@SharedStatics
public enum BinaryScaleFactor implements ScaleFactor {
    B(1l, ""),
    K(1024l, "K"),
    M(1024l * 1024l, "M"),
    G(1024l * 1024l * 1024l, "G"),
    T(1024l * 1024l * 1024l * 1024l, "T"),
    P(1024l * 1024l * 1024l * 1024l * 1024l, "P"),
    E(1024l * 1024l * 1024l * 1024l * 1024l * 1024l, "E");
    //these units have too big multipliers to fit in a long
    // (aka they are greater than 2^64) :
    //Z(1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Z"),
    //Y(1024l*1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Y");

    public static final BinaryScaleFactor MIN = B;
    public static final BinaryScaleFactor MAX = E;

    final private long multiplier;
    final private String unit;

    private BinaryScaleFactor(long multiplier, String unit) {
        this.multiplier = multiplier;
        this.unit = unit;
    }

    public long getMultiplier() {
        return multiplier;
    }

    public String getUnit() {
        return unit;
    }

    public String toString() {
        return multiplier + ", " + unit;
    }

    /**
     * Convert the given value to a size string like 64K
     *
     * @param value      the size to convert
     * @param nbDecimals number of significant figures to display after dot. use Integer.MAX_VALUE for all.
     * @return the text for the size
     */
    public static String apply(final long value, final int nbDecimals) {
        long v = value;
        BinaryScaleFactor unit = null;
        for (BinaryScaleFactor u : values()) {
            if ((v < 1024) && (v >= 0)) {
                unit = u;
                break;
            }

            v = v >>> 10;
        }
        unit = (unit == null) ? MAX : unit;
        float dv = ((float) value) / unit.getMultiplier();
        return NumberUtils.toString(dv, nbDecimals) + " " + unit.getUnit();
    }
}
