package sun.management;

import java.lang.management.MemoryPoolMXBean;

/**
 * @see sun.management.MemoryManagerImpl
 */
class NativeMemoryManagerImpl {
    /**
     * @see sun.management.MemoryManagerImpl#getMemoryPools0()
     */
    private static MemoryPoolMXBean[] getMemoryPools0(MemoryManagerImpl instance) {
        //todo implement it
        return new MemoryPoolMXBean[0];
    }
}
