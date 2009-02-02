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
 
package org.jnode.driver.video.ddc;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class EDID implements EDIDConstants {

    private final byte[] data;

    public EDID(byte[] data) {
        this.data = data;
    }

    public String getManufacturer() {
        final int x0 = data[VENDOR_SECTION + V_MANUFACTURER] & 0xff;
        final int x1 = data[VENDOR_SECTION + V_MANUFACTURER + 1] & 0xff;
        final char[] m = new char[3];
        m[0] = (char) ('@' + ((x0 & 0x7c) >> 2));
        m[1] = (char) ('@' + ((x0 & 0x03) << 3) + ((x1 & 0xE0) >> 5));
        m[2] = (char) ('@' + (x1 & 0x1F));
        return new String(m);
    }

    public int getProductID() {
        final int x0 = data[VENDOR_SECTION + V_PROD_ID] & 0xff;
        final int x1 = data[VENDOR_SECTION + V_PROD_ID + 1] & 0xff;
        return x0 | (x1 << 8);
    }

    public int getSerialNumber() {
        final int x0 = data[VENDOR_SECTION + V_SERIAL] & 0xff;
        final int x1 = data[VENDOR_SECTION + V_SERIAL + 1] & 0xff;
        final int x2 = data[VENDOR_SECTION + V_SERIAL + 2] & 0xff;
        final int x3 = data[VENDOR_SECTION + V_SERIAL + 3] & 0xff;
        return x0 | (x1 << 8) | (x2 << 16) | (x3 << 24);
    }

    public int getYear() {
        final int x0 = data[VENDOR_SECTION + V_YEAR] & 0xff;
        return x0 + 1990;
    }

    public int getWeek() {
        final int x0 = data[VENDOR_SECTION + V_WEEK] & 0xff;
        return x0;
    }

    public int getEDIDVersion() {
        final int x0 = data[VERSION_SECTION + V_VERSION] & 0xff;
        return x0;
    }

    public int getEDIDRevision() {
        final int x0 = data[VERSION_SECTION + V_REVISION] & 0xff;
        return x0;
    }

    public int getInputType() {
        final int x0 = data[DISPLAY_SECTION + D_INPUT] & 0xff;
        return (x0 & 0x80) >> 7;
    }

    public int getInputVoltage() {
        final int x0 = data[DISPLAY_SECTION + D_INPUT] & 0xff;
        return (x0 & 0x60) >> 5;
    }

    public int getInputSetup() {
        final int x0 = data[DISPLAY_SECTION + D_INPUT] & 0xff;
        return (x0 & 0x10) >> 4;
    }

    public int getInputSync() {
        final int x0 = data[DISPLAY_SECTION + D_INPUT] & 0xff;
        return (x0 & 0x0f);
    }

    public int getHSize() {
        final int x0 = data[DISPLAY_SECTION + D_HSIZE] & 0xff;
        return x0;
    }

    public int getVSize() {
        final int x0 = data[DISPLAY_SECTION + D_VSIZE] & 0xff;
        return x0;
    }

    public float getGamma() {
        final int x0 = data[DISPLAY_SECTION + D_GAMMA] & 0xff;
        return (x0 + 100.0f) / 100.0f;
    }

    public int getDPMS() {
        final int x0 = data[DISPLAY_SECTION + FEAT_S] & 0xff;
        return (x0 & 0xE0) >> 5;
    }

    public int getDisplayType() {
        final int x0 = data[DISPLAY_SECTION + FEAT_S] & 0xff;
        return (x0 & 0x18) >> 3;
    }

    public String toString() {
        return "Vendor(" + getManufacturer() + ", " + getProductID() + ", " + getSerialNumber() +
                ", " + "wk" + getWeek() + "-" + getYear() + ")\n" + "EDID-Version(" +
                getEDIDVersion() + "." + getEDIDRevision() + ")\n" + "Display(" + getInputType() +
                ", " + getInputVoltage() + ", " + getInputSetup() + ", " + getInputSync() + ", " +
                getHSize() + ", " + getVSize() + ", " + getGamma() + ", " + getDPMS() + ", " +
                getDisplayType() + ")";
    }

    public byte[] getRawData() {
        return data;
    }
}
