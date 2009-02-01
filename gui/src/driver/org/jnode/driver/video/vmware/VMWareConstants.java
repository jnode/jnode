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
 
package org.jnode.driver.video.vmware;

/**
 * @author epr
 */
public interface VMWareConstants {

    public static final int SVGA_LEGACY_BASE_PORT = 0x4560;
    public static final int SVGA_INDEX_PORT = 0x0;
    public static final int SVGA_VALUE_PORT = 0x1;
    public static final int SVGA_BIOS_PORT = 0x2;
    public static final int SVGA_NUM_PORTS = 0x3;

    public static final int SVGA_REG_ID = 0;
    public static final int SVGA_REG_ENABLE = 1;
    public static final int SVGA_REG_WIDTH = 2;
    public static final int SVGA_REG_HEIGHT = 3;
    public static final int SVGA_REG_MAX_WIDTH = 4;
    public static final int SVGA_REG_MAX_HEIGHT = 5;
    public static final int SVGA_REG_DEPTH = 6;
    public static final int SVGA_REG_BITS_PER_PIXEL = 7;
    public static final int SVGA_REG_PSEUDOCOLOR = 8;
    public static final int SVGA_REG_RED_MASK = 9;
    public static final int SVGA_REG_GREEN_MASK = 10;
    public static final int SVGA_REG_BLUE_MASK = 11;
    public static final int SVGA_REG_BYTES_PER_LINE = 12;
    public static final int SVGA_REG_FB_START = 13;
    public static final int SVGA_REG_FB_OFFSET = 14;
    public static final int SVGA_REG_FB_MAX_SIZE = 15;
    public static final int SVGA_REG_FB_SIZE = 16;

    public static final int SVGA_REG_CAPABILITIES = 17;
    public static final int SVGA_REG_MEM_START = 18; /*
                                                         * Memory for command
                                                         * FIFO and bitmaps
                                                         */
    public static final int SVGA_REG_MEM_SIZE = 19;
    public static final int SVGA_REG_CONFIG_DONE = 20; /*
                                                         * Set when memory area
                                                         * configured
                                                         */
    public static final int SVGA_REG_SYNC = 21; /*
                                                 * Write to force
                                                 * synchronization
                                                 */
    public static final int SVGA_REG_BUSY = 22; /* Read to check if sync is done */
    public static final int SVGA_REG_GUEST_ID = 23; /* Set guest OS identifier */
    public static final int SVGA_REG_CURSOR_ID = 24; /* ID of cursor */
    public static final int SVGA_REG_CURSOR_X = 25; /* Set cursor X position */
    public static final int SVGA_REG_CURSOR_Y = 26; /* Set cursor Y position */
    public static final int SVGA_REG_CURSOR_ON = 27; /* Turn cursor on/off */

    public static final int SVGA_REG_TOP = 28; /*
                                                 * Must be 1 greater than the
                                                 * last register
                                                 */

    public static final int SVGA_PALETTE_BASE = 1024; /*
                                                         * Base of SVGA color
                                                         * map
                                                         */

    public static final int SVGA_MAGIC = 0x900000;
    public static final int SVGA_ID_INVALID = 0xffffffff;
    public static final int SVGA_ID_0 = SVGA_MAGIC << 8;
    public static final int SVGA_ID_1 = (SVGA_MAGIC << 8) | 1;
    public static final int SVGA_ID_2 = (SVGA_MAGIC << 8) | 2;

    public static final int SVGA_FIFO_MIN = 0;
    public static final int SVGA_FIFO_MAX = 1;
    public static final int SVGA_FIFO_NEXT_CMD = 2;
    public static final int SVGA_FIFO_STOP = 3;

    public static final int SVGA_CMD_UPDATE = 1;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_FILL = 2;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_COPY = 3;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_DEFINE_BITMAP = 4;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_DEFINE_BITMAP_SCANLINE = 5;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_DEFINE_PIXMAP = 6;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_DEFINE_PIXMAP_SCANLINE = 7;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_BITMAP_FILL = 8;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_PIXMAP_FILL = 9;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_BITMAP_COPY = 10;
    /*
     * FIFO layout: Bitmap ID, Source X, Source Y, Dest X, Dest Y,
     */

    public static final int SVGA_CMD_RECT_PIXMAP_COPY = 11;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_FREE_OBJECT = 12;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_ROP_FILL = 13;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_ROP_COPY = 14;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_ROP_BITMAP_FILL = 15;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_ROP_PIXMAP_FILL = 16;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_RECT_ROP_BITMAP_COPY = 17;
    /*
     * FIFO layout: ID, Source X, Source Y,
     */

    public static final int SVGA_CMD_RECT_ROP_PIXMAP_COPY = 18;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_DEFINE_CURSOR = 19;
    /*
     * FIFO layout: ID, Hotspot X, Hotspot Y, Width, Height, Depth for AND mask,
     * Depth for XOR mask,
     */

    public static final int SVGA_CMD_DISPLAY_CURSOR = 20;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_MOVE_CURSOR = 21;
    /*
     * FIFO layout:
     */

    public static final int SVGA_CMD_DEFINE_ALPHA_CURSOR = 22;
    /*
     * FIFO layout: ID, Hotspot X, Hotspot Y, Width, Height,
     */

    public static final int SVGA_CMD_MAX = 22;

    public static final int GUEST_OS_OTHER = 0x5000 + 10;

    /*
     * Raster codes
     */
    public static final int SVGA_ROP_CLEAR = 0x00;
    public static final int SVGA_ROP_AND = 0x01;
    public static final int SVGA_ROP_AND_REVERSE = 0x02;
    public static final int SVGA_ROP_COPY = 0x03;
    public static final int SVGA_ROP_AND_INVERTED = 0x04;
    public static final int SVGA_ROP_NOOP = 0x05;
    public static final int SVGA_ROP_XOR = 0x06;
    public static final int SVGA_ROP_OR = 0x07;
    public static final int SVGA_ROP_NOR = 0x08;
    public static final int SVGA_ROP_EQUIV = 0x09;
    public static final int SVGA_ROP_INVERT = 0x0a;
    public static final int SVGA_ROP_OR_REVERSE = 0x0b;
    public static final int SVGA_ROP_COPY_INVERTED = 0x0c;
    public static final int SVGA_ROP_OR_INVERTED = 0x0d;
    public static final int SVGA_ROP_NAND = 0x0e;
    public static final int SVGA_ROP_SET = 0x0f;

    /*
     * Capabiities
     */

    public static final int SVGA_CAP_RECT_FILL = 0x0001;
    public static final int SVGA_CAP_RECT_COPY = 0x0002;
    public static final int SVGA_CAP_RECT_PAT_FILL = 0x0004;
    public static final int SVGA_CAP_OFFSCREEN = 0x0008;
    public static final int SVGA_CAP_RASTER_OP = 0x0010;
    public static final int SVGA_CAP_CURSOR = 0x0020;
    public static final int SVGA_CAP_CURSOR_BYPASS = 0x0040;
    public static final int SVGA_CAP_CURSOR_BYPASS_2 = 0x0080;
    public static final int SVGA_CAP_8BIT_EMULATION = 0x0100;
    public static final int SVGA_CAP_ALPHA_CURSOR = 0x0200;
    public static final int SVGA_CAP_GLYPH = 0x0400;
    public static final int SVGA_CAP_GLYPH_CLIPPING = 0x0800;
    public static final int SVGA_CAP_OFFSCREEN_1 = 0x1000;
    public static final int SVGA_CAP_ALPHA_BLEND = 0x2000;
}
