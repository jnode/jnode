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
 
package org.jnode.partitions;

import org.jnode.driver.Device;
import org.jnode.driver.block.BlockDeviceAPI;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public interface PartitionTableType {

    /**
     * Gets the unique name of this partition table type.
     */
    public String getName();

    /**
     * Can this partition table type be used on the given first sector of a
     * blockdevice?
     * 
     * @param devApi
     */
    public boolean supports(byte[] firstSector, BlockDeviceAPI devApi);

    /**
     * Create a partition table for a given device.
     * 
     * @param device
     * @param readOnly
     */
    public PartitionTable create(byte[] firstSector, Device device) throws PartitionTableException;

}
