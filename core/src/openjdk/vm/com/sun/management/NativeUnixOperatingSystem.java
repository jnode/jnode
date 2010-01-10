package com.sun.management;

import org.jnode.vm.VmSystem;

/**
 * @see com.sun.management.UnixOperatingSystem
 */
class NativeUnixOperatingSystem {
    /**
     * @see com.sun.management.UnixOperatingSystem#getCommittedVirtualMemorySize()
     */
    private static long getCommittedVirtualMemorySize(UnixOperatingSystem instance) {
        //todo implement it
        return -1;
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getTotalSwapSpaceSize()
     */
    private static long getTotalSwapSpaceSize(UnixOperatingSystem instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getFreeSwapSpaceSize()
     */
    private static long getFreeSwapSpaceSize(UnixOperatingSystem instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getProcessCpuTime()
     */
    private static long getProcessCpuTime(UnixOperatingSystem instance) {
        //todo implement it
        return -1;
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
        //todo implement it
        return 0;
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#getMaxFileDescriptorCount()
     */
    private static long getMaxFileDescriptorCount(UnixOperatingSystem instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see com.sun.management.UnixOperatingSystem#initialize()
     */
    private static void initialize() {
        //todo implement it
    }
}
