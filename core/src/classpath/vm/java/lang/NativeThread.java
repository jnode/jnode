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
 
package java.lang;

import org.jnode.vm.scheduler.VmThread;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.MonitorManager;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.VmSystem;

/**
 *
 */
class NativeThread {
    private static void checkArg0(Object vmThread) {
        if (((VmThread) vmThread).hasJavaThread()) {
            throw new IllegalArgumentException("vmThread has already a java thread associated with it");
        }
    }

    private static Object createVmThread0(Thread instance, Thread current) {
        return VmProcessor.current().createThread(instance);
    }

    private static Object createVmThread1(Thread instance, Object isolatedStatics) {
        return VmProcessor.current().createThread((VmIsolatedStatics) isolatedStatics, instance);
    }

    private static void updateName0(Thread instance) {
        ((VmThread) instance.vmThread).updateName();
    }

    private static int countStackFrames(Thread instance) {
        return ((VmThread) instance.vmThread).countStackFrames();
    }

    private static void destroy(Thread instance) {
        ((VmThread) instance.vmThread).destroy();
    }

    private static final int getPriority(Thread instance) {
        return ((VmThread) instance.vmThread).getPriority();
    }

    private static void interrupt(Thread instance) {
        ((VmThread) instance.vmThread).interrupt();
    }

    private static boolean isInterrupted(Thread instance) {
        return ((VmThread) instance.vmThread).isInterrupted(false);
    }

    private static final boolean isAlive(Thread instance) {
        return ((VmThread) instance.vmThread).isAlive();
    }

    private static boolean isInterupted0(Thread instance) {
        return ((VmThread) instance.vmThread).isInterrupted(true);
    }

    private static long currentKernelMillis0() {
        return VmSystem.currentKernelMillis();
    }

    private static final void resume(Thread instance) {
        ((VmThread) instance.vmThread).resume();
    }

    private static void start0(Thread instance) {
        ((VmThread) instance.vmThread).start();
    }

    private static final void stop(Thread instance) {
        ((VmThread) instance.vmThread).stop(new ThreadDeath());
    }

    private static void stop0(Thread instance, Throwable t) {
        ((VmThread) instance.vmThread).stop(t);
    }

    private static final void suspend(Thread instance) {
        ((VmThread) instance.vmThread).suspend();
    }

    private static final void setPriority(Thread instance, int priority) {
        ((VmThread) instance.vmThread).setPriority(priority);
    }

    private static final boolean isRunning(Thread instance) {
        VmThread vmt = (VmThread) instance.vmThread;
        return vmt.isRunning() || vmt.isYielding();
    }

    private static boolean isWaiting(Thread instance) {
        return ((VmThread) instance.vmThread).isWaiting();
    }

    private static boolean isStopping0(Thread instance) {
        return ((VmThread) instance.vmThread).isStopping();
    }

    private static long getId(Thread instance) {
        return ((VmThread) instance.vmThread).getId();
    }

    private static StackTraceElement[] getStackTrace0(Thread instance) {
        return NativeThrowable.backTrace2stackTrace(VmThread.getStackTrace((VmThread) instance.vmThread));
    }

    private static Thread currentThread() {
        VmThread current = VmThread.currentThread();
        if (current != null) {
            return current.asThread();
        } else {
            return null;
        }
    }

    private static boolean interrupted() {
        VmThread current = VmThread.currentThread();
        if (current != null) {
            return current.isInterrupted(true);
        } else {
            return false;
        }
    }

    private static void yield() {
        VmThread.yield();
    }

    private static void sleep0(long ms, int ns) throws InterruptedException {
        VmThread.currentThread().sleep(ms, ns);
    }

    private static boolean holdsLock(Object obj) {
        return MonitorManager.holdsLock(obj);
    }

    private static void die0() {
        org.jnode.vm.Unsafe.die("Root ThreadGroup creation failure.");
    }
}
