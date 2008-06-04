/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.vm.annotation.SharedStatics;

@SharedStatics
public enum DecimalScaleFactor implements ScaleFactor {
    B(1l, ""),
    K(1000l, "k"),
    M(1000l * 1000l, "M"),
    G(1000l * 1000l * 1000l, "G"),
    T(1000l * 1000l * 1000l * 1000l, "T"),
    P(1000l * 1000l * 1000l * 1000l * 1000l, "P"),
    E(1000l * 1000l * 1000l * 1000l * 1000l * 1000l, "E");
    //these units have too big multipliers to fit in a long
    // (aka they are greater than 2^64) :
    //Z(1000l*1000l*1000l*1000l*1000l*1000l*1000l, "Z"),
    //Y(1000l*1000l*1000l*1000l*1000l*1000l*1000l*1000l, "Y");

    public static final DecimalScaleFactor MIN = B;
    public static final DecimalScaleFactor MAX = E;

    private final long multiplier;
    private final String unit;

    private DecimalScaleFactor(long multiplier, String unit) {
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
     * @param v          the size to convert
     * @param nbDecimals number of significant figures to display after dot. use Integer.MAX_VALUE for all.
     * @return the text for the size
     */
    public static String apply(final long value, final int nbDecimals) {
        long v = value;
        DecimalScaleFactor unit = null;
        for (DecimalScaleFactor u : values()) {
            if ((v < 1000l) && (v >= 0l)) {
                unit = u;
                break;
            }

            v = v / 1000l;
        }
        unit = (unit == null) ? MAX : unit;
        float dv = ((float) value) / unit.getMultiplier();
        return NumberUtils.toString(dv, nbDecimals) + " " + unit.getUnit();
    }
}
