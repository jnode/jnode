/*
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

package org.jnode.driver.video.cirrus;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.image.JNodeBufferedImage;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.ddc.DDC1NoSignalException;
import org.jnode.driver.video.ddc.DDC1ParseException;
import org.jnode.driver.video.ddc.DDC1Reader;
import org.jnode.driver.video.ddc.EDID;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.driver.video.util.VesaGTF;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;
import org.vmmagic.unboxed.Address;

/**
 * @author peda
 */
public class CirrusCore extends AbstractSurface implements CirrusConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(CirrusCore.class);

    private static final boolean DEBUG = true;

    private final CirrusDriver driver;

    private FrameBufferConfiguration config;

    /** Hardware cursor implementation */
    // private final NVidiaHardwareCursor hwCursor;
    /** the EDID data for the connected monitor */
    private EDID edidData;

    private final MemoryResource mmio;

    private final MemoryResource videoRam;

    private final CirrusMMIO vgaIO;

    private BitmapGraphics bitmapGraphics;

    /**
     * @param driver
     * @param architecture
     * @param model
     * @param device
     */
    public CirrusCore(CirrusDriver driver, String architecture, String model,
            PCIDevice device) throws ResourceNotFreeException, DriverException {

        super(640, 480);
        this.driver = driver;

        final PCIHeaderType0 pciCfg = device.getConfig().asHeaderType0();

        final PCIBaseAddress fbAddr = pciCfg.getBaseAddresses()[0];
        final PCIBaseAddress mmioAddr = pciCfg.getBaseAddresses()[1];

        log.info("Found "
                + model
                + ", chipset 0x"
                + NumberUtils.hex(pciCfg.getRevision())
                + ", device-vendor ID 0x"
                + NumberUtils.hex(pciCfg.getDeviceID() << 16 + pciCfg
                        .getVendorID()));

        try {
            final ResourceManager rm = (ResourceManager) InitialNaming
                    .lookup(ResourceManager.NAME);
            final IOResource ports = claimPorts(rm, device, CIRRUS_FIRST_PORT,
                    CIRRUS_LAST_PORT - CIRRUS_FIRST_PORT);
            final int mmioBase = (int) mmioAddr.getMemoryBase() & 0xFF000000;
            final int mmioSize = mmioAddr.getSize();
            final int fbBase = (int) fbAddr.getMemoryBase() & 0xFF000000;
            final int fbSize = fbAddr.getSize();

            log.info("Found Cirrus, FB at 0x" + NumberUtils.hex(fbBase)
                    + " s0x" + NumberUtils.hex(fbSize) + ", MMIO at 0x"
                    + NumberUtils.hex(mmioBase) + " s0x"
                    + NumberUtils.hex(mmioSize));

            // TODO: support move of videoRam to an upper bank for byte swapping
            // (i.e. non LE)

            this.mmio = rm.claimMemoryResource(device, Address
                    .fromIntZeroExtend(mmioBase), mmioSize,
                    ResourceManager.MEMMODE_NORMAL);
            this.videoRam = rm.claimMemoryResource(device, Address
                    .fromIntZeroExtend(fbBase), fbSize,
                    ResourceManager.MEMMODE_NORMAL);

            this.vgaIO = new CirrusMMIO(mmio, ports);

        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException(ex);
        }
    }

    private IOResource claimPorts(final ResourceManager rm,
            final ResourceOwner owner, final int low, final int length)
        throws ResourceNotFreeException, DriverException {
        try {
            return AccessControllerUtils
                    .doPrivileged(new PrivilegedExceptionAction<IOResource>() {
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
     * Read the EDID data for the connected monitor via DDC
     */
    private final void readEDID() {

        DDC1Reader reader = new DDC1Reader(vgaIO);

        try {

            edidData = reader.read();
            log.debug(edidData.toString());

        } catch (DDC1NoSignalException e) {
            log.debug("No Monitor signal detected", e);
        } catch (DDC1ParseException e) {
            log.debug("Couldn't parse Monitor data", e);
        }
    }

    /**
     * returns all possible FrameBuffer Configurations
     * 
     * @return all possible FrameBuffer Configurations
     */
    public final FrameBufferConfiguration[] getConfigurations() {

        if (DEBUG)
            return new FrameBufferConfiguration[] {
                new CirrusConfiguration(640, 400, 32) };
        // return new FrameBufferConfiguration[] {new CirrusConfiguration( 1024,
        // 768, 32)};

        // read the edid data
        readEDID();

        if (edidData == null) {
            log.debug("Using a static set of FB configurations");
            // return static configurations if we couldn't detect the monitor
            // TODO: parse the vga bios for valid configurations
            return new FrameBufferConfiguration[] {
                new CirrusConfiguration(640, 480, 24),
                new CirrusConfiguration(640, 480, 32),
                new CirrusConfiguration(800, 600, 24),
                new CirrusConfiguration(800, 600, 32),
                new CirrusConfiguration(1024, 768, 24),
                new CirrusConfiguration(1020, 768, 32),
                new CirrusConfiguration(1280, 1024, 24),
                new CirrusConfiguration(1280, 1024, 32)};
        } else {
            log
                    .debug("Using FB configurations based on Monitor's capabilities");
            // return values matching the monitors configuration
            float aspect = (float) edidData.getHSize()
                    / (float) edidData.getVSize();
            // TODO: implement me, prefer native resolution
            return null;

        }
    }

    /**
     * Close the Cirrus FB
     * 
     * @see org.jnode.driver.video.Surface#close()
     */
    public synchronized void close() {
        log.debug("close");

        // TODO restore original VGA state...

        // TODO implement me

        driver.close();
        super.close();

        log.debug("End of close");
    }

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#convertColor(java.awt.Color)
     */
    protected int convertColor(Color color) {
        return color.getRGB();
    }

    /**
     * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int,
     *      int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        bitmapGraphics.copyArea(x, y, width, height, dx, dy);
    }

    /**
     * @see org.jnode.driver.video.Surface#drawCompatibleRaster(java.awt.image.Raster,
     *      int, int, int, int, int, int, java.awt.Color)
     */
    public final void drawCompatibleRaster(Raster raster, int srcX, int srcY,
            int dstX, int dstY, int width, int height, Color bgColor) {
        if (bgColor == null) {
            bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width,
                    height);
        } else {
            bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width,
                    height, convertColor(bgColor));
        }
    }

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#drawPixel(int, int, int,
     *      int)
     */
    public final void drawPixel(int x, int y, int color, int mode) {
        bitmapGraphics.drawPixels(x, y, 1, color, mode);
    }

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#fillRect(int, int, int,
     *      int, int, int)
     */
    public void fillRect(int x, int y, int w, int h, int color, int mode) {

        final int screenWidth = config.getScreenWidth();
        if (x + w > screenWidth) {
            w = screenWidth - x;
        }

        if ((x == 0) && (w == screenWidth)) {
            bitmapGraphics.drawPixels(0, y, screenWidth * height, color, mode);
        } else {
            super.fillRect(x, y, w, h, color, mode);
        }
    }

    /**
     * @see org.jnode.driver.video.Surface#getColorModel()
     */
    public ColorModel getColorModel() {
        return config.getColorModel();
    }

    /**
     * Open the given configuration
     * 
     * @param config
     */
    final void open(FrameBufferConfiguration config) {

        this.config = config;

        int width = config.getScreenWidth();
        int height = config.getScreenHeight();
        int bpp = config.getColorModel().getPixelSize();
        int bytesPerLine = 0;
        final int pixels = width * height;
        int startAddress = 0; // can be configured to 2048, 0-2048 io

        if (DEBUG) {
            int value = vgaIO.getSEQ(SR7_EXTENDED_SEQUENCER_MODE);
            Unsafe.debug("SEQ = " + Integer.toBinaryString(value) + "\n");
            int stat = vgaIO.getSTAT();
            Unsafe.debug("STAT = " + Integer.toBinaryString(stat) + "\n");
            int misc = vgaIO.getMISC();
            Unsafe.debug("MISC = " + Integer.toBinaryString(misc) + "\n");
            int hdr = vgaIO.getDACData();
            Unsafe.debug("DAC: " + Integer.toBinaryString(hdr) + "\n");
        }

        // change the size of this surface..
        setSize(width, height);

        // set hidden DAC to extended Color modes.. (we support 24bit and 32bit)
        vgaIO.setDACData(0xC5);

        // Set linear FB mode + extended color mode as requested. We support
        // 24bit and 32bit
        if (bpp == 24)
            vgaIO.setSEQ(SR7_EXTENDED_SEQUENCER_MODE, 0x15);
        else
            vgaIO.setSEQ(SR7_EXTENDED_SEQUENCER_MODE, 0x19);

        // programm the CRTC..
        setMode(bpp, 70);

        if (DEBUG) {
            // Check again after we set it...
            int value = vgaIO.getSEQ(SR7_EXTENDED_SEQUENCER_MODE);
            Unsafe.debug("SEQ = " + Integer.toBinaryString(value) + "\n");
            int stat = vgaIO.getSTAT();
            Unsafe.debug("STAT = " + Integer.toBinaryString(stat) + "\n");
            int misc = vgaIO.getMISC();
            Unsafe.debug("MISC = " + Integer.toBinaryString(misc) + "\n");
            int hdr = vgaIO.getDACData();
            Unsafe.debug("DAC: " + Integer.toBinaryString(hdr) + "\n");
        }

        // create the BitmapGraphics object and fill the screen black
        if (bpp == 24) {
            bitmapGraphics = BitmapGraphics.create24bppInstance(videoRam,
                    width, height, bytesPerLine, startAddress);
            videoRam.setInt24(startAddress, 0, pixels);
        } else {
            bitmapGraphics = BitmapGraphics.create32bppInstance(videoRam,
                    width, height, bytesPerLine, startAddress);
            videoRam.setInt(startAddress, 0, pixels);
        }

        Unsafe.debug("\nPixels = " + pixels + "\n");

        for (int i = 0; i < pixels; i++) {
            if (bpp == 24)
                videoRam.setInt(i * 3, i);
            if (bpp == 32)
                videoRam.setInt(i * 4, ~i);
        }

        for (int i = 0; i < 80; i++)
            videoRam.setInt(i * 4, 0xffcb8743);
        for (int i = 320; i < 380; i++)
            videoRam.setInt(i * 4, 0xff12569a);

        for (int u = 10; u < 150; u++)
            for (int v = 0; v < 50; v++)
                videoRam.setByte(u + v * 640 * 4, (byte) 0xff);
        for (int u = 10; u < 150; u++)
            for (int v = 50; v < 100; v++)
                videoRam.setByte(u + v * 640 * 4, (byte) 0);
        for (int u = 10; u < 150; u++)
            for (int v = 100; v < 150; v++)
                videoRam.setByte(u + v * 640 * 4, (byte) 0x7f);

        Unsafe.debug("End of open()\n");

        throw new Error("Make FB not-usable for testing");
    }

    private void setMode(int bpp, int refreshRate) {

        CirrusVGAState vgaState = new CirrusVGAState(vgaIO);
        vgaState.dump();

        VesaGTF gtf = VesaGTF.calculate(width, height, refreshRate);

        Unsafe.debug("GTF:\n" + gtf.toString() + "\n");
        Unsafe.debug("Try to set mode " + width + "x" + height + "@"
                + refreshRate + "Hz\n");

        // TODO: disable writeprotect in CR11[7]

        // Programm Synthesizer (we use VCLK0)
        double value = gtf.getHFrequency() / 14.31818; // Reference clock freq
                                                        // in MHz
        int denom = 1;
        while (value < 128) {
            if (value * 2.0 >= 128)
                break;
            value = value * 2.0;
            denom = denom << 1;
        }
        vgaIO.setSEQ(SRB_VCLK0_NUMERATOR, (int) value);
        vgaIO.setSEQ(SR1B_VCLK0_DENOMINATOR_AND_POST_SCALAR, denom << 1);
        // finally select VCLK0
        vgaIO.setMISC((vgaIO.getMISC() & ~12));

        // Programm CRTC
        vgaIO.setCRT(CR6_CRTC_VERTICAL_TOTAL, gtf.getVSyncEnd() + 2); // XXX
                                                                        // not
                                                                        // sure,
                                                                        // use
                                                                        // framelength?!
        vgaIO.setCRT(CR10_CRTC_VERTICAL_SYNC_START, gtf.getVSyncStart());
        vgaIO.setCRT(CR11_CRTC_VERTICAL_SYNC_END, gtf.getVSyncEnd()
                - gtf.getVSyncStart()); // XXX check
        vgaIO.setCRT(CR12_CRTC_VERTICAL_DISPLAY_END, height - 1); // only
                                                                    // least-significant
                                                                    // eight
                                                                    // bits are
                                                                    // used! ->
                                                                    // CR7 for
                                                                    // bit 8 and
                                                                    // 9
        vgaIO.setCRT(CR15_CRTC_VERTICAL_BLANKING_START, gtf.getVSyncStart());
        // TODO: handle overflow and handle shifts over than 1
        if (bpp == 24)
            vgaIO.setCRT(CR13_CRTC_OFFSET, 3 * width / 2 / 8);
        else
            vgaIO.setCRT(CR13_CRTC_OFFSET, 0x50); // TEST

        vgaIO.setCRT(CR7_CRTC_OVERFLOW, (gtf.getVSyncStart() >> 2) & 0x80 | // Bit7:
                                                                            // VerticalRetraceStart[9],
                                                                            // extends
                                                                            // CR10
                ((height - 1) >> 3) & 0x40 | // Bit6: VerticalDisplayEnd[9],
                                                // extends CR12
                ((gtf.getVSyncEnd() + 2) >> 4) & 0x20 | // Bit5:
                                                        // VerticalTotal[9],
                                                        // extends CR6
                (1 << 4) | // Bit4: LineCompare[8], XXX no idea, allways 1 for
                            // the moment
                (gtf.getVSyncStart() >> 5) & 0x8 | // Bit3:
                                                    // VerticalBlankingStart[8],
                                                    // extends CR15
                (gtf.getVSyncStart() >> 6) & 0x4 | // Bit2:
                                                    // VerticalRetraceStart[8],
                                                    // extends CR10
                ((height - 1) >> 7) & 0x2 | // Bit1: VerticalDisplayEnd[8],
                                            // extends CR12
                ((gtf.getVSyncEnd() + 2) >> 8) & 0x1 // Bit0:
                                                        // VerticalTotal[8],
                                                        // extends CR6
        );
        // We use no offset for the moment. This might be used for hardware
        // double buffering...
        vgaIO.setCRT(CRC_START_ADDRESS_HIGH, 0);
        vgaIO.setCRT(CRD_START_ADDRESS_LOW, 0);

        vgaIO.setCRT(CR0_CRTC_HORIZONTAL_TOTAL, gtf.getHSyncEnd() / 8 + 5); // XXX
                                                                            // not
                                                                            // sure,
                                                                            // use
                                                                            // framelength?!
        vgaIO.setCRT(CR1_CRTC_HORIZONTAL_DISPLAY_END, width / 8 - 1);
        vgaIO.setCRT(CR2_CRTC_HORIZONTAL_BLANKING_START,
                gtf.getHSyncStart() / 8 - 1); // XXX not sure
        vgaIO.setCRT(CR3_CRTC_HORIZONTAL_BLANKING_END,
                gtf.getHSyncEnd() / 8 + 1); // XXX not sure
        vgaIO.setCRT(CR4_CRTC_HORIZONTAL_SYNC_START, gtf.getHSyncStart() / 8);
        vgaIO.setCRT(CR5_CRTC_HORIZONTAL_SYNC_END, gtf.getHSyncEnd() / 8);

        // enable writeprotection of CRT registers
        vgaIO.setCRT(CR13_CRTC_OFFSET, 0x80 | vgaIO.getCRT(CR13_CRTC_OFFSET));

        vgaState.dump();
    }

    /*
     * private final void dumpVGA() { // TODO: Also dump Sequencer registers //
     * Register dump... Unsafe.debug("\nCR0: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR0_CRTC_HORIZONTAL_TOTAL)));
     * Unsafe.debug("\nCR1: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR1_CRTC_HORIZONTAL_DISPLAY_END)));
     * Unsafe.debug("\nCR2: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR2_CRTC_HORIZONTAL_BLANKING_START)));
     * Unsafe.debug("\nCR3: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR3_CRTC_HORIZONTAL_BLANKING_END)));
     * Unsafe.debug("\nCR4: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR4_CRTC_HORIZONTAL_SYNC_START)));
     * Unsafe.debug("\nCR5: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR5_CRTC_HORIZONTAL_SYNC_END)));
     * Unsafe.debug("\nCR6: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR6_CRTC_VERTICAL_TOTAL)));
     * Unsafe.debug("\nCR7: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR7_CRTC_OVERFLOW)));
     * Unsafe.debug("\nCRC: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CRC_START_ADDRESS_HIGH)));
     * Unsafe.debug("\nCRD: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CRD_START_ADDRESS_LOW)));
     * Unsafe.debug("\nCR10: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR10_CRTC_VERTICAL_SYNC_START)));
     * Unsafe.debug("\nCR11: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR11_CRTC_VERTICAL_SYNC_END)));
     * Unsafe.debug("\nCR12: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR12_CRTC_VERTICAL_DISPLAY_END)));
     * Unsafe.debug("\nCR13: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR13_CRTC_OFFSET)));
     * Unsafe.debug("\nCR14: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR14_CRTC_UNDERLINE_ROW_SCANLINE)));
     * Unsafe.debug("\nCR15: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR15_CRTC_VERTICAL_BLANKING_START)));
     * Unsafe.debug("\nCR17: ");
     * Unsafe.debug(Integer.toBinaryString(vgaIO.getCRT(CR17_CRTC_MODE_CONTROL)));
     * Unsafe.debug("\nCR18: ");
     * Unsafe.debug(Integer.toHexString(vgaIO.getCRT(CR18_CRTC_LINE_COMPARE)));
     * Unsafe.debug("\nCR1B: ");
     * Unsafe.debug(Integer.toBinaryString(vgaIO.getCRT(CR1B_EXTENDED_DISPLAY_CONTROLS)));
     * Unsafe.debug("\n"); }
     */

    /**
     * Release all resources
     */
    final void release() {
        mmio.release();
        videoRam.release();
    }

    /**
     * @see org.jnode.driver.video.Surface#drawAlphaRaster(java.awt.image.Raster,
     *      java.awt.geom.AffineTransform, int, int, int, int, int, int,
     *      java.awt.Color)
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX,
            int srcY, int dstX, int dstY, int width, int height, Color color) {
        bitmapGraphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY,
                width, height, convertColor(color));
    }

    @Override
    public int getRGBPixel(int x, int y) {
        return bitmapGraphics.doGetPixel(x, y);
    }

    @Override
    public int[] getRGBPixels(Rectangle region) {
        return bitmapGraphics.doGetPixels(region);
    }

    /**
     * Inner class implementing {@link FrameBufferConfiguration} for the Cirrus
     * cards. Only 24bpp and 32bpp modes are supported
     * 
     * @author peda
     */
    public class CirrusConfiguration extends FrameBufferConfiguration {

        public CirrusConfiguration(int width, int height, int bpp) {
            super(width, height, new DirectColorModel(bpp, 0xff0000, 0x00ff00,
                    0x0000ff));
        }

        @Override
        public BufferedImage createCompatibleImage(int w, int h,
                int transparency) {
            return new JNodeBufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

    }
}
