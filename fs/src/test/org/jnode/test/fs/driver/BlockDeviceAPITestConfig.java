/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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

package org.jnode.test.fs.driver;

import java.util.ArrayList;
import java.util.List;

import org.jnode.driver.block.BlockDeviceAPI;
import org.jnode.driver.ide.IDEConstants;
import org.jnode.test.support.ContextManager;
import org.jnode.test.support.TestConfig;
import org.jnode.util.NumberUtils;

/**
 * @author Fabien DUMINY
 */
public class BlockDeviceAPITestConfig implements TestConfig {
    final private static String DEVICE_SIZE_STR = "1M"; // may use multipliers
                                                        // (K, M, G)

    final private static int DEVICE_SIZE = (int) NumberUtils
            .getSize(DEVICE_SIZE_STR);

    private Class contextClass;

    private List partitions = new ArrayList();

    public BlockDeviceAPITestConfig(Class contextClass) {
        this.contextClass = contextClass;
    }

    public void addPartition(Partition partition) {
        partitions.add(partition);
    }

    public Partition[] getPartitions() {
        return (Partition[]) partitions
                .toArray(new Partition[partitions.size()]);
    }

    /**
     * @return
     */
    final public BlockDeviceAPI getBlockDeviceAPI() {
        return ((BlockDeviceAPIContext) ContextManager.getInstance()
                .getContext()).getApi();
    }

    final public Class getContextClass() {
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
        return ((BlockDeviceAPIContext) ContextManager.getInstance()
                .getContext()).getName();
    }
}
