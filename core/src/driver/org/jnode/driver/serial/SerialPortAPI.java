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

package org.jnode.driver.serial;

import org.jnode.driver.character.CharacterDeviceAPI;

/**
 * @author mgeisse
 *         <p/>
 *         Serial port API. Note: This should actually be a subinterface of
 *         CharacterDeviceAPI, but currently only single bytes can be sent.
 */
public interface SerialPortAPI extends CharacterDeviceAPI {
    public static final int BAUD1200 = 96;
    public static final int BAUD1800 = 64;
    public static final int BAUD2400 = 48;
    public static final int BAUD3600 = 32;
    public static final int BAUD4800 = 24;
    public static final int BAUD7200 = 16;
    public static final int BAUD9600 = 12;

    /**
     * Configure the data format to be sent. Data is sent in blocks of 5 to 8
     * bits, with 1, 1.5 or 2 stop bits and optional parity bits, in a
     * definable baud rate.
     * <p/>
     * The baud rate cannot be set directly, but only by a divisor parameter.
     * The actual rate is then determined by the formula (115200/divisor). This
     * is to allow most accurate control over the serial port hardware, which
     * allows control only through a divisor value. The driver interface
     * defines some constants for often-used baud rates.
     * <p/>
     * This driver always maps one byte from the driver's data channel to one
     * data block on the serial port. When reading or writing data in blocks
     * less than 8 bits, the most significant bits are ignored (sending) or
     * cleared (receiving).
     * <p/>
     * The number of stop bits is determined as follows: If the longStop
     * parameter is false, then one stop bit is used. Otherwise, a longer stop
     * sequence is used: If the data block length is 5 bits, then 1.5 stop bits
     * are used. If the data block length is 6-8 bits, then 2 stop bits are
     * used.
     * <p/>
     * Note that configuration of the serial port first requires to flush all
     * buffers. If there is more data to be sent or received, this method
     * blocks until that has happened.
     *
     * @param divisor  defines the baud rate divisor
     * @param bits     defines the number of bits per data block, ranging 5-8.
     * @param longStop determines whether more than one stop bit is used
     * @param parity   enables the parity bit
     * @param pEven    determines whether even parity (true) or odd parity (false)
     *                 is sent
     */
    public void configure(int divisor, int bits, boolean longStop, boolean parity, boolean pEven);

    /**
     * Configure the data format to be sent, using 8 data bits, no parity bit,
     * and 1 stop bit.
     *
     * @param divisor the baud rate divisor
     * @see org.jnode.driver.serial.SerialPortDriver#configure(int,int,boolean,boolean,boolean)
     */
    public void configure(int divisor);

    /**
     * This method is not part of the final serial port API.
     * <p/>
     * Receive a single byte from the serial port. This method blocks until a
     * byte is available.
     *
     * @return the received byte
     */
    public int readSingle();

    /**
     * This method is not part of the final serial port API.
     * <p/>
     * Send a single byte through the serial port. This function blocks until
     * the byte can be sent to the transmission buffer.
     *
     * @param value the byte to transmit
     */
    public void writeSingle(int value);

    /**
     * Wait until all buffered data is sent.
     */
    public void flush();
}
