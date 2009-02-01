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
 
package org.jnode.driver.block.floppy.support;

import org.jnode.driver.Device;
import org.jnode.driver.DriverException;
import org.jnode.driver.block.CHS;
import org.jnode.driver.block.Geometry;
import org.jnode.driver.block.floppy.FDC;
import org.jnode.driver.block.floppy.FloppyControllerBus;
import org.jnode.driver.block.floppy.FloppyDevice;
import org.jnode.driver.block.floppy.FloppyDriveParameters;
import org.jnode.driver.block.floppy.FloppyDriveParametersCommand;
import org.jnode.driver.block.floppy.FloppyIdCommand;
import org.jnode.driver.block.floppy.FloppyParameters;
import org.jnode.driver.block.floppy.FloppyReadSectorCommand;
import org.jnode.driver.block.floppy.FloppySeekCommand;
import org.jnode.driver.block.floppy.FloppyWriteSectorCommand;
import org.jnode.system.ResourceNotFreeException;

public interface FloppyDeviceFactory {
    /**
     * The name used to lookup this service.
     */
    public static final Class<FloppyDeviceFactory> NAME = FloppyDeviceFactory.class;

    FloppyDevice createDevice(FloppyControllerBus bus, int drive, FloppyDriveParameters dp);

    FDC createFDC(Device device) throws DriverException, ResourceNotFreeException;

    FloppyDriveParametersCommand createFloppyDriveParametersCommand(int drive, FloppyDriveParameters dp,
                                                                    FloppyParameters fp);

    FloppySeekCommand createFloppySeekCommand(int drive, int cylinder);

    FloppyReadSectorCommand createFloppyReadSectorCommand(int drive, Geometry geometry, CHS chs, int currentSectorSize,
                                                          boolean b, int gap1Size, byte[] dest, int destOffset);

    FloppyWriteSectorCommand createFloppyWriteSectorCommand(int drive, Geometry geometry, CHS chs,
                                                            int currentSectorSize, boolean b, int gap1Size, byte[] src,
                                                            int srcOffset);

    FloppyIdCommand createFloppyIdCommand(int drive);
}
