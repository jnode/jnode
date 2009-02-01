/*
 * $Id$
 *
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
 
package org.jnode.driver.net.lance;

import java.security.PrivilegedExceptionAction;

import javax.naming.NameNotFoundException;

import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIConstants;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIHeaderType0;
import org.jnode.driver.net.NetworkException;
import org.jnode.driver.net.ethernet.spi.Flags;
import org.jnode.driver.net.spi.AbstractDeviceCore;
import org.jnode.naming.InitialNaming;
import org.jnode.net.HardwareAddress;
import org.jnode.net.SocketBuffer;
import org.jnode.net.ethernet.EthernetAddress;
import org.jnode.net.ethernet.EthernetConstants;
import org.jnode.system.IOResource;
import org.jnode.system.IRQHandler;
import org.jnode.system.IRQResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;
import org.jnode.util.TimeoutException;

/**
 * This is the DeviceCore for LANCE and PCnet 32 compatable ethernet PCI drivers.
 * <p/>
 * The current implementation was specificaly designed for the AMD PCnet-PCI II
 * Ethernet Controller (Am79C970A), but should work for other AMD PCnet PCI devices.
 * The driver is based on information in the following specification from AMD.
 * http://www.amd.com/files/connectivitysolutions/networking/archivednetworking/19436.pdf
 * <p/>
 * Specificaly the following assumptions were made:
 * - Device supports Software Style 2 (PCnet-PCI) which defines the layout of the initialaztion
 * block and the descriptor rings.
 * <p/>
 * Note: It should be easy to expand this driver to remove these assuptions.
 *
 * @author Chirs Cole
 */
public class LanceCore extends AbstractDeviceCore implements IRQHandler, LanceConstants,
        EthernetConstants {

    // This is the number of descriptors for the receive and transmit rings
    // Note: Valid numbers are 2^x where x is 0..8 (1, 2, 4, 8, 16, .., 512)
    private static final int RX_DESCRIPTOR_LENGTH = 4;
    private static final int TX_DESCRIPTOR_LENGTH = 4;

    /**
     * Device Driver
     */
    private final LanceDriver driver;
    
    /**
     * Start of IO address space
     */
    private final int iobase;
    
    /**
     * IO address space resource
     */
    private final IOResource ioResource;
    private final IOAccess io;
    
    /**
     * IRQ resource
     */
    private final IRQResource irq;
    
    /**
     * My ethernet address
     */
    private EthernetAddress hwAddress;
    
    /**
     * Flags for the specific device found
     */
    private final LanceFlags flags;
    
    /**
     * Manager for receive and transmit rings as well as data buffers
     */
    private final BufferManager bufferManager;

    /**
     * Create a new instance and allocate all resources
     *
     * @throws ResourceNotFreeException
     */
    public LanceCore(LanceDriver driver, ResourceOwner owner, PCIDevice device, Flags flags)
        throws ResourceNotFreeException, DriverException {
        this.driver = driver;
        this.flags = (LanceFlags) flags;

        final PCIHeaderType0 config = device.getConfig().asHeaderType0();
        final int irq = config.getInterruptLine();

        final PCIBaseAddress[] addrs = config.getBaseAddresses();
        if (addrs.length < 1) {
            throw new DriverException("Cannot find iobase: not base addresses");
        }
        if (!addrs[0].isIOSpace()) {
            throw new DriverException("Cannot find iobase: first address is not I/O");
        }

        // Get the start of the IO address space
        iobase = addrs[0].getIOBase();
        final int iolength = addrs[0].getSize();
        log.debug("Found Lance IOBase: 0x" + NumberUtils.hex(iobase) + ", length: " + iolength);
        ResourceManager rm;
        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
        } catch (NameNotFoundException ex) {
            throw new DriverException("Cannot find ResourceManager");
        }
        this.irq = rm.claimIRQ(owner, irq, this, true);
        try {
            ioResource = claimPorts(rm, owner, iobase, iolength);
        } catch (ResourceNotFreeException ex) {
            this.irq.release();
            throw ex;
        }

        // Determine the type of IO access (Word or DWord)
        io = getIOAccess();
        log.debug("IO Access set to " + io.getType());

        // Set the flags based on the version of the device found
        setFlags();

        // Load the hw address
        this.hwAddress = loadHWAddress();

        log.info("Found " + this.flags.getChipName() + " at 0x" + NumberUtils.hex(iobase, 4) +
                " with MAC Address " + hwAddress);

        // Create rx & tx descriptor rings, initdata and databuffers
        this.bufferManager =
                new BufferManager(RX_DESCRIPTOR_LENGTH, TX_DESCRIPTOR_LENGTH,
                        CSR15_DRX | CSR15_DTX, hwAddress, 0, rm, owner);

        // Enable device to become a bus master on the PCI bus.
        config.setCommand(config.getCommand() | PCIConstants.PCI_COMMAND_MASTER);
    }

    private IOAccess getIOAccess() {
        // reset
        ioResource.inPortWord(iobase + WIO_RESET);
        ioResource.outPortWord(iobase + WIO_RAP, 0);
        if (ioResource.inPortWord(iobase + WIO_RDP) == 4) {
            ioResource.outPortWord(iobase + WIO_RAP, 88);
            if (ioResource.inPortWord(iobase + WIO_RAP) == 88) {
                return new WordIOAccess(ioResource, iobase);
            }
        }

        ioResource.inPortDword(iobase + DWIO_RESET);
        ioResource.outPortDword(iobase + DWIO_RAP, 0);
        if (ioResource.inPortDword(iobase + DWIO_RDP) == 4) {
            ioResource.outPortDword(iobase + DWIO_RAP, 88);
            if ((ioResource.inPortDword(iobase + DWIO_RAP) & 0xFFFF) == 88) {
                return new DWordIOAccess(ioResource, iobase);
            }
        }
        return null;
    }

    /**
     * Initialize this device.
     */
    public void initialize() {
        // reset the chip
        io.reset();

        // Set the Software Style to mode 2 (PCnet-PCI)
        // Note: this may not be compatable with older lance controllers (non PCnet)
        io.setBCR(20, 2);

        // TODO the device should be setup based on the flags for the chip version
        // Auto select port
        io.setBCR(2, BCR2_ASEL);
        // Enable full duplex
        io.setBCR(9, BCR9_FDEN);
        io.setCSR(4, CSR4_DMAPLUS | CSR4_APAD_XMT);
        io.setCSR(5, CSR5_LTINTEN | CSR5_SINTE | CSR5_SLPINTE | CSR5_EXDINTE | CSR5_MPINTE);

        // Set the address of the Initialization Block
        final int iaddr = bufferManager.getInitDataAddressAs32Bit();
        io.setCSR(1, iaddr & 0xFFFF);
        io.setCSR(2, (iaddr >> 16) & 0xFFFF);

        // Initialize the device with the Initialization Block and enable interrupts
        io.setCSR(0, CSR0_INIT | CSR0_IENA);
    }

    /**
     * Disable this device
     */
    public void disable() {
        io.reset();
        io.setCSR(0, CSR0_STOP);
    }

    /**
     * Release all resources
     */
    public void release() {
        ioResource.release();
        irq.release();
    }

    /**
     * Gets the hardware address of this card.
     */
    public HardwareAddress getHwAddress() {
        return hwAddress;
    }

    /**
     * Read the hardware address
     */
    private final EthernetAddress loadHWAddress() {
        final byte[] addr = new byte[ETH_ALEN];
        for (int i = 0; i < addr.length; i++) {
            addr[i] = (byte) ioResource.inPortByte(iobase + R_ETH_ADDR_OFFSET + i);
        }
        return new EthernetAddress(addr, 0);
    }

    private final void setFlags() {
        int chipVersion = io.getCSR(88) | (io.getCSR(89) << 16);
        chipVersion = (chipVersion >> 12) & 0xffff;

        flags.setForVersion(chipVersion);
    }

    /**
     * Transmit the given buffer
     *
     * @param buf
     * @param timeout
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public synchronized void transmit(SocketBuffer buf, HardwareAddress destination, long timeout)
        throws InterruptedException, TimeoutException {
        // Set the source address
        hwAddress.writeTo(buf, 6);

        // debug dump for investigating VWWare 3 network problems
        // dumpDebugInfo();

        // ask buffer manager to send out the data
        bufferManager.transmit(buf);

        // force the device to poll current transmit descriptor for new data
        io.setCSR(0, io.getCSR(0) | CSR0_TDMD);
    }

    /**
     * @see org.jnode.system.IRQHandler#handleInterrupt(int)
     */
    public void handleInterrupt(int irq) {
        while ((io.getCSR(0) & CSR0_INTR) != 0) {
            final int csr0 = io.getCSR(0);
            final int csr4 = io.getCSR(4);
            final int csr5 = io.getCSR(5);

            io.setCSR(0, csr0);
            io.setCSR(4, csr4);
            io.setCSR(5, csr5);

            // check if interrupt is due to Initialization Done
            if ((csr0 & CSR0_IDON) != 0) {
                log.info(flags.getChipName() + " Initialization Complete");

                // Now enable RX/TX
                io.setCSR(15, 0);

                // assert the Start and clear Initialization Done (IDON) flag
                // Note: there are reported errors due to setting IDON here but I have not seen any
                io.setCSR(0, CSR0_STRT | CSR0_IENA | CSR0_IDON);
            }

            // check if interrupt is due to Transmition Interrupt
            if ((csr0 & CSR0_TINT) != 0) {
                //log.debug("Transmition Interrupt");
            }

            // check if interrupt is due to Receive Interrupt
            if ((csr0 & CSR0_RINT) != 0) {
                //log.debug("Receive Interrupt");
                rxProcess();
            }

            // check if interrupt is due an error
            if ((csr0 & CSR0_ERR) != 0) {
                log.debug("Error Interrupt");

                // check if interrupt is due to Memory Error
                if ((csr0 & CSR0_MERR) != 0) {
                    log.debug("Memory Error");
                }

                // check if interrupt is due to Missed Frame
                if ((csr0 & CSR0_MISS) != 0) {
                    log.debug("Missed Frame");
                }

                // check if interrupt is due to Collision Error
                if ((csr0 & CSR0_CERR) != 0) {
                    log.debug("Collision Error");
                }

                // check if interrupt is due to a Bable transmitter time-out
                if ((csr0 & CSR0_BABL) != 0) {
                    log.debug("Bable transmitter time-out");
                }
            }

            // check if interrupt is due to a Missed Frame Counter Overflow
            if ((csr4 & CSR4_MFCO) == CSR4_MFCO) {
                log.debug("Missed Frame Counter Overflow");
            }

            // check if interrupt is due to a User Interrupt
            if ((csr4 & CSR4_UINT) == CSR4_UINT) {
                log.debug("User Interrupt");
            }

            // check if interrupt is due to a Receive Collision Counter Overflow
            if ((csr4 & CSR4_RCVCCO) == CSR4_RCVCCO) {
                log.debug("Receive Collision Counter Overflow");
            }

            // check if interrupt is due to a Transmit Start
            if ((csr4 & CSR4_TXSTRT) == CSR4_TXSTRT) {
                log.debug("Transmit Start");
            }

            // check if interrupt is due to a Jabber Error
            if ((csr4 & CSR4_JAB) == CSR4_JAB) {
                log.debug("Jabber Error");
            }

            // check if interrupt is due to a Jabber Error
            if ((csr4 & CSR4_JAB) == CSR4_JAB) {
                log.debug("Jabber Error");
            }

            // check if interrupt is due to a System Interrupt
            if ((csr5 & CSR5_SINT) == CSR5_SINT) {
                log.debug("System Interrupt");
            }

            // check if interrupt is due to a Sleep Interrupt
            if ((csr5 & CSR5_SLPINT) == CSR5_SLPINT) {
                log.debug("Sleep Interrupt");
            }

            // check if interrupt is due to a Excessive Deferral Interrupt
            if ((csr5 & CSR5_EXDINT) == CSR5_EXDINT) {
                log.debug("Excessive Deferral Interrupt");
            }

            // check if interrupt is due to a Magic Packet Interrupt
            if ((csr5 & CSR5_MPINT) == CSR5_MPINT) {
                log.debug("Magic Packet Interrupt");
            }

        }
    }

    private void rxProcess() {
        SocketBuffer skbuf;

        while ((skbuf = bufferManager.getPacket()) != null) {
            try {
                if (skbuf != null)
                    driver.onReceive(skbuf);
            } catch (NetworkException e) {
                // FIXME
                e.printStackTrace();
            } finally {
                // FIXME
            }
        }
    }

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner,
            final int low, final int length) throws ResourceNotFreeException, DriverException {
        try {
            return AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction<IOResource>() {
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

    final void dumpDebugInfo() {
        log.debug("Debug Dump");
        log.debug("CSR0 = " + io.getCSR(0));

        // stop the device so we can read all registers
        io.setCSR(0, CSR0_STOP);

        bufferManager.dumpData(log);

        int validVMWareLanceRegs[] = {
            0, 1, 2, 3, 4, 5, 8, 9, 10, 11, 12, 13, 14, 15, 24, 25, 30, 31, 58, 76, 77, 80,
            82, 88, 89, 112, 124};

        for (int i = 0; i < validVMWareLanceRegs.length; i++) {
            int csr_val = io.getCSR(validVMWareLanceRegs[i]);
            log.debug("CSR" + validVMWareLanceRegs[i] + " : " + NumberUtils.hex(csr_val, 4));
        }

        // try to start again, not sure if this works?
        io.setCSR(0, CSR0_STRT);

        /*
          for (int k = 0; k <= 22; k++) {
              int bcr_val = io.getBCR(k);
              log.debug("BCR" + k + " : " + NumberUtils.hex(bcr_val));
          }

                  for (int j = 0; j <= 0x3F; j++) {
              int pci_val = device.readConfigByte(j);
              log.debug(
                  "PCI" + NumberUtils.hex(j) + " : " + NumberUtils.hex(pci_val));
          }
         */
    }
}
