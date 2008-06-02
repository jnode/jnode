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

package org.jnode.driver.bus.ide;

import java.security.PrivilegedExceptionAction;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIBaseAddress;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.pci.PCIDeviceConfig;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.TimeUtils;
import org.jnode.util.TimeoutException;


/**
 * IDE IO-port accessor.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DefaultIDEIO implements IDEIO {

    /**
     * My logger
     */
    private static final Logger log = Logger.getLogger(DefaultIDEIO.class);

    /**
     * IDE Taskfile io space
     */
    private IOResource cmdBlock;

    /**
     * IDE High taskfile io space
     */
    private IOResource ctrlBlock;

    /**
     * First port address of command block
     */
    private final int cmdBlockStart;

    /**
     * First port address of control block
     */
    private final int ctrlBlockStart;

    /**
     * Port number of AltStatus register
     */
    private final int altStatusPort;

    /**
     * IRQ number to respond to
     */
    private final int irq;

    /**
     * Create a new instance
     */
    protected DefaultIDEIO(Device device, boolean primary)
        throws IllegalArgumentException, DriverException, ResourceNotFreeException {

        int cmdBlockStart = (primary ? IDE0_START_PORT : IDE1_START_PORT);
        int ctrlBlockStart = cmdBlockStart + HIGH_OFFSET;
        int cmdBlockSize = IDE_NR_PORTS;
        int ctrlBlockSize = IDE_NR_HIGH_PORTS;
        int altStatusPort = ctrlBlockStart + R8_ALTSTATUS_OFFSET;
        int irq = (primary ? IDE0_IRQ : IDE1_IRQ);
        boolean nativeMode = false;

        // Detect PCI IDE Controller, look for enhanced mode
        if (device instanceof PCIDevice) {
            final PCIDevice pciDev = (PCIDevice) device;
            final PCIDeviceConfig pciCfg = pciDev.getConfig();
            final int pIntf = pciCfg.getMinorClass();
            final int progMask = 0x02 | 0x08;
            final int enhModeMask = 0x01 | 0x04;
            if ((pIntf & progMask) == progMask) {
                // Mode is programmable, set enhanced mode
                //pciCfg.setMinorClass(pIntf | enhModeMask);                
            }
            if ((pciCfg.getMinorClass() & enhModeMask) == enhModeMask) {
                // Use enhanced mode
                final PCIBaseAddress[] baseAddrs = pciCfg.asHeaderType0().getBaseAddresses();
                final int idx = (primary ? 0 : 2);
                cmdBlockStart = baseAddrs[idx].getIOBase();
                cmdBlockSize = 8;
                ctrlBlockStart = baseAddrs[idx + 1].getIOBase();
                ctrlBlockSize = 4;
                altStatusPort = ctrlBlockStart + 0x02;
                irq = pciCfg.asHeaderType0().getInterruptLine();
                nativeMode = true;
            }
        }

        log.info("Using PCI IDE " + (nativeMode ? "Native" : "Compatibility") + " mode [irq=" + irq + "]");

        // Now claim the resources
        IOResource cmdBlock = null;
        IOResource ctrlBlock = null;
        final ResourceManager rm;
        try {
            rm = InitialNaming.lookup(ResourceManager.NAME);
            cmdBlock = claimPorts(rm, device, cmdBlockStart, cmdBlockSize);
            ctrlBlock = claimPorts(rm, device, ctrlBlockStart, ctrlBlockSize);
        } catch (NameNotFoundException ex) {
            throw new ResourceNotFreeException("Cannot find ResourceManager",
                ex);
        } catch (ResourceNotFreeException ex) {
            if (cmdBlock != null) {
                cmdBlock.release();
            }
            if (ctrlBlock != null) {
                ctrlBlock.release();
            }
            throw ex;
        }
        this.irq = irq;
        this.cmdBlockStart = cmdBlockStart;
        this.ctrlBlockStart = ctrlBlockStart;
        this.cmdBlock = cmdBlock;
        this.ctrlBlock = ctrlBlock;
        this.altStatusPort = altStatusPort;
    }

    /**
     * Stop this processor.
     */
    public void release() {
        cmdBlock.release();
        ctrlBlock.release();
    }

    /**
     * Gets a word from the data register
     *
     * @return a word from the data register
     */
    public final int getDataReg() {
        return cmdBlock.inPortWord(cmdBlockStart + RW16_DATA_OFFSET);
    }

    /**
     * Writes a word to the data register
     *
     * @param dataWord
     */
    public final void setDataReg(int dataWord) {
        cmdBlock.outPortWord(cmdBlockStart + RW16_DATA_OFFSET, dataWord);
    }

    /**
     * Gets the contents of the error register
     *
     * @return the contents of the error register
     */
    public final int getErrorReg() {
        return cmdBlock.inPortByte(cmdBlockStart + R8_ERROR_OFFSET);
    }

    /**
     * Sets the contents of the featureregister
     *
     * @param features
     */
    public final void setFeatureReg(int features) {
        cmdBlock.outPortByte(cmdBlockStart + W8_FEATURE_OFFSET, features);
    }

    /**
     * Gets the contents of the sector count register
     *
     * @return the contents of the sector count register
     */
    public final int getSectorCountReg() {
        return cmdBlock.inPortByte(cmdBlockStart + RW8_SECTOR_COUNT_OFFSET);
    }

    /**
     * Sets the sector count register
     *
     * @param sectorCount
     */
    public final void setSectorCountReg(int sectorCount) {
        cmdBlock.outPortByte(cmdBlockStart + RW8_SECTOR_COUNT_OFFSET, sectorCount);
    }

    /**
     * Gets the contents of the sector register
     *
     * @return the contents of the sector register
     */
    public final int getSectorReg() {
        return cmdBlock.inPortByte(cmdBlockStart + RW8_SECTOR_OFFSET);
    }

    /**
     * Gets the contents of the LBA low register
     *
     * @return the contents of the LBA low register
     */
    public final int getLbaLowReg() {
        return cmdBlock.inPortByte(cmdBlockStart + RW8_LBA_LOW_OFFSET);
    }

    /**
     * Gets the contents of the LBA mid register
     *
     * @return the contents of the LBA mid register
     */
    public final int getLbaMidReg() {
        return cmdBlock.inPortByte(cmdBlockStart + RW8_LBA_MID_OFFSET);
    }

    /**
     * Gets the contents of the LBA high register
     *
     * @return the contents of the LBA high register
     */
    public final int getLbaHighReg() {
        return cmdBlock.inPortByte(cmdBlockStart + RW8_LBA_HIGH_OFFSET);
    }

    /**
     * Sets the contents of the LBA low register
     *
     * @param value
     */
    public final void setLbaLowReg(int value) {
        cmdBlock.outPortByte(cmdBlockStart + RW8_LBA_LOW_OFFSET, value);
    }

    /**
     * Sets the contents of the LBA mid register
     *
     * @param value
     */
    public final void setLbaMidReg(int value) {
        cmdBlock.outPortByte(cmdBlockStart + RW8_LBA_MID_OFFSET, value);
    }

    /**
     * Sets the contents of the LBA high register
     *
     * @param value
     */
    public final void setLbaHighReg(int value) {
        cmdBlock.outPortByte(cmdBlockStart + RW8_LBA_HIGH_OFFSET, value);
    }

    /**
     * Sets the sector register
     *
     * @param sector
     */
    protected final void setSectorReg(int sector) {
        cmdBlock.outPortByte(cmdBlockStart + RW8_SECTOR_OFFSET, sector);
    }

    /**
     * Gets the combined cylinder value out the the cylinder LSB and MSB
     * registers.
     */
    protected final int getCylinderRegs() {
        final int lsb = cmdBlock.inPortByte(cmdBlockStart + RW8_CYLINDER_LSB_OFFSET);
        final int msb = cmdBlock.inPortByte(cmdBlockStart + RW8_CYLINDER_MSB_OFFSET);
        return ((msb & 0xFF) << 8) | (lsb & 0xFF);
    }

    /**
     * Sets the cylinder registers value (both LSB and MSB)
     */
    protected final void setCylinderRegs(int cylinder) {
        final int lsb = cylinder & 0xFF;
        final int msb = (cylinder >> 8) & 0xFF;
        cmdBlock.outPortByte(cmdBlockStart + RW8_CYLINDER_LSB_OFFSET, lsb);
        cmdBlock.outPortByte(cmdBlockStart + RW8_CYLINDER_MSB_OFFSET, msb);
    }

    /**
     * Gets the contents of the select register
     *
     * @return the contents of the select register
     */
    public final int getSelectReg() {
        return cmdBlock.inPortByte(cmdBlockStart + RW8_SELECT_OFFSET);
    }

    /**
     * Sets the select register
     *
     * @param select
     */
    public final void setSelectReg(int select) {
        cmdBlock.outPortByte(cmdBlockStart + RW8_SELECT_OFFSET, select);
    }

    /**
     * Gets the status of the IDE controller. Any pending IRQ is reset.
     *
     * @return the status of the IDE controller. Any pending IRQ is reset.
     */
    public final int getStatusReg() {
        return cmdBlock.inPortByte(cmdBlockStart + R8_STATUS_OFFSET);
    }

    /**
     * Gets the alternative status of the IDE controller. Any pending IRQ is
     * NOT reset.
     *
     * @return the alternative status of the IDE controller
     */
    public final int getAltStatusReg() {
        return ctrlBlock.inPortByte(altStatusPort);
    }

    /**
     * Sets the command register. This also activates the IDE controller so
     * always set other registers first.
     *
     * @param command
     */
    public final void setCommandReg(int command) {
        cmdBlock.outPortByte(cmdBlockStart + W8_COMMAND_OFFSET, command);
    }

    /**
     * Sets the control register.
     *
     * @param control The new value for the control register
     */
    public final void setControlReg(int control) {
        ctrlBlock.outPortByte(ctrlBlockStart + W8_CONTROL_OFFSET, control);
    }

    /**
     * Is this channel busy.
     *
     * @return if this channel is busy
     */
    public final boolean isBusy() {
        return ((getAltStatusReg() & ST_BUSY) == ST_BUSY);
    }

    /**
     * Block the current thread until the controller is not busy anymore.
     *
     * @param timeout
     * @throws TimeoutException
     */
    public final void waitUntilNotBusy(long timeout) throws TimeoutException {
        while (isBusy()) {
            if (timeout <= 0) {
                throw new TimeoutException("Timeout in waitUntilNotBusy");
            }
            TimeUtils.sleep(10);
            timeout -= 10;
        }
    }

    private IOResource claimPorts(final ResourceManager rm,
                                  final ResourceOwner owner, final int low, final int length)
        throws ResourceNotFreeException, DriverException {
        try {
            return (IOResource) AccessControllerUtils
                .doPrivileged(new PrivilegedExceptionAction() {

                    public Object run() throws ResourceNotFreeException {
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
     * @return Returns the irq.
     */
    public final int getIrq() {
        return this.irq;
    }
}
