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
 
package org.jnode.driver.net.bcm570x;

/**
 * Driver constants to the RTL8139 ethernet card. <p/> Build with help of the
 * donation from WebSprocket LLC
 * 
 * @author Martin Husted Hartvig
 */
public interface BCM570xConstants {

    public static final int MAX_MULTICAST_ADDR = 64;

    public static final int REALTEK_REG_SIZE = 256;

    public static final int REALTEK_REG_ALIGN = 256;

    // RTL8139B register offsets
    // NOTE: Not all are 32-bit wide
    public static final int REG_MAC0 = 0x00;
    public static final int REG_MAR0 = 0x08;
    public static final int REG_TX_STATUS0 = 0x10;
    public static final int REG_TX_ADDR0 = 0x20;
    public static final int REG_RX_BUF = 0x30;
    public static final int REG_RX_EARLY_CNT = 0x34; // 16-bit reg
    public static final int REG_RX_EARLY_STA = 0x36; // 8-bit reg
    public static final int REG_CHIPCMD = 0x37; // 8-bit reg
    public static final int REG_RX_BUF_PTR = 0x38; // 16-bit reg
    public static final int REG_RX_BUF_CNT = 0x3A; // 16-bit reg
    public static final int CAPR = 0x38; // 16-bit reg
    public static final int CBR = 0x3A; // 16-bit reg
    public static final int REG_INTR_MASK = 0x3C; // 16-bit reg
    public static final int REG_INTR_STATUS = 0x3E; // 16-bit reg
    public static final int REG_TX_CONFIG = 0x40;
    public static final int REG_CHIP_VERSION = 0x43;
    public static final int REG_RX_CONFIG = 0x44;
    public static final int REG_TIMER = 0x48;
    public static final int REG_RX_MISSED = 0x4C;
    public static final int REG_CFG9346 = 0x50; // 8-bit reg
    public static final int REG_CONFIG0 = 0x51; // 8-bit reg
    public static final int REG_CONFIG1 = 0x52; // 8-bit reg
    public static final int REG_FLASH = 0x54;
    public static final int MSR = 0x58; // 8-bit reg
    public static final int REG_CONFIG3 = 0x59; // 8-bit reg
    public static final int REG_MII_SMI = 0x5A; // 8-bit reg
    public static final int REG_CONFIG4 = 0x5a;
    public static final int REG_HLT_CLK = 0x5B; // 8-bit reg
    public static final int REG_MULTI_INTR = 0x5C;
    public static final int REG_TX_SUMMARY = 0x60; // 16-bit reg
    public static final int BMCR = 0x62; // 16-bit reg
    public static final int BMSR = 0x64; // 16-bit reg
    public static final int REG_NWAY_ADVT = 0x66; // 16-bit reg
    public static final int NWAY_LPAR = 0x68; // 16-bit reg
    public static final int REG_NWAY_EXPN = 0x6A;
    public static final int REG_FIFOTMS = 0x70;
    public static final int REG_CSCR = 0x74;
    public static final int REG_PARA78 = 0x78;
    public static final int REG_PARA7C = 0x7C;

    // Register bit definitions
    public static final int BMCR_RESET = 0x8000;
    public static final int CMD_RESET = 0x10;
    public static final int CMD_TX_ENABLE = 0x04;
    public static final int CMD_RX_ENABLE = 0x08;
    public static final int CMD_BUFFER_EMPTY = 0x01;
    public static final int TX_FIFO_THRESHOLD = 0x10;
    public static final int TX_DMA_BURST = 0x04;
    public static final int TX_IGT = 0x03;
    public static final int TX_CRC = 0x10000;
    public static final int TCR_CLRABT = 0x1;
    public static final int RX_FIFO_THRESHOLD = 0x4;
    public static final int RX_DMA_BURST = 0x4;
    public static final int RX_RBLEN = 2; // 32k ring buffer
    public static final int RX_WRAP = 0x80;
    public static final int RX_AER = 0x20; // Accept error packets
    public static final int RX_AR = 0x10;
    public static final int RX_BCAST = 0x8;
    public static final int RX_MCAST = 0x4;
    public static final int RX_MYPHYS = 0x2;
    public static final int RX_ALLPHYS = 0x1;
    public int txConfig = TX_DMA_BURST << 8 | TX_IGT << 24;
    public int txFlag = TX_FIFO_THRESHOLD << 16;

    // accept broadcasts and runt packets, wrap around the buffer
    public int rxConfig =
            (RX_DMA_BURST << 8) | (RX_FIFO_THRESHOLD << 13) | (RX_RBLEN << 11) | RX_AR | RX_BCAST |
                    RX_MYPHYS;
    public static final int INTR_RX_OK = 0x0001;
    public static final int INTR_RX_ERR = 0x0002;
    public static final int INTR_TX_OK = 0x0004;
    public static final int INTR_TX_ERR = 0x0008;
    public static final int INTR_RX_BUF_OVRFLO = 0x0010;
    public static final int INTR_LNKCHG = 0x0020;
    public static final int INTR_RX_UNDERRUN = 0x0020;
    public static final int INTR_RX_FIFO_OVRFLO = 0x0040;
    public static final int INTR_LEN_CHG = 0x2000;
    public static final int INTR_TIMEOUT = 0x4000;
    public static final int INTR_SYS_ERR = 0x8000;
    public static final int INTR_MASK =
            (INTR_SYS_ERR | INTR_TIMEOUT | INTR_LEN_CHG | INTR_RX_UNDERRUN | INTR_RX_BUF_OVRFLO |
                    INTR_RX_FIFO_OVRFLO | INTR_RX_ERR | INTR_RX_OK | INTR_TX_ERR | INTR_TX_OK);
    public static final int CFG1_MMIO = 0x08;
    public static final int CFG1_PIO = 0x04;
    public static final int CSCR_LINKCHANGE = 0x800;

    // 9346CR register definitions
    public static final int CFG9346_NORMAL = 0x0;
    public static final int CFG9346_AUTOLOAD = 0x40;
    public static final int CFG9346_PRG = 0x80;
    public static final int CFG9346_WE = 0xc0;

    // Misc definitions
    public static final int GENERIC_WAIT_TIME = 10; // micro secs
    public static final int REPEAT_TIMEOUT_COUNT = 1000;
    public static final int NUM_RX_DESCRIPTORS = 1; // check
    public static final int RX_BUF_IDX = 2; // 0 = 8192, 1 = 16384; 2 = 32768,
    // 3 = 65536;
    public static final int RX_BUF_SIZE = 8192 << RX_BUF_IDX;
    public static final int RX_BUF_WRAP_PAD = 2048;
    public static final int RX_BUF_EXTRA = 16; // extra room
    public static final int TOTAL_RX_BUF_SIZE = RX_BUF_SIZE + RX_BUF_EXTRA + RX_BUF_WRAP_PAD;
    public static final int RX_FRAMES = 32;
    public static final int MAX_ETH_FRAME_LEN = 1536;

    // Receive status
    public static final int RX_MAR = 0x8000; // Multicast address received
    public static final int RX_PAM = 0x4000; // Physical address matched
    public static final int RX_BAR = 0x2000; // Broadcast address received
    public static final int RX_ISE = 0x0020; // Invalid symbol error
                                                // (100B-TX)
    public static final int RX_RUNT = 0x0010; // runt packet
    public static final int RX_LONG = 0x0008; // long packet (>4k bytes)
    public static final int RX_CRC = 0x0004; // CRC error
    public static final int RX_FAE = 0x0002; // Frame alignment error
    public static final int RX_ROK = 0x0001; // rx ok

    // transmit status
    public static final int TX_CRS = 0x80000000; // Carrier sense lost
    public static final int TX_TABT = 0x40000000; // Transmit aborted
    public static final int TX_OWC = 0x20000000; // Out of window collision
    public static final int TX_CDH = 0x10000000; // CD heart beat
    public static final int TX_NCC = 0x0f000000; // Collision count mask
    public static final int TX_TOK = 0x00008000; // Transmit OK
    public static final int TX_TUN = 0x00004000; // Transmit FIFO underrun
    public static final int TX_OWN = 0x00002000; // Transmit is completed

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

    /**
     * Timeout for auto-negotiate in seconds
     */
    public static final int AUTO_NEGOTIATE_TIMEOUT = 15;
}
