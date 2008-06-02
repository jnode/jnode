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

package org.jnode.driver.block.floppy;

import org.jnode.driver.block.Geometry;

/**
 * @author epr
 */
public interface FloppyConstants {

    /**
     * IRQ Number for floppy disk controller
     */
    public static final int FLOPPY_IRQ = 6;

    /**
     * DMA channel for floppy disk controller
     */
    public static final int FLOPPY_DMA = 2;

    /**
     * Starting I/O address of primary controller
     */
    public static final int PRIMARY_START_PORT = 0x3f0;
    /**
     * Starting I/O address of secondary controller
     */
    public static final int SECONDARY_START_PORT = 0x370;
    /**
     * Number of ports starting from PR/SE_START_PORT
     */
    public static final int NR_PORTS_RANGE1 = 6;
    public static final int OFFSET_RANGE1 = 0;
    public static final int NR_PORTS_RANGE2 = 1;
    public static final int OFFSET_RANGE2 = 7;

    /**
     * Maximum number of result bytes from the FDC
     */
    public static final int MAX_REPLIES = 16;

    /**
     * Digital Output Register offset
     */
    public static final int RW8_DOR_OFFSET = 2;

    /**
     * Status register offset
     */
    public static final int R8_STATE_OFFSET = 4;

    /**
     * Data register offset
     */
    public static final int RW8_DATA_OFFSET = 5;

    /**
     * Digital Input Register offset
     */
    public static final int R8_DIR_OFFSET = 7;

    // Digital Output Register values
    public static final int DOR_DRIVE0 = 0x00;
    public static final int DOR_DRIVE1 = 0x01;
    public static final int DOR_DRIVE2 = 0x02;
    public static final int DOR_DRIVE3 = 0x03;
    public static final int DOR_NRESET = 0x04; // 1=active, 0=reset
    public static final int DOR_DMA = 0x08; // DMA & IRQ active
    public static final int DOR_MOTOR0 = 0x10;
    public static final int DOR_MOTOR1 = 0x20;
    public static final int DOR_MOTOR2 = 0x40;
    public static final int DOR_MOTOR3 = 0x80;

    public static final int DOR_DRIVE_MASK = 0x03;

    // Stage register values
    public static final int STATE_ACTIVE0 = 0x01;
    public static final int STATE_ACTIVE1 = 0x02;
    public static final int STATE_ACTIVE2 = 0x04;
    public static final int STATE_ACTIVE3 = 0x08;
    public static final int STATE_BUSY = 0x10;
    public static final int STATE_NDMA = 0x20;
    public static final int STATE_DIO = 0x40;
    public static final int STATE_READY = 0x80;

    // Digital Input Register values
    public static final int DIR_DISKCHANGE = 0x80;

    public static final int[] SECTOR_LENGTH = {
        128, 256, 512, 1024, 2048, 4096, 8192, 16384
    };

    // ST0 bits
    public static final int ST0_DRIVE_MASK = 0x03;
    public static final int ST0_HEAD1 = 0x04;
    public static final int ST0_NOTREADY = 0x08;
    public static final int ST0_EQUIPCHECK = 0x10;
    public static final int ST0_SEEKREADY = 0x20;
    public static final int ST0_CMDST_MASK = 0xC0;
    public static final int ST0_CMDST_NORMAL = 0x00; // Command finished normal
    public static final int ST0_CMDST_ERROR = 0x40; // Command finished with an error
    public static final int ST0_CMDST_INVCMD = 0x80; // Invalid command
    public static final int ST0_CMDST_POLL_ERROR = 0xC0; // Error due to change in ready signal

    public static final int NR_DRIVES = 4;

    /*

    {
        6240, 39, 2, 80, 0, 0x1B, 0x43, 0xAF, 0x28, "E3120"
    }

    ,**9 3.12MB3.5"

    {
        2880, 18, 2, 80, 0, 0x25, 0x00, 0xDF, 0x02, "h1440"
    }

    ,**10 1.44MB5.25"

    {
        3360, 21, 2, 80, 0, 0x1C, 0x00, 0xCF, 0x0C, "H1680"
    }

    ,**11 1.68MB3.5"

    {
        820, 10, 2, 41, 1, 0x25, 0x01, 0xDF, 0x2E, "h410"
    }

    ,**12 410KB5.25"

    {
        1640, 10, 2, 82, 0, 0x25, 0x02, 0xDF, 0x2E, "H820"
    }

    ,**13 820KB3.5"

    {
        2952, 18, 2, 82, 0, 0x25, 0x00, 0xDF, 0x02, "h1476"
    }

    ,**14 1.48MB5.25"

    {
        3444, 21, 2, 82, 0, 0x25, 0x00, 0xDF, 0x0C, "H1722"
    }

    ,**15 1.72MB3.5"

    {
        840, 10, 2, 42, 1, 0x25, 0x01, 0xDF, 0x2E, "h420"
    }

    ,**16 420KB5.25"

    {
        1660, 10, 2, 83, 0, 0x25, 0x02, 0xDF, 0x2E, "H830"
    }

    ,**17 830KB3.5"

    {
        2988, 18, 2, 83, 0, 0x25, 0x00, 0xDF, 0x02, "h1494"
    }

    ,**18 1.49MB5.25"

    {
        3486, 21, 2, 83, 0, 0x25, 0x00, 0xDF, 0x0C, "H1743"
    }

    ,**19 1.74MB3.5"

    {
        1760, 11, 2, 80, 0, 0x1C, 0x09, 0xCF, 0x00, "h880"
    }

    ,**20 880KB5.25"

    {
        2080, 13, 2, 80, 0, 0x1C, 0x01, 0xCF, 0x00, "D1040"
    }

    ,**21 1.04MB3.5"

    {
        2240, 14, 2, 80, 0, 0x1C, 0x19, 0xCF, 0x00, "D1120"
    }

    ,**22 1.12MB3.5"

    {
        3200, 20, 2, 80, 0, 0x1C, 0x20, 0xCF, 0x2C, "h1600"
    }

    ,**23 1.6MB5.25"

    {
        3520, 22, 2, 80, 0, 0x1C, 0x08, 0xCF, 0x2e, "H1760"
    }

    ,**24 1.76MB3.5"

    {
        3840, 24, 2, 80, 0, 0x1C, 0x20, 0xCF, 0x00, "H1920"
    }

    ,**25 1.92MB3.5"

    {
        6400, 40, 2, 80, 0, 0x25, 0x5B, 0xCF, 0x00, "E3200"
    }

    ,**26 3.20MB3.5"

    {
        7040, 44, 2, 80, 0, 0x25, 0x5B, 0xCF, 0x00, "E3520"
    }

    ,**27 3.52MB3.5"

    {
        7680, 48, 2, 80, 0, 0x25, 0x63, 0xCF, 0x00, "E3840"
    }

    ,**28 3.84MB3.5"

    {
        3680, 23, 2, 80, 0, 0x1C, 0x10, 0xCF, 0x00, "H1840"
    }

    ,**29 1.84MB3.5"

    {
        1600, 10, 2, 80, 0, 0x25, 0x02, 0xDF, 0x2E, "D800"
    }

    ,**30 800KB3.5"

    {
        3200, 20, 2, 80, 0, 0x1C, 0x00, 0xCF, 0x2C, "H1600"
    }

    ,**31 1.6MB3.5"
    TAKEN FROM
    Linux kernel
    */

    /**
     * 360K PC
     */
    public static final FloppyParameters FP_d360 =
        new FloppyParameters(new Geometry(40, 2, 9), 0x2a, 0x02, 0xDF, "d360");
    /**
     * 1.2M AT
     */
    public static final FloppyParameters FP_h1200 =
        new FloppyParameters(new Geometry(80, 2, 15), 0x1b, 0x00, 0xDF, "h1200");
    /**
     * 360K SS 3.5"
     */
    public static final FloppyParameters FP_D360 =
        new FloppyParameters(new Geometry(80, 1, 9), 0x2a, 0x02, 0xDF, "D360");
    /**
     * 720K 3.5"
     */
    public static final FloppyParameters FP_D720 =
        new FloppyParameters(new Geometry(80, 2, 9), 0x2a, 0x02, 0xDF, "D720");
    /**
     * 360K AT
     */
    public static final FloppyParameters FP_h360 =
        new FloppyParameters(new Geometry(40, 2, 9), 0x23, 0x01, 0xDF, "h360");
    /**
     * 720K AT
     */
    public static final FloppyParameters FP_h720 =
        new FloppyParameters(new Geometry(80, 2, 9), 0x23, 0x01, 0xDF, "h720");
    /**
     * 1.44M 3.5"
     */
    public static final FloppyParameters FP_H1440 =
        new FloppyParameters(new Geometry(80, 2, 18), 0x1b, 0x00, 0xCF, "H1440");
    /**
     * 2.88M 3.5"
     */
    public static final FloppyParameters FP_E2880 =
        new FloppyParameters(new Geometry(80, 2, 36), 0x1b, 0x43, 0xAF, "E2880");

    /**
     * All known floppy parameters
     */
    public static final FloppyParameters[] FLOPPY_PARAMS = {
        FP_d360, FP_h1200, FP_D360, FP_D720, FP_h360, FP_h720, FP_H1440, FP_E2880
    };

    // ------------------------------------------
    // Drive parameters

    /**
     * Unknown
     */
    public static final FloppyDriveParameters FDP_UNKNOWN =
        new FloppyDriveParameters(0, 500, 16, 16, 8000, "unknown",
            new FloppyParameters[]{FP_H1440, FP_D720, FP_E2880, FP_h1200, FP_d360, FP_h360, FP_D360});

    /**
     * 360K PC
     */
    public static final FloppyDriveParameters FDP_360K =
        new FloppyDriveParameters(1, 300, 16, 16, 8000, "360K PC",
            new FloppyParameters[]{FP_d360});

    /**
     * 5 1/4 HD AT
     */
    public static final FloppyDriveParameters FDP_1200K =
        new FloppyDriveParameters(2, 500, 16, 16, 6000, "1.2M",
            new FloppyParameters[]{FP_h1200, FP_h360, FP_h720});

    /**
     * 3.5" DD
     */
    public static final FloppyDriveParameters FDP_720K =
        new FloppyDriveParameters(3, 250, 16, 16, 3000, "720K",
            new FloppyParameters[]{FP_D720});

    /**
     * 3.5" HD
     */
    public static final FloppyDriveParameters FDP_1440K =
        new FloppyDriveParameters(4, 500, 16, 16, 4000, "1.44M",
            new FloppyParameters[]{FP_H1440, FP_D720});

    /**
     * 3.5" ED
     */
    public static final FloppyDriveParameters FDP_2880K_AMI_BIOS =
        new FloppyDriveParameters(5, 1000, 15, 8, 3000, "2.88M AMI BIOS",
            new FloppyParameters[]{FP_H1440, FP_E2880});

    /**
     * 3.5" ED
     */
    public static final FloppyDriveParameters FDP_2880K =
        new FloppyDriveParameters(6, 1000, 15, 8, 3000, "2.88M",
            new FloppyParameters[]{FP_H1440, FP_E2880});

    /**
     * All known floppy drive parameters
     */
    public static final FloppyDriveParameters[] DRIVE_PARAMS = {
        FDP_UNKNOWN, FDP_360K, FDP_1200K, FDP_720K, FDP_1440K, FDP_2880K_AMI_BIOS, FDP_2880K
    };

    /**
     * Timeout for seek command
     */
    public static final int SEEK_TIMEOUT = 5000;
    /**
     * Timeout for read/write commands
     */
    public static final int RW_TIMEOUT = 10000;
}
