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
 
package org.jnode.driver.video.nvidia;

import java.util.HashMap;

import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaHardwareCursor implements NVidiaConstants, HardwareCursorAPI {

    private final NVidiaVgaIO vgaIO;
    private final int architecture;
    /** Map between HardwareCursorImage and short[] */
    private final HashMap<HardwareCursorImage, short[]> cursorCache =
            new HashMap<HardwareCursorImage, short[]>();
    // cursor bitmap will be stored at the start of the framebuffer
    private static final int CURSOR_ADDRESS = 0;
    private static final int CURSOR_SIZE = 32;

    public NVidiaHardwareCursor(NVidiaVgaIO vgaIO, int architecture) {
        this.vgaIO = vgaIO;
        this.architecture = architecture;
    }

    final void initCursor() {

        /* set cursor bitmap adress ... */
        if (architecture <= NV04A) { /* or laptop */
            /* must be used this way on pre-NV10 and on all 'Go' cards! */
            /* cursorbitmap must start on 2Kbyte boundary: */
            /* set adress bit11-16, and set 'no doublescan' (registerbit 1 = 0) */
            vgaIO.setCRT(NVCRTCX_CURCTL0, ((CURSOR_ADDRESS & 0x0001f800) >> 9));
            /*
             * set adress bit17-23, and set graphics mode cursor(?) (registerbit
             * 7 = 1)
             */
            vgaIO.setCRT(NVCRTCX_CURCTL1, (((CURSOR_ADDRESS & 0x00fe0000) >> 17) | 0x80));
            /* set adress bit24-31 */
            vgaIO.setCRT(NVCRTCX_CURCTL2, ((CURSOR_ADDRESS & 0xff000000) >> 24));
        } else {
            /*
             * upto 4Gb RAM adressing: can be used on NV10 and later (except for
             * 'Go' cards)!
             */
            /*
             * NOTE: This register does not exist on pre-NV10 and 'Go' cards.
             */

            /* cursorbitmap must still start on 2Kbyte boundary: */
            vgaIO.setReg32(NV32_NV10CURADD32, CURSOR_ADDRESS & 0xfffff800);
        }

        /*
         * set cursor colour: not needed because of direct nature of cursor
         * bitmap.
         */

        /* clear cursor */
        vgaIO.getVideoMem().setShort(CURSOR_ADDRESS, (short) 0x7fff, CURSOR_SIZE * CURSOR_SIZE);

        /* select 32x32 pixel, 16bit color cursorbitmap, no doublescan */
        vgaIO.setReg32(NV32_CURCONF, 0x02000100);

        /* de-activate hardware cursor for now */
        setCursorVisible(false);
    }

    public void setCursorVisible(boolean visible) {
        int temp = vgaIO.getCRT(NVCRTCX_CURCTL0);
        if (visible) {
            temp |= 0x01;
        } else {
            temp &= 0xfe;
        }
        vgaIO.setCRT(NVCRTCX_CURCTL0, temp);
    }

    public void setCursorPosition(int x, int y) {
        vgaIO.setReg32(NVDAC_CURPOS, ((x & 0x0fff) | ((y & 0x0fff) << 16)));
    }

    /**
     * Sets the cursor image.
     */
    public void setCursorImage(HardwareCursor cursor) {
        final short[] cur = getCursor(cursor);
        if (cur != null) {
            vgaIO.getVideoMem().setShorts(cur, 0, CURSOR_ADDRESS, CURSOR_SIZE * CURSOR_SIZE);
        }
    }

    /**
     * Close the hw cursor.
     */
    public void closeCursor() {
        /* clear cursor */
        vgaIO.getVideoMem().setShort(CURSOR_ADDRESS, (short) 0x7fff, CURSOR_SIZE * CURSOR_SIZE);
        // Hide the cursor
        setCursorVisible(false);
    }

    private short[] getCursor(HardwareCursor cursor) {
        final HardwareCursorImage img = cursor.getImage(CURSOR_SIZE, CURSOR_SIZE);
        if (img == null) {
            return null;
        }
        short[] res = cursorCache.get(img);
        if (res == null) {
            res = new short[CURSOR_SIZE * CURSOR_SIZE];
            final int[] argb = img.getImage();
            for (int i = 0; i < CURSOR_SIZE * CURSOR_SIZE; i++) {
                final int v = argb[i];
                final int a = (v >>> 24) & 0xFF;
                final int r = ((v >> 16) & 0xFF) >> 3;
                final int g = ((v >> 8) & 0xFF) >> 3;
                final int b = (v & 0xFF) >> 3;

                res[i] = (short) ((r << 10) | (g << 5) | b);
                if (a != 0) {
                    res[i] |= 0x8000;
                }
            }
            cursorCache.put(img, res);
        }
        return res;
    }
}
