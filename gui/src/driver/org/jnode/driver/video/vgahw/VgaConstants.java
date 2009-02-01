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
 * @author epr
 */
public interface VgaConstants {

    public static final byte[] REDS = {0, 0, 0, 0, (byte) 168, (byte) 168, (byte) 168, (byte) 168, (byte) 84, (byte) 84,
        (byte) 84, (byte) 84, (byte) 255, (byte) 255, (byte) 255, (byte) 255};
    public static final byte[] GREENS = {0, 0, (byte) 168, (byte) 168, 0, 0, (byte) 84, (byte) 168, (byte) 84,
        (byte) 84, (byte) 255, (byte) 255, (byte) 84, (byte) 84, (byte) 255, (byte) 255};
    public static final byte[] BLUES = {0, (byte) 168, 0, (byte) 168, 0, (byte) 168, 0, (byte) 168, (byte) 84,
        (byte) 255, (byte) 84, (byte) 255, (byte) 84, (byte) 255, (byte) 84, (byte) 255};

    public static final int VGA_FIRST_PORT = 0x3b0;
    public static final int VGA_LAST_PORT = VGA_FIRST_PORT + 0x2f;

    /** Attribute index register */
    public static final int ATT_I = VGA_FIRST_PORT + 0x10;
    /** Attribute data read register */
    public static final int ATT_DR = VGA_FIRST_PORT + 0x11;
    /** Attribute data write register */
    public static final int ATT_DW = VGA_FIRST_PORT + 0x10;

    /** CRT Control index register */
    public static final int CRTC_I = VGA_FIRST_PORT + 0x24;
    /** CRT Control data register */
    public static final int CRTC_D = VGA_FIRST_PORT + 0x25;

    /** Miscellaneous write registers */
    public static final int MISC_W = VGA_FIRST_PORT + 0x12;
    /** Miscellaneous read registers */
    public static final int MISC_R = VGA_FIRST_PORT + 0x1C;

    /** Sequencer index register */
    public static final int SEQ_I = VGA_FIRST_PORT + 0x14;
    /** Sequencer data register */
    public static final int SEQ_D = VGA_FIRST_PORT + 0x15;

    /** DAC State register (read only) */
    public static final int DAC_SR = VGA_FIRST_PORT + 0x17;
    /** DAC Address Read mode register (write only) */
    public static final int DAC_RI = VGA_FIRST_PORT + 0x17;
    /** DAC Address Write mode register */
    public static final int DAC_WI = VGA_FIRST_PORT + 0x18;
    /** DAC Data register */
    public static final int DAC_D = VGA_FIRST_PORT + 0x19;

    /** Graphics-mode index register */
    public static final int GRAF_I = VGA_FIRST_PORT + 0x1E;
    /** Graphics-mode data register */
    public static final int GRAF_D = VGA_FIRST_PORT + 0x1F;

    /** CRT Control Mode index register */
    public static final int CRTC_IM = VGA_FIRST_PORT + 0x04;
    /** CRT Control Mode data register */
    public static final int CRTC_DM = VGA_FIRST_PORT + 0x05;

    /** Status color register */
    public static final int STATC = VGA_FIRST_PORT + 0x2A;
    /** Status mono register */
    public static final int STATM = VGA_FIRST_PORT + 0x0A;

    /** VGA Error register */
    public static final int VGAE = VGA_FIRST_PORT + 0x13;

    // Logical operation

    public static final int LOGOP_NONE = 0x00;
    public static final int LOGOP_AND = 0x08;
    public static final int LOGOP_OR = 0x10;
    public static final int LOGOP_XOR = 0x18;
}
