/*
 * $Id$
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
 
package org.jnode.driver.video.ati.radeon;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface RadeonConstants {

    public interface Architecture {

        public static final int R100 = 1;

        public static final int RV100 = 2;

        public static final int R200 = 3;

        public static final int RV200 = 4;

        public static final int RV250 = 5;

        public static final int R300 = 6;

        public static final int M6 = 7;

        public static final int M7 = 8;

        public static final int M9 = 9;
    }

    public static class MonitorType {

        public static final int NONE = 0;
        public static final int CRT = 1; /* CRT */
        public static final int LCD = 2; /* LCD */
        public static final int DFP = 3; /* DVI */
        public static final int CTV = 4; /* composite TV */
        public static final int STV = 5; /* S-Video out */

        private static final String[] NAMES = {"NONE", "CRT", "LCD", "DFP", "CTV", "STV"};

        public static String toString(int monitorType) {
            return NAMES[monitorType];
        }

    }

    public static final int RADEON_REGSIZE = 0x4000;

    public static final int MM_INDEX = 0x0000;

    public static final int MM_DATA = 0x0004;

    public static final int BUS_CNTL = 0x0030;

    public static final int HI_STAT = 0x004C;

    public static final int BUS_CNTL1 = 0x0034;

    public static final int I2C_CNTL_1 = 0x0094;

    public static final int CONFIG_CNTL = 0x00E0;

    public static final int CONFIG_MEMSIZE = 0x00F8;

    public static final int CONFIG_APER_0_BASE = 0x0100;

    public static final int CONFIG_APER_1_BASE = 0x0104;

    public static final int CONFIG_APER_SIZE = 0x0108;

    public static final int CONFIG_REG_1_BASE = 0x010C;

    public static final int CONFIG_REG_APER_SIZE = 0x0110;

    public static final int PAD_AGPINPUT_DELAY = 0x0164;

    public static final int PAD_CTLR_STRENGTH = 0x0168;

    public static final int PAD_CTLR_UPDATE = 0x016C;

    public static final int AGP_CNTL = 0x0174;

    public static final int BM_STATUS = 0x0160;

    public static final int CAP0_TRIG_CNTL = 0x0950;
    public static final int CAP1_TRIG_CNTL = 0x09c0; /* ? */

    public static final int VIPH_CONTROL = 0x0C40;

    public static final int VENDOR_ID = 0x0F00;

    public static final int DEVICE_ID = 0x0F02;

    public static final int COMMAND = 0x0F04;

    public static final int STATUS = 0x0F06;

    public static final int REVISION_ID = 0x0F08;

    public static final int REGPROG_INF = 0x0F09;

    public static final int SUB_CLASS = 0x0F0A;

    public static final int BASE_CODE = 0x0F0B;

    public static final int CACHE_LINE = 0x0F0C;

    public static final int LATENCY = 0x0F0D;

    public static final int HEADER = 0x0F0E;

    public static final int BIST = 0x0F0F;

    public static final int REG_MEM_BASE = 0x0F10;

    public static final int REG_IO_BASE = 0x0F14;

    public static final int REG_REG_BASE = 0x0F18;

    public static final int ADAPTER_ID = 0x0F2C;

    public static final int BIOS_ROM = 0x0F30;

    public static final int CAPABILITIES_PTR = 0x0F34;

    public static final int INTERRUPT_LINE = 0x0F3C;

    public static final int INTERRUPT_PIN = 0x0F3D;

    public static final int MIN_GRANT = 0x0F3E;

    public static final int MAX_LATENCY = 0x0F3F;

    public static final int ADAPTER_ID_W = 0x0F4C;

    public static final int PMI_CAP_ID = 0x0F50;

    public static final int PMI_NXT_CAP_PTR = 0x0F51;

    public static final int PMI_PMC_REG = 0x0F52;

    public static final int PM_STATUS = 0x0F54;

    public static final int PMI_DATA = 0x0F57;

    public static final int AGP_CAP_ID = 0x0F58;

    public static final int AGP_STATUS = 0x0F5C;

    public static final int AGP_COMMAND = 0x0F60;

    public static final int AIC_CTRL = 0x01D0;

    public static final int AIC_STAT = 0x01D4;

    public static final int AIC_PT_BASE = 0x01D8;

    public static final int AIC_LO_ADDR = 0x01DC;

    public static final int AIC_HI_ADDR = 0x01E0;

    public static final int AIC_TLB_ADDR = 0x01E4;

    public static final int AIC_TLB_DATA = 0x01E8;

    public static final int DAC_CNTL = 0x0058;

    public static final int DAC_CNTL2 = 0x007c;

    public static final int CRTC_GEN_CNTL = 0x0050;

    public static final int MEM_CNTL = 0x0140;

    public static final int EXT_MEM_CNTL = 0x0144;

    public static final int MC_AGP_LOCATION = 0x014C;

    public static final int MEM_IO_CNTL_A0 = 0x0178;

    public static final int MEM_INIT_LATENCY_TIMER = 0x0154;

    public static final int MEM_SDRAM_MODE_REG = 0x0158;

    public static final int AGP_BASE = 0x0170;

    public static final int MEM_IO_CNTL_A1 = 0x017C;

    public static final int MEM_IO_CNTL_B0 = 0x0180;

    public static final int MEM_IO_CNTL_B1 = 0x0184;

    public static final int MC_DEBUG = 0x0188;

    public static final int MC_STATUS = 0x0150;

    public static final int MEM_IO_OE_CNTL = 0x018C;

    public static final int MC_FB_LOCATION = 0x0148;

    public static final int HOST_PATH_CNTL = 0x0130;

    public static final int MEM_VGA_WP_SEL = 0x0038;

    public static final int MEM_VGA_RP_SEL = 0x003C;

    public static final int HDP_DEBUG = 0x0138;

    public static final int SW_SEMAPHORE = 0x013C;

    public static final int CRTC2_GEN_CNTL = 0x03f8;

    public static final int CRTC2_DISPLAY_BASE_ADDR = 0x033c;

    public static final int SURFACE_CNTL = 0x0B00;

    public static final int SURFACE0_LOWER_BOUND = 0x0B04;

    public static final int SURFACE1_LOWER_BOUND = 0x0B14;

    public static final int SURFACE2_LOWER_BOUND = 0x0B24;

    public static final int SURFACE3_LOWER_BOUND = 0x0B34;

    public static final int SURFACE4_LOWER_BOUND = 0x0B44;

    public static final int SURFACE5_LOWER_BOUND = 0x0B54;

    public static final int SURFACE6_LOWER_BOUND = 0x0B64;

    public static final int SURFACE7_LOWER_BOUND = 0x0B74;

    public static final int SURFACE0_UPPER_BOUND = 0x0B08;

    public static final int SURFACE1_UPPER_BOUND = 0x0B18;

    public static final int SURFACE2_UPPER_BOUND = 0x0B28;

    public static final int SURFACE3_UPPER_BOUND = 0x0B38;

    public static final int SURFACE4_UPPER_BOUND = 0x0B48;

    public static final int SURFACE5_UPPER_BOUND = 0x0B58;

    public static final int SURFACE6_UPPER_BOUND = 0x0B68;

    public static final int SURFACE7_UPPER_BOUND = 0x0B78;

    public static final int SURFACE0_INFO = 0x0B0C;

    public static final int SURFACE1_INFO = 0x0B1C;

    public static final int SURFACE2_INFO = 0x0B2C;

    public static final int SURFACE3_INFO = 0x0B3C;

    public static final int SURFACE4_INFO = 0x0B4C;

    public static final int SURFACE5_INFO = 0x0B5C;

    public static final int SURFACE6_INFO = 0x0B6C;

    public static final int SURFACE7_INFO = 0x0B7C;

    public static final int SURFACE_ACCESS_FLAGS = 0x0BF8;

    public static final int SURFACE_ACCESS_CLR = 0x0BFC;

    public static final int GEN_INT_CNTL = 0x0040;

    public static final int GEN_INT_STATUS = 0x0044;

    public static final int CRTC_EXT_CNTL = 0x0054;

    public static final int RB3D_CNTL = 0x1C3C;

    public static final int WAIT_UNTIL = 0x1720;

    public static final int ISYNC_CNTL = 0x1724;

    public static final int RBBM_GUICNTL = 0x172C;

    public static final int RBBM_STATUS = 0x0E40;

    public static final int RBBM_STATUS_alt_1 = 0x1740;

    public static final int RBBM_CNTL = 0x00EC;

    public static final int RBBM_CNTL_alt_1 = 0x0E44;

    public static final int RBBM_SOFT_RESET = 0x00F0;

    public static final int RBBM_SOFT_RESET_alt_1 = 0x0E48;

    public static final int NQWAIT_UNTIL = 0x0E50;

    public static final int RBBM_DEBUG = 0x0E6C;

    public static final int RBBM_CMDFIFO_ADDR = 0x0E70;

    public static final int RBBM_CMDFIFO_DATAL = 0x0E74;

    public static final int RBBM_CMDFIFO_DATAH = 0x0E78;

    public static final int RBBM_CMDFIFO_STAT = 0x0E7C;

    public static final int CRTC_STATUS = 0x005C;

    public static final int GPIO_VGA_DDC = 0x0060;

    public static final int GPIO_DVI_DDC = 0x0064;

    public static final int GPIO_MONID = 0x0068;

    public static final int GPIO_CRT2_DDC = 0x006c;

    public static final int PALETTE_INDEX = 0x00B0;

    public static final int PALETTE_DATA = 0x00B4;

    public static final int PALETTE_30_DATA = 0x00B8;

    public static final int CRTC_H_TOTAL_DISP = 0x0200;
    public static final int CRTC2_H_TOTAL_DISP = 0x0300;

    public static final int CRTC_H_SYNC_STRT_WID = 0x0204;
    public static final int CRTC2_H_SYNC_STRT_WID = 0x0304;

    public static final int CRTC_V_TOTAL_DISP = 0x0208;
    public static final int CRTC2_V_TOTAL_DISP = 0x0308;

    public static final int CRTC_V_SYNC_STRT_WID = 0x020C;
    public static final int CRTC2_V_SYNC_STRT_WID = 0x030c;

    public static final int CRTC_VLINE_CRNT_VLINE = 0x0210;

    public static final int CRTC_CRNT_FRAME = 0x0214;

    public static final int CRTC_GUI_TRIG_VLINE = 0x0218;

    public static final int CRTC_DEBUG = 0x021C;

    public static final int CRTC_OFFSET_RIGHT = 0x0220;

    public static final int CRTC_OFFSET = 0x0224;
    public static final int CRTC2_OFFSET = 0x0324;

    public static final int CRTC_OFFSET_CNTL = 0x0228;
    public static final int CRTC_TILE_EN = (1 << 15);

    public static final int CRTC2_OFFSET_CNTL = 0x0328;
    public static final int CRTC2_TILE_EN = (1 << 15);

    public static final int CRTC_PITCH = 0x022C;
    public static final int CRTC2_PITCH = 0x032c;

    public static final int OVR_CLR = 0x0230;

    public static final int OVR_WID_LEFT_RIGHT = 0x0234;

    public static final int OVR_WID_TOP_BOTTOM = 0x0238;

    public static final int DISPLAY_BASE_ADDR = 0x023C;

    public static final int SNAPSHOT_VH_COUNTS = 0x0240;

    public static final int SNAPSHOT_F_COUNT = 0x0244;

    public static final int N_VIF_COUNT = 0x0248;

    public static final int SNAPSHOT_VIF_COUNT = 0x024C;

    public static final int FP_CRTC_H_TOTAL_DISP = 0x0250;

    public static final int FP_CRTC_V_TOTAL_DISP = 0x0254;

    public static final int CRT_CRTC_H_SYNC_STRT_WID = 0x0258;

    public static final int CRT_CRTC_V_SYNC_STRT_WID = 0x025C;

    public static final int CUR_OFFSET = 0x0260;

    public static final int CUR_HORZ_VERT_POSN = 0x0264;

    public static final int CUR_HORZ_VERT_OFF = 0x0268;

    public static final int CUR_CLR0 = 0x026C;

    public static final int CUR_CLR1 = 0x0270;

    public static final int FP_HORZ_VERT_ACTIVE = 0x0278;

    public static final int CRTC_MORE_CNTL = 0x027C;

    public static final int DAC_EXT_CNTL = 0x0280;

    public static final int FP_GEN_CNTL = 0x0284;

    public static final int FP_HORZ_STRETCH = 0x028C;

    public static final int FP_VERT_STRETCH = 0x0290;

    public static final int FP_H_SYNC_STRT_WID = 0x02C4;

    public static final int FP_V_SYNC_STRT_WID = 0x02C8;

    public static final int AUX_WINDOW_HORZ_CNTL = 0x02D8;

    public static final int AUX_WINDOW_VERT_CNTL = 0x02DC;

    // public static final int DDA_CONFIG = 0x02e0;
    // public static final int DDA_ON_OFF = 0x02e4;
    public static final int DVI_I2C_CNTL_1 = 0x02e4;

    public static final int GRPH_BUFFER_CNTL = 0x02F0;

    public static final int VGA_BUFFER_CNTL = 0x02F4;

    public static final int OV0_Y_X_START = 0x0400;

    public static final int OV0_Y_X_END = 0x0404;

    public static final int OV0_PIPELINE_CNTL = 0x0408;

    public static final int OV0_REG_LOAD_CNTL = 0x0410;

    public static final int OV0_SCALE_CNTL = 0x0420;

    public static final int OV0_V_INC = 0x0424;

    public static final int OV0_P1_V_ACCUM_INIT = 0x0428;

    public static final int OV0_P23_V_ACCUM_INIT = 0x042C;

    public static final int OV0_P1_BLANK_LINES_AT_TOP = 0x0430;

    public static final int OV0_P23_BLANK_LINES_AT_TOP = 0x0434;

    public static final int OV0_BASE_ADDR = 0x043C;

    public static final int OV0_VID_BUF0_BASE_ADRS = 0x0440;

    public static final int OV0_VID_BUF1_BASE_ADRS = 0x0444;

    public static final int OV0_VID_BUF2_BASE_ADRS = 0x0448;

    public static final int OV0_VID_BUF3_BASE_ADRS = 0x044C;

    public static final int OV0_VID_BUF4_BASE_ADRS = 0x0450;

    public static final int OV0_VID_BUF5_BASE_ADRS = 0x0454;

    public static final int OV0_VID_BUF_PITCH0_VALUE = 0x0460;

    public static final int OV0_VID_BUF_PITCH1_VALUE = 0x0464;

    public static final int OV0_AUTO_FLIP_CNTRL = 0x0470;

    public static final int OV0_DEINTERLACE_PATTERN = 0x0474;

    public static final int OV0_SUBMIT_HISTORY = 0x0478;

    public static final int OV0_H_INC = 0x0480;

    public static final int OV0_STEP_BY = 0x0484;

    public static final int OV0_P1_H_ACCUM_INIT = 0x0488;

    public static final int OV0_P23_H_ACCUM_INIT = 0x048C;

    public static final int OV0_P1_X_START_END = 0x0494;

    public static final int OV0_P2_X_START_END = 0x0498;

    public static final int OV0_P3_X_START_END = 0x049C;

    public static final int OV0_FILTER_CNTL = 0x04A0;

    public static final int OV0_FOUR_TAP_COEF_0 = 0x04B0;

    public static final int OV0_FOUR_TAP_COEF_1 = 0x04B4;

    public static final int OV0_FOUR_TAP_COEF_2 = 0x04B8;

    public static final int OV0_FOUR_TAP_COEF_3 = 0x04BC;

    public static final int OV0_FOUR_TAP_COEF_4 = 0x04C0;

    public static final int OV0_FLAG_CNTRL = 0x04DC;

    public static final int OV0_SLICE_CNTL = 0x04E0;

    public static final int OV0_VID_KEY_CLR_LOW = 0x04E4;

    public static final int OV0_VID_KEY_CLR_HIGH = 0x04E8;

    public static final int OV0_GRPH_KEY_CLR_LOW = 0x04EC;

    public static final int OV0_GRPH_KEY_CLR_HIGH = 0x04F0;

    public static final int OV0_KEY_CNTL = 0x04F4;

    public static final int OV0_TEST = 0x04F8;

    public static final int SUBPIC_CNTL = 0x0540;

    public static final int SUBPIC_DEFCOLCON = 0x0544;

    public static final int SUBPIC_Y_X_START = 0x054C;

    public static final int SUBPIC_Y_X_END = 0x0550;

    public static final int SUBPIC_V_INC = 0x0554;

    public static final int SUBPIC_H_INC = 0x0558;

    public static final int SUBPIC_BUF0_OFFSET = 0x055C;

    public static final int SUBPIC_BUF1_OFFSET = 0x0560;

    public static final int SUBPIC_LC0_OFFSET = 0x0564;

    public static final int SUBPIC_LC1_OFFSET = 0x0568;

    public static final int SUBPIC_PITCH = 0x056C;

    public static final int SUBPIC_BTN_HLI_COLCON = 0x0570;

    public static final int SUBPIC_BTN_HLI_Y_X_START = 0x0574;

    public static final int SUBPIC_BTN_HLI_Y_X_END = 0x0578;

    public static final int SUBPIC_PALETTE_INDEX = 0x057C;

    public static final int SUBPIC_PALETTE_DATA = 0x0580;

    public static final int SUBPIC_H_ACCUM_INIT = 0x0584;

    public static final int SUBPIC_V_ACCUM_INIT = 0x0588;

    public static final int DISP_MISC_CNTL = 0x0D00;

    public static final int DAC_MACRO_CNTL = 0x0D04;

    public static final int DISP_PWR_MAN = 0x0D08;

    public static final int DISP_TEST_DEBUG_CNTL = 0x0D10;

    public static final int DISP_HW_DEBUG = 0x0D14;

    public static final int DAC_CRC_SIG1 = 0x0D18;

    public static final int DAC_CRC_SIG2 = 0x0D1C;

    public static final int OV0_LIN_TRANS_A = 0x0D20;

    public static final int OV0_LIN_TRANS_B = 0x0D24;

    public static final int OV0_LIN_TRANS_C = 0x0D28;

    public static final int OV0_LIN_TRANS_D = 0x0D2C;

    public static final int OV0_LIN_TRANS_E = 0x0D30;

    public static final int OV0_LIN_TRANS_F = 0x0D34;

    public static final int OV0_GAMMA_0_F = 0x0D40;

    public static final int OV0_GAMMA_10_1F = 0x0D44;

    public static final int OV0_GAMMA_20_3F = 0x0D48;

    public static final int OV0_GAMMA_40_7F = 0x0D4C;

    public static final int OV0_GAMMA_380_3BF = 0x0D50;

    public static final int OV0_GAMMA_3C0_3FF = 0x0D54;

    public static final int DISP_MERGE_CNTL = 0x0D60;

    public static final int DISP_OUTPUT_CNTL = 0x0D64;

    public static final int DISP_LIN_TRANS_GRPH_A = 0x0D80;

    public static final int DISP_LIN_TRANS_GRPH_B = 0x0D84;

    public static final int DISP_LIN_TRANS_GRPH_C = 0x0D88;

    public static final int DISP_LIN_TRANS_GRPH_D = 0x0D8C;

    public static final int DISP_LIN_TRANS_GRPH_E = 0x0D90;

    public static final int DISP_LIN_TRANS_GRPH_F = 0x0D94;

    public static final int DISP_LIN_TRANS_VID_A = 0x0D98;

    public static final int DISP_LIN_TRANS_VID_B = 0x0D9C;

    public static final int DISP_LIN_TRANS_VID_C = 0x0DA0;

    public static final int DISP_LIN_TRANS_VID_D = 0x0DA4;

    public static final int DISP_LIN_TRANS_VID_E = 0x0DA8;

    public static final int DISP_LIN_TRANS_VID_F = 0x0DAC;

    public static final int RMX_HORZ_FILTER_0TAP_COEF = 0x0DB0;

    public static final int RMX_HORZ_FILTER_1TAP_COEF = 0x0DB4;

    public static final int RMX_HORZ_FILTER_2TAP_COEF = 0x0DB8;

    public static final int RMX_HORZ_PHASE = 0x0DBC;

    public static final int DAC_EMBEDDED_SYNC_CNTL = 0x0DC0;

    public static final int DAC_BROAD_PULSE = 0x0DC4;

    public static final int DAC_SKEW_CLKS = 0x0DC8;

    public static final int DAC_INCR = 0x0DCC;

    public static final int DAC_NEG_SYNC_LEVEL = 0x0DD0;

    public static final int DAC_POS_SYNC_LEVEL = 0x0DD4;

    public static final int DAC_BLANK_LEVEL = 0x0DD8;

    public static final int CLOCK_CNTL_INDEX = 0x0008;

    public static final int CLOCK_CNTL_DATA = 0x000C;

    public static final int CP_RB_CNTL = 0x0704;

    public static final int CP_RB_BASE = 0x0700;

    public static final int CP_RB_RPTR_ADDR = 0x070C;

    public static final int CP_RB_RPTR = 0x0710;

    public static final int CP_RB_WPTR = 0x0714;

    public static final int CP_RB_WPTR_DELAY = 0x0718;

    public static final int CP_IB_BASE = 0x0738;

    public static final int CP_IB_BUFSZ = 0x073C;

    public static final int CP_CSQ_CNTL = 0x0740;

    public static final int SCRATCH_REG0 = 0x15E0;

    public static final int GUI_SCRATCH_REG0 = 0x15E0;

    public static final int SCRATCH_REG1 = 0x15E4;

    public static final int GUI_SCRATCH_REG1 = 0x15E4;

    public static final int SCRATCH_REG2 = 0x15E8;

    public static final int GUI_SCRATCH_REG2 = 0x15E8;

    public static final int SCRATCH_REG3 = 0x15EC;

    public static final int GUI_SCRATCH_REG3 = 0x15EC;

    public static final int SCRATCH_REG4 = 0x15F0;

    public static final int GUI_SCRATCH_REG4 = 0x15F0;

    public static final int SCRATCH_REG5 = 0x15F4;

    public static final int GUI_SCRATCH_REG5 = 0x15F4;

    public static final int SCRATCH_UMSK = 0x0770;

    public static final int SCRATCH_ADDR = 0x0774;

    public static final int DP_BRUSH_FRGD_CLR = 0x147C;

    public static final int DP_BRUSH_BKGD_CLR = 0x1478;

    public static final int DST_LINE_START = 0x1600;

    public static final int DST_LINE_END = 0x1604;

    public static final int SRC_OFFSET = 0x15AC;

    public static final int SRC_PITCH = 0x15B0;

    public static final int SRC_TILE = 0x1704;

    public static final int SRC_PITCH_OFFSET = 0x1428;

    public static final int SRC_X = 0x1414;

    public static final int SRC_Y = 0x1418;

    public static final int SRC_X_Y = 0x1590;

    public static final int SRC_Y_X = 0x1434;

    public static final int DST_Y_X = 0x1438;

    public static final int DST_WIDTH_HEIGHT = 0x1598;

    public static final int DST_HEIGHT_WIDTH = 0x143c;

    public static final int DST_OFFSET = 0x1404;

    public static final int SRC_CLUT_ADDRESS = 0x1780;

    public static final int SRC_CLUT_DATA = 0x1784;

    public static final int SRC_CLUT_DATA_RD = 0x1788;

    public static final int HOST_DATA0 = 0x17C0;

    public static final int HOST_DATA1 = 0x17C4;

    public static final int HOST_DATA2 = 0x17C8;

    public static final int HOST_DATA3 = 0x17CC;

    public static final int HOST_DATA4 = 0x17D0;

    public static final int HOST_DATA5 = 0x17D4;

    public static final int HOST_DATA6 = 0x17D8;

    public static final int HOST_DATA7 = 0x17DC;

    public static final int HOST_DATA_LAST = 0x17E0;

    public static final int DP_SRC_ENDIAN = 0x15D4;

    public static final int DP_SRC_FRGD_CLR = 0x15D8;

    public static final int DP_SRC_BKGD_CLR = 0x15DC;

    public static final int SC_LEFT = 0x1640;

    public static final int SC_RIGHT = 0x1644;

    public static final int SC_TOP = 0x1648;

    public static final int SC_BOTTOM = 0x164C;

    public static final int SRC_SC_RIGHT = 0x1654;

    public static final int SRC_SC_BOTTOM = 0x165C;

    public static final int DP_CNTL = 0x16C0;

    public static final int DP_CNTL_XDIR_YDIR_YMAJOR = 0x16D0;

    public static final int DP_DATATYPE = 0x16C4;

    public static final int DP_MIX = 0x16C8;

    public static final int DP_WRITE_MSK = 0x16CC;

    public static final int DP_XOP = 0x17F8;

    public static final int CLR_CMP_CLR_SRC = 0x15C4;

    public static final int CLR_CMP_CLR_DST = 0x15C8;

    public static final int CLR_CMP_CNTL = 0x15C0;

    public static final int CLR_CMP_MSK = 0x15CC;

    public static final int DSTCACHE_MODE = 0x1710;

    public static final int DSTCACHE_CTLSTAT = 0x1714;

    public static final int DEFAULT_PITCH_OFFSET = 0x16E0;

    public static final int DEFAULT_SC_BOTTOM_RIGHT = 0x16E8;

    public static final int DP_GUI_MASTER_CNTL = 0x146C;

    public static final int SC_TOP_LEFT = 0x16EC;

    public static final int SC_BOTTOM_RIGHT = 0x16F0;

    public static final int SRC_SC_BOTTOM_RIGHT = 0x16F4;

    public static final int RB2D_DSTCACHE_MODE = 0x3428;

    public static final int RB2D_DSTCACHE_CTLSTAT = 0x342C;

    public static final int LVDS_GEN_CNTL = 0x02d0;

    public static final int LVDS_PLL_CNTL = 0x02d4;

    public static final int TMDS_CRC = 0x02a0;

    public static final int TMDS_TRANSMITTER_CNTL = 0x02a4;

    public static final int RADEON_BASE_CODE = 0x0f0b;

    public static final int RADEON_BIOS_0_SCRATCH = 0x0010;

    public static final int RADEON_BIOS_1_SCRATCH = 0x0014;

    public static final int RADEON_BIOS_2_SCRATCH = 0x0018;

    public static final int RADEON_BIOS_3_SCRATCH = 0x001c;

    public static final int RADEON_BIOS_4_SCRATCH = 0x0020;

    public static final int RADEON_BIOS_5_SCRATCH = 0x0024;

    public static final int RADEON_BIOS_6_SCRATCH = 0x0028;

    public static final int RADEON_BIOS_7_SCRATCH = 0x002c;

    public static final int TV_DAC_CNTL = 0x088c;

    public static final int GPIOPAD_MASK = 0x0198;

    public static final int GPIOPAD_A = 0x019c;

    public static final int GPIOPAD_EN = 0x01a0;

    public static final int GPIOPAD_Y = 0x01a4;

    public static final int ZV_LCDPAD_MASK = 0x01a8;

    public static final int ZV_LCDPAD_A = 0x01ac;

    public static final int ZV_LCDPAD_EN = 0x01b0;

    public static final int ZV_LCDPAD_Y = 0x01b4;

    /* PLL Registers */
    public static final int CLK_PIN_CNTL = 0x0001;

    public static final int PPLL_CNTL = 0x0002;

    public static final int PPLL_REF_DIV = 0x0003;

    public static final int PPLL_DIV_0 = 0x0004;

    public static final int PPLL_DIV_1 = 0x0005;

    public static final int PPLL_DIV_2 = 0x0006;

    public static final int PPLL_DIV_3 = 0x0007;

    public static final int VCLK_ECP_CNTL = 0x0008;

    public static final int HTOTAL_CNTL = 0x0009;

    public static final int M_SPLL_REF_FB_DIV = 0x000a;

    public static final int AGP_PLL_CNTL = 0x000b;

    public static final int SPLL_CNTL = 0x000c;

    public static final int SCLK_CNTL = 0x000d;

    public static final int MPLL_CNTL = 0x000e;

    public static final int MDLL_CKO = 0x000f;

    public static final int MDLL_RDCKA = 0x0010;

    public static final int MCLK_CNTL = 0x0012;

    public static final int PLL_TEST_CNTL = 0x0013;

    public static final int CLK_PWRMGT_CNTL = 0x0014;

    public static final int PLL_PWRMGT_CNTL = 0x0015;

    public static final int MCLK_MISC = 0x001f;

    public static final int P2PLL_CNTL = 0x002a;

    public static final int P2PLL_REF_DIV = 0x002b;
    public static final int P2PLL_DIV_0 = 0x002c;

    public static final int PIXCLKS_CNTL = 0x002d;
    public static final int HTOTAL2_CNTL = 0x002e;

    /* MCLK_CNTL bit constants */
    public static final int FORCEON_MCLKA = (1 << 16);

    public static final int FORCEON_MCLKB = (1 << 17);

    public static final int FORCEON_YCLKA = (1 << 18);

    public static final int FORCEON_YCLKB = (1 << 19);

    public static final int FORCEON_MC = (1 << 20);

    public static final int FORCEON_AIC = (1 << 21);

    /* BUS_CNTL bit constants */
    public static final int BUS_DBL_RESYNC = 0x00000001;

    public static final int BUS_MSTR_RESET = 0x00000002;

    public static final int BUS_FLUSH_BUF = 0x00000004;

    public static final int BUS_STOP_REQ_DIS = 0x00000008;

    public static final int BUS_ROTATION_DIS = 0x00000010;

    public static final int BUS_MASTER_DIS = 0x00000040;

    public static final int BUS_ROM_WRT_EN = 0x00000080;

    public static final int BUS_DIS_ROM = 0x00001000;

    public static final int BUS_PCI_READ_RETRY_EN = 0x00002000;

    public static final int BUS_AGP_AD_STEPPING_EN = 0x00004000;

    public static final int BUS_PCI_WRT_RETRY_EN = 0x00008000;

    public static final int BUS_MSTR_RD_MULT = 0x00100000;

    public static final int BUS_MSTR_RD_LINE = 0x00200000;

    public static final int BUS_SUSPEND = 0x00400000;

    public static final int LAT_16X = 0x00800000;

    public static final int BUS_RD_DISCARD_EN = 0x01000000;

    public static final int BUS_RD_ABORT_EN = 0x02000000;

    public static final int BUS_MSTR_WS = 0x04000000;

    public static final int BUS_PARKING_DIS = 0x08000000;

    public static final int BUS_MSTR_DISCONNECT_EN = 0x10000000;

    public static final int BUS_WRT_BURST = 0x20000000;

    public static final int BUS_READ_BURST = 0x40000000;

    public static final int BUS_RDY_READ_DLY = 0x80000000;

    /* CLOCK_CNTL_INDEX bit constants */
    public static final int PLL_WR_EN = 0x00000080;
    public static final int PLL_DIV_SEL_MASK = (3 << 8);
    public static final int PLL_DIV_SEL_DIV0 = (0 << 8);
    public static final int PLL_DIV_SEL_DIV1 = (1 << 8);
    public static final int PLL_DIV_SEL_DIV2 = (2 << 8);
    public static final int PLL_DIV_SEL_DIV3 = (3 << 8);

    /* CONFIG_CNTL bit constants */
    public static final int CFG_VGA_RAM_EN = 0x00000100;

    /* CRTC_EXT_CNTL bit constants */
    public static final int VGA_ATI_LINEAR = 0x00000008;

    public static final int VGA_128KAP_PAGING = 0x00000010;

    public static final int XCRT_CNT_EN = (1 << 6);

    public static final int CRTC_HSYNC_DIS = (1 << 8);

    public static final int CRTC_VSYNC_DIS = (1 << 9);

    public static final int CRTC_DISPLAY_DIS = (1 << 10);

    public static final int CRTC_CRT_ON = (1 << 15);

    /* DSTCACHE_CTLSTAT bit constants */
    public static final int RB2D_DC_FLUSH = (3 << 0);

    public static final int RB2D_DC_FLUSH_ALL = 0xf;

    public static final int RB2D_DC_BUSY = (1 << 31);

    /* CRTC_GEN_CNTL bit constants */
    public static final int CRTC_DBL_SCAN_EN = 0x00000001;

    public static final int CRTC_CUR_EN = 0x00010000;

    public static final int CRTC_INTERLACE_EN = (1 << 1);

    public static final int CRTC_EXT_DISP_EN = (1 << 24);

    public static final int CRTC_EN = (1 << 25);

    public static final int CRTC_DISP_REQ_EN_B = (1 << 26);

    /* CRTC_STATUS bit constants */
    public static final int CRTC_VBLANK = 0x00000001;

    /* CRTC2_GEN_CNTL bit constants */
    public static final int CRTC2_DBL_SCAN_EN = (1 << 0);
    public static final int CRTC2_INTERLACE_EN = (1 << 1);
    public static final int CRTC2_SYNC_TRISTAT = (1 << 4);
    public static final int CRTC2_HSYNC_TRISTAT = (1 << 5);
    public static final int CRTC2_VSYNC_TRISTAT = (1 << 6);
    public static final int CRTC2_CRT2_ON = (1 << 7);
    public static final int CRTC2_PIX_WIDTH_SHIFT = 8;
    public static final int CRTC2_PIX_WIDTH_MASK = (0xf << 8);
    public static final int CRTC2_ICON_EN = (1 << 15);
    public static final int CRTC2_CUR_EN = (1 << 16);
    public static final int CRTC2_CUR_MODE_MASK = (7 << 20);
    public static final int CRTC2_DISP_DIS = (1 << 23);
    public static final int CRTC2_EN = (1 << 25);
    public static final int CRTC2_DISP_REQ_EN_B = (1 << 26);
    public static final int CRTC2_HSYNC_DIS = (1 << 28);
    public static final int CRTC2_VSYNC_DIS = (1 << 29);

    /* CUR_OFFSET, CUR_HORZ_VERT_POSN, CUR_HORZ_VERT_OFF bit constants */
    public static final int CUR_LOCK = 0x80000000;

    /* FP bit constants */
    public static final int FP_CRTC_H_TOTAL_MASK = 0x000003ff;

    public static final int FP_CRTC_H_DISP_MASK = 0x01ff0000;

    public static final int FP_CRTC_V_TOTAL_MASK = 0x00000fff;

    public static final int FP_CRTC_V_DISP_MASK = 0x0fff0000;

    public static final int FP_H_SYNC_STRT_CHAR_MASK = 0x00001ff8;

    public static final int FP_H_SYNC_WID_MASK = 0x003f0000;

    public static final int FP_V_SYNC_STRT_MASK = 0x00000fff;

    public static final int FP_V_SYNC_WID_MASK = 0x001f0000;

    public static final int FP_CRTC_H_TOTAL_SHIFT = 0x00000000;

    public static final int FP_CRTC_H_DISP_SHIFT = 0x00000010;

    public static final int FP_CRTC_V_TOTAL_SHIFT = 0x00000000;

    public static final int FP_CRTC_V_DISP_SHIFT = 0x00000010;

    public static final int FP_H_SYNC_STRT_CHAR_SHIFT = 0x00000003;

    public static final int FP_H_SYNC_WID_SHIFT = 0x00000010;

    public static final int FP_V_SYNC_STRT_SHIFT = 0x00000000;

    public static final int FP_V_SYNC_WID_SHIFT = 0x00000010;

    /* FP_GEN_CNTL bit constants */
    public static final int FP_FPON = (1 << 0);

    public static final int FP_TMDS_EN = (1 << 2);

    public static final int FP_EN_TMDS = (1 << 7);

    public static final int FP_DETECT_SENSE = (1 << 8);

    public static final int FP_SEL_CRTC2 = (1 << 13);

    public static final int FP_CRTC_DONT_SHADOW_HPAR = (1 << 15);

    public static final int FP_CRTC_DONT_SHADOW_VPAR = (1 << 16);

    public static final int FP_CRTC_DONT_SHADOW_HEND = (1 << 17);

    public static final int FP_CRTC_USE_SHADOW_VEND = (1 << 18);

    public static final int FP_RMX_HVSYNC_CONTROL_EN = (1 << 20);

    public static final int FP_DFP_SYNC_SEL = (1 << 21);

    public static final int FP_CRTC_LOCK_8DOT = (1 << 22);

    public static final int FP_CRT_SYNC_SEL = (1 << 23);

    public static final int FP_USE_SHADOW_EN = (1 << 24);

    public static final int FP_CRT_SYNC_ALT = (1 << 26);

    /* LVDS_GEN_CNTL bit constants */
    public static final int LVDS_ON = (1 << 0);

    public static final int LVDS_DISPLAY_DIS = (1 << 1);

    public static final int LVDS_PANEL_TYPE = (1 << 2);

    public static final int LVDS_PANEL_FORMAT = (1 << 3);

    public static final int LVDS_EN = (1 << 7);

    public static final int LVDS_BL_MOD_LEVEL_MASK = 0x0000ff00;

    public static final int LVDS_BL_MOD_LEVEL_SHIFT = 8;

    public static final int LVDS_BL_MOD_EN = (1 << 16);

    public static final int LVDS_DIGON = (1 << 18);

    public static final int LVDS_BLON = (1 << 19);

    public static final int LVDS_SEL_CRTC2 = (1 << 23);

    public static final int LVDS_STATE_MASK =
            (LVDS_ON | LVDS_DISPLAY_DIS | LVDS_BL_MOD_LEVEL_MASK | LVDS_EN | LVDS_DIGON | LVDS_BLON);

    /* LVDS_PLL_CNTL bit constatns */
    public static final int HSYNC_DELAY_SHIFT = 0x1c;

    public static final int HSYNC_DELAY_MASK = (0xf << 0x1c);

    /* TMDS_TRANSMITTER_CNTL bit constants */
    public static final int TMDS_PLL_EN = (1 << 0);

    public static final int TMDS_PLLRST = (1 << 1);

    public static final int TMDS_RAN_PAT_RST = (1 << 7);

    public static final int ICHCSEL = (1 << 28);

    /* FP_HORZ_STRETCH bit constants */
    public static final int HORZ_STRETCH_RATIO_MASK = 0xffff;

    public static final int HORZ_STRETCH_RATIO_MAX = 4096;

    public static final int HORZ_PANEL_SIZE = (0x1ff << 16);

    public static final int HORZ_PANEL_SHIFT = 16;

    public static final int HORZ_STRETCH_PIXREP = (0 << 25);

    public static final int HORZ_STRETCH_BLEND = (1 << 26);

    public static final int HORZ_STRETCH_ENABLE = (1 << 25);

    public static final int HORZ_AUTO_RATIO = (1 << 27);

    public static final int HORZ_FP_LOOP_STRETCH = (0x7 << 28);

    public static final int HORZ_AUTO_RATIO_INC = (1 << 31);

    /* FP_VERT_STRETCH bit constants */
    public static final int VERT_STRETCH_RATIO_MASK = 0xfff;

    public static final int VERT_STRETCH_RATIO_MAX = 4096;

    public static final int VERT_PANEL_SIZE = (0xfff << 12);

    public static final int VERT_PANEL_SHIFT = 12;

    public static final int VERT_STRETCH_LINREP = (0 << 26);

    public static final int VERT_STRETCH_BLEND = (1 << 26);

    public static final int VERT_STRETCH_ENABLE = (1 << 25);

    public static final int VERT_AUTO_RATIO_EN = (1 << 27);

    public static final int VERT_FP_LOOP_STRETCH = (0x7 << 28);

    public static final int VERT_STRETCH_RESERVED = 0xf1000000;

    /* DAC_CNTL bit constants */
    public static final int DAC_8BIT_EN = 0x00000100;

    public static final int DAC_4BPP_PIX_ORDER = 0x00000200;

    public static final int DAC_CRC_EN = 0x00080000;

    public static final int DAC_MASK_ALL = (0xff << 24);

    public static final int DAC_EXPAND_MODE = (1 << 14);

    public static final int DAC_VGA_ADR_EN = (1 << 13);

    public static final int DAC_RANGE_CNTL = (3 << 0);

    public static final int DAC_BLANKING = (1 << 2);

    public static final int DAC_CMP_EN = (1 << 3);

    /* DAC_CNTL2 bit constants */
    public static final int DAC2_CMP_EN = (1 << 7);

    /* GEN_RESET_CNTL bit constants */
    public static final int SOFT_RESET_GUI = 0x00000001;

    public static final int SOFT_RESET_VCLK = 0x00000100;

    public static final int SOFT_RESET_PCLK = 0x00000200;

    public static final int SOFT_RESET_ECP = 0x00000400;

    public static final int SOFT_RESET_DISPENG_XCLK = 0x00000800;

    /* MEM_CNTL bit constants */
    public static final int MEM_CTLR_STATUS_IDLE = 0x00000000;

    public static final int MEM_CTLR_STATUS_BUSY = 0x00100000;

    public static final int MEM_SEQNCR_STATUS_IDLE = 0x00000000;

    public static final int MEM_SEQNCR_STATUS_BUSY = 0x00200000;

    public static final int MEM_ARBITER_STATUS_IDLE = 0x00000000;

    public static final int MEM_ARBITER_STATUS_BUSY = 0x00400000;

    public static final int MEM_REQ_UNLOCK = 0x00000000;

    public static final int MEM_REQ_LOCK = 0x00800000;

    /* RBBM_SOFT_RESET bit constants */
    public static final int SOFT_RESET_CP = (1 << 0);

    public static final int SOFT_RESET_HI = (1 << 1);

    public static final int SOFT_RESET_SE = (1 << 2);

    public static final int SOFT_RESET_RE = (1 << 3);

    public static final int SOFT_RESET_PP = (1 << 4);

    public static final int SOFT_RESET_E2 = (1 << 5);

    public static final int SOFT_RESET_RB = (1 << 6);

    public static final int SOFT_RESET_HDP = (1 << 7);

    /* SURFACE_CNTL bit consants */
    public static final int SURF_TRANSLATION_DIS = (1 << 8);

    public static final int NONSURF_AP0_SWP_16BPP = (1 << 20);

    public static final int NONSURF_AP0_SWP_32BPP = (1 << 21);

    public static final int NONSURF_AP1_SWP_16BPP = (1 << 22);

    public static final int NONSURF_AP1_SWP_32BPP = (1 << 23);

    /* DEFAULT_SC_BOTTOM_RIGHT bit constants */
    public static final int DEFAULT_SC_RIGHT_MAX = (0x1fff << 0);

    public static final int DEFAULT_SC_BOTTOM_MAX = (0x1fff << 16);

    /* MM_INDEX bit constants */
    public static final int MM_APER = 0x80000000;

    /* CLR_CMP_CNTL bit constants */
    public static final int COMPARE_SRC_FALSE = 0x00000000;

    public static final int COMPARE_SRC_TRUE = 0x00000001;

    public static final int COMPARE_SRC_NOT_EQUAL = 0x00000004;

    public static final int COMPARE_SRC_EQUAL = 0x00000005;

    public static final int COMPARE_SRC_EQUAL_FLIP = 0x00000007;

    public static final int COMPARE_DST_FALSE = 0x00000000;

    public static final int COMPARE_DST_TRUE = 0x00000100;

    public static final int COMPARE_DST_NOT_EQUAL = 0x00000400;

    public static final int COMPARE_DST_EQUAL = 0x00000500;

    public static final int COMPARE_DESTINATION = 0x00000000;

    public static final int COMPARE_SOURCE = 0x01000000;

    public static final int COMPARE_SRC_AND_DST = 0x02000000;

    /* DP_CNTL bit constants */
    public static final int DST_X_RIGHT_TO_LEFT = 0x00000000;

    public static final int DST_X_LEFT_TO_RIGHT = 0x00000001;

    public static final int DST_Y_BOTTOM_TO_TOP = 0x00000000;

    public static final int DST_Y_TOP_TO_BOTTOM = 0x00000002;

    public static final int DST_X_MAJOR = 0x00000000;

    public static final int DST_Y_MAJOR = 0x00000004;

    public static final int DST_X_TILE = 0x00000008;

    public static final int DST_Y_TILE = 0x00000010;

    public static final int DST_LAST_PEL = 0x00000020;

    public static final int DST_TRAIL_X_RIGHT_TO_LEFT = 0x00000000;

    public static final int DST_TRAIL_X_LEFT_TO_RIGHT = 0x00000040;

    public static final int DST_TRAP_FILL_RIGHT_TO_LEFT = 0x00000000;

    public static final int DST_TRAP_FILL_LEFT_TO_RIGHT = 0x00000080;

    public static final int DST_BRES_SIGN = 0x00000100;

    public static final int DST_HOST_BIG_ENDIAN_EN = 0x00000200;

    public static final int DST_POLYLINE_NONLAST = 0x00008000;

    public static final int DST_RASTER_STALL = 0x00010000;

    public static final int DST_POLY_EDGE = 0x00040000;

    /* DP_CNTL_YDIR_XDIR_YMAJOR bit constants (short version of DP_CNTL) */
    public static final int DST_X_MAJOR_S = 0x00000000;

    public static final int DST_Y_MAJOR_S = 0x00000001;

    public static final int DST_Y_BOTTOM_TO_TOP_S = 0x00000000;

    public static final int DST_Y_TOP_TO_BOTTOM_S = 0x00008000;

    public static final int DST_X_RIGHT_TO_LEFT_S = 0x00000000;

    public static final int DST_X_LEFT_TO_RIGHT_S = 0x80000000;

    /* DP_DATATYPE bit constants */
    public static final int DST_8BPP = 0x00000002;

    public static final int DST_15BPP = 0x00000003;

    public static final int DST_16BPP = 0x00000004;

    public static final int DST_24BPP = 0x00000005;

    public static final int DST_32BPP = 0x00000006;

    public static final int DST_8BPP_RGB332 = 0x00000007;

    public static final int DST_8BPP_Y8 = 0x00000008;

    public static final int DST_8BPP_RGB8 = 0x00000009;

    public static final int DST_16BPP_VYUY422 = 0x0000000b;

    public static final int DST_16BPP_YVYU422 = 0x0000000c;

    public static final int DST_32BPP_AYUV444 = 0x0000000e;

    public static final int DST_16BPP_ARGB4444 = 0x0000000f;

    public static final int BRUSH_SOLIDCOLOR = 0x00000d00;

    public static final int SRC_MONO = 0x00000000;

    public static final int SRC_MONO_LBKGD = 0x00010000;

    public static final int SRC_DSTCOLOR = 0x00030000;

    public static final int BYTE_ORDER_MSB_TO_LSB = 0x00000000;

    public static final int BYTE_ORDER_LSB_TO_MSB = 0x40000000;

    public static final int DP_CONVERSION_TEMP = 0x80000000;

    public static final int HOST_BIG_ENDIAN_EN = (1 << 29);

    /* DP_GUI_MASTER_CNTL bit constants */
    public static final int GMC_SRC_PITCH_OFFSET_DEFAULT = 0x00000000;

    public static final int GMC_SRC_PITCH_OFFSET_LEAVE = 0x00000001;

    public static final int GMC_DST_PITCH_OFFSET_DEFAULT = 0x00000000;

    public static final int GMC_DST_PITCH_OFFSET_LEAVE = 0x00000002;

    public static final int GMC_SRC_CLIP_DEFAULT = 0x00000000;

    public static final int GMC_SRC_CLIP_LEAVE = 0x00000004;

    public static final int GMC_DST_CLIP_DEFAULT = 0x00000000;

    public static final int GMC_DST_CLIP_LEAVE = 0x00000008;

    public static final int GMC_BRUSH_8x8MONO = 0x00000000;

    public static final int GMC_BRUSH_8x8MONO_LBKGD = 0x00000010;

    public static final int GMC_BRUSH_8x1MONO = 0x00000020;

    public static final int GMC_BRUSH_8x1MONO_LBKGD = 0x00000030;

    public static final int GMC_BRUSH_1x8MONO = 0x00000040;

    public static final int GMC_BRUSH_1x8MONO_LBKGD = 0x00000050;

    public static final int GMC_BRUSH_32x1MONO = 0x00000060;

    public static final int GMC_BRUSH_32x1MONO_LBKGD = 0x00000070;

    public static final int GMC_BRUSH_32x32MONO = 0x00000080;

    public static final int GMC_BRUSH_32x32MONO_LBKGD = 0x00000090;

    public static final int GMC_BRUSH_8x8COLOR = 0x000000a0;

    public static final int GMC_BRUSH_8x1COLOR = 0x000000b0;

    public static final int GMC_BRUSH_1x8COLOR = 0x000000c0;

    public static final int GMC_BRUSH_SOLID_COLOR = 0x000000d0;

    public static final int GMC_DST_8BPP = 0x00000200;

    public static final int GMC_DST_15BPP = 0x00000300;

    public static final int GMC_DST_16BPP = 0x00000400;

    public static final int GMC_DST_24BPP = 0x00000500;

    public static final int GMC_DST_32BPP = 0x00000600;

    public static final int GMC_DST_8BPP_RGB332 = 0x00000700;

    public static final int GMC_DST_8BPP_Y8 = 0x00000800;

    public static final int GMC_DST_8BPP_RGB8 = 0x00000900;

    public static final int GMC_DST_16BPP_VYUY422 = 0x00000b00;

    public static final int GMC_DST_16BPP_YVYU422 = 0x00000c00;

    public static final int GMC_DST_32BPP_AYUV444 = 0x00000e00;

    public static final int GMC_DST_16BPP_ARGB4444 = 0x00000f00;

    public static final int GMC_SRC_MONO = 0x00000000;

    public static final int GMC_SRC_MONO_LBKGD = 0x00001000;

    public static final int GMC_SRC_DSTCOLOR = 0x00003000;

    public static final int GMC_BYTE_ORDER_MSB_TO_LSB = 0x00000000;

    public static final int GMC_BYTE_ORDER_LSB_TO_MSB = 0x00004000;

    public static final int GMC_DP_CONVERSION_TEMP_9300 = 0x00008000;

    public static final int GMC_DP_CONVERSION_TEMP_6500 = 0x00000000;

    public static final int GMC_DP_SRC_RECT = 0x02000000;

    public static final int GMC_DP_SRC_HOST = 0x03000000;

    public static final int GMC_DP_SRC_HOST_BYTEALIGN = 0x04000000;

    public static final int GMC_3D_FCN_EN_CLR = 0x00000000;

    public static final int GMC_3D_FCN_EN_SET = 0x08000000;

    public static final int GMC_DST_CLR_CMP_FCN_LEAVE = 0x00000000;

    public static final int GMC_DST_CLR_CMP_FCN_CLEAR = 0x10000000;

    public static final int GMC_AUX_CLIP_LEAVE = 0x00000000;

    public static final int GMC_AUX_CLIP_CLEAR = 0x20000000;

    public static final int GMC_WRITE_MASK_LEAVE = 0x00000000;

    public static final int GMC_WRITE_MASK_SET = 0x40000000;

    public static final int GMC_CLR_CMP_CNTL_DIS = (1 << 28);

    public static final int GMC_SRC_DATATYPE_COLOR = (3 << 12);

    public static final int ROP3_S = 0x00cc0000;

    public static final int ROP3_SRCCOPY = 0x00cc0000;

    public static final int ROP3_P = 0x00f00000;

    public static final int ROP3_PATCOPY = 0x00f00000;

    public static final int DP_SRC_SOURCE_MASK = (7 << 24);

    public static final int GMC_BRUSH_NONE = (15 << 4);

    public static final int DP_SRC_SOURCE_MEMORY = (2 << 24);

    public static final int GMC_BRUSH_SOLIDCOLOR = 0x000000d0;

    /* DP_MIX bit constants */
    public static final int DP_SRC_RECT = 0x00000200;

    public static final int DP_SRC_HOST = 0x00000300;

    public static final int DP_SRC_HOST_BYTEALIGN = 0x00000400;

    /* MPLL_CNTL bit constants */
    public static final int MPLL_RESET = 0x00000001;

    /* MDLL_CKO bit constants */
    public static final int MCKOA_SLEEP = 0x00000001;

    public static final int MCKOA_RESET = 0x00000002;

    public static final int MCKOA_REF_SKEW_MASK = 0x00000700;

    public static final int MCKOA_FB_SKEW_MASK = 0x00007000;

    /* MDLL_RDCKA bit constants */
    public static final int MRDCKA0_SLEEP = 0x00000001;

    public static final int MRDCKA0_RESET = 0x00000002;

    public static final int MRDCKA1_SLEEP = 0x00010000;

    public static final int MRDCKA1_RESET = 0x00020000;

    /* VCLK_ECP_CNTL constants */
    public static final int PIXCLK_ALWAYS_ONb = 0x00000040;

    public static final int PIXCLK_DAC_ALWAYS_ONb = 0x00000080;
    public static final int VCLK_SRC_SEL_MASK = (3 << 0);
    public static final int VCLK_SRC_CPU_CLK = (0 << 0);
    public static final int VCLK_SRC_PSCAN_CLK = (1 << 0);
    public static final int VCLK_SRC_BYTE_CLK = (2 << 0);
    public static final int VCLK_SRC_PPLL_CLK = (3 << 0);
    public static final int ECP_DIV_SHIFT = 8;
    public static final int ECP_DIV_MASK = (3 << 8);
    public static final int ECP_DIV_VCLK = (0 << 8);
    public static final int ECP_DIV_VCLK_2 = (1 << 8);

    /* BUS_CNTL1 constants */
    public static final int BUS_CNTL1_MOBILE_PLATFORM_SEL_MASK = 0x0c000000;

    public static final int BUS_CNTL1_MOBILE_PLATFORM_SEL_SHIFT = 26;

    public static final int BUS_CNTL1_AGPCLK_VALID = 0x80000000;

    /* PLL_PWRMGT_CNTL constants */
    public static final int PLL_PWRMGT_CNTL_SPLL_TURNOFF = 0x00000002;

    public static final int PLL_PWRMGT_CNTL_PPLL_TURNOFF = 0x00000004;

    public static final int PLL_PWRMGT_CNTL_P2PLL_TURNOFF = 0x00000008;

    public static final int PLL_PWRMGT_CNTL_TVPLL_TURNOFF = 0x00000010;

    public static final int PLL_PWRMGT_CNTL_MOBILE_SU = 0x00010000;

    public static final int PLL_PWRMGT_CNTL_SU_SCLK_USE_BCLK = 0x00020000;

    public static final int PLL_PWRMGT_CNTL_SU_MCLK_USE_BCLK = 0x00040000;

    /* TV_DAC_CNTL constants */
    public static final int TV_DAC_CNTL_BGSLEEP = 0x00000040;

    public static final int TV_DAC_CNTL_DETECT = 0x00000010;

    public static final int TV_DAC_CNTL_BGADJ_MASK = 0x000f0000;

    public static final int TV_DAC_CNTL_DACADJ_MASK = 0x00f00000;

    public static final int TV_DAC_CNTL_BGADJ__SHIFT = 16;

    public static final int TV_DAC_CNTL_DACADJ__SHIFT = 20;

    public static final int TV_DAC_CNTL_RDACPD = 0x01000000;

    public static final int TV_DAC_CNTL_GDACPD = 0x02000000;

    public static final int TV_DAC_CNTL_BDACPD = 0x04000000;

    /* DISP_MISC_CNTL constants */
    public static final int DISP_MISC_CNTL_SOFT_RESET_GRPH_PP = (1 << 0);

    public static final int DISP_MISC_CNTL_SOFT_RESET_SUBPIC_PP = (1 << 1);

    public static final int DISP_MISC_CNTL_SOFT_RESET_OV0_PP = (1 << 2);

    public static final int DISP_MISC_CNTL_SOFT_RESET_GRPH_SCLK = (1 << 4);

    public static final int DISP_MISC_CNTL_SOFT_RESET_SUBPIC_SCLK = (1 << 5);

    public static final int DISP_MISC_CNTL_SOFT_RESET_OV0_SCLK = (1 << 6);

    public static final int DISP_MISC_CNTL_SOFT_RESET_GRPH2_PP = (1 << 12);

    public static final int DISP_MISC_CNTL_SOFT_RESET_GRPH2_SCLK = (1 << 15);

    public static final int DISP_MISC_CNTL_SOFT_RESET_LVDS = (1 << 16);

    public static final int DISP_MISC_CNTL_SOFT_RESET_TMDS = (1 << 17);

    public static final int DISP_MISC_CNTL_SOFT_RESET_DIG_TMDS = (1 << 18);

    public static final int DISP_MISC_CNTL_SOFT_RESET_TV = (1 << 19);

    /* DISP_PWR_MAN constants */
    public static final int DISP_PWR_MAN_DISP_PWR_MAN_D3_CRTC_EN = (1 << 0);

    public static final int DISP_PWR_MAN_DISP2_PWR_MAN_D3_CRTC2_EN = (1 << 4);

    public static final int DISP_PWR_MAN_DISP_D3_RST = (1 << 16);

    public static final int DISP_PWR_MAN_DISP_D3_REG_RST = (1 << 17);

    public static final int DISP_PWR_MAN_DISP_D3_GRPH_RST = (1 << 18);

    public static final int DISP_PWR_MAN_DISP_D3_SUBPIC_RST = (1 << 19);

    public static final int DISP_PWR_MAN_DISP_D3_OV0_RST = (1 << 20);

    public static final int DISP_PWR_MAN_DISP_D1D2_GRPH_RST = (1 << 21);

    public static final int DISP_PWR_MAN_DISP_D1D2_SUBPIC_RST = (1 << 22);

    public static final int DISP_PWR_MAN_DISP_D1D2_OV0_RST = (1 << 23);

    public static final int DISP_PWR_MAN_DIG_TMDS_ENABLE_RST = (1 << 24);

    public static final int DISP_PWR_MAN_TV_ENABLE_RST = (1 << 25);

    public static final int DISP_PWR_MAN_AUTO_PWRUP_EN = (1 << 26);

    /* masks */

    public static final int CONFIG_MEMSIZE_MASK = 0x1f000000;

    public static final int MEM_CFG_TYPE = 0x40000000;

    public static final int DST_OFFSET_MASK = 0x003fffff;

    public static final int DST_PITCH_MASK = 0x3fc00000;

    public static final int DEFAULT_TILE_MASK = 0xc0000000;

    public static final int PPLL_DIV_SEL_MASK = 0x00000300;

    public static final int PPLL_RESET = (1 << 0);
    public static final int PPLL_SLEEP = (1 << 1);

    public static final int PPLL_ATOMIC_UPDATE_EN = 0x00010000;

    public static final int PPLL_REF_DIV_MASK = 0x000003ff;

    public static final int PPLL_FB3_DIV_MASK = 0x000007ff;

    public static final int PPLL_POST3_DIV_MASK = 0x00070000;

    public static final int PPLL_ATOMIC_UPDATE_R = 0x00008000;

    public static final int PPLL_ATOMIC_UPDATE_W = 0x00008000;

    public static final int PPLL_VGA_ATOMIC_UPDATE_EN = 0x00020000;

    public static final int GUI_ACTIVE = 0x80000000;

    public static final byte[] BIOS_ROM_SIGNATURE = {0x55, (byte) 0xAA};
    public static final byte[] ATI_ROM_SIGNATURE = {'7', '6', '1', '2', '9', '5', '5', '2', '0'};
}
