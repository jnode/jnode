package sun.management;

import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryUsage;

/**
 * @see sun.management.MemoryImpl
 */
class NativeMemoryImpl {
    /**
     * @see sun.management.MemoryImpl#getMemoryPools0()
     */
    private static MemoryPoolMXBean[] getMemoryPools0() {
        //todo implement it
        return new MemoryPoolMXBean[0];
    }
    /**
     * @see sun.management.MemoryImpl#getMemoryManagers0()
     */
    private static MemoryManagerMXBean[] getMemoryManagers0() {
        //todo implement it
        return new MemoryManagerMXBean[0];
    }
    /**
     * @see sun.management.MemoryImpl#getMemoryUsage0(boolean)
     */
    private static MemoryUsage getMemoryUsage0(MemoryImpl instance, boolean arg1) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.MemoryImpl#setVerboseGC(boolean)
     */
    private static void setVerboseGC(MemoryImpl instance, boolean arg1) {
        //todo implement it
    }
}
