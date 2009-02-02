/*
 * $Id$
 *
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
 
package org.jnode.driver.bus.smbus;

import javax.naming.NamingException;
import org.jnode.naming.InitialNaming;

/**
 * SMBus controller.
 * <p/>
 * <p>
 * Title: SMBus
 * </p>
 * <p>
 * Description:
 * </p>
 * The System Management Bus (SMBus) is a two-wire interface through which various system component
 * chips can communicate with each other and with the rest of the system. It is based on the
 * principles of . SMBus provides a control bus for system and power management related tasks. A
 * system may use SMBus to pass messages to and from devices instead of tripping individual control
 * lines. Removing the individual control lines reduces pin count. Accepting messages ensures
 * future expandability. With System Management Bus, a device can provide manufacturer information,
 * tell the system what its model/part number is, save its state for a suspend event, report
 * different types of errors, accept control parameters, and return its status.
 * <P>
 * The System Management Bus Specification refers to three types of devices. A slave is a device
 * that is receiving or responding to a command. A master is a device that issues commands,
 * generates the clocks, and terminates the transfer. A host is a specialized master that provides
 * the main interface to the system's CPU. A host must be a master-slave and must support the SMBus
 * host notify protocol. There may be at most one host in a system. One example of a hostless
 * system is a simple battery charging station. The station might sit plugged into a wall waiting
 * to charge a smart battery. A device may be designed so that it is never a master and always a
 * slave. A device may act as a slave most of the time, but in special instances it may become a
 * master. A device can also be designed to be a master only. A system host is an example of a
 * device that acts as a host most of the time but that includes some slave behavior..
 * <P>
 * Any device that exists on the System Management Bus as a slave has a unique address called the
 * Slave Address. For reference, the following addresses are reserved and must not be used by or
 * assigned to any SMBus slave device unless otherwise detailed by this specification.
 * <P>
 * Bits 7-1:Slave Address
 * <P>
 * Bit 0: R/W# bit
 * <P>
 * Addr RW Comment
 * <P>
 * 0000 000 0 General Call Address
 * <P>
 * 0000 000 1 START byte
 * <P>
 * 0000 001 X CBUS address
 * <P>
 * 0000 010 X Address reserved for different bus format
 * <P>
 * 0000 011 X Reserved for future use
 * <P>
 * 0000 1XX X Reserved for future use
 * <P>
 * 0101 000 X Reserved for ACCESS.bus host
 * <P>
 * 0110 111 X Reserved for ACCESS.bus default address
 * <P>
 * 1111 0XX X 10-bit slave addressing
 * <P>
 * 1111 1XX X Reserved for future use
 * <P>
 * 0001 000 X SMBus Host
 * <P>
 * 0001 100 X SMBus Alert Response Address
 * <P>
 * 1100 001 X SMBus Device Default Address
 * <P>
 * All other addresses are available for address assignment for dynamic address devices and/or for
 * miscellaneous devices. Miscellaneous device addresses are discussed in Section 5.2.1.4.
 * <p/>
 * <p>
 * Licence: GNU LGPL
 * </p>
 * <p>
 * </p>
 *
 * @author Francois-Frederic Ozog
 * @version 1.0
 */

public abstract class SMBusControler {

    public static final Class<SMBusControler> NAME = SMBusControler.class; //"system/smbus";

    public SMBusControler() {
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            System.err.println("Cannot register Keyboard Interpreter name in InitialNaming namespace");
        }
    }

    /**
     * Here, part of the slave address denotes the command \u2013 the R/W# bit. The R/W# bit may be used
     * to simply turn a device function on or off, or enable/disable a low-power standby mode.
     * There is no data sent or received.
     * <p/>
     * The quick command implementation is good for very small devices that have limited support
     * for the SMBus specification. It also limits data on the bus for simple devices.
     *
     * @param address a 7 bit quantity
     * @return ACK/NACK from device
     * @throws java.security.InvalidParameterException
     *
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract boolean quickCommand(byte address)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException;

    /**
     * A simple device may recognize its own slave address and accept up to 256 possible encoded
     * commands in the form of a byte that follows the slave address. All or parts of the Send Byte
     * may contribute to the command. For example, the highest 7 bits of the command code might
     * specify an access to a feature, while the least significant bit would tell the device to
     * turn the feature on or off. Or, a device may set the \u201cvolume\u201d of its output based on the
     * value it received from the Send Byte protocol.
     *
     * @param address a 7 bit quantity
     * @param value   a 8 bit quantity
     * @return ACK/NACK from device
     * @throws java.security.InvalidParameterException
     *
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract boolean sendByte(byte address, byte value)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException;

    /**
     * The Receive Byte is similar to a Send Byte, the only difference being the direction of data
     * transfer. A simple device may have information that the host needs. It can do so with the
     * Receive Byte protocol. The same device may accept both Send Byte and Receive Byte protocols.
     * A NACK (a \u20181\u2019 in the ACK bit position) signifies the end of a read transfer.
     *
     * @param address a 7 bit quantity
     * @return ACK/NACK from device
     * @throws java.security.InvalidParameterException
     *
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract byte receiveByte(byte address)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException;

    /**
     * The first byte of a Write Byte access is the command code. The next one or two bytes,
     * respectively, are the data to be written. In this example the master asserts the slave
     * device address followed by the write bit. The device acknowledges and the master delivers
     * the command code. The slave again acknowledges before the master sends the data byte or word
     * (low byte first). The slave acknowledges each byte, and the entire transaction is finished
     * with a STOP condition.
     *
     * @param address a 7 bit quantity
     * @param value   a 8 bit quantity
     * @return ACK/NACK from device
     * @throws java.security.InvalidParameterException
     *
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract boolean writeByte(byte address, byte offset, byte value)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException, java.io.IOException;

    /**
     * The first byte of a Write Word access is the command code. The next one or two bytes,
     * respectively, are the data to be written. In this example the master asserts the slave
     * device address followed by the write bit. The device acknowledges and the master delivers
     * the command code. The slave again acknowledges before the master sends the data byte or word
     * (low byte first). The slave acknowledges each byte, and the entire transaction is finished
     * with a STOP condition.
     *
     * @param address a 7 bit quantity
     * @param value   a 16 bit quantity
     * @return ACK/NACK from device
     * @throws java.security.InvalidParameterException
     *
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract boolean writeWord(byte address, byte offset, int value)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException, java.io.IOException;

    /**
     * Reading data is slightly more complicated than writing data. First the host must write a
     * command to the slave device. Then it must follow that command with a repeated START
     * condition to denote a read from that device\u2019s address. The slave then returns one or two
     * bytes of data.
     * <p/>
     * Note that there is no STOP condition before the repeated START condition, and that a NACK
     * signifies the end of the read transfer.
     *
     * @param deviceaddress a 7 bit quantity
     * @param address       a 8 bit quantity used to identify what is requested
     * @return byte read
     * @throws java.security.InvalidParameterException
     *
     * @throws java.io.IOException if transfer encounters an error
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract byte readByte(byte deviceaddress, byte address)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException;

    /**
     * Reading data is slightly more complicated than writing data. First the host must write a
     * command to the slave device. Then it must follow that command with a repeated START
     * condition to denote a read from that device\u2019s address. The slave then returns one or two
     * bytes of data.
     * <p/>
     * Note that there is no STOP condition before the repeated START condition, and that a NACK
     * signifies the end of the read transfer.
     *
     * @param address a 7 bit quantity
     * @return word (16 bits) read
     * @throws java.security.InvalidParameterException
     *
     * @throws java.io.IOException if transfer encounters an error
     */
    public abstract int readWord(byte address, byte offset)
        throws java.security.InvalidParameterException, java.io.IOException;

    /**
     * The process call is so named because a command sends data and waits for the slave to return
     * a value dependent on that data. The protocol is simply a Write Word followed by a Read Word
     * without the Read-Word command field and the Write-Word STOP bit.
     * <p/>
     * Note that there is no STOP condition before the repeated START condition, and that a NACK
     * signifies the end of the read transfer.
     *
     * @param address   a 7 bit quatntity
     * @param command   device specific command
     * @param parameter a 16 bits quantity
     * @return a 16 bits quantity
     * @throws java.security.InvalidParameterException
     *
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract int processCall(byte address, byte command, int parameter)
        throws java.security.InvalidParameterException, java.lang.UnsupportedOperationException;

    /**
     * The Block Write begins with a slave address and a write condition. After the command code
     * the host issues a byte count which describes how many more bytes will follow in the message.
     * If a slave has 20 bytes to send, the byte count field will have the value 20 (14h), followed
     * by the 20 bytes of data. The byte count does not include the PEC byte. The byte count may
     * not be 0. A Block Read or Write is allowed to transfer a maximum of 32 data bytes.
     *
     * @param address a 7 bit quantity
     * @param block   size must be gretater than 0 and less than 32 bytes
     * @return ACK/NACK status
     * @throws java.security.InvalidParameterException
     *
     * @throws java.io.IOException
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract boolean blockWrite(byte address, byte[] block)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException;

    /**
     * A Block Read differs from a block write in that the repeated START condition exists to
     * satisfy the requirement for a change in the transfer direction. A NACK immediately preceding
     * the STOP condition signifies the end of the read transfer.
     *
     * @param address a 7 bit quantity
     * @return ACK/NACK status
     * @throws java.security.InvalidParameterException
     *
     * @throws java.io.IOException
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract byte[] blockRead(byte address, byte offset)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException,
        java.io.IOException;

    /**
     * The block write-block read process call is a two-part message. The call begins with a slave
     * address and a write condition. After the command code the host issues a write byte count (M)
     * that describes how many more bytes will be written in the first part of the message. If a
     * master has 6 bytes to send, the byte count field will have the value 6 (0000 0110b),
     * followed by the 6 bytes of data. The write byte count (M) cannot be zero.
     * <p/>
     * The second part of the message is a block of read data beginning with a repeated start
     * condition followed by the slave address and a Read bit. The next byte is the read byte count
     * (N), which may differ from the write byte count (M). The read byte count (N) cannot be zero.
     * <p/>
     * The combined data payload must not exceed 32 bytes. The byte length restrictions of this
     * process call are summarized as follows:
     * <P>\u2022 M ???2651 byte
     * <P>\u2022 N \u22651 byte
     * <P>\u2022 M + N \u226432 bytes
     * <p/>
     * The read byte count does not include the PEC byte. The PEC is computed on the total message
     * beginning with the first slave address and using the normal PEC computational rules. It is
     * highly recommended that a PEC byte be used with the Block Write-Block Read Process Call.
     * <p/>
     * Note that there is no STOP condition before the repeated START condition, and that a NACK
     * signifies the end of the read transfer.
     *
     * @param address  a 7 bit quantity
     * @param inblock  data sent to the device
     * @param outblock data returned from the device
     * @return ACK/NACK status
     * @throws java.security.InvalidParameterException
     *                             in particular in inblock.size() + outblock.size() greater than 32, or
     *                             inblock.size==0 or outblock.size==0
     * @throws java.io.IOException if transfer interupted
     * @throws java.lang.UnsupportedOperationException
     *
     */
    public abstract boolean blockWriteProcessCall(byte address, byte[] inblock, byte[] outblock)
        throws java.security.InvalidParameterException, java.io.IOException, java.lang.UnsupportedOperationException;

    public abstract void probeDevices(SMBus bus);
    /*
      * {
      *
      */
}
