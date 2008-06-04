/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

package org.jnode.driver.bus.smbus;

/**
 * DIMM device.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class DIMM extends SMBusDevice {

    public static final int SPDTABLE_SIZE = 128;

    byte[] rawSPDTable = null;

    public DIMM(SMBus bus, String name) {
        super(bus, name, 0);
    }

    private String type2String(int type) {
        switch (type) {
            case 2:
                return "EDO";
            case 4:
                return "SDRAM";
            case 7:
                return "SDRAM DDR";
            default:
                return "Unknown RAM type";
        }
    }

    /**
     * DIMMDriver fills the array when starting the device
     *
     * @param table
     */
    public void setSPDTable(byte[] table) {
        rawSPDTable = table;
    }

    public int getType() {
        return rawSPDTable[2];
    }

    public int getRevisionCode() {
        return rawSPDTable[62];
    }

    public byte[] getManufacturerCode() {
        byte[] info = new byte[8];
        for (int i = 0; i < 8; i++)
            info[i] = rawSPDTable[64 + i];
        return info;
    }

    public byte[] getManufacturerData() {
        byte[] info = new byte[27];
        for (int i = 0; i < 27; i++)
            info[i] = rawSPDTable[99 + i];
        return info;
    }

    public String toString() {
        if (rawSPDTable == null)
            return "SPDTable not filled";
        String tmp = "Device: " + super.getId() + " type=" + getType() + "(" + type2String(getType()) + ")";
        return tmp;
    }
}
