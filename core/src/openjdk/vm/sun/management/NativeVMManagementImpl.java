/*
 * $Id$
 *
 * Copyright (C) 2003-2013 JNode.org
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
 
package sun.management;

import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.isolate.VmIsolate;

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
        return VmUtils.getVm().availableProcessors();
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
