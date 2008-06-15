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

package org.jnode.driver.video.ati.radeon;

import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPITimingBlock {

    private final int dot_clock; // 9

    private final int h_display; // 19

    private final int h_sync_start; // 21

    private final int h_sync_width; // 23 (8-bit)

    private final int h_total; // 17

    private final int v_display; // 26

    private final int v_sync; // 28

    private final int v_total; // 24

    private final int xres;

    private final int yres;

    /**
     * Initialize this instance.
     * 
     * @param biosRom
     */
    FPITimingBlock(MemoryResource biosRom, int offset) {
        this.xres = biosRom.getChar(offset + 0);
        this.yres = biosRom.getChar(offset + 2);
        this.dot_clock = biosRom.getChar(offset + 9) * 10;
        this.h_total = biosRom.getShort(offset + 17) & 0xFFFF;
        this.h_display = biosRom.getShort(offset + 19) & 0xFFFF;
        this.h_sync_start = biosRom.getChar(offset + 21);
        this.h_sync_width = biosRom.getByte(offset + 23) & 0xFF;
        this.v_total = biosRom.getChar(offset + 24);
        this.v_display = biosRom.getChar(offset + 26);
        this.v_sync = biosRom.getChar(offset + 28);
    }

    /**
     * @return Returns the dot_clock.
     */
    final int getDotClock() {
        return dot_clock;
    }

    /**
     * @return Returns the h_display.
     */
    final int getHDisplay() {
        return h_display;
    }

    /**
     * @return Returns the h_sync_start.
     */
    final int getHSyncStart() {
        return h_sync_start;
    }

    /**
     * @return Returns the h_sync_width.
     */
    final int getHSyncWidth() {
        return h_sync_width;
    }

    /**
     * @return Returns the h_total.
     */
    final int getHTotal() {
        return h_total;
    }

    /**
     * @return Returns the v_display.
     */
    final int getVDisplay() {
        return v_display;
    }

    /**
     * @return Returns the v_sync.
     */
    final int getVSync() {
        return v_sync;
    }

    /**
     * @return Returns the v_total.
     */
    final int getVTotal() {
        return v_total;
    }

    /**
     * @return Returns the xres.
     */
    final int getXres() {
        return xres;
    }

    /**
     * @return Returns the yres.
     */
    final int getYres() {
        return yres;
    }

    /**
     * Convert this timing into a display mode.
     * 
     * @return
     */
    final DisplayMode toDisplayMode(DisplayMode src, int panel_xres, int panel_yres) {
        final int hblank = (h_total - h_display) * 8;
        final int hOver_plus = ((h_sync_start - h_display - 1) * 8) & 0x7FFF;
        final int vblank = v_total - v_display;
        final int vOver_plus = (v_sync & 0x7ff) - v_display;
        final int vSync_width = (v_sync & 0xf800) >> 11;

        final int xres = Math.min(panel_xres, src.getWidth());
        final int yres = Math.min(panel_yres, src.getHeight());

        final int hTotal = xres + hblank;
        final int hSyncStart = xres + hOver_plus;
        final int hSyncEnd = hSyncStart + h_sync_width;
        final int vTotal = yres + vblank;
        final int vSyncStart = yres + vOver_plus;
        final int vSyncEnd = vSyncStart + vSync_width;

        return new DisplayMode(src.getFreq(), xres, hSyncStart, hSyncEnd, hTotal, yres, vSyncStart,
                vSyncEnd, vTotal);
    }

    public String toString() {
        return "XRes:" + getXres() + ", YRes:" + getYres() + ",DotClock:" + getDotClock() +
                ", HTotal:" + getHTotal() + ", HDisplay:" + getHDisplay() + ", HSyncStart:" +
                getHSyncStart() + ", HSyncWith:" + getHSyncWidth() + ", VTotal:" + getVTotal() +
                ", VDisplay:" + getVDisplay() + ", VSync:" + getVSync();
    }
}
