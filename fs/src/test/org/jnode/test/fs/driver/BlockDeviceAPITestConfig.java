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
 
package org.jnode.test.fs.driver;

import java.util.ArrayList;
import java.util.List;
import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.bus.ide.IDEConstants;
import org.jnode.test.support.ContextManager;
import org.jnode.test.support.TestConfig;
import org.jnode.util.NumberUtils;

/**
 * @author Fabien DUMINY
 */
public class BlockDeviceAPITestConfig implements TestConfig {
    private static final String DEVICE_SIZE_STR = "1M"; // may use multipliers
    // (K, M, G)

    private static final int DEVICE_SIZE = (int) NumberUtils.getSize(DEVICE_SIZE_STR);

    private Class<?> contextClass;

    private final List<Partition> partitions = new ArrayList<Partition>();

    public BlockDeviceAPITestConfig(Class contextClass) {
        this.contextClass = contextClass;
    }

    public void addPartition(Partition partition) {
        partitions.add(partition);
    }

    public Partition[] getPartitions() {
        return partitions
            .toArray(new Partition[partitions.size()]);
    }

    /**
     * @return
     */
    public final BlockDeviceAPI getBlockDeviceAPI() {
        return ((BlockDeviceAPIContext) ContextManager.getInstance().getContext()).getApi();
    }

    public final Class<?> getContextClass() {
        return contextClass;
    }

    /**
     *
     */
    public String toString() {
        if (ContextManager.getInstance().getContext() == null) {
            return getContextClass().getName() + "[NO_CONTEXT]";
        }

        BlockDeviceAPI api = getBlockDeviceAPI();
        return (api == null) ? getContextClass().getName() + "[NO_API]" : api
            .getClass().getName();
    }

    public int getDeviceSize() {
        return DEVICE_SIZE;
    }

    public int getDeviceNbSectors() {
        return DEVICE_SIZE / IDEConstants.SECTOR_SIZE;
    }

    public String getName() {
        return ((BlockDeviceAPIContext) ContextManager.getInstance().getContext()).getName();
    }
}
