/*
 * $Id$
 */
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;

/**
 * Class used to identify the current processor.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86CpuID {

	public static final int FEAT_FPU = (1 << 0);
	public static final int FEAT_VME = (1 << 1);
	public static final int FEAT_DE = (1 << 2);
	public static final int FEAT_PSE = (1 << 3);
	public static final int FEAT_TSC = (1 << 4);
	public static final int FEAT_MSR = (1 << 5);
	public static final int FEAT_PAE = (1 << 6);
	public static final int FEAT_MCE = (1 << 7);
	public static final int FEAT_CX8 = (1 << 8);
	public static final int FEAT_APIC = (1 << 9);
	public static final int FEAT_SEP = (1 << 11);
	public static final int FEAT_MTRR = (1 << 12);
	public static final int FEAT_PGE = (1 << 13);
	public static final int FEAT_MCA = (1 << 14);
	public static final int FEAT_CMOV = (1 << 15);
	public static final int FEAT_PAT = (1 << 16);
	public static final int FEAT_PSE36 = (1 << 17);
	public static final int FEAT_PSN = (1 << 18);
	public static final int FEAT_CLFSH = (1 << 19);
	public static final int FEAT_DS = (1 << 21);
	public static final int FEAT_ACPI = (1 << 22);
	public static final int FEAT_MMX = (1 << 23);
	public static final int FEAT_FXSR = (1 << 24);
	public static final int FEAT_SSE = (1 << 25);
	public static final int FEAT_SSE2 = (1 << 26);
	public static final int FEAT_SS = (1 << 27);
	public static final int FEAT_HTT = (1 << 28);
	public static final int FEAT_TM = (1 << 29);
	public static final int FEAT_PBE = (1 << 31);
	
	/** The cpu id data */
	private final int[] data;
	/** Name of the processor */
	private String name;
	private final int steppingID;
	private final int model;
	private final int family;
	private final int features;
	
	/**
	 * Initialize this instance
	 */
	public X86CpuID() {
		final int length = Unsafe.getCPUID(null);
		this.data = new int[length];
		Unsafe.getCPUID(data);
		final int eax = data[4];
		this.steppingID = eax & 0xF;
		this.model = (eax >> 4) & 0xF;
		this.family = (eax >> 8) & 0xF;
		this.features = data[7];
	}
	
	/**
	 * Gets the processor name.
	 * @return The processor name
	 */
	public String getName() {
		if (name == null) {
			final StringBuffer buf = new StringBuffer();
			intToString(buf, data[1]);
			intToString(buf, data[3]);
			intToString(buf, data[2]);
			name = buf.toString();
		}
		return name;
	}
	
	private final void intToString(StringBuffer buf, int value) {
		buf.append((char)(value & 0xFF));
		buf.append((char)((value >> 8) & 0xFF));
		buf.append((char)((value >> 16) & 0xFF));
		buf.append((char)((value >>> 24) & 0xFF));
	}
	

	/**
	 * @return Returns the family.
	 */
	public final int getFamily() {
		return this.family;
	}

	/**
	 * @return Returns the model.
	 */
	public final int getModel() {
		return this.model;
	}

	/**
	 * @return Returns the steppingID.
	 */
	public final int getSteppingID() {
		return this.steppingID;
	}

	/**
	 * @return Returns the features.
	 */
	public final int getFeatures() {
		return this.features;
	}
	
	/**
	 * Has this CPU a given feature.
	 * @param feature
	 * @return boolean
	 */
	public final boolean hasFeature(int feature) {
		return ((this.features & feature) == feature);
	}

	/**
	 * Convert all features to a human readable string.
	 * @return The available features.
	 */
	private final String getFeatureString() {
		final StringBuffer buf = new StringBuffer();
		getFeatureString(buf, FEAT_FPU, "FPU");
		getFeatureString(buf, FEAT_VME, "VME");
		getFeatureString(buf, FEAT_DE, "DE");
		getFeatureString(buf, FEAT_PSE, "PSE");
		getFeatureString(buf, FEAT_TSC, "TSC");
		getFeatureString(buf, FEAT_MSR, "MSR");
		getFeatureString(buf, FEAT_PAE, "PAE");
		getFeatureString(buf, FEAT_MCE, "MCE");
		getFeatureString(buf, FEAT_CX8, "CX8");
		getFeatureString(buf, FEAT_APIC, "APIC");
		getFeatureString(buf, FEAT_SEP, "SEP");
		getFeatureString(buf, FEAT_MTRR, "MTRR");
		getFeatureString(buf, FEAT_PGE, "PGE");
		getFeatureString(buf, FEAT_MCA, "MCA");
		getFeatureString(buf, FEAT_CMOV, "CMOV");
		getFeatureString(buf, FEAT_PAT, "PAT");
		getFeatureString(buf, FEAT_PSE36, "PSE36");
		getFeatureString(buf, FEAT_PSN, "PSN");
		getFeatureString(buf, FEAT_CLFSH, "CLFSH");
		getFeatureString(buf, FEAT_DS, "DS");
		getFeatureString(buf, FEAT_ACPI, "ACPI");
		getFeatureString(buf, FEAT_MMX, "MMX");
		getFeatureString(buf, FEAT_FXSR, "FXSR");
		getFeatureString(buf, FEAT_SSE, "SSE");
		getFeatureString(buf, FEAT_SSE2, "SSE2");
		getFeatureString(buf, FEAT_SS, "SS");
		getFeatureString(buf, FEAT_HTT, "HTT");
		getFeatureString(buf, FEAT_TM, "TM");
		getFeatureString(buf, FEAT_PBE, "PBE");
		return buf.toString();
	}

	private final void getFeatureString(StringBuffer buf, int feature, String featName) {
		if (hasFeature(feature)) {
			if (buf.length() > 0) {
				buf.append(',');
			}
			buf.append(featName);
		}
	}

	/**
	 * Convert to a string representation.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CPU:" +
		" name:" + getName() +
		" family:" + getFamily() +
		" model:" + getModel() +
		" step:" + getSteppingID() +
		" features:" + getFeatureString() +
		" raw:" + NumberUtils.hex(data, 8);
	}	
}
