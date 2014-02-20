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

package org.jnode.vm.scheduler;

import java.lang.management.ThreadInfo;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.LockSupport;
import org.jnode.annotation.Inline;
import org.jnode.annotation.Internal;
import org.jnode.annotation.KernelSpace;
import org.jnode.annotation.LoadStatics;
import org.jnode.annotation.MagicPermission;
import org.jnode.annotation.PrivilegedActionPragma;
import org.jnode.annotation.SharedStatics;
import org.jnode.annotation.Uninterruptible;
import org.jnode.util.NumberUtils;
import org.jnode.vm.NativeHelper;
import org.jnode.vm.Unsafe;
import org.jnode.vm.VmAccessControlContext;
import org.jnode.vm.VmAccessController;
import org.jnode.vm.VmExit;
import org.jnode.vm.VmIOContext;
import org.jnode.vm.VmImpl;
import org.jnode.vm.VmMagic;
import org.jnode.vm.VmReflection;
import org.jnode.vm.VmStackFrame;
import org.jnode.vm.VmStackReader;
import org.jnode.vm.VmSystem;
import org.jnode.vm.classmgr.ObjectFlags;
import org.jnode.vm.classmgr.VmField;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.facade.ObjectVisitor;
import org.jnode.vm.facade.VmHeapManager;
import org.jnode.vm.facade.VmUtils;
import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.objects.VmSystemObject;
import org.vmmagic.pragma.UninterruptiblePragma;
import org.vmmagic.unboxed.Address;

import static java.lang.Thread.State.BLOCKED;
import static java.lang.Thread.State.NEW;
import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.WAITING;

/**
 * VM thread implementation
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Fabien DUMINY (fduminy at jnode.org)
 */
@SharedStatics
@MagicPermission
public abstract class VmThread extends VmSystemObject implements org.jnode.vm.facade.VmThread {

    /**
     * If the stackpointer grows to this distance from the size of the stack, a
     * stack overflow exception is raised.
     */
    public static final int STACK_OVERFLOW_LIMIT_SLOTS = 256;

    public static final int DEFAULT_STACK_SLOTS = 16 * 1024;

    public static final int STACKTRACE_LIMIT = 256;

    public static final int EX_NULLPOINTER = 0;

    public static final int EX_PAGEFAULT = 1;

    public static final int EX_INDEXOUTOFBOUNDS = 2;

    public static final int EX_DIV0 = 3;

    public static final int EX_ABSTRACTMETHOD = 4;

    public static final int EX_STACKOVERFLOW = 5;

    // public static final int EX_CLASSCAST = 6;
    public static final int EX_COPRO_OR = 7;

    public static final int EX_COPRO_ERR = 8;

    /**
     * A link to my java.lang.Thread
     */
    private Thread javaThread;

    /**
     * Next pointer used in queues
     */
    protected final VmThreadQueueEntry queueEntry = new VmThreadQueueEntry(this);

    /**
     * Next pointer used in list of sleeping threads
     */
    protected final VmThreadQueueEntry sleepQueueEntry = new VmThreadQueueEntry(
        this);

    /**
     * Next pointer used in list of all threads
     */
    protected final VmThreadQueueEntry allThreadsEntry = new VmThreadQueueEntry(
        this);

    /**
     * The size (in bytes) of the stack of this thread
     */
    private int stackSize;

    /**
     * The pointer to the stack
     */
    private Object stack;

    /**
     * The pointer to end of the stack (used by native code)
     */
    protected volatile Address stackEnd;

    /**
     * Has this thread had a stackoverflow?
     */
    private boolean stackOverflow;

    /**
     * The current state of this thread
     */
    private int threadState = CREATED;

    /**
     * When to wakeup (if sleeping)
     */
    protected long wakeupTime;

    /**
     * The monitor i'm waiting for
     */
    private Monitor waitForMonitor;

    /**
     * The most recently owned monitor.
     */
    private Monitor lastOwnedMonitor;

    /**
     * My priority
     */
    protected int priority = Thread.NORM_PRIORITY;

    /**
     * Identifier of this thread
     */
    private final int id;

    /**
     * Identifier of the last created thread
     */
    private static int lastId;

    /**
     * Has this thread been interrupted?
     */
    private boolean interrupted;

    /**
     * Is this thread in an exception initialization?
     */
    boolean inException;

    /**
     * Is this thread in the process of being stopped?
     */
    private boolean stopping;

    /**
     * Name of this thread.
     */
    private String name;

    /**
     * Inherited context of this thread
     */
    private VmAccessControlContext context;

    private boolean inSystemException;

    /**
     * The isolated statics table of the isolate that this thread is currently
     * running is
     */
    private volatile VmIsolatedStatics isolatedStatics;

    /**
     * The processor that is required to run this thread. Null means no specific
     * requirements
     */
    private volatile VmProcessor requiredProcessor;

    /**
     * The processor currently at work on this thread
     */
    volatile VmProcessor currentProcessor;

    /**
     * Number of actual locked objects (used by javax.management API).
     */
    private int nbLockedObjects = 0;

    /**
     * Locked objects (used by javax.management API).
     * The actual number of elements is given by {@link #nbLockedObjects}.
     */
    private final Object[] lockedObjects = new Object[256];

    /**
     * {@link org.jnode.vm.classmgr.VmMethod}s where objects were locked (used by javax.management API).
     * The actual number of elements is given by {@link #nbLockedObjects}.
     */
    private final VmMethod[] lockVmMethods = new VmMethod[lockedObjects.length];
//    private final int[] lockProgCounters = new int[lockedObjects.length];

    /**
     * State is set to CREATED by the static initializer. Once set to other than
     * CREATED, it should never go back. Alternates between RUNNING and
     * SUSPENDED/WAITING as suspend()/wait() and reseume() are called.
     * <p/>
     * Can be set to INTERRUPTED if ASLEEP, or WAITING. Can be set to DESTROYED
     * at any time. Can be set to STOPPED at any time.
     * <p/>
     * If a Thread blocks in a wait(), its state stays as RUNNING. This might
     * need to change.
     * <p/>
     * Can sleep if RUNNING. <blockquote>
     * <p/>
     * <pre>
     *    /--------------------\ V | CREATED -&gt; RUNNING -&gt; SUSPENDED /
     *    &lt;/blockquote&gt;
     * </pre>
     */
    static final int CREATED = 0;

    static final int ASLEEP = 3;

    static final int DESTROYED = 5;

    static final int RUNNING = 1;

    static final int STOPPED = 4;

    static final int SUSPENDED = 2;

    static final int WAITING_ENTER = 6;

    static final int WAITING_NOTIFY = 7;

    static final int WAITING_NOTIFY_TIMEOUT = 8;

    static final int YIELDING = 9;

    static final int MAXSTATE = YIELDING;

    static final String[] STATE_NAMES = {"CREATED", "RUNNING", "SUSPENDED",
        "ASLEEP", "STOPPED", "DESTROYED", "WAITING_ENTER",
        "WAITING_NOTIFY", "WAITING_NOTIFY_TIMEOUT", "YIELDING"};

    /**
     * VmMethod instance for {@link java.util.concurrent.locks.AbstractOwnableSynchronizer#getExclusiveOwnerThread()}.
     */
    private static VmMethod aosGetExclusiveOwnerThread;

    /**
     * VmField instance for {@link java.lang.Thread#vmThread}.
     */
    private static VmField threadVmThread;

    /**
     * VmMethod instance for {@link java.lang.management.ThreadInfo#ThreadInfo(Thread, int, Object, Thread, long, long, long, long, StackTraceElement[], Object[], int[], Object[])}.
     */
    private static VmMethod tiConstructor;

    /**
     * Create a new instance. This constructor can only be called during the
     * bootstrap phase.
     */
    protected VmThread(VmIsolatedStatics isolatedStatics, int slotSize) {
        this.threadState = RUNNING;
        this.stackSize = DEFAULT_STACK_SLOTS * slotSize;
        this.id = (1 << ObjectFlags.THREAD_ID_SHIFT);
        MonitorManager.testThreadId(this.id);
        this.isolatedStatics = isolatedStatics;
        getScheduler().registerThread(this);
    }

    /**
     * Create a new instance. This constructor can only be called during the
     * bootstrap phase.
     */
    protected VmThread(VmIsolatedStatics isolatedStatics, Object stack,
                       Address stackEnd, int stackSize) {
        this.isolatedStatics = isolatedStatics;
        this.threadState = RUNNING;
        this.stackSize = stackSize;
        this.stack = stack;
        this.stackEnd = stackEnd;
        this.id = createId();
        MonitorManager.testThreadId(this.id);
    }

    /**
     * Create a new instance.
     *
     * @param javaThread
     */
    public VmThread(VmIsolatedStatics isolatedStatics, Thread javaThread) {
        this.isolatedStatics = isolatedStatics;
        this.javaThread = javaThread;
        this.threadState = CREATED;
        this.stackSize = DEFAULT_STACK_SLOTS * VmUtils.getVm().getArch().getReferenceSize();
        this.id = createId();
        MonitorManager.testThreadId(this.id);
        this.context = VmAccessController.getContext();
    }

    /**
     * Return the current thread
     *
     * @return The current thread
     */
    public static VmThread currentThread() {
        return VmMagic.currentProcessor().getCurrentThread();
    }

    /**
     * Initialize the threading system.
     */
    @Internal
    public static void initialize() {
        // Ensure that we have a java.lang.Thread object for the root thread.
        final VmThread currentThread = currentThread();
        currentThread.asThread();
        lastId = currentThread.id;
    }

    /**
     * Count the number of stackframes in this thread.
     *
     * @return int
     */
    public final int countStackFrames() {
        final VmProcessor proc = VmProcessor.current();
        final VmStackReader reader = proc.getArchitecture().getStackReader();
        return reader.countStackFrames(VmMagic.getCurrentFrame());
    }

    /**
     * {@inheritDoc}
     */
    public final Thread asThread() {
        if (javaThread == null) {
            javaThread = new Thread(this);
        }
        return javaThread;
    }

    /**
     * Has this object already a reference to a java.lang.Thread object?
     *
     * @return boolean
     */
    public final boolean hasJavaThread() {
        return (javaThread != null);
    }

    final void checkAccess() {
        asThread().checkAccess();
    }

    public final void start() {
        switch (threadState) {
            case CREATED: {
                // Screen.debug("thread.start");
                final VmScheduler scheduler = VmMagic.currentProcessor()
                    .getScheduler();
                stack = VmSystem.allocStack(stackSize);
                Unsafe.initThread(this, stack, stackSize);
                stackEnd = getStackEnd(stack, stackSize);
                scheduler.registerThread(this);
                threadState = RUNNING;
                scheduler.addToReadyQueue(this, false, "thread.start");
                break;
            }
            case RUNNING:
            case SUSPENDED:
            case WAITING_ENTER:
            case WAITING_NOTIFY:
            case WAITING_NOTIFY_TIMEOUT:
            case ASLEEP:
                throw new IllegalThreadStateException("already started");
            case STOPPED:
                /* XXX */
                break;
            case DESTROYED:
                throw new IllegalThreadStateException("destroyed");
            default:
                throw new IllegalThreadStateException("Unknown thread state");
        }
    }

    /**
     * Stop the thread permanently.
     *
     * @param ex
     * @throws UninterruptiblePragma
     */
    public final void stop(Throwable ex) throws UninterruptiblePragma {
        this.stopping = true;
        if (javaThread != null) {
            javaThread.onExit();
            //exit the current isolate if needed
            if (ex instanceof ThreadDeath) {
                VmIsolate.currentIsolate().implicitExit(this, 0);
            } else {
                VmIsolate.currentIsolate().uncaughtExceptionExit();
            }
            // Notify joining threads
            synchronized (javaThread) {
                javaThread.notifyAll();
            }
        }

        // Do the low level stop uninterrupted
        doStop();
    }

    /**
     * Stop the thread permanently.
     *
     * @param ex
     * @throws UninterruptiblePragma
     */
    public final void stopForced(Throwable ex) throws UninterruptiblePragma {
        this.stopping = true;
        if (javaThread != null) {
            javaThread.onExit();
            // Notify joining threads
            synchronized (javaThread) {
                javaThread.notifyAll();
            }
        }

        // Do the low level stop uninterrupted
        doStop();
    }

    /**
     * Stop the thread permanently.
     *
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    private final void doStop() {
        //release monitors
        Monitor lom = lastOwnedMonitor;
        while (lom != null) {
            Monitor prev = lom.getPrevious();
            lom.release(this);
            if (prev == lom)
                break;
            lom = prev;
        }
        lastOwnedMonitor = null;


        final VmProcessor proc = VmMagic.currentProcessor();
        final VmThread current = proc.getCurrentThread();
        proc.getScheduler().unregisterThread(this);
        // Go into low level stuff
        proc.disableReschedule(true);
        this.threadState = STOPPED;
        if (current == this) {
            proc.suspend(true);
        } else {
            proc.enableReschedule(true);
        }
    }

    /**
     * Destroys this thread, without any cleanup. Any monitors it has locked
     * remain locked. (This method is not implemented.)
     */
    public final void destroy() {
    }

    /**
     * Interrupt this thread.
     *
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    public final void interrupt() {
        // Set interrupted state
        this.interrupted = true;

        // Add to scheduler queue
        final VmProcessor proc = VmMagic.currentProcessor();
        proc.disableReschedule(true);
        try {
            switch (threadState) {
                case ASLEEP:
                case WAITING_ENTER:
                case WAITING_NOTIFY:
                case WAITING_NOTIFY_TIMEOUT: {
                    // Remove from queues
                    wakeUpByScheduler();
                    // Add to ready queue
                    proc.getScheduler().addToReadyQueue(this, false, "thread.interrupt");
                    break;
                }
            }
        } finally {
            proc.enableReschedule(true);
        }
    }

    /**
     * Test the interruption status. If interrupted, the interrupted status is
     * cleared and an InterruptedException is thrown, otherwise this method
     * returns without any change in state.
     *
     * @throws UninterruptiblePragma
     * @throws InterruptedException
     */
    final void testAndClearInterruptStatus() throws UninterruptiblePragma,
        InterruptedException {
        final boolean throwIE = this.interrupted;
        if (throwIE) {
            this.interrupted = false;
            throw new InterruptedException();
        }
    }

    /**
     * Resume this thread.
     */
    public final void resume() {
        checkAccess();
        if (threadState != SUSPENDED) {
            throw new IllegalThreadStateException("Not suspended");
        } else {
            final VmProcessor proc = VmProcessor.current();
            threadState = RUNNING;
            // FIXME make multi cpu safe
            proc.getScheduler().addToReadyQueue(this, false, "thread.resume");
        }
    }

    /**
     * Set the state from suspended to yielding
     */
    @KernelSpace
    @Uninterruptible
    final void unsecureResume() {
        final VmProcessor proc = VmMagic.currentProcessor();
        if (threadState == SUSPENDED) {
            threadState = RUNNING;
            // FIXME make multi cpu safe
            proc.getScheduler().addToReadyQueue(this, false,
                "thread.unsecureResume");
        }
    }

    /**
     * Suspend this thread.
     *
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    final void unsecureSuspend() {
        if (threadState != RUNNING) {
            throw new IllegalThreadStateException("Not running");
        } else {
            final VmProcessor proc = VmMagic.currentProcessor();
            proc.disableReschedule(true);
            this.threadState = SUSPENDED;
            proc.suspend(true);
        }
    }

    /**
     * Suspend this thread.
     *
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    public final void suspend() {
        checkAccess();
        unsecureSuspend();
    }

    /**
     * Give up the CPU.
     */
    @Inline
    public static void yield() {
        VmProcessor.current().yield(false);
    }

    /**
     * Set the state to YIELDING.
     *
     * @throws UninterruptiblePragma
     */
    final void setYieldingState() throws UninterruptiblePragma {
        if (threadState == RUNNING) {
            threadState = YIELDING;
        }
    }

    /**
     * Go to sleep for the given period.
     *
     * @param millis
     * @param nanos
     * @throws InterruptedException
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    public final void sleep(long millis, int nanos) throws InterruptedException {
        if (currentThread() != this) {
            return;
        }
        if (threadState != RUNNING) {
            return;
        }

        // Test interrupted status
        if (this.interrupted) {
            this.interrupted = false;
            throw new InterruptedException();
        }

        final long wakeupTime = VmSystem.currentKernelMillis() + millis;
        final VmProcessor proc = VmProcessor.current();
        proc.disableReschedule(true);
        this.wakeupTime = wakeupTime;
        this.threadState = ASLEEP;
        proc.getScheduler().addToSleepQueue(this);

        /* Now un-schedule myself */
        proc.suspend(true);

        /* We're back alive */
        testAndClearInterruptStatus();
    }

    /**
     * Returns <code>true</code> if the thread represented by this object is
     * running (including suspended, asleep, or interrupted). Returns
     * <code>false</code> if the thread hasn't be started yet, is stopped or
     * destroyed.
     *
     * @return <code>true</code> if thread is alive, <code>false</code> if
     * not.
     * @see #start()
     * @see #stop(Throwable)
     * @see #suspend()
     * @see #interrupt()
     */
    public final boolean isAlive() {
        switch (threadState) {
            case CREATED:
                return false;
            case RUNNING:
            case SUSPENDED:
            case WAITING_ENTER:
            case WAITING_NOTIFY:
            case WAITING_NOTIFY_TIMEOUT:
            case ASLEEP:
            case YIELDING:
                return !stopping;
            case STOPPED:
            case DESTROYED:
                return false;
            default:
                throw new RuntimeException("reality failure");
        }
    }

    /**
     * Is this thread in the running state?
     *
     * @return boolean
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    public final boolean isRunning() {
        return (threadState == RUNNING);
    }

    /**
     * Is this thread in the yielding state?
     *
     * @return boolean
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    public final boolean isYielding() {
        return (threadState == YIELDING);
    }

    /**
     * Is this thread in the process of being stopped?
     *
     * @return boolean
     */
    public final boolean isStopping() {
        return stopping;
    }

    /**
     * Has this thread been interrupted?
     *
     * @return boolean
     */
    public final boolean isInterrupted(boolean clearFlag) {
        boolean result = this.interrupted;
        if (clearFlag) {
            this.interrupted = false;
        }
        return result;
    }

    /**
     * Is this thread waiting in a monitor?
     *
     * @return boolean
     */
    @KernelSpace
    @Uninterruptible
    public final boolean isWaiting() {
        return ((threadState >= WAITING_ENTER) && (threadState <= WAITING_NOTIFY_TIMEOUT));
    }

    /**
     * Gets the state of this thread.
     *
     * @return the thread state value
     */
    @KernelSpace
    public final int getThreadState() {
        return threadState;
    }

    /**
     * Gets the thread this thread is waiting for (or null).
     *
     * @return the VmThread for the thread that this one is
     * waiting for, or {@code null}.
     */
    @KernelSpace
    final VmThread getWaitForThread() {
        Monitor m = this.waitForMonitor;
        return (m != null) ? m.getOwner() : null;
    }

    /**
     * Gets a human readable name for the current thread state.
     *
     * @return the thread state name
     */
    @KernelSpace
    @Uninterruptible
    public final String getThreadStateName() {
        return STATE_NAMES[threadState];
    }

    /**
     * Call the run method of the (java version of) the given thread and when
     * run returns, kill the given thread. This method is called by the native
     * code that is setup by Unsafe.initThread.
     *
     * @param thread
     */
    @LoadStatics
    protected static final void runThread(VmThread thread) {
        Throwable t = null;
        try {
            thread.asThread().run();
        } catch (Throwable ex) {
            try {
                t = ex;

                if ((ex instanceof VmExit)) {
                    //print VmExit stacktrace when jnode.debug system property is set to true
                    if ("true".equals(VmIOContext.getGlobalProperties().getProperty("jnode.debug"))) {
                        ex.printStackTrace();
                    }
                } else {
                    ex.printStackTrace();
                }
            } catch (Throwable ex2) {
                /* Ignore */
            }
        } finally {
            try {
                if (t == null)
                    thread.stop(new ThreadDeath());
                else
                    thread.stop(t);
            } catch (Throwable ex) {
                /* Ignore */
                while (true) {
                    Unsafe.idle();
                }
            }
        }
    }

    /**
     * Is it already time for me to wakeup?
     *
     * @param curTime
     * @return boolean
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final boolean canWakeup(long curTime) {
        return (curTime >= wakeupTime);
    }

    /**
     * Setup this thread to wait for the given monitor
     *
     * @param monitor
     * @param waitState One of the Thread WAITING_XYZ states.
     */
    @Uninterruptible
    final void prepareWait(Monitor monitor, int waitState) {
        // Keep this order of assignments!
        this.waitForMonitor = monitor;
        this.threadState = waitState;
    }

    /**
     * Wake this thread up after being locked in the given monitor.
     *
     * @param monitor
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    final void wakeupAfterMonitor(Monitor monitor) {
        if (isWaiting()) {
            final VmProcessor proc = VmMagic.currentProcessor();
            proc.disableReschedule(true);
            try {
                this.threadState = RUNNING;
                proc.getScheduler().addToReadyQueue(this, false,
                    "thread.wakeupAfterMonitor");
            } finally {
                proc.enableReschedule(true);
            }
        } else {
            Unsafe.debug("Oops thread was not waiting? threadState="
                + threadState);
        }
    }

    /**
     * This thread is selected as new thread by the scheduler. Set the thread
     * state to running.
     *
     * @throws UninterruptiblePragma
     */
    @KernelSpace
    @Uninterruptible
    final void wakeUpByScheduler() {
        switch (threadState) {
            case ASLEEP:
            case RUNNING:
            case YIELDING: {
                // Do nothing
                break;
            }
            case WAITING_ENTER:
            case WAITING_NOTIFY:
            case WAITING_NOTIFY_TIMEOUT: {
                final Monitor mon = this.waitForMonitor;
                mon.removeThreadFromQueues(this);
                break;
            }
            default: {
                Unsafe.debug("Incorrect threadState in wakeUpByScheduler ");
                Unsafe.debug(threadState);
            }
        }
        threadState = RUNNING;
    }

    /**
     * @return The thread's priority.
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * Sets the thread's priority.
     *
     * @param priority The priority to set
     */
    public void setPriority(int priority) {
        checkAccess();
        if ((priority < Thread.MIN_PRIORITY)
            || (priority > Thread.MAX_PRIORITY)) {
            throw new IllegalArgumentException("Invalid priority");
        }
        if (asThread() instanceof SystemThread) {
            this.priority = priority;
        }
    }

    /**
     * Gets the stack of this thread
     *
     * @return The stack
     */
    protected final Object getStack() {
        return stack;
    }

    /**
     * Gets the size of the stack (of this thread) in bytes.
     *
     * @return The stack size
     */
    public int getStackSize() {
        return stackSize;
    }

    /**
     * Create a new (unique) thread identifier.
     *
     * @return The id
     */
    private static synchronized int createId() {
        if (lastId == 0) {
            lastId = 8;
        }
        final int id = ++lastId;
        return id << ObjectFlags.THREAD_ID_SHIFT;
    }

    /**
     * Gets the identifier of this thread. This identifier has already been
     * shifted by THREAD_ID_SHIFT.
     *
     * @return The id
     * @see ObjectFlags#THREAD_ID_SHIFT
     */
    @KernelSpace
    @Uninterruptible
    public final int getId() {
        return id;
    }

    /**
     * Gets the name of this thread.
     *
     * @return the name of this thread.
     */
    @KernelSpace
    public final String getName() {
        return name;
    }

    /**
     * Update my name from the java thread that wraps me.
     */
    public final void updateName() {
        if (this.javaThread != null) {
            this.name = javaThread.getName();
        }
    }

    /**
     * @return {@code true} if the stack has overflowed.
     */
    boolean isStackOverflow() {
        return this.stackOverflow;
    }

    /**
     * Verify the state of this thread.
     *
     * @throws UninterruptiblePragma
     */
    final void verifyState() throws UninterruptiblePragma {
        switch (threadState) {
            case CREATED:
                if (queueEntry.isInUse()) {
                    throw new Error(
                        "Created thread cannot have an inuse queueEntry");
                }
                if (sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Created thread cannot have an inuse sleepQueueEntry");
                }
                break;
            case ASLEEP:
                if (queueEntry.isInUse()) {
                    throw new Error(
                        "Sleeping thread cannot have an inuse queueEntry");
                }
                if (!sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Sleeping thread must have an inuse sleepQueueEntry");
                }
                break;
            case DESTROYED:
                if (queueEntry.isInUse()) {
                    throw new Error(
                        "Destroyed thread cannot have an inuse queueEntry");
                }
                if (sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Destroyed thread cannot have an inuse sleepQueueEntry");
                }
                break;
            case RUNNING:
                if (!queueEntry.isInUse()) {
                    if (VmProcessor.current().getCurrentThread() != this) {
                        throw new Error(
                            "Running thread must be inuse on ready queue or current thread");
                    }
                }
                if (sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Running thread cannot have an inuse sleepQueueEntry");
                }
                break;
            case STOPPED:
                if (queueEntry.isInUse()) {
                    throw new Error(
                        "Stopped thread cannot have an inuse queueEntry");
                }
                if (sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Stopped thread cannot have an inuse sleepQueueEntry");
                }
                break;
            case SUSPENDED:
                if (queueEntry.isInUse()) {
                    throw new Error(
                        "Suspended thread cannot have an inuse queueEntry");
                }
                if (sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Suspended thread cannot have an inuse sleepQueueEntry");
                }
                break;
            case WAITING_ENTER:
            case WAITING_NOTIFY:
            case WAITING_NOTIFY_TIMEOUT:
                if (waitForMonitor == null) {
                    throw new Error("Waiting thread must have a waitForMonitor");
                }
                if (!queueEntry.isInUse()) {
                    throw new Error("Waiting thread must have an inuse queueEntry");
                }
                break;
            case YIELDING:
                if (!queueEntry.isInUse()) {
                    throw new Error("Yielding thread must have an inuse queueEntry");
                }
                if (sleepQueueEntry.isInUse()) {
                    throw new Error(
                        "Yielding thread cannot have an inuse sleepQueueEntry");
                }
                break;
            default:
                throw new Error("Unknown thread state " + threadState);
        }
        // Now detect deadlocks
        detectDeadlock(null, true); //concurrentLocks=true
    }

    /**
     * Convert to a String representation.
     *
     * @return String
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (javaThread != null) {
            return '%' + javaThread.getName() + ", st"
                + STATE_NAMES[threadState] + '%';
        } else {
            return "%@null@, st" + threadState + '%';
        }
    }

    /**
     * Gets the most current stackframe of this thread. This method is only
     * valid when this thread is not running.
     *
     * @return The stack frame
     */
    @KernelSpace
    @Internal
    public abstract Address getStackFrame();

    /**
     * Gets the most current instruction pointer of this thread. This method is
     * only valid when this thread is not running.
     *
     * @return The instruction pointer
     */
    protected abstract Address getInstructionPointer();

    /**
     * Gets the stackframe of the last system exception of this thread.
     */
    protected abstract Address getExceptionStackFrame();

    /**
     * Gets the instruction pointer of the last system exception of this thread.
     */
    protected abstract Address getExceptionInstructionPointer();

    /**
     * Calculate the end of the stack.
     *
     * @param stack
     * @param stackSize
     * @return End address of the stack
     */
    protected abstract Address getStackEnd(Object stack, int stackSize);

    /**
     * Gets a human readable representation of the system exception state.
     *
     * @return String
     */
    public abstract String getReadableErrorState();

    /**
     * Gets the inherited control context of this thread.
     *
     * @return Returns the control.
     */
    @Internal
    public final VmAccessControlContext getContext() {
        return this.context;
    }

    /**
     * Sets the inherited control context of this thread.
     *
     * @param context The control to set.
     */
    @Internal
    public final void setContext(VmAccessControlContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Uninterruptible
    @Override
    public final void detectDeadlock(List<org.jnode.vm.facade.VmThread> deadLockCycle, boolean concurrentLocks) {
        // first, ensure the initial list is empty (when it's not null)
        if (deadLockCycle != null) {
            deadLockCycle.clear();
        }

        if (isWaiting()) {
            walkWaitingThreads(this, deadLockCycle, concurrentLocks);
        }
    }

    /**
     * Helper for detectDeadlock
     *
     * @param thread          The thread to check for deadlock.
     * @param deadLockCycle   The list of threads involved in a deadlock.
     * @param concurrentLocks If true, includes concurrent locks in the search.
     * @throws UninterruptiblePragma
     */
    @Uninterruptible
    private final void walkWaitingThreads(VmThread thread, List<org.jnode.vm.facade.VmThread> deadLockCycle,
                                          boolean concurrentLocks) {
        if (thread == null) {
            if (deadLockCycle != null) {
                deadLockCycle.clear();
            }
            return;
        }
        final Monitor waitForMonitor = thread.waitForMonitor;
        final VmThread owner;
        if (waitForMonitor == null) {
            VmThread concurrentOwner = null;
            if (concurrentLocks) {
                concurrentOwner = thread.getConcurrentOwner();
            }

            if (concurrentOwner == null) {
                if (deadLockCycle != null) {
                    deadLockCycle.clear();
                }
                return;
            }
            owner = concurrentOwner;
        } else {
            owner = waitForMonitor.getOwner();
        }

        if (owner == this) {
            // we have found a cycle
            // => deadLockCycle contains threads involved in the deadlock
            if (deadLockCycle != null) {
                deadLockCycle.add(owner);
            } else {
                // We have a deadlock
                Unsafe.debug("Deadlock[");
                Unsafe.debug(this.asThread().getName());
                Unsafe.debug(", ");
                Unsafe.debug(owner.asThread().getName());
            }
        } else {
            if (deadLockCycle != null) {
                deadLockCycle.add(owner);
            }
            walkWaitingThreads(owner, deadLockCycle, concurrentLocks);
        }
    }

    private VmThread getConcurrentOwner() {
        return getConcurrentOwner(LockSupport.getBlocker(asThread()), null); // get java.lang.Thread#parkBlocker
    }


    private VmThread getConcurrentOwner(Object blockerObj, List<AbstractOwnableSynchronizer> ownableSynchronizers) {
        VmThread concurrentOwner = null;
        if (blockerObj instanceof AbstractOwnableSynchronizer) {
            if (ownableSynchronizers != null) {
                ownableSynchronizers.add((AbstractOwnableSynchronizer) blockerObj);
            }

            if (aosGetExclusiveOwnerThread == null) {
                aosGetExclusiveOwnerThread =
                    NativeHelper.findDeclaredMethod(AbstractOwnableSynchronizer.class, "getExclusiveOwnerThread",
                        "()Ljava/lang/Thread;");
            }
            Thread thread = NativeHelper.invoke(Thread.class, blockerObj, aosGetExclusiveOwnerThread);
            if (thread != null) {
                if (threadVmThread == null) {
                    threadVmThread = NativeHelper.findDeclaredField(Thread.class, "vmThread");
                }
                concurrentOwner = (VmThread) VmReflection.getObject(threadVmThread, thread);
            }
        }
        return concurrentOwner;
    }

    /**
     * Gets the inSystemException attribute. Reading this attribute also clears
     * it.
     *
     * @return Returns the inSystemException.
     */
    final boolean isInSystemException() {
        final boolean rc = this.inSystemException;
        this.inSystemException = false;
        return rc;
    }

    /**
     * Sets the inSystemException state.
     */
    final void setInSystemException() {
        this.inSystemException = true;
    }

    /**
     * @param isolatedStatics
     */
    public final void switchToIsolate(VmIsolatedStatics isolatedStatics) {
        final VmProcessor proc = VmMagic.currentProcessor();
        this.isolatedStatics = isolatedStatics;
        if (proc.currentThread == this) {
            proc.setIsolatedStatics(isolatedStatics);
        }
    }

    /**
     * {@inheritDoc}
     */
    public abstract boolean accept(ObjectVisitor visitor,
                                   VmHeapManager heapManager);

    /**
     * @return the requiredProcessor
     */
    @KernelSpace
    @Uninterruptible
    protected final VmProcessor getRequiredProcessor() {
        return requiredProcessor;
    }

    /**
     * @param requiredProcessor the requiredProcessor to set
     */
    protected final void setRequiredProcessor(VmProcessor requiredProcessor) {
        this.requiredProcessor = requiredProcessor;
    }

    /**
     * @return the currentProcessor
     */
    final VmProcessor getCurrentProcessor() {
        return currentProcessor;
    }

    /**
     * @param currentProcessor the currentProcessor to set
     */
    final void setCurrentProcessor(VmProcessor currentProcessor) {
        this.currentProcessor = currentProcessor;
    }

    /**
     * Gets the stacktrace of a given thread.
     *
     * @param current
     * @return The stacktrace
     */
    public static Object[] getStackTrace(VmThread current) {
        return getStackTrace(current, STACKTRACE_LIMIT);
    }

    private static Object[] getStackTrace(VmThread current, int stackTraceLimit) {
        if (current.inException) {
            Unsafe.debug("Exception in getStackTrace");
            VmProcessor.current().getArchitecture().getStackReader()
                .debugStackTrace();
            Unsafe.die("getStackTrace");
            return null;
        } else {
            current.inException = true;
        }

        if (VmUtils.getVm().getHeapManager().isLowOnMemory()) {
            return null;
        }

        final VmProcessor proc = VmProcessor.current();
        final VmStackReader reader = proc.getArchitecture().getStackReader();
        final VmStackFrame[] mt;
        boolean inSystemException = false;
        // Address lastIP = null;
        if (current.isInSystemException()) {
            proc.disableReschedule(false);
            try {
                mt = reader.getVmStackTrace(current.getExceptionStackFrame(),
                    current.getExceptionInstructionPointer(),
                    stackTraceLimit);
            } finally {
                proc.enableReschedule(false);
            }
            inSystemException = true;
        } else if (current == proc.getCurrentThread()) {
            final Address curFrame = VmMagic.getCurrentFrame();
            mt = reader.getVmStackTrace(reader.getPrevious(curFrame), reader
                .getReturnAddress(curFrame), stackTraceLimit);
        } else {
            proc.disableReschedule(false);
            try {
                mt = reader.getVmStackTrace(current.getStackFrame(), current
                    .getInstructionPointer(), stackTraceLimit);
                // lastIP = current.getInstructionPointer();
            } finally {
                proc.enableReschedule(false);
            }
        }
        final int cnt = (mt == null) ? 0 : mt.length;

        VmType<?> lastClass = null;

        // skip the first element which is VMThrowable.fillInStackTrace() ...
        // unless this is a system exception 
        int i = inSystemException ? 0 : 1;
        while (i < cnt) {
            final VmStackFrame f = mt[i];
            if (f == null) {
                break;
            }
            final VmMethod method = f.getMethod();
            if (method == null) {
                break;
            }
            final VmType<?> vmClass = method.getDeclaringClass();
            if (vmClass == null) {
                break;
            }
            final VmType<?> sClass = vmClass.getSuperClass();
            if (lastClass != null && sClass != lastClass && vmClass != lastClass) {
                break;
            }
            final String mname = method.getName();
            if (mname == null) {
                break;
            }
            if (!("<init>".equals(mname) || "fillInStackTrace".equals(mname) || "getStackTrace"
                .equals(mname))) {
                break;
            }
            lastClass = vmClass;
            i++;
        }

        final VmStackFrame[] st = new VmStackFrame[cnt - i];
        int j = 0;
        for (; i < cnt; i++) {
            st[j++] = mt[i];
        }

        current.inException = false;
        return st;
    }

    /**
     * Create an exception for a system-trapped situation.
     *
     * @param nr
     * @param address
     * @return Throwable
     * @throws UninterruptiblePragma
     */
    @LoadStatics
    @PrivilegedActionPragma
    public static Throwable systemException(int nr, int address)
        throws UninterruptiblePragma {
        // if (VmSystem.debug > 0) {
        // Unsafe.debugStackTrace();
        // }

        // Do stack overflows without anything that is not
        // absolutely needed
        if (nr == EX_STACKOVERFLOW) {
            if (true) {
                Unsafe.debug("Stack overflow:\n");
                Unsafe.debugStackTrace(50);
                Unsafe.debug('\n');
            }
            throw new StackOverflowError();
        }

        if (false) {
            Unsafe.debug(nr);
            Unsafe.debug(address);
            Unsafe.die("System exception");
        }
        // Unsafe.debug(nr); Unsafe.debug(address);
        final String hexAddress = NumberUtils.hex(address, 8);
        final VmThread current = VmProcessor.current().getCurrentThread();
        // final String state = " (" + current.getReadableErrorState() + ")";
        final String state = "";
        // Mark a system exception, so the stacktrace uses the exception frame
        // instead of the current frame.
        current.setInSystemException();
        switch (nr) {
            case EX_NULLPOINTER:
                return new NullPointerException("NPE at address " + hexAddress
                    + state);
            case EX_PAGEFAULT:
                return new InternalError("Page fault at " + hexAddress + state);
            case EX_INDEXOUTOFBOUNDS:
                return new ArrayIndexOutOfBoundsException("Out of bounds at index "
                    + address + state);
            case EX_DIV0:
                return new ArithmeticException("Division by zero at address "
                    + hexAddress + state);
            case EX_ABSTRACTMETHOD:
                return new AbstractMethodError("Abstract method at " + hexAddress
                    + state);
            case EX_STACKOVERFLOW:
                return new StackOverflowError();
            case EX_COPRO_OR:
                throw new ArithmeticException("Coprocessor overrun");
            case EX_COPRO_ERR:
                throw new ArithmeticException("Coprocessor error");
            default:
                return new UnknownError("Unknown system-exception at " + hexAddress
                    + state);
        }
    }

    public VmIsolatedStatics getIsolatedStatics() {
        return isolatedStatics;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Uninterruptible
    public ThreadInfo getThreadInfo(boolean lockedMonitors, boolean lockedSynchronizers, int maxDepth, boolean useCache,
                                    final List<AbstractOwnableSynchronizer> cachedOwnableSynchronizers) {
        int actualDepth = ((maxDepth < 0) || (maxDepth > STACKTRACE_LIMIT)) ? STACKTRACE_LIMIT : maxDepth;
        VmStackFrame[] vmStackFrames = (VmStackFrame[]) getStackTrace(this, actualDepth);
        StackTraceElement[] stackTrace =
            (actualDepth == 0) ? null : VmImpl.backTrace2stackTrace(vmStackFrames);

        // find owned monitors
        Object[] monitors = null;
        int[] stackDepths = null;
        if (lockedMonitors && (nbLockedObjects > 0)) {
            if (nbLockedObjects == lockedObjects.length) {
                // avoid a copy : ThreadInfo constructor doesn't keep a reference on this array
                monitors = lockedObjects;
            } else {
                monitors = new Object[nbLockedObjects];
                System.arraycopy(lockedObjects, 0, monitors, 0, nbLockedObjects);
            }

            stackDepths = new int[monitors.length];
            for (int sdIndex = 0; sdIndex < stackDepths.length; sdIndex++) {
                VmMethod lockVmMethod = lockVmMethods[sdIndex];
//                int lockProgCounter = lockProgCounters[sdIndex];

                // search lockVmStackFrame in vmStackFrames
                stackDepths[sdIndex] = -1;
                for (int vsfIndex = 0; vsfIndex < vmStackFrames.length; vsfIndex++) {
                    VmStackFrame vmStackFrame = vmStackFrames[vsfIndex];
                    if (/*(vmStackFrame.getProgramCounter() == lockProgCounter) &&*/ //TODO fix this
                        (vmStackFrame.getMethod() == lockVmMethod)) {
                        stackDepths[sdIndex] = vsfIndex;
                        break;
                    }
                }
            }
        }

        // find owned synchronizers
        Object[] synchronizers = null;
        if (lockedSynchronizers) {
            final List<Object> mySynchronizers = new ArrayList<Object>();
            if (useCache) {
                for (AbstractOwnableSynchronizer s : cachedOwnableSynchronizers) {
                    addIfMySynchronizer(s, null, mySynchronizers);
                }
            } else {
                ObjectVisitor visitor = new ObjectVisitor() {
                    @Override
                    public boolean visit(Object object) {
                        addIfMySynchronizer(object, cachedOwnableSynchronizers, mySynchronizers);
                        return true;
                    }
                };
                VmUtils.getVm().getHeapManager().accept(visitor);
            }
            synchronizers = mySynchronizers.isEmpty() ? null : mySynchronizers.toArray();
        }

        boolean noLock = ((waitForMonitor == null) || (waitForMonitor.getOwner() == null));
        Object lockObj = (waitForMonitor == null) ? null : waitForMonitor.getLockedObject();
        Thread lockOwner = noLock ? null : waitForMonitor.getOwner().asThread();

        //TODO fill these values :
        long blockedCount = 0;
        long blockedTime = 0;
        long waitedCount = 0;
        long waitedTime = 0;
        // end of TO DO "fill these values"

        return createThreadInfo(asThread(), threadState, lockObj, lockOwner, blockedCount, blockedTime, waitedCount,
            waitedTime,
            stackTrace, monitors, stackDepths, synchronizers);
    }

    private void addIfMySynchronizer(Object object, List<AbstractOwnableSynchronizer> allSynchronizers,
                                     List<Object> mySynchronizers) {
        VmThread owner = getConcurrentOwner(object, allSynchronizers);
        if (owner == this) {
            mySynchronizers.add(object);
        }
    }

    private static final ThreadInfo createThreadInfo(Thread t, int state, Object lockObj, Thread lockOwner,
                                                     long blockedCount, long blockedTime,
                                                     long waitedCount, long waitedTime,
                                                     StackTraceElement[] stackTrace,
                                                     Object[] monitors,
                                                     int[] stackDepths,
                                                     Object[] synchronizers) {
        if (tiConstructor == null) {
            tiConstructor = NativeHelper.findConstructor(ThreadInfo.class,
                "(Ljava/lang/Thread;ILjava/lang/Object;Ljava/lang/Thread;JJJJ[Ljava/lang/StackTraceElement;[Ljava/lang/Object;[I[Ljava/lang/Object;)V");
        }

        try {
            return (ThreadInfo) VmReflection.newInstance(tiConstructor,
                new Object[]{t, state, lockObj, lockOwner, blockedCount, blockedTime, waitedCount, waitedTime,
                    stackTrace, monitors, stackDepths, synchronizers});
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    @Uninterruptible
    final Monitor getLastOwnedMonitor() {
        return lastOwnedMonitor;
    }

    @Uninterruptible
    final void setLastOwnedMonitor(Monitor lastOwnedMonitor) {
        this.lastOwnedMonitor = lastOwnedMonitor;
    }

    private VmScheduler getScheduler() {
        return ((VmImpl) VmUtils.getVm()).getScheduler();
    }

    /**
     * This method must NOT ALLOCATE ANY NEW OBJECT (directly or not) because
     * allocation need a lock and thus calling this again => infinite recursion => StackOverflow.
     *
     * @param lockedObject
     */
    @Uninterruptible
    void addLockedObject(Object lockedObject) {
        if (nbLockedObjects >= lockedObjects.length) {
            debug("addLockedObject : too many lockedObjects");
            return;
        }

//        int programCounter = 0;
//        lockVmMethods[nbLockedObjects] = VmProcessor.current().getArchitecture().getStackReader().getStackTraceAt(3).getBytecode().getLineNr(programCounter);
        lockVmMethods[nbLockedObjects] = VmProcessor.current().getArchitecture().getStackReader().getStackTraceAt(3);
//        lockProgCounters[nbLockedObjects] = 0; //TODO fix this
        lockedObjects[nbLockedObjects] = lockedObject;
        nbLockedObjects++;
    }

    @Uninterruptible
    void removeLockedObject(Object lockedObject) {
        if (lockedObject == null) {
            debug("removeLockedObject : lockedObject is null");
            return;
        }
        boolean waitCase = (threadState == WAITING_NOTIFY) || (threadState == WAITING_NOTIFY_TIMEOUT);
        if (nbLockedObjects == 0) {
//            debug("removeLockedObject : no locked object to remove");
            return;
        }

        // we have exited a monitor
        final int lastLockedObject = nbLockedObjects - 1;
        if (lockedObject != lockedObjects[lastLockedObject]) {
            if (waitCase) {
                for (int i = 0; i < nbLockedObjects; i++) {
                    Object obj = lockedObjects[i];
                    if (obj == lockedObject) {
                        Unsafe.debug("removeLockedObject: lockedObject ");
                        Unsafe.debug(lockedObject.toString());
                        Unsafe.debug(" found at index ");
                        Unsafe.debug(i);
                        Unsafe.debug("/");
                        Unsafe.debug(nbLockedObjects);
                        Unsafe.debug('\n');
                        break;
                    }
                }
            }
            return;
        }

        lockedObjects[lastLockedObject] = null; // help gc
        nbLockedObjects--;
    }

    @Inline
    private final void debug(String message) {
        Unsafe.debug(message);
        Unsafe.debug(" for thread ");
        debugThread();
        Unsafe.debug('\n');
    }

    @Inline
    private final void debugThread() {
        Unsafe.debug(id);
        Unsafe.debug(" - ");
        Unsafe.debug(name);
    }

    /**
     * Initialize the the mapping between VmThread states and Thread.State.
     * TODO this mapping might need some review because meaning of some states is not clear.
     *
     * @param vmThreadStateValues
     * @param vmThreadStateNames
     */
    public static void getThreadStateValues(int[][] vmThreadStateValues, String[][] vmThreadStateNames) {
        vmThreadStateValues[NEW.ordinal()] = new int[]{CREATED};
        vmThreadStateNames[NEW.ordinal()] = new String[]{NEW.name()};

        vmThreadStateValues[RUNNABLE.ordinal()] = new int[]{RUNNING, SUSPENDED, YIELDING, ASLEEP};
        vmThreadStateNames[RUNNABLE.ordinal()] =
            new String[]{RUNNABLE.name() + ".RUNNING", RUNNABLE.name() + ".SUSPENDED", RUNNABLE.name() + ".YIELDING",
                RUNNABLE.name() + ".ASLEEP"};

        vmThreadStateValues[BLOCKED.ordinal()] = new int[]{WAITING_ENTER};
        vmThreadStateNames[BLOCKED.ordinal()] = new String[]{BLOCKED.name()};

        vmThreadStateValues[WAITING.ordinal()] = new int[]{WAITING_NOTIFY};
        vmThreadStateNames[WAITING.ordinal()] = new String[]{WAITING.name()};

        vmThreadStateValues[TIMED_WAITING.ordinal()] = new int[]{WAITING_NOTIFY_TIMEOUT};
        vmThreadStateNames[TIMED_WAITING.ordinal()] = new String[]{TIMED_WAITING.name()};

        vmThreadStateValues[TERMINATED.ordinal()] = new int[]{STOPPED, DESTROYED};
        vmThreadStateNames[TERMINATED.ordinal()] =
            new String[]{TERMINATED.name() + ".STOPPED", TERMINATED.name() + ".DESTROYED"};
    }
}
