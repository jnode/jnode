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
 
package org.jnode.driver.video.ati.radeon;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonHardwareCursor implements RadeonConstants, HardwareCursorAPI {

    /** My logger */
    private static final Logger log = Logger.getLogger(RadeonHardwareCursor.class);
    /** Radeon register accessor */
    private final RadeonVgaIO io;
    /** Map between HardwareCursorImage and short[] */
    private final HashMap<HardwareCursorImage, byte[]> cursorCache =
            new HashMap<HardwareCursorImage, byte[]>();
    /** Memory reserved for cursor images */
    private final MemoryResource cursorMem;

    private int horzOffset;
    private int vertOffset;

    /**
     * Initialize this instance.
     * 
     * @param io
     */
    public RadeonHardwareCursor(RadeonCore kernel, RadeonVgaIO io)
        throws IndexOutOfBoundsException, ResourceNotFreeException {
        this.io = io;
        this.cursorMem = kernel.claimDeviceMemory(4096, 16);
        log.debug("Cursor memory at offset 0x" + NumberUtils.hex(cursorMem.getOffset().toInt()));
    }

    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorImage(org.jnode.driver.video.HardwareCursor)
     */
    public void setCursorImage(HardwareCursor cursor) {

        // Background color
        io.setReg32(CUR_CLR0, 0xffffff);
        // Foreground color
        io.setReg32(CUR_CLR1, 0);

        // Set shape
        final byte[] cur = getCursor(cursor);
        if (cur != null) {
            io.setReg32(CUR_HORZ_VERT_OFF, vertOffset + (horzOffset << 16));
            cursorMem.setBytes(cur, 0, 0, 1024);
        }
    }

    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorPosition(int, int)
     */
    public void setCursorPosition(int x, int y) {
        // if upper-left corner of cursor is outside of
        // screen, we have to use special registers to clip it
        int xorigin = 0;
        int yorigin = 0;

        if (x < 0) {
            xorigin = -x;
        }

        if (y < 0) {
            yorigin = -y;
        }

        // Radeon_WaitForFifo( ai, 3 );

        io.setReg32(CUR_HORZ_VERT_OFF, CUR_LOCK | ((xorigin + horzOffset) << 16) |
                (yorigin + vertOffset));
        io.setReg32(CUR_HORZ_VERT_POSN, CUR_LOCK | (((xorigin != 0) ? 0 : x) << 16) |
                ((yorigin != 0) ? 0 : y));
        io.setReg32(CUR_OFFSET, (int) cursorMem.getOffset().toInt() + xorigin + yorigin * 16);
    }

    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorVisible(boolean)
     */
    public void setCursorVisible(boolean visible) {
        int tmp = io.getReg32(CRTC_GEN_CNTL);
        if (visible) {
            tmp |= CRTC_CUR_EN;
        } else {
            tmp &= ~CRTC_CUR_EN;
        }
        io.setReg32(CRTC_GEN_CNTL, tmp);
    }

    /**
     * Close this hw cursor
     */
    final void close() {
        setCursorVisible(false);
    }

    private byte[] getCursor(HardwareCursor cursor) {
        final HardwareCursorImage img = cursor.getImage(32, 32);
        if (img == null) {
            return null;
        }
        final int w = img.getWidth();
        final int h = img.getHeight();
        horzOffset = 64 - w;
        vertOffset = 64 - h;

        byte[] res = (byte[]) cursorCache.get(img);
        if (res == null) {
            res = new byte[1024];
            final int[] argb = img.getImage();

            for (int row = 0; row < h; row++) {
                final int imgOfs = row * w;
                final int resOfs = row * 16;

                for (int x = 0; x < w; x++) {
                    final int resRowIdx = resOfs + (((64 - w) + x) >> 3);
                    final int resRowBit = 1 << (7 - (((64 - w) + x) & 7));

                    final int v = argb[imgOfs + x];
                    final int a = (v >>> 24) & 0xFF;
                    final int r = ((v >> 16) & 0xFF);
                    final int g = ((v >> 8) & 0xFF);
                    final int b = (v & 0xFF);

                    if (a != 0) {
                        // Opaque
                        if (((r + g + b) / 3) >= 128) {
                            // White (CUR_CLR0)
                            // and:0, xor:0
                        } else {
                            // Black (CUR_CLR1)
                            // and:0, xor:1
                            res[resRowIdx + 8] |= resRowBit;
                        }
                    } else {
                        // Transparent
                        // and:1, xor:0
                        res[resRowIdx] |= resRowBit;
                    }
                }
            }
            cursorCache.put(img, res);
        }
        return res;
    }
}
