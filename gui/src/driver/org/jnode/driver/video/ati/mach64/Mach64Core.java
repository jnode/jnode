/*
 * $Id$
 */
package org.jnode.driver.video.ati.mach64;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.driver.pci.PCIBaseAddress;
import org.jnode.driver.pci.PCIConstants;
import org.jnode.driver.pci.PCIDevice;
import org.jnode.driver.pci.PCIDeviceConfig;
import org.jnode.driver.video.HardwareCursorAPI;
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
final class Mach64Core implements Mach64Constants  {

	/** My logger */
	private final Logger log = Logger.getLogger(getClass());

	/** The driver we belong to */
	private final Mach64Driver driver;
	
	/** Framebuffer memory */
	private final MemoryResource deviceRam;
	
	/** Memory mapped I/O region, block 0*/
	private final MemoryResource mmio0;
	
	/** I/O utility */
	private final Mach64VgaIO vgaIO;
	
	/**
	 * Initialize this instance.
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
		
		final PCIBaseAddress fbAddr = pciCfg.getBaseAddresses()[0];

		log.info("Found ATI " + model + ", pci " + pciCfg);
		
		try {
			final ResourceManager rm = (ResourceManager) InitialNaming
			.lookup(ResourceManager.NAME);
			
			final int fbBase = (int) fbAddr.getMemoryBase() /* & 0xFF800000 */;
			final int memSize = fbAddr.getSize();
			log.info("Memory size " + NumberUtils.size(memSize));

			// Map Device RAM
			this.deviceRam = rm.claimMemoryResource(device, Address
					.fromIntZeroExtend(fbBase), memSize, ResourceManager.MEMMODE_NORMAL);
			
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
			
			log.debug("Found ATI " + model + ", FB at 0x"
					+ NumberUtils.hex(fbBase) + "s0x"
					+ NumberUtils.hex(memSize));

		} catch (NameNotFoundException ex) {
			throw new ResourceNotFreeException(ex);
		}

		
	}
	
	/**
	 * Open the given configuration.
	 * @param config
	 * @return
	 * @throws ResourceNotFreeException
	 */
	final Mach64Surface open(Mach64Configuration config) 
	throws ResourceNotFreeException {
		return null;
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
}
