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
 
package org.jnode.driver.video.nvidia;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.awt.util.BitmapGraphics;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.video.FrameBufferConfiguration;
import org.jnode.driver.video.ddc.DisplayDataChannelAPI;
import org.jnode.driver.video.spi.DpmsState;
import org.jnode.driver.video.util.AbstractSurface;
import org.jnode.driver.video.vgahw.DisplayMode;
import org.jnode.naming.InitialNaming;
import org.jnode.system.MemoryResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.util.NumberUtils;
import org.vmmagic.unboxed.Address;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NVidiaCore extends AbstractSurface implements NVidiaConstants, DisplayDataChannelAPI {
	/** Acceleration functions */
	private final NVidiaAcceleration acc;
	private final int architecture;
	private BitmapGraphics bitmapGraphics;
	private int bitsPerPixel = 32;
	private int bytesPerLine;
	private FrameBufferConfiguration config;
	private final int CrystalFreqKHz;
	private final int DDCBase = 0x3e;
	private final NVidiaDriver driver;
	/** Hardware cursor implementation */
	private final NVidiaHardwareCursor hwCursor;
	/** My logger */
	private static final Logger log = Logger.getLogger(NVidiaCore.class);
	private final int MaxVClockFreqKHz;
	/** Size of card memory in MB */
	private final int memSize;
	private final MemoryResource mmio;
	private final NVidiaVgaState oldVgaState = new NVidiaVgaState();
	/** Should acceleration be used */
	private final boolean useAcc = true;
	private final NVidiaVgaIO vgaIO;
	private final MemoryResource videoRam;

	/**
	 * @param driver
	 * @param architecture
	 * @param model
	 * @param device
	 */
	public NVidiaCore(NVidiaDriver driver, int architecture, String model, PCIDevice device) throws ResourceNotFreeException, DriverException {
		super(640, 480);
		this.driver = driver;
		this.architecture = architecture;
		final PCIHeaderType0 pciCfg = device.getConfig().asHeaderType0();
		final PCIBaseAddress ioAddr = pciCfg.getBaseAddresses()[0];
		final PCIBaseAddress fbAddr = pciCfg.getBaseAddresses()[1];
		log.info("Found NVidia " + model + ", chipset 0x" + NumberUtils.hex(pciCfg.getRevision()));
		try {
			final ResourceManager rm = (ResourceManager) InitialNaming.lookup(ResourceManager.NAME);
			final int ioBase = (int) ioAddr.getMemoryBase() & 0xFF800000;
			final int ioSize = ioAddr.getSize();
			final int fbBase = (int) fbAddr.getMemoryBase() & 0xFF800000;
			final int fbSize = fbAddr.getSize();

			log.debug("Found NVidia, FB at 0x" + NumberUtils.hex(fbBase) + "s0x" + NumberUtils.hex(fbSize) + ", MMIO at 0x" + NumberUtils.hex(ioBase));

			this.mmio = rm.claimMemoryResource(device, Address.fromIntZeroExtend(ioBase), ioSize, ResourceManager.MEMMODE_NORMAL);
			this.videoRam = rm.claimMemoryResource(device, Address.fromIntZeroExtend(fbBase), fbSize, ResourceManager.MEMMODE_NORMAL);
			this.vgaIO = new NVidiaVgaIO(mmio, videoRam);

		} catch (NameNotFoundException ex) {
			throw new ResourceNotFreeException(ex);
		}
		this.hwCursor = new NVidiaHardwareCursor(vgaIO, architecture);
		this.acc = new NVidiaAcceleration(vgaIO, architecture);
		this.memSize = getMemorySize();

		switch (architecture) {
			case NV04A :
				{
					final int bootInfo = vgaIO.getReg32(NV32_NVSTRAPINFO2);
					CrystalFreqKHz = ((bootInfo & 0x00000040) != 0) ? 14318 : 13500;
					MaxVClockFreqKHz = 250000;
				}
				break;
			case NV10A :
				{
					final int bootInfo = vgaIO.getReg32(NV32_NVSTRAPINFO2);
					CrystalFreqKHz = ((bootInfo & 0x00000040) != 0) ? 14318 : 13500;
					MaxVClockFreqKHz = 350000;
				}
				break;
			case NV28M :
				{
					final int bootInfo = vgaIO.getReg32(NV32_NVSTRAPINFO2);
					CrystalFreqKHz = ((bootInfo & 0x00000040) != 0) ? 14318 : 13500;
					MaxVClockFreqKHz = 200000;
				}
				break;
			default :
				throw new DriverException("Unknown architecture " + architecture);
		}
		log.debug("Memory size     =" + memSize + "MB");
		log.debug("CrystalFreqKHz  =" + CrystalFreqKHz);
		log.debug("MaxVClockFreqKHz=" + MaxVClockFreqKHz);
	}

	/**
	 * Calculate the best VClock settings
	 * 
	 * @param clockIn
	 * @return The best values in the form of { M, N, P, clock }
	 */
	private int[] calcVCLock(int clockIn) {
		int DeltaOld = Integer.MAX_VALUE;
		final int VClk = clockIn;
		final int lowM;
		final int highM;
		final boolean isNV3 = (architecture < NV04A);
		if (CrystalFreqKHz == 14318) {
			lowM = 8;
			highM = 14 - (isNV3 ? 1 : 0);
		} else {
			lowM = 7;
			highM = 13 - (isNV3 ? 1 : 0);
		}
		final int highP = 4 - (isNV3 ? 1 : 0);
		final int best[] = new int[4];
		for (int P = 0; P <= highP; P++) {
			int Freq = VClk << P;
			if ((Freq >= 128000) && (Freq <= MaxVClockFreqKHz)) {
				for (int M = lowM; M <= highM; M++) {
					final int N = (VClk * M / CrystalFreqKHz) << P;
					final int DeltaNew;
					Freq = (CrystalFreqKHz * N / M) >> P;
					if (Freq > VClk) {
						DeltaNew = Freq - VClk;
					} else {
						DeltaNew = VClk - Freq;
					}
					if (DeltaNew < DeltaOld) {
						best[0] = M;
						best[1] = N;
						best[2] = P;
						best[3] = Freq;
						DeltaOld = DeltaNew;
					}
				}
			}
		}
		if (DeltaOld == Integer.MAX_VALUE) {
			throw new RuntimeException("Cannot find a suitable VClock");
		} else {
			return best;
		}
	}

	/**
	 * Close the SVGA screen
	 * 
	 * @see org.jnode.driver.video.Surface#close()
	 */
	public synchronized void close() {
	    log.debug("close");
		hwCursor.closeCursor();
		final DpmsState dpmsState = getDpms();
		log.debug("Old DPMS state: " + dpmsState);
		setDpms(DpmsState.OFF);
		vgaIO.unlock();
	    log.debug("restore old VGA state");
		oldVgaState.restoreToVGA(vgaIO);
	    log.debug("restore DPMS state");
		setDpms(dpmsState);

		// For debugging purposes
		//final NVidiaVgaState debugState = new NVidiaVgaState();
		//debugState.saveFromVGA(vgaIO);
		//log.debug("Restored state: " + debugState);
		// End of debugging purposes

		driver.close(this);
		super.close();
		log.debug("End of close");
	}

	/**
	 * Terminate a DDC1 readout
	 */
	public void closeDDC1() {
		// Disable access to extended registers
		//vgaIO.lock();
	}

	/**
	 * @see org.jnode.driver.video.util.AbstractSurface#convertColor(java.awt.Color)
	 */
	protected int convertColor(Color color) {
		return color.getRGB();
	}

	/**
	 * @see org.jnode.driver.video.Surface#copyArea(int, int, int, int, int, int)
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		bitmapGraphics.copyArea(x, y, width, height, dx, dy);
	}

	/**
	 * @see org.jnode.driver.video.Surface#drawCompatibleRaster(java.awt.image.Raster, int, int, int, int, int, int, java.awt.Color)
	 */
	public final void drawCompatibleRaster(Raster raster, int srcX, int srcY, int dstX, int dstY, int width, int height, Color bgColor) {
		if (bgColor == null) {
			bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width, height);
		} else {
			bitmapGraphics.drawImage(raster, srcX, srcY, dstX, dstY, width, height, convertColor(bgColor));
		}
	}

	/**
	 * @see org.jnode.driver.video.util.AbstractSurface#drawPixel(int, int, int, int)
	 */
	protected final void drawPixel(int x, int y, int color, int mode) {
		bitmapGraphics.drawPixels(x, y, 1, color, mode);
	}
	/**
	 * @see org.jnode.driver.video.util.AbstractSurface#fillRect(int, int, int, int, int, int)
	 */
	public void fillRect(int x, int y, int w, int h, int color, int mode) {
		final int screenWidth = config.getScreenWidth();
		if (x + w >= screenWidth) {
			w = screenWidth - x;				
		}
		if (useAcc && (mode == PAINT_MODE)) {
			acc.setupRectangle(color);
			acc.fillRectangle(x, y, w, h);
		} else {
			if ((x == 0) && (w == screenWidth)) {
				bitmapGraphics.drawPixels(0, y, screenWidth * height, color, mode);
			} else {
				super.fillRect(x, y, w, h, color, mode);
			}
		}
	}

	/**
	 * @see org.jnode.driver.video.Surface#getColorModel()
	 */
	public ColorModel getColorModel() {
		return config.getColorModel();
	}

	/**
	 * @see org.jnode.driver.video.ddc.DisplayDataChannelAPI#getDDC1Bit()
	 */
	public boolean getDDC1Bit() {
		/* wait for Vsync */
		while ((vgaIO.getSTAT() & 0x08) != 0) { /* wait */
		}
		while ((vgaIO.getSTAT() & 0x08) == 0) { /* wait */
		}

		/* Get the result */
		final int val = vgaIO.getCRT(DDCBase);
		//log.debug("getDDC1Bit: val=0x" + NumberUtils.hex(val, 2));
		return ((val & DDC_SDA_READ_MASK) != 0);
	}

	/**
	 * Gets the current DPMS state
	 * 
	 * @return
	 */
	private DpmsState getDpms() {
		final boolean display = ((vgaIO.getSEQ(NVSEQX_CLKMODE) & 0x20) == 0);
		final int repaint1 = vgaIO.getCRT(NVCRTCX_REPAINT1);
		final boolean hsync = ((repaint1 & 0x80) == 0);
		final boolean vsync = ((repaint1 & 0x40) == 0);
		return new DpmsState(display, hsync, vsync);
	}

	/**
	 * Gets the hardware cursor implementation
	 */
	public NVidiaHardwareCursor getHardwareCursor() {
		return hwCursor;
	}

	/**
	 * Detect the size of installed memory.
	 * 
	 * @return Size in MB.
	 */
	private final int getMemorySize() {
		final int size;
		if (architecture == NV04A) {
			final int strapinfo = vgaIO.getReg32(NV32_NV4STRAPINFO);

			if ((strapinfo & 0x00000100) != 0) {
				/* Unified memory architecture used */
				log.debug("INFO: NV4 architecture chip with UMA detected");
				size = ((((strapinfo & 0x0000f000) >> 12) * 2) + 2);

			} else {
				/* private memory architecture used */
				switch (strapinfo & 0x00000003) {
					case 0 :
						size = 32;
						break;
					case 1 :
						size = 4;
						break;
					case 2 :
						size = 8;
						break;
					case 3 :
						size = 16;
						break;
					default :
						// Cannot get here, but just to keep the compiler silent.
						size = 8;
				}
			}
		} else {
			// NV10, NV20
			//final int dev_manID = CFGR(DEVID);
			final int strapinfo = vgaIO.getReg32(NV32_NV10STRAPINFO);

			//case 0x01a010de: /* Nvidia GeForce2 Integrated GPU */
			//	size = (((CFGR(GF2IGPU) & 0x000007c0) >> 6) + 1);
			//	break;
			//case 0x01f010de: /* Nvidia GeForce4 MX Integrated GPU */
			//	size = (((CFGR(GF4MXIGPU) & 0x000007f0) >> 4) + 1);
			//remove this line if det. is OK: int amt = pciReadLong(pciTag(0, 0, 1), 0x84);
			//	break;
      log.info("strapinfo "+strapinfo);
			switch ((strapinfo & 0x0ff00000) >> 20) {
				case 2 :
					size = 2;
					break;
				case 4 :
					size = 4;
					break;
				case 8 :
					size = 8;
					break;
				case 16 :
					size = 16;
					break;
				case 32 :
					size = 32;
					break;
				case 64 :
					size = 64;
					break;
				case 128 :
					size = 128;
					break;
				default :
					size = 16;
					log.info("NV10/20 architecture chip with unknown RAM amount detected, setting 16Mb");
					break;
			}
		}
		return size;
	}

	/**
	 * Gets the start address of the video memory
	 */
	final int getVideoStartAddress() {
		int startadd = 0;
		if (architecture < NV10A) {
			/* upto 32Mb RAM adressing: must be used this way on pre-NV10! */

			/* set standard registers */
			/* (NVidia: startadress in 32bit words (b2 - b17) */
			startadd |= ((vgaIO.getCRT(NVCRTCX_FBSTADDL) & 0xFF) << 2);
			startadd |= ((vgaIO.getCRT(NVCRTCX_FBSTADDH) & 0xFF) << 10);

			/* set extended registers */
			/* NV4 extended bits: (b18-22) */
			int temp = vgaIO.getCRT(NVCRTCX_REPAINT0) & 0x1f;
			startadd |= (temp << 18);
			/* NV4 extended bits: (b23-24) */
			temp = vgaIO.getCRT(NVCRTCX_HEB) & 0x60;
			startadd |= (temp << 18);
		} else {
			/* upto 4Gb RAM adressing: must be used on NV10 and later! */
			/*
			 * NOTE: While this register also exists on pre-NV10 cards, it will wrap-around at 16Mb boundaries!!
			 */

			/* 30bit adress in 32bit words */
			startadd = vgaIO.getReg32(NV32_NV10FBSTADD32) & 0xfffffffc;
		}
		return startadd;
	}

	/**
	 * Open the given configuration
	 * 
	 * @param config
	 */
	final void open(NVidiaConfiguration config) {
		this.config = config;
		// Save the current state
		oldVgaState.saveFromVGA(vgaIO);
		//log.debug("Old VGA State: " + oldVgaState);
		//log.debug("Old start address:0x" +
		// NumberUtils.hex(getVideoStartAddress()));

		// Enabled access to extended registers
		vgaIO.unlock();

		// Turn off the screen
		final DpmsState dpmsState = getDpms();
		//log.debug("Old DPMS state: " + dpmsState);
		setDpms(DpmsState.OFF);

		/* power-up all nvidia hardware function blocks */
		/*
		 * bit 28: OVERLAY ENGINE (BES), bit 25: CRTC2, (> NV04A) bit 24: CRTC1, bit 20: framebuffer, bit 16: PPMI, bit 12: PGRAPH, bit 8: PFIFO, bit 4: PMEDIA, bit 0: TVOUT. (> NV04A)
		 */
		//log.debug("PWRUPCTRL=0x" +
		// NumberUtils.hex(mmio.getInt(NV32_PWRUPCTRL)));
		mmio.setInt(NV32_PWRUPCTRL, 0x13111111);

		// Set the new configuration
		this.bitsPerPixel = config.getBitsPerPixel();
		this.width = config.getScreenWidth();
		this.height = config.getScreenHeight();
		final int pixelDepth = (this.bitsPerPixel + 1) / 8;
		this.bytesPerLine = width * pixelDepth;
		final int startAddress = 2048;
		try {
			final NVidiaVgaState newState = config.getVgaState();
			newState.calcForConfiguration(config, architecture, vgaIO);
			newState.restoreToVGA(vgaIO);
			setPLL(config.getMode());
			//setColourDepth
			setPalette(1.0f);
			// setDacMode
			setPitch(bytesPerLine);
			setVideoStartAddress(startAddress);
			setTiming(config.getMode());
			hwCursor.initCursor();
			// Setup accelration
			acc.accInit(startAddress, bytesPerLine, memSize, bitsPerPixel);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
	 * Release all resources
	 */
	final void release() {
		mmio.release();
		videoRam.release();
	}

	private void setDpms(DpmsState state) {
		//log.debug("Setting DPMS to " + state);
		/* start synchronous reset: required before turning screen off! */
		vgaIO.setSEQ(NVSEQX_RESET, 0x01);

		/* turn screen off */
		int clkmode = vgaIO.getSEQ(NVSEQX_CLKMODE);
		if (state.isDisplay()) {
			vgaIO.setSEQ(NVSEQX_CLKMODE, clkmode & ~0x20);
			/* end synchronous reset if display should be enabled */
			vgaIO.setSEQ(NVSEQX_RESET, 0x03);
		} else {
			vgaIO.setSEQ(NVSEQX_CLKMODE, clkmode | 0x20);
		}

		int repaint1 = vgaIO.getCRT(NVCRTCX_REPAINT1);
		if (state.isHsync()) {
			repaint1 &= 0x7f;
		} else {
			repaint1 |= 0x80;
		}
		if (state.isVsync()) {
			repaint1 &= 0xbf;
		} else {
			repaint1 |= 0x40;
		}
		vgaIO.setCRT(NVCRTCX_REPAINT1, repaint1);
	}

	private final void setPalette(float brightness) {
		vgaIO.setReg8(NV8_PALMASK, 0xff);
		for (int i = 0; i < 256; i++) {
			int v = (int) (i * brightness);
			if (v > 255) {
				v = 255;
			}
			vgaIO.setDACWriteIndex(i);
			vgaIO.setDACData(v); // r
			vgaIO.setDACData(v); // g
			vgaIO.setDACData(v); // b
		}
	}

	private void setPitch(int bytesPerLine) {
		final int offset = bytesPerLine / 8;
		//log.info("setPitch: offset 0x" + NumberUtils.hex(offset) + "
		// bytesPerLine 0x" + NumberUtils.hex(bytesPerLine));
		//program the card!
		vgaIO.setCRT(NVCRTCX_PITCHL, offset & 0x00ff);
		final int temp = vgaIO.getCRT(NVCRTCX_REPAINT0);
		vgaIO.setCRT(NVCRTCX_REPAINT0, (temp & 0x1f) | ((offset & 0x0700) >> 3));
	}

	private void setPLL(DisplayMode mode) {
		final int[] best = calcVCLock(mode.getFreq());
		final int m = best[0];
		final int n = best[1];
		final int p = best[2];
		final int freq = best[3];
		log.debug("Programming PLL to M" + NumberUtils.hex(m, 2) + " N" + NumberUtils.hex(n, 2) + " P" + NumberUtils.hex(p, 2) + " at " + freq + "KHz");

		// select pixelPLL registerset C
		vgaIO.setReg32(NVDAC_PLLSEL, 0x10000700);

		// program new frequency
		vgaIO.setReg32(NVDAC_PIXPLLC, ((p << 16) | (n << 8) | m));
	}

	private void setTiming(DisplayMode mode) {
		log.debug("Setting timing to " + mode);
		// Modify parameters as required by standard VGA
		final int htotal = ((mode.getHTotal() >> 3) - 5);
		final int hdisp_e = ((mode.getWidth() >> 3) - 1);
		final int hblnk_s = hdisp_e;
		final int hblnk_e = (htotal + 4); //0;
		final int hsync_s = (mode.getHsyncStart() >> 3);
		final int hsync_e = (mode.getHsyncEnd() >> 3);

		final int vtotal = mode.getVTotal() - 2;
		final int vdisp_e = mode.getHeight() - 1;
		final int vblnk_s = vdisp_e;
		final int vblnk_e = (vtotal + 1);
		final int vsync_s = mode.getVsyncStart(); //-1;
		final int vsync_e = mode.getVsyncEnd(); //-1;

		log.debug("HOR:" + htotal + " " + hdisp_e + " " + hblnk_s + " " + hblnk_e + " " + hsync_s + " " + hsync_e);
		log.debug("VER:" + vtotal + " " + vdisp_e + " " + vblnk_s + " " + vblnk_e + " " + vsync_s + " " + vsync_e);

		/*
		 * prevent memory adress counter from being reset (linecomp may not occur)
		 */
		final int linecomp = 0x3ff; // mode.getHeight();

		//	  fixme: flatpanel 'don't touch' update needed for 'Go' cards!?!
		if (true) {
			/* actually program the card! */
			/* unlock CRTC registers at index 0-7 */
			//vgaIO.setCRT(NVCRTCX_LOCK, 0x57);
			vgaIO.unlock();
			/* horizontal standard VGA regs */
			vgaIO.setCRT(NVCRTCX_HTOTAL, (htotal & 0xff));
			vgaIO.setCRT(NVCRTCX_HDISPE, (hdisp_e & 0xff));
			vgaIO.setCRT(NVCRTCX_HBLANKS, (hblnk_s & 0xff));
			/* also unlock vertical retrace registers in advance */
			vgaIO.setCRT(NVCRTCX_HBLANKE, ((hblnk_e & 0x1f) | 0x80));
			vgaIO.setCRT(NVCRTCX_HSYNCS, (hsync_s & 0xff));
			vgaIO.setCRT(NVCRTCX_HSYNCE, ((hsync_e & 0x1f) | ((hblnk_e & 0x20) << 2)));

			/* vertical standard VGA regs */
			vgaIO.setCRT(NVCRTCX_VTOTAL, (vtotal & 0xff));
			int overflow = 0;
			overflow |= ((vtotal & 0x100) >> (8 - 0)); // VDT_8
			overflow |= ((vdisp_e & 0x100) >> (8 - 1)); // VDE_8
			overflow |= ((vsync_s & 0x100) >> (8 - 2)); // VRS_8
			overflow |= ((vblnk_s & 0x100) >> (8 - 3)); // VBS_8
			overflow |= ((vtotal & 0x200) >> (9 - 5)); // VDT_9
			overflow |= ((vdisp_e & 0x200) >> (9 - 6)); // VDE_9
			overflow |= ((vsync_s & 0x200) >> (9 - 7)); // VRS_9
			vgaIO.setCRT(NVCRTCX_OVERFLOW, overflow);
			vgaIO.setCRT(NVCRTCX_PRROWSCN, 0x00); /* not used */

			int maxsclin = 0;
			maxsclin |= ((vblnk_s & 0x200) >> (9 - 5)); // VBS_9
			maxsclin |= ((linecomp & 0x200) >> (9 - 6)); // LC_9
			vgaIO.setCRT(NVCRTCX_MAXSCLIN, maxsclin);
			vgaIO.setCRT(NVCRTCX_VSYNCS, (vsync_s & 0xff));
			vgaIO.setCRT(NVCRTCX_VSYNCE, ((vgaIO.getCRT(NVCRTCX_VSYNCE) & 0x70) | (vsync_e & 0x0f)));
			vgaIO.setCRT(NVCRTCX_VDISPE, (vdisp_e & 0xff));
			vgaIO.setCRT(NVCRTCX_VBLANKS, (vblnk_s & 0xff));
			vgaIO.setCRT(NVCRTCX_VBLANKE, (vblnk_e & 0xff));
			vgaIO.setCRT(NVCRTCX_LINECOMP, (linecomp & 0xff));

			/* horizontal extended regs */
			int heb = vgaIO.getCRT(NVCRTCX_HEB) & 0xe0;
			heb |= ((htotal & 0x100) >> (8 - 0)); // HDT_8
			heb |= ((hdisp_e & 0x100) >> (8 - 1)); // HDE_8
			heb |= ((hblnk_s & 0x100) >> (8 - 2)); // HBS_8
			heb |= ((hsync_s & 0x100) >> (8 - 3)); // HRS_8
			heb |= ((linecomp & 0x100) >> (8 - 4)); // ILC_8
			vgaIO.setCRT(NVCRTCX_HEB, heb);

			/* (mostly) vertical extended regs */
			int lsr = vgaIO.getCRT(NVCRTCX_LSR) & 0xc0;
			lsr |= ((vtotal & 0x400) >> (10 - 0)); // VDT_10
			lsr |= ((vdisp_e & 0x400) >> (10 - 1)); // VDE_10
			lsr |= ((vsync_s & 0x400) >> (10 - 2)); // VRS_10
			lsr |= ((vblnk_s & 0x400) >> (10 - 3)); // VRBS_10
			lsr |= ((hblnk_e & 0x040) >> (6 - 4)); // HBE_6
			vgaIO.setCRT(NVCRTCX_LSR, lsr);

			// extra
			int ebr = 0;
			ebr |= ((vtotal & 0x800) >> (11 - 0)); // VDT_11
			ebr |= ((vdisp_e & 0x800) >> (11 - 2)); // VDE_11
			ebr |= ((vsync_s & 0x800) >> (11 - 4)); // VRS_11
			ebr |= ((vblnk_s & 0x800) >> (11 - 8)); // VBS_11
			vgaIO.setCRT(NVCRTCX_EBR, ebr);

			/* setup 'large screen' mode */
			final int repaint1 = vgaIO.getCRT(NVCRTCX_REPAINT1);
			if (mode.getWidth() >= 1280) {
				vgaIO.setCRT(NVCRTCX_REPAINT1, (repaint1 & 0xfb));
			} else {
				vgaIO.setCRT(NVCRTCX_REPAINT1, (repaint1 | 0x04));
			}

			/* setup HSYNC & VSYNC polarity */
			/*
			 * LOG(2, ("CRTC: sync polarity: ")); int temp = vgaIO.getReg8(NV8_MISCR); if (target.timing.flags & B_POSITIVE_HSYNC) { LOG(2, ("H:pos ")); temp &= ~0x40; } else { LOG(2, ("H:neg "));
			 * temp |= 0x40; } if (target.timing.flags & B_POSITIVE_VSYNC) { LOG(2, ("V:pos ")); temp &= ~0x80; } else { LOG(2, ("V:neg ")); temp |= 0x80; }
			 */
		}
	}

	/**
	 * Start a DDC1 readout
	 */
	public void setupDDC1() {
		// Enable access to extended registers
		//vgaIO.unlock();
		vgaIO.setCRT(NVCRTCX_LOCK, 0x57);
	}

	/**
	 * Sets the start address of the video memory
	 * 
	 * @param startadd
	 */
	private void setVideoStartAddress(int startadd) {
		if (architecture < NV10A) {
			/* upto 32Mb RAM adressing: must be used this way on pre-NV10! */

			/* set standard registers */
			/* (NVidia: startadress in 32bit words (b2 - b17) */
			vgaIO.setCRT(NVCRTCX_FBSTADDL, (startadd >> 2) & 0xFF);
			vgaIO.setCRT(NVCRTCX_FBSTADDH, (startadd >> 10) & 0xFF);

			/* set extended registers */
			/* NV4 extended bits: (b18-22) */
			int temp = vgaIO.getCRT(NVCRTCX_REPAINT0) & 0xE0;
			vgaIO.setCRT(NVCRTCX_REPAINT0, temp | ((startadd >> 18) & 0x1f));
			/* NV4 extended bits: (b23-24) */
			temp = vgaIO.getCRT(NVCRTCX_HEB) & 0xdf;
			vgaIO.setCRT(NVCRTCX_HEB, temp | ((startadd >> 18) & 0x20));
			//temp = (CRTCR(HEB) & 0x9f);
			//CRTCW(HEB, (temp | ((startadd & 0x01800000) >> 18)));
		} else {
			/* upto 4Gb RAM adressing: must be used on NV10 and later! */
			/*
			 * NOTE: While this register also exists on pre-NV10 cards, it will wrap-around at 16Mb boundaries!!
			 */

			/* 30bit adress in 32bit words */
			vgaIO.setReg32(NV32_NV10FBSTADD32, startadd & 0xfffffffc);
		}

		int temp = vgaIO.getATT(NVATBX_HORPIXPAN) & 0xf9;
		vgaIO.setATT(NVATBX_HORPIXPAN, temp | ((startadd & 3) << 1));
	}

    /**
     * @see org.jnode.driver.video.Surface#drawAlphaRaster(java.awt.image.Raster, java.awt.geom.AffineTransform, int, int, int, int, int, int, java.awt.Color)
     */
    public void drawAlphaRaster(Raster raster, AffineTransform tx, int srcX, int srcY, int dstX,
            int dstY, int width, int height, Color color) {
        bitmapGraphics.drawAlphaRaster(raster, tx, srcX, srcY, dstX, dstY, width, height, convertColor(color));
    }
}
