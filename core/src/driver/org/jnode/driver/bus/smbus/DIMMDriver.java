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
 
package org.jnode.driver.bus.smbus;

import java.io.IOException;
import java.security.InvalidParameterException;
import org.apache.log4j.Logger;
import org.jnode.driver.Driver;
import org.jnode.driver.DriverException;

/**
 * DIMM device driver.
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

public class DIMMDriver extends Driver {

    private static final Logger log = Logger.getLogger(DIMMDriver.class);
    private SMBus bus;
    private byte address;
    private DIMM dimmDevice;
    private int spdTableLength = 0;
    private byte[] spdTable = null;

    public DIMMDriver(SMBus bus, byte address) {
        this.bus = bus;
        this.address = address;
    }

    protected void stopDevice() throws org.jnode.driver.DriverException {
        /** @todo Implement this org.jnode.driver.Driver abstract method */
    }

    protected void startDevice() throws org.jnode.driver.DriverException {
        dimmDevice = (DIMM) getDevice();
        // now read the SPD table
        try {
            if (!DIMMDriver.canExist(bus, address))
                throw new DriverException("Device doesn't exist");

            log.debug("Getting SPD Table from " + dimmDevice.getId() + ":");
            spdTableLength = (bus.readByte(address, (byte) 0)) & 0xff;
            log.debug(" length=" + spdTableLength);

            spdTable = new byte[spdTableLength];
            spdTable[0] = (byte) spdTableLength;
            for (int i = 1; i < spdTableLength; i++)
                spdTable[i] = bus.readByte(address, (byte) i);
            dimmDevice.setSPDTable(spdTable);
        } catch (UnsupportedOperationException ex) {
            //todo empty?
        } catch (IOException ex) {
            //todo empty?
        } catch (InvalidParameterException ex) {
            //todo empty?
        }

    }

    public static boolean canExist(SMBus bus, byte address) {
        try {
            int spdTableLength = (bus.readByte(address, (byte) 0)) & 0xff;
            if (spdTableLength < DIMM.SPDTABLE_SIZE) {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

}
