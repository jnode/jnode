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
 
package org.jnode.driver.block.floppy;

/**
 * @author epr
 */
public class FloppyDriveParameters {

    private final int cmosType;
    private final int mtr;
    private final int hlt;
    private final int hut;
    private final int srt;
    private final String name;
    private final FloppyParameters[] autodetect;

    /**
     * Create a new instance
     *
     * @param cmosType
     * @param mtr
     * @param hlt
     * @param hut
     * @param srt
     * @param name
     * @param autodetect
     */
    public FloppyDriveParameters(
        int cmosType,
        int mtr,
        int hlt,
        int hut,
        int srt,
        String name,
        FloppyParameters[] autodetect) {
        this.cmosType = cmosType;
        this.mtr = mtr;
        this.hlt = hlt;
        this.hut = hut;
        this.srt = srt;
        this.autodetect = autodetect;
        this.name = name;
    }

    /**
     * Is this drive present?
     *
     * @return boolean
     */
    public boolean isPresent() {
        return (cmosType != 0);
    }

    /**
     * Gets a human readable type name for this drive
     *
     * @return name
     */
    public String getTypeName() {
        return name;
    }

    /**
     * Gets the head load time fom this drive in msec
     *
     * @return head load time
     */
    public int getHeadLoadTime() {
        return hlt;
    }

    /**
     * Gets the head unload time fom this drive in msec
     *
     * @return head unload time
     */
    public int getHeadUnloadTime() {
        return hut;
    }

    /**
     * Gets the step rate time fom this drive in usec
     *
     * @return step rate time
     */
    public int getStepRateTime() {
        return srt;
    }

    /**
     * Gets the maximum transfer rate for this drive in Kb/sec
     *
     * @return int
     */
    public int getMaxTransferRate() {
        return mtr;
    }

    /**
     * Gets the CMOS type of this drive
     *
     * @return int
     */
    public int getCmosType() {
        return cmosType;
    }

    /**
     * Gets the formats that can be autodetected on this drive
     *
     * @return parameters
     */
    public FloppyParameters[] getAutodetectFormats() {
        return autodetect;
    }

}
