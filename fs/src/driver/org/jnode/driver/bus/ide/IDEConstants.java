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
 
package org.jnode.driver.bus.ide;

/**
 * @author epr
 */
public interface IDEConstants {

    /**
     * First port number of first IDE Task file
     */
    public static final int IDE0_START_PORT = 0x1f0;

    /**
     * First port number of second IDE Task file
     */
    public static final int IDE1_START_PORT = 0x170;

    /**
     * Data register offset in Task file
     */
    public static final int RW16_DATA_OFFSET = 0;

    /**
     * Error register offset in Task file
     */
    public static final int R8_ERROR_OFFSET = 1;

    /**
     * Feature register offset in Task file
     */
    public static final int W8_FEATURE_OFFSET = 1;

    /**
     * #Sectors register offset in Task file
     */
    public static final int RW8_SECTOR_COUNT_OFFSET = 2;

    /**
     * Sector register offset in Task file
     */
    public static final int RW8_SECTOR_OFFSET = 3;
    public static final int RW8_LBA_LOW_OFFSET = RW8_SECTOR_OFFSET;

    /**
     * Cylinder LSB register offset in Task file
     */
    public static final int RW8_CYLINDER_LSB_OFFSET = 4;
    public static final int RW8_LBA_MID_OFFSET = RW8_CYLINDER_LSB_OFFSET;

    /**
     * Cylinder MSB register offset in Task file
     */
    public static final int RW8_CYLINDER_MSB_OFFSET = 5;
    public static final int RW8_LBA_HIGH_OFFSET = RW8_CYLINDER_MSB_OFFSET;

    /**
     * Drive/Head register offset in Task file
     */
    public static final int RW8_SELECT_OFFSET = 6;

    /**
     * Status register offset in Task file
     */
    public static final int R8_STATUS_OFFSET = 7;

    /**
     * Alternative status register offset in Task file
     */
    public static final int R8_ALTSTATUS_OFFSET = 0x0;

    /**
     * Control register offset in Task file
     */
    public static final int W8_CONTROL_OFFSET = 0x0;

    /**
     * Command register offset in Task file
     */
    public static final int W8_COMMAND_OFFSET = 7;

    public static final int HIGH_OFFSET = 0x206;
    public static final int IDE_NR_PORTS = 8;
    public static final int IDE_NR_HIGH_PORTS = 1;
    public static final int IDE_NR_TASKFILES = 2;
    public static final int IDE0_IRQ = 14;
    public static final int IDE1_IRQ = 15;
    public static final int SECTOR_SIZE = 512;

    // --------------------------------
    // Error bits

    /**
     * NDM Address mark not found
     */
    public static final int ERR_NDM = 0x01;

    /**
     * Track 0 not found
     */
    public static final int ERR_NT0 = 0x02;

    /**
     * Abort
     */
    public static final int ERR_ABORT = 0x04;

    /**
     * MCR
     */
    public static final int ERR_MCR = 0x08;

    /**
     * ID mark not found
     */
    public static final int ERR_NID = 0x10;

    /**
     * MC
     */
    public static final int ERR_MC = 0x20;

    /**
     * Unrecoverable error
     */
    public static final int ERR_UNC = 0x40;

    /**
     * Bad block
     */
    public static final int ERR_BBK = 0x80;

    // --------------------------------
    // Status bits
    public static final int ST_BUSY = 0x80;
    public static final int ST_DEVICE_READY = 0x40;
    public static final int ST_DEVICE_FAULT = 0x20;
    //public static final int ST_SKC = 0x10;
    public static final int ST_DATA_REQUEST = 0x08;
    public static final int ST_ERROR = 0x01;

    // --------------------------------
    // Drive/Head (SELECT) bits

    public static final int SEL_BLANK = 0xa0;
    public static final int SEL_HEAD_MASK = 0x0f;
    public static final int SEL_DRIVE_MASK = 0x10;
    public static final int SEL_DRIVE_SLAVE = 0x10;
    public static final int SEL_DRIVE_MASTER = 0x00;
    public static final int SEL_LBA = 0x40;

    // --------------------------------
    // Control bits

    /**
     * Default control value
     */
    public static final int CTR_BLANK = 0x00;
    /**
     * Interrupt enable (0==enabled, 1==disabled)
     */
    public static final int CTR_IEN = 0x02;
    /**
     * Software reset (1==reset, 0==reset finished)
     */
    public static final int CTR_SRST = 0x04;

    // --------------------------------
    // Timeout

    public static final int IDE_TIMEOUT = 1000; /* ms */
    // Timeout used in data transfer commands
    public static final long IDE_DATA_XFER_TIMEOUT = 10000; /* ms */

    // --------------------------------
    // ATA/ATAPI Commands pre T13 Spec
    public static final int CMD_NOP = 0x00;
    public static final int CFA_REQ_EXT_ERROR_CODE = 0x03; /* CFA Request Extended Error Code */
    public static final int CMD_SRST = 0x08; /* ATAPI soft reset command */
    public static final int CMD_DEVICE_RESET = 0x08;
    public static final int CMD_RESTORE = 0x10;
    public static final int CMD_READ = 0x20; /* 28-Bit */
    public static final int CMD_READ_EXT = 0x24; /* 48-Bit */
    public static final int CMD_READDMA_EXT = 0x25; /* 48-Bit */
    public static final int CMD_READDMA_QUEUED_EXT = 0x26; /* 48-Bit */
    public static final int CMD_READ_NATIVE_MAX_EXT = 0x27; /* 48-Bit */
    public static final int CMD_MULTREAD_EXT = 0x29; /* 48-Bit */
    public static final int CMD_WRITE = 0x30; /* 28-Bit */
    public static final int CMD_WRITE_EXT = 0x34; /* 48-Bit */
    public static final int CMD_WRITEDMA_EXT = 0x35; /* 48-Bit */
    public static final int CMD_WRITEDMA_QUEUED_EXT = 0x36; /* 48-Bit */
    public static final int CMD_SET_MAX_EXT = 0x37; /* 48-Bit */
    public static final int CFA_WRITE_SECT_WO_ERASE = 0x38; /* CFA Write Sectors without erase */
    public static final int CMD_MULTWRITE_EXT = 0x39; /* 48-Bit */
    public static final int CMD_WRITE_VERIFY = 0x3C; /* 28-Bit */
    public static final int CMD_VERIFY = 0x40; /* 28-Bit - Read Verify Sectors */
    public static final int CMD_VERIFY_EXT = 0x42; /* 48-Bit */
    public static final int CMD_FORMAT = 0x50;
    public static final int CMD_INIT = 0x60;
    public static final int CMD_SEEK = 0x70;
    public static final int CFA_TRANSLATE_SECTOR = 0x87; /* CFA Translate Sector */
    public static final int CMD_DIAGNOSE = 0x90;
    public static final int CMD_SPECIFY = 0x91; /* set drive geometry translation */
    public static final int CMD_DOWNLOAD_MICROCODE = 0x92;
    public static final int CMD_STANDBYNOW2 = 0x94;
    public static final int CMD_SETIDLE2 = 0x97;
    public static final int CMD_CHECKPOWERMODE2 = 0x98;
    public static final int CMD_SLEEPNOW2 = 0x99;
    public static final int CMD_PACKETCMD = 0xA0; /* Send a packet command. */
    public static final int CMD_PIDENTIFY = 0xA1; /* identify ATAPI device */
    public static final int CMD_QUEUED_SERVICE = 0xA2;
    public static final int CMD_SMART = 0xB0; /* self-monitoring and reporting */
    public static final int CFA_ERASE_SECTORS = 0xC0;
    public static final int CMD_MULTREAD = 0xC4; /* read sectors using multiple mode*/
    public static final int CMD_MULTWRITE = 0xC5; /* write sectors using multiple mode */
    public static final int CMD_SETMULT = 0xC6; /* enable/disable multiple mode */
    public static final int CMD_READDMA_QUEUED = 0xC7; /* read sectors using Queued DMA transfers */
    public static final int CMD_READDMA = 0xC8; /* read sectors using DMA transfers */
    public static final int CMD_WRITEDMA = 0xCA; /* write sectors using DMA transfers */
    public static final int CMD_WRITEDMA_QUEUED = 0xCC; /* write sectors using Queued DMA transfers */
    public static final int CFA_WRITE_MULTI_WO_ERASE = 0xCD; /* CFA Write multiple without erase */
    public static final int CMD_GETMEDIASTATUS = 0xDA;
    public static final int CMD_DOORLOCK = 0xDE; /* lock door on removable drives */
    public static final int CMD_DOORUNLOCK = 0xDF; /* unlock door on removable drives */
    public static final int CMD_STANDBYNOW1 = 0xE0;
    public static final int CMD_IDLEIMMEDIATE = 0xE1; /* force drive to become "ready" */
    public static final int CMD_STANDBY = 0xE2; /* Set device in Standby Mode */
    public static final int CMD_SETIDLE1 = 0xE3;
    public static final int CMD_READ_BUFFER = 0xE4; /* force read only 1 sector */
    public static final int CMD_CHECKPOWERMODE1 = 0xE5;
    public static final int CMD_SLEEPNOW1 = 0xE6;
    public static final int CMD_FLUSH_CACHE = 0xE7;
    public static final int CMD_WRITE_BUFFER = 0xE8; /* force write only 1 sector */
    public static final int CMD_FLUSH_CACHE_EXT = 0xEA; /* 48-Bit */
    public static final int CMD_IDENTIFY = 0xEC; /* ask drive to identify itself */
    public static final int CMD_MEDIAEJECT = 0xED;
    public static final int CMD_IDENTIFY_DMA = 0xEE; /* same as WIN_IDENTIFY, but DMA */
    public static final int CMD_SETFEATURES = 0xEF; /* set special drive features */
    public static final int EXABYTE_ENABLE_NEST = 0xF0;
    public static final int CMD_SECURITY_SET_PASS = 0xF1;
    public static final int CMD_SECURITY_UNLOCK = 0xF2;
    public static final int CMD_SECURITY_ERASE_PREPARE = 0xF3;
    public static final int CMD_SECURITY_ERASE_UNIT = 0xF4;
    public static final int CMD_SECURITY_FREEZE_LOCK = 0xF5;
    public static final int CMD_SECURITY_DISABLE = 0xF6;
    public static final int CMD_READ_NATIVE_MAX = 0xF8; /* return the native maximum address */
    public static final int CMD_SET_MAX = 0xF9;
    public static final int DISABLE_SEAGATE = 0xFB;

}
