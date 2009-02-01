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
 
package org.jnode.test.gui;

import org.jnode.driver.ApiNotFoundException;
import org.jnode.driver.Device;
import org.jnode.driver.DeviceNotFoundException;
import org.jnode.driver.DeviceUtils;
import org.jnode.driver.video.ddc.DDC1NoSignalException;
import org.jnode.driver.video.ddc.DDC1ParseException;
import org.jnode.driver.video.ddc.DDC1Reader;
import org.jnode.driver.video.ddc.DisplayDataChannelAPI;
import org.jnode.driver.video.ddc.EDID;
import org.jnode.util.NumberUtils;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class DDC1Test {

    public static void main(String[] args) {

        final String devId = (args.length > 0) ? args[0] : "fb0";
        try {
            final Device dev = DeviceUtils.getDevice(devId);

            System.out.println("Reading DDC1 data, please wait");
            final DisplayDataChannelAPI api = dev.getAPI(DisplayDataChannelAPI.class);
            final DDC1Reader reader = new DDC1Reader(api);
            final EDID data = reader.read();

            System.out.println("DDC1-EDID=" + data);
            System.out.println("DDC1-EDID (raw)=" + NumberUtils.hex(data.getRawData()));
        } catch (DeviceNotFoundException ex) {
            System.out.println("Cannot find device " + devId);
        } catch (ApiNotFoundException ex) {
            System.out.println("No DisplayDataChannelAPI found on device " + devId);
        } catch (DDC1NoSignalException ex) {
            System.out.println("No DDC1 signal found");
        } catch (DDC1ParseException ex) {
            System.out.println("Invalid DDC1 data read: " + ex.getMessage() + ", it does not hurd to try again");
        }
    }
}
