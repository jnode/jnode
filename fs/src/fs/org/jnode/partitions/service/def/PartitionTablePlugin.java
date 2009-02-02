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
 
package org.jnode.partitions.service.def;

import java.util.Collection;

import javax.naming.NamingException;

import org.jnode.naming.InitialNaming;
import org.jnode.partitions.PartitionTableType;
import org.jnode.partitions.service.PartitionTableService;
import org.jnode.plugin.Plugin;
import org.jnode.plugin.PluginDescriptor;
import org.jnode.plugin.PluginException;

public class PartitionTablePlugin extends Plugin implements PartitionTableService {

    private final PartitionTableTypeManager typeMgr;

    /**
     * @param descriptor
     */
    public PartitionTablePlugin(PluginDescriptor descriptor) {
        super(descriptor);
        this.typeMgr = new PartitionTableTypeManager(descriptor.getExtensionPoint("types"));
    }

    /**
     * @see org.jnode.plugin.Plugin#startPlugin()
     */
    @Override
    protected void startPlugin() throws PluginException {
        try {
            InitialNaming.bind(NAME, this);
        } catch (NamingException ex) {
            throw new PluginException(ex);
        }
    }

    /**
     * @see org.jnode.plugin.Plugin#stopPlugin()
     */
    @Override
    protected void stopPlugin() throws PluginException {
        InitialNaming.unbind(NAME);
    }

    /**
     * @see org.jnode.partitions.service.PartitionTableService#partitionTableTypes()
     */
    public Collection<PartitionTableType> partitionTableTypes() {
        return typeMgr.partitionTableTypes();
    }
}
