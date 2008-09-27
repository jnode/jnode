/*
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

package org.jnode.driver.video.vesa;

import gnu.classpath.SystemProperties;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.HardwareCursor;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.Surface;
import org.jnode.driver.video.cursor.SoftwareCursor;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;

/**
 * 
 * @author Fabien DUMINY (fduminy at jnode.org)
 * 
 */
public class VESACore extends AbstractSurface implements HardwareCursorAPI {

    /** My logger */
    private static final Logger log = Logger.getLogger(VESACore.class);
    private final VESADriver driver;
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
    private int bytesPerLine;
    private int offset;
    private int displayWidth;
    private SoftwareCursor bitmapGraphics;
    private ColorModel model;

    /**
     * Create a new instance
     * 
     * @param driver
     * @param device
     */
    public VESACore(VESADriver driver, VbeInfoBlock vbeInfoBlock, ModeInfoBlock modeInfoBlock,
            PCIDevice device) throws ResourceNotFreeException, DriverException {
        super(modeInfoBlock.getXResolution(), modeInfoBlock.getYResolution());

        this.driver = driver;
        Address address = Address.fromIntZeroExtend(modeInfoBlock.getRamBase());

        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            this.capabilities = vbeInfoBlock.getCapabilities();
            this.maxWidth = modeInfoBlock.getXResolution();
            this.maxHeight = modeInfoBlock.getYResolution();
            this.bitsPerPixel = modeInfoBlock.getBitsPerPixel();

            int multiplier = 1;
            if (bitsPerPixel == 16)
                multiplier = 2;
            else if (bitsPerPixel == 24)
                multiplier = 3;
            else if (bitsPerPixel == 32)
                multiplier = 4;

            this.bytesPerLine = modeInfoBlock.getXResolution() * multiplier;
            this.videoRamSize = bytesPerLine * modeInfoBlock.getYResolution();

            this.videoRam =
                    rm.claimMemoryResource(device, address, videoRamSize,
                            ResourceManager.MEMMODE_NORMAL);

            BitmapGraphics graphics;
            switch (bitsPerPixel) {
                case 8:
                    graphics =
                            BitmapGraphics.create8bppInstance(videoRam, width, height,
                                    bytesPerLine, 0);

                    // 8 bits color depth
                    this.redMask = 0x00000003; // TODO get from modeInfoBlock
                    this.greenMask = 0x0000000C; // TODO get from modeInfoBlock
                    this.blueMask = 0x00000030; // TODO get from modeInfoBlock
                    break;
                case 16:
                    graphics =
                            BitmapGraphics.create16bppInstance(videoRam, width, height,
                                    bytesPerLine, 0);

                    // 16 bits color depth
                    this.redMask = 0x00007C00; // TODO get from modeInfoBlock
                    this.greenMask = 0x000003E0; // TODO get from modeInfoBlock
                    this.blueMask = 0x0000001F; // TODO get from modeInfoBlock
                    break;
                case 24:
                    graphics =
                            BitmapGraphics.create24bppInstance(videoRam, width, height,
                                    bytesPerLine, 0);

                    // 24 bits color depth
                    this.redMask = 0x00FF0000; // TODO get from modeInfoBlock
                    this.greenMask = 0x0000FF00; // TODO get from modeInfoBlock
                    this.blueMask = 0x000000FF; // TODO get from modeInfoBlock
                    break;
                case 32:
                    graphics =
                            BitmapGraphics.create32bppInstance(videoRam, width, height,
                                    bytesPerLine, 0);

                    // 32 bits color depth
                    this.redMask = 0x00FF0000; // TODO get from modeInfoBlock
                    this.greenMask = 0x0000FF00; // TODO get from modeInfoBlock
                    this.blueMask = 0x000000FF; // TODO get from modeInfoBlock
                    break;
                default:
                    throw new DriverException("Unknown bits/pixel value " + bitsPerPixel);
            }
            bitmapGraphics = new SoftwareCursor(graphics);

            String transparency = SystemProperties.getProperty("org.jnode.awt.transparency");
            if ((bitsPerPixel == 32) && (transparency != null) && "true".equals(transparency)) {
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
    }

    /**
     * Close the SVGA screen
     * 
     * @see org.jnode.driver.video.Surface#close()
     */
    public synchronized void close() {
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
        this.model = model;
        setSize(width, height);
        this.width = width;
        this.height = height;
        BitmapGraphics graphics;
        switch (bitsPerPixel) {
            case 8:
                graphics =
                        BitmapGraphics.create8bppInstance(videoRam, width, height, bytesPerLine,
                                offset);
                bitmapGraphics.setBitmapGraphics(graphics);
                break;
            case 16:
                graphics =
                        BitmapGraphics.create16bppInstance(videoRam, width, height, bytesPerLine,
                                offset);
                bitmapGraphics.setBitmapGraphics(graphics);
                break;
            case 24:
                graphics =
                        BitmapGraphics.create24bppInstance(videoRam, width, height, bytesPerLine,
                                offset);
                bitmapGraphics.setBitmapGraphics(graphics);
                break;
            case 32:
                graphics =
                        BitmapGraphics.create32bppInstance(videoRam, width, height, bytesPerLine,
                                offset, model.getTransparency());
                bitmapGraphics.setBitmapGraphics(graphics);
                break;
        }
        dumpState();
    }

    public FrameBufferConfiguration[] getConfigs() {
        try {
            final ColorModel cm =
                    new DirectColorModel(bitsPerPixel, redMask, greenMask, blueMask, alphaMask);
            return new FrameBufferConfiguration[] {new VESAConfiguration(maxWidth, maxHeight, cm), };
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
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
        // TODO
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
        updateScreen(dx, dy, width, height);
    }

    /**
     * Draw an image to this surface
     * 
     * @param src
     * @param srcX
     * @param srcY
     * @param x The upper left x coordinate
     * @param y The upper left y coordinate
     * @param w
     * @param h
     * @param bgColor The background color to use for transparent pixels. If
     *            null, no transparent pixels are unmodified on the destination
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
        if ((width > 0) && (height > 0)) {
            // TODO optimize it like in VMWareCore ?
            for (int line = y + height - 1; line >= y; line--) {
                bitmapGraphics.drawPixels(x, line, width, color, mode);
            }
        }
    }

    /**
     * Dump the state to log.
     */
    public final void dumpState() {
        log.debug("Max. Resolution " + maxWidth + "*" + maxHeight);
        log.debug("Cur. Resolution " + width + "*" + height);
        log.debug("Bits/Pixel      " + bitsPerPixel);
        log.debug("Bytes/Line      " + bytesPerLine);
        log.debug("Offset          " + offset);
        log.debug("Display width   " + displayWidth);
        log.debug("Red mask        0x" + NumberUtils.hex(redMask));
        log.debug("Green mask      0x" + NumberUtils.hex(greenMask));
        log.debug("Blue mask       0x" + NumberUtils.hex(blueMask));
        log.debug("Capabilities    0x" + NumberUtils.hex(capabilities));
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
        if (x1 == x2) {
            // Vertical line
            fillRect(x1, Math.min(y1, y2), 1, Math.abs(y2 - y1), c, mode);
        } else if (y1 == y2) {
            // Horizontal line
            // drawHorizontalLine(Math.min(x1, x2), y1, Math.abs(x2 - x1)+1, c,
            // mode);
            fillRect(Math.min(x1, x2), y1, Math.abs(x2 - x1) + 1, 1, c, mode);
        } else {
            super.drawLine(x1, y1, x2, y2, c, mode);
        }
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
     * 
     * E.g. getMaskShift(0xFF00) == 8
     * 
     * @param mask
     * @return
     */
    private final int getMaskShift(int mask) {
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
        bitmapGraphics.setCursorPosition(x, y);
    }

    /**
     * @see org.jnode.driver.video.HardwareCursorAPI#setCursorVisible(boolean)
     */
    public synchronized void setCursorVisible(boolean visible) {
        bitmapGraphics.setCursorVisible(visible);
    }

    /**
     * Sets the cursor image.
     * 
     * @param cursor
     */
    public void setCursorImage(HardwareCursor cursor) {
        bitmapGraphics.setCursorImage(cursor);
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
}
