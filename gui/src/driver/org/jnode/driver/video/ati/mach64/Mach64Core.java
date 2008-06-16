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

package org.jnode.driver.video.ati.mach64;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIConstants;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.driver.video.HardwareCursorAPI;
import org.jnode.driver.video.spi.DpmsState;
import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Extent;
import org.vmmagic.unboxed.Offset;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
final class Mach64Core implements Mach64Constants {

    /** My logger */
    private static final Logger log = Logger.getLogger(Mach64Core.class);

    /** The driver we belong to */
    private final Mach64Driver driver;

    /** Framebuffer memory */
    private final MemoryResource deviceRam;

    /** Memory mapped I/O region, block 0 */
    private final MemoryResource mmio0;

    /** I/O utility */
    private final Mach64VgaIO vgaIO;

    /** Register state before opening a surface */
    private final Mach64VgaState oldVgaState;

    /** Register state after opening a surface */
    private final Mach64VgaState currentState;

    /**
     * Initialize this instance.
     * 
     * @param driver
     * @param device
     * @throws DriverException
     */
    public Mach64Core(Mach64Driver driver, String model, PCIDevice device)
        throws ResourceNotFreeException, DriverException {
        this.driver = driver;

        final PCIDeviceConfig pciCfg = device.getConfig();
        // Disable VGA I/O, enable memory mapped I/O
        int cmd = pciCfg.getCommand();
        cmd &= ~PCIConstants.PCI_COMMAND_IO;
        cmd |= PCIConstants.PCI_COMMAND_MEMORY;
        pciCfg.setCommand(cmd);

        final PCIBaseAddress fbAddr = pciCfg.asHeaderType0().getBaseAddresses()[0];

        log.info("Found ATI " + model + ", pci " + pciCfg);

        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);

            final int fbBase = (int) fbAddr.getMemoryBase() /* & 0xFF800000 */;
            final int memSize = fbAddr.getSize();
            log.info("Memory size " + NumberUtils.size(memSize));

            // Map Device RAM
            this.deviceRam =
                    rm.claimMemoryResource(device, Address.fromIntZeroExtend(fbBase), memSize,
                            ResourceManager.MEMMODE_NORMAL);

            // Map MMIO block 0, first test for 8Mb framebuffers.
            Offset block0Ofs = Offset.fromIntZeroExtend(0x7ffc00);
            Extent mmioSize = Extent.fromIntZeroExtend(1024); // 1K
            MemoryResource mmio0 = deviceRam.claimChildResource(block0Ofs, mmioSize, false);
            Mach64VgaIO io = new Mach64VgaIO(deviceRam, mmio0);
            if ((io.getReg32(CONFIG_CHIP_ID) & CFG_CHIP_TYPE) != pciCfg.getDeviceID()) {
                // Try for 4Mb framebuffers.
                mmio0.release();
                block0Ofs = Offset.fromIntZeroExtend(0x3ffc00);
                mmio0 = deviceRam.claimChildResource(block0Ofs, mmioSize, false);
                io = new Mach64VgaIO(deviceRam, mmio0);
                if ((io.getReg32(CONFIG_CHIP_ID) & CFG_CHIP_TYPE) != pciCfg.getDeviceID()) {
                    throw new DriverException("Cannot find block0 registers.");
                }
            }
            this.vgaIO = io;
            this.mmio0 = mmio0;

            log.debug("Found ATI " + model + ", FB at 0x" + NumberUtils.hex(fbBase) + "s0x" +
                    NumberUtils.hex(memSize));

        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException(ex);
        }

        this.oldVgaState = new Mach64VgaState();
        this.currentState = new Mach64VgaState();
    }

    /**
     * Open the given configuration.
     * 
     * @param config
     * @return
     * @throws ResourceNotFreeException
     */
    final Mach64Surface open(Mach64Configuration config) throws ResourceNotFreeException {

        // Calculate new configuration
        final DisplayMode mode = config.getDisplayMode();
        final int width = mode.getWidth();
        final int height = mode.getHeight();
        final int pixels = width * height;
        final int bitsPerPixel = config.getBitsPerPixel();
        final int bytesPerLine = config.getBytesPerLine();
        final int bytesPerScreen = bytesPerLine * height;
        currentState.calcForConfiguration(config, vgaIO);

        // Allocate the screen memory
        final MemoryResource screen = claimDeviceMemory(bytesPerScreen, 4 * 1024);

        // Block all interrupts.
        vgaIO.disableIRQ();

        // Save the current state
        oldVgaState.saveFromVGA(vgaIO);

        // Turn off the screen
        final DpmsState dpmsState = getDpms();
        setDpms(DpmsState.OFF);

        try {
            // Set new configuration

            // Create the graphics helper & clear the screen
            final BitmapGraphics bitmapGraphics;
            switch (config.getBitsPerPixel()) {
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
            return new Mach64Surface(this, config, bitmapGraphics, screen);
        } finally {
            // Restore screen
            setDpms(dpmsState);
        }
    }

    /**
     * Close the display.
     */
    final void close() {

        driver.close(this);
    }

    /**
     * Release all resources.
     */
    final void release() {
        this.deviceRam.release();
        this.mmio0.release();
    }

    /**
     * Gets the hardware cursor API implementation.
     */
    final HardwareCursorAPI getHardwareCursor() {
        return null;
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
     * Gets the monitor power state.
     */
    final DpmsState getDpms() {
        final int val = vgaIO.getReg32(CRTC_GEN_CNTL);
        final boolean display = ((val & CRTC_DISPLAY_DIS) == 0);
        final boolean hsync = ((val & CRTC_HSYNC_DIS) == 0);
        final boolean vsync = ((val & CRTC_VSYNC_DIS) == 0);
        return new DpmsState(display, hsync, vsync);
    }

    /**
     * Sets the monitor power state.
     */
    final void setDpms(DpmsState state) {
        int crtc_ext_cntl = vgaIO.getReg32(CRTC_GEN_CNTL);

        crtc_ext_cntl &= ~(CRTC_DISPLAY_DIS | CRTC_HSYNC_DIS | CRTC_VSYNC_DIS);

        if (!state.isDisplay()) {
            crtc_ext_cntl |= CRTC_DISPLAY_DIS;
        }
        if (!state.isHsync()) {
            crtc_ext_cntl |= CRTC_HSYNC_DIS;
        }
        if (!state.isVsync()) {
            crtc_ext_cntl |= CRTC_VSYNC_DIS;
        }

        vgaIO.setReg32(CRTC_GEN_CNTL, crtc_ext_cntl);
        log.debug("Set CRTC_GEN_CNTL to 0x" + NumberUtils.hex(crtc_ext_cntl));
    }
}
