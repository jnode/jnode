/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.spi.DpmsState;
import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonCore implements RadeonConstants {

    /** My logger */
    final Logger log = Logger.getLogger(getClass());

    private final RadeonDriver driver;

    /** Memory mapped IO space */
    private final MemoryResource mmio;

    /** All video RAM of the device */
    private final MemoryResource deviceRam;

    private final RadeonVgaIO vgaIO;

    private final int memSize;

    private final boolean hasCRTC2;

    private final RadeonVgaState oldVgaState;

    private final RadeonVgaState currentState;

    private final RadeonPLLInfo pllInfo;

    private final RadeonHardwareCursor hwCursor;

    /**
     * @param driver
     * @param architecture
     * @param model
     * @param device
     */
    public RadeonCore(RadeonDriver driver, int architecture, String model,
            PCIDevice device) throws ResourceNotFreeException, DriverException {
        this.driver = driver;
        this.hasCRTC2 = (architecture != Architecture.R100);
        this.pllInfo = new RadeonPLLInfo();

        final PCIDeviceConfig pciCfg = device.getConfig();
        final PCIBaseAddress ioAddr = pciCfg.getBaseAddresses()[ 2];
        final PCIBaseAddress fbAddr = pciCfg.getBaseAddresses()[ 0];
        log.info("Found ATI " + model + ", chipset 0x"
                + NumberUtils.hex(pciCfg.getRevision()));
        try {
            final ResourceManager rm = (ResourceManager) InitialNaming
                    .lookup(ResourceManager.NAME);
            final int ioBase = (int) ioAddr.getMemoryBase();
            final int ioSize = ioAddr.getSize();
            final int fbBase = (int) fbAddr.getMemoryBase() /* & 0xFF800000 */;

            this.mmio = rm.claimMemoryResource(device, Address.valueOf(ioBase),
                    ioSize, ResourceManager.MEMMODE_NORMAL);
            this.vgaIO = new RadeonVgaIO(mmio);
            this.memSize = readMemorySize();
            log.info("Memory size " + (memSize / (1024 * 1024)) + "MB");

            this.deviceRam = rm.claimMemoryResource(device, Address
                    .valueOf(fbBase), memSize, ResourceManager.MEMMODE_NORMAL);
            vgaIO.setVideoRam(deviceRam);
            log.debug("Found ATI " + model + ", FB at 0x"
                    + NumberUtils.hex(fbBase) + "s0x"
                    + NumberUtils.hex(memSize) + ", MMIO at 0x"
                    + NumberUtils.hex(ioBase));

        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException(ex);
        }

        // Read the current state of the device
        this.oldVgaState = new RadeonVgaState(hasCRTC2, vgaIO);
        this.currentState = new RadeonVgaState(hasCRTC2, vgaIO);

        // Claim the first 128K for VGA only
        deviceRam.claimChildResource(0, 128 * 1024, false);

        // Allocate the hardware cursor
        this.hwCursor = new RadeonHardwareCursor(this, vgaIO);
    }

    /**
     * Open the given configuration
     * 
     * @param config
     */
    final RadeonSurface open(RadeonConfiguration config)
            throws ResourceNotFreeException {
        // Calculate new configuration
        final DisplayMode mode = config.getDisplayMode();
        final int width = mode.getWidth();
        final int height = mode.getHeight();
        final int pixels = width * height;
        final int bitsPerPixel = config.getBitsPerPixel();
        final int bytesPerLine = config.getBytesPerLine();
        final int bytesPerScreen = bytesPerLine * height;
        currentState.calcForConfiguration(config, pllInfo, vgaIO);

        // Allocate the screen memory
        final MemoryResource screen = claimDeviceMemory(bytesPerScreen,
                4 * 1024);
        log.debug("Screen at 0x" + NumberUtils.hex(screen.getOffset()) + ", size 0x" + NumberUtils.hex(screen.getSize()));

        //if (true) { throw new ResourceNotFreeException("TEST"); }

        // Save the current state
        oldVgaState.saveFromVGA(vgaIO);

        // Turn off the screen
        final DpmsState dpmsState = getDpms();
        setDpms(DpmsState.OFF);
        
        try {            
            // Set the new configuration
            currentState.restoreToVGA(vgaIO);
            vgaIO.setReg32(CRTC_OFFSET, (int) screen.getOffset());
            
            // Set the 8-bit palette
            setPalette(1.0f);
            
            // Create the graphics helper & clear the screen
            final BitmapGraphics bitmapGraphics;
            switch (bitsPerPixel) {
            case 8:
                bitmapGraphics = BitmapGraphics.create8bppInstance(screen, width,
                        height, bytesPerLine, 0);
                screen.setByte(0, (byte) 0, pixels);
                break;
            case 16:
                bitmapGraphics = BitmapGraphics.create16bppInstance(screen, width,
                        height, bytesPerLine, 0);
                screen.setShort(0, (byte) 0, pixels);
                break;
            case 24:
                bitmapGraphics = BitmapGraphics.create24bppInstance(screen, width,
                        height, bytesPerLine, 0);
                screen.setInt24(0, 0, pixels);
                break;
            case 32:
                bitmapGraphics = BitmapGraphics.create32bppInstance(screen, width,
                        height, bytesPerLine, 0);
                screen.setInt(0, 0, pixels);
                break;
            default:
                throw new IllegalArgumentException("Invalid bits per pixel "
                        + bitsPerPixel);
            }
            return new RadeonSurface(this, config, bitmapGraphics, screen);            
        } finally {
            // Turn the screen back on
            setDpms(dpmsState);
        }
    }

    /**
     * Close the SVGA screen
     * 
     * @see org.jnode.driver.video.Surface#close()
     */
    final synchronized void close() {
        hwCursor.close();
        
        // Save the screen state and turn of the screen
        final DpmsState dpmsState = getDpms();
        setDpms(DpmsState.OFF);
        try {
            oldVgaState.restoreToVGA(vgaIO);
        } finally {
            // Restore the screen
            setDpms(dpmsState);
        }

        driver.close(this);
    }

    /**
     * Release all resources
     *  
     */
    final void release() {
        mmio.release();
        deviceRam.release();
    }

    /**
     * Gets the hardware cursor API implementation.
     */
    final HardwareCursorAPI getHardwareCursor() {
        return hwCursor;
    }

    /**
     * Read the framebuffer memory size.
     */
    private final int readMemorySize() {
        return vgaIO.getReg32(CONFIG_MEMSIZE) & CONFIG_MEMSIZE_MASK;
    }

    /**
     * Gets the monitor power state.
     */
    final DpmsState getDpms() {
        final int val = vgaIO.getReg32(CRTC_EXT_CNTL);
        final boolean display = ((val & CRTC_DISPLAY_DIS) == 0);
        final boolean hsync = ((val & CRTC_HSYNC_DIS) == 0);
        final boolean vsync = ((val & CRTC_VSYNC_DIS) == 0);
        return new DpmsState(display, hsync, vsync);
    }

    /**
     * Sets the monitor power state.
     */
    final void setDpms(DpmsState state) {
        int val = vgaIO.getReg32(CRTC_EXT_CNTL);
        val &= ~(CRTC_DISPLAY_DIS | CRTC_HSYNC_DIS | CRTC_VSYNC_DIS);
        val |= state.isDisplay() ? 0 : CRTC_DISPLAY_DIS;
        val |= state.isHsync() ? 0 : CRTC_HSYNC_DIS;
        val |= state.isVsync() ? 0 : CRTC_VSYNC_DIS;
        vgaIO.setReg32(CRTC_EXT_CNTL, val);
    }

    /**
     * Sets the palette to 8-bit RGB values.
     * 
     * @param brightness
     */
    private final void setPalette(float brightness) {
        for (int i = 0; i < 256; i++) {
            final int v = Math.min(255, (int) (i * brightness));
            vgaIO.setPaletteEntry(i, v, v, v);
        }
    }

    /**
     * Claim a portion of RAM on the device.
     * 
     * @param size
     * @param align
     * @return @throws
     *         IndexOutOfBoundsException
     * @throws ResourceNotFreeException
     */
    final MemoryResource claimDeviceMemory(long size, int align)
            throws IndexOutOfBoundsException, ResourceNotFreeException {
        return deviceRam.claimChildResource(size, align);
    }
}