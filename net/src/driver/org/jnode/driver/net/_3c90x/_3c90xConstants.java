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
 
package org.jnode.driver.net._3c90x;

/**
 * @author epr
 */
public interface _3c90xConstants {

    /* Register definitions for the 3c905 ***/
    public static final int regPowerMgmtCtrl_w = 0x7c;
    /**
     * 905B Revision Only                 *
     */
    public static final int regUpMaxBurst_w = 0x7a;
    /**
     * 905B Revision Only                 *
     */
    public static final int regDnMaxBurst_w = 0x78;
    /**
     * 905B Revision Only                 *
     */
    public static final int regDebugControl_w = 0x74;
    /**
     * 905B Revision Only                 *
     */
    public static final int regDebugData_l = 0x70;
    /**
     * 905B Revision Only                 *
     */
    public static final int regRealTimeCnt_l = 0x40;
    /**
     * Universal                          *
     */
    public static final int regUpBurstThresh_b = 0x3e;
    /**
     * 905B Revision Only                 *
     */
    public static final int regUpPoll_b = 0x3d;
    /**
     * 905B Revision Only                 *
     */
    public static final int regUpPriorityThresh_b = 0x3c;
    /**
     * 905B Revision Only                 *
     */
    public static final int regUpListPtr_l = 0x38;
    /**
     * Universal                          *
     */
    public static final int regCountdown_w = 0x36;
    /**
     * Universal                          *
     */
    public static final int regFreeTimer_w = 0x34;
    /**
     * Universal                          *
     */
    public static final int regUpPktStatus_l = 0x30;
    /**
     * Universal with Exception; pg 130   *
     */
    public static final int regTxFreeThresh_b = 0x2f;
    /**
     * 90X Revision Only                  *
     */
    public static final int regDnPoll_b = 0x2d;
    /**
     * 905B Revision Only                 *
     */
    public static final int regDnPriorityThresh_b = 0x2c;
    /**
     * 905B Revision Only                 *
     */
    public static final int regDnBurstThresh_b = 0x2a;
    /**
     * 905B Revision Only                 *
     */
    public static final int regDnListPtr_l = 0x24;
    /**
     * Universal with Exception; pg 107   *
     */
    public static final int regDmaCtrl_l = 0x20;
    /** Universal with Exception; pg 106   **/
    /**                                    **/
    public static final int regIntStatusAuto_w = 0x1e;
    /**
     * 905B Revision Only                 *
     */
    public static final int regTxStatus_b = 0x1b;
    /**
     * Universal with Exception; pg 113   *
     */
    public static final int regTimer_b = 0x1a;
    /**
     * Universal                          *
     */
    public static final int regTxPktId_b = 0x18;
    /**
     * 905B Revision Only                 *
     */
    public static final int regCommandIntStatus_w = 0x0e;
    /** Universal (Command Variations)     **/

    /**
     * following are windowed registers *
     */
    // Registers7
    public static final int regPowerMgmtEvent_7_w = 0x0c;
    /**
     * 905B Revision Only                 *
     */
    public static final int regVlanEtherType_7_w = 0x04;
    /**
     * 905B Revision Only                 *
     */
    public static final int regVlanMask_7_w = 0x00;
    /**
     * 905B Revision Only                 *
     */

    // Registers6
    public static final int regBytesXmittedOk_6_w = 0x0c;
    /**
     * Universal                          *
     */
    public static final int regBytesRcvdOk_6_w = 0x0a;
    /**
     * Universal                          *
     */
    public static final int regUpperFramesOk_6_b = 0x09;
    /**
     * Universal                          *
     */
    public static final int regFramesDeferred_6_b = 0x08;
    /**
     * Universal                          *
     */
    public static final int regFramesRecdOk_6_b = 0x07;
    /**
     * Universal with Exceptions; pg 142  *
     */
    public static final int regFramesXmittedOk_6_b = 0x06;
    /**
     * Universal                          *
     */
    public static final int regRxOverruns_6_b = 0x05;
    /**
     * Universal                          *
     */
    public static final int regLateCollisions_6_b = 0x04;
    /**
     * Universal                          *
     */
    public static final int regSingleCollisions_6_b = 0x03;
    /**
     * Universal                          *
     */
    public static final int regMultipleCollisions_6_b = 0x02;
    /**
     * Universal                          *
     */
    public static final int regSqeErrors_6_b = 0x01;
    /**
     * Universal                          *
     */
    public static final int regCarrierLost_6_b = 0x00;
    /**
     * Universal                          *
     */

    // Registers5
    public static final int regIndicationEnable_5_w = 0x0c;
    /**
     * Universal                          *
     */
    public static final int regInterruptEnable_5_w = 0x0a;
    /**
     * Universal                          *
     */
    public static final int regTxReclaimThresh_5_b = 0x09;
    /**
     * 905B Revision Only                 *
     */
    public static final int regRxFilter_5_b = 0x08;
    /**
     * Universal                          *
     */
    public static final int regRxEarlyThresh_5_w = 0x06;
    /**
     * Universal                          *
     */
    public static final int regTxStartThresh_5_w = 0x00;
    /**
     * Universal                          *
     */

    // Registers4
    public static final int regUpperBytesOk_4_b = 0x0d;
    /**
     * Universal                          *
     */
    public static final int regBadSSD_4_b = 0x0c;
    /**
     * Universal                          *
     */
    public static final int regMediaStatus_4_w = 0x0a;
    /**
     * Universal with Exceptions; pg 201  *
     */
    public static final int regPhysicalMgmt_4_w = 0x08;
    /**
     * Universal                          *
     */
    public static final int regNetworkDiagnostic_4_w = 0x06;
    /**
     * Universal with Exceptions; pg 203  *
     */
    public static final int regFifoDiagnostic_4_w = 0x04;
    /**
     * Universal with Exceptions; pg 196  *
     */
    public static final int regVcoDiagnostic_4_w = 0x02;
    /**
     * Undocumented?                      *
     */

    // Registers3
    public static final int regTxFree_3_w = 0x0c;
    /**
     * Universal                          *
     */
    public static final int regRxFree_3_w = 0x0a;
    /**
     * Universal with Exceptions; pg 125  *
     */
    public static final int regResetMediaOptions_3_w = 0x08;
    /** Media Options on B Revision;       **/
    /**
     * Reset Options on Non-B Revision    *
     */
    public static final int regMacControl_3_w = 0x06;
    /**
     * Universal with Exceptions; pg 199  *
     */
    public static final int regMaxPktSize_3_w = 0x04;
    /**
     * 905B Revision Only                 *
     */
    public static final int regInternalConfig_3_l = 0x00;
    /** Universal; different bit           **/
    /**
     * definitions; pg 59                 *
     */

    // Registers2
    public static final int regResetOptions_2_w = 0x0c;
    /**
     * 905B Revision Only                 *
     */
    public static final int regStationMask_2_3w = 0x06;
    /**
     * Universal with Exceptions; pg 127  *
     */
    public static final int regStationAddress_2_3w = 0x00;
    /**
     * Universal with Exceptions; pg 127  *
     */

    // Registers1
    public static final int regRxStatus_1_w = 0x0a;
    /**
     * 90X Revision Only; Pg 126          *
     */

    // Registers0
    public static final int regEepromData_0_w = 0x0c;
    /**
     * Universal                          *
     */
    public static final int regEepromCommand_0_w = 0x0a;
    /**
     * Universal                          *
     */
    public static final int regBiosRomData_0_b = 0x08;
    /**
     * 905B Revision Only                 *
     */
    public static final int regBiosRomAddr_0_l = 0x04;
    /**
     * 905B Revision Only                 *
     */

    /* The names for the eight register windows ***/
    public static final int winPowerVlan7 = 0x07;
    public static final int winStatistics6 = 0x06;
    public static final int winTxRxControl5 = 0x05;
    public static final int winDiagnostics4 = 0x04;
    public static final int winTxRxOptions3 = 0x03;
    public static final int winAddressing2 = 0x02;
    public static final int winUnused1 = 0x01;
    public static final int winEepromBios0 = 0x00;

    /* Command definitions for the 3c90X ***/
    public static final int cmdGlobalReset = 0x00;
    /**
     * Universal with Exceptions; pg 151 *
     */
    public static final int cmdSelectRegisterWindow = 0x01;
    /**
     * Universal                         *
     */
    public static final int cmdEnableDcConverter = 0x02;
    /**                                   **/
    public static final int cmdRxDisable = 0x03;
    /**                                   **/
    public static final int cmdRxEnable = 0x04;
    /**
     * Universal                         *
     */
    public static final int cmdRxReset = 0x05;
    /**
     * Universal                         *
     */
    public static final int cmdStallCtl = 0x06;
    /**
     * Universal                         *
     */
    public static final int cmdTxEnable = 0x09;
    /**
     * Universal                         *
     */
    public static final int cmdTxDisable = 0x0A;
    /**                                   **/
    public static final int cmdTxReset = 0x0B;
    /**
     * Universal                         *
     */
    public static final int cmdRequestInterrupt = 0x0C;
    /**                                   **/
    public static final int cmdAcknowledgeInterrupt = 0x0D;
    /**
     * Universal                         *
     */
    public static final int cmdSetInterruptEnable = 0x0E;
    /**
     * Universal                         *
     */
    public static final int cmdSetIndicationEnable = 0x0F;
    /**
     * Universal                         *
     */
    public static final int cmdSetRxFilter = 0x10;
    /**
     * Universal                         *
     */
    public static final int cmdSetRxEarlyThresh = 0x11;
    /**                                   **/
    public static final int cmdSetTxStartThresh = 0x13;
    /**                                   **/
    public static final int cmdStatisticsEnable = 0x15;
    /**                                   **/
    public static final int cmdStatisticsDisable = 0x16;
    /**                                   **/
    public static final int cmdDisableDcConverter = 0x17;
    /**                                   **/
    public static final int cmdSetTxReclaimThresh = 0x18;
    /**                                   **/
    public static final int cmdSetHashFilterBit = 0x19;
    /**                                   **/

    /* Values for int status register bitmask **/
    public static final int INT_INTERRUPTLATCH = (1 << 0);
    public static final int INT_HOSTERROR = (1 << 1);
    public static final int INT_TXCOMPLETE = (1 << 2);
    public static final int INT_RXCOMPLETE = (1 << 4);
    public static final int INT_RXEARLY = (1 << 5);
    public static final int INT_INTREQUESTED = (1 << 6);
    public static final int INT_UPDATESTATS = (1 << 7);
    public static final int INT_LINKEVENT = (1 << 8);
    public static final int INT_DNCOMPLETE = (1 << 9);
    public static final int INT_UPCOMPLETE = (1 << 10);
    public static final int INT_CMDINPROGRESS = (1 << 12);
    public static final int INT_WINDOWNUMBER = (7 << 13);

    /* UpPktStatus bits */
    public static final int upPktLenMask = 0x1FFF;
    public static final int upError = (1 << 14);
    public static final int upComplete = (1 << 15);
    public static final int upOverrun = (1 << 16);
    public static final int runtFrame = (1 << 17);
    public static final int alignmentError = (1 << 18);
    public static final int crcError = (1 << 19);
    public static final int oversizedFrame = (1 << 20);
    public static final int dribbleBits = (1 << 23);
    public static final int upOverflow = (1 << 24);
    public static final int ipChecksumError = (1 << 25);
    public static final int tcpChecksumError = (1 << 26);
    public static final int udpChecksumError = (1 << 27);
    public static final int impliedBufferEnable = (1 << 28);
    public static final int ipChecksumChecked = (1 << 29);
    public static final int tcpChecksumChecked = (1 << 30);
    public static final int udpChecksumChecked = (1 << 31);

    /* Driver specific values */
    public static final int RX_FRAMES = 32;
    public static final int MAX_SERVICE = 32;
}
