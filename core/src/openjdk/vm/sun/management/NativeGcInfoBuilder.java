package sun.management;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryUsage;
import com.sun.management.GcInfo;

/**
 * @see sun.management.GcInfoBuilder
 */
class NativeGcInfoBuilder {
    /**
     * @see sun.management.GcInfoBuilder#getNumGcExtAttributes(java.lang.management.GarbageCollectorMXBean)
     */
    private static int getNumGcExtAttributes(GcInfoBuilder instance, GarbageCollectorMXBean arg1) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.GcInfoBuilder#fillGcAttributeInfo(java.lang.management.GarbageCollectorMXBean, int, java.lang.String[], char[], java.lang.String[])
     */
    private static void fillGcAttributeInfo(GcInfoBuilder instance, GarbageCollectorMXBean arg1, int arg2, String[] arg3, char[] arg4, String[] arg5) {
        //todo implement it
    }
    /**
     * @see sun.management.GcInfoBuilder#getLastGcInfo0(java.lang.management.GarbageCollectorMXBean, int, java.lang.Object[], char[], java.lang.management.MemoryUsage[], java.lang.management.MemoryUsage[])
     */
    private static GcInfo getLastGcInfo0(GcInfoBuilder instance, GarbageCollectorMXBean arg1, int arg2, Object[] arg3, char[] arg4, MemoryUsage[] arg5, MemoryUsage[] arg6) {
        //todo implement it
        return null;
    }
}
