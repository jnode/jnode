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
 
package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.Context;
import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;

/**
 * Command used to remove a partition from a device.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class RemovePartitionCommand extends BasePartitionCommand {

    /**
     * Constructor.
     * @param device The device to use.
     * @partitionNumber Number (zero based) of the device's partition.
     */
    public RemovePartitionCommand(IDEDevice device, int partitionNumber) {
        super("remove partition", device, partitionNumber);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void doExecute(Context context) throws CommandException {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "remove partition " + partitionNumber + " on device" + device.getId();
    }

}
