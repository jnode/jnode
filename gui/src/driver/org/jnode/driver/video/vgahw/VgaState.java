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
 
package org.jnode.driver.video.vgahw;

import java.awt.image.IndexColorModel;

import org.jnode.system.MemoryResource;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class VgaState {

    // private static final Logger log = Logger.getLogger(VgaState.class);
    private final int[] seq;
    private final int[] crt;
    private final int[] gra;
    private final int[] att;
    private int misc;
    private IndexColorModel palette;
    private byte[] fonts;
    private static final int FONT_STATE_SIZE = 0x9600;

    protected static final int DEF_SEQ_LENGTH = 5;
    protected static final int DEF_CRT_LENGTH = 25;
    protected static final int DEF_GRA_LENGTH = 9;
    protected static final int DEF_ATT_LENGTH = 21;
    protected static final int DEF_MISC = 0xE3;

    /**
     * Create a new instance
     */
    public VgaState() {
        this((IndexColorModel) null);
    }

    /**
     * Create a new instance
     */
    public VgaState(IndexColorModel cm) {
        this(DEF_SEQ_LENGTH, DEF_CRT_LENGTH, DEF_GRA_LENGTH, DEF_ATT_LENGTH, DEF_MISC, cm);
    }

    /**
     * Create a new instance
     */
    public VgaState(int numSeq, int numCrt, int numGra, int numAtt, int misc, IndexColorModel cm) {
        seq = new int[numSeq];
        crt = new int[numCrt];
        gra = new int[numGra];
        att = new int[numAtt];
        this.misc = misc;
        this.palette = cm;
    }

    /**
     * Create a new instance
     */
    public VgaState(int[] seq, int[] crt, int[] gra, int[] att, int misc, IndexColorModel cm) {
        this.seq = copy(seq);
        this.crt = copy(crt);
        this.gra = copy(gra);
        this.att = copy(att);
        this.misc = misc;
        this.palette = cm;
    }

    /**
     * Create a new instance
     */
    public VgaState(VgaState src) {
        seq = copy(src.seq);
        crt = copy(src.crt);
        gra = copy(src.gra);
        att = copy(src.att);
        this.misc = src.misc;
    }

    /** Get a seq register from this state */
    public int getSEQ(int index) {
        return seq[index];
    }

    /** Set a seq regsiter in this state */
    public void setSEQ(int index, int value) {
        seq[index] = value;
    }

    /** Get a crt register from this state */
    public int getCRT(int index) {
        return crt[index];
    }

    /** Set a crt regsiter in this state */
    public void setCRT(int index, int value) {
        crt[index] = value;
    }

    /** Get a gra register from this state */
    public int getGRA(int index) {
        return gra[index];
    }

    /** Set a gra regsiter in this state */
    public void setGRA(int index, int value) {
        gra[index] = value;
    }

    /** Get a att register from this state */
    public int getATT(int index) {
        return att[index];
    }

    /** Set a att regsiter in this state */
    public void setATT(int index, int value) {
        att[index] = value;
    }

    /** Get the misc register in this state */
    public int getMISC() {
        return misc;
    }

    /** Set the misc register in this state */
    public void setMISC(int value) {
        this.misc = value;
    }

    public boolean isColorMode() {
        return ((misc & 1) == 1);
    }

    /**
     * Save the state of the VGA-system to this object.
     * 
     * @param io
     */
    public void saveFromVGA(VgaIO io) {
        savePalette(io);
        saveFont(io);
        saveCRT(io);
        saveATT(io);
        saveGRA(io);
        saveSEQ(io);
        misc = io.getMISC();
    }

    /**
     * Restore this state to the VGA system
     * 
     * @param io
     */
    public void restoreToVGA(VgaIO io) {
        if (isColorMode()) {
            VgaUtils.setColorMode(io);
        } else {
            VgaUtils.setMonoMode(io);
        }
        restoreFont(io);
        io.setMISC(misc);
        restoreSEQ(io);
        restoreCRT(io);
        restoreGRA(io);
        restoreATT(io);
        io.getSTAT();
        restorePalette(io);
    }

    private final void saveATT(VgaIO io) {
        for (int i = 0; i < att.length; i++) {
            att[i] = io.getATT(i);
        }
    }

    protected void saveCRT(VgaIO io) {
        for (int i = 0; i < crt.length; i++) {
            crt[i] = io.getCRT(i);
        }
    }

    private final void saveGRA(VgaIO io) {
        for (int i = 0; i < gra.length; i++) {
            gra[i] = io.getGRAF(i);
        }
    }

    private final void saveSEQ(VgaIO io) {
        for (int i = 0; i < seq.length; i++) {
            seq[i] = io.getSEQ(i);
        }
    }

    private final void restoreSEQ(VgaIO io) {
        io.setSEQ(0, 1);
        for (int i = 1; i < seq.length; i++) {
            io.setSEQ(i, seq[i]);
        }
        io.setSEQ(0, 3);
    }

    protected void restoreCRT(VgaIO io) {
        io.setCRT(0x11, (io.getCRT(0x11) & 0x7F));
        for (int i = 0; i < crt.length; i++) {
            io.setCRT(i, crt[i]);
        }
    }

    private final void restoreGRA(VgaIO io) {
        for (int i = 0; i < gra.length; i++) {
            io.setGRAF(i, gra[i]);
        }
    }

    private final void restoreATT(VgaIO io) {
        for (int i = 0; i < att.length; i++) {
            io.setATT(i, att[i]);
        }
    }

    private static final int[] copy(int[] src) {
        final int dst[] = new int[src.length];
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    protected void savePalette(VgaIO io) {
        // log.debug("savePalette", new Exception("Trace"));
        final int size = getPaletteSize(io);
        final byte[] r = new byte[size];
        final byte[] g = new byte[size];
        final byte[] b = new byte[size];
        for (int i = size - 1; i >= 0; i--) {
            getPaletteEntry(io, i, r, g, b);
        }
        // log.debug("Save-Size:" + size + ", R=" + NumberUtils.hex(r));
        palette = new IndexColorModel(8/* 32 *//* 6 */, size, r, g, b);
    }

    /**
     * Gets the length of the palette.
     * 
     * @param io
     * @return
     */
    protected int getPaletteSize(VgaIO io) {
        return 256;
    }

    protected void restorePalette(VgaIO io) {
        if (palette != null) {
            // log.debug("restorePalette", new Exception("Trace"));
            final int size = Math.min(256, palette.getMapSize());
            // log.debug("size=" + size + ", pal.mapsize=" +
            // palette.getMapSize());
            final byte[] r = new byte[size];
            final byte[] g = new byte[size];
            final byte[] b = new byte[size];
            palette.getReds(r);
            palette.getGreens(g);
            palette.getBlues(b);
            // log.debug("Restore-Size:" + size + ", R=" + NumberUtils.hex(r));
            for (int i = size - 1; i >= 0; i--) {
                setPaletteEntry(io, i, r[i] & 0xff, g[i] & 0xff, b[i] & 0xff);
            }
        }
    }

    protected void setPaletteEntry(VgaIO io, int colorIndex, int r, int g, int b) {
        io.setDACWriteIndex(colorIndex);
        io.setDACData(r);
        io.setDACData(g);
        io.setDACData(b);
    }

    protected void getPaletteEntry(VgaIO io, int colorIndex, byte[] r, byte[] g, byte[] b) {
        io.setDACReadIndex(colorIndex);
        r[colorIndex] = (byte) io.getDACData();
        g[colorIndex] = (byte) io.getDACData();
        b[colorIndex] = (byte) io.getDACData();
    }

    /* save the fonts */
    protected void saveFont(VgaIO io) {
        if (fonts == null) {
            fonts = new byte[FONT_STATE_SIZE * 4];
        }
        final MemoryResource vgaMem = io.getVideoMem();
        int ofs = 0;
        final int oldSeq2 = io.getSEQ(2);
        final int oldSeq4 = io.getSEQ(4);
        final int oldGra4 = io.getGRAF(4);
        final int oldGra5 = io.getGRAF(5);
        final int oldGra6 = io.getGRAF(6);
        io.setSEQ(4, 0x06);
        io.setGRAF(5, 0);
        io.setGRAF(6, 0x05);

        io.setSEQ(2, 1);
        io.setGRAF(4, 0);
        vgaMem.getBytes(0, fonts, ofs, FONT_STATE_SIZE);
        ofs += FONT_STATE_SIZE;

        io.setSEQ(2, 2);
        io.setGRAF(4, 1);
        vgaMem.getBytes(0, fonts, ofs, FONT_STATE_SIZE);
        ofs += FONT_STATE_SIZE;

        io.setSEQ(2, 4);
        io.setGRAF(4, 2);
        vgaMem.getBytes(0, fonts, ofs, FONT_STATE_SIZE);
        ofs += FONT_STATE_SIZE;

        io.setSEQ(2, 8);
        io.setGRAF(4, 3);
        vgaMem.getBytes(0, fonts, ofs, FONT_STATE_SIZE);

        // restore
        io.setSEQ(2, oldSeq2);
        io.setSEQ(4, oldSeq4);
        io.setGRAF(4, oldGra4);
        io.setGRAF(5, oldGra5);
        io.setGRAF(6, oldGra6);
    }

    private void restoreFont(VgaIO io) {
        if (fonts != null) {
            final MemoryResource vgaMem = io.getVideoMem();
            int ofs = 0;

            final int oldSeq2 = io.getSEQ(0x02);
            final int oldSeq4 = io.getSEQ(0x04);
            final int oldGra1 = io.getGRAF(0x01);
            final int oldGra3 = io.getGRAF(0x03);
            final int oldGra5 = io.getGRAF(0x05);
            final int oldGra8 = io.getGRAF(0x08);

            io.setSEQ(4, 0x06);
            io.setGRAF(1, 0);
            io.setGRAF(3, 0);
            io.setGRAF(5, 0);
            io.setGRAF(8, 0xFF);

            io.setSEQ(2, 1);
            io.setGRAF(4, 0);
            vgaMem.setBytes(fonts, ofs, 0, FONT_STATE_SIZE);
            ofs += FONT_STATE_SIZE;

            io.setSEQ(2, 2);
            io.setGRAF(4, 1);
            vgaMem.setBytes(fonts, ofs, 0, FONT_STATE_SIZE);
            ofs += FONT_STATE_SIZE;

            io.setSEQ(2, 4);
            io.setGRAF(4, 2);
            vgaMem.setBytes(fonts, ofs, 0, FONT_STATE_SIZE);
            ofs += FONT_STATE_SIZE;

            io.setSEQ(2, 8);
            io.setGRAF(4, 3);
            vgaMem.setBytes(fonts, ofs, 0, FONT_STATE_SIZE);

            // restore
            io.setSEQ(2, oldSeq2);
            io.setSEQ(4, oldSeq4);
            io.setGRAF(1, oldGra1);
            io.setGRAF(3, oldGra3);
            io.setGRAF(5, oldGra5);
            io.setGRAF(8, oldGra8);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "VGA[SEQ:" + NumberUtils.hex(seq, 2) + ", CRT:" + NumberUtils.hex(crt, 2) +
                ", GRA:" + NumberUtils.hex(gra, 2) + ", ATT:" + NumberUtils.hex(att, 2) +
                ", MISC:" + NumberUtils.hex(misc, 2);
    }

}
