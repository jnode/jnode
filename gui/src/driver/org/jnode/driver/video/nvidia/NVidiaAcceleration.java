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
 * Hardware acceleration driver for NVidia video cards.
 * 
 * This class has been made possible with big help from <a
 * href="http://web.inter.nl.net/users/be-hold/BeOS/NVdriver/">Rudolf
 * Cornelissen</a>.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaAcceleration implements NVidiaConstants {

    /** NVidia card io */
    private final NVidiaVgaIO io;
    /** Card architecture */
    private final int architecture;

    /**
     * Initialize this instance.
     * 
     * @param io
     */
    public NVidiaAcceleration(NVidiaVgaIO io, int architecture) {
        this.io = io;
        this.architecture = architecture;
    }

    /**
     * rectangle fill - i.e. workspace and window background color span fill.
     * i.e. (selected) menuitem background color (Dano)
     */
    public void setupRectangle(int color) {
        /*
         * setup solid pattern: wait for room in fifo for pattern cmd if needed.
         * (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_PAT_FIFOFREE)) >> 2) < 5) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup pattern (writing 5 32bit words) */
        setACC(NVACC_PAT_SHAPE, 0); /* 0 = 8x8, 1 = 64x1, 2 = 1x64 */
        setACC(NVACC_PAT_COLOR0, 0xffffffff);
        setACC(NVACC_PAT_COLOR1, 0xffffffff);
        setACC(NVACC_PAT_MONO1, 0xffffffff);
        setACC(NVACC_PAT_MONO2, 0xffffffff);

        /*
         * ROP3 registers (Raster OPeration): wait for room in fifo for ROP cmd
         * if needed. (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_ROP_FIFOFREE)) >> 2) < 1) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup ROP (writing 1 32bit word) for GXcopy */
        setACC(NVACC_ROP_ROP3, 0xcc);

        /*
         * setup fill color: wait for room in fifo for bitmap cmd if needed.
         * (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_BMP_FIFOFREE)) >> 2) < 1) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup color (writing 1 32bit word) */
        setACC(NVACC_BMP_COLOR1A, color);
    }

    /**
     * Fill a rectangle with the color setup by {@link #setupRectangle(int)}.
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public void fillRectangle(int x, int y, int w, int h) {
        /*
         * instruct engine what to fill: wait for room in fifo for bitmap cmd if
         * needed. (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_BMP_FIFOFREE)) >> 2) < 2) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup fill (writing 2 32bit words) */
        setACC(NVACC_BMP_UCRECTL_0, ((x << 16) | (y & 0x0000ffff)));
        setACC(NVACC_BMP_UCRECSZ_0, (((w) << 16) | (h & 0x0000ffff)));
    }

    /**
     * Setup for rectangle invert - i.e. text cursor and text selection.
     */
    public void setupInvertRectangle() {
        /*
         * setup solid pattern: wait for room in fifo for pattern cmd if needed.
         * (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_PAT_FIFOFREE)) >> 2) < 5) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup pattern (writing 5 32bit words) */
        setACC(NVACC_PAT_SHAPE, 0); /* 0 = 8x8, 1 = 64x1, 2 = 1x64 */
        setACC(NVACC_PAT_COLOR0, 0xffffffff);
        setACC(NVACC_PAT_COLOR1, 0xffffffff);
        setACC(NVACC_PAT_MONO1, 0xffffffff);
        setACC(NVACC_PAT_MONO2, 0xffffffff);

        /*
         * ROP3 registers (Raster OPeration): wait for room in fifo for ROP cmd
         * if needed. (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_ROP_FIFOFREE)) >> 2) < 1) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup ROP (writing 1 32bit word) for GXinvert */
        setACC(NVACC_ROP_ROP3, 0x55);

        /*
         * reset fill color: wait for room in fifo for bitmap cmd if needed.
         * (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_BMP_FIFOFREE)) >> 2) < 1) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now reset color (writing 1 32bit word) */
        setACC(NVACC_BMP_COLOR1A, 0);
    }

    /**
     * Invert a given rectangle. Call {@link #setupInvertRectangle()} first.
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public void invertRectangle(int x, int y, int w, int h) {
        /*
         * instruct engine what to invert: wait for room in fifo for bitmap cmd
         * if needed. (fifo holds 256 32bit words: count those, not bytes)
         */
        while (((io.getReg16(NV16_BMP_FIFOFREE)) >> 2) < 2) {
            /* snooze a bit so I do not hammer the bus */
            snooze(10);
        }
        /* now setup invert (writing 2 32bit words) */
        setACC(NVACC_BMP_UCRECTL_0, ((x << 16) | (y & 0x0000ffff)));
        setACC(NVACC_BMP_UCRECSZ_0, (((w) << 16) | (h & 0x0000ffff)));
    }

    /**
     * Engine required init. AFAIK this must be done for every new screenmode.
     * 
     * @param frameBufferOffset Offset of active screen in framebuffer
     * @param bytesPerRow The number of bytes per row
     * @param memorySize The size of the cards memory in MB
     * @param bitsPerPixel The number of bits per pixel (8, 15, 16, 32)
     */
    public void accInit(int frameBufferOffset, int bytesPerRow, int memorySize, int bitsPerPixel) {
        /* setup PTIMER: */
        /* set timer numerator to 8 (in b0-15) */
        setACC(NVACC_PT_NUMERATOR, 0x00000008);
        /* set timer denominator to 3 (in b0-15) */
        setACC(NVACC_PT_DENOMINATR, 0x00000003);
        /* disable timer-alarm INT requests (b0) */
        setACC(NVACC_PT_INTEN, 0x00000000);
        /* reset timer-alarm INT status bit (b0) */
        setACC(NVACC_PT_INTSTAT, 0xffffffff);

        /* enable PRAMIN write access on pre NV10 before programming it! */
        if (architecture == NV04A) {
            /*
             * set framebuffer config: type = notiling, PRAMIN write access
             * enabled
             */
            io.setReg32(NV32_PFB_CONFIG_0, 0x00001114);
        }

        /** * PFIFO ** */
        /* (setup caches) */
        /* disable caches reassign */
        setACC(NVACC_PF_CACHES, 0x00000000);
        /* cache1 push0 access disabled */
        setACC(NVACC_PF_CACH1_PSH0, 0x00000000);
        /* cache1 pull0 access disabled */
        setACC(NVACC_PF_CACH1_PUL0, 0x00000000);
        /* cache1 push1 mode = pio */
        setACC(NVACC_PF_CACH1_PSH1, 0x00000000);
        /* cache1 DMA instance adress = 0 (b0-15) */
        setACC(NVACC_PF_CACH1_DMAI, 0x00000000);
        /* cache0 push0 access disabled */
        setACC(NVACC_PF_CACH0_PSH0, 0x00000000);
        /* cache0 pull0 access disabled */
        setACC(NVACC_PF_CACH0_PUL0, 0x00000000);
        /*
         * RAM HT (hash table(?)) baseadress = $10000 (b4-8), size = 4k, search =
         * 128 (byte offset between hash 'sets'(?))
         */
        /* (note: so(?) HT base is $00710000, last is $00710fff) */
        setACC(NVACC_PF_RAMHT, 0x03000100);
        /* RAM FC baseadress = $11000 (b3-8) (size is fixed to 0.5k(?)) */
        /* (note: so(?) FC base is $00711000, last is $007111ff) */
        setACC(NVACC_PF_RAMFC, 0x00000110);
        /* RAM RO baseadress = $11200 (b1-8), size = 0.5k */
        /* (note: so(?) RO base is $00711200, last is $007113ff) */
        /*
         * (note also: This means(?) the PRAMIN CTX registers are accessible
         * from base $00711400)
         */
        setACC(NVACC_PF_RAMRO, 0x00000112);
        /* PFIFO size: ch0-15 = 512 bytes, ch16-31 = 124 bytes */
        setACC(NVACC_PF_SIZE, 0x0000ffff);
        /* cache1 hash instance = $ffff (b0-15) */
        setACC(NVACC_PF_CACH1_HASH, 0x0000ffff);
        /* disable all PFIFO INTs */
        setACC(NVACC_PF_INTEN, 0x00000000);
        /* reset all PFIFO INT status bits */
        setACC(NVACC_PF_INTSTAT, 0xffffffff);
        /* cache0 pull0 engine = acceleration engine (graphics) */
        setACC(NVACC_PF_CACH0_PUL1, 0x00000001);
        /* cache1 push0 access enabled */
        setACC(NVACC_PF_CACH1_PSH0, 0x00000001);
        /* cache1 pull0 access enabled */
        setACC(NVACC_PF_CACH1_PUL0, 0x00000001);
        /* cache1 pull1 engine = acceleration engine (graphics) */
        setACC(NVACC_PF_CACH1_PUL1, 0x00000001);
        /* enable PFIFO caches reassign */
        setACC(NVACC_PF_CACHES, 0x00000001);

        /** * PRAMIN ** */
        /* RAMHT space (hash-table(?)) */
        /* (first set) */
        setACC(NVACC_HT_HANDL_00, 0x80000010); /* 32bit handle */
        setACC(NVACC_HT_VALUE_00, 0x80011145); /*
                                                 * instance $1145, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_01, 0x80000011); /* 32bit handle */
        setACC(NVACC_HT_VALUE_01, 0x80011146); /*
                                                 * instance $1146, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_02, 0x80000012); /* 32bit handle */
        setACC(NVACC_HT_VALUE_02, 0x80011147); /*
                                                 * instance $1147, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_03, 0x80000013); /* 32bit handle */
        setACC(NVACC_HT_VALUE_03, 0x80011148); /*
                                                 * instance $1148, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_04, 0x80000014); /* 32bit handle */
        setACC(NVACC_HT_VALUE_04, 0x80011149); /*
                                                 * instance $1149, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_05, 0x80000015); /* 32bit handle */
        setACC(NVACC_HT_VALUE_05, 0x8001114a); /*
                                                 * instance $114a, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_06, 0x80000016); /* 32bit handle */
        if (architecture != NV04A) {
            setACC(NVACC_HT_VALUE_06, 0x80011150); /*
                                                     * instance $1150, engine =
                                                     * acc engine, CHID = $00
                                                     */
        } else {
            setACC(NVACC_HT_VALUE_06, 0x8001114f); /*
                                                     * instance $114f, engine =
                                                     * acc engine, CHID = $00
                                                     */
        }
        /* (second set) */
        setACC(NVACC_HT_HANDL_10, 0x80000000); /* 32bit handle */
        setACC(NVACC_HT_VALUE_10, 0x80011142); /*
                                                 * instance $1142, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_11, 0x80000001); /* 32bit handle */
        setACC(NVACC_HT_VALUE_11, 0x80011143); /*
                                                 * instance $1143, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_12, 0x80000002); /* 32bit handle */
        setACC(NVACC_HT_VALUE_12, 0x80011144); /*
                                                 * instance $1144, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_13, 0x80000003); /* 32bit handle */
        setACC(NVACC_HT_VALUE_13, 0x8001114b); /*
                                                 * instance $114b, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_14, 0x80000004); /* 32bit handle */
        setACC(NVACC_HT_VALUE_14, 0x8001114c); /*
                                                 * instance $114c, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_15, 0x80000005); /* 32bit handle */
        setACC(NVACC_HT_VALUE_15, 0x8001114d); /*
                                                 * instance $114d, engine = acc
                                                 * engine, CHID = $00
                                                 */
        setACC(NVACC_HT_HANDL_16, 0x80000006); /* 32bit handle */
        setACC(NVACC_HT_VALUE_16, 0x8001114e); /*
                                                 * instance $114e, engine = acc
                                                 * engine, CHID = $00
                                                 */
        if (architecture != NV04A) {
            setACC(NVACC_HT_HANDL_17, 0x80000007); /* 32bit handle */
            setACC(NVACC_HT_VALUE_17, 0x8001114f); /*
                                                     * instance $114f, engine =
                                                     * acc engine, CHID = $00
                                                     */
        }
        /*
         * program CTX registers: CTX1 is mostly done later (colorspace
         * dependant)
         */
        /* (setup 'root' set first) */
        setACC(NVACC_PR_CTX0_R, 0x00003000); /*
                                                 * NVclass = NVroot, chromakey
                                                 * and userclip enabled
                                                 */
        /*
         * fixme: CTX1_R should reflect RAM amount? (no influence on current
         * used functions)
         */
        setACC(NVACC_PR_CTX1_R, 0x01ffffff); /* cardmemory mask(?) */
        setACC(NVACC_PR_CTX2_R, 0x00000002); /* ??? */
        setACC(NVACC_PR_CTX3_R, 0x00000002); /* ??? */
        /* (setup set '0') */
        setACC(NVACC_PR_CTX0_0, 0x01008043); /*
                                                 * NVclass $043, patchcfg
                                                 * ROP_AND, nv10+: little endian
                                                 */
        setACC(NVACC_PR_CTX2_0, 0x00000000); /*
                                                 * DMA0 and DMA1 instance
                                                 * invalid
                                                 */
        setACC(NVACC_PR_CTX3_0, 0x00000000); /* method traps disabled */
        /* (setup set '1') */
        setACC(NVACC_PR_CTX0_1, 0x01008019); /*
                                                 * NVclass $019, patchcfg
                                                 * ROP_AND, nv10+: little endian
                                                 */
        setACC(NVACC_PR_CTX2_1, 0x00000000); /*
                                                 * DMA0 and DMA1 instance
                                                 * invalid
                                                 */
        setACC(NVACC_PR_CTX3_1, 0x00000000); /* method traps disabled */
        /* (setup set '2') */
        setACC(NVACC_PR_CTX0_2, 0x01008018); /*
                                                 * NVclass $018, patchcfg
                                                 * ROP_AND, nv10+: little endian
                                                 */
        setACC(NVACC_PR_CTX2_2, 0x00000000); /*
                                                 * DMA0 and DMA1 instance
                                                 * invalid
                                                 */
        setACC(NVACC_PR_CTX3_2, 0x00000000); /* method traps disabled */
        /* (setup set '3') */
        setACC(NVACC_PR_CTX0_3, 0x01008021); /*
                                                 * NVclass $021, patchcfg
                                                 * ROP_AND, nv10+: little endian
                                                 */
        setACC(NVACC_PR_CTX2_3, 0x00000000); /*
                                                 * DMA0 and DMA1 instance
                                                 * invalid
                                                 */
        setACC(NVACC_PR_CTX3_3, 0x00000000); /* method traps disabled */
        /* (setup set '4') */
        setACC(NVACC_PR_CTX0_4, 0x0100805f); /*
                                                 * NVclass $05f, patchcfg
                                                 * ROP_AND, nv10+: little endian
                                                 */
        setACC(NVACC_PR_CTX2_4, 0x00000000); /*
                                                 * DMA0 and DMA1 instance
                                                 * invalid
                                                 */
        setACC(NVACC_PR_CTX3_4, 0x00000000); /* method traps disabled */
        /* (setup set '5') */
        setACC(NVACC_PR_CTX0_5, 0x0100804b); /*
                                                 * NVclass $04b, patchcfg
                                                 * ROP_AND, nv10+: little endian
                                                 */
        setACC(NVACC_PR_CTX2_5, 0x00000000); /*
                                                 * DMA0 and DMA1 instance
                                                 * invalid
                                                 */
        setACC(NVACC_PR_CTX3_5, 0x00000000); /* method traps disabled */
        /* (setup set '6') */
        setACC(NVACC_PR_CTX0_6, 0x0100a048);
        /*
         * NVclass $048, patchcfg ROP_AND, userclip enable, nv10+: little endian
         */
        setACC(NVACC_PR_CTX1_6, 0x00000d01); /* format is A8RGB24, MSB mono */
        setACC(NVACC_PR_CTX2_6, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_6, 0x00000000); /* method traps disabled */
        /* (setup set '7') */
        if (architecture != NV04A) {
            setACC(NVACC_PR_CTX0_7, 0x0300a094);
            /*
             * NVclass $094, patchcfg ROP_AND, userclip enable, context surface0
             * valid, nv10+: little endian
             */
        } else {
            setACC(NVACC_PR_CTX0_7, 0x0300a054);
            /*
             * NVclass $054, patchcfg ROP_AND, userclip enable, context surface0
             * valid
             */
        }
        setACC(NVACC_PR_CTX1_7, 0x00000d01); /* format is A8RGB24, MSB mono */
        setACC(NVACC_PR_CTX2_7, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_7, 0x00000000); /* method traps disabled */
        /* (setup set '8') */
        if (architecture != NV04A) {
            setACC(NVACC_PR_CTX0_8, 0x0300a095);
            /*
             * NVclass $095, patchcfg ROP_AND, userclip enable, context surface0
             * valid, nv10+: little endian
             */
        } else {
            setACC(NVACC_PR_CTX0_8, 0x0300a055);
            /*
             * NVclass $055, patchcfg ROP_AND, userclip enable, context surface0
             * valid
             */
        }
        setACC(NVACC_PR_CTX1_8, 0x00000d01); /* format is A8RGB24, MSB mono */
        setACC(NVACC_PR_CTX2_8, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_8, 0x00000000); /* method traps disabled */
        /* (setup set '9') */
        setACC(NVACC_PR_CTX0_9, 0x00000058); /*
                                                 * NVclass $058, nv10+: little
                                                 * endian
                                                 */
        setACC(NVACC_PR_CTX2_9, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_9, 0x00000000); /* method traps disabled */
        /* (setup set 'A') */
        setACC(NVACC_PR_CTX0_A, 0x00000059); /*
                                                 * NVclass $059, nv10+: little
                                                 * endian
                                                 */
        setACC(NVACC_PR_CTX2_A, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_A, 0x00000000); /* method traps disabled */
        /* (setup set 'B') */
        setACC(NVACC_PR_CTX0_B, 0x0000005a); /*
                                                 * NVclass $05a, nv10+: little
                                                 * endian
                                                 */
        setACC(NVACC_PR_CTX2_B, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_B, 0x00000000); /* method traps disabled */
        /* (setup set 'C') */
        setACC(NVACC_PR_CTX0_C, 0x0000005b); /*
                                                 * NVclass $05b, nv10+: little
                                                 * endian
                                                 */
        setACC(NVACC_PR_CTX2_C, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_C, 0x00000000); /* method traps disabled */
        /* (setup set 'D') */
        if (architecture != NV04A) {
            setACC(NVACC_PR_CTX0_D, 0x00000093); /*
                                                     * NVclass $093, nv10+:
                                                     * little endian
                                                     */
        } else {
            setACC(NVACC_PR_CTX0_D, 0x0300a01c);
            /*
             * NVclass $01c, patchcfg ROP_AND, userclip enable, context surface0
             * valid
             */
        }
        setACC(NVACC_PR_CTX2_D, 0x11401140); /* DMA0, DMA1 instance = $1140 */
        setACC(NVACC_PR_CTX3_D, 0x00000000); /* method traps disabled */
        /* (setup set 'E' if needed) */
        if (architecture != NV04A) {
            setACC(NVACC_PR_CTX0_E, 0x0300a01c);
            /*
             * NVclass $01c, patchcfg ROP_AND, userclip enable, context surface0
             * valid, nv10+: little endian
             */
            setACC(NVACC_PR_CTX2_E, 0x11401140); /*
                                                     * DMA0, DMA1 instance =
                                                     * $1140
                                                     */
            setACC(NVACC_PR_CTX3_E, 0x00000000); /* method traps disabled */
        }

        /** * PGRAPH ** */
        if (architecture != NV04A) {
            /* set resetstate for most function blocks */
            setACC(NVACC_DEBUG0, 0x0003ffff);
            /* init some function blocks */
            setACC(NVACC_DEBUG1, 0x00118701);
            setACC(NVACC_DEBUG2, 0x24f82ad9);
            setACC(NVACC_DEBUG3, 0x55de0030);
            /* end resetstate for the function blocks */
            setACC(NVACC_DEBUG0, 0x00000000);
            /* disable specific functions */
            setACC(NVACC_NV10_DEBUG4, 0);
        } else {
            /* init some function blocks */
            setACC(NVACC_DEBUG0, 0x1231c001);
            setACC(NVACC_DEBUG1, 0x72111101);
            setACC(NVACC_DEBUG2, 0x11d5f071);
            setACC(NVACC_DEBUG3, 0x10d4ff31);
        }

        /* reset all cache sets */
        setACC(NVACC_CACHE1_1, 0);
        setACC(NVACC_CACHE1_2, 0);
        setACC(NVACC_CACHE1_3, 0);
        setACC(NVACC_CACHE1_4, 0);
        setACC(NVACC_CACHE1_5, 0);
        setACC(NVACC_CACHE2_1, 0);
        setACC(NVACC_CACHE2_2, 0);
        setACC(NVACC_CACHE2_3, 0);
        setACC(NVACC_CACHE2_4, 0);
        setACC(NVACC_CACHE2_5, 0);
        setACC(NVACC_CACHE3_1, 0);
        setACC(NVACC_CACHE3_2, 0);
        setACC(NVACC_CACHE3_3, 0);
        setACC(NVACC_CACHE3_4, 0);
        setACC(NVACC_CACHE3_5, 0);
        setACC(NVACC_CACHE4_1, 0);
        setACC(NVACC_CACHE4_2, 0);
        setACC(NVACC_CACHE4_3, 0);
        setACC(NVACC_CACHE4_4, 0);
        setACC(NVACC_CACHE4_5, 0);
        if (architecture != NV04A)
            setACC(NVACC_NV10_CACHE5_1, 0);
        setACC(NVACC_CACHE5_2, 0);
        setACC(NVACC_CACHE5_3, 0);
        setACC(NVACC_CACHE5_4, 0);
        setACC(NVACC_CACHE5_5, 0);
        if (architecture != NV04A)
            setACC(NVACC_NV10_CACHE6_1, 0);
        setACC(NVACC_CACHE6_2, 0);
        setACC(NVACC_CACHE6_3, 0);
        setACC(NVACC_CACHE6_4, 0);
        setACC(NVACC_CACHE6_5, 0);
        if (architecture != NV04A)
            setACC(NVACC_NV10_CACHE7_1, 0);
        setACC(NVACC_CACHE7_2, 0);
        setACC(NVACC_CACHE7_3, 0);
        setACC(NVACC_CACHE7_4, 0);
        setACC(NVACC_CACHE7_5, 0);
        if (architecture != NV04A)
            setACC(NVACC_NV10_CACHE8_1, 0);
        setACC(NVACC_CACHE8_2, 0);
        setACC(NVACC_CACHE8_3, 0);
        setACC(NVACC_CACHE8_4, 0);
        setACC(NVACC_CACHE8_5, 0);

        if (architecture != NV04A) {
            /* reset (disable) context switch stuff */
            setACC(NVACC_NV10_CTX_SW1, 0);
            setACC(NVACC_NV10_CTX_SW2, 0);
            setACC(NVACC_NV10_CTX_SW3, 0);
            setACC(NVACC_NV10_CTX_SW4, 0);
            setACC(NVACC_NV10_CTX_SW5, 0);
        }

        /* setup accesible card memory range for acc engine */
        setACC(NVACC_BBASE0, 0x00000000);
        setACC(NVACC_BBASE1, 0x00000000);
        setACC(NVACC_BBASE2, 0x00000000);
        setACC(NVACC_BBASE3, 0x00000000);
        setACC(NVACC_BLIMIT0, ((memorySize << 20) - 1));
        setACC(NVACC_BLIMIT1, ((memorySize << 20) - 1));
        setACC(NVACC_BLIMIT2, ((memorySize << 20) - 1));
        setACC(NVACC_BLIMIT3, ((memorySize << 20) - 1));
        if (architecture >= NV10A) {
            setACC(NVACC_NV10_BBASE4, 0x00000000);
            setACC(NVACC_NV10_BBASE5, 0x00000000);
            setACC(NVACC_NV10_BLIMIT4, ((memorySize << 20) - 1));
            setACC(NVACC_NV10_BLIMIT5, ((memorySize << 20) - 1));
        }
        if (architecture >= NV20A) {
            /*
             * fixme(?): assuming more BLIMIT registers here: Then how about
             * BBASE6-9? (linux fixed value 'BLIMIT6-9' 0x01ffffff)
             */
            setACC(NVACC_NV20_BLIMIT6, ((memorySize << 20) - 1));
            setACC(NVACC_NV20_BLIMIT7, ((memorySize << 20) - 1));
            setACC(NVACC_NV20_BLIMIT8, ((memorySize << 20) - 1));
            setACC(NVACC_NV20_BLIMIT9, ((memorySize << 20) - 1));
        }

        /* disable all acceleration engine INT reguests */
        setACC(NVACC_ACC_INTE, 0x00000000);

        /* reset all acceration engine INT status bits */
        setACC(NVACC_ACC_INTS, 0xffffffff);
        if (architecture != NV04A) {
            /* context control enabled */
            setACC(NVACC_NV10_CTX_CTRL, 0x10010100);
            /* all acceleration buffers, pitches and colors are valid */
            setACC(NVACC_NV10_ACC_STAT, 0xffffffff);
        } else {
            /* context control enabled */
            setACC(NVACC_NV04_CTX_CTRL, 0x10010100);
            /* all acceleration buffers, pitches and colors are valid */
            setACC(NVACC_NV04_ACC_STAT, 0xffffffff);
        }
        /* enable acceleration engine command FIFO */
        setACC(NVACC_FIFO_EN, 0x00000001);
        /* pattern shape value = 8x8, 2 color */
        setACC(NVACC_PAT_SHP, 0x00000000);
        if (architecture != NV04A) {
            /* surface type is non-swizzle */
            setACC(NVACC_NV10_SURF_TYP, 0x00000001);
        } else {
            /* surface type is non-swizzle */
            setACC(NVACC_NV04_SURF_TYP, 0x00000001);
        }

        /* Set pixel width and format */
        switch (bitsPerPixel) {
            case 8:
                /* acc engine */
                setACC(NVACC_FORMATS, 0x00001010);
                if (architecture < NV30A)
                    setACC(NVACC_BPIXEL, 0x00111111); /*
                                                         * set depth 0-5: 4 bits
                                                         * per color
                                                         */
                else
                    setACC(NVACC_BPIXEL, 0x00000021); /*
                                                         * set depth 0-1: 5 bits
                                                         * per color
                                                         */
                setACC(NVACC_STRD_FMT, 0x03020202);
                /* PRAMIN */
                setACC(NVACC_PR_CTX1_0, 0x00000302); /*
                                                         * format is X24Y8, LSB
                                                         * mono
                                                         */
                setACC(NVACC_PR_CTX1_1, 0x00000302); /*
                                                         * format is X24Y8, LSB
                                                         * mono
                                                         */
                setACC(NVACC_PR_CTX1_2, 0x00000202); /*
                                                         * format is X16A8Y8,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_3, 0x00000302); /*
                                                         * format is X24Y8, LSB
                                                         * mono
                                                         */
                setACC(NVACC_PR_CTX1_4, 0x00000302); /*
                                                         * format is X24Y8, LSB
                                                         * mono
                                                         */
                setACC(NVACC_PR_CTX1_5, 0x00000302); /*
                                                         * format is X24Y8, LSB
                                                         * mono
                                                         */
                setACC(NVACC_PR_CTX1_9, 0x00000302); /*
                                                         * format is X24Y8, LSB
                                                         * mono
                                                         */
                setACC(NVACC_PR_CTX2_9, 0x00000302); /*
                                                         * dma_instance 0 valid,
                                                         * instance 1 invalid
                                                         */
                setACC(NVACC_PR_CTX1_B, 0x00000000); /* format is invalid */
                setACC(NVACC_PR_CTX1_C, 0x00000000); /* format is invalid */
                if (architecture == NV04A) {
                    setACC(NVACC_PR_CTX1_D, 0x00000302); /*
                                                             * format is X24Y8,
                                                             * LSB mono
                                                             */
                } else {
                    setACC(NVACC_PR_CTX1_D, 0x00000000); /* format is invalid */
                    setACC(NVACC_PR_CTX1_E, 0x00000302); /*
                                                             * format is X24Y8,
                                                             * LSB mono
                                                             */
                }
                break;
            case 15:
                /* acc engine */
                setACC(NVACC_FORMATS, 0x00002071);
                if (architecture < NV30A)
                    setACC(NVACC_BPIXEL, 0x00226222); /*
                                                         * set depth 0-5: 4 bits
                                                         * per color
                                                         */
                else
                    setACC(NVACC_BPIXEL, 0x00000042); /*
                                                         * set depth 0-1: 5 bits
                                                         * per color
                                                         */
                setACC(NVACC_STRD_FMT, 0x09080808);
                /* PRAMIN */
                setACC(NVACC_PR_CTX1_0, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_1, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_2, 0x00000802); /*
                                                         * format is X16A1RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_3, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_4, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_5, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_9, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX2_9, 0x00000902); /*
                                                         * dma_instance 0 valid,
                                                         * instance 1 invalid
                                                         */
                if (architecture == NV04A) {
                    setACC(NVACC_PR_CTX1_B, 0x00000702); /*
                                                             * format is
                                                             * X1RGB15, LSB mono
                                                             */
                    setACC(NVACC_PR_CTX1_C, 0x00000702); /*
                                                             * format is
                                                             * X1RGB15, LSB mono
                                                             */
                } else {
                    setACC(NVACC_PR_CTX1_B, 0x00000902); /*
                                                             * format is
                                                             * X17RGB15, LSB
                                                             * mono
                                                             */
                    setACC(NVACC_PR_CTX1_C, 0x00000902); /*
                                                             * format is
                                                             * X17RGB15, LSB
                                                             * mono
                                                             */
                    setACC(NVACC_PR_CTX1_E, 0x00000902); /*
                                                             * format is
                                                             * X17RGB15, LSB
                                                             * mono
                                                             */
                }
                setACC(NVACC_PR_CTX1_D, 0x00000902); /*
                                                         * format is X17RGB15,
                                                         * LSB mono
                                                         */
                break;
            case 16:
                /* acc engine */
                setACC(NVACC_FORMATS, 0x000050C2);
                if (architecture < NV30A)
                    setACC(NVACC_BPIXEL, 0x00556555); /*
                                                         * set depth 0-5: 4 bits
                                                         * per color
                                                         */
                else
                    setACC(NVACC_BPIXEL, 0x000000a5); /*
                                                         * set depth 0-1: 5 bits
                                                         * per color
                                                         */
                if (architecture == NV04A)
                    setACC(NVACC_STRD_FMT, 0x0c0b0b0b);
                else
                    setACC(NVACC_STRD_FMT, 0x000b0b0c);
                /* PRAMIN */
                setACC(NVACC_PR_CTX1_0, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_1, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_2, 0x00000b02); /*
                                                         * format is A16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_3, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_4, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_5, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_9, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX2_9, 0x00000c02); /*
                                                         * dma_instance 0 valid,
                                                         * instance 1 invalid
                                                         */
                if (architecture == NV04A) {
                    setACC(NVACC_PR_CTX1_B, 0x00000702); /*
                                                             * format is
                                                             * X1RGB15, LSB mono
                                                             */
                    setACC(NVACC_PR_CTX1_C, 0x00000702); /*
                                                             * format is
                                                             * X1RGB15, LSB mono
                                                             */
                } else {
                    setACC(NVACC_PR_CTX1_B, 0x00000c02); /*
                                                             * format is
                                                             * X16RGB16, LSB
                                                             * mono
                                                             */
                    setACC(NVACC_PR_CTX1_C, 0x00000c02); /*
                                                             * format is
                                                             * X16RGB16, LSB
                                                             * mono
                                                             */
                    setACC(NVACC_PR_CTX1_E, 0x00000c02); /*
                                                             * format is
                                                             * X16RGB16, LSB
                                                             * mono
                                                             */
                }
                setACC(NVACC_PR_CTX1_D, 0x00000c02); /*
                                                         * format is X16RGB16,
                                                         * LSB mono
                                                         */
                break;
            case 32:
                /* acc engine */
                setACC(NVACC_FORMATS, 0x000070e5);
                if (architecture < NV30A)
                    setACC(NVACC_BPIXEL, 0x0077d777); /*
                                                         * set depth 0-5: 4 bits
                                                         * per color
                                                         */
                else
                    setACC(NVACC_BPIXEL, 0x000000e7); /*
                                                         * set depth 0-1: 5 bits
                                                         * per color
                                                         */
                setACC(NVACC_STRD_FMT, 0x0e0d0d0d);
                /* PRAMIN */
                setACC(NVACC_PR_CTX1_0, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_1, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_2, 0x00000d02); /*
                                                         * format is A8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_3, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_4, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_5, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_9, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX2_9, 0x00000e02); /*
                                                         * dma_instance 0 valid,
                                                         * instance 1 invalid
                                                         */
                setACC(NVACC_PR_CTX1_B, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_C, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                setACC(NVACC_PR_CTX1_D, 0x00000e02); /*
                                                         * format is X8RGB24,
                                                         * LSB mono
                                                         */
                if (architecture >= NV10A)
                    setACC(NVACC_PR_CTX1_E, 0x00000e02); /*
                                                             * format is
                                                             * X8RGB24, LSB mono
                                                             */
                break;
            default:
                throw new IllegalArgumentException("ACC: init, invalid bit depth " + bitsPerPixel);
        }

        /* setup some extra stuff for NV30A */
        if (architecture == NV30A) {
            /*
             * fixme: Does not belong here (and not needed?)
             * if(!chip->flatPanel) { chip->PRAMDAC0[0x0578/4] = state->vpllB;
             * //0x00680578 = ??? never modified! chip->PRAMDAC0[0x057C/4] =
             * state->vpll2B; //0x0068057c = ??? never modified! }
             */

            /* activate Zcullflush(?) */
            setACC(NVACC_DEBUG3, (getACC(NVACC_DEBUG3) | 0x00000001));
            /* unknown */
            setACC(NVACC_NV30_WHAT, (getACC(NVACC_NV30_WHAT) | 0x00040000));
        }

        /** * setup screen location and pitch ** */
        switch (architecture) {
            case NV04A:
            case NV10A:
                /* location of active screen in framebuffer */
                setACC(NVACC_OFFSET0, frameBufferOffset);
                setACC(NVACC_OFFSET1, frameBufferOffset);
                setACC(NVACC_OFFSET2, frameBufferOffset);
                setACC(NVACC_OFFSET3, frameBufferOffset);
                setACC(NVACC_OFFSET4, frameBufferOffset);
                setACC(NVACC_OFFSET5, frameBufferOffset);

                /* setup buffer pitch */
                setACC(NVACC_PITCH0, bytesPerRow & 0x0000ffff);
                setACC(NVACC_PITCH1, bytesPerRow & 0x0000ffff);
                setACC(NVACC_PITCH2, bytesPerRow & 0x0000ffff);
                setACC(NVACC_PITCH3, bytesPerRow & 0x0000ffff);
                setACC(NVACC_PITCH4, bytesPerRow & 0x0000ffff);
                break;
            case NV20A:
            case NV30A:
                /* location of active screen in framebuffer */
                setACC(NVACC_NV20_OFFSET0, frameBufferOffset);
                setACC(NVACC_NV20_OFFSET1, frameBufferOffset);
                setACC(NVACC_NV20_OFFSET2, frameBufferOffset);
                setACC(NVACC_NV20_OFFSET3, frameBufferOffset);

                /* setup buffer pitch */
                setACC(NVACC_NV20_PITCH0, bytesPerRow & 0x0000ffff);
                setACC(NVACC_NV20_PITCH1, bytesPerRow & 0x0000ffff);
                setACC(NVACC_NV20_PITCH2, bytesPerRow & 0x0000ffff);
                setACC(NVACC_NV20_PITCH3, bytesPerRow & 0x0000ffff);
                break;
        }

        /** * setup tile and pipe stuff ** */
        if (architecture >= NV10A) {
            /*
             * fixme: setup elsewhere (does not belong here):
             * chip->PRAMDAC[0x00000404/4] |= (1 << 25);//0x00680404 = ???
             */

            /* setup acc engine tile stuff: */
            /* reset tile adresses */
            setACC(NVACC_NV10_FBTIL0AD, 0);
            setACC(NVACC_NV10_FBTIL1AD, 0);
            setACC(NVACC_NV10_FBTIL2AD, 0);
            setACC(NVACC_NV10_FBTIL3AD, 0);
            setACC(NVACC_NV10_FBTIL4AD, 0);
            setACC(NVACC_NV10_FBTIL5AD, 0);
            setACC(NVACC_NV10_FBTIL6AD, 0);
            setACC(NVACC_NV10_FBTIL7AD, 0);
            /* copy tile setup stuff from 'source' to acc engine */
            if (architecture >= NV20A) {
                /* unknown: */
                setACC(NVACC_NV20_WHAT0, getACC(NVACC_NV20_FBWHAT0));
                setACC(NVACC_NV20_WHAT1, getACC(NVACC_NV20_FBWHAT1));
            }
            /* tile 0: */
            /* tile invalid, tile adress = $00000 (18bit) */
            setACC(NVACC_NV10_TIL0AD, getACC(NVACC_NV10_FBTIL0AD));
            /* set tile end adress (18bit) */
            setACC(NVACC_NV10_TIL0ED, getACC(NVACC_NV10_FBTIL0ED));
            /* set tile size pitch (8bit: b8-15) */
            setACC(NVACC_NV10_TIL0PT, getACC(NVACC_NV10_FBTIL0PT));
            /* set tile status */
            setACC(NVACC_NV10_TIL0ST, getACC(NVACC_NV10_FBTIL0ST));
            /* tile 1: */
            setACC(NVACC_NV10_TIL1AD, getACC(NVACC_NV10_FBTIL1AD));
            setACC(NVACC_NV10_TIL1ED, getACC(NVACC_NV10_FBTIL1ED));
            setACC(NVACC_NV10_TIL1PT, getACC(NVACC_NV10_FBTIL1PT));
            setACC(NVACC_NV10_TIL1ST, getACC(NVACC_NV10_FBTIL1ST));
            /* tile 2: */
            setACC(NVACC_NV10_TIL2AD, getACC(NVACC_NV10_FBTIL2AD));
            setACC(NVACC_NV10_TIL2ED, getACC(NVACC_NV10_FBTIL2ED));
            setACC(NVACC_NV10_TIL2PT, getACC(NVACC_NV10_FBTIL2PT));
            setACC(NVACC_NV10_TIL2ST, getACC(NVACC_NV10_FBTIL2ST));
            /* tile 3: */
            setACC(NVACC_NV10_TIL3AD, getACC(NVACC_NV10_FBTIL3AD));
            setACC(NVACC_NV10_TIL3ED, getACC(NVACC_NV10_FBTIL3ED));
            setACC(NVACC_NV10_TIL3PT, getACC(NVACC_NV10_FBTIL3PT));
            setACC(NVACC_NV10_TIL3ST, getACC(NVACC_NV10_FBTIL3ST));
            /* tile 4: */
            setACC(NVACC_NV10_TIL4AD, getACC(NVACC_NV10_FBTIL4AD));
            setACC(NVACC_NV10_TIL4ED, getACC(NVACC_NV10_FBTIL4ED));
            setACC(NVACC_NV10_TIL4PT, getACC(NVACC_NV10_FBTIL4PT));
            setACC(NVACC_NV10_TIL4ST, getACC(NVACC_NV10_FBTIL4ST));
            /* tile 5: */
            setACC(NVACC_NV10_TIL5AD, getACC(NVACC_NV10_FBTIL5AD));
            setACC(NVACC_NV10_TIL5ED, getACC(NVACC_NV10_FBTIL5ED));
            setACC(NVACC_NV10_TIL5PT, getACC(NVACC_NV10_FBTIL5PT));
            setACC(NVACC_NV10_TIL5ST, getACC(NVACC_NV10_FBTIL5ST));
            /* tile 6: */
            setACC(NVACC_NV10_TIL6AD, getACC(NVACC_NV10_FBTIL6AD));
            setACC(NVACC_NV10_TIL6ED, getACC(NVACC_NV10_FBTIL6ED));
            setACC(NVACC_NV10_TIL6PT, getACC(NVACC_NV10_FBTIL6PT));
            setACC(NVACC_NV10_TIL6ST, getACC(NVACC_NV10_FBTIL6ST));
            /* tile 7: */
            setACC(NVACC_NV10_TIL7AD, getACC(NVACC_NV10_FBTIL7AD));
            setACC(NVACC_NV10_TIL7ED, getACC(NVACC_NV10_FBTIL7ED));
            setACC(NVACC_NV10_TIL7PT, getACC(NVACC_NV10_FBTIL7PT));
            setACC(NVACC_NV10_TIL7ST, getACC(NVACC_NV10_FBTIL7ST));

            /* setup pipe */
            /* set eyetype to local, lightning is off */
            setACC(NVACC_NV10_XFMOD0, 0x10000000);
            /* disable all lights */
            setACC(NVACC_NV10_XFMOD1, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00000040);
            setACC(NVACC_NV10_PIPEDAT, 0x00000008);

            setACC(NVACC_NV10_PIPEADR, 0x00000200);
            for (int cnt = 0; cnt < (3 * 16); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00000040);
            setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00000800);
            for (int cnt = 0; cnt < (16 * 16); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            /* turn lightning on */
            setACC(NVACC_NV10_XFMOD0, 0x30000000);
            /* set light 1 to infinite type, other lights remain off */
            setACC(NVACC_NV10_XFMOD1, 0x00000004);

            setACC(NVACC_NV10_PIPEADR, 0x00006400);
            for (int cnt = 0; cnt < (59 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00006800);
            for (int cnt = 0; cnt < (47 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00006c00);
            for (int cnt = 0; cnt < (3 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00007000);
            for (int cnt = 0; cnt < (19 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00007400);
            for (int cnt = 0; cnt < (12 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00007800);
            for (int cnt = 0; cnt < (12 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00004400);
            for (int cnt = 0; cnt < (8 * 4); cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00000000);
            for (int cnt = 0; cnt < 16; cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);

            setACC(NVACC_NV10_PIPEADR, 0x00000040);
            for (int cnt = 0; cnt < 4; cnt++)
                setACC(NVACC_NV10_PIPEDAT, 0x00000000);
        }

        /** * setup acceleration engine command shortcuts (so via fifo) ** */
        /* (b31 = 1 selects 'config' function?) */
        setACC(NVACC_FIFO_00800000, 0x80000000); /* Raster OPeration */
        setACC(NVACC_FIFO_00802000, 0x80000001); /* Clip */
        setACC(NVACC_FIFO_00804000, 0x80000002); /* Pattern */
        setACC(NVACC_FIFO_00806000, 0x80000010); /* Pixmap (not used) */
        setACC(NVACC_FIFO_00808000, 0x80000011); /* Blit */
        setACC(NVACC_FIFO_0080a000, 0x80000012); /* Bitmap */
        setACC(NVACC_FIFO_0080c000, 0x80000016); /* Line (not used) */
        setACC(NVACC_FIFO_0080e000, 0x80000014); /* ??? (not used) */

        /*
         * do first actual acceleration engine command: setup clipping region
         * (workspace size) to 32768 x 32768 pixels: wait for room in fifo for
         * clipping cmd if needed. (fifo holds 256 32bit words: count those, not
         * bytes)
         */
        while (((io.getReg16(NV16_CLP_FIFOFREE)) >> 2) < 2) {
            try {
                /* snooze a bit so I do not hammer the bus */
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                // Ignore
            }
        }
        /* now setup clipping (writing 2 32bit words) */
        setACC(NVACC_CLP_TOPLEFT, 0x00000000);
        setACC(NVACC_CLP_WIDHEIGHT, 0x80008000);
    }

    /**
     * Wait until engine completely idle.
     */
    /*
     * private final void waitUntilIdle() { while (getACC(NVACC_STATUS) != 0) {
     * snooze(25); }
     */

    /**
     * Read from the acceleration engine registers.
     * 
     * @param reg
     * @return int
     */
    private final int getACC(int reg) {
        return io.getReg32(reg);
    }

    /**
     * Write to the acceleration engine registers.
     * 
     * @param reg
     */
    private final void setACC(int reg, int value) {
        io.setReg32(reg, value);
    }

    private final void snooze(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            // Ignore
        }
    }
}
