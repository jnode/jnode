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

package org.jnode.driver.net.eepro100;

/**
 * @author flesire
 */
public interface EEPRO100Constants {
    static final int MAX_ETH_FRAME_LEN = 1536;
    /* The ring sizes should be a power of two for efficiency. */
    static final int TX_RING_SIZE = 32; /* Effectively 2 entries fewer. */
    static final int RX_RING_SIZE = 32;

    /* Actual number of TX packets queued, must be <= TX_RING_SIZE-2. */
    static final int TX_QUEUE_LIMIT = 12;
    static final int TX_QUEUE_UNFULL = 8;

    /* Time in jiffies before concluding the transmitter is hung. */
    static final int HZ = 1000;
    static final int TX_TIMEOUT = (2 * HZ);

    /* PHY media interface chips. */
    static final String phys[] = {
        "None", "i82553-A/B", "i82553-C", "i82503", "DP83840", "80c240", "80c24", "i82555",
        "Microlinear", "Level One", "DP83840A", "ICS 1890", "unknown-12", "unknown-13",
        "unknown-14", "unknown-15"
    };
    static final int NonSuchPhy = 0;
    static final int I82553AB = 1;
    static final int I82553C = 2;
    static final int I82503 = 3;
    static final int DP83840 = 4;
    static final int S80C240 = 5;
    static final int S80C24 = 6;
    static final int I82555 = 7;
    static final int DP83840A = 10;

    /*
     * The parameters for a CmdConfigure operation. There are so many options
     * that it would be difficult to document each bit. We mostly use the
     * default or recommended settings.
     */
    static final byte i82557ConfigCmd[] = {
        22, 0x08, 0, 0, 0, 0, 0x32, 0x03, 1, /* 1=Use MII 0=Use AUI */
        0, 0x2E, 0, 0x60, 0, (byte) 0xf2, 0x48, 0, 0x40, (byte) 0xf2, 
        (byte) 0x80, /* 0x40=Force full-duplex */ 
        0x3f, 0x05};
    static final byte i82558ConfigCmd[] = {
        22, 0x08, 0, 1, 0, 0, (byte) 0x22, 0x03, 1, /* 1=Use MII 0=Use AUI */
        0, 0x2E, 0, 0x60, 0x8, (byte) 0x88, 0x68, 0, 0x40, (byte) 0xf2, 
        (byte) 0x84, /* 0xBD->0xFD=Force full-duplex */
        0x31, 0x05};

    /* EEPROM_Ctrl bits. */
    static final int EE_SHIFT_CLK = 0x01; /* EEPROM shift clock. */
    static final int EE_CS = 0x02; /* EEPROM chip select. */
    static final int EE_DATA_WRITE = 0x04; /* EEPROM chip data in. */
    static final int EE_DATA_READ = 0x08; /* EEPROM chip data out. */
    static final int EE_WRITE_0 = 0x4802;
    static final int EE_WRITE_1 = 0x4806;
    static final int EE_ENB = (0x4800 | EE_CS);
    static final int EE_READ_CMD = 6;

    static final int SCBStatus = 0;
    static final int SCBCmd = 2; /* Rx/Command Unit command and status. */
    static final int SCBPointer = 4; /* General purpose pointer. */
    static final int SCBPort = 8; /* Misc. commands and operands. */
    static final int SCBflash = 12;
    static final int SCBeeprom = 14; /* EEPROM and flash memory control. */
    static final int SCBCtrlMDI = 16; /* MDI interface control. */
    static final int SCBEarlyRx = 20; /* Early receive byte count. */

    /* Commands that can be put in a command list entry. */
    static final int CmdNOp = 0;
    static final int CmdIASetup = 0x10000;
    static final int CmdConfigure = 0x20000;
    static final int CmdMulticastList = 0x30000;
    static final int CmdTx = 0x40000;
    static final int CmdTDR = 0x50000;
    static final int CmdDump = 0x60000;
    static final int CmdDiagnose = 0x70000;
    static final int CmdSuspend = 0x40000000; /* Suspend after completion. */
    static final int CmdIntr = 0x20000000; /* Interrupt after completion. */
    static final int CmdTxFlex = 0x00080000; /* Use "Flexible mode" for CmdTx command. */

    /* Do atomically if possible. */
    static final int SCBMaskCmdDone = 0x8000;
    static final int SCBMaskRxDone = 0x4000;
    static final int SCBMaskCmdIdle = 0x2000;
    static final int SCBMaskRxSuspend = 0x1000;
    static final int SCBMaskEarlyRx = 0x0800;
    static final int SCBMaskFlowCtl = 0x0400;
    static final int SCBTriggerIntr = 0x0200;
    static final int SCBMaskAll = 0x0100;

    /* The rest are Rx and Tx commands. */
    static final int CUStart = 0x0010;
    static final int CUResume = 0x0020;
    static final int CUHiPriStart = 0x0030;
    static final int CUStatsAddr = 0x0040;
    static final int CUShowStats = 0x0050;
    static final int CUCmdBase = 0x0060; /* CU Base address (set to zero) . */
    static final int CUDumpStats = 0x0070; /* Dump then reset stats counters. */
    static final int CUHiPriResume = 0x00b0; /* Resume for the high priority Tx queue.,*/

    static final int RxStart = 0x0001;
    static final int RxResume = 0x0002;
    static final int RxAbort = 0x0004;
    static final int RxAddrLoad = 0x0006;
    static final int RxResumeNoResources = 0x0007;
    static final int IntrCmdDone = 0x8000;
    static final int IntrRxDone = 0x4000;
    static final int IntrCmdIdle = 0x2000;
    static final int IntrRxSuspend = 0x1000;
    static final int IntrMIIDone = 0x0800;
    static final int IntrDrvrIntr = 0x0400;
    static final int IntrAllNormal = 0xfc00;
    static final int PortReset = 0;
    static final int PortSelfTest = 1;
    static final int PortPartialReset = 2;
    static final int PortDump = 3;

    static final int RxComplete = 0x8000;
    static final int RxOK = 0x2000;
    static final int RxErrCRC = 0x0800;
    static final int RxErrAlign = 0x0400;
    static final int RxErrTooBig = 0x0200;
    static final int RxErrSymbol = 0x0010;
    static final int RxEth2Type = 0x0020;
    static final int RxNoMatch = 0x0004;
    static final int RxNoIAMatch = 0x0002;
    static final int TxUnderrun = 0x1000;
    static final int StatusComplete = 0x8000;
}
