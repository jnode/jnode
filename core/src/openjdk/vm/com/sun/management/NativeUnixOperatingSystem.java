/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
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

package com.sun.management;

import java.io.IOException;
import java.io.VMIOUtils;
import org.jnode.vm.VmSystem;

/**
 * @see com.sun.management.UnixOperatingSystem
 */
class NativeUnixOperatingSystem {
    /**
     * @see com.sun.management.UnixOperatingSystem#getCommittedVirtualMemorySize()
     */
    private static long getCommittedVirtualMemorySize(UnixOperatingSystem instance) {
        return -1; //TODO implement SWAP
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getTotalSwapSpaceSize()
     */
    private static long getTotalSwapSpaceSize(UnixOperatingSystem instance) {
        return 0; //TODO implement SWAP
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getFreeSwapSpaceSize()
     */
    private static long getFreeSwapSpaceSize(UnixOperatingSystem instance) {
        return 0; //TODO implement SWAP
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getProcessCpuTime()
     */
    private static long getProcessCpuTime(UnixOperatingSystem instance) {
        return VmSystem.currentKernelMillis();
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getFreePhysicalMemorySize()
     */
    private static long getFreePhysicalMemorySize(UnixOperatingSystem instance) {
        return VmSystem.freeMemory();
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getTotalPhysicalMemorySize()
     */
    private static long getTotalPhysicalMemorySize(UnixOperatingSystem instance) {
        return VmSystem.totalMemory();
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getOpenFileDescriptorCount()
     */
    private static long getOpenFileDescriptorCount(UnixOperatingSystem instance) {
        try {
            return VMIOUtils.getAPI().getOpenFileDescriptorCount();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see com.sun.management.UnixOperatingSystem#getMaxFileDescriptorCount()
     */
    private static long getMaxFileDescriptorCount(UnixOperatingSystem instance) {
        // returns Integer.MAX_VALUE since
        // VMFileSystemAPI.getOpenFileDescriptorCount() returns an int.
        return Integer.MAX_VALUE;
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#initialize()
     */
    private static void initialize() {
        //todo implement it
    }
}
