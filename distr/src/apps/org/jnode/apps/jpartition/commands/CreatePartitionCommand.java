/*
 * $Id$
 *
 * Copyright (C) 2003-2014 JNode.org
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
 * Command to create a partition.
 * @author Fabien DUMINY (fduminy@jnode.org)
 *
 */
public class CreatePartitionCommand extends BasePartitionCommand {
    /**
     * The start sector of the partition to create.
     */
    private final long start;
    
    /**
     * The number of sectors of the partition to create.
     */
    private final long size;

    /**
     * Constructor.
     * @param device The device to use.
     * @partitionNumber Number (zero based) of the device's partition.
     * @param start The start sector of the partition to create.
     * @param size The number of sectors of the partition to create.
     */
    public CreatePartitionCommand(IDEDevice device, int partitionNumber, long start, long size) {
        super("create partition", device, partitionNumber);
        this.start = start;
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void doExecute(Context context) throws CommandException {
        // PartitionHelper helper = createPartitionHelper();
        // try {
        //
        // helper.write();
        // } catch (IOException e) {
        // throw new CommandException(e);
        // }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "create partition [" + start + ", " + (start + size - 1) + "] on device " +
                device.getId();
    }
}
