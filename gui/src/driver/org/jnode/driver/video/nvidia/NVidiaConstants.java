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

package org.jnode.driver.video.nvidia;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface NVidiaConstants {

    // NVidia architectures
    public static final int NV04A = 1;
    public static final int NV10A = 2;
    public static final int NV20A = 3;
    public static final int NV30A = 4;
    public static final int NV28M = 5;

    /* NV registers definitions and macros for access to */

    // new:
    /* PCI_config_space */
    public static final int NVCFG_DEVID = 0x00;
    public static final int NVCFG_DEVCTRL = 0x04;
    public static final int NVCFG_CLASS = 0x08;
    public static final int NVCFG_HEADER = 0x0c;
    public static final int NVCFG_BASE1REGS = 0x10;
    public static final int NVCFG_BASE2FB = 0x14;
    public static final int NVCFG_BASE3 = 0x18;
    public static final int NVCFG_BASE4 = 0x1c; // unknown if used
    public static final int NVCFG_BASE5 = 0x20; // unknown if used
    public static final int NVCFG_BASE6 = 0x24; // unknown if used
    public static final int NVCFG_BASE7 = 0x28; // unknown if used
    public static final int NVCFG_SUBSYSID1 = 0x2c;
    public static final int NVCFG_ROMBASE = 0x30;
    public static final int NVCFG_CFG_0 = 0x34;
    public static final int NVCFG_CFG_1 = 0x38; // unknown if used
    public static final int NVCFG_INTERRUPT = 0x3c;
    public static final int NVCFG_SUBSYSID2 = 0x40;
    public static final int NVCFG_AGPREF = 0x44;
    public static final int NVCFG_AGPSTAT = 0x48;
    public static final int NVCFG_AGPCMD = 0x4c;
    public static final int NVCFG_ROMSHADOW = 0x50;
    public static final int NVCFG_VGA = 0x54;
    public static final int NVCFG_SCHRATCH = 0x58;
    public static final int NVCFG_CFG_10 = 0x5c;
    public static final int NVCFG_CFG_11 = 0x60;
    public static final int NVCFG_CFG_12 = 0x64;
    public static final int NVCFG_CFG_13 = 0x68; // unknown if used
    public static final int NVCFG_CFG_14 = 0x6c; // unknown if used
    public static final int NVCFG_CFG_15 = 0x70; // unknown if used
    public static final int NVCFG_CFG_16 = 0x74; // unknown if used
    public static final int NVCFG_CFG_17 = 0x78; // unknown if used
    public static final int NVCFG_GF2IGPU = 0x7c;
    public static final int NVCFG_CFG_19 = 0x80; // unknown if used
    public static final int NVCFG_GF4MXIGPU = 0x84;
    public static final int NVCFG_CFG_21 = 0x88; // unknown if used
    public static final int NVCFG_CFG_22 = 0x8c; // unknown if used
    public static final int NVCFG_CFG_23 = 0x90; // unknown if used
    public static final int NVCFG_CFG_24 = 0x94; // unknown if used
    public static final int NVCFG_CFG_25 = 0x98; // unknown if used
    public static final int NVCFG_CFG_26 = 0x9c; // unknown if used
    public static final int NVCFG_CFG_27 = 0xa0; // unknown if used
    public static final int NVCFG_CFG_28 = 0xa4; // unknown if used
    public static final int NVCFG_CFG_29 = 0xa8; // unknown if used
    public static final int NVCFG_CFG_30 = 0xac; // unknown if used
    public static final int NVCFG_CFG_31 = 0xb0; // unknown if used
    public static final int NVCFG_CFG_32 = 0xb4; // unknown if used
    public static final int NVCFG_CFG_33 = 0xb8; // unknown if used
    public static final int NVCFG_CFG_34 = 0xbc; // unknown if used
    public static final int NVCFG_CFG_35 = 0xc0; // unknown if used
    public static final int NVCFG_CFG_36 = 0xc4; // unknown if used
    public static final int NVCFG_CFG_37 = 0xc8; // unknown if used
    public static final int NVCFG_CFG_38 = 0xcc; // unknown if used
    public static final int NVCFG_CFG_39 = 0xd0; // unknown if used
    public static final int NVCFG_CFG_40 = 0xd4; // unknown if used
    public static final int NVCFG_CFG_41 = 0xd8; // unknown if used
    public static final int NVCFG_CFG_42 = 0xdc; // unknown if used
    public static final int NVCFG_CFG_43 = 0xe0; // unknown if used
    public static final int NVCFG_CFG_44 = 0xe4; // unknown if used
    public static final int NVCFG_CFG_45 = 0xe8; // unknown if used
    public static final int NVCFG_CFG_46 = 0xec; // unknown if used
    public static final int NVCFG_CFG_47 = 0xf0; // unknown if used
    public static final int NVCFG_CFG_48 = 0xf4; // unknown if used
    public static final int NVCFG_CFG_49 = 0xf8; // unknown if used
    public static final int NVCFG_CFG_50 = 0xfc; // unknown if used

    /* Nvidia PCI direct registers */
    public static final int NV32_PWRUPCTRL = 0x00000200;
    public static final int NV8_MISCW = 0x000c03c2;
    public static final int NV8_MISCR = 0x000c03cc;
    public static final int NV8_SEQIND = 0x000c03c4;
    public static final int NV16_SEQIND = 0x000c03c4;
    public static final int NV8_SEQDAT = 0x000c03c5;
    public static final int NV8_GRPHIND = 0x000c03ce;
    public static final int NV16_GRPHIND = 0x000c03ce;
    public static final int NV8_GRPHDAT = 0x000c03cf;

    /* bootstrap info registers */
    public static final int NV32_NV4STRAPINFO = 0x00100000;
    public static final int NV32_PFB_CONFIG_0 = 0x00100200;
    public static final int NV32_NV10STRAPINFO = 0x0010020c;
    public static final int NV32_NVSTRAPINFO2 = 0x00101000;

    /* primary head */
    public static final int NV8_ATTRINDW = 0x006013c0;
    public static final int NV8_ATTRDATW = 0x006013c0;
    public static final int NV8_ATTRDATR = 0x006013c1;
    public static final int NV8_CRTCIND = 0x006013d4;
    public static final int NV16_CRTCIND = 0x006013d4;
    public static final int NV8_CRTCDAT = 0x006013d5;
    public static final int NV8_INSTAT1 = 0x006013da;
    public static final int NV32_NV10FBSTADD32 = 0x00600800;
    public static final int NV32_CONFIG = 0x00600804; // not yet used
                                                        // (coldstart)...
    public static final int NV32_RASTER = 0x00600808;
    public static final int NV32_NV10CURADD32 = 0x0060080c;
    public static final int NV32_CURCONF = 0x00600810;
    public static final int NV32_FUNCSEL = 0x00600860;

    /* secondary head */
    public static final int NV8_ATTR2INDW = 0x006033c0;
    public static final int NV8_ATTR2DATW = 0x006033c0;
    public static final int NV8_ATTR2DATR = 0x006033c1;
    public static final int NV8_CRTC2IND = 0x006033d4;
    public static final int NV16_CRTC2IND = 0x006033d4;
    public static final int NV8_CRTC2DAT = 0x006033d5;
    public static final int NV8_2INSTAT1 = 0x006033da; // verify!!!
    public static final int NV32_NV10FB2STADD32 = 0x00602800; // verify!!!
    public static final int NV32_RASTER2 = 0x00602808; // verify!!!
    public static final int NV32_NV10CUR2ADD32 = 0x0060280c; // verify!!!
    public static final int NV32_2CURCONF = 0x00602810; // verify!!!
    public static final int NV32_2FUNCSEL = 0x00602860;

    /* Nvidia DAC direct registers (standard VGA palette RAM registers) */
    /* primary head */
    public static final int NV8_PALMASK = 0x006813c6;
    public static final int NV8_PALINDR = 0x006813c7;
    public static final int NV8_PALINDW = 0x006813c8;
    public static final int NV8_PALDATA = 0x006813c9;
    /* secondary head */
    public static final int NV8_PAL2MASK = 0x006833c6; // verify!!!
    public static final int NV8_PAL2INDR = 0x006833c7; // verify!!!
    public static final int NV8_PAL2INDW = 0x006833c8; // verify!!!
    public static final int NV8_PAL2DATA = 0x006833c9; // verify!!!

    /* Nvidia PCI direct DAC registers (32bit) */
    /* primary head */
    public static final int NVDAC_CURPOS = 0x00680300;
    public static final int NVDAC_PIXPLLC = 0x00680508;
    public static final int NVDAC_PLLSEL = 0x0068050c;
    public static final int NVDAC_GENCTRL = 0x00680600;
    /* secondary head */
    public static final int NVDAC2_CURPOS = 0x00680b00;
    public static final int NVDAC2_PIXPLLC = 0x00680d20; // verify!!!
    public static final int NVDAC2_PLLSEL = 0x00680d0c; // verify!!!
    public static final int NVDAC2_GENCTRL = 0x00680e00; // verify!!!

    /* Nvidia CRTC indexed registers */
    /* VGA standard registers: */
    public static final int NVCRTCX_HTOTAL = 0x00;
    public static final int NVCRTCX_HDISPE = 0x01;
    public static final int NVCRTCX_HBLANKS = 0x02;
    public static final int NVCRTCX_HBLANKE = 0x03;
    public static final int NVCRTCX_HSYNCS = 0x04;
    public static final int NVCRTCX_HSYNCE = 0x05;
    public static final int NVCRTCX_VTOTAL = 0x06;
    public static final int NVCRTCX_OVERFLOW = 0x07;
    public static final int NVCRTCX_PRROWSCN = 0x08;
    public static final int NVCRTCX_MAXSCLIN = 0x09;
    public static final int NVCRTCX_VGACURCTRL = 0x0a;
    public static final int NVCRTCX_FBSTADDH = 0x0c; // confirmed
    public static final int NVCRTCX_FBSTADDL = 0x0d; // confirmed
    public static final int NVCRTCX_VSYNCS = 0x10;
    public static final int NVCRTCX_VSYNCE = 0x11;
    public static final int NVCRTCX_VDISPE = 0x12;
    public static final int NVCRTCX_PITCHL = 0x13; // confirmed
    public static final int NVCRTCX_VBLANKS = 0x15;
    public static final int NVCRTCX_VBLANKE = 0x16;
    public static final int NVCRTCX_MODECTL = 0x17;
    public static final int NVCRTCX_LINECOMP = 0x18;
    /* Nvidia specific registers: */
    public static final int NVCRTCX_REPAINT0 = 0x19;
    public static final int NVCRTCX_REPAINT1 = 0x1a;
    public static final int NVCRTCX_ARBITRATION0 = 0x1b;
    public static final int NVCRTCX_LOCK = 0x1f;
    public static final int NVCRTCX_ARBITRATION1 = 0x20;
    public static final int NVCRTCX_LSR = 0x25; // Assorted extra bits
    public static final int NVCRTCX_PIXEL = 0x28;
    public static final int NVCRTCX_HEB = 0x2d; // Horizontal extended bits
    public static final int NVCRTCX_CURCTL2 = 0x2f;
    public static final int NVCRTCX_CURCTL1 = 0x30;
    public static final int NVCRTCX_CURCTL0 = 0x31;
    public static final int NVCRTCX_EBR = 0x41;

    /* Nvidia ATTRIBUTE indexed registers */
    /* VGA standard registers: */
    public static final int NVATBX_MODECTL = 0x10;
    public static final int NVATBX_OSCANCOLOR = 0x11;
    public static final int NVATBX_COLPLANE_EN = 0x12;
    public static final int NVATBX_HORPIXPAN = 0x13; // confirmed
    public static final int NVATBX_COLSEL = 0x14;

    /* Nvidia SEQUENCER indexed registers */
    /* VGA standard registers: */
    public static final int NVSEQX_RESET = 0x00;
    public static final int NVSEQX_CLKMODE = 0x01;
    public static final int NVSEQX_MEMMODE = 0x04;

    /* Nvidia GRAPHICS indexed registers */
    /* VGA standard registers: */
    public static final int NVGRPHX_ENSETRESET = 0x01;
    public static final int NVGRPHX_DATAROTATE = 0x03;
    public static final int NVGRPHX_READMAPSEL = 0x04;
    public static final int NVGRPHX_MODE = 0x05;
    public static final int NVGRPHX_MISC = 0x06;
    public static final int NVGRPHX_BITMASK = 0x08;

    /* Nvidia BES (Back End Scaler) registers (>= NV10) */
    public static final int NVBES_NV10_BUFSEL = 0x00008700;
    public static final int NVBES_NV10_GENCTRL = 0x00008704;
    public static final int NVBES_NV10_COLKEY = 0x00008b00;
    /* buffer 0 */
    public static final int NVBES_NV10_0BUFADR = 0x00008900;
    public static final int NVBES_NV10_0MEMMASK = 0x00008908;
    public static final int NVBES_NV10_0BRICON = 0x00008910;
    public static final int NVBES_NV10_0SAT = 0x00008918;
    public static final int NVBES_NV10_0WHAT = 0x00008920;
    public static final int NVBES_NV10_0SRCSIZE = 0x00008928;
    public static final int NVBES_NV10_0SRCREF = 0x00008930;
    public static final int NVBES_NV10_0ISCALH = 0x00008938;
    public static final int NVBES_NV10_0ISCALV = 0x00008940;
    public static final int NVBES_NV10_0DSTREF = 0x00008948;
    public static final int NVBES_NV10_0DSTSIZE = 0x00008950;
    public static final int NVBES_NV10_0SRCPTCH = 0x00008958;
    /* buffer 1 */
    public static final int NVBES_NV10_1BUFADR = 0x00008904;
    public static final int NVBES_NV10_1MEMMASK = 0x0000890c;
    public static final int NVBES_NV10_1BRICON = 0x00008914;
    public static final int NVBES_NV10_1SAT = 0x0000891c;
    public static final int NVBES_NV10_1WHAT = 0x00008924;
    public static final int NVBES_NV10_1SRCSIZE = 0x0000892c;
    public static final int NVBES_NV10_1SRCREF = 0x00008934;
    public static final int NVBES_NV10_1ISCALH = 0x0000893c;
    public static final int NVBES_NV10_1ISCALV = 0x00008944;
    public static final int NVBES_NV10_1DSTREF = 0x0000894c;
    public static final int NVBES_NV10_1DSTSIZE = 0x00008954;
    public static final int NVBES_NV10_1SRCPTCH = 0x0000895c;
    /* Nvidia MPEG2 hardware decoder (GeForce4MX only) */
    public static final int NVBES_DEC_GENCTRL = 0x00001588;

    /*
     * chip->PMC[= 0x00008140/4] = 0;
     */
    // end new.
    /* NV 2nd CRTC registers (>= G400) */
    public static final int NVCR2_CTL = 0x3C10;
    public static final int NVCR2_HPARAM = 0x3C14;
    public static final int NVCR2_HSYNC = 0x3C18;
    public static final int NVCR2_VPARAM = 0x3C1C;
    public static final int NVCR2_VSYNC = 0x3C20;
    public static final int NVCR2_PRELOAD = 0x3C24;
    public static final int NVCR2_STARTADD0 = 0x3C28;
    public static final int NVCR2_STARTADD1 = 0x3C2C;
    public static final int NVCR2_OFFSET = 0x3C40;
    public static final int NVCR2_MISC = 0x3C44;
    public static final int NVCR2_VCOUNT = 0x3C48;
    public static final int NVCR2_DATACTL = 0x3C4C;

    /* NV ACCeleration registers */
    public static final int NVACC_DWGCTL = 0x1C00;
    public static final int NVACC_MACCESS = 0x1C04;
    public static final int NVACC_MCTLWTST = 0x1C08;
    public static final int NVACC_ZORG = 0x1C0C;
    public static final int NVACC_PLNWT = 0x1C1C;
    public static final int NVACC_BCOL = 0x1C20;
    public static final int NVACC_FCOL = 0x1C24;
    public static final int NVACC_XYSTRT = 0x1C40;
    public static final int NVACC_XYEND = 0x1C44;
    public static final int NVACC_SGN = 0x1C58;
    public static final int NVACC_LEN = 0x1C5C;
    public static final int NVACC_AR0 = 0x1C60;
    public static final int NVACC_AR3 = 0x1C6C;
    public static final int NVACC_AR5 = 0x1C74;
    public static final int NVACC_CXBNDRY = 0x1C80;
    public static final int NVACC_FXBNDRY = 0x1C84;
    public static final int NVACC_YDSTLEN = 0x1C88;
    public static final int NVACC_PITCH = 0x1C8C;
    public static final int NVACC_YDST = 0x1C90;
    public static final int NVACC_YDSTORG = 0x1C94;
    public static final int NVACC_YTOP = 0x1C98;
    public static final int NVACC_YBOT = 0x1C9C;
    public static final int NVACC_CXLEFT = 0x1CA0;
    public static final int NVACC_CXRIGHT = 0x1CA4;
    public static final int NVACC_FXLEFT = 0x1CA8;
    public static final int NVACC_FXRIGHT = 0x1CAC;
    // public static final int NVACC_STATUS = 0x1E14;
    public static final int NVACC_ICLEAR = 0x1E18; /*
                                                     * required for interrupt
                                                     * stuff
                                                     */
    public static final int NVACC_IEN = 0x1E1C; /* required for interrupt stuff */
    public static final int NVACC_RST = 0x1E40;
    public static final int NVACC_MEMRDBK = 0x1E44;
    public static final int NVACC_OPMODE = 0x1E54;
    public static final int NVACC_PRIMADDRESS = 0x1E58;
    public static final int NVACC_PRIMEND = 0x1E5C;
    public static final int NVACC_TEXORG = 0x2C24; // >= G100
    public static final int NVACC_DWGSYNC = 0x2C4C; // >= G200
    public static final int NVACC_TEXORG1 = 0x2CA4; // >= G200
    public static final int NVACC_TEXORG2 = 0x2CA8; // >= G200
    public static final int NVACC_TEXORG3 = 0x2CAC; // >= G200
    public static final int NVACC_TEXORG4 = 0x2CB0; // >= G200
    public static final int NVACC_SRCORG = 0x2CB4; // >= G200
    public static final int NVACC_DSTORG = 0x2CB8; // >= G200

    /* NV ACCeleration registers */
    /* engine initialisation registers */
    public static final int NVACC_FORMATS = 0x00400618;
    public static final int NVACC_OFFSET0 = 0x00400640;
    public static final int NVACC_OFFSET1 = 0x00400644;
    public static final int NVACC_OFFSET2 = 0x00400648;
    public static final int NVACC_OFFSET3 = 0x0040064c;
    public static final int NVACC_OFFSET4 = 0x00400650;
    public static final int NVACC_OFFSET5 = 0x00400654;
    public static final int NVACC_BBASE0 = 0x00400658;
    public static final int NVACC_BBASE1 = 0x0040065c;
    public static final int NVACC_BBASE2 = 0x00400660;
    public static final int NVACC_BBASE3 = 0x00400664;
    public static final int NVACC_NV10_BBASE4 = 0x00400668;
    public static final int NVACC_NV10_BBASE5 = 0x0040066c;
    public static final int NVACC_PITCH0 = 0x00400670;
    public static final int NVACC_PITCH1 = 0x00400674;
    public static final int NVACC_PITCH2 = 0x00400678;
    public static final int NVACC_PITCH3 = 0x0040067c;
    public static final int NVACC_PITCH4 = 0x00400680;
    public static final int NVACC_BLIMIT0 = 0x00400684;
    public static final int NVACC_BLIMIT1 = 0x00400688;
    public static final int NVACC_BLIMIT2 = 0x0040068c;
    public static final int NVACC_BLIMIT3 = 0x00400690;
    public static final int NVACC_NV10_BLIMIT4 = 0x00400694;
    public static final int NVACC_NV10_BLIMIT5 = 0x00400698;
    public static final int NVACC_BPIXEL = 0x00400724;
    public static final int NVACC_NV20_OFFSET0 = 0x00400820;
    public static final int NVACC_NV20_OFFSET1 = 0x00400824;
    public static final int NVACC_NV20_OFFSET2 = 0x00400828;
    public static final int NVACC_NV20_OFFSET3 = 0x0040082c;
    public static final int NVACC_STRD_FMT = 0x00400830;
    public static final int NVACC_NV20_PITCH0 = 0x00400850;
    public static final int NVACC_NV20_PITCH1 = 0x00400854;
    public static final int NVACC_NV20_PITCH2 = 0x00400858;
    public static final int NVACC_NV20_PITCH3 = 0x0040085c;
    public static final int NVACC_NV20_BLIMIT6 = 0x00400864;
    public static final int NVACC_NV20_BLIMIT7 = 0x00400868;
    public static final int NVACC_NV20_BLIMIT8 = 0x0040086c;
    public static final int NVACC_NV20_BLIMIT9 = 0x00400870;
    public static final int NVACC_NV30_WHAT = 0x00400890;

    /* specials */
    public static final int NVACC_DEBUG0 = 0x00400080;
    public static final int NVACC_DEBUG1 = 0x00400084;
    public static final int NVACC_DEBUG2 = 0x00400088;
    public static final int NVACC_DEBUG3 = 0x0040008c;
    public static final int NVACC_NV10_DEBUG4 = 0x00400090;
    public static final int NVACC_ACC_INTS = 0x00400100;
    public static final int NVACC_ACC_INTE = 0x00400140;
    public static final int NVACC_NV10_CTX_CTRL = 0x00400144;
    public static final int NVACC_STATUS = 0x00400700;
    public static final int NVACC_NV04_SURF_TYP = 0x0040070c;
    public static final int NVACC_NV10_SURF_TYP = 0x00400710;
    public static final int NVACC_NV04_ACC_STAT = 0x00400710;
    public static final int NVACC_NV10_ACC_STAT = 0x00400714;
    public static final int NVACC_FIFO_EN = 0x00400720;
    public static final int NVACC_PAT_SHP = 0x00400810;
    public static final int NVACC_NV10_XFMOD0 = 0x00400f40;
    public static final int NVACC_NV10_XFMOD1 = 0x00400f44;
    public static final int NVACC_NV10_PIPEADR = 0x00400f50;
    public static final int NVACC_NV10_PIPEDAT = 0x00400f54;

    /* PGRAPH cache registers */
    public static final int NVACC_CACHE1_1 = 0x00400160;
    public static final int NVACC_CACHE1_2 = 0x00400180;
    public static final int NVACC_CACHE1_3 = 0x004001a0;
    public static final int NVACC_CACHE1_4 = 0x004001c0;
    public static final int NVACC_CACHE1_5 = 0x004001e0;
    public static final int NVACC_CACHE2_1 = 0x00400164;
    public static final int NVACC_CACHE2_2 = 0x00400184;
    public static final int NVACC_CACHE2_3 = 0x004001a4;
    public static final int NVACC_CACHE2_4 = 0x004001c4;
    public static final int NVACC_CACHE2_5 = 0x004001e4;
    public static final int NVACC_CACHE3_1 = 0x00400168;
    public static final int NVACC_CACHE3_2 = 0x00400188;
    public static final int NVACC_CACHE3_3 = 0x004001a8;
    public static final int NVACC_CACHE3_4 = 0x004001c8;
    public static final int NVACC_CACHE3_5 = 0x004001e8;
    public static final int NVACC_CACHE4_1 = 0x0040016c;
    public static final int NVACC_CACHE4_2 = 0x0040018c;
    public static final int NVACC_CACHE4_3 = 0x004001ac;
    public static final int NVACC_CACHE4_4 = 0x004001cc;
    public static final int NVACC_CACHE4_5 = 0x004001ec;
    public static final int NVACC_NV10_CACHE5_1 = 0x00400170;
    public static final int NVACC_NV04_CTX_CTRL = 0x00400170;
    public static final int NVACC_CACHE5_2 = 0x00400190;
    public static final int NVACC_CACHE5_3 = 0x004001b0;
    public static final int NVACC_CACHE5_4 = 0x004001d0;
    public static final int NVACC_CACHE5_5 = 0x004001f0;
    public static final int NVACC_NV10_CACHE6_1 = 0x00400174;
    public static final int NVACC_CACHE6_2 = 0x00400194;
    public static final int NVACC_CACHE6_3 = 0x004001b4;
    public static final int NVACC_CACHE6_4 = 0x004001d4;
    public static final int NVACC_CACHE6_5 = 0x004001f4;
    public static final int NVACC_NV10_CACHE7_1 = 0x00400178;
    public static final int NVACC_CACHE7_2 = 0x00400198;
    public static final int NVACC_CACHE7_3 = 0x004001b8;
    public static final int NVACC_CACHE7_4 = 0x004001d8;
    public static final int NVACC_CACHE7_5 = 0x004001f8;
    public static final int NVACC_NV10_CACHE8_1 = 0x0040017c;
    public static final int NVACC_CACHE8_2 = 0x0040019c;
    public static final int NVACC_CACHE8_3 = 0x004001bc;
    public static final int NVACC_CACHE8_4 = 0x004001dc;
    public static final int NVACC_CACHE8_5 = 0x004001fc;
    public static final int NVACC_NV10_CTX_SW1 = 0x0040014c;
    public static final int NVACC_NV10_CTX_SW2 = 0x00400150;
    public static final int NVACC_NV10_CTX_SW3 = 0x00400154;
    public static final int NVACC_NV10_CTX_SW4 = 0x00400158;
    public static final int NVACC_NV10_CTX_SW5 = 0x0040015c;

    /* used RAMHT registers (hash-table(?)) */
    public static final int NVACC_HT_HANDL_00 = 0x00710000;
    public static final int NVACC_HT_VALUE_00 = 0x00710004;
    public static final int NVACC_HT_HANDL_01 = 0x00710008;
    public static final int NVACC_HT_VALUE_01 = 0x0071000c;
    public static final int NVACC_HT_HANDL_02 = 0x00710010;
    public static final int NVACC_HT_VALUE_02 = 0x00710014;
    public static final int NVACC_HT_HANDL_03 = 0x00710018;
    public static final int NVACC_HT_VALUE_03 = 0x0071001c;
    public static final int NVACC_HT_HANDL_04 = 0x00710020;
    public static final int NVACC_HT_VALUE_04 = 0x00710024;
    public static final int NVACC_HT_HANDL_05 = 0x00710028;
    public static final int NVACC_HT_VALUE_05 = 0x0071002c;
    public static final int NVACC_HT_HANDL_06 = 0x00710030;
    public static final int NVACC_HT_VALUE_06 = 0x00710034;
    public static final int NVACC_HT_HANDL_10 = 0x00710080;
    public static final int NVACC_HT_VALUE_10 = 0x00710084;
    public static final int NVACC_HT_HANDL_11 = 0x00710088;
    public static final int NVACC_HT_VALUE_11 = 0x0071008c;
    public static final int NVACC_HT_HANDL_12 = 0x00710090;
    public static final int NVACC_HT_VALUE_12 = 0x00710094;
    public static final int NVACC_HT_HANDL_13 = 0x00710098;
    public static final int NVACC_HT_VALUE_13 = 0x0071009c;
    public static final int NVACC_HT_HANDL_14 = 0x007100a0;
    public static final int NVACC_HT_VALUE_14 = 0x007100a4;
    public static final int NVACC_HT_HANDL_15 = 0x007100a8;
    public static final int NVACC_HT_VALUE_15 = 0x007100ac;
    public static final int NVACC_HT_HANDL_16 = 0x007100b0;
    public static final int NVACC_HT_VALUE_16 = 0x007100b4;
    public static final int NVACC_HT_HANDL_17 = 0x007100b8;
    public static final int NVACC_HT_VALUE_17 = 0x007100bc;

    /* MAVEN registers ( <= G400) */
    public static final int NVMAV_PGM = 0x3E;
    public static final int NVMAV_PIXPLLM = 0x80;
    public static final int NVMAV_PIXPLLN = 0x81;
    public static final int NVMAV_PIXPLLP = 0x82;
    public static final int NVMAV_GAMMA1 = 0x83;
    public static final int NVMAV_GAMMA2 = 0x84;
    public static final int NVMAV_GAMMA3 = 0x85;
    public static final int NVMAV_GAMMA4 = 0x86;
    public static final int NVMAV_GAMMA5 = 0x87;
    public static final int NVMAV_GAMMA6 = 0x88;
    public static final int NVMAV_GAMMA7 = 0x89;
    public static final int NVMAV_GAMMA8 = 0x8A;
    public static final int NVMAV_GAMMA9 = 0x8B;
    public static final int NVMAV_MONSET = 0x8C;
    public static final int NVMAV_TEST = 0x8D;
    public static final int NVMAV_WREG_0X8E_L = 0x8E;
    public static final int NVMAV_WREG_0X8E_H = 0x8F;
    public static final int NVMAV_HSCALETV = 0x90;
    public static final int NVMAV_TSCALETVL = 0x91;
    public static final int NVMAV_TSCALETVH = 0x92;
    public static final int NVMAV_FFILTER = 0x93;
    public static final int NVMAV_MONEN = 0x94;
    public static final int NVMAV_RESYNC = 0x95;
    public static final int NVMAV_LASTLINEL = 0x96;
    public static final int NVMAV_LASTLINEH = 0x97;
    public static final int NVMAV_WREG_0X98_L = 0x98;
    public static final int NVMAV_WREG_0X98_H = 0x99;
    public static final int NVMAV_HSYNCLENL = 0x9A;
    public static final int NVMAV_HSYNCLENH = 0x9B;
    public static final int NVMAV_HSYNCSTRL = 0x9C;
    public static final int NVMAV_HSYNCSTRH = 0x9D;
    public static final int NVMAV_HDISPLAYL = 0x9E;
    public static final int NVMAV_HDISPLAYH = 0x9F;
    public static final int NVMAV_HTOTALL = 0xA0;
    public static final int NVMAV_HTOTALH = 0xA1;
    public static final int NVMAV_VSYNCLENL = 0xA2;
    public static final int NVMAV_VSYNCLENH = 0xA3;
    public static final int NVMAV_VSYNCSTRL = 0xA4;
    public static final int NVMAV_VSYNCSTRH = 0xA5;
    public static final int NVMAV_VDISPLAYL = 0xA6;
    public static final int NVMAV_VDISPLAYH = 0xA7;
    public static final int NVMAV_VTOTALL = 0xA8;
    public static final int NVMAV_VTOTALH = 0xA9;
    public static final int NVMAV_HVIDRSTL = 0xAA;
    public static final int NVMAV_HVIDRSTH = 0xAB;
    public static final int NVMAV_VVIDRSTL = 0xAC;
    public static final int NVMAV_VVIDRSTH = 0xAD;
    public static final int NVMAV_VSOMETHINGL = 0xAE;
    public static final int NVMAV_VSOMETHINGH = 0xAF;
    public static final int NVMAV_OUTMODE = 0xB0;
    public static final int NVMAV_LOCK = 0xB3;
    public static final int NVMAV_LUMA = 0xB9;
    public static final int NVMAV_VDISPLAYTV = 0xBE;
    public static final int NVMAV_STABLE = 0xBF;
    public static final int NVMAV_HDISPLAYTV = 0xC2;
    public static final int NVMAV_BREG_0XC6 = 0xC6;

    public static final int NV_PGRAPH_BOFFSET0 = 0x00400640;
    public static final int NV_PGRAPH_BOFFSET1 = 0x00400644;
    public static final int NV_PGRAPH_BOFFSET2 = 0x00400648;
    public static final int NV_PGRAPH_BOFFSET3 = 0x0040064c;
    public static final int NV_PGRAPH_BPITCH0 = 0x00400670;
    public static final int NV_PGRAPH_BPITCH1 = 0x00400674;
    public static final int NV_PGRAPH_BPITCH2 = 0x00400678;
    public static final int NV_PGRAPH_BPITCH3 = 0x0040067c;

    /* cache setup registers */
    public static final int NVACC_PF_INTSTAT = 0x00002100;
    public static final int NVACC_PF_INTEN = 0x00002140;
    public static final int NVACC_PF_RAMHT = 0x00002210;
    public static final int NVACC_PF_RAMFC = 0x00002214;
    public static final int NVACC_PF_RAMRO = 0x00002218;
    public static final int NVACC_PF_CACHES = 0x00002500;
    public static final int NVACC_PF_SIZE = 0x0000250c;
    public static final int NVACC_PF_CACH0_PSH0 = 0x00003000;
    public static final int NVACC_PF_CACH0_PUL0 = 0x00003050;
    public static final int NVACC_PF_CACH0_PUL1 = 0x00003054;
    public static final int NVACC_PF_CACH1_PSH0 = 0x00003200;
    public static final int NVACC_PF_CACH1_PSH1 = 0x00003204;
    public static final int NVACC_PF_CACH1_DMAI = 0x0000322c;
    public static final int NVACC_PF_CACH1_PUL0 = 0x00003250;
    public static final int NVACC_PF_CACH1_PUL1 = 0x00003254;
    public static final int NVACC_PF_CACH1_HASH = 0x00003258;

    /* Ptimer registers */
    public static final int NVACC_PT_INTSTAT = 0x00009100;
    public static final int NVACC_PT_INTEN = 0x00009140;
    public static final int NVACC_PT_NUMERATOR = 0x00009200;
    public static final int NVACC_PT_DENOMINATR = 0x00009210;

    /* used PRAMIN registers */
    public static final int NVACC_PR_CTX0_R = 0x00711400;
    public static final int NVACC_PR_CTX1_R = 0x00711404;
    public static final int NVACC_PR_CTX2_R = 0x00711408;
    public static final int NVACC_PR_CTX3_R = 0x0071140c;
    public static final int NVACC_PR_CTX0_0 = 0x00711420;
    public static final int NVACC_PR_CTX1_0 = 0x00711424;
    public static final int NVACC_PR_CTX2_0 = 0x00711428;
    public static final int NVACC_PR_CTX3_0 = 0x0071142c;
    public static final int NVACC_PR_CTX0_1 = 0x00711430;
    public static final int NVACC_PR_CTX1_1 = 0x00711434;
    public static final int NVACC_PR_CTX2_1 = 0x00711438;
    public static final int NVACC_PR_CTX3_1 = 0x0071143c;
    public static final int NVACC_PR_CTX0_2 = 0x00711440;
    public static final int NVACC_PR_CTX1_2 = 0x00711444;
    public static final int NVACC_PR_CTX2_2 = 0x00711448;
    public static final int NVACC_PR_CTX3_2 = 0x0071144c;
    public static final int NVACC_PR_CTX0_3 = 0x00711450;
    public static final int NVACC_PR_CTX1_3 = 0x00711454;
    public static final int NVACC_PR_CTX2_3 = 0x00711458;
    public static final int NVACC_PR_CTX3_3 = 0x0071145c;
    public static final int NVACC_PR_CTX0_4 = 0x00711460;
    public static final int NVACC_PR_CTX1_4 = 0x00711464;
    public static final int NVACC_PR_CTX2_4 = 0x00711468;
    public static final int NVACC_PR_CTX3_4 = 0x0071146c;
    public static final int NVACC_PR_CTX0_5 = 0x00711470;
    public static final int NVACC_PR_CTX1_5 = 0x00711474;
    public static final int NVACC_PR_CTX2_5 = 0x00711478;
    public static final int NVACC_PR_CTX3_5 = 0x0071147c;
    public static final int NVACC_PR_CTX0_6 = 0x00711480;
    public static final int NVACC_PR_CTX1_6 = 0x00711484;
    public static final int NVACC_PR_CTX2_6 = 0x00711488;
    public static final int NVACC_PR_CTX3_6 = 0x0071148c;
    public static final int NVACC_PR_CTX0_7 = 0x00711490;
    public static final int NVACC_PR_CTX1_7 = 0x00711494;
    public static final int NVACC_PR_CTX2_7 = 0x00711498;
    public static final int NVACC_PR_CTX3_7 = 0x0071149c;
    public static final int NVACC_PR_CTX0_8 = 0x007114a0;
    public static final int NVACC_PR_CTX1_8 = 0x007114a4;
    public static final int NVACC_PR_CTX2_8 = 0x007114a8;
    public static final int NVACC_PR_CTX3_8 = 0x007114ac;
    public static final int NVACC_PR_CTX0_9 = 0x007114b0;
    public static final int NVACC_PR_CTX1_9 = 0x007114b4;
    public static final int NVACC_PR_CTX2_9 = 0x007114b8;
    public static final int NVACC_PR_CTX3_9 = 0x007114bc;
    public static final int NVACC_PR_CTX0_A = 0x007114c0;
    public static final int NVACC_PR_CTX1_A = 0x007114c4; /* not used */
    public static final int NVACC_PR_CTX2_A = 0x007114c8;
    public static final int NVACC_PR_CTX3_A = 0x007114cc;
    public static final int NVACC_PR_CTX0_B = 0x007114d0;
    public static final int NVACC_PR_CTX1_B = 0x007114d4;
    public static final int NVACC_PR_CTX2_B = 0x007114d8;
    public static final int NVACC_PR_CTX3_B = 0x007114dc;
    public static final int NVACC_PR_CTX0_C = 0x007114e0;
    public static final int NVACC_PR_CTX1_C = 0x007114e4;
    public static final int NVACC_PR_CTX2_C = 0x007114e8;
    public static final int NVACC_PR_CTX3_C = 0x007114ec;
    public static final int NVACC_PR_CTX0_D = 0x007114f0;
    public static final int NVACC_PR_CTX1_D = 0x007114f4;
    public static final int NVACC_PR_CTX2_D = 0x007114f8;
    public static final int NVACC_PR_CTX3_D = 0x007114fc;
    public static final int NVACC_PR_CTX0_E = 0x00711500;
    public static final int NVACC_PR_CTX1_E = 0x00711504;
    public static final int NVACC_PR_CTX2_E = 0x00711508;
    public static final int NVACC_PR_CTX3_E = 0x0071150c;

    /* engine tile registers src */
    public static final int NVACC_NV20_FBWHAT0 = 0x00100200;
    public static final int NVACC_NV20_FBWHAT1 = 0x00100204;
    public static final int NVACC_NV10_FBTIL0AD = 0x00100240;
    public static final int NVACC_NV10_FBTIL0ED = 0x00100244;
    public static final int NVACC_NV10_FBTIL0PT = 0x00100248;
    public static final int NVACC_NV10_FBTIL0ST = 0x0010024c;
    public static final int NVACC_NV10_FBTIL1AD = 0x00100250;
    public static final int NVACC_NV10_FBTIL1ED = 0x00100254;
    public static final int NVACC_NV10_FBTIL1PT = 0x00100258;
    public static final int NVACC_NV10_FBTIL1ST = 0x0010025c;
    public static final int NVACC_NV10_FBTIL2AD = 0x00100260;
    public static final int NVACC_NV10_FBTIL2ED = 0x00100264;
    public static final int NVACC_NV10_FBTIL2PT = 0x00100268;
    public static final int NVACC_NV10_FBTIL2ST = 0x0010026c;
    public static final int NVACC_NV10_FBTIL3AD = 0x00100270;
    public static final int NVACC_NV10_FBTIL3ED = 0x00100274;
    public static final int NVACC_NV10_FBTIL3PT = 0x00100278;
    public static final int NVACC_NV10_FBTIL3ST = 0x0010027c;
    public static final int NVACC_NV10_FBTIL4AD = 0x00100280;
    public static final int NVACC_NV10_FBTIL4ED = 0x00100284;
    public static final int NVACC_NV10_FBTIL4PT = 0x00100288;
    public static final int NVACC_NV10_FBTIL4ST = 0x0010028c;
    public static final int NVACC_NV10_FBTIL5AD = 0x00100290;
    public static final int NVACC_NV10_FBTIL5ED = 0x00100294;
    public static final int NVACC_NV10_FBTIL5PT = 0x00100298;
    public static final int NVACC_NV10_FBTIL5ST = 0x0010029c;
    public static final int NVACC_NV10_FBTIL6AD = 0x001002a0;
    public static final int NVACC_NV10_FBTIL6ED = 0x001002a4;
    public static final int NVACC_NV10_FBTIL6PT = 0x001002a8;
    public static final int NVACC_NV10_FBTIL6ST = 0x001002ac;
    public static final int NVACC_NV10_FBTIL7AD = 0x001002b0;
    public static final int NVACC_NV10_FBTIL7ED = 0x001002b4;
    public static final int NVACC_NV10_FBTIL7PT = 0x001002b8;
    public static final int NVACC_NV10_FBTIL7ST = 0x001002bc;
    /* engine tile registers dst */
    public static final int NVACC_NV20_WHAT0 = 0x004009a4;
    public static final int NVACC_NV20_WHAT1 = 0x004009a8;
    public static final int NVACC_NV10_TIL0AD = 0x00400b00;
    public static final int NVACC_NV10_TIL0ED = 0x00400b04;
    public static final int NVACC_NV10_TIL0PT = 0x00400b08;
    public static final int NVACC_NV10_TIL0ST = 0x00400b0c;
    public static final int NVACC_NV10_TIL1AD = 0x00400b10;
    public static final int NVACC_NV10_TIL1ED = 0x00400b14;
    public static final int NVACC_NV10_TIL1PT = 0x00400b18;
    public static final int NVACC_NV10_TIL1ST = 0x00400b1c;
    public static final int NVACC_NV10_TIL2AD = 0x00400b20;
    public static final int NVACC_NV10_TIL2ED = 0x00400b24;
    public static final int NVACC_NV10_TIL2PT = 0x00400b28;
    public static final int NVACC_NV10_TIL2ST = 0x00400b2c;
    public static final int NVACC_NV10_TIL3AD = 0x00400b30;
    public static final int NVACC_NV10_TIL3ED = 0x00400b34;
    public static final int NVACC_NV10_TIL3PT = 0x00400b38;
    public static final int NVACC_NV10_TIL3ST = 0x00400b3c;
    public static final int NVACC_NV10_TIL4AD = 0x00400b40;
    public static final int NVACC_NV10_TIL4ED = 0x00400b44;
    public static final int NVACC_NV10_TIL4PT = 0x00400b48;
    public static final int NVACC_NV10_TIL4ST = 0x00400b4c;
    public static final int NVACC_NV10_TIL5AD = 0x00400b50;
    public static final int NVACC_NV10_TIL5ED = 0x00400b54;
    public static final int NVACC_NV10_TIL5PT = 0x00400b58;
    public static final int NVACC_NV10_TIL5ST = 0x00400b5c;
    public static final int NVACC_NV10_TIL6AD = 0x00400b60;
    public static final int NVACC_NV10_TIL6ED = 0x00400b64;
    public static final int NVACC_NV10_TIL6PT = 0x00400b68;
    public static final int NVACC_NV10_TIL6ST = 0x00400b6c;
    public static final int NVACC_NV10_TIL7AD = 0x00400b70;
    public static final int NVACC_NV10_TIL7ED = 0x00400b74;
    public static final int NVACC_NV10_TIL7PT = 0x00400b78;
    public static final int NVACC_NV10_TIL7ST = 0x00400b7c;

    /* acc engine fifo setup registers (for function_register 'mappings') */
    public static final int NVACC_FIFO_00800000 = 0x00800000;
    public static final int NVACC_FIFO_00802000 = 0x00802000;
    public static final int NVACC_FIFO_00804000 = 0x00804000;
    public static final int NVACC_FIFO_00806000 = 0x00806000;
    public static final int NVACC_FIFO_00808000 = 0x00808000;
    public static final int NVACC_FIFO_0080a000 = 0x0080a000;
    public static final int NVACC_FIFO_0080c000 = 0x0080c000;
    public static final int NVACC_FIFO_0080e000 = 0x0080e000;

    /* ROP3 registers (Raster OPeration) */
    public static final int NV16_ROP_FIFOFREE = 0x00800010; /* little endian */
    public static final int NVACC_ROP_ROP3 = 0x00800300; /*
                                                             * 'mapped' from
                                                             * 0x00420300
                                                             */

    /* pattern registers */
    public static final int NV16_PAT_FIFOFREE = 0x00804010; /* little endian */
    public static final int NVACC_PAT_SHAPE = 0x00804308; /*
                                                             * 'mapped' from
                                                             * 0x00460308
                                                             */
    public static final int NVACC_PAT_COLOR0 = 0x00804310; /*
                                                             * 'mapped' from
                                                             * 0x00460310
                                                             */
    public static final int NVACC_PAT_COLOR1 = 0x00804314; /*
                                                             * 'mapped' from
                                                             * 0x00460314
                                                             */
    public static final int NVACC_PAT_MONO1 = 0x00804318; /*
                                                             * 'mapped' from
                                                             * 0x00460318
                                                             */
    public static final int NVACC_PAT_MONO2 = 0x0080431c; /*
                                                             * 'mapped' from
                                                             * 0x0046031c
                                                             */

    /* clip registers */
    public static final int NV16_CLP_FIFOFREE = 0x00802010; /* little endian */
    public static final int NVACC_CLP_TOPLEFT = 0x00802300; /*
                                                             * 'mapped' from
                                                             * 0x00450300
                                                             */
    public static final int NVACC_CLP_WIDHEIGHT = 0x00802304; /*
                                                                 * 'mapped' from
                                                                 * 0x00450304
                                                                 */

    /* used bitmap registers */
    public static final int NV16_BMP_FIFOFREE = 0x0080a010; /* little endian */
    public static final int NVACC_BMP_COLOR1A = 0x0080a3fc; /*
                                                             * 'mapped' from
                                                             * 0x006b03fc
                                                             */
    public static final int NVACC_BMP_UCRECTL_0 = 0x0080a400; /*
                                                                 * 'mapped' from
                                                                 * 0x006b0400
                                                                 */
    public static final int NVACC_BMP_UCRECSZ_0 = 0x0080a404; /*
                                                                 * 'mapped' from
                                                                 * 0x006b0404
                                                                 */

    /*
     * DDC1 support only requires DDC_SDA_MASK, DDC2 support requires
     * DDC_SDA_MASK and DDC_SCL_MASK
     */
    public static final int DDC_SDA_READ_MASK = (1 << 3);
    public static final int DDC_SCL_READ_MASK = (1 << 2);
    public static final int DDC_SDA_WRITE_MASK = (1 << 4);
    public static final int DDC_SCL_WRITE_MASK = (1 << 5);

}
