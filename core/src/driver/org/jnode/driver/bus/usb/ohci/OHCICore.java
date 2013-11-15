package org.jnode.driver.bus.usb.ohci;

import javax.naming.NameNotFoundException;

import org.apache.log4j.Logger;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.driver.bus.usb.USBControlPipe;
import org.jnode.driver.bus.usb.USBDevice;
import org.jnode.driver.bus.usb.USBEndPoint;
import org.jnode.driver.bus.usb.USBHostControllerAPI;
import org.jnode.driver.bus.usb.USBHubAPI;
import org.jnode.driver.bus.usb.USBPipe;
import org.jnode.driver.bus.usb.uhci.UHCICore;
import org.jnode.naming.InitialNaming;
import org.jnode.system.resource.ResourceManager;
import org.jnode.system.resource.ResourceNotFreeException;
import org.jnode.util.NumberUtils;

public class OHCICore implements USBHostControllerAPI {

	/**
     * My logger
     */
    private static final Logger log = Logger.getLogger(UHCICore.class);
    
    /**
     * The pci device
     */
    private final PCIDevice device;
    
    /**
     * System wide resource manager
     */
    private final ResourceManager rm;
    
    /**
     * Default ctor
     * @param device
     */
    OHCICore(PCIDevice device) throws ResourceNotFreeException, DriverException {
    	this.device = device;
        final PCIDeviceConfig cfg = device.getConfig();
        final PCIBaseAddress baseAddr = getBaseAddress(cfg);
        try {
            this.rm = InitialNaming.lookup(ResourceManager.NAME);
            final int ioBase = baseAddr.getIOBase();
            final int ioSize = baseAddr.getSize();
            log.info("Found OHCI at 0x" + NumberUtils.hex(ioBase));

            /*this.io = new UHCIIO(claimPorts(rm, device, ioBase, ioSize));
            this.bus = new USBBus(device, this);
            this.rootHub = new UHCIRootHub(io, bus);
            final Schedule schedule = new Schedule(rm);
            this.pipeMgr = new UHCIPipeManager(rm, schedule);

            final int irqNr = cfg.asHeaderType0().getInterruptLine() & 0xF;
            // Workaround for some VIA chips
            cfg.asHeaderType0().setInterruptLine(irqNr);
            this.irq = rm.claimIRQ(device, irqNr, this, true);
            log.debug("Using IRQ " + irqNr);

            // Reset the HC
            resetHC();

            // Set the enabled interrupts
            io.setInterruptEnable(0x000F);
            // Set the framelist pointer
            io.setFrameListBaseAddress(schedule.getFrameList().getDescriptorAddress());
            // Go!
            setRun(true);*/
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException(ex);
        }
    }

    /**
     * Release all resources
     */
    public void release() {
        // Disable both ports
        /*final int max = rootHub.getNumPorts();
        for (int i = 0; i < max; i++) {
            rootHub.setPortEnabled(i, false);
        }
        // Stop the HC
        setRun(false);
        irq.release();
        io.release();*/
    }

    /**
     * Get the IO base address from the given PCI config
     *
     * @param cfg
     * @return @throws
     *         DriverException
     */
    private PCIBaseAddress getBaseAddress(PCIDeviceConfig cfg) throws DriverException {
        final PCIBaseAddress[] addresses = cfg.asHeaderType0().getBaseAddresses();
        final PCIBaseAddress a = addresses[0];
        if (a != null)
        	return a;
        throw new DriverException("No base address found");
    }

	@Override
	public USBControlPipe createDefaultControlPipe(USBDevice device) {
		return null;
	}

	@Override
	public USBPipe createPipe(USBEndPoint endPoint) {
		return null;
	}

	@Override
	public USBHubAPI getRootHUB() {
		return null;
	}


}
