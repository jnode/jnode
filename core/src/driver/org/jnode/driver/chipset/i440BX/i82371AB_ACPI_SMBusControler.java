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

package org.jnode.driver.chipset.i440BX;

import java.security.PrivilegedExceptionAction;
import javax.naming.NameNotFoundException;
import org.apache.log4j.Logger;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.DriverException;
import org.jnode.driver.bus.pci.PCIDevice;
import org.jnode.driver.bus.smbus.DIMM;
import org.jnode.driver.bus.smbus.DIMMDriver;
import org.jnode.driver.bus.smbus.SMBus;
import org.jnode.driver.bus.smbus.SMBusControler;
import org.jnode.naming.InitialNaming;
import org.jnode.system.IOResource;
import org.jnode.system.ResourceManager;
import org.jnode.system.ResourceNotFreeException;
import org.jnode.system.ResourceOwner;
import org.jnode.util.AccessControllerUtils;
import org.jnode.util.NumberUtils;

/**
 * i82371AB_ACPI_SMBusControler.
 * <p/>
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public class i82371AB_ACPI_SMBusControler extends SMBusControler {

    private static final Logger log = Logger.getLogger(i82371AB_ACPI_SMBusControler.class);
    PCIDevice device;
    int hostStatusIORegister = 0;
    int slaveStatusIORegister = 0;
    int hostControlIORegister = 0;
    int hostCommandIORegister = 0;
    int hostAddressIORegister = 0;
    int hostData0IORegister = 0;
    int hostData1IORegister = 0;
    int blockDataIORegister = 0;
    int slaveControlIORegister = 0;
    int shadowCommandIORegister = 0;
    int slaveEventIORegister = 0;
    int slaveDataIORegister = 0;

    public static final byte CONTROL_START = 0x40; // bit 6

    public static final byte CONTROL_PROTOCOL_QUIKCOMMAND = 0x0;
    public static final byte CONTROL_PROTOCOL_READWRITE_BYTE = 0x8;
    public static final byte CONTROL_PROTOCOL_READWRITE_WORD = 0xC;
    public static final byte CONTROL_PROTOCOL_READWRITE_BLOCK = 0x14;
    public static final byte CONTROL_PROTOCOL_SENDRECEIVE_BYTE = 0x4;

    private static final byte ADDRESS_READ_TAG = 1;
    private static final byte ADDRESS_WRITE_TAG = 0;

    public static final byte CONTROL_INTERUPT_ENABLED = 1;
    public static final byte CONTROL_INTERUPT_DISABLED = 0;

    IOResource ioRes = null;

    public i82371AB_ACPI_SMBusControler(PCIDevice device) throws DriverException {
        this.device = device;
        // gets the IO registers address base from the PCI configuration register
        // see paragraph 7.3 in chipset specification
        int dword = device.readConfigWord(0x90);
        int base = dword & 0xFFF0; // see 7.1.127 in specification for clearing the last digit
        hostStatusIORegister = base++;
        slaveStatusIORegister = base++;
        hostControlIORegister = base++;
        hostCommandIORegister = base++;
        hostAddressIORegister = base++;
        hostData0IORegister = base++;
        hostData1IORegister = base++;
        blockDataIORegister = base++;
        slaveControlIORegister = base++;
        shadowCommandIORegister = base++;
        slaveEventIORegister = base++; // word size register
        slaveDataIORegister = ++base; // word size register

        // makes sure we generate IRQ9, not SMI and that the SMBus is enabled
        device.writeConfigByte(0xd2, 0x9);
        /*int config =*/
        device.readConfigByte(0xd2);

        try {
            final ResourceManager rm = InitialNaming.lookup(ResourceManager.NAME);
            try {
                ioRes = claimPorts(rm, device, hostStatusIORegister, 14);
            } catch (ResourceNotFreeException ex1) {
                //todo empty?
            }
        } catch (NameNotFoundException ex) {
            System.err.println("Cannot find ResourceManager: " + ex);
        }

    }

    public boolean sendByte(byte address, byte value)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException {
        ioRes.outPortByte(hostAddressIORegister, address | ADDRESS_WRITE_TAG);
        ioRes.outPortByte(hostData0IORegister, value);
        ioRes.outPortByte(hostCommandIORegister, value);
        ioRes.outPortByte(hostControlIORegister,
            CONTROL_START | CONTROL_PROTOCOL_READWRITE_BYTE | CONTROL_INTERUPT_DISABLED);
        byte status = statusWait();
        return status == 0;
    }

    public int processCall(byte address, byte command, int parameter)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("SMBus processCallI not supported by Intel 8273AB .");
    }

    public int readWord(byte smbaddress, byte reference)
        throws java.security.InvalidParameterException, java.io.IOException {
        reset();
        ioRes.outPortByte(hostAddressIORegister, smbaddress | ADDRESS_READ_TAG);
        ioRes.outPortByte(hostCommandIORegister, reference);
        ioRes.outPortByte(hostControlIORegister,
            CONTROL_START | CONTROL_PROTOCOL_READWRITE_WORD | CONTROL_INTERUPT_DISABLED);
        byte status = statusWait();
        if ((status & 0x10) > 0)
            throw new java.io.IOException("Failed SMBus readWord transaction on bus address " +
                NumberUtils.hex(smbaddress) + " and reference=" + NumberUtils.hex(reference));
        else if ((status & 0x08) > 0)
            throw new java.io.IOException(
                "SMBus collision for readWord transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        if ((status & 0x04) > 0)
            throw new java.io.IOException(
                "Device error for SMBus readWord transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        return (ioRes.inPortByte(hostData0IORegister)) & 0xff + ((ioRes.inPortByte(hostData1IORegister)) & 0xff << 8);
    }

    public byte receiveByte(byte address)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException {
        /** @todo Implement this org.jnode.driver.smbus.SMBusControler abstract method */
        throw new java.lang.UnsupportedOperationException("Method receiveByteImpl() not yet implemented.");
    }

    public boolean blockWrite(byte address, byte[] block)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException {
        /** @todo Implement this org.jnode.driver.smbus.SMBusControler abstract method */
        throw new java.lang.UnsupportedOperationException("Method blockWriteImpl() not yet implemented.");
    }

    public boolean quickCommand(byte address)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException {
        /** @todo Implement this org.jnode.driver.smbus.SMBusControler abstract method */
        throw new java.lang.UnsupportedOperationException("Method quickCommandImpl() not yet implemented.");
    }

    public byte[] blockRead(byte smbaddress, byte reference)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException,
        java.io.IOException {
        reset();
        ioRes.outPortByte(hostAddressIORegister, smbaddress | ADDRESS_READ_TAG);
        ioRes.outPortByte(hostCommandIORegister, reference);
        ioRes.outPortByte(hostControlIORegister,
            CONTROL_START | CONTROL_PROTOCOL_READWRITE_BLOCK | CONTROL_INTERUPT_DISABLED);
        byte status = statusWait();
        if ((status & 0x10) > 0)
            throw new java.io.IOException("Failed SMBus blockRead transaction on bus address " +
                NumberUtils.hex(smbaddress) + " and reference=" + NumberUtils.hex(reference));
        else if ((status & 0x08) > 0)
            throw new java.io.IOException(
                "SMBus collision for blockRead transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        if ((status & 0x04) > 0)
            throw new java.io.IOException(
                "Device error for SMBus blockRead transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        int size = ioRes.inPortByte(hostData0IORegister) & 0xff;
        byte[] res = new byte[size];
        ioRes.inPortByte(hostControlIORegister); // according to specification, reset chip internal
        // index to read block data
        for (int i = 0; i < size; i++) {
            res[i] = (byte) ioRes.inPortByte(blockDataIORegister);
        }
        return res;
    }

    public boolean writeWord(byte smbaddress, byte reference, int value)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException, java.io.IOException {
        reset();
        ioRes.outPortByte(hostAddressIORegister, smbaddress | ADDRESS_WRITE_TAG);
        ioRes.outPortByte(hostCommandIORegister, reference);
        ioRes.outPortByte(hostData0IORegister, value & 0xff);
        ioRes.outPortByte(hostData1IORegister, (value & 0xff00) >> 8);
        ioRes.outPortByte(hostControlIORegister,
            CONTROL_START | CONTROL_PROTOCOL_READWRITE_WORD | CONTROL_INTERUPT_DISABLED);
        byte status = statusWait();
        if ((status & 0x10) > 0)
            throw new java.io.IOException("Failed SMBus writeWord transaction on bus address " +
                NumberUtils.hex(smbaddress) + " and reference=" + NumberUtils.hex(reference));
        else if ((status & 0x08) > 0)
            throw new java.io.IOException(
                "SMBus collision for writeWord transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        if ((status & 0x04) > 0)
            throw new java.io.IOException(
                "Device error for SMBus writeWord transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        return true;
    }

    public boolean writeByte(byte smbaddress, byte reference, byte value)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException, java.io.IOException {
        reset();
        ioRes.outPortByte(hostAddressIORegister, smbaddress | ADDRESS_WRITE_TAG);
        ioRes.outPortByte(hostCommandIORegister, reference);
        ioRes.outPortByte(hostData0IORegister, value);
        ioRes.outPortByte(hostControlIORegister,
            CONTROL_START | CONTROL_PROTOCOL_READWRITE_BYTE | CONTROL_INTERUPT_DISABLED);
        byte status = statusWait();
        if ((status & 0x10) > 0)
            throw new java.io.IOException("Failed SMBus writeByte transaction on bus address " +
                NumberUtils.hex(smbaddress) + " and reference=" + NumberUtils.hex(reference));
        else if ((status & 0x08) > 0)
            throw new java.io.IOException(
                "SMBus collision for writeByte transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        if ((status & 0x04) > 0)
            throw new java.io.IOException(
                "Device error for SMBus writeByte transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        return true;
    }

    public boolean blockWriteProcessCall(byte address, byte[] inblock, byte[] outblock)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException {
        throw new java.lang.UnsupportedOperationException("SMBus processCall not supported by Intel 8273AB.");
    }

    public byte readByte(byte smbaddress, byte reference)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException {
        reset();
        ioRes.outPortByte(hostAddressIORegister, smbaddress | ADDRESS_READ_TAG);
        ioRes.outPortByte(hostCommandIORegister, reference);
        ioRes.outPortByte(hostControlIORegister,
            CONTROL_START | CONTROL_PROTOCOL_READWRITE_BYTE | CONTROL_INTERUPT_DISABLED);
        byte status = statusWait();
        if ((status & 0x10) > 0)
            throw new java.io.IOException("Failed SMBus readByte transaction on bus address " +
                NumberUtils.hex(smbaddress) + " and reference=" + NumberUtils.hex(reference));
        else if ((status & 0x08) > 0)
            throw new java.io.IOException(
                "SMBus collision for readByte transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        if ((status & 0x04) > 0)
            throw new java.io.IOException(
                "Device error for SMBus readByte transaction on bus address " + NumberUtils.hex(smbaddress) +
                    " and reference=" + NumberUtils.hex(reference));
        return (byte) ioRes.inPortByte(hostData0IORegister);
    }

    public void probeDevices(SMBus bus) {
        // this controler is a SMBus version 1.0, so there are no dynamic discovery.
        // use well known addresses and probe each address

        byte res = 0;

        // probes for the DIM

        for (byte i = 0; i < 8; i++) {
            try {
                byte address = (byte) (0xa0 | (i << 1));
                if (DIMMDriver.canExist(bus, address)) {
                    res = readByte((byte) (0xa0 | (i << 1)), (byte) 2);
                    log.debug("Discovered DIMM " + i + " type :" + Integer.toHexString(
                        (int) res));
                    DIMM dimmDevice = new DIMM(bus, "DIMM-" + i);
                    bus.addDevice(dimmDevice);
                    DIMMDriver dimmDriver = new DIMMDriver(bus, address);
                    dimmDevice.setDriver(dimmDriver);
                    DeviceUtils.getDeviceManager().register(dimmDevice);
                    log.info(dimmDevice.toString());
                }
            } catch (Exception ex) {
                log.debug("DIMM " + i + " not present");
            }

        }

    }

    private void reset() {
        statusWait(); // just make sure it is available (may be errors due to last conditions so...
        ioRes.outPortByte(hostStatusIORegister, 0x1e); // ...clears the error bits
    }

    private byte statusWait() {
        byte status = 0;
        for (int i = 0; i < 500; i++) ; // dumb delay : see specification update
        status = (byte) ioRes.inPortByte(hostStatusIORegister);
        int i;
        for (i = 0; (status & 0x01) == 1 && i < 2500000; i++)
            status = (byte) ioRes.inPortByte(hostStatusIORegister);
        if (i == 2500000) {
            System.err.println("SMBus wait status timeout");
            return -1;
        }
        return status;

    }

    private IOResource claimPorts(final ResourceManager rm, final ResourceOwner owner, final int low, final int length)
        throws ResourceNotFreeException, DriverException {
        try {
            return (IOResource) AccessControllerUtils.doPrivileged(new PrivilegedExceptionAction() {
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
}
