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

package org.jnode.driver.net.ne2000;

/**
 * @author epr
 */
public interface Ne2000Constants {

    // Register Definitions

    public static final int NE_P0_CR = 0x00;
    public static final int NE_P0_PSTART = 0x01;
    public static final int NE_P0_PSTOP = 0x02;
    public static final int NE_P0_BOUND = 0x03;
    public static final int NE_P0_TSR = 0x04;
    public static final int NE_P0_TPSR = 0x04;
    public static final int NE_P0_TBCR0 = 0x05;
    public static final int NE_P0_TBCR1 = 0x06;
    public static final int NE_P0_ISR = 0x07;
    public static final int NE_P0_RSAR0 = 0x08;
    public static final int NE_P0_RSAR1 = 0x09;
    public static final int NE_P0_RBCR0 = 0x0A;
    public static final int NE_P0_RBCR1 = 0x0B;
    public static final int NE_P0_RSR = 0x0C;
    public static final int NE_P0_RCR = 0x0C;
    public static final int NE_P0_TCR = 0x0D;
    public static final int NE_P0_DCR = 0x0E;
    public static final int NE_P0_IMR = 0x0F;
    public static final int NE_P0_CNTR0 = 0x0D;
    public static final int NE_P0_CNTR1 = 0x0E;
    public static final int NE_P0_CNTR2 = 0x0F;

    public static final int NE_P1_CR = 0x00;
    public static final int NE_P1_PAR0 = 0x01;
    public static final int NE_P1_PAR1 = 0x02;
    public static final int NE_P1_PAR2 = 0x03;
    public static final int NE_P1_PAR3 = 0x04;
    public static final int NE_P1_PAR4 = 0x05;
    public static final int NE_P1_PAR5 = 0x06;
    public static final int NE_P1_CURR = 0x07;
    public static final int NE_P1_MAR0 = 0x08;

    public static final int NE_CR_PS0 = 0x00;        /* Page 0 select */
    public static final int NE_CR_PS1 = 0x40;        /* Page 1 select */
    public static final int NE_CR_PS2 = 0x80;        /* Page 2 select */
    public static final int NE_CR_PS_MASK = 0xC0;        /* Page mask */
    public static final int NE_CR_RD2 = 0x20;        /* Remote DMA control */
    public static final int NE_CR_RD1 = 0x10;
    public static final int NE_CR_RD0 = 0x08;
    public static final int NE_CR_TXP = 0x04;        /* transmit packet */
    public static final int NE_CR_STA = 0x02;        /* start */
    public static final int NE_CR_STP = 0x01;        /* stop */

    public static final int NE_CR_RREAD = NE_CR_RD0;
    public static final int NE_CR_RWRITE = NE_CR_RD1;
    public static final int NE_CR_SENDPACKET = NE_CR_RD0 | NE_CR_RD1;
    public static final int NE_CR_NODMA = NE_CR_RD2;

    public static final int NE_RCR_SEP = 0x01;        /* receive also with error */
    public static final int NE_RCR_AR = 0x02;        /* Accept length < 64 */
    public static final int NE_RCR_AB = 0x04;        /* Accept broadcast */
    public static final int NE_RCR_AM = 0x08;        /* Accept multicast */
    public static final int NE_RCR_PRO = 0x10;        /* Accept all (also not my address) */
    public static final int NE_RCR_MON = 0x20;        /* monitor mode */

    public static final int NE_RXCONFIG = NE_RCR_AR | NE_RCR_AB | NE_RCR_AM /*| NE_RCR_PRO*/;
    public static final int NE_RXOFF = NE_RCR_MON;
    public static final int NE_TXCONFIG = 0x00;
    public static final int NE_TXOFF = 0x02;    /* EN0_TXCR: Transmitter off */

    public static final int NE_DCR_FT1 = 0x40;
    public static final int NE_DCR_LS = 0x08;        /* Loopback select */
    public static final int NE_DCR_WTS = 0x01;        /* Word transfer select */

    public static final int NE_ISR_PRX = 0x01;        /* successful recv */
    public static final int NE_ISR_PTX = 0x02;        /* successful xmit */
    public static final int NE_ISR_RXE = 0x04;        /* receive error */
    public static final int NE_ISR_TXE = 0x08;        /* transmit error */
    public static final int NE_ISR_OVW = 0x10;        /* Overflow */
    public static final int NE_ISR_CNT = 0x20;        /* Counter overflow */
    public static final int NE_ISR_RDC = 0x40;        /* Remote DMA complete */
    public static final int NE_ISR_RST = 0x80;        /* reset */
    public static final int NE_ISRCONFIG = 0x3f;        /* Interrupts we will enable */

    public static final int NE_RSR_PRX = 0x01;        /* successful recv */
    public static final int NE_RSR_CRC = 0x02;        /* CRC error */
    public static final int NE_RSR_FAE = 0x04;        /* Frame alignment error */
    public static final int NE_RSR_OVER = 0x08;        /* FIFO overrun */
    public static final int NE_RSR_MPA = 0x10;        /* FIFO overrun */

    public static final int NE_DATA = 0x10;
    public static final int NE_RESET = 0x1f;
    public static final int NE_NR_PORTS = 0x20;

    public static final int NE_PAGESIZE = 256;
    public static final int NE_MAX_ISR_LOOPS = 12;
}
