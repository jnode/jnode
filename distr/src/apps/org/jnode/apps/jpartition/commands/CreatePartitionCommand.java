/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
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
 
package org.jnode.apps.jpartition.commands;

import org.jnode.apps.jpartition.commands.framework.CommandException;
import org.jnode.driver.bus.ide.IDEDevice;

public class CreatePartitionCommand extends BasePartitionCommand {
    private final long start;
    private final long size;

    public CreatePartitionCommand(IDEDevice device, int partitionNumber, long start, long size) {
        super("create partition", device, partitionNumber);
        this.start = start;
        this.size = size;
    }

    @Override
    protected final void doExecute() throws CommandException {
        // PartitionHelper helper = createPartitionHelper();
        // try {
        //
        // helper.write();
        // } catch (IOException e) {
        // throw new CommandException(e);
        // }
    }

    @Override
    public String toString() {
        return "create partition [" + start + ", " + (start + size - 1) + "] on device " +
                device.getId();
    }
}
