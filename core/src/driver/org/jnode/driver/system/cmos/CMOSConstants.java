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
 
package org.jnode.driver.system.cmos;

/**
 * @author epr
 */
public interface CMOSConstants {

    /**
     * Port number for CMOS address register
     */
    public static final int PRW8_ADDRESS = 0x70;

    /**
     * Port number for CMOS data register
     */
    public static final int PRW8_DATA = 0x71;

    public static final int CMOS_FIRST_PORT = PRW8_ADDRESS;
    public static final int CMOS_LAST_PORT = PRW8_DATA;

    // --------------------------------
    // CMOS registers

    public static final int CMOS_RTC_SECONDS = 0x00;
    public static final int CMOS_RTC_SECONDS_ALARM = 0x01;
    public static final int CMOS_RTC_MINUTES = 0x02;
    public static final int CMOS_RTC_MINUTES_ALARM = 0x03;
    public static final int CMOS_RTC_HOURS = 0x04;
    public static final int CMOS_RTC_HOURS_ALARM = 0x05;
    public static final int CMOS_RTC_DAY_OF_WEEK = 0x06;
    public static final int CMOS_RTC_DAY_OF_MONTH = 0x07;
    public static final int CMOS_RTC_MONTH = 0x08;
    public static final int CMOS_RTC_YEAR = 0x09;
    public static final int CMOS_FLOPPY_DRIVES = 0x10;

    /* control registers - Moto names
      */
    public static final int RTC_REG_A = 10;
    public static final int RTC_REG_B = 11;
    public static final int RTC_REG_C = 12;
    public static final int RTC_REG_D = 13;

    /**
     * *******************************************************************
     * register details
     * ********************************************************************
     */
    public static final int RTC_FREQ_SELECT = RTC_REG_A;

    /* update-in-progress  - set to "1" 244 microsecs before RTC goes off the bus,
      * reset after update (may take 1.984ms @ 32768Hz RefClock) is complete,
      * totalling to a max high interval of 2.228 ms.
      */
    public static final int RTC_UIP = 0x80;
    public static final int RTC_DIV_CTL = 0x70;
    /* divider control: refclock values 4.194 / 1.049 MHz / 32.768 kHz */
    public static final int RTC_REF_CLCK_4MHZ = 0x00;
    public static final int RTC_REF_CLCK_1MHZ = 0x10;
    public static final int RTC_REF_CLCK_32KHZ = 0x20;
    /* 2 values for divider stage reset, others for "testing purposes only" */
    public static final int RTC_DIV_RESET1 = 0x60;
    public static final int RTC_DIV_RESET2 = 0x70;
    /* Periodic intr. / Square wave rate select. 0=none, 1=32.8kHz,... 15=2Hz */
    public static final int RTC_RATE_SELECT = 0x0F;

    /**
     * ******************************************************************
     */
    public static final int RTC_CONTROL = RTC_REG_B;
    public static final int RTC_SET = 0x80;    /* disable updates for clock setting */
    public static final int RTC_PIE = 0x40;    /* periodic interrupt enable */
    public static final int RTC_AIE = 0x20;    /* alarm interrupt enable */
    public static final int RTC_UIE = 0x10;    /* update-finished interrupt enable */
    public static final int RTC_SQWE = 0x08;    /* enable square-wave output */
    public static final int RTC_DM_BINARY = 0x04;    /* all time/date values are BCD if clear */
    public static final int RTC_24H = 0x02;    /* 24 hour mode - else hours bit 7 means pm */
    public static final int RTC_DST_EN = 0x01;    /* auto switch DST - works f. USA only */

    /**
     * ******************************************************************
     */
    public static final int RTC_INTR_FLAGS = RTC_REG_C;
    /* caution - cleared by read */
    public static final int RTC_IRQF = 0x80;    /* any of the following 3 is active */
    public static final int RTC_PF = 0x40;
    public static final int RTC_AF = 0x20;
    public static final int RTC_UF = 0x10;

    /**
     * ******************************************************************
     */
    public static final int RTC_VALID = RTC_REG_D;
    public static final int RTC_VRT = 0x80;    /* valid RAM and time */
    /**********************************************************************/


}
