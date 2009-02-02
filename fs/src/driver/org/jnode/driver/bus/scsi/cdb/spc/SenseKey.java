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
 
package org.jnode.driver.bus.scsi.cdb.spc;


/**
 * Sense key wrapper.
 * See SCSI Primary Commands-3, section 4.5.6.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public final class SenseKey {

    private final int key;
    private final String name;
    private static final SenseKey[] KEYS = {
        new SenseKey(0, "NO SENSE"),
        new SenseKey(1, "RECOVERED ERROR"),
        new SenseKey(2, "NOT READY"),
        new SenseKey(3, "MEDIUM ERROR"),
        new SenseKey(4, "HARDWARE ERROR"),
        new SenseKey(5, "ILLEGAL REQUEST"),
        new SenseKey(6, "UNIT ATTENTION"),
        new SenseKey(7, "DATA PROTECT"),
        new SenseKey(8, "BLANK CHECK"),
        new SenseKey(9, "VENDOR SPECIFIC"),
        new SenseKey(10, "COPY ABORTED"),
        new SenseKey(11, "ABORTED COMMMAND"),
        new SenseKey(12, "Obsolete"),
        new SenseKey(13, "VOLUME OVERFLOW"),
        new SenseKey(14, "MISCOMPARE"),
        new SenseKey(15, "Reserved")
    };

    private SenseKey(int key, String name) {
        this.key = key;
        this.name = name;
    }

    /**
     * Gets the SenseKey wrapper for a given key number.
     *
     * @param key
     * @return
     */
    public static SenseKey valueOf(int key) {
        return KEYS[key];
    }

    public final boolean isNoSense() {
        return (key == 0);
    }

    public final boolean isRecoveredError() {
        return (key == 1);
    }

    public final boolean isNotReady() {
        return (key == 2);
    }

    public final boolean isMediumError() {
        return (key == 3);
    }

    public final boolean isHardwareError() {
        return (key == 4);
    }

    public final boolean isIllegalRequest() {
        return (key == 5);
    }

    public final boolean isUnitAttention() {
        return (key == 6);
    }

    public final boolean isDataProtect() {
        return (key == 7);
    }

    public final boolean isBlankCheck() {
        return (key == 8);
    }

    public final boolean isVendorSpecific() {
        return (key == 9);
    }

    public final boolean isCopyAborted() {
        return (key == 10);
    }

    public final boolean isAbortedCommand() {
        return (key == 11);
    }

    public final boolean isVolumeOverflow() {
        return (key == 13);
    }

    public final boolean isMiscompare() {
        return (key == 14);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return key;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public final String toString() {
        return name;
    }
}
