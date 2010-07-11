/*
 * $Id$
 *
 * Copyright (C) 2003-2010 JNode.org
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
import java.util.ArrayList;

import org.jnode.vm.facade.VmThread;
import org.jnode.vm.facade.VmThreadVisitor;
import org.jnode.vm.facade.VmUtils;

/**
 * @see sun.management.ThreadImpl
 */
class NativeThreadImpl {
    /**
     * @see sun.management.ThreadImpl#getThreads()
     */
    private static Thread[] getThreads() {
        final ArrayList<Thread> tl = new ArrayList<Thread>();
        VmUtils.getVm().accept(new VmThreadVisitor() {
            @Override
            public boolean visit(VmThread thread) {
                tl.add(thread.asThread());
                return true;
            }
        });
        return tl.toArray(new Thread[tl.size()]);
    }

    /**
     * @see sun.management.ThreadImpl#getThreadInfo0(long[], int, java.lang.management.ThreadInfo[])
     */
    private static void getThreadInfo0(long[] arg1, int arg2, ThreadInfo[] arg3) {
        //todo implement it
    }
    /**
     * @see sun.management.ThreadImpl#getThreadTotalCpuTime0(long)
     */
    private static long getThreadTotalCpuTime0(long arg1) {
        //todo add CPU time mesurement support
        throw new UnsupportedOperationException();
    }
    /**
     * @see sun.management.ThreadImpl#getThreadUserCpuTime0(long)
     */
    private static long getThreadUserCpuTime0(long arg1) {
        //todo add CPU time mesurement support
        throw new UnsupportedOperationException();
    }
    /**
     * @see sun.management.ThreadImpl#setThreadCpuTimeEnabled0(boolean)
     */
    private static void setThreadCpuTimeEnabled0(boolean arg1) {
        //todo add CPU time mesurement support
        throw new UnsupportedOperationException();
    }
    /**
     * @see sun.management.ThreadImpl#setThreadContentionMonitoringEnabled0(boolean)
     */
    private static void setThreadContentionMonitoringEnabled0(boolean arg1) {
        //todo add thread contention monitoring support
        throw new UnsupportedOperationException();
    }
    /**
     * @see sun.management.ThreadImpl#findMonitorDeadlockedThreads0()
     */
    private static Thread[] findMonitorDeadlockedThreads0() {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.ThreadImpl#findDeadlockedThreads0()
     */
    private static Thread[] findDeadlockedThreads0() {
        //todo implement it
        return null;
    }
    /**
     * @see sun.management.ThreadImpl#resetPeakThreadCount0()
     */
    private static void resetPeakThreadCount0() {
        //todo implement it
    }
    /**
     * @see sun.management.ThreadImpl#dumpThreads0(long[], boolean, boolean)
     */
    private static ThreadInfo[] dumpThreads0(long[] arg1, boolean arg2, boolean arg3) {
        //todo implement it
        return new ThreadInfo[0];
    }
    /**
     * @see sun.management.ThreadImpl#resetContentionTimes0(long)
     */
    private static void resetContentionTimes0(long arg1) {
        //todo implement it
    }
}
