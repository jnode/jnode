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
 
package org.jnode.driver.bus.usb.hub;

import org.jnode.driver.bus.usb.USBConstants;

/**
 * Constants used in the USB HUB driver.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface USBHubConstants extends USBConstants {

    /*
      * Hub request types
      */
    public static final int USB_RT_HUB = (USB_TYPE_CLASS | USB_RECIP_DEVICE);
    public static final int USB_RT_PORT = (USB_TYPE_CLASS | USB_RECIP_OTHER);

    /*
      * Hub class requests See USB 2.0 spec Table 11-16
      */
    public static final int HUB_CLEAR_TT_BUFFER = 8;
    public static final int HUB_RESET_TT = 9;
    public static final int HUB_GET_TT_STATE = 10;
    public static final int HUB_STOP_TT = 11;

    /*
      * Hub Class feature numbers See USB 2.0 spec Table 11-17
      */
    public static final int C_HUB_LOCAL_POWER = 0;
    public static final int C_HUB_OVER_CURRENT = 1;

    /*
      * Port feature numbers See USB 2.0 spec Table 11-17
      */
    public static final int USB_PORT_FEAT_CONNECTION = 0;
    public static final int USB_PORT_FEAT_ENABLE = 1;
    public static final int USB_PORT_FEAT_SUSPEND = 2;
    public static final int USB_PORT_FEAT_OVER_CURRENT = 3;
    public static final int USB_PORT_FEAT_RESET = 4;
    public static final int USB_PORT_FEAT_POWER = 8;
    public static final int USB_PORT_FEAT_LOWSPEED = 9;
    public static final int USB_PORT_FEAT_HIGHSPEED = 10;
    public static final int USB_PORT_FEAT_C_CONNECTION = 16;
    public static final int USB_PORT_FEAT_C_ENABLE = 17;
    public static final int USB_PORT_FEAT_C_SUSPEND = 18;
    public static final int USB_PORT_FEAT_C_OVER_CURRENT = 19;
    public static final int USB_PORT_FEAT_C_RESET = 20;
    public static final int USB_PORT_FEAT_TEST = 21;
    public static final int USB_PORT_FEAT_INDICATOR = 22;

    /*
      * wPortStatus bit field See USB 2.0 spec Table 11-21
      */
    public static final int USB_PORT_STAT_CONNECTION = 0x0001;
    public static final int USB_PORT_STAT_ENABLE = 0x0002;
    public static final int USB_PORT_STAT_SUSPEND = 0x0004;
    public static final int USB_PORT_STAT_OVERCURRENT = 0x0008;
    public static final int USB_PORT_STAT_RESET = 0x0010;
    /* bits 5 to 7 are reserved */
    public static final int USB_PORT_STAT_POWER = 0x0100;
    public static final int USB_PORT_STAT_LOW_SPEED = 0x0200;
    public static final int USB_PORT_STAT_HIGH_SPEED = 0x0400;
    public static final int USB_PORT_STAT_TEST = 0x0800;
    public static final int USB_PORT_STAT_INDICATOR = 0x1000;
    /* bits 13 to 15 are reserved */

    /*
      * wPortChange bit field See USB 2.0 spec Table 11-22 Bits 0 to 4 shown, bits 5 to 15 are
      * reserved
      */
    public static final int USB_PORT_STAT_C_CONNECTION = 0x0001;
    public static final int USB_PORT_STAT_C_ENABLE = 0x0002;
    public static final int USB_PORT_STAT_C_SUSPEND = 0x0004;
    public static final int USB_PORT_STAT_C_OVERCURRENT = 0x0008;
    public static final int USB_PORT_STAT_C_RESET = 0x0010;

    /*
      * wHubCharacteristics (masks) See USB 2.0 spec Table 11-13, offset 3
      */
    public static final int HUB_CHAR_LPSM = 0x0003; /* D1 .. D0 */
    public static final int HUB_CHAR_COMPOUND = 0x0004; /* D2 */
    public static final int HUB_CHAR_OCPM = 0x0018; /* D4 .. D3 */
    public static final int HUB_CHAR_TTTT = 0x0060; /* D6 .. D5 */
    public static final int HUB_CHAR_PORTIND = 0x0080; /* D7 */

    /*
      * Hub Status & Hub Change bit masks See USB 2.0 spec Table 11-19 and Table 11-20 Bits 0 and 1
      * for wHubStatus and wHubChange Bits 2 to 15 are reserved for both
      */
    public static final int HUB_STATUS_LOCAL_POWER = 0x0001;
    public static final int HUB_STATUS_OVERCURRENT = 0x0002;
    public static final int HUB_CHANGE_LOCAL_POWER = 0x0001;
    public static final int HUB_CHANGE_OVERCURRENT = 0x0002;

    /*
      * Hub descriptor See USB 2.0 spec Table 11-13
      */
    public static final int USB_DT_HUB = (USB_TYPE_CLASS | 0x09);
    public static final int USB_DT_HUB_NONVAR_SIZE = 7;
}
