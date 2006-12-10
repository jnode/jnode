/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2003-2006 JNode.org
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

import java.io.PrintStream;

import org.jnode.util.NumberUtils;
import org.jnode.vm.CpuID;
import org.jnode.vm.MathSupport;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;
import org.jnode.vm.VmAddress;
import org.jnode.vm.VmArchitecture;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmSystem;
import org.jnode.vm.VmSystemObject;
import org.jnode.vm.annotation.Inline;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.LoadStatics;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.annotation.NoFieldAlignments;
import org.jnode.vm.annotation.Uninterruptible;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmSharedStatics;
import org.jnode.vm.compiler.GCMapIterator;
import org.jnode.vm.compiler.NativeCodeCompiler;
import org.jnode.vm.performance.PerformanceCounters;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.ObjectReference;
import org.vmmagic.unboxed.Word;

/**
 * Abstract processor wrapper.
 * 
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@NoFieldAlignments
@Uninterruptible
@MagicPermission
public abstract class VmProcessor extends VmSystemObject {

    /** The thread switch indicator KEEP THIS THE FIRST FIELD!!! */
    private volatile Word threadSwitchIndicator;

    /** Reference to myself; used in assembly code */
    final VmProcessor me;

    /** The current thread on this processor */
    protected volatile VmThread currentThread;

    /** The isolated statics table of the current thread (int[]) */
    private volatile Object isolatedStaticsTable;

    /** The isolated statics table of the current thread */
    private volatile VmIsolatedStatics isolatedStatics;

    /** The next thread to schedule on this processor */
    volatile VmThread nextThread;

    /** Stack end of current thread. This field is used by the native code. */
    protected volatile VmAddress stackEnd;

    /** Stack end of kernel stack. This field is used by the native code. */
    protected volatile Address kernelStackEnd;

    /** The identifier of this processor */
    private int id;

    /** The identifier of this processor as string */
    private String idString;

    /** IRQ manager for this processor */
    private IRQManager irqMgr;

    /** Address of threadSwitchIndicator */
    private Address tsiAddress;

    /** The scheduler that is used */
    private final VmScheduler scheduler;

    /** The architecture of this processor */
    private final VmArchitecture architecture;

    /** The idle thread */
    private IdleThread idleThread;

    private int lockCount;

    /** CPU identification */
    private transient CpuID cpuId;

    /** The statics table (int[]) */
    private volatile Object staticsTable;

    /** The processor speed indication */
    private float jnodeMips;

    private int lastThreadPriority;

    private int sameThreadPriorityCount;

    /** The data specific to this processor used by the heap manager */
    private final Object heapData;

    /** Per processor MathSupport memory structures */
    private final MathSupport mathSupport = new MathSupport();

    /**
     * Per processor GC map iterators. The index in the array is based on the
     * index in the VmArchitecture#getCompilers array.
     */
    private final GCMapIterator[] gcMapIterators;

    /**
     * Per processor native code compiler id. The index in the array is based on
     * the index in the VmArchitecture#getCompilers array.
     */
    private final int[] compilerIds;

    /** Indicate the a thread switch is needed */
    public static final int TSI_SWITCH_NEEDED = 0x0001;

    /** Indicate that the system is ready for thread switching */
    public static final int TSI_SYSTEM_READY = 0x0002;

    /** Indicate the a thread switch is in progress */
    public static final int TSI_SWITCH_ACTIVE = 0x0004;

    /** Indicate the a thread switch cannot occur */
    public static final int TSI_BLOCK_SWITCH = 0x0008;

    /** Indicate the a thread switch is requested */
    public static final int TSI_SWITCH_REQUESTED = TSI_SWITCH_NEEDED
            | TSI_SYSTEM_READY;

    /**
     * Get the processor that the current thread is running on.
     * 
     * @return
     */
    @Inline
    @org.jnode.vm.annotation.Uninterruptible
    @KernelSpace
    public static VmProcessor current() {
        return VmMagic.currentProcessor();
    }

    /**
     * Initialize this instance
     * 
     * @param id
     * @param architecture
     */
    public VmProcessor(int id, VmArchitecture architecture,
            VmSharedStatics sharedStatics, VmIsolatedStatics isolatedStatics,
            VmScheduler scheduler) {
        this.id = id;
        this.idString = formatId(id);
        this.me = this;
        this.architecture = architecture;
        this.scheduler = scheduler;
        this.staticsTable = sharedStatics.getTable();
        this.isolatedStatics = isolatedStatics;
        this.isolatedStaticsTable = isolatedStatics.getTable();
        this.currentThread = createThread(isolatedStatics);
        this.heapData = Vm.getHeapManager().createProcessorHeapData(this);

        final NativeCodeCompiler[] compilers = architecture.getCompilers();
        final int compilerCount = compilers.length;
        this.gcMapIterators = new GCMapIterator[compilerCount];
        this.compilerIds = new int[compilerCount];
        for (int i = 0; i < compilerCount; i++) {
            compilerIds[i] = compilers[i].getMagic();
            gcMapIterators[i] = compilers[i].createGCMapIterator();
        }
    }

    /**
     * Gets the architecture of this processor.
     * 
     * @return the architecture of this processor
     */
    @Inline
    @KernelSpace
    public final VmArchitecture getArchitecture() {
        return architecture;
    }

    /**
     * Gets the current thread on this processor
     * 
     * @return The current thread on this processor
     * @throws UninterruptiblePragma
     */
    @Inline
    @Uninterruptible
    public final VmThread getCurrentThread() {
        return currentThread;
    }

    /**
     * Gets the identifier of this processor.
     * 
     * @return Returns the id.
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Gets the identifier of this processor as string.
     * 
     * @return Returns the id.
     */
    @Uninterruptible
    @KernelSpace
    public final String getIdString() {
        return this.idString;
    }

    /**
     * Sets the id of this processor.
     * 
     * @param id
     */
    protected final void setId(int id) {
        this.id = id;
        this.idString = formatId(id);
    }

    /**
     * Create an ID string for the given id
     * 
     * @param id
     * @return
     */
    protected String formatId(int id) {
        return "0x" + NumberUtils.hex(id, 2);
    }

    /**
     * Block any yieldpoints on this processor.
     * 
     * FIXME: should not be public, but GC still uses it
     */
    @Inline
    @Internal
    @Uninterruptible
    public final void disableReschedule(boolean claimSchedulerLock) {
        getTSIAddress().atomicOr(Word.fromIntSignExtend(TSI_BLOCK_SWITCH));
        lockCount++;
        if (claimSchedulerLock) {
            scheduler.lock();
        }
    }

    /**
     * Unblock any yieldpoints on this processor.
     * 
     * FIXME: should not be public, but GC still uses it
     */
    @Inline
    @Internal
    @Uninterruptible
    public final void enableReschedule(boolean releaseSchedulerLock) {
        if (releaseSchedulerLock) {
            scheduler.unlock();
        }
        lockCount--;
        if (lockCount == 0) {
            getTSIAddress()
                    .atomicAnd(Word.fromIntSignExtend(~TSI_BLOCK_SWITCH));
        }
    }

    /**
     * This method is called by the generated yieldpoints if a threadswitch is
     * requested.
     */
    final void yieldPoint() {

    }

    /**
     * Is this processor busy switching threads.
     * 
     * @return true or false
     */
    public final boolean isThreadSwitchActive() {
        return (!threadSwitchIndicator.and(
                Word.fromIntZeroExtend(TSI_SWITCH_ACTIVE)).isZero());
    }

    /**
     * Give up the current cpu-time, and add the current thread to the back of
     * the ready queue.
     * 
     * @param ignorePriority
     *            If true, the thread is always added to the back of the list,
     *            regarding its priority.
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    final void yield(boolean ignorePriority) {
        final VmThread t = this.currentThread;
        t.setYieldingState();
        scheduler.addToReadyQueue(t, ignorePriority, "proc.yield");
        threadSwitchIndicator = threadSwitchIndicator.or(Word
                .fromIntZeroExtend(TSI_SWITCH_NEEDED));
        if (threadSwitchIndicator.NE(Word
                .fromIntZeroExtend(TSI_SWITCH_REQUESTED))) {
            Unsafe.debug("Yield with invalid tsi: " + threadSwitchIndicator);
            architecture.getStackReader().debugStackTrace();
            Unsafe.die("yield");
        }
        Unsafe.yieldPoint();
    }

    /**
     * Given the current cpu-time. The current thread is not added to any queue.
     * 
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    final void suspend(boolean releaseSchedulerLock) {
        if (lockCount != 1) {
            Unsafe.debug("Suspend with invalid lockCount: ");
            Unsafe.debug(lockCount);
            architecture.getStackReader().debugStackTrace();
            Unsafe.die("suspend");
        }
        final VmThread t = this.currentThread;
        t.setYieldingState();
        threadSwitchIndicator = threadSwitchIndicator.or(Word
                .fromIntZeroExtend(TSI_SWITCH_NEEDED));
        enableReschedule(releaseSchedulerLock);
        if (threadSwitchIndicator.NE(Word
                .fromIntZeroExtend(TSI_SWITCH_REQUESTED))) {
            Unsafe.debug("Suspend with invalid tsi: ");
            Unsafe.debug(threadSwitchIndicator);
            architecture.getStackReader().debugStackTrace(50);
            Unsafe.die("VmProcessor#suspend");
        }
        Unsafe.yieldPoint();
    }

    /**
     * This method is called by the timer interrupt with interrupts disabled.
     * Keep this method as short and as fast as possible!
     * 
     * @throws UninterruptiblePragma
     */
    @LoadStatics
    @KernelSpace
    @Uninterruptible
    final void reschedule() {
        // Unsafe.debug("R");
        this.nextThread = null;
        try {
            // Get the current thread
            final VmThread current = currentThread;
            this.nextThread = current;

            // Process kernel debugger data
            if (Unsafe.isKdbEnabled()) {
                scheduler.processAllKdbInput();
            }

            // Dispatch interrupts if we already have an IRQ manager.
            final IRQManager irqMgr = this.irqMgr;
            if (irqMgr != null) {
                irqMgr.dispatchInterrupts(current);
            }

            // Add the current thread to the ready queue, if the state
            // is running.
            if (current.isRunning()) {
                scheduler.addToReadyQueue(current, false, getIdString());
            } else {
                // Screen.debug("<Non-running thread in reschedule "+
                // current.getThreadState() + "
                // called "+ current.asThread().getName() + "/>");
                // Unsafe.die();
            }

            // Determine the new thread.

            // Should we wakeup a sleeping thread?
            VmThread newThread = scheduler.popFirstSleepingThread();

            // Take the first thread from the ready queue
            if (newThread == null) {
                newThread = scheduler.popFirstReadyThread();
            }

            // It no other thread want to run, that means that the idle thread
            // has been halted, we give up.
            if (newThread == null) {
                Unsafe.debug("No Threads to run!\n");
                scheduler.dump();
                Unsafe.die("No thread to run in reschedule");
            }

            newThread.wakeUpByScheduler();
            this.nextThread = newThread;

            final int priority = newThread.priority;
            if (priority == lastThreadPriority) {
                sameThreadPriorityCount++;
                if ((priority > Thread.NORM_PRIORITY)
                        && ((sameThreadPriorityCount % 2500) == 0)) {
                    Unsafe.debug("Maybe stuck in high priority: ");
                    Unsafe.debug(newThread.getName());
                    if (sameThreadPriorityCount > 100000) {
                        getArchitecture().getStackReader().debugStackTrace(
                                current);
                        Unsafe.die("Probably deadlock in high priority thread");
                    }
                }
            } else {
                lastThreadPriority = priority;
                sameThreadPriorityCount = 0;
            }
        } catch (Throwable ex) {
            try {
                ex.printStackTrace();
            } catch (Throwable ex2) {
                Unsafe.debug("Exception in Exception in Scheduler");
                /* Ignore */
            }
            Unsafe.die("Exception in reschedule");
        }
    }

    /**
     * Create a new thread
     * 
     * @return The new thread
     */
    protected abstract VmThread createThread(VmIsolatedStatics isolatedStatics);

    /**
     * Create a new thread
     * 
     * @param javaThread
     * @return The new thread
     */
    public final VmThread createThread(Thread javaThread) {
        return createThread(getIsolatedStatics(), javaThread);
    }

    /**
     * Create a new thread
     * 
     * @param javaThread
     * @return The new thread
     */
    public abstract VmThread createThread(VmIsolatedStatics isolatedStatics,
            Thread javaThread);

    /**
     * Gets the IRQ counters array.
     * 
     * @return The irq counter array
     */
    @KernelSpace
    @Uninterruptible
    protected abstract int[] getIrqCounters();

    /**
     * Mark the system are ready for thread switching
     */
    @Internal
    public final void systemReadyForThreadSwitch() {
        if (idleThread == null) {
            idleThread = new IdleThread();
            idleThread.start();
        }
        getTSIAddress().atomicOr(Word.fromIntSignExtend(TSI_SYSTEM_READY));
    }

    /**
     * Gets the address of the threadSwitchIndicator field in this object. It is
     * assumed the this field is the first field of this class!
     * 
     * @return The address of the thread switch indicator
     */
    protected final Address getTSIAddress() {
        if (tsiAddress.isZero()) {
            tsiAddress = ObjectReference.fromObject(this).toAddress();
        }
        return tsiAddress;
    }

    /**
     * Gets my IRQ manager.
     * 
     * @return The irq manager
     */
    public final synchronized IRQManager getIRQManager() {
        if (irqMgr == null) {
            irqMgr = architecture.createIRQManager(this);
        }
        return irqMgr;
    }

    /**
     * Gets the CPU identification of this processor.
     * 
     * @return The CPU id.
     */
    public final CpuID getCPUID() {
        if (cpuId == null) {
            final int length = Unsafe.getCPUID(null);
            final int[] id = new int[length];
            Unsafe.getCPUID(id);
            cpuId = loadCPUID(id);
        }
        return cpuId;
    }

    /**
     * Load the CPU id.
     * 
     * @param id
     *            The idenfication returned by Unsafe.getCpuID
     * @return CpuID
     */
    protected abstract CpuID loadCPUID(int[] id);

    /**
     * Set the CPU id.
     * 
     * @param id
     *            The new cpu id
     */
    protected final void setCPUID(CpuID id) {
        this.cpuId = id;
    }

    /**
     * @return Returns the shared statics table (int[]).
     */
    @Inline
    @Internal
    public final Object getSharedStaticsTable() {
        return this.staticsTable;
    }

    /**
     * @return Returns the isolated statics table (int[]).
     */
    @Inline
    @Internal
    public final Object getIsolatedStaticsTable() {
        return this.isolatedStaticsTable;
    }

    /**
     * @param staticsTable
     *            The staticsTable to set.
     */
    final void setStaticsTable(Object staticsTable) {
        this.staticsTable = staticsTable;
    }

    /**
     * Calculate the processor speed and delay loops.
     */
    @Internal
    public final void calibrate() {
        this.jnodeMips = VmSystem.calculateJNodeMips();
        final String num = NumberUtils.toString(jnodeMips, 2);
        System.out.println("Processor " + getId() + ": " + getCPUID().getName()
                + ", " + num + " JNodeMIPS");
    }

    /**
     * Gets the processor speed indication.
     * 
     * @return
     */
    public final float getJNodeMips() {
        return jnodeMips;
    }

    /**
     * Print statistics information on the given stream.
     * 
     * @param out
     */
    public abstract void dumpStatistics(PrintStream out);

    /**
     * Gets the isolates statics table of the current thread.
     * 
     * @return Returns the isolatedStatics.
     */
    public final VmIsolatedStatics getIsolatedStatics() {
        return isolatedStatics;
    }

    /**
     * Gets the processor specific heap data.
     * 
     * @return Returns the heapData.
     */
    @Inline
    public final Object getHeapData() {
        return heapData;
    }

    /**
     * Gets the GC map iterator for the given native code compiler.
     * 
     * @param compiler
     * @return
     */
    @Inline
    public final GCMapIterator getGCMapIterator(NativeCodeCompiler compiler) {
        final int magic = compiler.getMagic();
        for (int i = 0;; i++) {
            if (compilerIds[i] == magic) {
                return gcMapIterators[i];
            }
        }
    }

    /**
     * @return Returns the mathSupport.
     */
    @Inline
    @Internal
    public final MathSupport getMathSupport() {
        return mathSupport;
    }

    /**
     * Gets the performance counter accessor of this processor.
     * 
     * @return
     */
    public abstract PerformanceCounters getPerformanceCounters();

    /**
     * @param isolatedStatics
     *            the isolatedStatics to set
     */
    final void setIsolatedStatics(VmIsolatedStatics isolatedStatics) {
        final Object table = isolatedStatics.getTable();
        this.isolatedStatics = isolatedStatics;
        this.isolatedStaticsTable = table;
    }

    /**
     * @return the idleThread
     */
    protected final IdleThread getIdleThread() {
        return idleThread;
    }

    /**
     * @return the scheduler
     */
    @Uninterruptible
    @KernelSpace
    public final VmScheduler getScheduler() {
        return scheduler;
    }
}
