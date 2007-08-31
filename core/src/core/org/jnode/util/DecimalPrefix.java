/*
 * $Id$
 */
package org.jnode.util;

import org.jnode.vm.annotation.SharedStatics;

@SharedStatics
public enum DecimalPrefix {
	B(1l, ""),
	K(1000l, "k"),
	M(1000l*1000l, "M"),
	G(1000l*1000l*1000l, "G"),
	T(1000l*1000l*1000l*1000l, "T"),
	P(1000l*1000l*1000l*1000l*1000l, "P"),
	E(1000l*1000l*1000l*1000l*1000l*1000l, "E");
	//these units have too big multipliers to fit in a long
	// (aka they are greater than 2^64) :
	//Z(1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Z"),
	//Y(1024l*1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Y");

	public static final DecimalPrefix MIN = B;
	public static final DecimalPrefix MAX = E;

	final private long multiplier;
	final private String unit;

	private DecimalPrefix(long multiplier, String unit)
	{
		this.multiplier = multiplier;
		this.unit = unit;
	}

	public long getMultiplier() {
		return multiplier;
	}

	public String getUnit() {
		return unit;
	}

	public String toString()
	{
		return multiplier + ", " + unit;
	}

    /**
	 * Convert the given value to a size string like 64K
	 * @param v the size to convert
	 * @return the text for of the size
	 */
    public static String apply(long v) {
        for (DecimalPrefix unit : values()) {
            if ((v < 1000l) && (v > 0l)) {
                return String.valueOf(v) + unit.getUnit();
            }

            v = v / 1000l;
        }
        return String.valueOf(v / 1000l) + MAX.getUnit();
    }
}
