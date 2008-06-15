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

package org.jnode.driver.video.ati.mach64;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface Mach64Constants {

    /* NON-GUI sparse IO register offsets */

    public static final int sioCONFIG_CHIP_ID = 0x1B;
    public static final int sioCONFIG_CNTL = 0x1A;
    public static final int sioSCRATCH_REG0 = 0x10;
    public static final int sioSCRATCH_REG1 = 0x11;
    public static final int sioCONFIG_STAT0 = 0x1C;
    public static final int sioMEM_CNTL = 0x14;
    public static final int sioDAC_REGS = 0x17;
    public static final int sioDAC_CNTL = 0x18;
    public static final int sioGEN_TEST_CNTL = 0x19;
    public static final int sioCLOCK_CNTL = 0x12;
    public static final int sioCRTC_GEN_CNTL = 0x07;

    /* NON-GUI MEMORY MAPPED Registers - expressed in BYTE offsets */

    public static final int CRTC_H_TOTAL_DISP = 0x0000; /* Dword offset 00 */
    public static final int CRTC_H_SYNC_STRT_WID = 0x0004; /* Dword offset 01 */
    public static final int CRTC_V_TOTAL_DISP = 0x0008; /* Dword offset 02 */
    public static final int CRTC_V_SYNC_STRT_WID = 0x000C; /* Dword offset 03 */
    public static final int CRTC_VLINE_CRNT_VLINE = 0x0010; /* Dword offset 04 */
    public static final int CRTC_OFF_PITCH = 0x0014; /* Dword offset 05 */
    public static final int CRTC_INT_CNTL = 0x0018; /* Dword offset 06 */
    public static final int CRTC_GEN_CNTL = 0x001C; /* Dword offset 07 */

    public static final int DSP_CONFIG = 0x0020; /* Dword offset 08 */
    public static final int DSP_ON_OFF = 0x0024; /* Dword offset 09 */

    public static final int SHARED_CNTL = 0x0038; /* Dword offset 0E */

    public static final int OVR_CLR = 0x0040; /* Dword offset 10 */
    public static final int OVR_WID_LEFT_RIGHT = 0x0044; /* Dword offset 11 */
    public static final int OVR_WID_TOP_BOTTOM = 0x0048; /* Dword offset 12 */

    public static final int CUR_CLR0 = 0x0060; /* Dword offset 18 */
    public static final int CUR_CLR1 = 0x0064; /* Dword offset 19 */
    public static final int CUR_OFFSET = 0x0068; /* Dword offset 1A */
    public static final int CUR_HORZ_VERT_POSN = 0x006C; /* Dword offset 1B */
    public static final int CUR_HORZ_VERT_OFF = 0x0070; /* Dword offset 1C */

    public static final int HW_DEBUG = 0x007C; /* Dword offset 1F */

    public static final int SCRATCH_REG0 = 0x0080; /* Dword offset 20 */
    public static final int SCRATCH_REG1 = 0x0084; /* Dword offset 21 */

    public static final int CLOCK_CNTL = 0x0090; /* Dword offset 24 */

    public static final int BUS_CNTL = 0x00A0; /* Dword offset 28 */

    public static final int MEM_CNTL = 0x00B0; /* Dword offset 2C */

    public static final int MEM_VGA_WP_SEL = 0x00B4; /* Dword offset 2D */
    public static final int MEM_VGA_RP_SEL = 0x00B8; /* Dword offset 2E */

    public static final int DAC_REGS = 0x00C0; /* Dword offset 30 */
    public static final int DAC_W_INDEX = 0x00C0; /* Dword offset 30 */
    public static final int DAC_DATA = 0x00C1; /* Dword offset 30 */
    public static final int DAC_MASK = 0x00C2; /* Dword offset 30 */
    public static final int DAC_R_INDEX = 0x00C3; /* Dword offset 30 */
    public static final int DAC_CNTL = 0x00C4; /* Dword offset 31 */

    public static final int GEN_TEST_CNTL = 0x00D0; /* Dword offset 34 */

    public static final int CONFIG_CNTL = 0x00DC; /*
                                                     * Dword offset 37 (CT, ET,
                                                     * VT)
                                                     */
    public static final int CONFIG_CHIP_ID = 0x00E0; /* Dword offset 38 */
    public static final int CONFIG_STAT0 = 0x00E4; /* Dword offset 39 */
    public static final int CONFIG_STAT1 = 0x00E8; /* Dword offset 3A */

    /* GUI MEMORY MAPPED Registers */

    public static final int DST_OFF_PITCH = 0x0100; /* Dword offset 40 */
    public static final int DST_X = 0x0104; /* Dword offset 41 */
    public static final int DST_Y = 0x0108; /* Dword offset 42 */
    public static final int DST_Y_X = 0x010C; /* Dword offset 43 */
    public static final int DST_WIDTH = 0x0110; /* Dword offset 44 */
    public static final int DST_HEIGHT = 0x0114; /* Dword offset 45 */
    public static final int DST_HEIGHT_WIDTH = 0x0118; /* Dword offset 46 */
    public static final int DST_X_WIDTH = 0x011C; /* Dword offset 47 */
    public static final int DST_BRES_LNTH = 0x0120; /* Dword offset 48 */
    public static final int DST_BRES_ERR = 0x0124; /* Dword offset 49 */
    public static final int DST_BRES_INC = 0x0128; /* Dword offset 4A */
    public static final int DST_BRES_DEC = 0x012C; /* Dword offset 4B */
    public static final int DST_CNTL = 0x0130; /* Dword offset 4C */

    public static final int SRC_OFF_PITCH = 0x0180; /* Dword offset 60 */
    public static final int SRC_X = 0x0184; /* Dword offset 61 */
    public static final int SRC_Y = 0x0188; /* Dword offset 62 */
    public static final int SRC_Y_X = 0x018C; /* Dword offset 63 */
    public static final int SRC_WIDTH1 = 0x0190; /* Dword offset 64 */
    public static final int SRC_HEIGHT1 = 0x0194; /* Dword offset 65 */
    public static final int SRC_HEIGHT1_WIDTH1 = 0x0198; /* Dword offset 66 */
    public static final int SRC_X_START = 0x019C; /* Dword offset 67 */
    public static final int SRC_Y_START = 0x01A0; /* Dword offset 68 */
    public static final int SRC_Y_X_START = 0x01A4; /* Dword offset 69 */
    public static final int SRC_WIDTH2 = 0x01A8; /* Dword offset 6A */
    public static final int SRC_HEIGHT2 = 0x01AC; /* Dword offset 6B */
    public static final int SRC_HEIGHT2_WIDTH2 = 0x01B0; /* Dword offset 6C */
    public static final int SRC_CNTL = 0x01B4; /* Dword offset 6D */

    public static final int HOST_DATA0 = 0x0200; /* Dword offset 80 */
    public static final int HOST_DATA1 = 0x0204; /* Dword offset 81 */
    public static final int HOST_DATA2 = 0x0208; /* Dword offset 82 */
    public static final int HOST_DATA3 = 0x020C; /* Dword offset 83 */
    public static final int HOST_DATA4 = 0x0210; /* Dword offset 84 */
    public static final int HOST_DATA5 = 0x0214; /* Dword offset 85 */
    public static final int HOST_DATA6 = 0x0218; /* Dword offset 86 */
    public static final int HOST_DATA7 = 0x021C; /* Dword offset 87 */
    public static final int HOST_DATA8 = 0x0220; /* Dword offset 88 */
    public static final int HOST_DATA9 = 0x0224; /* Dword offset 89 */
    public static final int HOST_DATAA = 0x0228; /* Dword offset 8A */
    public static final int HOST_DATAB = 0x022C; /* Dword offset 8B */
    public static final int HOST_DATAC = 0x0230; /* Dword offset 8C */
    public static final int HOST_DATAD = 0x0234; /* Dword offset 8D */
    public static final int HOST_DATAE = 0x0238; /* Dword offset 8E */
    public static final int HOST_DATAF = 0x023C; /* Dword offset 8F */
    public static final int HOST_CNTL = 0x0240; /* Dword offset 90 */

    public static final int PAT_REG0 = 0x0280; /* Dword offset A0 */
    public static final int PAT_REG1 = 0x0284; /* Dword offset A1 */
    public static final int PAT_CNTL = 0x0288; /* Dword offset A2 */

    public static final int SC_LEFT = 0x02A0; /* Dword offset A8 */
    public static final int SC_RIGHT = 0x02A4; /* Dword offset A9 */
    public static final int SC_LEFT_RIGHT = 0x02A8; /* Dword offset AA */
    public static final int SC_TOP = 0x02AC; /* Dword offset AB */
    public static final int SC_BOTTOM = 0x02B0; /* Dword offset AC */
    public static final int SC_TOP_BOTTOM = 0x02B4; /* Dword offset AD */

    public static final int DP_BKGD_CLR = 0x02C0; /* Dword offset B0 */
    public static final int DP_FRGD_CLR = 0x02C4; /* Dword offset B1 */
    public static final int DP_WRITE_MASK = 0x02C8; /* Dword offset B2 */
    public static final int DP_CHAIN_MASK = 0x02CC; /* Dword offset B3 */
    public static final int DP_PIX_WIDTH = 0x02D0; /* Dword offset B4 */
    public static final int DP_MIX = 0x02D4; /* Dword offset B5 */
    public static final int DP_SRC = 0x02D8; /* Dword offset B6 */

    public static final int CLR_CMP_CLR = 0x0300; /* Dword offset C0 */
    public static final int CLR_CMP_MASK = 0x0304; /* Dword offset C1 */
    public static final int CLR_CMP_CNTL = 0x0308; /* Dword offset C2 */

    public static final int FIFO_STAT = 0x0310; /* Dword offset C4 */

    public static final int CONTEXT_MASK = 0x0320; /* Dword offset C8 */
    public static final int CONTEXT_LOAD_CNTL = 0x032C; /* Dword offset CB */

    public static final int GUI_TRAJ_CNTL = 0x0330; /* Dword offset CC */
    public static final int GUI_STAT = 0x0338; /* Dword offset CE */

    /* CRTC control values */

    public static final int CRTC_H_SYNC_NEG = 0x00200000;
    public static final int CRTC_V_SYNC_NEG = 0x00200000;

    public static final int CRTC_DBL_SCAN_EN = 0x00000001;
    public static final int CRTC_INTERLACE_EN = 0x00000002;
    public static final int CRTC_HSYNC_DIS = 0x00000004;
    public static final int CRTC_VSYNC_DIS = 0x00000008;
    public static final int CRTC_CSYNC_EN = 0x00000010;
    public static final int CRTC_PIX_BY_2_EN = 0x00000020;
    public static final int CRTC_DISPLAY_DIS = 0x00000040;

    public static final int CRTC_PIX_WIDTH = 0x00000700;
    public static final int CRTC_PIX_WIDTH_4BPP = 0x00000100;
    public static final int CRTC_PIX_WIDTH_8BPP = 0x00000200;
    public static final int CRTC_PIX_WIDTH_15BPP = 0x00000300;
    public static final int CRTC_PIX_WIDTH_16BPP = 0x00000400;
    public static final int CRTC_PIX_WIDTH_24BPP = 0x00000500;
    public static final int CRTC_PIX_WIDTH_32BPP = 0x00000600;

    public static final int CRTC_BYTE_PIX_ORDER = 0x00000800;
    public static final int CRTC_PIX_ORDER_MSN_LSN = 0x00000000;
    public static final int CRTC_PIX_ORDER_LSN_MSN = 0x00000800;

    public static final int CRTC_FIFO_LWM = 0x000f0000;
    public static final int CRTC_LOCK_REGS = 0x00400000;
    public static final int CRTC_EXT_DISP_EN = 0x01000000;
    public static final int CRTC_EXT_EN = 0x02000000;

    public static final int CRTC_CRNT_VLINE = 0x07f00000;
    public static final int CRTC_VBLANK = 0x00000001;

    /* DAC control values */

    public static final int DAC_EXT_SEL_RS2 = 0x01;
    public static final int DAC_EXT_SEL_RS3 = 0x02;
    public static final int DAC_8BIT_EN = 0x00000100;
    public static final int DAC_PIX_DLY_MASK = 0x00000600;
    public static final int DAC_PIX_DLY_0NS = 0x00000000;
    public static final int DAC_PIX_DLY_2NS = 0x00000200;
    public static final int DAC_PIX_DLY_4NS = 0x00000400;
    public static final int DAC_BLANK_ADJ_MASK = 0x00001800;
    public static final int DAC_BLANK_ADJ_0 = 0x00000000;
    public static final int DAC_BLANK_ADJ_1 = 0x00000800;
    public static final int DAC_BLANK_ADJ_2 = 0x00001000;

    /* Mix control values */

    public static final int MIX_NOT_DST = 0x0000;
    public static final int MIX_0 = 0x0001;
    public static final int MIX_1 = 0x0002;
    public static final int MIX_DST = 0x0003;
    public static final int MIX_NOT_SRC = 0x0004;
    public static final int MIX_XOR = 0x0005;
    public static final int MIX_XNOR = 0x0006;
    public static final int MIX_SRC = 0x0007;
    public static final int MIX_NAND = 0x0008;
    public static final int MIX_NOT_SRC_OR_DST = 0x0009;
    public static final int MIX_SRC_OR_NOT_DST = 0x000a;
    public static final int MIX_OR = 0x000b;
    public static final int MIX_AND = 0x000c;
    public static final int MIX_SRC_AND_NOT_DST = 0x000d;
    public static final int MIX_NOT_SRC_AND_DST = 0x000e;
    public static final int MIX_NOR = 0x000f;

    /* Maximum engine dimensions */
    public static final int ENGINE_MIN_X = 0;
    public static final int ENGINE_MIN_Y = 0;
    public static final int ENGINE_MAX_X = 4095;
    public static final int ENGINE_MAX_Y = 16383;

    /* Mach64 engine bit constants - these are typically ORed together */

    /* HW_DEBUG register constants */
    /* For RagePro only... */
    public static final int AUTO_FF_DIS = 0x000001000;
    public static final int AUTO_BLKWRT_DIS = 0x000002000;

    /* BUS_CNTL register constants */
    public static final int BUS_FIFO_ERR_ACK = 0x00200000;
    public static final int BUS_HOST_ERR_ACK = 0x00800000;
    public static final int BUS_APER_REG_DIS = 0x00000010;

    /* GEN_TEST_CNTL register constants */
    public static final int GEN_OVR_OUTPUT_EN = 0x20;
    public static final int HWCURSOR_ENABLE = 0x80;
    public static final int GUI_ENGINE_ENABLE = 0x100;
    public static final int BLOCK_WRITE_ENABLE = 0x200;

    /* DSP_CONFIG register constants */
    public static final int DSP_XCLKS_PER_QW = 0x00003fff;
    public static final int DSP_LOOP_LATENCY = 0x000f0000;
    public static final int DSP_PRECISION = 0x00700000;

    /* DSP_ON_OFF register constants */
    public static final int DSP_OFF = 0x000007ff;
    public static final int DSP_ON = 0x07ff0000;

    /* SHARED_CNTL register constants */
    public static final int CTD_FIFO5 = 0x01000000;

    /* CLOCK_CNTL register constants */
    public static final int CLOCK_SEL = 0x0f;
    public static final int CLOCK_DIV = 0x30;
    public static final int CLOCK_DIV1 = 0x00;
    public static final int CLOCK_DIV2 = 0x10;
    public static final int CLOCK_DIV4 = 0x20;
    public static final int CLOCK_STROBE = 0x40;
    public static final int PLL_WR_EN = 0x02;

    /* PLL registers */
    public static final int PLL_MACRO_CNTL = 0x01;
    public static final int PLL_REF_DIV = 0x02;
    public static final int PLL_GEN_CNTL = 0x03;
    public static final int MCLK_FB_DIV = 0x04;
    public static final int PLL_VCLK_CNTL = 0x05;
    public static final int VCLK_POST_DIV = 0x06;
    public static final int VCLK0_FB_DIV = 0x07;
    public static final int VCLK1_FB_DIV = 0x08;
    public static final int VCLK2_FB_DIV = 0x09;
    public static final int VCLK3_FB_DIV = 0x0A;
    public static final int PLL_XCLK_CNTL = 0x0B;
    public static final int PLL_TEST_CTRL = 0x0E;
    public static final int PLL_TEST_COUNT = 0x0F;

    /* Fields in PLL registers */
    public static final int PLL_PC_GAIN = 0x07;
    public static final int PLL_VC_GAIN = 0x18;
    public static final int PLL_DUTY_CYC = 0xE0;
    public static final int PLL_OVERRIDE = 0x01;
    public static final int PLL_MCLK_RST = 0x02;
    public static final int OSC_EN = 0x04;
    public static final int EXT_CLK_EN = 0x08;
    public static final int MCLK_SRC_SEL = 0x70;
    public static final int EXT_CLK_CNTL = 0x80;
    public static final int VCLK_SRC_SEL = 0x03;
    public static final int PLL_VCLK_RST = 0x04;
    public static final int VCLK_INVERT = 0x08;
    public static final int VCLK0_POST = 0x03;
    public static final int VCLK1_POST = 0x0C;
    public static final int VCLK2_POST = 0x30;
    public static final int VCLK3_POST = 0xC0;

    /* CONFIG_CNTL register constants */
    public static final int APERTURE_4M_ENABLE = 1;
    public static final int APERTURE_8M_ENABLE = 2;
    public static final int VGA_APERTURE_ENABLE = 4;

    /* CONFIG_STAT0 register constants (GX, CX) */
    public static final int CFG_BUS_TYPE = 0x00000007;
    public static final int CFG_MEM_TYPE = 0x00000038;
    public static final int CFG_INIT_DAC_TYPE = 0x00000e00;

    /* CONFIG_STAT0 register constants (CT, ET, VT) */
    public static final int CFG_MEM_TYPE_xT = 0x00000007;

    public static final int ISA = 0;
    public static final int EISA = 1;
    public static final int LOCAL_BUS = 6;
    public static final int PCI = 7;

    /* Memory types for GX, CX */
    public static final int DRAMx4 = 0;
    public static final int VRAMx16 = 1;
    public static final int VRAMx16ssr = 2;
    public static final int DRAMx16 = 3;
    public static final int GraphicsDRAMx16 = 4;
    public static final int EnhancedVRAMx16 = 5;
    public static final int EnhancedVRAMx16ssr = 6;

    /* Memory types for CT, ET, VT, GT */
    public static final int DRAM = 1;
    public static final int EDO_DRAM = 2;
    public static final int PSEUDO_EDO = 3;
    public static final int SDRAM = 4;
    public static final int SGRAM = 5;

    public static final int DAC_INTERNAL = 0x00;
    public static final int DAC_IBMRGB514 = 0x01;
    public static final int DAC_ATI68875 = 0x02;
    public static final int DAC_TVP3026_A = 0x72;
    public static final int DAC_BT476 = 0x03;
    public static final int DAC_BT481 = 0x04;
    public static final int DAC_ATT20C491 = 0x14;
    public static final int DAC_SC15026 = 0x24;
    public static final int DAC_MU9C1880 = 0x34;
    public static final int DAC_IMSG174 = 0x44;
    public static final int DAC_ATI68860_B = 0x05;
    public static final int DAC_ATI68860_C = 0x15;
    public static final int DAC_TVP3026_B = 0x75;
    public static final int DAC_STG1700 = 0x06;
    public static final int DAC_ATT498 = 0x16;
    public static final int DAC_STG1702 = 0x07;
    public static final int DAC_SC15021 = 0x17;
    public static final int DAC_ATT21C498 = 0x27;
    public static final int DAC_STG1703 = 0x37;
    public static final int DAC_CH8398 = 0x47;
    public static final int DAC_ATT20C408 = 0x57;

    public static final int CLK_ATI18818_0 = 0;
    public static final int CLK_ATI18818_1 = 1;
    public static final int CLK_STG1703 = 2;
    public static final int CLK_CH8398 = 3;
    public static final int CLK_INTERNAL = 4;
    public static final int CLK_ATT20C408 = 5;
    public static final int CLK_IBMRGB514 = 6;

    /* MEM_CNTL register constants */
    public static final int MEM_SIZE_ALIAS = 0x00000007;
    public static final int MEM_SIZE_512K = 0x00000000;
    public static final int MEM_SIZE_1M = 0x00000001;
    public static final int MEM_SIZE_2M = 0x00000002;
    public static final int MEM_SIZE_4M = 0x00000003;
    public static final int MEM_SIZE_6M = 0x00000004;
    public static final int MEM_SIZE_8M = 0x00000005;
    public static final int MEM_SIZE_16M = 0x00000006;
    public static final int MEM_SIZE_ALIAS_GTB = 0x0000000F;
    public static final int MEM_SIZE_2M_GTB = 0x00000003;
    public static final int MEM_SIZE_4M_GTB = 0x00000007;
    public static final int MEM_SIZE_6M_GTB = 0x00000009;
    public static final int MEM_SIZE_8M_GTB = 0x0000000B;
    public static final int MEM_SIZE_16M_GTB = 0x0000000F;
    public static final int MEM_TRP = 0x00000300;
    public static final int MEM_TRCD = 0x00000C00;
    public static final int MEM_TCRD = 0x00001000;
    public static final int MEM_TRAS = 0x00070000;
    public static final int MEM_BNDRY = 0x00030000;
    public static final int MEM_BNDRY_0K = 0x00000000;
    public static final int MEM_BNDRY_256K = 0x00010000;
    public static final int MEM_BNDRY_512K = 0x00020000;
    public static final int MEM_BNDRY_1M = 0x00030000;
    public static final int MEM_BNDRY_EN = 0x00040000;

    /* ATI PCI constants */
    public static final int PCI_ATI_VENDOR_ID = 0x1002;
    public static final int PCI_MACH64_GX_ID = 0x4758;
    public static final int PCI_MACH64_CX_ID = 0x4358;
    public static final int PCI_MACH64_CT_ID = 0x4354;
    public static final int PCI_MACH64_ET_ID = 0x4554;
    public static final int PCI_MACH64_VT_ID = 0x5654;
    public static final int PCI_MACH64_VU_ID = 0x5655;
    public static final int PCI_MACH64_GT_ID = 0x4754;
    public static final int PCI_MACH64_GU_ID = 0x4755;
    public static final int PCI_MACH64_GB_ID = 0x4742;
    public static final int PCI_MACH64_GD_ID = 0x4744;
    public static final int PCI_MACH64_GI_ID = 0x4749;
    public static final int PCI_MACH64_GP_ID = 0x4750;
    public static final int PCI_MACH64_GQ_ID = 0x4751;
    public static final int PCI_MACH64_VV_ID = 0x5656;
    public static final int PCI_MACH64_GV_ID = 0x4756;
    public static final int PCI_MACH64_GW_ID = 0x4757;
    public static final int PCI_MACH64_GZ_ID = 0x475A;
    public static final int PCI_MACH64_LD_ID = 0x4C44;
    public static final int PCI_MACH64_LG_ID = 0x4C47;
    public static final int PCI_MACH64_LB_ID = 0x4C42;
    public static final int PCI_MACH64_LI_ID = 0x4C49;
    public static final int PCI_MACH64_LP_ID = 0x4C50;

    /* CONFIG_CHIP_ID register constants */
    public static final int CFG_CHIP_TYPE = 0x0000FFFF;
    public static final int CFG_CHIP_CLASS = 0x00FF0000;
    public static final int CFG_CHIP_REV = 0xFF000000;
    public static final int CFG_CHIP_VERSION = 0x07000000;
    public static final int CFG_CHIP_FOUNDRY = 0x38000000;
    public static final int CFG_CHIP_REVISION = 0xC0000000;

    /* Chip IDs read from CONFIG_CHIP_ID */
    public static final int MACH64_GX_ID = 0xD7;
    public static final int MACH64_CX_ID = 0x57;
    public static final int MACH64_CT_ID = 0x4354;
    public static final int MACH64_ET_ID = 0x4554;
    public static final int MACH64_VT_ID = 0x5654;
    public static final int MACH64_VU_ID = 0x5655;
    public static final int MACH64_GT_ID = 0x4754;
    public static final int MACH64_GU_ID = 0x4755;
    public static final int MACH64_GB_ID = 0x4742;
    public static final int MACH64_GD_ID = 0x4744;
    public static final int MACH64_GI_ID = 0x4749;
    public static final int MACH64_GP_ID = 0x4750;
    public static final int MACH64_GQ_ID = 0x4751;
    public static final int MACH64_VV_ID = 0x5656;
    public static final int MACH64_GV_ID = 0x4756;
    public static final int MACH64_GW_ID = 0x4757;
    public static final int MACH64_GZ_ID = 0x475A;
    public static final int MACH64_LD_ID = 0x4C44;
    public static final int MACH64_LG_ID = 0x4C47;
    public static final int MACH64_LB_ID = 0x4C42;
    public static final int MACH64_LI_ID = 0x4C49;
    public static final int MACH64_LP_ID = 0x4C50;
    public static final int MACH64_UNKNOWN_ID = 0x0000;

    /* DST_CNTL register constants */
    public static final int DST_X_RIGHT_TO_LEFT = 0;
    public static final int DST_X_LEFT_TO_RIGHT = 1;
    public static final int DST_Y_BOTTOM_TO_TOP = 0;
    public static final int DST_Y_TOP_TO_BOTTOM = 2;
    public static final int DST_X_MAJOR = 0;
    public static final int DST_Y_MAJOR = 4;
    public static final int DST_X_TILE = 8;
    public static final int DST_Y_TILE = 0x10;
    public static final int DST_LAST_PEL = 0x20;
    public static final int DST_POLYGON_ENABLE = 0x40;
    public static final int DST_24_ROTATION_ENABLE = 0x80;

    /* SRC_CNTL register constants */
    public static final int SRC_PATTERN_ENABLE = 1;
    public static final int SRC_ROTATION_ENABLE = 2;
    public static final int SRC_LINEAR_ENABLE = 4;
    public static final int SRC_BYTE_ALIGN = 8;
    public static final int SRC_LINE_X_RIGHT_TO_LEFT = 0;
    public static final int SRC_LINE_X_LEFT_TO_RIGHT = 0x10;

    /* HOST_CNTL register constants */
    public static final int HOST_BYTE_ALIGN = 1;

    /* GUI_TRAJ_CNTL register constants */
    public static final int PAT_MONO_8x8_ENABLE = 0x01000000;
    public static final int PAT_CLR_4x2_ENABLE = 0x02000000;
    public static final int PAT_CLR_8x1_ENABLE = 0x04000000;

    /* DP_CHAIN_MASK register constants */
    public static final int DP_CHAIN_4BPP = 0x8888;
    public static final int DP_CHAIN_7BPP = 0xD2D2;
    public static final int DP_CHAIN_8BPP = 0x8080;
    public static final int DP_CHAIN_8BPP_RGB = 0x9292;
    public static final int DP_CHAIN_15BPP = 0x4210;
    public static final int DP_CHAIN_16BPP = 0x8410;
    public static final int DP_CHAIN_24BPP = 0x8080;
    public static final int DP_CHAIN_32BPP = 0x8080;

    /* DP_PIX_WIDTH register constants */
    public static final int DST_1BPP = 0;
    public static final int DST_4BPP = 1;
    public static final int DST_8BPP = 2;
    public static final int DST_15BPP = 3;
    public static final int DST_16BPP = 4;
    public static final int DST_32BPP = 6;
    public static final int SRC_1BPP = 0;
    public static final int SRC_4BPP = 0x100;
    public static final int SRC_8BPP = 0x200;
    public static final int SRC_15BPP = 0x300;
    public static final int SRC_16BPP = 0x400;
    public static final int SRC_32BPP = 0x600;
    public static final int HOST_1BPP = 0;
    public static final int HOST_4BPP = 0x10000;
    public static final int HOST_8BPP = 0x20000;
    public static final int HOST_15BPP = 0x30000;
    public static final int HOST_16BPP = 0x40000;
    public static final int HOST_32BPP = 0x60000;
    public static final int BYTE_ORDER_MSB_TO_LSB = 0;
    public static final int BYTE_ORDER_LSB_TO_MSB = 0x1000000;

    /* DP_MIX register constants */
    public static final int BKGD_MIX_NOT_D = 0;
    public static final int BKGD_MIX_ZERO = 1;
    public static final int BKGD_MIX_ONE = 2;
    public static final int BKGD_MIX_D = 3;
    public static final int BKGD_MIX_NOT_S = 4;
    public static final int BKGD_MIX_D_XOR_S = 5;
    public static final int BKGD_MIX_NOT_D_XOR_S = 6;
    public static final int BKGD_MIX_S = 7;
    public static final int BKGD_MIX_NOT_D_OR_NOT_S = 8;
    public static final int BKGD_MIX_D_OR_NOT_S = 9;
    public static final int BKGD_MIX_NOT_D_OR_S = 10;
    public static final int BKGD_MIX_D_OR_S = 11;
    public static final int BKGD_MIX_D_AND_S = 12;
    public static final int BKGD_MIX_NOT_D_AND_S = 13;
    public static final int BKGD_MIX_D_AND_NOT_S = 14;
    public static final int BKGD_MIX_NOT_D_AND_NOT_S = 15;
    public static final int BKGD_MIX_D_PLUS_S_DIV2 = 0x17;
    public static final int FRGD_MIX_NOT_D = 0;
    public static final int FRGD_MIX_ZERO = 0x10000;
    public static final int FRGD_MIX_ONE = 0x20000;
    public static final int FRGD_MIX_D = 0x30000;
    public static final int FRGD_MIX_NOT_S = 0x40000;
    public static final int FRGD_MIX_D_XOR_S = 0x50000;
    public static final int FRGD_MIX_NOT_D_XOR_S = 0x60000;
    public static final int FRGD_MIX_S = 0x70000;
    public static final int FRGD_MIX_NOT_D_OR_NOT_S = 0x80000;
    public static final int FRGD_MIX_D_OR_NOT_S = 0x90000;
    public static final int FRGD_MIX_NOT_D_OR_S = 0xa0000;
    public static final int FRGD_MIX_D_OR_S = 0xb0000;
    public static final int FRGD_MIX_D_AND_S = 0xc0000;
    public static final int FRGD_MIX_NOT_D_AND_S = 0xd0000;
    public static final int FRGD_MIX_D_AND_NOT_S = 0xe0000;
    public static final int FRGD_MIX_NOT_D_AND_NOT_S = 0xf0000;
    public static final int FRGD_MIX_D_PLUS_S_DIV2 = 0x170000;

    /* DP_SRC register constants */
    public static final int BKGD_SRC_BKGD_CLR = 0;
    public static final int BKGD_SRC_FRGD_CLR = 1;
    public static final int BKGD_SRC_HOST = 2;
    public static final int BKGD_SRC_BLIT = 3;
    public static final int BKGD_SRC_PATTERN = 4;
    public static final int FRGD_SRC_BKGD_CLR = 0;
    public static final int FRGD_SRC_FRGD_CLR = 0x100;
    public static final int FRGD_SRC_HOST = 0x200;
    public static final int FRGD_SRC_BLIT = 0x300;
    public static final int FRGD_SRC_PATTERN = 0x400;
    public static final int MONO_SRC_ONE = 0;
    public static final int MONO_SRC_PATTERN = 0x10000;
    public static final int MONO_SRC_HOST = 0x20000;
    public static final int MONO_SRC_BLIT = 0x30000;

    /* CLR_CMP_CNTL register constants */
    public static final int COMPARE_FALSE = 0;
    public static final int COMPARE_TRUE = 1;
    public static final int COMPARE_NOT_EQUAL = 4;
    public static final int COMPARE_EQUAL = 5;
    public static final int COMPARE_DESTINATION = 0;
    public static final int COMPARE_SOURCE = 0x1000000;

    /* FIFO_STAT register constants */
    public static final int FIFO_ERR = 0x80000000;

    /* CONTEXT_LOAD_CNTL constants */
    public static final int CONTEXT_NO_LOAD = 0;
    public static final int CONTEXT_LOAD = 0x10000;
    public static final int CONTEXT_LOAD_AND_DO_FILL = 0x20000;
    public static final int CONTEXT_LOAD_AND_DO_LINE = 0x30000;
    public static final int CONTEXT_EXECUTE = 0;
    public static final int CONTEXT_CMD_DISABLE = 0x80000000;

    /* GUI_STAT register constants */
    public static final int ENGINE_IDLE = 0;
    public static final int ENGINE_BUSY = 1;
    public static final int SCISSOR_LEFT_FLAG = 0x10;
    public static final int SCISSOR_RIGHT_FLAG = 0x20;
    public static final int SCISSOR_TOP_FLAG = 0x40;
    public static final int SCISSOR_BOTTOM_FLAG = 0x80;

    /* ATI VGA Extended Regsiters */
    public static final int sioATIEXT = 0x1ce;
    public static final int bioATIEXT = 0x3ce;

    public static final int ATI2E = 0xae;
    public static final int ATI32 = 0xb2;
    public static final int ATI36 = 0xb6;

    /* VGA Graphics Controller Registers */
    public static final int VGAGRA = 0x3ce;
    public static final int GRA06 = 0x06;

    /* VGA Seququencer Registers */
    public static final int VGASEQ = 0x3c4;
    public static final int SEQ02 = 0x02;
    public static final int SEQ04 = 0x04;

    public static final int MACH64_MAX_X = ENGINE_MAX_X;
    public static final int MACH64_MAX_Y = ENGINE_MAX_Y;

    public static final int INC_X = 0x0020;
    public static final int INC_Y = 0x0080;

    public static final int RGB16_555 = 0x0000;
    public static final int RGB16_565 = 0x0040;
    public static final int RGB16_655 = 0x0080;
    public static final int RGB16_664 = 0x00c0;

    public static final int POLY_TEXT_TYPE = 0x0001;
    public static final int IMAGE_TEXT_TYPE = 0x0002;
    public static final int TEXT_TYPE_8_BIT = 0x0004;
    public static final int TEXT_TYPE_16_BIT = 0x0008;
    public static final int POLY_TEXT_TYPE_8 = (POLY_TEXT_TYPE | TEXT_TYPE_8_BIT);
    public static final int IMAGE_TEXT_TYPE_8 = (IMAGE_TEXT_TYPE | TEXT_TYPE_8_BIT);
    public static final int POLY_TEXT_TYPE_16 = (POLY_TEXT_TYPE | TEXT_TYPE_16_BIT);
    public static final int IMAGE_TEXT_TYPE_16 = (IMAGE_TEXT_TYPE | TEXT_TYPE_16_BIT);

    public static final int MACH64_NUM_CLOCKS = 16;
    public static final int MACH64_NUM_FREQS = 50;
}
