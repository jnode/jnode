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
 
package org.jnode.system.x86;

/**
 * @author epr
 */
public interface DMAConstants {

    /* DMA controller registers */
    public static final int DMA1_CMD_REG = 0x08; /* command register (w) */
    public static final int DMA1_STAT_REG = 0x08; /* status register (r) */
    public static final int DMA1_REQ_REG = 0x09; /* request register (w) */
    public static final int DMA1_MASK_REG = 0x0A; /* single-channel mask (w) */
    public static final int DMA1_MODE_REG = 0x0B; /* mode register (w) */
    public static final int DMA1_CLEAR_FF_REG = 0x0C;
    /* clear pointer flip-flop (w) */
    public static final int DMA1_TEMP_REG = 0x0D; /* Temporary Register (r) */
    public static final int DMA1_RESET_REG = 0x0D; /* Master Clear (w) */
    public static final int DMA1_CLR_MASK_REG = 0x0E; /* Clear Mask */
    public static final int DMA1_MASK_ALL_REG = 0x0F;
    /* all-channels mask (w) */

    public static final int DMA2_CMD_REG = 0xD0; /* command register (w) */
    public static final int DMA2_STAT_REG = 0xD0; /* status register (r) */
    public static final int DMA2_REQ_REG = 0xD2; /* request register (w) */
    public static final int DMA2_MASK_REG = 0xD4; /* single-channel mask (w) */
    public static final int DMA2_MODE_REG = 0xD6; /* mode register (w) */
    public static final int DMA2_CLEAR_FF_REG = 0xD8;
    /* clear pointer flip-flop (w) */
    public static final int DMA2_TEMP_REG = 0xDA; /* Temporary Register (r) */
    public static final int DMA2_RESET_REG = 0xDA; /* Master Clear (w) */
    public static final int DMA2_CLR_MASK_REG = 0xDC; /* Clear Mask */
    public static final int DMA2_MASK_ALL_REG = 0xDE;
    /* all-channels mask (w) */

    public static final int DMA_ADDR_0 = 0x00; /* DMA address registers */
    public static final int DMA_ADDR_1 = 0x02;
    public static final int DMA_ADDR_2 = 0x04;
    public static final int DMA_ADDR_3 = 0x06;
    public static final int DMA_ADDR_4 = 0xC0;
    public static final int DMA_ADDR_5 = 0xC4;
    public static final int DMA_ADDR_6 = 0xC8;
    public static final int DMA_ADDR_7 = 0xCC;

    public static final int DMA_CNT_0 = 0x01; /* DMA count registers */
    public static final int DMA_CNT_1 = 0x03;
    public static final int DMA_CNT_2 = 0x05;
    public static final int DMA_CNT_3 = 0x07;
    public static final int DMA_CNT_4 = 0xC2;
    public static final int DMA_CNT_5 = 0xC6;
    public static final int DMA_CNT_6 = 0xCA;
    public static final int DMA_CNT_7 = 0xCE;

    public static final int DMA_PAGE_0 = 0x87; /* DMA page registers */
    public static final int DMA_PAGE_1 = 0x83;
    public static final int DMA_PAGE_2 = 0x81;
    public static final int DMA_PAGE_3 = 0x82;
    public static final int DMA_PAGE_5 = 0x8B;
    public static final int DMA_PAGE_6 = 0x89;
    public static final int DMA_PAGE_7 = 0x8A;

    /**
     * I/O to memory, no autoinit, increment, single mode
     */
    public static final int DMA_MODE_READ = 0x44;
    /**
     * memory to I/O, no autoinit, increment, single mode
     */
    public static final int DMA_MODE_WRITE = 0x48;
    /**
     * pass thru DREQ->HRQ, DACK<-HLDA only
     */
    public static final int DMA_MODE_CASCADE = 0xC0;

    public static final int DMA_AUTOINIT = 0x10;
}
