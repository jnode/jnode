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

package org.jnode.driver.video.ati.radeon;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.bus.pci.PCIRomAddress;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.spi.DpmsState;
import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.MemoryScanner;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class RadeonCore implements RadeonConstants {

    /** My logger */
    private static final Logger log = Logger.getLogger(RadeonCore.class);

    /** The driver */
    private final RadeonDriver driver;

    /** Memory mapped IO space */
    private final MemoryResource mmio;

    /** All video RAM of the device */
    private final MemoryResource deviceRam;

    /** Video ROM (can be null) */
    private final MemoryResource rom;

    /** Register accessor */
    private final RadeonVgaIO vgaIO;

    /** Register state before opening a surface */
    private final RadeonVgaState oldVgaState;

    /** Register state after opening a surface */
    private final RadeonVgaState currentState;

    /** Hardware cursor implementation */
    private final RadeonHardwareCursor hwCursor;

    /** General Radeon Framebuffer info */
    private final FBInfo fbinfo;

    /** The acceleration functions */
    private final RadeonAcceleration accel;

    /**
     * @param driver
     * @param architecture
     * @param model
     * @param device
     */
    public RadeonCore(RadeonDriver driver, int architecture, String model, PCIDevice device)
            throws ResourceNotFreeException, DriverException {
        this.driver = driver;
        this.fbinfo = new FBInfo(architecture);

        final PCIHeaderType0 pciCfg = device.getConfig().asHeaderType0();
        final PCIBaseAddress ioAddr = pciCfg.getBaseAddresses()[2];
        final PCIBaseAddress fbAddr = pciCfg.getBaseAddresses()[0];
        final PCIRomAddress romAddr = pciCfg.getRomAddress();
        log.info("Found ATI " + model + ", chipset 0x" + NumberUtils.hex(pciCfg.getRevision()));
        try {
            final ResourceManager rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
            final int ioBase = (int) ioAddr.getMemoryBase();
            final int ioSize = ioAddr.getSize();
            final int fbBase = (int) fbAddr.getMemoryBase() /* & 0xFF800000 */;

            // Map Memory Mapped IO
            this.mmio =
                    rm.claimMemoryResource(device, Address.fromIntZeroExtend(ioBase), ioSize,
                            ResourceManager.MEMMODE_NORMAL);
            this.vgaIO = new RadeonVgaIO(mmio);
            final int memSize = readMemorySize();
            log.info("Memory size " + NumberUtils.size(memSize));
            this.accel = new RadeonAcceleration(vgaIO);

            // Map Device RAM
            this.deviceRam =
                    rm.claimMemoryResource(device, Address.fromIntZeroExtend(fbBase), memSize,
                            ResourceManager.MEMMODE_NORMAL);
            vgaIO.setVideoRam(deviceRam);

            // Find ROM
            MemoryResource rom = null;
            if (romAddr != null) {
                romAddr.setEnabled(true);
                if (romAddr.isEnabled()) {
                    rom =
                            rm.claimMemoryResource(device, Address.fromIntZeroExtend(romAddr
                                    .getRomBase()), romAddr.getSize(),
                                    ResourceManager.MEMMODE_NORMAL);
                    if (!verifyBiosSignature(rom)) {
                        log.info("Signature mismatch");
                        rom.release();
                        rom = null;
                    }
                } else {
                    log.debug("Failed to enabled expansion ROM");
                }
            }
            if (rom == null) {
                // Use the ISA regions rom instead
                rom = findRom(device, rm);
            }
            this.rom = rom;

            log.debug("Found ATI " + model + ", FB at 0x" + NumberUtils.hex(fbBase) + "s0x" +
                    NumberUtils.hex(memSize) + ", MMIO at 0x" + NumberUtils.hex(ioBase) + ", ROM " +
                    pciCfg.getRomAddress());

            fbinfo.readMonitorInfo(vgaIO);
            if (this.rom != null) {
                log.info("ROM[0-3] 0x" + NumberUtils.hex(rom.getInt(0)));
                // Read monitor information
                fbinfo.readFPIInfo(rom);
            }

        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException(ex);
        }

        // Read the current state of the device
        this.oldVgaState = new RadeonVgaState(architecture, fbinfo.hasCRTC2, vgaIO);
        this.currentState = new RadeonVgaState(architecture, fbinfo.hasCRTC2, vgaIO);

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
    final RadeonSurface open(RadeonConfiguration config) throws ResourceNotFreeException {

        // Get the best matching config
        config = fbinfo.getBestConfiguration(config);
        log.debug("BestConfig:" + config);

        // Calculate new configuration
        final DisplayMode mode = config.getDisplayMode();
        final int width = mode.getWidth();
        final int height = mode.getHeight();
        final int pixels = width * height;
        final int bitsPerPixel = config.getBitsPerPixel();
        final int bytesPerLine = config.getBytesPerLine();
        final int bytesPerScreen = bytesPerLine * height;
        log.debug("PLLInfo:" + fbinfo.getPllInfo());
        currentState.calcForConfiguration(config, vgaIO, fbinfo);

        // Disable video interrupts
        vgaIO.disableIRQ();

        // Allocate the screen memory
        final MemoryResource screen = claimDeviceMemory(bytesPerScreen, 4 * 1024);
        // final MemoryResource screen = deviceRam;
        log.debug("Screen at 0x" + NumberUtils.hex(screen.getOffset().toInt()) + ", size 0x" +
                NumberUtils.hex(screen.getSize().toInt()));

        // if (true) { throw new ResourceNotFreeException("TEST"); }

        // Save the current state
        oldVgaState.saveFromVGA(vgaIO);
        log.debug("oldState:" + oldVgaState);

        // Turn off the screen
        final DpmsState dpmsState = getDpms();
        setDpms(DpmsState.OFF);

        try {
            // Set the new configuration
            currentState.restoreToVGA(vgaIO);
            log.debug("NewState: " + currentState);
            vgaIO.setReg32(CRTC_OFFSET, (int) screen.getOffset().toInt());
            if (fbinfo.hasCRTC2) {
                vgaIO.setReg32(CRTC2_OFFSET, (int) screen.getOffset().toInt());
            }

            // Set the 8-bit palette
            setPalette(1.0f);

            // Create the graphics helper & clear the screen
            final BitmapGraphics bitmapGraphics;
            switch (bitsPerPixel) {
                case 8:
                    bitmapGraphics =
                            BitmapGraphics.create8bppInstance(screen, width, height, bytesPerLine,
                                    0);
                    screen.setByte(0, (byte) 0, pixels);
                    break;
                case 16:
                    bitmapGraphics =
                            BitmapGraphics.create16bppInstance(screen, width, height, bytesPerLine,
                                    0);
                    screen.setShort(0, (byte) 0, pixels);
                    break;
                case 24:
                    bitmapGraphics =
                            BitmapGraphics.create24bppInstance(screen, width, height, bytesPerLine,
                                    0);
                    screen.setInt24(0, 0, pixels);
                    break;
                case 32:
                    bitmapGraphics =
                            BitmapGraphics.create32bppInstance(screen, width, height, bytesPerLine,
                                    0);
                    screen.setInt(0, 0, pixels);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid bits per pixel " + bitsPerPixel);
            }
            return new RadeonSurface(this, config, bitmapGraphics, screen, accel);
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
        if (rom != null) {
            rom.release();
        }
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
        final int val2 = vgaIO.getReg32(LVDS_GEN_CNTL);
        final boolean display;
        final boolean hsync;
        final boolean vsync;
        if (fbinfo.getDviDispType() == MonitorType.LCD) {
            display = ((val2 & LVDS_DISPLAY_DIS) == 0);
            hsync = vsync = display;
        } else {
            display = ((val & CRTC_DISPLAY_DIS) == 0);
            hsync = ((val & CRTC_HSYNC_DIS) == 0);
            vsync = ((val & CRTC_VSYNC_DIS) == 0);
        }
        return new DpmsState(display, hsync, vsync);
    }

    /**
     * Sets the monitor power state.
     */
    final void setDpms(DpmsState state) {
        int crtc_ext_cntl = vgaIO.getReg32(CRTC_EXT_CNTL);
        int lvds_gen_cntl = vgaIO.getReg32(LVDS_GEN_CNTL);
        // log.debug("Get LVDS_GEN_CTRL 0x" + NumberUtils.hex(lvds_gen_cntl));

        crtc_ext_cntl &= ~(CRTC_DISPLAY_DIS | CRTC_HSYNC_DIS | CRTC_VSYNC_DIS);
        lvds_gen_cntl &= ~(LVDS_DISPLAY_DIS | LVDS_ON);

        if (state.isDisplay()) {
            lvds_gen_cntl |= (LVDS_BLON | LVDS_ON);
        } else {
            crtc_ext_cntl |= CRTC_DISPLAY_DIS;
            lvds_gen_cntl |= LVDS_DISPLAY_DIS;
        }
        if (!state.isHsync()) {
            crtc_ext_cntl |= CRTC_HSYNC_DIS;
        }
        if (!state.isVsync()) {
            crtc_ext_cntl |= CRTC_VSYNC_DIS;
        }

        if (fbinfo.getDviDispType() == MonitorType.LCD) {
            vgaIO.setReg32(LVDS_GEN_CNTL, lvds_gen_cntl);
            log.debug("Set LVDS_GEN_CTRL to 0x" + NumberUtils.hex(lvds_gen_cntl));
        } else {
            vgaIO.setReg32(CRTC_EXT_CNTL, crtc_ext_cntl);
            log.debug("Set CRTC_EXT_CNTL to 0x" + NumberUtils.hex(crtc_ext_cntl));
        }
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
     * @return
     * @throws IndexOutOfBoundsException
     * @throws ResourceNotFreeException
     */
    final MemoryResource claimDeviceMemory(int size, int align)
        throws IndexOutOfBoundsException, ResourceNotFreeException {
        return deviceRam.claimChildResource(size, align);
    }

    /**
     * Look for the ROM in the ISA region.
     * 
     * @param rm
     * @return The claimed ROM region, or null if not found.
     */
    private final MemoryResource findRom(final ResourceOwner owner, final ResourceManager rm)
        throws ResourceNotFreeException {
        final MemoryScanner scanner =
                AccessController.doPrivileged(new PrivilegedAction<MemoryScanner>() {

                    public MemoryScanner run() {
                        return rm.getMemoryScanner();
                    }
                });

        final Address start = Address.fromIntZeroExtend(0xC0000);
        final Address end = Address.fromIntZeroExtend(0xF0000);
        final int size = end.toWord().sub(start.toWord()).toInt();
        final int stepSize = 0x1000;
        int offset = 0;
        while (offset < size) {
            final Address romAddr;
            // Search for BIOS expansion
            romAddr =
                    scanner.findInt8Array(start.add(offset), size - offset, BIOS_ROM_SIGNATURE, 0,
                            BIOS_ROM_SIGNATURE.length, stepSize);
            if (romAddr == null) {
                return null;
            } else {
                offset = romAddr.toWord().sub(start.toWord()).toInt() + stepSize;
            }
            // Search for ATI signature
            final Address atiSigAddr;
            atiSigAddr =
                    scanner.findInt8Array(romAddr, 128, ATI_ROM_SIGNATURE, 0,
                            ATI_ROM_SIGNATURE.length, 1);
            if (atiSigAddr == null) {
                continue;
            }

            // We found it
            // Claim a small region, so we can read the size.
            MemoryResource mem;
            mem = rm.claimMemoryResource(owner, romAddr, 4, ResourceManager.MEMMODE_NORMAL);
            final int blocks = mem.getByte(2) & 0xFF;
            final int romSize = blocks * 512;
            mem.release();

            log.info("Found ATI ROM at 0x" + NumberUtils.hex(romAddr.toInt()) + " size=" +
                    NumberUtils.size(romSize));
            return rm.claimMemoryResource(owner, romAddr, romSize, ResourceManager.MEMMODE_NORMAL);
        }

        return null;
    }

    /**
     * Verify the ROM signature as being an ATI BIOS expansion area.
     * 
     * @param rom
     * @return
     */
    private final boolean verifyBiosSignature(MemoryResource rom) {
        if ((rom.getByte(0) & 0xFF) != 0x55) {
            return false;
        }
        if ((rom.getByte(1) & 0xFF) != 0xAA) {
            return false;
        }
        return true;
    }
}
