/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.driver.video.spi.DpmsState;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.jnode.vm.Address;


/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class RadeonCore extends AbstractSurface implements RadeonConstants {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());
    private RadeonConfiguration config;
    private final RadeonDriver driver;
    private final MemoryResource mmio;
    private final MemoryResource videoRam;
    private final RadeonVgaIO vgaIO;
    private final int memSize;
    private final boolean hasCRTC2;
    private final RadeonVgaState oldVgaState;
    private final RadeonVgaState currentState;
    private int bitsPerPixel;
    private BitmapGraphics bitmapGraphics;
    private int startAddress;
    private int bytesPerLine;
    private final RadeonPLLInfo pllInfo;
    
	/**
	 * @param driver
	 * @param architecture
	 * @param model
	 * @param device
	 */
	public RadeonCore(RadeonDriver driver, int architecture, String model, PCIDevice device) throws ResourceNotFreeException, DriverException {
		super(640, 480);
		this.driver = driver;
		this.hasCRTC2 = (architecture != Architecture.R100); 
		this.pllInfo = new RadeonPLLInfo();
		
		final PCIDeviceConfig pciCfg = device.getConfig();
		final PCIBaseAddress ioAddr = pciCfg.getBaseAddresses()[2];
		final PCIBaseAddress fbAddr = pciCfg.getBaseAddresses()[0];
		log.info("Found ATI " + model + ", chipset 0x" + NumberUtils.hex(pciCfg.getRevision()));
		try {
			final ResourceManager rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
			final int ioBase = (int) ioAddr.getMemoryBase();
			final int ioSize = ioAddr.getSize();
			final int fbBase = (int) fbAddr.getMemoryBase() /*& 0xFF800000*/;


			this.mmio = rm.claimMemoryResource(device, Address.valueOf(ioBase), ioSize, ResourceManager.MEMMODE_NORMAL);
			this.vgaIO = new RadeonVgaIO(mmio);
			this.memSize = readMemorySize();

			this.videoRam = rm.claimMemoryResource(device, Address.valueOf(fbBase), memSize, ResourceManager.MEMMODE_NORMAL);
			vgaIO.setVideoRam(videoRam);
			log.debug("Found ATI " + model + ", FB at 0x" + NumberUtils.hex(fbBase) + "s0x" + NumberUtils.hex(memSize) + ", MMIO at 0x" + NumberUtils.hex(ioBase));

		} catch (NameNotFoundException ex) {
			throw new ResourceNotFreeException(ex);
		}
		//this.hwCursor = new NVidiaHardwareCursor(vgaIO, architecture);
		//this.acc = new NVidiaAcceleration(vgaIO, architecture);
		
		log.info("Memory size " + (memSize / (1024*1024)) + "MB");
		oldVgaState = new RadeonVgaState(hasCRTC2, vgaIO);
		currentState = new RadeonVgaState(hasCRTC2, vgaIO);
	}

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#convertColor(java.awt.Color)
     */
    protected final int convertColor(Color color) {
        return color.getRGB();
    }
    
    /**
     * @see org.jnode.driver.video.util.AbstractSurface#drawPixel(int, int, int, int)
     */
    protected void drawPixel(int x, int y, int color, int mode) {
		bitmapGraphics.drawPixels(x, y, 1, color, mode);
    }
    
    /**
     * @see org.jnode.driver.video.Surface#drawCompatibleRaster(java.awt.image.Raster, int, int, int, int, int, int, java.awt.Color)
     */
    public void drawCompatibleRaster(Raster raster, int srcX, int srcY,
            int dstX, int dstY, int width, int height, Color bgColor) {
		if (bgColor == null) {
			bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width, height);
		} else {
			bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width, height, convertColor(bgColor));
		}
    }
    
    /**
     * @see org.jnode.driver.video.Surface#getColorModel()
     */
    public final ColorModel getColorModel() {
        return config.getColorModel();
    }
    
	/**
	 * Open the given configuration
	 * 
	 * @param config
	 */
	final void open(RadeonConfiguration config) {
		this.config = config;
		// Save the current state
		oldVgaState.saveFromVGA(vgaIO);

		// Turn off the screen
		final DpmsState dpmsState = getDpms();
		setDpms(DpmsState.OFF);

		// Calculate new configuration
		this.bitsPerPixel = config.getBitsPerPixel();
		this.width = config.getScreenWidth();
		this.height = config.getScreenHeight();
		this.startAddress = 2048;
		final int pixelDepth = (this.bitsPerPixel + 1) / 8;
		this.bytesPerLine = width * pixelDepth;		
		currentState.saveFromVGA(vgaIO);
		log.info("OldState=" + currentState);
		currentState.calcForConfiguration(config, pllInfo, vgaIO);
		log.info("CalcState=" + currentState);
		
		// Set the new configuration
		currentState.restoreToVGA(vgaIO);
		vgaIO.setReg32(CRTC_OFFSET, startAddress);
		
		// Set the 8-bit palette
		setPalette(1.0f);

		// Create the graphics helper & clear the screen
		final int pixels = width * height;
		switch (bitsPerPixel) {
			case 8 :
				bitmapGraphics = BitmapGraphics.create8bppInstance(videoRam, width, height, bytesPerLine, startAddress);
				videoRam.setByte(startAddress, (byte) 0, pixels);
				break;
			case 16 :
				bitmapGraphics = BitmapGraphics.create16bppInstance(videoRam, width, height, bytesPerLine, startAddress);
				videoRam.setShort(startAddress, (byte) 0, pixels);
				break;
			case 24 :
				bitmapGraphics = BitmapGraphics.create24bppInstance(videoRam, width, height, bytesPerLine, startAddress);
				videoRam.setInt24(startAddress, 0, pixels);
				break;
			case 32 :
				bitmapGraphics = BitmapGraphics.create32bppInstance(videoRam, width, height, bytesPerLine, startAddress);
				videoRam.setInt(startAddress, 0, pixels);
				break;
		}

		// Turn the screen back on
		setDpms(dpmsState);
	}
	
	/**
	 * Close the SVGA screen
	 * 
	 * @see org.jnode.driver.video.Surface#close()
	 */
	public synchronized void close() {
		//hwCursor.closeCursor();
		final DpmsState dpmsState = getDpms();
		//log.debug("Old DPMS state: " + dpmsState);
		setDpms(DpmsState.OFF);
		oldVgaState.restoreToVGA(vgaIO);
		setDpms(dpmsState);

		driver.close(this);
		super.close();
		
		log.info("DAC_CNTL=0x" + NumberUtils.hex(vgaIO.getReg32(DAC_CNTL)));
	}
	
    /**
     * Release all resources
     *
     */
    final void release() {
        mmio.release();
        videoRam.release();
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
     * @param brightness
     */
	private final void setPalette(float brightness) {
		for (int i = 0; i < 256; i++) {
			final int v = Math.min(255, (int) (i * brightness));
			vgaIO.setPaletteEntry(i, v, v, v);
		}
	}
}
