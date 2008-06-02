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

package org.jnode.driver.block.usb.storage;

import org.jnode.driver.bus.usb.USBConstants;

/**
 * @author flesire
 *         <p/>
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface USBStorageConstants extends USBConstants {
    /* Sub Classes */
    public final static int US_SC_RBC = 0x01;        /* Typically, flash devices */
    public final static int US_SC_8020 = 0x02;        /* CD-ROM */
    public final static int US_SC_QIC = 0x03;        /* QIC-157 Tapes */
    public final static int US_SC_UFI = 0x04;        /* Floppy */
    public final static int US_SC_8070 = 0x05;        /* Removable media */
    public final static int US_SC_SCSI = 0x06;        /* Transparent */
    public final static int US_SC_ISD200 = 0x07;        /* ISD200 ATA */

    public final static int US_SC_MIN = US_SC_RBC;
    public final static int US_SC_MAX = US_SC_ISD200;
    public final static int US_SC_DEVICE = 0xff;        /* Use device's value */

    /* Protocols */
    public final static int US_PR_CBI = 0x00;        /* Control/Bulk/Interrupt */
    public final static int US_PR_CB = 0x01;        /* Control/Bulk w/o interrupt */
    public final static int US_PR_BULK = 0x50;        /* bulk only */

    public final static int US_PR_SCM_ATAPI = 0x80;        /* SCM-ATAPI bridge */
    public final static int US_PR_EUSB_SDDR09 = 0x81;    /* SCM-SCSI bridge for SDDR-09 */
    public final static int US_PR_SDDR55 = 0x82;        /* SDDR-55 (made up) */
    public final static int US_PR_DPCM_USB = 0xf0;        /* Combination CB/SDDR09 */
    public final static int US_PR_FREECOM = 0xf1;        /* Freecom */
    public final static int US_PR_DATAFAB = 0xf2;        /* Datafab chipsets */
    public final static int US_PR_JUMPSHOT = 0xf3;        /* Lexar Jumpshot */
    public final static int US_PR_DEVICE = 0xff;        /* Use device's value */

    /* Transport */
    public final static int CB_RESET_CMD_SIZE = 12;

    public final static int US_BULK_CB_SIGN = 0x43425355;    /*spells out USBC */
    public final static int US_BULK_CB_WRAP_LEN = 31;
    public final static int US_BULK_CB_LUN_MASK = 0x07;
    public final static int US_BULK_FLAG_OUT = 0x00;
    public final static int US_BULK_FLAG_IN = 0x80;


    public final static int US_BULK_CS_SIGN = 0x53425355;    /*spells out USBS */
    public final static int US_BULK_CS_WRAP_LEN = 13;

    public final static int US_BULK_CS_CMD_NO_ERROR = 0x00; // No error occurs
    public final static int US_BULK_CS_CMD_FAILED = 0x01;
    public final static int US_BULK_CS_CMD_WRONG_SEQUENCE = 0x02;


}
