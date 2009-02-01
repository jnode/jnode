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

import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.system.MemoryResource;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class FPIBlock {
    private final int fbk_divider;

    private final String name;
    private final int post_divider;
    private final int ref_divider;

    private final FPITimingBlock timing;
    private final boolean use_bios_dividers;

    private final int xres;

    private final int yres;

    /**
     * Initialize this instance.
     * 
     * @param biosRom
     */
    FPIBlock(MemoryResource biosRom, int offset) {
        final byte[] nameArr = new byte[24];
        biosRom.getBytes(offset + 1, nameArr, 0, nameArr.length);
        this.name = new String(nameArr).trim();
        this.xres = biosRom.getChar(offset + 25);
        this.yres = biosRom.getChar(offset + 27);

        this.ref_divider = biosRom.getChar(offset + 46);
        this.post_divider = biosRom.getByte(offset + 48) & 0xFF;
        this.fbk_divider = biosRom.getChar(offset + 49);
        this.use_bios_dividers = ((ref_divider != 0) && (fbk_divider > 3));

        System.out.println("BIOS: rd=" + ref_divider + ", pd=" + post_divider + ", fbd=" +
                fbk_divider);

        // Read timing blocks
        FPITimingBlock foundTiming = null;
        for (int i = 0; i < 20; i++) {
            final int ofs = biosRom.getShort(offset + 64 + (i * 2)) & 0xFFFF;
            if (ofs == 0) {
                break;
            }
            final FPITimingBlock timing = new FPITimingBlock(biosRom, ofs);
            if ((timing.getXres() == xres) && (timing.getYres() == yres)) {
                foundTiming = timing;
                break;
            }
        }
        this.timing = foundTiming;
    }

    /**
     * Gets the best matching mode.
     */
    public DisplayMode getBestMode(DisplayMode src) {
        if (timing != null) {
            return timing.toDisplayMode(src, xres, yres);
        } else {
            throw new IllegalArgumentException("No matching mode found");
        }
    }

    /**
     * @return Returns the fbk_divider.
     */
    final int getBiosFeedbackDivider() {
        return fbk_divider;
    }

    /**
     * @return Returns the post_divider.
     */
    final int getBiosPostDivider() {
        return post_divider;
    }

    /**
     * @return Returns the ref_divider.
     */
    final int getBiosRefDivider() {
        return ref_divider;
    }

    /**
     * @return Returns the name.
     */
    final String getName() {
        return name;
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

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Name:");
        buf.append(getName());
        buf.append(", XRes:");
        buf.append(getXres());
        buf.append(", YRes:");
        buf.append(getYres());
        if (use_bios_dividers) {
            buf.append(", BIOS-divs(rd=");
            buf.append(ref_divider);
            buf.append(", pd=");
            buf.append(post_divider);
            buf.append("fbd=");
            buf.append(fbk_divider);
            buf.append(")");
        }
        if (timing != null) {
            buf.append(", Timing:");
            buf.append(timing);
        }
        return buf.toString();
    }

    /**
     * @return Returns the use_bios_dividers.
     */
    final boolean useBiosDividers() {
        return use_bios_dividers;
    }
}
