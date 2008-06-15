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

/**
 * Implementation of hardware accelerated functions.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class RadeonAcceleration implements RadeonConstants {

    private static final int FIFO_TIMEOUT = 500;
    private final RadeonVgaIO io;

    /**
     * Initialize this instance.
     * 
     * @param io
     */
    public RadeonAcceleration(RadeonVgaIO io) {
        this.io = io;
    }

    /**
     * Copy contents of the screen from the given source to the given
     * destination location with a given size.
     * 
     * @param srcX
     * @param srcY
     * @param width
     * @param height
     * @param dstX
     * @param dstY
     */
    public void screenToScreenCopy(int srcX, int srcY, int width, int height, int dstX, int dstY) {

        waitForFifo(4);
        io.setReg32(SRC_Y_X, (srcY << 16) | srcX);
        io.setReg32(DP_MIX, ROP3_SRCCOPY | DP_SRC_RECT);
        io.setReg32(DP_CNTL, DST_X_LEFT_TO_RIGHT | DST_Y_TOP_TO_BOTTOM);
        io.setReg32(DP_DATATYPE, SRC_DSTCOLOR | io.getReg32(DP_DATATYPE));

        waitForFifo(2);
        io.setReg32(DST_Y_X, (dstY << 16) | dstX);
        // Initialize the operation
        io.setReg32(DST_HEIGHT_WIDTH, height << 16 | width);
    }

    public void waitForFifo(int entries) {
        final long startTick = System.currentTimeMillis();
        while ((io.getReg32(CP_CSQ_CNTL) & 0xFF) < entries) {
            // Wait
            final long now = System.currentTimeMillis();
            if ((now - startTick) > FIFO_TIMEOUT) {
                throw new IllegalStateException("FIFO not empty in time.");
            }
        }
    }
}
