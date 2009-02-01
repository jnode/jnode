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
 
package org.jnode.apps.jpartition.consoleview;

import org.jnode.apps.jpartition.consoleview.components.Labelizer;
import org.jnode.apps.jpartition.model.Partition;
import org.jnode.util.NumberUtils;

class PartitionLabelizer implements Labelizer<Partition> {
    static final PartitionLabelizer INSTANCE = new PartitionLabelizer();

    public String getLabel(Partition partition) {
        if (partition == null) {
            throw new NullPointerException("partition is null");
        }

        StringBuilder sb = new StringBuilder();

        sb.append('[').append(partition.getStart()).append(',').append(partition.getEnd()).append(
                ']');
        sb.append(" (").append(NumberUtils.toBinaryByte(partition.getSize())).append(") ");
        String format = partition.isUsed() ? partition.getFormat() : "unused";
        sb.append(format);

        return sb.toString();
    }
}
