/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2006 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
package org.jnode.partitions.service;

import java.util.Collection;

import org.jnode.partitions.PartitionTableType;

public interface PartitionTableService {

    /**
     * Name used to lookup a FileSystemTypeManager in the initial namespace.
     */
    public static final Class<PartitionTableService> NAME = PartitionTableService.class;

    /**
     * Gets all registered partition table types. All instances of the returned
     * collection are instanceof PartitionTableType.
     */
    public Collection<PartitionTableType> partitionTableTypes();

}
