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
 
package org.jnode.driver.ps2;

/**
 * @author qades
 */
public interface PS2Constants {

    String PS2_KEYBOARD_DEV = "ps2keyboard";
    String PS2_POINTER_DEV = "ps2mouse";

    int KB_IRQ = 1;
    int MOUSE_IRQ = 12;

    int PS2_DATA_PORT = 0x60;
    int PS2_CTRL_PORT = 0x64;
    int PS2_STAT_PORT = 0x64;

    /*
     * Controller Commands
     */
    int CCMD_READ_MODE = 0x20; /* Read mode bits */
    int CCMD_WRITE_MODE = 0x60; /* Write mode bits */
    int CCMD_GET_VERSION = 0xA1; /* Get controller version */
    int CCMD_MOUSE_DISABLE = 0xA7; /* Disable mouse interface */
    int CCMD_MOUSE_ENABLE = 0xA8; /* Enable mouse interface */
    int CCMD_TEST_MOUSE = 0xA9; /* Mouse interface test */
    int CCMD_SELF_TEST = 0xAA; /* Controller self test */
    int CCMD_KB_TEST = 0xAB; /* Keyboard interface test */
    int CCMD_KB_DISABLE = 0xAD; /* Keyboard interface disable */
    int CCMD_KB_ENABLE = 0xAE; /* Keyboard interface enable */
    int CCMD_WRITE_AUX_OBUF = 0xD3; /*
                                     * Write to output buffer as if initiated by
                                     * the auxiliary device
                                     */
    int CCMD_WRITE_MOUSE = 0xD4; /* Write the following byte to the mouse */

    /*
     * Status Register Bits
     */

    int STAT_OBF = 0x01; /* Keyboard output buffer full */
    int STAT_IBF = 0x02; /* Keyboard input buffer full */
    int STAT_SELFTEST = 0x04; /* Self test successful */
    int STAT_CMD = 0x08; /* Last write was a command write (0=data) */
    int STAT_UNLOCKED = 0x10; /* Zero if keyboard locked */
    int STAT_MOUSE_OBF = 0x20; /* Mouse output buffer full */
    int STAT_GTO = 0x40; /* General receive/xmit timeout */
    int STAT_PERR = 0x80; /* Parity error */

    /*
     * Controller Mode Register Bits
     */

    int MODE_INT = 0x01; /* Keyboard data generate IRQ1 */
    int MODE_MOUSE_INT = 0x02; /* Mouse data generate IRQ12 */
    int MODE_SYS = 0x04; /* The system flag (?) */
    int MODE_NO_KEYLOCK = 0x08; /*
                                 * The keylock doesn't affect the keyboard if
                                 * set
                                 */
    int MODE_DISABLE_KBD = 0x10; /* Disable keyboard interface */
    int MODE_DISABLE_MOUSE = 0x20; /* Disable mouse interface */
    int MODE_KCC = 0x40; /* Scan code conversion to PC format */
    int MODE_RFU = 0x80;

    int MODE_DEFAULT = MODE_INT | MODE_MOUSE_INT | MODE_SYS | MODE_KCC;

    /*
     * Device commands
     */
    int CMD_GET_ID = 0xF2; /* Get the device ID */
    int CMD_SET_RATE = 0xF3; /* Set device rate */
    int CMD_ENABLE = 0xF4; /* Enable scanning */
    int CMD_DISABLE = 0xF5; /* Disable scanning */
    int CMD_RESET = 0xFF; /* Reset */

    /*
     * Device Replies
     */

    int REPLY_POR = 0xAA; /* Power on reset */
    int REPLY_ACK = 0xFA; /* Command ACK */
    int REPLY_RESEND = 0xFE; /* Command NACK, send the cmd again */

    int COMMAND_TIMEOUT = 750;

}
