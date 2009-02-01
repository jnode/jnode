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
 
package org.jnode.driver.bus.usb.uhci;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface UHCIConstants {
    /*
      * Universal Host Controller Interface data structures and defines
      */

    /* Command register */
    public static final int USBCMD = 0;
    public static final int USBCMD_RS = 0x0001; /* Run/Stop */
    public static final int USBCMD_HCRESET = 0x0002; /* Host reset */
    public static final int USBCMD_GRESET = 0x0004; /* Global reset */
    public static final int USBCMD_EGSM = 0x0008; /* Global Suspend Mode */
    public static final int USBCMD_FGR = 0x0010; /* Force Global Resume */
    public static final int USBCMD_SWDBG = 0x0020; /* SW Debug mode */
    public static final int USBCMD_CF = 0x0040; /* Config Flag (sw only) */
    public static final int USBCMD_MAXP = 0x0080;
    /*
      * Max Packet (0 = 32, 1 = 64)
      */

    /* Status register */
    public static final int USBSTS = 2;
    public static final int USBSTS_USBINT = 0x0001; /* Interrupt due to IOC */
    public static final int USBSTS_ERROR = 0x0002; /* Interrupt due to error */
    public static final int USBSTS_RD = 0x0004; /* Resume Detect */
    public static final int USBSTS_HSE = 0x0008;
    /*
      * Host System Error - basically PCI problems
      */
    public static final int USBSTS_HCPE = 0x0010;
    /*
      * Host Controller Process Error - the scripts were buggy
      */
    public static final int USBSTS_HCH = 0x0020; /* HC Halted */

    /* Interrupt enable register */
    public static final int USBINTR = 4;
    public static final int USBINTR_TIMEOUT = 0x0001;
    /*
      * Timeout/CRC error enable
      */
    public static final int USBINTR_RESUME = 0x0002; /* Resume interrupt enable */
    public static final int USBINTR_IOC = 0x0004;
    /*
      * Interrupt On Complete enable
      */
    public static final int USBINTR_SP = 0x0008;
    /*
      * Short packet interrupt enable
      */

    public static final int USBFRNUM = 6;
    public static final int USBFLBASEADD = 8;
    public static final int USBSOF = 12;

    /* USB port status and control registers */
    public static final int USBPORTSC1 = 16;
    public static final int USBPORTSC2 = 18;
    /*
      * Current Connect Status ("device present")
      */
    public static final int USBPORTSC_CCS = 0x0001; /* Current connect status */
    public static final int USBPORTSC_CSC = 0x0002; /* Connect Status Change */
    public static final int USBPORTSC_PE = 0x0004; /* Port Enable */
    public static final int USBPORTSC_PEC = 0x0008; /* Port Enable Change */
    public static final int USBPORTSC_LS = 0x0030; /* Line Status */
    public static final int USBPORTSC_RD = 0x0040; /* Resume Detect */
    public static final int USBPORTSC_LSDA = 0x0100;
    /*
      * Low Speed Device Attached
      */
    public static final int USBPORTSC_PR = 0x0200; /* Port Reset */
    public static final int USBPORTSC_SUSP = 0x1000; /* Suspend */

    /*
      * Legacy support PCI registers
      */
    public static final int USBLEGSUP = 0xC0;
    public static final int USBLEGSUP_DEFAULT = 0x2000; /* only PIRQ enable set */

    /*
      * for TD-status
      */
    public static final int TD_CTRL_SPD = (1 << 29); /* Short Packet Detect */
    public static final int TD_CTRL_C_ERR_MASK = (3 << 27); /* Error Counter bits */
    public static final int TD_CTRL_C_ERR_SHIFT = 27;
    public static final int TD_CTRL_LS = (1 << 26); /* Low Speed Device */
    public static final int TD_CTRL_IOS = (1 << 25); /* Isochronous Select */
    public static final int TD_CTRL_IOC = (1 << 24); /* Interrupt on Complete */
    public static final int TD_CTRL_ACTIVE = (1 << 23); /* TD Active */
    public static final int TD_CTRL_STALLED = (1 << 22); /* TD Stalled */
    public static final int TD_CTRL_DBUFERR = (1 << 21); /* Data Buffer Error */
    public static final int TD_CTRL_BABBLE = (1 << 20); /* Babble Detected */
    public static final int TD_CTRL_NAK = (1 << 19); /* NAK Received */
    public static final int TD_CTRL_CRCTIMEO = (1 << 18); /* CRC/Time Out Error */
    public static final int TD_CTRL_BITSTUFF = (1 << 17); /* Bit Stuff Error */
    public static final int TD_CTRL_ACTLEN_MASK = 0x7FF; /* actual length, encoded as n - 1 */

    public static final int TD_CTRL_ANY_ERROR =
        (TD_CTRL_STALLED | TD_CTRL_DBUFERR | TD_CTRL_BABBLE | TD_CTRL_CRCTIMEO | TD_CTRL_BITSTUFF);
}
