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

import gnu.classpath.SystemProperties;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.io.PrintWriter;
import java.security.PrivilegedExceptionAction;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCI_IDs;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.HardwareCursorImage;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.naming.InitialNaming;
import org.jnode.system.resource.IOResource;
import org.jnode.system.resource.MemoryResource;
import org.jnode.system.resource.ResourceManager;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.system.resource.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;

/**
 * @author epr
 * @author Levente S\u00e1ntha
 */
public class VMWareCore extends AbstractSurface implements VMWareConstants, PCI_IDs,
    HardwareCursorAPI {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(VMWareCore.class);
    private final VMWareDriver driver;
    private final int indexPort;
    private final int valuePort;
    private final IOResource ports;
    private final MemoryResource fifoMem;
    private final MemoryResource videoRam;
    private final int videoRamSize;
    private final int maxWidth;
    private final int maxHeight;
    private final int bitsPerPixel;
    private final int redMask;
    private final int greenMask;
    private final int blueMask;
    private final int alphaMask;
    private final int redMaskShift;
    private final int greenMaskShift;
    private final int blueMaskShift;
    private final int alphaMaskShift;
    private final int capabilities;
    private final int fifoCapabilities;
    private final int deviceId;
    private int bytesPerLine;
    private int offset;
    private int displayWidth;
    private boolean fifoDirty = false;
    private BitmapGraphics bitmapGraphics;
    private ColorModel model;
    private static final int MOUSE_ID = 0;
    private int curX;
    private int curY;
    private boolean curVisible;

    /**
     * Create a new instance
     *
     * @param driver
     * @param device
     */
    public VMWareCore(VMWareDriver driver, PCIDevice device) throws ResourceNotFreeException,
        DriverException {
        super(640, 480);
        this.driver = driver;
        final int basePort;
        if (device.getConfig().getDeviceID() == PCI_DEVICE_ID_VMWARE_SVGA) {
            basePort = SVGA_LEGACY_BASE_PORT;
            this.indexPort = SVGA_LEGACY_BASE_PORT + SVGA_INDEX_PORT * 4;
            this.valuePort = SVGA_LEGACY_BASE_PORT + SVGA_VALUE_PORT * 4;
        } else {
            basePort = device.getConfig().asHeaderType0().getBaseAddresses()[0].getIOBase();
            this.indexPort = basePort + SVGA_INDEX_PORT;
            this.valuePort = basePort + SVGA_VALUE_PORT;
        }

        log.debug("Found VMWare SVGA device using ports 0x" + NumberUtils.hex(indexPort) +
            " and 0x" + NumberUtils.hex(valuePort));

        try {
            // Allocate IO register space
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            ports = claimPorts(rm, device, basePort, SVGA_NUM_PORTS * 4);

            // Detect deviceID
            deviceId = getVMWareID();
            if (deviceId == SVGA_ID_0 || deviceId == SVGA_ID_INVALID) {
                dumpState();
                throw new DriverException("No supported VMWare SVGA found, found id 0x" +
                    NumberUtils.hex(deviceId));
            } else {
                log.debug("VMWare SVGA ID: 0x" + NumberUtils.hex(deviceId));
            }

            // Initialize and start FIFO
            fifoMem = initFifo(device, rm);

            // Read info
            this.capabilities = getReg32(SVGA_REG_CAPABILITIES);
            this.fifoCapabilities = hasCapability(SVGA_CAP_EXTENDED_FIFO) ? getFIFO(SVGA_FIFO_CAPABILITIES) : 0;
            this.videoRamSize = getReg32(SVGA_REG_FB_MAX_SIZE);
            this.maxWidth = getReg32(SVGA_REG_MAX_WIDTH);
            this.maxHeight = getReg32(SVGA_REG_MAX_HEIGHT);
            final int bitsPerPixel = getReg32(SVGA_REG_BITS_PER_PIXEL);
            this.bytesPerLine = getReg32(SVGA_REG_BYTES_PER_LINE);

            // Allocate framebuffer memory
            final Address videoRamAddr;
            if (device.getConfig().getDeviceID() == PCI_DEVICE_ID_VMWARE_SVGA2) {
                videoRamAddr =
                    Address.fromLong(device.getConfig().asHeaderType0().getBaseAddresses()[1].getMemoryBase());
            } else {
                videoRamAddr = Address.fromIntZeroExtend(SVGA_REG_FB_START);

            }
            this.videoRam = rm.claimMemoryResource(device, videoRamAddr, videoRamSize, ResourceManager.MEMMODE_NORMAL);
            this.bitsPerPixel = bitsPerPixel;
            switch (bitsPerPixel) {
                case 8:
                    bitmapGraphics =
                        BitmapGraphics.create8bppInstance(videoRam, width, height,
                            bytesPerLine, 0);
                    break;
                case 16:
                    bitmapGraphics =
                        BitmapGraphics.create16bppInstance(videoRam, width, height,
                            bytesPerLine, 0);
                    break;
                case 24:
                    bitmapGraphics =
                        BitmapGraphics.create24bppInstance(videoRam, width, height,
                            bytesPerLine, 0);
                    break;
                case 32:
                    bitmapGraphics =
                        BitmapGraphics.create32bppInstance(videoRam, width, height,
                            bytesPerLine, 0);
                    break;
                default:
                    throw new DriverException("Unknown bits/pixel value " + bitsPerPixel);
            }
            this.redMask = getReg32(SVGA_REG_RED_MASK);
            this.greenMask = getReg32(SVGA_REG_GREEN_MASK);
            this.blueMask = getReg32(SVGA_REG_BLUE_MASK);
            String transparency = SystemProperties.getProperty("org.jnode.awt.transparency");
            if (transparency != null && "true".equals(transparency)) {
                // todo get this in the safe way
                this.alphaMask = 0xff000000; // - transparency enabled
            } else {
                this.alphaMask = 0x00000000; // - transparency disabled
            }
            this.redMaskShift = getMaskShift(redMask);
            this.greenMaskShift = getMaskShift(greenMask);
            this.blueMaskShift = getMaskShift(blueMask);
            this.alphaMaskShift = getMaskShift(alphaMask);
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException(ex);
        }
    }

    /**
     * Release all resources
     */
    public final void release() {
        ports.release();
        fifoMem.release();
        videoRam.release();
    }

    /**
     * Open a given configuration
     *
     * @param config
     */
    public void open(FrameBufferConfiguration config) {
        final int w = config.getScreenWidth();
        final int h = config.getScreenHeight();
        setMode(w, h, config.getColorModel());
        fillRect(0, 0, w, h, 0, PAINT_MODE);
        dumpState(); // For debugging purposes
        defineCursor();
    }

    /**
     * Close the SVGA screen
     *
     * @see org.jnode.driver.video.Surface#close()
     */
    public synchronized void close() {
        disableSVGA();
        driver.close(this);
        super.close();
    }

    /**
     * Initialize the graphics mode
     *
     * @param width
     * @param height
     */
    public final void setMode(int width, int height, ColorModel model) {
        setReg32(SVGA_REG_WIDTH, width);
        setReg32(SVGA_REG_HEIGHT, height);
        setReg32(SVGA_REG_ENABLE, 1);
        this.model = model;
        this.offset = getReg32(SVGA_REG_FB_OFFSET);
        setReg32(SVGA_REG_GUEST_ID, GUEST_OS_OTHER);
        this.displayWidth = (getReg32(SVGA_REG_BYTES_PER_LINE) * 8) / ((bitsPerPixel + 7) & ~7);
        this.bytesPerLine = getReg32(SVGA_REG_BYTES_PER_LINE);
        setSize(width, height);
        this.width = width;
        this.height = height;
        switch (bitsPerPixel) {
            case 8:
                bitmapGraphics =
                    BitmapGraphics.create8bppInstance(videoRam, width, height, bytesPerLine,
                        offset);
                break;
            case 16:
                bitmapGraphics =
                    BitmapGraphics.create16bppInstance(videoRam, width, height, bytesPerLine,
                        offset);
                break;
            case 24:
                bitmapGraphics =
                    BitmapGraphics.create24bppInstance(videoRam, width, height, bytesPerLine,
                        offset);
                break;
            case 32:
                bitmapGraphics =
                    BitmapGraphics.create32bppInstance(videoRam, width, height, bytesPerLine,
                        offset, model.getTransparency());
                break;
        }
        dumpState();
    }

    public FrameBufferConfiguration[] getConfigs() {
        final ColorModel cm = new DirectColorModel(bitsPerPixel, redMask, greenMask, blueMask, alphaMask);
        return new FrameBufferConfiguration[]{
            new VMWareConfiguration(800, 600, cm),
            new VMWareConfiguration(1024, 768, cm),
            new VMWareConfiguration(1280, 1024, cm),
            new VMWareConfiguration(640, 480, cm),
            new VMWareConfiguration(1280, 800, cm),
        };
    }

    /**
     * Disable the SVGA mode.
     */
    public final void disableSVGA() {
        setReg32(SVGA_REG_ENABLE, 0);
    }

    /**
     * Update the entire screen
     */
    public final void updateScreen() {
        updateScreen(0, 0, width, height);
    }

    /**
     * Update the given region of the screen
     */
    public final void updateScreen(int x, int y, int width, int height) {
        writeWordToFIFO(SVGA_CMD_UPDATE);
        writeWordToFIFO(x);
        writeWordToFIFO(y);
        writeWordToFIFO(width);
        writeWordToFIFO(height);
    }

    /**
     * Draw the given shape
     *
     * @param shape
     * @param color
     * @param mode
     */
    public final synchronized void draw(Shape shape, Shape clip, AffineTransform tx, Color color,
                                        int mode) {
        syncFIFO();
        super.draw(shape, clip, tx, color, mode);
        final Rectangle r = getBounds(shape, tx);
        updateScreen(r.x - 1, r.y - 1, r.width + 2, r.height + 2);
    }

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        bitmapGraphics.copyArea(x, y, width, height, dx, dy);
        updateScreen(x + dx, y + dy, width, height);
    }

    /**
     * Draw an image to this surface
     *
     * @param src
     * @param srcX
     * @param srcY
     * @param x       The upper left x coordinate
     * @param y       The upper left y coordinate
     * @param w
     * @param h
     * @param bgColor The background color to use for transparent pixels. If
     *                null, no transparent pixels are unmodified on the destination
     */
    public void drawCompatibleRaster(Raster src, int srcX, int srcY, int x, int y, int w, int h,
                                     Color bgColor) {
        if (bgColor != null) {
            bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h, convertColor(bgColor));
        } else {
            bitmapGraphics.drawImage(src, srcX, srcY, x, y, w, h);
        }
        updateScreen(x, y, w, h);
    }

    /**
     * @see org.jnode.driver.video.Surface#fill(Shape, Shape, AffineTransform,
     *      Color, int)
     */
    public final synchronized void fill(Shape shape, Shape clip, AffineTransform tx, Color color,
                                        int mode) {
        syncFIFO();
        super.fill(shape, clip, tx, color, mode);
        final Rectangle b = getBounds(shape, tx);
        updateScreen(b.x, b.y, b.width, b.height);
    }

    /**
     * Fill a given rectangle with a given color
     */
    public final synchronized void fillRectangle(int x1, int y1, int x2, int y2, Color color,
                                                 int mode) {
        fillRect(x1, y1, x2 - x1, y2 - y1, convertColor(color), mode);
    }

    /**
     * Fill a given rectangle with a given color
     *
     * @param x
     * @param y
     * @param width
     * @param height
     * @param color
     * @param mode
     */
    public final void fillRect(int x, int y, int width, int height, int color, int mode) {
        if (x < 0) {
            width = Math.max(0, x + width);
            x = 0;
        }
        if (y < 0) {
            height = Math.max(0, y + height);
            y = 0;
        }
        if ((width <= 0) || (height <= 0))
            return;

        if (mode == Surface.XOR_MODE) {
            if (hasCapability(SVGA_CAP_RASTER_OP)) {
                writeWordToFIFO(SVGA_CMD_RECT_ROP_FILL);
                writeWordToFIFO(color);
                writeWordToFIFO(x);
                writeWordToFIFO(y);
                writeWordToFIFO(width);
                writeWordToFIFO(height);
                writeWordToFIFO(SVGA_ROP_XOR);
                return;
            }
        } else /* Paint mode */ {
            if (hasCapability(SVGA_CAP_RECT_FILL)) {
                writeWordToFIFO(SVGA_CMD_RECT_FILL);
                writeWordToFIFO(color);
                writeWordToFIFO(x);
                writeWordToFIFO(y);
                writeWordToFIFO(width);
                writeWordToFIFO(height);
                return;
            }
        }

        // bitmapGraphics.fillRect(x, y, width, height, color, mode);
        super.fillRect(x, y, width, height, color, mode);
    }

    /**
     * Dump the state of the SVGA to log.
     */
    public final void dumpState() {
        log.debug("Max. Resolution " + maxWidth + "*" + maxHeight);
        log.debug("Cur. Resolution " + width + "*" + height);
        log.debug("Bits/Pixel      " + bitsPerPixel);
        log.debug("Bytes/Line      " + bytesPerLine);
        log.debug("Offset          " + offset);
        log.debug("Display width   " + displayWidth);
        log.debug("Red mask        0x" + NumberUtils.hex(getReg32(SVGA_REG_RED_MASK)));
        log.debug("Green mask      0x" + NumberUtils.hex(getReg32(SVGA_REG_GREEN_MASK)));
        log.debug("Blue mask       0x" + NumberUtils.hex(getReg32(SVGA_REG_BLUE_MASK)));
        log.debug("Capabilities    0x" + NumberUtils.hex(capabilities));
        log.debug("FB.size         0x" + NumberUtils.hex(getReg32(SVGA_REG_FB_SIZE)));
        log.debug("FB.maxsize      0x" + NumberUtils.hex(getReg32(SVGA_REG_FB_MAX_SIZE)));
    }

    /**
     * Set the pixel at the given location to the given color.
     *
     * @param x
     * @param y
     * @param color
     */
    public final void drawPixel(int x, int y, int color, int mode) {
        bitmapGraphics.drawPixels(x, y, 1, color, mode);
        /*
         * if ((x >= 0) && (x < width) && (y > = 0) && (y < height)) { y; final
         * int ofs; if (mode == Surface.XOR_MODE) { switch (bitsPerPixel) { case
         * 8 : ofs = ofsY + x; videoRam.xorByte(ofs, (byte) color, 1); break;
         * case 16 : ofs = ofsY + (x << 1); videoRam.xorShort(ofs, (short)
         * color, 1); break; case 24 : 3); videoRam.xorShort(ofs, (short) (color &
         * 0xFFFF), 1); videoRam.xorByte(ofs + 2, (byte) ((color >> 16) & 0xFF),
         * 1); break; case 32 : ofs = ofsY + (x << 2); videoRam.xorInt(ofs,
         * color, 1); break; default : throw new RuntimeException("Unknown
         * bitsPerPixel"); } } else { switch (bitsPerPixel) { case 8 : ofs =
         * ofsY + x; videoRam.setByte(ofs, (byte) color); break; case 16 : ofs =
         * ofsY + (x << 1); videoRam.setShort(ofs, (short) color); break; case
         * 24 : 3); videoRam.setShort(ofs, (short) (color & 0xFFFF));
         * videoRam.setByte(ofs + 2, (byte) ((color >> 16) & 0xFF)); break; case
         * 32 : ofs = ofsY + (x << 2); videoRam.setInt(ofs, color); break;
         * default : throw new RuntimeException("Unknown bitsPerPixel"); } }
         */
    }

    /**
     * Low level draw line method. This method does not call updateScreen.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param c
     * @param mode
     */
    public final void drawLine(int x1, int y1, int x2, int y2, int c, int mode) {
        super.drawLine(x1, y1, x2, y2, c, mode);
        // todo optimize it
        /*
         * if (x1 == x2) { // Vertical line fillRect(x1, Math.min(y1, y2), 1,
         * Math.abs(y2 - y1), c, mode); } else if (y1 == y2) { // Horizontal
         * line //drawHorizontalLine(Math.min(x1, x2), y1, Math.abs(x2 - x1)+1,
         * c, mode); fillRect(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, 1, c,
         * mode); } else { super.drawLine(x1, y1, x2, y2, c, mode); }
         */
    }

    protected final void drawHorizontalLine(int x, int y, int w, int color, int mode) {
        if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
            w = Math.min(width - x, w);
            final int ofsY = bytesPerLine * y;
            int ofs;
            if (mode == Surface.XOR_MODE) {
                switch (bitsPerPixel) {
                    case 8:
                        ofs = ofsY + x;
                        videoRam.xorByte(ofs, (byte) color, w);
                        break;
                    case 16:
                        ofs = ofsY + (x << 1);
                        videoRam.xorShort(ofs, (short) color, w);
                        break;
                    case 24:
                        ofs = ofsY + (x * 3);
                        while (w > 0) {
                            videoRam.xorShort(ofs, (short) (color & 0xFFFF), 1);
                            videoRam.xorByte(ofs + 2, (byte) ((color >> 16) & 0xFF), 1);
                            w--;
                            ofs += 3;
                        }
                        break;
                    case 32:
                        ofs = ofsY + (x << 2);
                        videoRam.xorInt(ofs, color, w);
                        break;
                    default:
                        throw new RuntimeException("Unknown bitsPerPixel");
                }
            } else {
                switch (bitsPerPixel) {
                    case 8:
                        ofs = ofsY + x;
                        videoRam.setByte(ofs, (byte) color, w);
                        break;
                    case 16:
                        ofs = ofsY + (x << 1);
                        videoRam.setShort(ofs, (short) color, w);
                        break;
                    case 24:
                        ofs = ofsY + (x * 3);
                        while (w > 0) {
                            videoRam.setShort(ofs, (short) (color & 0xFFFF));
                            videoRam.setByte(ofs + 2, (byte) ((color >> 16) & 0xFF));
                            w--;
                            ofs += 3;
                        }
                        break;
                    case 32:
                        ofs = ofsY + (x << 2);
                        videoRam.setInt(ofs, color, w);
                        break;
                    default:
                        throw new RuntimeException("Unknown bitsPerPixel");
                }
            }
        }
    }

    /**
     * Gets the contents of a 32-bit SVGA register
     *
     * @param index
     * @return
     */
    private int getReg32(int index) {
        ports.outPortDword(indexPort, index);
        return ports.inPortDword(valuePort);
    }

    /**
     * Sets the contents of a 32-bit SVGA register
     *
     * @param index
     * @param value
     */
    private void setReg32(int index, int value) {
        ports.outPortDword(indexPort, index);
        ports.outPortDword(valuePort, value);
    }

    /**
     * Write the given word to the FIFO.
     *
     * @param value
     */
    private void writeWordToFIFO(int value) {
        fifoDirty = true;
        // Debug.out.println ("VMWare::WriteWordToFIFO(" +
        // Integer.toHexString(nValue) + ") pos: "
        // + ReadFIFO (SVGA_FIFO_NEXT_CMD));
        /* Need to sync? */

        final int fifoMin = getFIFO(SVGA_FIFO_MIN);
        final int fifoMax = getFIFO(SVGA_FIFO_MAX);
        final int fifoNextCmd = getFIFO(SVGA_FIFO_NEXT_CMD);

        if ((fifoNextCmd + 4 == getFIFO(SVGA_FIFO_STOP)) ||
            (fifoNextCmd == (fifoMax - 4) && getFIFO(SVGA_FIFO_STOP) == fifoMin)) {
            log.debug("VMWare::WriteWordToFIFO() syncing FIFO");
            setReg32(SVGA_REG_SYNC, 1);
            while (getReg32(SVGA_REG_BUSY) != 0) {
                Thread.yield();
            }
        }
        setFIFO(fifoNextCmd / 4, value);
        setFIFO(SVGA_FIFO_NEXT_CMD, fifoNextCmd + 4);
        if (getFIFO(SVGA_FIFO_NEXT_CMD) == fifoMax) {
            setFIFO(SVGA_FIFO_NEXT_CMD, fifoMin);
        }
    }

    /**
     * Wait until the SVGA is ready
     */
    private final void syncFIFO() {
        if (fifoDirty) {
            setReg32(SVGA_REG_SYNC, 1);
            while (getReg32(SVGA_REG_BUSY) != 0) {
                Thread.yield();
            }
            fifoDirty = false;
        }
    }

    /**
     * Gets a FIFO entry
     *
     * @param index
     * @return
     */
    private final int getFIFO(int index) {
        return fifoMem.getInt(index * 4);
    }

    /**
     * Sets a FIFO entry
     *
     * @param index
     * @param value
     */
    private final void setFIFO(int index, int value) {
        fifoMem.setInt(index * 4, value);
    }

    /**
     * Convert the given color to a value suitable for VMWare
     *
     * @param color
     */
    protected final int convertColor(Color color) {
        return convertColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    /**
     * Convert the given color to a value suitable for VMWare
     *
     * @param r
     * @param g
     * @param b
     */
    protected final int convertColor(int r, int g, int b) {
        return ((r << redMaskShift) & redMask) | ((g << greenMaskShift) & greenMask) |
            ((b << blueMaskShift) & blueMask);
    }

    protected final int convertColor(int r, int g, int b, int a) {
        return ((a << alphaMaskShift) & alphaMask) | ((r << redMaskShift) & redMask) |
            ((g << greenMaskShift) & greenMask) | ((b << blueMaskShift) & blueMask);
    }

    /**
     * Gets the SVGA_ID of the VMware SVGA adapter. This function should hide
     * any backward compatibility mess.
     */
    private int getVMWareID() {
        int vmware_svga_id;

        /***********************************************************************
         * Any version with any SVGA_ID_ to SVGA_ID_0 to support versions of
         * this driver with SVGA_ID_0.
         *
         * Versions of SVGA_ID_0 ignore writes to the SVGA_REG_ID register.
         *
         * Versions of SVGA_ID_1 will allow us to overwrite the content of the
         * SVGA_REG_ID register only with the values SVGA_ID_0 or SVGA_ID_1.
         *
         * Versions of SVGA_ID_2 will allow us to overwrite the content of the
         * SVGA_REG_ID register only with the values SVGA_ID_0 or SVGA_ID_1 or
         * SVGA_ID_2.
         */

        setReg32(SVGA_REG_ID, SVGA_ID_2);
        vmware_svga_id = getReg32(SVGA_REG_ID);

        if (vmware_svga_id == SVGA_ID_2) {
            return SVGA_ID_2;
        }

        setReg32(SVGA_REG_ID, SVGA_ID_1);
        vmware_svga_id = getReg32(SVGA_REG_ID);
        if (vmware_svga_id == SVGA_ID_1) {
            return SVGA_ID_1;
        }

        if (vmware_svga_id == SVGA_ID_0) {
            return SVGA_ID_0;
        }

        /* No supported VMware SVGA devices found */
        return SVGA_ID_INVALID;
    }

    /**
     * Claim and initialize the FIFO.
     *
     * @param device
     * @param rm
     * @return
     */
    private final MemoryResource initFifo(PCIDevice device, ResourceManager rm)
        throws ResourceNotFreeException {
        final int size = getReg32(SVGA_REG_MEM_SIZE);
        final Address address;

        if (device.getConfig().getDeviceID() == PCI_DEVICE_ID_VMWARE_SVGA2) {
            address = Address.fromLong(device.getConfig().asHeaderType0().getBaseAddresses()[2].getMemoryBase());
        } else {
            final int physBase = getReg32(SVGA_REG_MEM_START);
            address = Address.fromIntZeroExtend(physBase);
        }

        log.debug("Found FIFO at 0x" + NumberUtils.hex(address.toInt()) + ", size 0x" + NumberUtils.hex(size));

        final MemoryResource res = rm.claimMemoryResource(device, address, size, ResourceManager.MEMMODE_NORMAL);
        final int fifoMin = SVGA_FIFO_NUM_REGS * 4;
        res.setInt(SVGA_FIFO_MIN * 4, fifoMin);
        res.setInt(SVGA_FIFO_MAX * 4, size);
        res.setInt(SVGA_FIFO_NEXT_CMD * 4, fifoMin);
        res.setInt(SVGA_FIFO_STOP * 4, fifoMin);
        setReg32(SVGA_REG_CONFIG_DONE, 1);

        return res;
    }

    /**
     * Gets the size of the video ram in bytes.
     */
    final int getVideoRamSize() {
        return this.videoRamSize;
    }

    /**
     * Gets the maximum screen height in pixels
     */
    final int getMaxHeight() {
        return this.maxHeight;
    }

    /**
     * Gets the maximum screen width in pixels
     */
    final int getMaxWidth() {
        return this.maxWidth;
    }

    /**
     * Gets the number of bits per pixel
     */
    final int getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    /**
     * Gets the number of bytes per line
     */
    final int getBytesPerLine() {
        return this.bytesPerLine;
    }

    /**
     * Gets the number of shift needed for the given mask.
     * <p/>
     * E.g. getMaskShift(0xFF00) == 8
     *
     * @param mask
     * @return
     */
    private int getMaskShift(int mask) {
        if (mask == 0)
            return 0;
        int count = 0;
        while ((mask & 1) == 0) {
            count++;
            mask = mask >> 1;
        }
        return count;
    }

    /**
     * @see org.jnode.driver.video.Surface#getColorModel()
     */
    public ColorModel getColorModel() {
        return model;
    }

    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorPosition(int, int)
     */
    public synchronized void setCursorPosition(int x, int y) {
        this.curX = x;
        this.curY = y;
        setCursor(curVisible, curX, curY);
    }

    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorVisible(boolean)
     */
    public synchronized void setCursorVisible(boolean visible) {
        this.curVisible = visible;
        setCursor(curVisible, curX, curY);
    }

    /**
     * Sets the cursor image.
     *
     * @param cursor
     */
    public void setCursorImage(HardwareCursor cursor) {
        if (hasCapability(SVGA_CAP_ALPHA_CURSOR)) {
            defineARGBCursor(cursor.getImage(20, 20));
        } else {
            defineCursor(cursor.getImage(20, 20));
        }
    }

    private static int SVGA_BITMAP_SIZE(int w, int h) {
        return ((((w) + 31) >> 5) * (h));
    }

    private static int SVGA_PIXMAP_SIZE(int w, int h, int bpp) {
        return (((((w) * (bpp)) + 31) >> 5) * (h));
    }

    /**
     * Sets the cursor image.
     */
    private void defineCursor(HardwareCursorImage cursor) {

        final int[] argb = cursor.getImage();
        final int width = cursor.getWidth();
        final int height = cursor.getHeight();
        final int bpp = getBitsPerPixel();

        final int size = width * height;
        if (argb.length != size)
            throw new IllegalArgumentException("argb.length != width*height");

        final int bmSize = SVGA_BITMAP_SIZE(width, height);
        final int pmSize = SVGA_PIXMAP_SIZE(width, height, bpp);

        final int[] andMask = new int[size];
        final int[] xorMask = new int[size];

        for (int i = 0; i < size; i++) {
            final int v = argb[i];
            final int a = (v >>> 24) & 0xFF;
            final int r = (v >> 16) & 0xFF;
            final int g = (v >> 8) & 0xFF;
            final int b = v & 0xFF;
            if (a != 0) {
                // opaque
                andMask[i] = 0;
                xorMask[i] = v & convertColor(r, g, b, a);
            } else {
                // transparent
                andMask[i] = 0xFFFFFFFF;
                xorMask[i] = 0;
            }
        }

        // Wait for the FIFO
        syncFIFO();

        // Command
        writeWordToFIFO(SVGA_CMD_DEFINE_CURSOR);
        // Mouse id
        writeWordToFIFO(MOUSE_ID);
        // Hotspot X
        writeWordToFIFO(cursor.getHotSpotX());
        // Hotspot Y
        writeWordToFIFO(cursor.getHotSpotY());
        // Width
        writeWordToFIFO(width);
        // Height
        writeWordToFIFO(height);
        // Depth for AND mask
        writeWordToFIFO(1);
        // Depth for XOR mask
        writeWordToFIFO(bpp);
        // Scanlines for AND mask
        for (int i = 0; i < bmSize; i++) {
            writeWordToFIFO(andMask[i]);
        }
        // Scanlines for XOR mask
        for (int i = 0; i < pmSize; i++) {
            writeWordToFIFO(xorMask[i]);
        }
    }

    /**
     * Sets the cursor image.
     */
    private void defineARGBCursor(HardwareCursorImage cursor) {

        final int[] argb = cursor.getImage();
        final int size = cursor.getWidth() * cursor.getHeight();

        if (argb.length != size)
            throw new IllegalArgumentException("argb.length != width*height");

        // Wait for the FIFO
        syncFIFO();

        // Command
        writeWordToFIFO(SVGA_CMD_DEFINE_ALPHA_CURSOR);
        // Mouse id
        writeWordToFIFO(MOUSE_ID);
        // Hotspot X
        writeWordToFIFO(cursor.getHotSpotX());
        // Hotspot Y
        writeWordToFIFO(cursor.getHotSpotY());
        // Width
        writeWordToFIFO(cursor.getWidth());
        // Height
        writeWordToFIFO(cursor.getHeight());
        // Scanlines
        for (int i = 0; i < size; i++) {
            writeWordToFIFO(argb[i]);
        }
    }

    private void defineCursor() {
        // Wait for the FIFO
        syncFIFO();

        final int width = 4;
        final int height = 4;
        final int bpp = getBitsPerPixel();

        // Command
        writeWordToFIFO(SVGA_CMD_DEFINE_CURSOR);
        // Mouse id
        writeWordToFIFO(MOUSE_ID);
        // Hotspot X
        writeWordToFIFO(0);
        // Hotspot Y
        writeWordToFIFO(0);
        // Width
        writeWordToFIFO(width);
        // Height
        writeWordToFIFO(height);
        // Depth for AND mask
        writeWordToFIFO(1);
        // Depth for XOR mask
        writeWordToFIFO(bpp);
        // Scanlines for AND mask
        final int bmSize = SVGA_BITMAP_SIZE(width, height);
        for (int i = 0; i < bmSize; i++) {
            writeWordToFIFO(0);
        }
        // Scanlines for XOR mask
        final int pmSize = SVGA_PIXMAP_SIZE(width, height, bpp);
        for (int i = 0; i < pmSize; i++) {
            writeWordToFIFO(0xFFFFFF);
        }
        syncFIFO();
        setCursor(false, 0, 0);
    }

    private void setCursor(boolean visible, int x, int y) {
        if (hasFIFOCapability(SVGA_FIFO_CAP_CURSOR_BYPASS_3)) {
            setFIFO(SVGA_FIFO_CURSOR_ON, visible ? 1 : 0);
            setFIFO(SVGA_FIFO_CURSOR_X, x);
            setFIFO(SVGA_FIFO_CURSOR_Y, y);
            setFIFO(SVGA_FIFO_CURSOR_COUNT, getFIFO(SVGA_FIFO_CURSOR_COUNT) + 1);
        } else {
            setReg32(SVGA_REG_CURSOR_ID, MOUSE_ID);
            if (visible) {
                if (hasCapability(SVGA_CAP_CURSOR)) {
                    if (hasCapability(SVGA_CAP_CURSOR_BYPASS)) { //
                        //System.out.println("bypass " + x + ", " + y);
                        setReg32(SVGA_REG_CURSOR_X, x);
                        setReg32(SVGA_REG_CURSOR_Y, y);
                    } else { // System.out.println("move " + x + ", " + y);
                        syncFIFO();
                        writeWordToFIFO(SVGA_CMD_MOVE_CURSOR);
                        writeWordToFIFO(x);
                        writeWordToFIFO(y);
                    }
                }

            }
            setReg32(SVGA_REG_CURSOR_ON, visible ? 1 : 0);
        }
    }

    /**
     * Check whether the SVGA device has a particular capability bits.
     */
    private boolean hasCapability(int cap) {
        return ((this.capabilities & cap) == cap);
    }

    /**
     * Check whether the SVGA device has a particular FIFO capability bits.
     */
    private boolean hasFIFOCapability(int fifoCap) {
        return (fifoCapabilities & fifoCap) == fifoCap;
    }

    /**
     * Claim an IO port range.
     */
    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner,
                                  final int low, final int length) throws ResourceNotFreeException, DriverException {
        try {
            return AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction<IOResource>() {
                public IOResource run() throws ResourceNotFreeException {
                    return rm.claimIOResource(owner, low, length);
                }
            });
        } catch (ResourceNotFreeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DriverException("Unknown exception", ex);
        }
    }

    /**
     * @see org.jnode.driver.video.Surface#drawAlphaRaster(java.awt.image.Raster,
     *      java.awt.geom.AffineTransform, int, int, int, int, int, int,
     *      java.awt.Color)
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY, int dstX,
                                int dstY, int width, int height, Color color) {
        bitmapGraphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY, width, height,
            convertColor(color));
    }

    @Override
    public int getRGBPixel(int x, int y) {
        return bitmapGraphics.doGetPixel(x, y);
    }

    @Override
    public int[] getRGBPixels(Rectangle region) {
        return bitmapGraphics.doGetPixels(region);
    }

    @Override
    public void update(int x, int y, int width, int height) {
        syncFIFO();
        updateScreen(x, y, width, height);
    }

    public void showInfo(PrintWriter out) {
        out.println("Capabilities : " + NumberUtils.hex(capabilities));
        out.println("FIFO Capabil.: " + NumberUtils.hex(fifoCapabilities));
        out.println("Bit/pixel    : " + bitsPerPixel);

        out.println("FIFO_MIN     : " + NumberUtils.hex(getFIFO(SVGA_FIFO_MIN)));
        out.println("FIFO_MAX     : " + NumberUtils.hex(getFIFO(SVGA_FIFO_MAX)));
        out.println("FIFO_NEXT_CMD: " + NumberUtils.hex(getFIFO(SVGA_FIFO_NEXT_CMD)));
        out.println("FIFO_STOP    : " + NumberUtils.hex(getFIFO(SVGA_FIFO_STOP)));
    }
}
