/*
 * $Id: header.txt 5714 2010-01-03 13:33:07Z lsantha $
 *
 * Copyright (C) 2003-2012 JNode.org
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
 
package org.jnode.apps.vmware.disk.handler.sparse;

import org.apache.log4j.Logger;

/**
 * Wrote from the 'Virtual Disk Format 1.0' specifications (from VMWare).
 * 
 * @author Fabien DUMINY (fduminy at jnode dot org)
 * 
 */
public class SparseExtentHeader {
    static final Logger LOG = Logger.getLogger(SparseExtentHeader.class);

    // flags
    private boolean validNewLineDetectionTest;
    private boolean redundantGrainTableWillBeUsed;

    private long capacity;
    private long grainSize;
    private long descriptorOffset;
    private long descriptorSize;
    private long rgdOffset;

    // additional/computed fields
    private long grainTableCoverage;

    private int numGTEsPerGT = 512;

    private long gdOffset;
    private long overHead;
    private boolean uncleanShutdown;

    /**
     * 
     * @return
     */
    public int getNumGTEsPerGT() {
        return numGTEsPerGT;
    }

    /**
     * 
     * @return
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     * 
     * @return gtCoverage (grain table coverage) in sectors
     */
    public long getGrainTableCoverage() {
        return grainTableCoverage;
    }

    /**
     * 
     * @return
     */
    public long getGrainSize() {
        return grainSize;
    }

    /**
     * 
     * @return
     */
    public long getDescriptorOffset() {
        return descriptorOffset;
    }

    /**
     * 
     * @return
     */
    public boolean isValidNewLineDetectionTest() {
        return validNewLineDetectionTest;
    }

    /**
     * 
     * @param validNewLineDetectionTest
     */
    public void setValidNewLineDetectionTest(boolean validNewLineDetectionTest) {
        this.validNewLineDetectionTest = validNewLineDetectionTest;
    }

    /**
     * 
     * @return
     */
    public boolean isRedundantGrainTableWillBeUsed() {
        return redundantGrainTableWillBeUsed;
    }

    /**
     * 
     * @param redundantGrainTableWillBeUsed
     */
    public void setRedundantGrainTableWillBeUsed(boolean redundantGrainTableWillBeUsed) {
        this.redundantGrainTableWillBeUsed = redundantGrainTableWillBeUsed;
    }

    /**
     * 
     * @return
     */
    public long getDescriptorSize() {
        return descriptorSize;
    }

    /**
     * 
     * @param descriptorSize
     */
    public void setDescriptorSize(long descriptorSize) {
        this.descriptorSize = descriptorSize;
    }

    /**
     * 
     * @return
     */
    public long getRgdOffset() {
        return rgdOffset;
    }

    /**
     * 
     * @param rgdOffset
     */
    public void setRgdOffset(long rgdOffset) {
        this.rgdOffset = rgdOffset;
    }

    /**
     * 
     * @return
     */
    public long getGdOffset() {
        return gdOffset;
    }

    /**
     * 
     * @param gdOffset
     */
    public void setGdOffset(long gdOffset) {
        this.gdOffset = gdOffset;
    }

    /**
     * 
     * @return
     */
    public long getOverHead() {
        return overHead;
    }

    /**
     * 
     * @param overHead
     */
    public void setOverHead(long overHead) {
        this.overHead = overHead;
    }

    /**
     * 
     * @return
     */
    public boolean isUncleanShutdown() {
        return uncleanShutdown;
    }

    /**
     * 
     * @param uncleanShutdown
     */
    public void setUncleanShutdown(boolean uncleanShutdown) {
        this.uncleanShutdown = uncleanShutdown;
    }

    /**
     * 
     * @param capacity
     */
    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    /**
     * 
     * @param grainSize
     */
    public void setGrainSize(long grainSize) {
        this.grainSize = grainSize;
    }

    /**
     * 
     * @param descriptorOffset
     */
    public void setDescriptorOffset(long descriptorOffset) {
        this.descriptorOffset = descriptorOffset;
    }

    /**
     * 
     * @param grainTableCoverage
     */
    public void setGrainTableCoverage(long grainTableCoverage) {
        this.grainTableCoverage = grainTableCoverage;
    }

    /**
     * 
     * @param numGTEsPerGT
     */
    public void setNumGTEsPerGT(int numGTEsPerGT) {
        this.numGTEsPerGT = numGTEsPerGT;
    }

    /**
     * 
     */
    @Override
    public String toString() {
        // flags
        return "SparseExtentHeader[validNewLineDetectionTest=" + validNewLineDetectionTest +
                ", redundantGrainTableWillBeUsed=" + redundantGrainTableWillBeUsed + ", capacity=" +
                capacity + ", grainSize=" + grainSize + ", descriptorOffset=" + descriptorOffset +
                ", descriptorSize=" + descriptorSize + ", rgdOffset=" + rgdOffset +
                ", grainTableCoverage=" + grainTableCoverage + ", numGTEsPerGT=" + numGTEsPerGT +
                ", gdOffset=" + gdOffset + ", overHead=" + overHead + ", uncleanShutdown=" +
                uncleanShutdown + "]";
    }
}
