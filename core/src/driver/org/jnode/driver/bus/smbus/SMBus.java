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

package org.jnode.driver.bus.smbus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jnode.driver.Bus;
import org.jnode.driver.Device;

/**
 * SMBus.
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

public class SMBus extends Bus {

    public static final byte ADDRESS_HOST = 0x10; // 0001 000 SMBus Host
    // System Management Bus
    // Specification\u00b9

    public static final byte ADDRESS_BATTERY_CHARGER = 0x12; // 0001 001
    // Smart Battery
    // Charger Smart
    // Battery
    // Charger
    // Specification\u00b9

    public static final byte ADDRESS_BATTERY_SELECTOR = 0x14; // 0001 010
    // Smart Battery
    // Selector

    public static final byte ADDRESS_BATTERY = 0x16; // 0001 011 Smart
    // Battery Smart Battery
    // Data
    // Specification\u00b9

    public static final byte ADDRESS_ALERT = 0x18; // 0001 100 SMBus Alert
    // Response System
    // Management Bus
    // Specification\u00b9

    public static final byte ADDRESS_ACCESSBUS_HOST = 0x50; // 0101 000
    // ACCESS.bus host

    public static final byte ADDRESS_LCD_CONTARST = 0x58; // 0101 100 Reserved
    // by previous
    // versions of the
    // SMBus
    // specification for
    // LCD Contrast
    // Controller. This
    // address may be
    // reassigned in
    // future versions
    // of the SMBus
    // specification.

    public static final byte ADDRESS_BACK_LIGHT = 0x5A; // 0101 101 Reserved by
    // previous versions of
    // the SMBus
    // specification for
    // CCFL Backlight
    // Driver. This address
    // may be reassigned in
    // future versions of
    // the SMBus
    // specification.

    public static final byte ADDRESS_ACCESSBUS_DEFAULT = 0x6E; // 0110 111
    // ACCESS.bus
    // default
    // address

    public static final byte ADDRESS_PCMCIA0 = (byte) 0x80; // 1000 0XX Reserved
    // by previous
    // versions of the
    // SMBus
    // specification for
    // PCMCIA Socket
    // Controllers (4
    // addresses) .
    // These addresses
    // may be reassigned
    // in future
    // versions of the
    // SMBus
    // specification.

    public static final byte ADDRESS_PCMCIA1 = (byte) 0x82; // 1000 0XX Reserved
    // by previous
    // versions of the
    // SMBus
    // specification for
    // PCMCIA Socket
    // Controllers (4
    // addresses) .
    // These addresses
    // may be reassigned
    // in future
    // versions of the
    // SMBus
    // specification.

    public static final byte ADDRESS_PCMCIA2 = (byte) 0x84; // 1000 0XX Reserved
    // by previous
    // versions of the
    // SMBus
    // specification for
    // PCMCIA Socket
    // Controllers (4
    // addresses) .
    // These addresses
    // may be reassigned
    // in future
    // versions of the
    // SMBus
    // specification.

    public static final byte ADDRESS_PCMCIA3 = (byte) 0x86; // 1000 0XX Reserved
    // by previous
    // versions of the
    // SMBus
    // specification for
    // PCMCIA Socket
    // Controllers (4
    // addresses) .
    // These addresses
    // may be reassigned
    // in future
    // versions of the
    // SMBus
    // specification.

    public static final byte ADDRESS_VGA = (byte) 0x88; // 1000 100 Reserved by
    // previous versions of
    // the SMBus
    // specification for
    // (VGA) Graphics
    // Controller. This
    // address may be
    // reassigned in future
    // versions of the SMBus
    // specification.

    public static final byte ADDRESS_UNRESTRICTED0 = (byte) 0x90; // 1001 0XX
    // Unrestricted
    // addresses
    // System
    // Management
    // Bus
    // Specification

    public static final byte ADDRESS_UNRESTRICTED1 = (byte) 0x92; // 1001 0XX
    // Unrestricted
    // addresses
    // System
    // Management
    // Bus
    // Specification

    public static final byte ADDRESS_UNRESTRICTED2 = (byte) 0x93; // 1001 0XX
    // Unrestricted
    // addresses
    // System
    // Management
    // Bus
    // Specification

    public static final byte ADDRESS_UNRESTRICTED3 = (byte) 0x96; // 1001 0XX
    // Unrestricted
    // addresses
    // System
    // Management
    // Bus
    // Specification

    public static final byte ADDRESS_RAMDIM_BASE = (byte) 0xA0; // 1010 XXX RAM
    // DIM specs

    public static final byte ADDRESS_DEFAULT = (byte) 0xC2; // 1100 001 SMBus
    // Device Default
    // Address System
    // Management Bus
    // Specification\u00b9

    private final SMBusControler controler;

    private final List<SMBusDevice> devices = new ArrayList<SMBusDevice>();

    public SMBus(Device parent, SMBusControler controler) {
        super(parent);
        this.controler = controler;
    }

    public void probeDevices() {
        controler.probeDevices(this);
    }

    public void addDevice(SMBusDevice device) {
        devices.add(device);
    }

    public List<SMBusDevice> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    public boolean quickCommand(byte address)
        throws java.security.InvalidParameterException,
        java.lang.UnsupportedOperationException {
        return controler.quickCommand(address);
    }

    public boolean sendByte(byte address, byte value)
        throws java.security.InvalidParameterException,
        java.lang.UnsupportedOperationException {
        return controler.sendByte(address, value);
    }

    public byte receiveByte(byte address)
        throws java.security.InvalidParameterException,
        java.lang.UnsupportedOperationException {
        return controler.receiveByte(address);
    }

    public boolean writeByte(byte address, byte offset, byte value)
        throws java.security.InvalidParameterException,
        java.lang.UnsupportedOperationException, java.io.IOException {
        return controler.writeByte(address, offset, value);
    }

    public boolean writeWord(byte address, byte offset, int value)
        throws java.security.InvalidParameterException,
        java.lang.UnsupportedOperationException, java.io.IOException {
        return controler.writeWord(address, offset, value);
    }

    public byte readByte(byte deviceaddress, byte address)
        throws java.security.InvalidParameterException,
        java.io.IOException, java.lang.UnsupportedOperationException {
        return controler.readByte(deviceaddress, address);
    }

    public int readWord(byte address, byte offset)
        throws java.security.InvalidParameterException, java.io.IOException {
        return controler.readWord(address, offset);
    }

    public int processCall(byte address, byte command, int parameter)
        throws java.security.InvalidParameterException,
        java.lang.UnsupportedOperationException {
        return controler.processCall(address, command, parameter);
    }

    public boolean blockWrite(byte address, byte[] block)
        throws java.security.InvalidParameterException,
        java.io.IOException, java.lang.UnsupportedOperationException {
        return controler.blockWrite(address, block);
    }

    public byte[] blockRead(byte address, byte command)
        throws java.security.InvalidParameterException,
        java.io.IOException, java.lang.UnsupportedOperationException,
        java.io.IOException {
        return controler.blockRead(address, command);
    }

    public boolean blockWriteProcessCall(byte address, byte[] inblock,
                                         byte[] outblock) throws java.security.InvalidParameterException,
        java.io.IOException, java.lang.UnsupportedOperationException {
        return controler.blockWriteProcessCall(address, inblock, outblock);
    }

}
