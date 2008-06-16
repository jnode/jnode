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

package org.jnode.driver.video.ddc;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface EDIDConstants {

    /* read complete EDID record */
    public static final int EDID1_LEN = 128;
    public static final int BITS_PER_BYTE = 9;
    public static final int HEADER = 6;
    public static final int STD_TIMINGS = 8;
    public static final int DET_TIMINGS = 4;

    public static final byte[] HEADER_SIGNATURE = {0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, 0x00};

    /* header: 0x00 0xFF 0xFF 0xFF 0xFF 0xFF 0xFF 0x00 */
    public static final int HEADER_SECTION = 0;
    public static final int HEADER_LENGTH = 8;

    /* vendor section */
    public static final int VENDOR_SECTION = (HEADER_SECTION + HEADER_LENGTH);
    public static final int V_MANUFACTURER = 0;
    public static final int V_PROD_ID = (V_MANUFACTURER + 2);
    public static final int V_SERIAL = (V_PROD_ID + 2);
    public static final int V_WEEK = (V_SERIAL + 4);
    public static final int V_YEAR = (V_WEEK + 1);
    public static final int VENDOR_LENGTH = (V_YEAR + 1);

    /* EDID version */
    public static final int VERSION_SECTION = (VENDOR_SECTION + VENDOR_LENGTH);
    public static final int V_VERSION = 0;
    public static final int V_REVISION = (V_VERSION + 1);
    public static final int VERSION_LENGTH = (V_REVISION + 1);

    /* display information */
    public static final int DISPLAY_SECTION = (VERSION_SECTION + VERSION_LENGTH);
    public static final int D_INPUT = 0;
    public static final int D_HSIZE = (D_INPUT + 1);
    public static final int D_VSIZE = (D_HSIZE + 1);
    public static final int D_GAMMA = (D_VSIZE + 1);
    public static final int FEAT_S = (D_GAMMA + 1);
    public static final int D_RG_LOW = (FEAT_S + 1);
    public static final int D_BW_LOW = (D_RG_LOW + 1);
    public static final int D_REDX = (D_BW_LOW + 1);
    public static final int D_REDY = (D_REDX + 1);
    public static final int D_GREENX = (D_REDY + 1);
    public static final int D_GREENY = (D_GREENX + 1);
    public static final int D_BLUEX = (D_GREENY + 1);
    public static final int D_BLUEY = (D_BLUEX + 1);
    public static final int D_WHITEX = (D_BLUEY + 1);
    public static final int D_WHITEY = (D_WHITEX + 1);
    public static final int DISPLAY_LENGTH = (D_WHITEY + 1);

    /* supported VESA and other standard timings */
    public static final int ESTABLISHED_TIMING_SECTION = (DISPLAY_SECTION + DISPLAY_LENGTH);
    public static final int E_T1 = 0;
    public static final int E_T2 = (E_T1 + 1);
    public static final int E_TMANU = (E_T2 + 1);
    public static final int E_TIMING_LENGTH = (E_TMANU + 1);

    /* non predefined standard timings supported by display */
    public static final int STD_TIMING_SECTION = (ESTABLISHED_TIMING_SECTION + E_TIMING_LENGTH);
    public static final int STD_TIMING_INFO_LEN = 2;
    public static final int STD_TIMING_INFO_NUM = STD_TIMINGS;
    public static final int STD_TIMING_LENGTH = (STD_TIMING_INFO_LEN * STD_TIMING_INFO_NUM);

    /* detailed timing info of non standard timings */
    public static final int DET_TIMING_SECTION = (STD_TIMING_SECTION + STD_TIMING_LENGTH);
    public static final int DET_TIMING_INFO_LEN = 18;
    public static final int MONITOR_DESC_LEN = DET_TIMING_INFO_LEN;
    public static final int DET_TIMING_INFO_NUM = DET_TIMINGS;
    public static final int DET_TIMING_LENGTH = (DET_TIMING_INFO_LEN * DET_TIMING_INFO_NUM);

    /* number of EDID sections to follow */
    public static final int NO_EDID = (DET_TIMING_SECTION + DET_TIMING_LENGTH);
    /* one byte checksum */
    public static final int CHECKSUM = (NO_EDID + 1);

}
