/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.driver.net.via_rhine;

/**
 * @author Levente S\u00e1ntha
 */
interface ViaRhineConstants {

    int byPAR0 = 0;
    int byRCR = 6;
    int byTCR = 7;
    int byCR0 = 8;
    int byCR1 = 9;
    int byISR0 = 0x0c;
    int byISR1 = 0x0d;
    int byIMR0 = 0x0e;
    int byIMR1 = 0x0f;
    int byMAR0 = 0x10;
    int byMAR1 = 0x11;
    int byMAR2 = 0x12;
    int byMAR3 = 0x13;
    int byMAR4 = 0x14;
    int byMAR5 = 0x15;
    int byMAR6 = 0x16;
    int byMAR7 = 0x17;
    int dwCurrentRxDescAddr = 0x18;
    int dwCurrentTxDescAddr = 0x1c;
    int dwCurrentRDSE0 = 0x20;
    int dwCurrentRDSE1 = 0x24;
    int dwCurrentRDSE2 = 0x28;
    int dwCurrentRDSE3 = 0x2c;
    int dwNextRDSE0 = 0x30;
    int dwNextRDSE1 = 0x34;
    int dwNextRDSE2 = 0x38;
    int dwNextRDSE3 = 0x3c;
    int dwCurrentTDSE0 = 0x40;
    int dwCurrentTDSE1 = 0x44;
    int dwCurrentTDSE2 = 0x48;
    int dwCurrentTDSE3 = 0x4c;
    int dwNextTDSE0 = 0x50;
    int dwNextTDSE1 = 0x54;
    int dwNextTDSE2 = 0x58;
    int dwNextTDSE3 = 0x5c;
    int dwCurrRxDMAPtr = 0x60;
    int dwCurrTxDMAPtr = 0x64;
    int byMPHY = 0x6c;
    int byMIISR = 0x6d;
    int byBCR0 = 0x6e;
    int byBCR1 = 0x6f;
    int byMIICR = 0x70;
    int byMIIAD = 0x71;
    int wMIIDATA = 0x72;
    int byEECSR = 0x74;
    int byTEST = 0x75;
    int byGPIO = 0x76;
    int byCFGA = 0x78;
    int byCFGB = 0x79;
    int byCFGC = 0x7a;
    int byCFGD = 0x7b;
    int wTallyCntMPA = 0x7c;
    int wTallyCntCRC = 0x7d;
    int bySTICKHW = 0x83;
    int byWOLcrClr = 0xA4;
    int byWOLcgClr = 0xA7;
    int byPwrcsrClr = 0xAC;

/*---------------------  Exioaddr Definitions -------------------------*/

    /*
     * Bits in the RCR register
     */
    int RCR_RRFT2 = 0x80;
    int RCR_RRFT1 = 0x40;
    int RCR_RRFT0 = 0x20;
    int RCR_PROM = 0x10;
    int RCR_AB = 0x08;
    int RCR_AM = 0x04;
    int RCR_AR = 0x02;
    int RCR_SEP = 0x01;

    /*
     * Bits in the TCR register
     */
    int TCR_RTSF = 0x80;
    int TCR_RTFT1 = 0x40;
    int TCR_RTFT0 = 0x20;
    int TCR_OFSET = 0x08;
    int TCR_LB1 = 0x04;    /* loopback[1] */
    int TCR_LB0 = 0x02;    /* loopback[0] */

    /*
     * Bits in the CR0 register
     */
    int CR0_RDMD = 0x40;    /* rx descriptor polling demand */
    int CR0_TDMD = 0x20;    /* tx descriptor polling demand */
    int CR0_TXON = 0x10;
    int CR0_RXON = 0x08;
    int CR0_STOP = 0x04;    /* stop NIC, default = 1 */
    int CR0_STRT = 0x02;    /* start NIC */
    int CR0_INIT = 0x01;    /* start init process */

    /*
    * Bits in the CR1 register
    */
    int CR1_SFRST = 0x80    /* software reset */;
    int CR1_RDMD1 = 0x40    /* RDMD1 */;
    int CR1_TDMD1 = 0x20    /* TDMD1 */;
    int CR1_KEYPAG = 0x10    /* turn on par/key */;
    int CR1_DPOLL = 0x08    /* disable rx/tx auto polling */;
    int CR1_FDX = 0x04    /* full duplex mode */;
    int CR1_ETEN = 0x02    /* early tx mode */;
    int CR1_EREN = 0x01    /* early rx mode */;

    /*
     * Bits in the CR register
     */
    int CR_RDMD = 0x0040    /* rx descriptor polling demand */;
    int CR_TDMD = 0x0020    /* tx descriptor polling demand */;
    int CR_TXON = 0x0010;
    int CR_RXON = 0x0008;
    int CR_STOP = 0x0004    /* stop NIC, default = 1 */;
    int CR_STRT = 0x0002    /* start NIC */;
    int CR_INIT = 0x0001    /* start init process */;
    int CR_SFRST = 0x8000    /* software reset */;
    int CR_RDMD1 = 0x4000    /* RDMD1 */;
    int CR_TDMD1 = 0x2000    /* TDMD1 */;
    int CR_KEYPAG = 0x1000    /* turn on par/key */;
    int CR_DPOLL = 0x0800    /* disable rx/tx auto polling */;
    int CR_FDX = 0x0400    /* full duplex mode */;
    int CR_ETEN = 0x0200    /* early tx mode */;
    int CR_EREN = 0x0100    /* early rx mode */;

    /*
     * Bits in the IMR0 register
     */
    int IMR0_CNTM = 0x80;
    int IMR0_BEM = 0x40;
    int IMR0_RUM = 0x20;
    int IMR0_TUM = 0x10;
    int IMR0_TXEM = 0x08;
    int IMR0_RXEM = 0x04;
    int IMR0_PTXM = 0x02;
    int IMR0_PRXM = 0x01;

    /* define imrshadow */
    int IMRShadow = 0x5AFF;

    /*
     * Bits in the IMR1 register
     */
    int IMR1_INITM = 0x80;
    int IMR1_SRCM = 0x40;
    int IMR1_NBFM = 0x10;
    int IMR1_PRAIM = 0x08;
    int IMR1_RES0M = 0x04;
    int IMR1_ETM = 0x02;
    int IMR1_ERM = 0x01;

    /*
     * Bits in the ISR register
     */
    int ISR_INITI = 0x8000;
    int ISR_SRCI = 0x4000;
    int ISR_ABTI = 0x2000;
    int ISR_NORBF = 0x1000;
    int ISR_PKTRA = 0x0800;
    int ISR_RES0 = 0x0400;
    int ISR_ETI = 0x0200;
    int ISR_ERI = 0x0100;
    int ISR_CNT = 0x0080;
    int ISR_BE = 0x0040;
    int ISR_RU = 0x0020;
    int ISR_TU = 0x0010;
    int ISR_TXE = 0x0008;
    int ISR_RXE = 0x0004;
    int ISR_PTX = 0x0002;
    int ISR_PRX = 0x0001;

    /*
     * Bits in the ISR0 register;
     */
    int ISR0_CNT = 0x80;
    int ISR0_BE = 0x40;
    int ISR0_RU = 0x20;
    int ISR0_TU = 0x10;
    int ISR0_TXE = 0x08;
    int ISR0_RXE = 0x04;
    int ISR0_PTX = 0x02;
    int ISR0_PRX = 0x01;

    /*
     * Bits in the ISR1 register
     */
    int ISR1_INITI = 0x80;
    int ISR1_SRCI = 0x40;
    int ISR1_NORBF = 0x10;
    int ISR1_PKTRA = 0x08;
    int ISR1_ETI = 0x02;
    int ISR1_ERI = 0x01;

    /* ISR ABNORMAL CONDITION */
    int ISR_ABNORMAL = ISR_BE + ISR_RU + ISR_TU + ISR_CNT + ISR_NORBF + ISR_PKTRA;

    /*
     * Bits in the MIISR register;
     */
    int MIISR_MIIERR = 0x08;
    int MIISR_MRERR = 0x04;
    int MIISR_LNKFL = 0x02;
    int MIISR_SPEED = 0x01;

    /*
     * Bits in the MIICR register;
     */
    int MIICR_MAUTO = 0x80;
    int MIICR_RCMD = 0x40;
    int MIICR_WCMD = 0x20;
    int MIICR_MDPM = 0x10;
    int MIICR_MOUT = 0x08;
    int MIICR_MDO = 0x04;
    int MIICR_MDI = 0x02;
    int MIICR_MDC = 0x01;

    /*
     * Bits in the EECSR register;
     */
    int EECSR_EEPR = 0x80    /* eeprom programed status, 73h means programed */;
    int EECSR_EMBP = 0x40    /* eeprom embeded programming */;
    int EECSR_AUTOLD = 0x20    /* eeprom content reload */;
    int EECSR_DPM = 0x10    /* eeprom direct programming */;
    int EECSR_CS = 0x08    /* eeprom CS pin */;
    int EECSR_SK = 0x04    /* eeprom SK pin */;
    int EECSR_DI = 0x02    /* eeprom DI pin */;
    int EECSR_DO = 0x01    /* eeprom DO pin */;

/*
 * Bits in the BCR0 register;
 */

    int BCR0_CRFT2 = 0x20;
    int BCR0_CRFT1 = 0x10;
    int BCR0_CRFT0 = 0x08;
    int BCR0_DMAL2 = 0x04;
    int BCR0_DMAL1 = 0x02;
    int BCR0_DMAL0 = 0x01;

    /*
     * Bits in the BCR1 register;
     */
    int BCR1_CTSF = 0x20;
    int BCR1_CTFT1 = 0x10;
    int BCR1_CTFT0 = 0x08;
    int BCR1_POT2 = 0x04;
    int BCR1_POT1 = 0x02;
    int BCR1_POT0 = 0x01;

    /*
     * Bits in the CFGA register;
     */
    int CFGA_EELOAD = 0x80    /* enable eeprom embeded and direct programming */;
    int CFGA_JUMPER = 0x40;
    int CFGA_MTGPIO = 0x08;
    int CFGA_T10EN = 0x02;
    int CFGA_AUTO = 0x01;

    /*
     * Bits in the CFGB register;
     */
    int CFGB_PD = 0x80;
    int CFGB_POLEN = 0x02;
    int CFGB_LNKEN = 0x01;

    /*
     * Bits in the CFGC register;
     */
    int CFGC_M10TIO = 0x80;
    int CFGC_M10POL = 0x40;
    int CFGC_PHY1 = 0x20;
    int CFGC_PHY0 = 0x10;
    int CFGC_BTSEL = 0x08;
    int CFGC_BPS2 = 0x04    /* bootrom select[2] */;
    int CFGC_BPS1 = 0x02    /* bootrom select[1] */;
    int CFGC_BPS0 = 0x01    /* bootrom select[0] */;

    /*
     * Bits in the CFGD register;
     */
    int CFGD_GPIOEN = 0x80;
    int CFGD_DIAG = 0x40;
    int CFGD_MAGIC = 0x10;
    int CFGD_RANDOM = 0x08;
    int CFGD_CFDX = 0x04;
    int CFGD_CEREN = 0x02;
    int CFGD_CETEN = 0x01;

    /* Bits in RSR */
    int RSR_RERR = 0x00000001;
    int RSR_CRC = 0x00000002;
    int RSR_FAE = 0x00000004;
    int RSR_FOV = 0x00000008;
    int RSR_LONG = 0x00000010;
    int RSR_RUNT = 0x00000020;
    int RSR_SERR = 0x00000040;
    int RSR_BUFF = 0x00000080;
    int RSR_EDP = 0x00000100;
    int RSR_STP = 0x00000200;
    int RSR_CHN = 0x00000400;
    int RSR_PHY = 0x00000800;
    int RSR_BAR = 0x00001000;
    int RSR_MAR = 0x00002000;
    int RSR_RXOK = 0x00008000;
    int RSR_ABNORMAL = RSR_RERR + RSR_LONG + RSR_RUNT;

    /* Bits in TSR */
    int TSR_NCR0 = 0x00000001;
    int TSR_NCR1 = 0x00000002;
    int TSR_NCR2 = 0x00000004;
    int TSR_NCR3 = 0x00000008;
    int TSR_COLS = 0x00000010;
    int TSR_CDH = 0x00000080;
    int TSR_ABT = 0x00000100;
    int TSR_OWC = 0x00000200;
    int TSR_CRS = 0x00000400;
    int TSR_UDF = 0x00000800;
    int TSR_TBUFF = 0x00001000;
    int TSR_SERR = 0x00002000;
    int TSR_JAB = 0x00004000;
    int TSR_TERR = 0x00008000;
    int TSR_ABNORMAL = TSR_TERR + TSR_OWC + TSR_ABT + TSR_JAB + TSR_CRS;
    int TSR_OWN_BIT = 0x80000000;

    int CB_DELAY_LOOP_WAIT = 10;    /* 10ms */

    /* enabled mask value of irq */
    int W_IMR_MASK_VALUE = 0x1BFF;    /* initial value of IMR */

    /* Ethernet address filter type */
    int PKT_TYPE_DIRECTED = 0x0001;    /* obsolete, directed address is always accepted */
    int PKT_TYPE_MULTICAST = 0x0002;
    int PKT_TYPE_ALL_MULTICAST = 0x0004;
    int PKT_TYPE_BROADCAST = 0x0008;
    int PKT_TYPE_PROMISCUOUS = 0x0020;
    int PKT_TYPE_LONG = 0x2000;
    int PKT_TYPE_RUNT = 0x4000;
    int PKT_TYPE_ERROR = 0x8000;    /* accept error packets, e.g. CRC error */

    /* Loopback mode */
    int NIC_LB_NONE = 0x00;
    int NIC_LB_INTERNAL = 0x01;
    int NIC_LB_PHY = 0x02;    /* MII or Internal-10BaseT loopback */

    int TX_RING_SIZE = 16;
    int RX_RING_SIZE = 16;
    int PKT_BUF_SZ = 1536;    /* Size of each temporary Rx buffer. */

    int PCI_REG_MODE3 = 0x53;
    int MODE3_MIION = 0x04;    /* in PCI_REG_MOD3 OF PCI space */

    /* Offsets to the device registers. */
    int StationAddr = 0x00;
    int RxConfig = 0x06;
    int TxConfig = 0x07;
    int ChipCmd = 0x08;
    int IntrStatus = 0x0C;
    int IntrEnable = 0x0E;
    int MulticastFilter0 = 0x10;
    int MulticastFilter1 = 0x14;
    int RxRingPtr = 0x18;
    int TxRingPtr = 0x1C;
    int GFIFOTest = 0x54;
    int MIIPhyAddr = 0x6C;
    int MIIStatus = 0x6D;
    int PCIBusConfig = 0x6E;
    int MIICmd = 0x70;
    int MIIRegAddr = 0x71;
    int MIIData = 0x72;
    int MACRegEEcsr = 0x74;
    int ConfigA = 0x78;
    int ConfigB = 0x79;
    int ConfigC = 0x7A;
    int ConfigD = 0x7B;
    int RxMissed = 0x7C;
    int RxCRCErrs = 0x7E;
    int MiscCmd = 0x81;
    int StickyHW = 0x83;
    int IntrStatus2 = 0x84;
    int WOLcrClr = 0xA4;
    int WOLcgClr = 0xA7;
    int PwrcsrClr = 0xAC;

    /* Bits in the interrupt status/mask registers. */
    int IntrRxDone = 0x0001;
    int IntrRxErr = 0x0004;
    int IntrRxEmpty = 0x0020;
    int IntrTxDone = 0x0002;
    int IntrTxError = 0x0008;
    int IntrTxUnderrun = 0x0210;
    int IntrPCIErr = 0x0040;
    int IntrStatsMax = 0x0080;
    int IntrRxEarly = 0x0100;
    int IntrRxOverflow = 0x0400;
    int IntrRxDropped = 0x0800;
    int IntrRxNoBuf = 0x1000;
    int IntrTxAborted = 0x2000;
    int IntrLinkChange = 0x4000;
    int IntrRxWakeUp = 0x8000;
    int IntrNormalSummary = 0x0003;
    int IntrAbnormalSummary = 0xC260;
    int IntrTxDescRace = 0x080000;        // mapped from IntrStatus2
    int IntrTxErrSummary = 0x082218;


    int DEFAULT_INTR = IntrRxDone | IntrRxErr | IntrRxEmpty |
        IntrRxOverflow | IntrRxDropped | IntrRxNoBuf;

    //enum rhine_revs
    int VT86C100A = 0x00;
    int VTunknown0 = 0x20;
    int VT6102 = 0x40;
    int VT8231 = 0x50; /* Integrated MAC */
    int VT8233 = 0x60; /* Integrated MAC */
    int VT8235 = 0x74; /* Integrated MAC */
    int VT8237 = 0x78; /* Integrated MAC */
    int VTunknown1 = 0x7C;
    int VT6105 = 0x80;
    int VT6105_B0 = 0x83;
    int VT6105L = 0x8A;
    int VT6107 = 0x8C;
    int VTunknown2 = 0x8E;
    int VT6105M = 0x90;
}
