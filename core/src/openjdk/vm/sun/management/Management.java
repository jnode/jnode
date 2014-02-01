package sun.management;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import org.jnode.vm.facade.VmThread;
import org.jnode.vm.facade.VmThreadVisitor;
import org.jnode.vm.facade.VmUtils;

/**
 * JNode implementations of native methods for package sun.management.
 * This class is a delegate for all sun.management.Native* classes.
 *
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
class Management {
    static final Thread[] getThreads(final boolean onlyDeadLocked, final boolean concurrentLocks) {
        final ArrayList<VmThread> vmThreads = new ArrayList<VmThread>();
        final List<VmThread> deadLockCycle = onlyDeadLocked ? new ArrayList<VmThread>() : null;
        VmUtils.getVm().accept(new VmThreadVisitor() {
            @Override
            public boolean visit(VmThread thread) {
                if (onlyDeadLocked) {
                    if (!vmThreads.contains(thread)) {
                        thread.detectDeadlock(deadLockCycle, concurrentLocks); //deadLockCycle will be first cleared
                        for (VmThread t : deadLockCycle) {
                            if (t == null) {
                                continue;
                            }
                            vmThreads.add(t);
                        }
                    }
                } else {
                    vmThreads.add(thread);
                }

                return true;
            }
        });

        Thread[] result = null;
        if (!vmThreads.isEmpty()) {
            result = new Thread[vmThreads.size()];
            for (int i = 0; i < vmThreads.size(); i++) {
                result[i] = vmThreads.get(i).asThread();
            }
        }
        return result;
    }

    /**
     * @param ids                 Array of thread identifiers or null for all threads.
     * @param lockedMonitors
     * @param lockedSynchronizers
     * @param infos               Array of thread informations to fill or null for all threads.
     * @return Array filled thread informations. If <code>infos</code> is null, this is a new array with informations on threads;
     * else it's the array given by <code>infos</code> parameter.
     * @params maxDepth The maximal depth of the stack dump (-1 means entire stack; 0 means no stacktrace).
     */
    static final ThreadInfo[] fillThreadInfos(final long[] ids, final boolean lockedMonitors,
                                              final boolean lockedSynchronizers,
                                              final int maxDepth, final ThreadInfo[] infos) {
        final boolean allThreads = (ids == null);
        final List<ThreadInfo> allInfos = allThreads ? new ArrayList<ThreadInfo>() : null;

        VmUtils.getVm().accept(new VmThreadVisitor() {
            int count;

            @Override
            public boolean visit(VmThread thread) {
                if (allThreads) {
                    allInfos.add(thread.getThreadInfo(lockedMonitors, lockedSynchronizers, maxDepth, false, null));
                } else {
                    // optimisation : since VmThread#getThreadInfo iterates over all heaps objects and we call it many times,
                    // we use this cache that is filled at the first call.
                    List<AbstractOwnableSynchronizer> cachedOwnableSynchronizers =
                        lockedSynchronizers ? new ArrayList<AbstractOwnableSynchronizer>() : null;

                    for (int i = 0; i < ids.length; i++) {
                        if ((infos[i] == null) && (thread.getId() == ids[i])) {
                            infos[i] = thread.getThreadInfo(lockedMonitors, lockedSynchronizers, maxDepth, (count > 0),
                                cachedOwnableSynchronizers);
                            count++;
                            break;
                        }
                    }
                }

                return allThreads || (count < infos.length);
            }
        });

        return allThreads ? allInfos.toArray(new ThreadInfo[allInfos.size()]) : infos;
    }
}
