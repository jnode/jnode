/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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

    /*
     * Block 1 (basic registers): The originally defined FIFO registers.
     * These exist and are valid for all versions of the FIFO protocol.
     */
    public static final int SVGA_FIFO_MIN = 0;
    public static final int SVGA_FIFO_MAX = 1;
    public static final int SVGA_FIFO_NEXT_CMD = 2;
    public static final int SVGA_FIFO_STOP = 3;

    /*
     * Block 2 (extended registers): Mandatory registers for the extended
     * FIFO.  These exist if the SVGA caps register includes
     * SVGA_CAP_EXTENDED_FIFO; some of them are valid only if their
     * associated capability bit is enabled.
     *
     * Note that when originally defined, SVGA_CAP_EXTENDED_FIFO implied
     * support only for (FIFO registers) CAPABILITIES, FLAGS, and FENCE.
     * This means that the guest has to test individually (in most cases
     * using FIFO caps) for the presence of registers after this; the VMX
     * can define "extended FIFO" to mean whatever it wants, and currently
     * won't enable it unless there's room for that set and much more.
     */

    public static final int SVGA_FIFO_CAPABILITIES = 4;
    public static final int SVGA_FIFO_FLAGS = 5;
    // Valid with SVGA_FIFO_CAP_FENCE:
    public static final int SVGA_FIFO_FENCE = 6;

    /*
     * Block 3a (optional extended registers): Additional registers for the
     * extended FIFO, whose presence isn't actually implied by
     * SVGA_CAP_EXTENDED_FIFO; these exist if SVGA_FIFO_MIN is high enough to
     * leave room for them.
     *
     * These in block 3a, the VMX currently considers mandatory for the extended
     * FIFO.
     */

    // Valid if exists (i.e. if extended FIFO enabled):
    public static final int SVGA_FIFO_3D_HWVERSION = 7;    /*
                                                            * See
                                                            * SVGA3dHardwareVersion
                                                            * in svga3d_reg.h
                                                            */
    // Valid with SVGA_FIFO_CAP_PITCHLOCK:
    public static final int SVGA_FIFO_PITCHLOCK = 8;

    // Valid with SVGA_FIFO_CAP_CURSOR_BYPASS_3:
    public static final int SVGA_FIFO_CURSOR_ON = 9;   /*
                                                        * Cursor bypass 3 show/hide
                                                        * register
                                                        */
    public static final int SVGA_FIFO_CURSOR_X = 10;   /*
                                                        * Cursor bypass 3 x
                                                        * register
                                                        */
    public static final int SVGA_FIFO_CURSOR_Y = 11;   /*
                                                        * Cursor bypass 3 y
                                                        * register
                                                        */
    public static final int SVGA_FIFO_CURSOR_COUNT = 12;   /*
                                                            * Incremented when any
                                                            * of the other 3 change
                                                            */
    public static final int SVGA_FIFO_CURSOR_LAST_UPDATED = 13;    /*
                                                                    * Last time the
                                                                    * host updated
                                                                    * the cursor
                                                                    */

    // Valid with SVGA_FIFO_CAP_RESERVE:
    public static final int SVGA_FIFO_RESERVED = 14;   /*
                                                        * Bytes past NEXT_CMD with
                                                        * real contents
                                                        */

    /*
     * Valid with SVGA_FIFO_CAP_SCREEN_OBJECT or SVGA_FIFO_CAP_SCREEN_OBJECT_2:
     *
     * By default this is SVGA_ID_INVALID, to indicate that the cursor
     * coordinates are specified relative to the virtual root. If this is set to
     * a specific screen ID, cursor position is reinterpreted as a signed offset
     * relative to that screen's origin.
     */
    public static final int SVGA_FIFO_CURSOR_SCREEN_ID = 15;

    /*
     * Valid with SVGA_FIFO_CAP_DEAD
     *
     * An arbitrary value written by the host, drivers should not use it.
     */
    public static final int SVGA_FIFO_DEAD = 16;

    /*
     * Valid with SVGA_FIFO_CAP_3D_HWVERSION_REVISED:
     *
     * Contains 3D HWVERSION (see SVGA3dHardwareVersion in svga3d_reg.h) on
     * platforms that can enforce graphics resource limits.
     */
    public static final int SVGA_FIFO_3D_HWVERSION_REVISED = 17;

    /*
     * XXX: The gap here, up until SVGA_FIFO_3D_CAPS, can be used for new
     * registers, but this must be done carefully and with judicious use of
     * capability bits, since comparisons based on SVGA_FIFO_MIN aren't enough
     * to tell you whether the register exists: we've shipped drivers and
     * products that used SVGA_FIFO_3D_CAPS but didn't know about some of the
     * earlier ones. The actual order of introduction was: - PITCHLOCK - 3D_CAPS
     * - CURSOR_* (cursor bypass 3) - RESERVED So, code that wants to know
     * whether it can use any of the aforementioned registers, or anything else
     * added after PITCHLOCK and before 3D_CAPS, needs to reason about something
     * other than SVGA_FIFO_MIN.
     */

    /*
     * 3D caps block space; valid with 3D hardware version >=
     * SVGA3D_HWVERSION_WS6_B1.
     */
    public static final int SVGA_FIFO_3D_CAPS = 32;
    public static final int SVGA_FIFO_3D_CAPS_LAST = 32 + 255;

    /*
     * End of VMX's current definition of "extended-FIFO registers". Registers
     * before here are always enabled/disabled as a block; either the extended
     * FIFO is enabled and includes all preceding registers, or it's disabled
     * entirely.
     *
     * Block 3b (truly optional extended registers): Additional registers for
     * the extended FIFO, which the VMX already knows how to enable and disable
     * with correct granularity.
     *
     * Registers after here exist if and only if the guest SVGA driver sets
     * SVGA_FIFO_MIN high enough to leave room for them.
     */

    // Valid if register exists:
    public static final int SVGA_FIFO_GUEST_3D_HWVERSION = SVGA_FIFO_3D_CAPS_LAST + 1; /*
                                                                                        * Guest
                                                                                        * driver
                                                                                        * 's
                                                                                        * 3D
                                                                                        * version
                                                                                        */
    public static final int SVGA_FIFO_FENCE_GOAL = SVGA_FIFO_GUEST_3D_HWVERSION + 1;   /*
                                                                                        * Matching
                                                                                        * target
                                                                                        * for
                                                                                        * SVGA_IRQFLAG_FENCE_GOAL
                                                                                        */
    public static final int SVGA_FIFO_BUSY = SVGA_FIFO_FENCE_GOAL + 1; /*
                                                                        * See
                                                                        * "FIFO Synchronization Registers"
                                                                        */

    /*
     * Always keep this last. This defines the maximum number of registers we
     * know about. At power-on, this value is placed in the SVGA_REG_MEM_REGS
     * register, and we expect the guest driver to allocate this much space in
     * FIFO memory for registers.
     */
    public static final int SVGA_FIFO_NUM_REGS = SVGA_FIFO_BUSY;

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
     * Capabilities
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
    public static final int SVGA_CAP_3D = 0x00004000;
    public static final int SVGA_CAP_EXTENDED_FIFO = 0x00008000;
    public static final int SVGA_CAP_MULTIMON = 0x00010000; // Legacy multi-monitor support
    public static final int SVGA_CAP_PITCHLOCK = 0x00020000;
    public static final int SVGA_CAP_IRQMASK = 0x00040000;
    public static final int SVGA_CAP_DISPLAY_TOPOLOGY = 0x00080000; // Legacy multi-monitor support
    public static final int SVGA_CAP_GMR = 0x00100000;
    public static final int SVGA_CAP_TRACES = 0x00200000;
    public static final int SVGA_CAP_GMR2 = 0x00400000;
    public static final int SVGA_CAP_SCREEN_OBJECT_2 = 0x00800000;

    /*
     * FIFO Capabilities
     *
     *      Fence -- Fence register and command are supported
     *      Accel Front -- Front buffer only commands are supported
     *      Pitch Lock -- Pitch lock register is supported
     *      Video -- SVGA Video overlay units are supported
     *      Escape -- Escape command is supported
     *
     * XXX: Add longer descriptions for each capability, including a list
     *      of the new features that each capability provides.
     *
     * SVGA_FIFO_CAP_SCREEN_OBJECT --
     *
     *    Provides dynamic multi-screen rendering, for improved Unity and
     *    multi-monitor modes. With Screen Object, the guest can
     *    dynamically create and destroy 'screens', which can represent
     *    Unity windows or virtual monitors. Screen Object also provides
     *    strong guarantees that DMA operations happen only when
     *    guest-initiated. Screen Object deprecates the BAR1 guest
     *    framebuffer (GFB) and all commands that work only with the GFB.
     *
     *    New registers:
     *       FIFO_CURSOR_SCREEN_ID, VIDEO_DATA_GMRID, VIDEO_DST_SCREEN_ID
     *
     *    New 2D commands:
     *       DEFINE_SCREEN, DESTROY_SCREEN, DEFINE_GMRFB, BLIT_GMRFB_TO_SCREEN,
     *       BLIT_SCREEN_TO_GMRFB, ANNOTATION_FILL, ANNOTATION_COPY
     *
     *    New 3D commands:
     *       BLIT_SURFACE_TO_SCREEN
     *
     *    New guarantees:
     *
     *       - The host will not read or write guest memory, including the GFB,
     *         except when explicitly initiated by a DMA command.
     *
     *       - All DMA, including legacy DMA like UPDATE and PRESENT_READBACK,
     *         is guaranteed to complete before any subsequent FENCEs.
     *
     *       - All legacy commands which affect a Screen (UPDATE, PRESENT,
     *         PRESENT_READBACK) as well as new Screen blit commands will
     *         all behave consistently as blits, and memory will be read
     *         or written in FIFO order.
     *
     *         For example, if you PRESENT from one SVGA3D surface to multiple
     *         places on the screen, the data copied will always be from the
     *         SVGA3D surface at the time the PRESENT was issued in the FIFO.
     *         This was not necessarily true on devices without Screen Object.
     *
     *         This means that on devices that support Screen Object, the
     *         PRESENT_READBACK command should not be necessary unless you
     *         actually want to read back the results of 3D rendering into
     *         system memory. (And for that, the BLIT_SCREEN_TO_GMRFB
     *         command provides a strict superset of functionality.)
     *
     *       - When a screen is resized, either using Screen Object commands or
     *         legacy multimon registers, its contents are preserved.
     *
     * SVGA_FIFO_CAP_GMR2 --
     *
     *    Provides new commands to define and remap guest memory regions (GMR).
     *
     *    New 2D commands:
     *       DEFINE_GMR2, REMAP_GMR2.
     *
     * SVGA_FIFO_CAP_3D_HWVERSION_REVISED --
     *
     *    Indicates new register SVGA_FIFO_3D_HWVERSION_REVISED exists.
     *    This register may replace SVGA_FIFO_3D_HWVERSION on platforms
     *    that enforce graphics resource limits.  This allows the platform
     *    to clear SVGA_FIFO_3D_HWVERSION and disable 3D in legacy guest
     *    drivers that do not limit their resources.
     *
     *    Note this is an alias to SVGA_FIFO_CAP_GMR2 because these indicators
     *    are codependent (and thus we use a single capability bit).
     *
     * SVGA_FIFO_CAP_SCREEN_OBJECT_2 --
     *
     *    Modifies the DEFINE_SCREEN command to include a guest provided
     *    backing store in GMR memory and the bytesPerLine for the backing
     *    store.  This capability requires the use of a backing store when
     *    creating screen objects.  However if SVGA_FIFO_CAP_SCREEN_OBJECT
     *    is present then backing stores are optional.
     *
     * SVGA_FIFO_CAP_DEAD --
     *
     *    Drivers should not use this cap bit.  This cap bit can not be
     *    reused since some hosts already expose it.
     */

    public static final int SVGA_FIFO_CAP_NONE = 0;
    public static final int SVGA_FIFO_CAP_FENCE = (1 << 0);
    public static final int SVGA_FIFO_CAP_ACCELFRONT = (1 << 1);
    public static final int SVGA_FIFO_CAP_PITCHLOCK = (1 << 2);
    public static final int SVGA_FIFO_CAP_VIDEO = (1 << 3);
    public static final int SVGA_FIFO_CAP_CURSOR_BYPASS_3 = (1 << 4);
    public static final int SVGA_FIFO_CAP_ESCAPE = (1 << 5);
    public static final int SVGA_FIFO_CAP_RESERVE = (1 << 6);
    public static final int SVGA_FIFO_CAP_SCREEN_OBJECT = (1 << 7);
    public static final int SVGA_FIFO_CAP_GMR2 = (1 << 8);
    public static final int SVGA_FIFO_CAP_3D_HWVERSION_REVISED = SVGA_FIFO_CAP_GMR2;
    public static final int SVGA_FIFO_CAP_SCREEN_OBJECT_2 = (1 << 9);
    public static final int SVGA_FIFO_CAP_DEAD = (1 << 10);

    /*
     * FIFO Flags
     *
     *      Accel Front -- Driver should use front buffer only commands
     */

    public static final int SVGA_FIFO_FLAG_NONE = 0;
    public static final int SVGA_FIFO_FLAG_ACCELFRONT = (1 << 0);
    public static final int SVGA_FIFO_FLAG_RESERVED = (1 << 31); // Internal use only

}
