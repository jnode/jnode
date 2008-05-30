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
 *         <p/>
 *         TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public interface EEPRO100Constants {
    final static int MAX_ETH_FRAME_LEN = 1536;
    /* The ring sizes should be a power of two for efficiency. */
    final static int TX_RING_SIZE = 32; /* Effectively 2 entries fewer. */
    final static int RX_RING_SIZE = 32;

    /* Actual number of TX packets queued, must be <= TX_RING_SIZE-2. */
    final static int TX_QUEUE_LIMIT = 12;
    final static int TX_QUEUE_UNFULL = 8;

    /* Time in jiffies before concluding the transmitter is hung. */
    final static int HZ = 1000;
    final static int TX_TIMEOUT = (2 * HZ);

    /* PHY media interface chips. */
    final static String phys[] = {"None", "i82553-A/B", "i82553-C", "i82503", "DP83840", "80c240", "80c24", "i82555",
        "Microlinear", "Level One", "DP83840A", "ICS 1890", "unknown-12", "unknown-13", "unknown-14", "unknown-15",};
    final static int NonSuchPhy = 0;
    final static int I82553AB = 1;
    final static int I82553C = 2;
    final static int I82503 = 3;
    final static int DP83840 = 4;
    final static int S80C240 = 5;
    final static int S80C24 = 6;
    final static int I82555 = 7;
    final static int DP83840A = 10;

    /*
     * The parameters for a CmdConfigure operation. There are so many options
     * that it would be difficult to document each bit. We mostly use the
     * default or recommended settings.
     */
    final static byte i82557ConfigCmd[] = {22, 0x08, 0, 0, 0, 0, 0x32, 0x03, 1, /*
                                                                     * 1=Use MII
                                                                     * 0=Use AUI
                                                                     */
        0, 0x2E, 0, 0x60, 0, (byte) 0xf2, 0x48, 0, 0x40, (byte) 0xf2, (byte) 0x80, /*
                                                                                * 0x40=Force
                                                                                * full-duplex
                                                                                */
        0x3f, 0x05};
    final static byte i82558ConfigCmd[] = {22, 0x08, 0, 1, 0, 0, (byte) 0x22, 0x03, 1, /*
                                                                            * 1=Use
                                                                            * MII
                                                                            * 0=Use
                                                                            * AUI
                                                                            */
        0, 0x2E, 0, 0x60, 0x8, (byte) 0x88, 0x68, 0, 0x40, (byte) 0xf2, (byte) 0x84, /*
                                                                                  * 0xBD->0xFD=Force
                                                                                  * full-duplex
                                                                                  */
        0x31, 0x05};

    /* EEPROM_Ctrl bits. */
    final static int EE_SHIFT_CLK = 0x01; /* EEPROM shift clock. */
    final static int EE_CS = 0x02; /* EEPROM chip select. */
    final static int EE_DATA_WRITE = 0x04; /* EEPROM chip data in. */
    final static int EE_DATA_READ = 0x08; /* EEPROM chip data out. */
    final static int EE_WRITE_0 = 0x4802;
    final static int EE_WRITE_1 = 0x4806;
    final static int EE_ENB = (0x4800 | EE_CS);
    final static int EE_READ_CMD = 6;

    final static int SCBStatus = 0;
    final static int SCBCmd = 2; /* Rx/Command Unit command and status. */
    final static int SCBPointer = 4; /* General purpose pointer. */
    final static int SCBPort = 8; /* Misc. commands and operands. */
    final static int SCBflash = 12;
    final static int SCBeeprom = 14; /* EEPROM and flash memory control. */
    final static int SCBCtrlMDI = 16; /* MDI interface control. */
    final static int SCBEarlyRx = 20; /* Early receive byte count. */

    /* Commands that can be put in a command list entry. */
    final static int CmdNOp = 0;
    final static int CmdIASetup = 0x10000;
    final static int CmdConfigure = 0x20000;
    final static int CmdMulticastList = 0x30000;
    final static int CmdTx = 0x40000;
    final static int CmdTDR = 0x50000;
    final static int CmdDump = 0x60000;
    final static int CmdDiagnose = 0x70000;
    final static int CmdSuspend = 0x40000000; /* Suspend after completion. */
    final static int CmdIntr = 0x20000000; /* Interrupt after completion. */
    final static int CmdTxFlex = 0x00080000; /*
                                              * Use "Flexible mode" for CmdTx
                                              * command.
                                              */

    /* Do atomically if possible. */
    final static int SCBMaskCmdDone = 0x8000;
    final static int SCBMaskRxDone = 0x4000;
    final static int SCBMaskCmdIdle = 0x2000;
    final static int SCBMaskRxSuspend = 0x1000;
    final static int SCBMaskEarlyRx = 0x0800;
    final static int SCBMaskFlowCtl = 0x0400;
    final static int SCBTriggerIntr = 0x0200;
    final static int SCBMaskAll = 0x0100;

    /* The rest are Rx and Tx commands. */
    final static int CUStart = 0x0010;
    final static int CUResume = 0x0020;
    final static int CUHiPriStart = 0x0030;
    final static int CUStatsAddr = 0x0040;
    final static int CUShowStats = 0x0050;
    final static int CUCmdBase = 0x0060; /* CU Base address (set to zero) . */
    final static int CUDumpStats = 0x0070; /* Dump then reset stats counters. */
    final static int CUHiPriResume = 0x00b0; /*
                                              * Resume for the high priority Tx
                                              * queue.
                                              */

    final static int RxStart = 0x0001;
    final static int RxResume = 0x0002;
    final static int RxAbort = 0x0004;
    final static int RxAddrLoad = 0x0006;
    final static int RxResumeNoResources = 0x0007;
    final static int IntrCmdDone = 0x8000;
    final static int IntrRxDone = 0x4000;
    final static int IntrCmdIdle = 0x2000;
    final static int IntrRxSuspend = 0x1000;
    final static int IntrMIIDone = 0x0800;
    final static int IntrDrvrIntr = 0x0400;
    final static int IntrAllNormal = 0xfc00;
    final static int PortReset = 0;
    final static int PortSelfTest = 1;
    final static int PortPartialReset = 2;
    final static int PortDump = 3;

    final static int RxComplete = 0x8000;
    final static int RxOK = 0x2000;
    final static int RxErrCRC = 0x0800;
    final static int RxErrAlign = 0x0400;
    final static int RxErrTooBig = 0x0200;
    final static int RxErrSymbol = 0x0010;
    final static int RxEth2Type = 0x0020;
    final static int RxNoMatch = 0x0004;
    final static int RxNoIAMatch = 0x0002;
    final static int TxUnderrun = 0x1000;
    final static int StatusComplete = 0x8000;
}
