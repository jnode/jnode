/*
 * $Id$
 *
 * Copyright (C) 2003-2009 JNode.org
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
 
package org.jnode.vm.scheduler;

import org.jnode.util.NumberUtils;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmMagic;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.ObjectLayout;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Word;

/**
 * @author epr
 */
@MagicPermission
@Uninterruptible
public final class MonitorManager {

    /**
     * A fast implementation of the monitorEnter opcode. This implementation is
     * based on a thin-lock, present if the status word of the header of each
     * object.
     *
     * @param object
     */
    public static void monitorEnter(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }

        // Prepare
        final Word tid = Word.fromIntZeroExtend(VmMagic.currentProcessor().getCurrentThread().getId());
        final Address objectPtr = ObjectReference.fromObject(object).toAddress();
        final Address statusPtr = objectPtr.add(ObjectLayout.FLAGS_SLOT * Address.size());

        for (;;) {
            // attempt fast path: object is not locked.
            final Word oldlockword = statusPtr.prepareWord();

            final Word statusFlags = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.STATUS_FLAGS_MASK));
            if (statusPtr.attempt(statusFlags, statusFlags.or(tid))) {
                // fast path succeeded, the object was not locked and
                // has been locked by the current thread.
                return;
            }

            // object is locked or has an inflated lock.
            if (!oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED)).isZero()) {
                // slow path 2: high bit of lock word is set --> inflated lock
                final Monitor m = getMonitor(oldlockword);
                m.enter();
                return;
            } else if (oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.THREAD_ID_MASK)).EQ(tid)) {
                // Current thread owns the thinlock
                final Word counter = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_COUNT_MASK));
                if (counter.EQ(Word.fromIntZeroExtend(ObjectFlags.LOCK_COUNT_MASK))) {
                    // thin lock entry counter == max, so we need to inflate
                    // ourselves.
                    installInflatedLock(object, null).enter();
                    return;
                } else {
                    // not-quite-so-fast path: locked by current thread.
                    // increment counter.
                    // Try to update lock, it may be inflated by some other
                    // thread, so
                    // be cautious
                    final Word newlockword;
                    newlockword = oldlockword.add(Word.fromIntZeroExtend(ObjectFlags.LOCK_COUNT_INC));
                    // Try to update lock, it may be inflated by some other
                    // thread, so
                    // be cautious
                    if (statusPtr.attempt(oldlockword, newlockword)) {
                        return;
                    }
                }
            } else {
                // Another thread owns the lock
                // thin lock owned by another thread.
                int ownerId = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.THREAD_ID_MASK)).toInt();
                VmThread thread = VmMagic.currentProcessor().getScheduler().getThreadById(ownerId);
                if (thread == null) {
                    //the owner of the lock was destroyed               
                    //aquire the lock in fast fashion
                    statusPtr.store(statusFlags.or(tid));
                    return;
                } else {
                    // install an inflated lock.
                    installInflatedLock(object, thread).enter();
                }
                return;
            }

            Unsafe.debug("@monitorEnter loop@");
        }
    }

    /**
     * Monitorexit runtime routine. Checks for thin lock usage, otherwise falls
     * back to inflated locks.
     *
     * @param object
     */
    public static void monitorExit(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }

        // Prepare
        final Word tid = Word.fromIntZeroExtend(VmMagic.currentProcessor().getCurrentThread().getId());
        final Address objectPtr = ObjectReference.fromObject(object).toAddress();
        final Address statusPtr = objectPtr.add(ObjectLayout.FLAGS_SLOT * Address.size());

        for (;;) {
            final Word oldlockword = statusPtr.prepareWord();

            // Unsafe.debug(" exit:");
            // proc.getArchitecture().getStackReader().debugStackTrace();
            // Unsafe.debug(oldlockword); Unsafe.debug(tid); Unsafe.debug('}');

            // if not inflated and tid matches, this contains status flags and
            // counter
            if (!oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED)).isZero()) {
                // inflated lock
                final Monitor m = getMonitor(oldlockword);
                m.exit();
                return;
            } else if (oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.THREAD_ID_MASK)).EQ(tid)) {
                // owned by current thread
                final Word counter = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_COUNT_MASK));
                final Word newlockword;
                if (counter.isZero()) {
                    // Count is zero, clear tid field
                    newlockword = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.STATUS_FLAGS_MASK));
                } else {
                    // count is non-zero, decrement count
                    newlockword = oldlockword.sub(Word.fromIntSignExtend(ObjectFlags.LOCK_COUNT_INC));
                }
                if (statusPtr.attempt(oldlockword, newlockword)) {
                    return;
                }
            } else {
                // lock not owned by us!
                String exMsg = "Lock not owned by us:";
                Unsafe.debug(exMsg);
                Unsafe.debug(object.getClass().getName());
                // Extra debug info
                Unsafe.debug("\n");
                Unsafe.debug(oldlockword);
                Unsafe.debug(objectPtr);
                Unsafe.debug(statusPtr);
                Unsafe.debug(oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_COUNT_MASK)));
                Unsafe.debug(tid);
                Unsafe.debug(statusPtr.prepareWord());
                VmMagic.currentProcessor().getArchitecture().getStackReader().debugStackTrace();
                Unsafe.die("Please report this problem with the above values to epr@jnode.org");
                throw new IllegalMonitorStateException(exMsg);
            }
        }
    }

    /**
     * Wait on the given object, until interrupted or notified
     *
     * @param object
     * @param timeout
     * @throws InterruptedException
     */
    public static void wait(Object object, long timeout) throws InterruptedException {
        // Test interrupted state of current thread
        if (VmThread.currentThread().isInterrupted(true)) {
            throw new InterruptedException();
        }

        getOwnedInflatedMonitor(object).Wait(timeout);
    }

    /**
     * Notify the first thread waiting on the given object
     *
     * @param object
     */
    public static void notify(Object object) {
        getOwnedInflatedMonitor(object).Notify(false);
    }

    /**
     * Notify all threads waiting on the given object
     *
     * @param object
     */
    public static void notifyAll(Object object) {
        getOwnedInflatedMonitor(object).Notify(true);
    }

    /**
     * Gets the inflated monitor of the given object. The object must have been
     * locked by the current thread, otherwise an IllegalMonitorStateException
     * is thrown.
     *
     * @param object
     * @return The monitor
     * @throws IllegalMonitorStateException
     * @throw IllegalMonitorStateException
     */
    private static Monitor getOwnedInflatedMonitor(Object object)
        throws IllegalMonitorStateException {
        final Monitor m = installInflatedLock(object, null);
        if (!m.isOwner(VmMagic.currentProcessor().getCurrentThread())) {
            // lock not owned by us!
            String exMsg = "Object not locked by current thread";
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
        }
        return m;
    }

    /**
     * Make sure that object K has an inflated monitor.
     *
     * @param k
     */
    static void setupInflatedLock(Object k) {
        installInflatedLock(k, null);
    }

    /**
     * Installs an inflated lock on the given object. Uses a spin-loop to wait
     * until the object is unlocked or inflated.
     *
     * @param k the object for which the inflated lock is installed
     * @param thread
     * @return the Monitor object representing the lock
     */
    private static Monitor installInflatedLock(Object k, VmThread thread) {
        Monitor m = null;
        Word monAddr = null;

        final Address kPtr = ObjectReference.fromObject(k).toAddress();
        final Address statusPtr = kPtr.add(ObjectLayout.FLAGS_SLOT * Address.size());

        for (;;) {
            final Word oldlockword = statusPtr.prepareWord();
            if (!oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED)).isZero()) {
                // inflated by another thread, use that one.
                return getMonitor(oldlockword);
            }

            if (m == null) {
                m = new Monitor(VmMagic.currentProcessor().getCurrentThread(), 1);
                monAddr = ObjectReference.fromObject(m).toAddress().toWord();
                if (!monAddr.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED
                    | ObjectFlags.STATUS_FLAGS_MASK)).isZero()) {
                    throw new InternalError("Monitor object has address that conflicts with header flags 0x"
                        + NumberUtils.hex(monAddr.toInt()));
                }
            }

            // Put entry count & owner in monitor
            int lockcount = 1 + oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_COUNT_MASK)).
                rshl(ObjectFlags.LOCK_COUNT_SHIFT).toInt();
            int ownerId = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.THREAD_ID_MASK)).toInt();
            if (thread == null) {
                thread = VmMagic.currentProcessor().getScheduler().getThreadById(ownerId);
            }
            m.initialize(thread, lockcount);

            final Word statusFlags = oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.STATUS_FLAGS_MASK));
            final Word newlockword = monAddr.or(statusFlags).or(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED));
            if (statusPtr.attempt(oldlockword, newlockword)) {
                // successfully obtained inflated lock.
                return m;
            }
        }
    }

    /**
     * Get the Monitor object associated with this lockword.
     *
     * @param lockword
     * @return The monitor
     */
    private static Monitor getMonitor(Word lockword) {
        final Address address = lockword.and(Word.fromIntZeroExtend(
            ~(ObjectFlags.LOCK_EXPANDED | ObjectFlags.STATUS_FLAGS_MASK))).toAddress();

        if (address.isZero()) {
            String exMsg = "Inflated monitor is null????";
            Unsafe.debug(exMsg);
            throw new IllegalMonitorStateException(exMsg);
        }
        return (Monitor) address.toObjectReference().toObject();
    }

    /**
     * Gets the inflated monitor of an object (if any).
     *
     * @param object
     * @return The inflated monitor of the given object, or null if the given
     *         object has no inflated monitor.
     */
    @Internal
    public static Monitor getInflatedMonitor(Object object) {
        final Word oldlockword = ObjectReference.fromObject(object).toAddress().
            add(ObjectLayout.FLAGS_SLOT * Address.size()).loadWord();

        if (!oldlockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED)).isZero()) {
            return getMonitor(oldlockword);
        } else {
            return null;
        }
    }

    /**
     * Checks whether the current thread holds the monitor on a given object.
     * This allows you to do <code>assert Thread.holdsLock(obj)</code>.
     *
     * @param obj the object to test lock ownership on.
     * @return true if the current thread is currently synchronized on obj
     * @throws NullPointerException if obj is null
     * @since 1.4
     */
    public static boolean holdsLock(Object obj) {
        final VmThread current = VmThread.currentThread();

        final Word lockword = ObjectReference.fromObject(obj).toAddress().
            add(ObjectLayout.FLAGS_SLOT * Address.size()).prepareWord();

        if (!lockword.and(Word.fromIntZeroExtend(ObjectFlags.LOCK_EXPANDED)).isZero()) {
            return getMonitor(lockword).isOwner(current);
        } else {
            final Word tid = Word.fromIntZeroExtend(current.getId());
            return lockword.and(Word.fromIntZeroExtend(ObjectFlags.THREAD_ID_MASK)).EQ(tid);
        }
    }

    /**
     * Make sure the given thread id does fit into the space reserved for it by
     * the thinlock stuff.
     *
     * @param tid
     */
    static void testThreadId(int tid) {
        if ((tid & ~ObjectFlags.THREAD_ID_MASK) != 0) {
            throw new InternalError("Invalid thread id " + tid);
        }
        if ((tid & ObjectFlags.THREAD_ID_MASK) == 0) {
            throw new InternalError("Invalid thread id " + tid);
        }
    }
}
