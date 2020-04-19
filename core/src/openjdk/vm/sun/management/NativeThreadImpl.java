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

package sun.management;

import java.lang.management.ThreadInfo;

/**
 * @author Levente S\u00e1ntha
 * @author Fabien DUMINY (fduminy at jnode.org)
 * @see sun.management.ThreadImpl
 */
class NativeThreadImpl {
    /**
     * @see sun.management.ThreadImpl#getThreads()
     */
    private static Thread[] getThreads() {
        return Management.getThreads(false, false);
    }

    /**
     * @param ids    The identifiers of the threads.
     * @param result The informations about threads identified by their id (parameter <code>ids</code>) or null.
     * @params maxDepth The maximal depth of the stack dump (-1 means entire stack; 0 means no stacktrace).
     * @see sun.management.ThreadImpl#getThreadInfo0(long[], int, java.lang.management.ThreadInfo[])
     * <p/>
     * Look at method <code>jmm_GetThreadInfo</code> in hotspot sources (hotspot/src/share/vm/services/management.cpp)
     * for further details.
     */
    private static void getThreadInfo0(long[] ids, int maxDepth, ThreadInfo[] result) {
        Management.fillThreadInfos(ids, false, false, maxDepth, result);
    }

    /**
     * @see sun.management.ThreadImpl#getThreadTotalCpuTime0(long)
     */
    private static long getThreadTotalCpuTime0(long id) {
        return Management.getThreadById(id).getTotalCpuTime();
    }

    /**
     * @see sun.management.ThreadImpl#getThreadUserCpuTime0(long)
     */
    private static long getThreadUserCpuTime0(long id) {
        return Management.getThreadById(id).getUserCpuTime();
    }

    /**
     * @see sun.management.ThreadImpl#setThreadCpuTimeEnabled0(boolean)
     */
    private static void setThreadCpuTimeEnabled0(boolean enabled) {
        // for now, simply ignore (==> thread CPU time always enabled)
    }

    /**
     * @see sun.management.ThreadImpl#setThreadContentionMonitoringEnabled0(boolean)
     */
    private static void setThreadContentionMonitoringEnabled0(boolean arg1) {
        //todo add thread contention monitoring support
        throw new UnsupportedOperationException();
    }

    /**
     * Find locked threads by only walking through monitors.
     * <p/>
     * Look at method <code>find_deadlocks_at_safepoint</code> in hotspot sources (src/share/vm/services/threadService.cpp)
     * for further details.
     *
     * @see sun.management.ThreadImpl#findMonitorDeadlockedThreads0()
     */
    private static Thread[] findMonitorDeadlockedThreads0() {
        return Management.getThreads(true, false); // concurrentLocks=false
    }

    /**
     * This method is like {@link #findDeadlockedThreads0()} but is walking through concurrent locks.
     * Concurrent locks are defined by looking at {@link java.util.concurrent.locks.AbstractOwnableSynchronizer#exclusiveOwnerThread}
     * and {@link java.lang.Thread#parkBlocker} fields.
     * <p/>
     * Look at method <code>find_deadlocks_at_safepoint</code> in hotspot sources (src/share/vm/services/threadService.cpp)
     * for further details.
     *
     * @see sun.management.ThreadImpl#findDeadlockedThreads0()
     */
    private static Thread[] findDeadlockedThreads0() {
        return Management.getThreads(true, true); // concurrentLocks=true
    }

    /**
     * @see sun.management.ThreadImpl#resetPeakThreadCount0()
     */
    private static void resetPeakThreadCount0() {
        org.jnode.vm.scheduler.VmThread.resetPeakThreadCount();
    }

    /**
     * @param ids                 If null, dump all threads.
     * @param lockedMonitors
     * @param lockedSynchronizers
     * @see sun.management.ThreadImpl#dumpThreads0(long[], boolean, boolean)
     * <p/>
     * Look at method <code>jmm_DumpThreads</code> in hotspot sources (hotspot/src/share/vm/services/management.cpp)
     * for further details.
     */
    private static ThreadInfo[] dumpThreads0(long[] ids, boolean lockedMonitors, boolean lockedSynchronizers) {
        ThreadInfo[] result = (ids == null) ? null : new ThreadInfo[ids.length];
        return Management.fillThreadInfos(ids, lockedMonitors, lockedSynchronizers, -1, result);
    }

    /**
     * @see sun.management.ThreadImpl#resetContentionTimes0(long)
     */
    private static void resetContentionTimes0(long arg1) {
        //todo implement it
    }
}
