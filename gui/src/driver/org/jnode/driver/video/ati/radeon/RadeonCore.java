/*
 * $Id$
 */
package org.jnode.driver.video.ati.radeon;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
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
    private final int architecture;
    private final MemoryResource mmio;
    private final MemoryResource videoRam;
    private final RadeonVgaIO vgaIO;
    private final int memSize;
    
	/**
	 * @param driver
	 * @param architecture
	 * @param model
	 * @param device
	 */
	public RadeonCore(RadeonDriver driver, int architecture, String model, PCIDevice device) throws ResourceNotFreeException, DriverException {
		super(640, 480);
		this.driver = driver;
		this.architecture = architecture;
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
			this.memSize = getMemorySize();

			this.videoRam = rm.claimMemoryResource(device, Address.valueOf(fbBase), memSize, ResourceManager.MEMMODE_NORMAL);
			log.debug("Found ATI " + model + ", FB at 0x" + NumberUtils.hex(fbBase) + "s0x" + NumberUtils.hex(memSize) + ", MMIO at 0x" + NumberUtils.hex(ioBase));

		} catch (NameNotFoundException ex) {
			throw new ResourceNotFreeException(ex);
		}
		//this.hwCursor = new NVidiaHardwareCursor(vgaIO, architecture);
		//this.acc = new NVidiaAcceleration(vgaIO, architecture);
		
		log.info("Memory size " + (memSize / (1024*1024)) + "MB");
	}

    /**
     * @see org.jnode.driver.video.util.AbstractSurface#convertColor(java.awt.Color)
     */
    protected int convertColor(Color color) {
        // TODO Auto-generated method stub
        return 0;
    }
    
    /**
     * @see org.jnode.driver.video.util.AbstractSurface#drawPixel(int, int, int, int)
     */
    protected void drawPixel(int x, int y, int color, int mode) {
        // TODO Auto-generated method stub

    }
    
    /**
     * @see org.jnode.driver.video.Surface#drawCompatibleRaster(java.awt.image.Raster, int, int, int, int, int, int, java.awt.Color)
     */
    public void drawCompatibleRaster(Raster raster, int srcX, int srcY,
            int dstX, int dstY, int width, int height, Color bgColor) {
        // TODO Auto-generated method stub

    }
    
    /**
     * @see org.jnode.driver.video.Surface#getColorModel()
     */
    public ColorModel getColorModel() {
        // TODO Auto-generated method stub
        return null;
    }
    
	/**
	 * Open the given configuration
	 * 
	 * @param config
	 */
	final void open(RadeonConfiguration config) {
		this.config = config;
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
    private final int getMemorySize() {
        return vgaIO.getReg32(CONFIG_MEMSIZE) & CONFIG_MEMSIZE_MASK;
    }
}
