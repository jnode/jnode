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
 
package org.jnode.vm.x86;

import org.jnode.util.NumberUtils;
import org.jnode.vm.CpuID;

/**
 * Class used to identify the current processor.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class X86CpuID extends CpuID {

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
    // Extended features
    public static final long FEAT_EST = (1L << 39);
    public static final long FEAT_TM2 = (1L << 40);
    public static final long FEAT_CNXTID = (1L << 42);

    // Family codes
    public static final int FAM_486 = 0x04;
    public static final int FAM_PENTIUM = 0x05;
    public static final int FAM_PENTIUM_2_3 = 0x06;
    public static final int FAM_PENTIUM4 = 0x0F;

    /**
     * The cpu id data
     */
    private final int[] data;
    /**
     * Vendor of the processor
     */
    private String vendor;
    private final int steppingID;
    private final int model;
    private final int family;
    private final int features;
    private final long exFeatures;

    /**
     * Create a cpu id that contains the data of a processor identified by the given processor id.
     *
     * @param procId "i586", "pentium" for Pentium, "i686", "pentium2" for Pentium II, "pentium3" for
     *               Pentium III "pentium4" for Pentium 4 can be null
     * @return New cpu id.
     */
    public static X86CpuID createID(String procId) {
        // Handle default
        if (procId == null) {
            procId = "pentium";
        }
        final int[] id;
        if (procId.equals("pentium4")) {
            // Pentium 4
            id = new int[12];
            id[0] = 0x02;
            id[7] = FEAT_FPU | FEAT_PSE | FEAT_CMOV | FEAT_SSE | FEAT_SSE2;
        } else if (procId.equals("pentium3")) {
            // Pentium 3
            id = new int[16];
            id[0] = 0x03;
            id[7] = FEAT_FPU | FEAT_PSE | FEAT_CMOV | FEAT_SSE;
        } else if (procId.equals("pentium2")) {
            // Pentium 2
            id = new int[12];
            id[0] = 0x02;
            id[7] = FEAT_FPU | FEAT_PSE | FEAT_CMOV;

        } else {
            // Pentium
            id = new int[8];
            id[0] = 0x01;
            id[7] = FEAT_FPU | FEAT_PSE;
        }
        // Set name GenuineIntel
        id[1] = 0x756e6547;
        id[2] = 0x6c65746e;
        id[3] = 0x49656e69;
        return new X86CpuID(id);
    }

    /**
     * Initialize this instance
     */
    X86CpuID(int[] data) {
        this.data = data;
        final int eax = data[4];
        this.steppingID = eax & 0xF;
        this.model = (eax >> 4) & 0xF;
        this.family = (eax >> 8) & 0xF;
        this.features = data[7];
        this.exFeatures = features | (((long) data[6]) << 32);
    }

    public String getName() {
        return getVendor();
    }

    /**
     * Gets the processor name.
     *
     * @return The processor name
     */
    public String getVendor() {
        if (vendor == null) {
            final StringBuilder buf = new StringBuilder();
            intToString(buf, data[1]);
            intToString(buf, data[3]);
            intToString(buf, data[2]);
            vendor = buf.toString();
        }
        return vendor;
    }

    /**
     * Is this the id of an Intel CPU.
     *
     * @return
     */
    public boolean isIntel() {
        return getVendor().equals(X86Vendor.INTEL.getId());
    }

    /**
     * Is this the id of an Intel CPU.
     *
     * @return
     */
    public boolean isAMD() {
        return getVendor().equals(X86Vendor.AMD.getId());
    }

    private final void intToString(StringBuilder buf, int value) {
        buf.append((char) (value & 0xFF));
        buf.append((char) ((value >> 8) & 0xFF));
        buf.append((char) ((value >> 16) & 0xFF));
        buf.append((char) ((value >>> 24) & 0xFF));
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
     *
     * @param feature
     * @return boolean
     */
    public final boolean hasFeature(long feature) {
        return ((this.exFeatures & feature) == feature);
    }

    /**
     * Gets the number of logical processors.
     * This method will only return more then 1 of this processor
     * has the Hyper Threading feature.
     *
     * @return The number of logical processors.
     */
    public final int getLogicalProcessors() {
        if (hasFeature(FEAT_HTT)) {
            // EBX bits 16-23 when EAX == 1
            return (data[5] >> 16) & 0xFF;
        } else {
            return 1;
        }
    }

    /**
     * Calculate the physical package id for the given APIC id.
     *
     * @param apicId
     * @return
     */
    public final int getPhysicalPackageId(int apicId) {
        int index_lsb = 0;
        int index_msb = 31;
        final int numLogicalProcessors = getLogicalProcessors();

        int tmp = numLogicalProcessors;
        while ((tmp & 1) == 0) {
            tmp >>= 1;
            index_lsb++;
        }
        tmp = numLogicalProcessors;
        while ((tmp & 0x80000000) == 0) {
            tmp <<= 1;
            index_msb--;
        }
        if (index_lsb != index_msb) {
            index_msb++;
        }

        return ((data[5] >> 24) & 0xFF) >> index_msb;
    }

    /**
     * Has this CPU a given feature.
     *
     * @param feature
     * @return boolean
     */
    public final boolean hasFeature(int feature) {
        return ((this.features & feature) == feature);
    }

    public final boolean hasFPU() {
        return hasFeature(FEAT_FPU);
    }

    public final boolean hasVME() {
        return hasFeature(FEAT_VME);
    }

    public final boolean hasDE() {
        return hasFeature(FEAT_DE);
    }

    public final boolean hasPSE() {
        return hasFeature(FEAT_PSE);
    }

    public final boolean hasTSC() {
        return hasFeature(FEAT_TSC);
    }

    public final boolean hasMSR() {
        return hasFeature(FEAT_MSR);
    }

    public final boolean hasPAE() {
        return hasFeature(FEAT_PAE);
    }

    public final boolean hasMCE() {
        return hasFeature(FEAT_MCE);
    }

    public final boolean hasCX8() {
        return hasFeature(FEAT_CX8);
    }

    public final boolean hasAPIC() {
        return hasFeature(FEAT_APIC);
    }

    public final boolean hasSEP() {
        return hasFeature(FEAT_SEP);
    }

    public final boolean hasMTRR() {
        return hasFeature(FEAT_MTRR);
    }

    public final boolean hasPGE() {
        return hasFeature(FEAT_PGE);
    }

    public final boolean hasMCA() {
        return hasFeature(FEAT_MCA);
    }

    public final boolean hasCMOV() {
        return hasFeature(FEAT_CMOV);
    }

    public final boolean hasPAT() {
        return hasFeature(FEAT_PAT);
    }

    public final boolean hasPSE36() {
        return hasFeature(FEAT_PSE36);
    }

    public final boolean hasPSN() {
        return hasFeature(FEAT_PSN);
    }

    public final boolean hasCLFSH() {
        return hasFeature(FEAT_CLFSH);
    }

    public final boolean hasDS() {
        return hasFeature(FEAT_DS);
    }

    public final boolean hasACPI() {
        return hasFeature(FEAT_ACPI);
    }

    public final boolean hasMMX() {
        return hasFeature(FEAT_MMX);
    }

    public final boolean hasFXSR() {
        return hasFeature(FEAT_FXSR);
    }

    public final boolean hasSSE() {
        return hasFeature(FEAT_SSE);
    }

    public final boolean hasSSE2() {
        return hasFeature(FEAT_SSE2);
    }

    public final boolean hasSS() {
        return hasFeature(FEAT_SS);
    }

    public final boolean hasHTT() {
        return hasFeature(FEAT_HTT);
    }

    public final boolean hasTM() {
        return hasFeature(FEAT_TM);
    }

    public final boolean hasPBE() {
        return hasFeature(FEAT_PBE);
    }

    // Extended features
    public final boolean hasEST() {
        return hasFeature(FEAT_EST);
    }

    public final boolean hasTM2() {
        return hasFeature(FEAT_TM2);
    }

    public final boolean hasCNXTID() {
        return hasFeature(FEAT_CNXTID);
    }

    /**
     * Convert all features to a human readable string.
     *
     * @return The available features.
     */
    private final String getFeatureString() {
        final StringBuilder buf = new StringBuilder();
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
        // Extended features
        getFeatureString(buf, FEAT_EST, "EST");
        getFeatureString(buf, FEAT_TM2, "TM2");
        getFeatureString(buf, FEAT_CNXTID, "CNXTID");
        return buf.toString();
    }

    private final void getFeatureString(StringBuilder buf, int feature, String featName) {
        if (hasFeature(feature)) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(featName);
        }
    }

    private final void getFeatureString(StringBuilder buf, long feature, String featName) {
        if (hasFeature(feature)) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(featName);
        }
    }

    /**
     * Convert to a string representation.
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CPU:");
        sb.append(" name:");
        sb.append(getName());
        sb.append(" family:");
        sb.append(getFamily());
        sb.append(" model:");
        sb.append(getModel());
        sb.append(" step:");
        sb.append(getSteppingID());
        if (hasFeature(FEAT_HTT)) {
            sb.append(" #log.proc:");
            sb.append(getLogicalProcessors());
        }
        sb.append(" features:");
        sb.append(getFeatureString());
        sb.append(" raw:");
        sb.append(NumberUtils.hex(data, 8));
        return sb.toString();
    }
}
