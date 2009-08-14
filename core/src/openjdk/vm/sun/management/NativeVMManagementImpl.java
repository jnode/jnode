package sun.management;

import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.Vm;

/**
 * @see sun.management.VMManagementImpl
 */
class NativeVMManagementImpl {
    /**
     * @see sun.management.VMManagementImpl#getVersion0()
     */
    private static String getVersion0() {
        //for java 6
        return "1.2";
    }
    /**
     * @see sun.management.VMManagementImpl#initOptionalSupportFields()
     */
    private static void initOptionalSupportFields() {
        //todo implement it
    }
    /**
     * @see sun.management.VMManagementImpl#isThreadContentionMonitoringEnabled()
     */
    private static boolean isThreadContentionMonitoringEnabled(VMManagementImpl instance) {
        //todo implement it
        return false;
    }
    /**
     * @see sun.management.VMManagementImpl#isThreadCpuTimeEnabled()
     */
    private static boolean isThreadCpuTimeEnabled(VMManagementImpl instance) {
        //todo implement it
        return false;
    }
    /**
     * @see sun.management.VMManagementImpl#getTotalClassCount()
     */
    private static long getTotalClassCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getUnloadedClassCount()
     */
    private static long getUnloadedClassCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getVerboseClass()
     */
    private static boolean getVerboseClass(VMManagementImpl instance) {
        //todo implement it
        return false;
    }
    /**
     * @see sun.management.VMManagementImpl#getVerboseGC()
     */
    private static boolean getVerboseGC(VMManagementImpl instance) {
        //todo implement it
        return false;
    }
    /**
     * @see sun.management.VMManagementImpl#getProcessId()
     */
    private static int getProcessId(VMManagementImpl instance) {
        return VmIsolate.currentIsolate().getId();
    }
    /**
     * @see sun.management.VMManagementImpl#getVmArguments0()
     */
    private static String getVmArguments0(VMManagementImpl instance) {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.VMManagementImpl#getStartupTime()
     */
    private static long getStartupTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getAvailableProcessors()
     */
    private static int getAvailableProcessors(VMManagementImpl instance) {
        return Vm.getVm().availableProcessors();
    }
    /**
     * @see sun.management.VMManagementImpl#getTotalCompileTime()
     */
    private static long getTotalCompileTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getTotalThreadCount()
     */
    private static long getTotalThreadCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getLiveThreadCount()
     */
    private static int getLiveThreadCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getPeakThreadCount()
     */
    private static int getPeakThreadCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getDaemonThreadCount()
     */
    private static int getDaemonThreadCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getSafepointCount()
     */
    private static long getSafepointCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getTotalSafepointTime()
     */
    private static long getTotalSafepointTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getSafepointSyncTime()
     */
    private static long getSafepointSyncTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getTotalApplicationNonStoppedTime()
     */
    private static long getTotalApplicationNonStoppedTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getLoadedClassSize()
     */
    private static long getLoadedClassSize(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getUnloadedClassSize()
     */
    private static long getUnloadedClassSize(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getClassLoadingTime()
     */
    private static long getClassLoadingTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getMethodDataSize()
     */
    private static long getMethodDataSize(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getInitializedClassCount()
     */
    private static long getInitializedClassCount(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getClassInitializationTime()
     */
    private static long getClassInitializationTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
    /**
     * @see sun.management.VMManagementImpl#getClassVerificationTime()
     */
    private static long getClassVerificationTime(VMManagementImpl instance) {
        //todo implement it
        return 0;
    }
}
