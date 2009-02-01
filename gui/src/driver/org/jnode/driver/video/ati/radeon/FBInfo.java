/*
 * $Id$
 *
 * JNode.org
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
 
package org.jnode.driver.video.ati.radeon;

import org.apache.log4j.Logger;
import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;

/**
 * FrameBuffer info.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FBInfo implements RadeonConstants {

    private static final Logger log = Logger.getLogger(FBInfo.class);

    final boolean hasCRTC2;

    private int dviDispType = MonitorType.NONE;

    private int crtDispType = MonitorType.NONE;

    private FPIBlock fpi;

    private PLLInfo pllInfo;

    /**
     * Initialize this instance.
     * 
     * @param architecture
     */
    FBInfo(int architecture) {
        this.hasCRTC2 = (architecture != Architecture.R100);
        this.pllInfo = new PLLInfo(architecture);
    }

    /**
     * @return Returns the crtDispType.
     */
    final int getCrtDispType() {
        return crtDispType;
    }

    /**
     * @return Returns the dviDispType.
     */
    final int getDviDispType() {
        return dviDispType;
    }

    /**
     * @return Returns the fpi.
     */
    final FPIBlock getFpi() {
        return fpi;
    }

    final int getPanelXres() {
        if (fpi != null) {
            return fpi.getXres();
        } else {
            return 1024;
        }
    }

    final int getPanelYres() {
        if (fpi != null) {
            return fpi.getYres();
        } else {
            return 768;
        }
    }

    /**
     * Gets the primary monitor type.
     * 
     * @see MonitorType
     * @return
     */
    final int getPrimaryMonitorType() {
        switch (dviDispType) {
            case MonitorType.NONE:
            case MonitorType.STV:
            case MonitorType.CTV:
                return crtDispType;
            default:
                return dviDispType;
        }
    }

    /**
     * Read monitor information from the given rom.
     * 
     * @param rom
     */
    final void readMonitorInfo(RadeonVgaIO vgaIO) {
        dviDispType = MonitorType.NONE;
        crtDispType = MonitorType.NONE;

        if (hasCRTC2) {
            final int tmp = vgaIO.getReg32(RADEON_BIOS_4_SCRATCH);
            log.info("bios4-scratch: 0x" + NumberUtils.hex(tmp));

            /* primary DVI port */
            if ((tmp & 0x08) != 0) {
                dviDispType = MonitorType.DFP;
            } else if ((tmp & 0x4) != 0) {
                dviDispType = MonitorType.LCD;
            } else if ((tmp & 0x200) != 0) {
                dviDispType = MonitorType.CRT;
            } else if ((tmp & 0x10) != 0) {
                dviDispType = MonitorType.CTV;
            } else if ((tmp & 0x20) != 0) {
                dviDispType = MonitorType.STV;
            }

            /* secondary CRT port */
            if ((tmp & 0x2) != 0) {
                crtDispType = MonitorType.CRT;
            } else if ((tmp & 0x800) != 0) {
                crtDispType = MonitorType.DFP;
            } else if ((tmp & 0x400) != 0) {
                crtDispType = MonitorType.LCD;
            } else if ((tmp & 0x1000) != 0) {
                crtDispType = MonitorType.CTV;
            } else if ((tmp & 0x2000) != 0) {
                crtDispType = MonitorType.STV;
            }
        } else {
            final int tmp = vgaIO.getReg32(FP_GEN_CNTL);

            if ((tmp & FP_EN_TMDS) != 0) {
                crtDispType = MonitorType.DFP;
            } else {
                crtDispType = MonitorType.CRT;
            }
        }

        log.info("Found monitor type dvi:" + MonitorType.toString(dviDispType) + ", crt:" +
                MonitorType.toString(crtDispType));
    }

    final void readFPIInfo(MemoryResource rom) {
        final int biosHdr = rom.getShort(0x48) & 0xFFFF;
        final int fpiOffset = rom.getShort(biosHdr + 0x40) & 0xFFFF;

        log.debug("FpiOffset: " + fpiOffset);
        fpi = new FPIBlock(rom, fpiOffset);
        log.debug("FPI: " + fpi);

        pllInfo = new PLLInfo(rom);
    }

    /**
     * @return Returns the pllInfo.
     */
    final PLLInfo getPllInfo() {
        return pllInfo;
    }

    /**
     * Gets the best matching mode.
     */
    public RadeonConfiguration getBestConfiguration(RadeonConfiguration src) {
        if (fpi != null) {
            return new RadeonConfiguration(src.getBitsPerPixel(), fpi.getBestMode(src
                    .getDisplayMode()));
        } else {
            return src;
        }
    }

}
