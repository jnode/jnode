package org.jnode.util;

import java.math.BigInteger;
import org.jnode.vm.annotation.SharedStatics;

@SharedStatics
public enum SizeUnit {
	B(1l, "B"),
	K(1024l, "K"),
	M(1024l*1024l, "M"),
	G(1024l*1024l*1024l, "G"),
	T(1024l*1024l*1024l*1024l, "T"),
	P(1024l*1024l*1024l*1024l*1024l, "P"),
	E(1024l*1024l*1024l*1024l*1024l*1024l, "E");
	//these units have too big multipliers to fit in a long
	// (aka they are greater than 2^64) :
	//Z(1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Z"),
	//Y(1024l*1024l*1024l*1024l*1024l*1024l*1024l*1024l, "Y");
	
	public static final SizeUnit MIN = B;
	public static final SizeUnit MAX = E;
	
	final private long multiplier;
	final private String unit; 

	private SizeUnit(long multiplier, String unit)
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
}
