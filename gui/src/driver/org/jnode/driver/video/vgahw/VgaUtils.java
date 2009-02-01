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
 
package org.jnode.driver.video.vgahw;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VgaUtils {

    public static void screenOff(VgaIO io) {
        io.setSEQ(0, 1);
        io.setSEQ(1, io.getSEQ(1) | 0x20);
        io.setSEQ(0, 3);
        io.getSTAT();
        io.setATTIndex(0);
    }

    public static void screenOn(VgaIO io) {
        io.setSEQ(0, 1);
        io.setSEQ(1, io.getSEQ(1) & 0xDF);
        io.setSEQ(0, 3);
        io.getSTAT();
        io.setATTIndex(0x20);
    }

    public static void setColorMode(VgaIO io) {
        io.setMISC(io.getMISC() | 0x01);
    }

    public static void setMonoMode(VgaIO io) {
        io.setMISC(io.getMISC() & 0xFE);
    }

    /**
     * Disable access to CRTC registers 0x00-0x07
     * 
     * @param io
     */
    public static void lockCRTC(VgaIO io) {
        io.setCRT(0x11, io.getCRT(0x11) | 0x80);
    }

    /**
     * Enable access to CRTC registers 0x00-0x07
     * 
     * @param io
     */
    public static void unlockCRTC(VgaIO io) {
        io.setCRT(0x11, io.getCRT(0x11) & ~0x80);
    }

}
