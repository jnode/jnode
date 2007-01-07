/* Thread -- an independent thread of executable code
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006
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

import java.util.Map;
import java.util.WeakHashMap;


import org.jnode.security.JNodePermission;
import org.jnode.vm.VmSystem;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.Internal;
import org.jnode.vm.classmgr.VmIsolatedStatics;
import org.jnode.vm.isolate.IsolateThread;
import org.jnode.vm.scheduler.MonitorManager;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmThread;

/**
 * Kore implementation of the <code>java.lang.Thread</code> class.
 * <p>
 * All native methods are indirected through <code>java.lang.NativeLang</code>.
 * 
 * @version Kore 0.0.3, June 1997
 * @author Glynn Clements <a href="mailto:glynn@sensei.co.uk">glynn@sensei.co.uk
 *         </a>
 * @author E. Prangsma (connection to JNode)
 */
public class Thread implements Runnable {

    /**
     * Number of threads created. Used only for generating "unique" names.
     * 
     * @see java.lang.Thread#autoName()
     */
    private static int count = 0;

    private static final JNodePermission GETVMTHREAD_PERM = new JNodePermission(
            "getVmThread");

    public final static int MAX_PRIORITY = 10;

    public final static int MIN_PRIORITY = 1;

    public final static int NORM_PRIORITY = 5;

    /** Thread local storage. Package accessible for use by
     * InheritableThreadLocal.
     */
   WeakHashMap locals;

    /**
     * Gets the active number of threads in the current thread's thread group.
     * 
     * @return the active number of threads in the current thread's thread
     *         group.
     */
    public static int activeCount() {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
     * Generate a "unique" default name for a thread.
     */
    private static synchronized String autoName() {
        return "Thread-" + (++count);
    }

    /**
     * Gets the current thread.
     * 
     * @return
     */
    public static Thread currentThread() {
        VmThread current = VmThread.currentThread();
        if (current != null) {
            return current.asThread();
        } else {
            return null;
        }
    }

    /**
     * Prints a stack trace of the current thread.
     */
    public static void dumpStack() {
        new Exception("Stack trace").printStackTrace();
    }

    public static int enumerate(Thread[] threads) {
        return currentThread().getThreadGroup().enumerate(threads);
    }

    /**
     * Checks whether the current thread holds the monitor on a given object.
     * This allows you to do <code>assert Thread.holdsLock(obj)</code>.
     * 
     * @param obj
     *            the object to test lock ownership on.
     * @return true if the current thread is currently synchronized on obj
     * @throws NullPointerException
     *             if obj is null
     * @since 1.4
     */
    public static boolean holdsLock(Object obj) {
        return MonitorManager.holdsLock(obj);
    }

    /**
     * Has the current thread been interrupted.
     * The interrupted flag is cleared.
     * @return
     */
    public static boolean interrupted() {
        VmThread current = VmThread.currentThread();
        if (current != null) {
            return current.isInterrupted(true);
        } else {
            return false;
        }
    }

    public static void sleep(long millis) throws InterruptedException {
        sleep(millis, 0);
    }

    /**
     * XXX needs to be synchronized to avoid losing interrupts? it checks and
     * sets state, which should be protected...
     */
    public static void sleep(long millis, int nanos)
            throws InterruptedException {
        VmThread.currentThread().sleep(millis, nanos);
    }

    public static void yield() {
        VmThread.yield();
    }

    /** The context classloader of this thread */
    private ClassLoader contextClassLoader;

    /** Is this a daemon thread? */
    private boolean daemon;

    /** The group this thread belongs to */
    ThreadGroup group;

    /** The name of this thread */
    String name;

    /** The thread in which I was created */
    private final Thread parent;

    /** A runnable target (if any) */
    private final Runnable target;

    /** The VM thread implementing this thread */
    private final VmThread vmThread;

    /**
     * Create a new default instance
     * 
     * @see java.lang.Object#Object()
     */
    public Thread() {
        this(null, null, autoName());
    }

    /**
     * Create a new instance with a runnable as thread runner.
     * 
     * @param target
     */
    public Thread(Runnable target) {
        this(null, target, autoName());
    }

    /**
     * Create a new instance with a runnable as thread runner and a given name.
     * 
     * @param target
     * @param name
     */
    public Thread(Runnable target, String name) {
        this(null, target, name);
    }

    /**
     * Create a new instance with a given name.
     * 
     * @param name
     */
    public Thread(String name) {
        this(null, null, name);
    }

    /**
     * Create a new instance with a given group as containing group and a
     * runnable as thread runner.
     * 
     * @param group
     * @param target
     */
    public Thread(ThreadGroup group, Runnable target) {
        this(group, target, autoName());
    }

    /**
     * Create a new instance with a given group as containing group, a runnable
     * as thread runner and a given name.
     * 
     * @param group
     * @param target
     * @param name
     */
    public Thread(ThreadGroup group, Runnable target, String name) {
        Thread current = currentThread();

        if (group != null) {
            group.checkAccess();
        } else {
            group = current.getThreadGroup();
        }

        if (group == null) {
            throw new InternalError("Live thread has invalid group: " + name);
        }

        group.addThread(this);

        this.group = group;
        this.target = target;
        this.name = name;
        this.parent = current;

        this.daemon = current.isDaemon();

        this.vmThread = VmProcessor.current().createThread(this);
        this.vmThread.setPriority(current.getPriority());
        this.vmThread.updateName();
        
        InheritableThreadLocal.newChildThread(this); // FDy : CLASSPATH patch ?
    }

      /**
   * Allocate a new Thread object, as if by
   * <code>Thread(group, null, name)</code>, and give it the specified stack
   * size, in bytes. The stack size is <b>highly platform independent</b>,
   * and the virtual machine is free to round up or down, or ignore it
   * completely.  A higher value might let you go longer before a
   * <code>StackOverflowError</code>, while a lower value might let you go
   * longer before an <code>OutOfMemoryError</code>.  Or, it may do absolutely
   * nothing! So be careful, and expect to need to tune this value if your
   * virtual machine even supports it.
   *
   * @param group the group to put the Thread into
   * @param target the Runnable object to execute
   * @param name the name for the Thread
   * @param size the stack size, in bytes; 0 to be ignored
   * @throws NullPointerException if name is null
   * @throws SecurityException if this thread cannot access <code>group</code>
   * @throws IllegalThreadStateException if group is destroyed
   * @since 1.4
   */
  public Thread(ThreadGroup group, Runnable target, String name, long size)
  {
     Thread current = currentThread();

        if (group != null) {
            group.checkAccess();
        } else {
            group = current.getThreadGroup();
        }

        if (group == null) {
            throw new InternalError("Live thread has invalid group: " + name);
        }

        group.addThread(this);

        this.group = group;
        this.target = target;
        this.name = name;
        this.parent = current;

        this.daemon = current.isDaemon();

        this.vmThread = VmProcessor.current().createThread(this);
        this.vmThread.setPriority(current.getPriority());
        this.vmThread.updateName();

        InheritableThreadLocal.newChildThread(this); // FDy : CLASSPATH patch ?
  }

    /**
     * Create a new instance with a given group as containing group, a runnable
     * as thread runner and a given name.
     * 
     * @param group
     * @param target
     * @param name
     */
    protected Thread(ThreadGroup group, Runnable target, String name, VmIsolatedStatics isolatedStatics) {
        if (!(this instanceof IsolateThread)) {
            throw new SecurityException("Constructor can only be called from IsolateThread");
        }
        if (group == null) {
            throw new InternalError("Isolate thread has invalid group: " + name);
        }

        group.addThread(this);

        this.group = group;
        this.target = target;
        this.name = name;
        this.parent = null;
        this.daemon = false;

        this.vmThread = VmProcessor.current().createThread(isolatedStatics, this);
        this.vmThread.setPriority(this.getPriority());
        this.vmThread.updateName();
        
        InheritableThreadLocal.newChildThread(this); // FDy : CLASSPATH patch ?
    }

    /**
     * Create a new instance with a given group as containing group and a given
     * name.
     * 
     * @param group
     * @param name
     */
    public Thread(ThreadGroup group, String name) {
        this(group, null, name);
    }

    /**
     * Create a new thread from a given VmThread. Only used for the main thread.
     * 
     * @param vmThread
     * @throws IllegalArgumentException
     *             If the given vmThread is not the root thread or the given
     *             vmThread already has an associated java thread.
     */
    public Thread(VmThread vmThread) throws IllegalArgumentException {
        if (vmThread.hasJavaThread()) {
            throw new IllegalArgumentException(
                    "vmThread has already a java thread associated with it");
        }
        this.vmThread = vmThread;
        this.group = ThreadGroup.root;
        this.group.addThread(this);
        this.name = "System";
        this.target = null;
        this.parent = null;
        this.vmThread.updateName();
    }

    /**
     * Has the current execution context enough access to modify this object?
     */
    public void checkAccess() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkAccess(this);
        }
    }

    /**
     * Counts the number of stackframes in this thread.
     * 
     * @return
     * @deprecated
     */
    public int countStackFrames() {
        return vmThread.countStackFrames();
    }

    /**
     * Originally intended to destroy this thread, this method was never
     * implemented by Sun, and is hence a no-op.
     * @deprecated
     */
    public void destroy() {
        vmThread.destroy();
    }

    /**
     * Gets the context classloader for this thread. The context ClassLoader is
     * provided by the creator of the thread for use by code running in this
     * thread when loading classes and resources. If not set, the default is the
     * ClassLoader context of the parent Thread. The context ClassLoader of the
     * primordial thread is typically set to the class loader used to load the
     * application. First, if there is a security manager, and the caller's
     * class loader is not null and the caller's class loader is not the same as
     * or an ancestor of the context class loader for the thread whose context
     * class loader is being requested, then the security manager's
     * checkPermission method is called with a
     * <code>RuntimePermission("getClassLoader")</code> permission to see if
     * it's ok to get the context ClassLoader.
     * 
     * @return
     */
    public ClassLoader getContextClassLoader() {
        if (contextClassLoader != null) {
            return contextClassLoader;
        } else if (parent != null) {
            return parent.getContextClassLoader();
        } else {
            return getClass().getClassLoader();
        }
    }

    /**
     * Gets the identifier of this thread
     * 
     * @return the identifier of this thread.
     * @since 1.5
     */
    public long getId() {
        return vmThread.getId();
    }

    /**
     * Gets the name of this thread
     * 
     * @return
     */
    public final String getName() {
        return name;
    }

    /**
     * Gets the priority of this thread
     * 
     * @return
     */
    public final int getPriority() {
        return vmThread.getPriority();
    }

    /**
     * Gets the group this thread is a child of.
     * 
     * @return
     */
    public final ThreadGroup getThreadGroup() {
        return isAlive() ? group : null;
    }

    /**
     * Gets the internal thread representation
     * 
     * @return
     */
    public final VmThread getVmThread() {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(GETVMTHREAD_PERM);
        }
        return vmThread;
    }

    /**
     * Gets the internal thread representation.
     * Used for kernel space access.
     * 
     * @return
     */
    @KernelSpace
    @Internal
    protected final VmThread getVmThreadKS() {
        return vmThread;
    }

    /**
     * Interrupt this thread.
     */
    public void interrupt() {
        vmThread.interrupt();
    }

    /**
     * Returns <code>true</code> if the thread represented by this object is
     * running (including suspended, asleep, or interrupted.) Returns
     * <code>false</code> if the thread hasn't be started yet, is stopped or
     * destroyed.
     * 
     * @return <code>true</code> if thread is alive, <code>false</code> if
     *         not.
     * @see #start()
     * @see #stop()
     * @see #suspend()
     * @see #interrupt()
     */
    public final boolean isAlive() {
        return vmThread.isAlive();
    }

    public final boolean isDaemon() {
        return daemon;
    }

    /**
     * Has this thread been interrupted
     * @return
     */
    public boolean isInterrupted() {
        return vmThread.isInterrupted(false);
    }

    /**
     * Is this thread in the running state?
     * 
     * @return boolean
     */
    public final boolean isRunning() {
        return vmThread.isRunning() || vmThread.isYielding();
    }

    /**
     * Is this thread waiting in a monitor?
     * 
     * @return boolean
     */
    public boolean isWaiting() {
        return vmThread.isWaiting();
    }

    /**
     * Join with the current thread. The calling thread will block until this
     * thread dies. XXX wait()s on the thread. Seems like this would cause a
     * spurious wakeup if some code synchronizes on the thread and uses
     * wait/notify for some other purpose.
     */
    public final synchronized void join() throws InterruptedException {
        // Test interrupted status
        if (vmThread.isInterrupted(true)) {
            throw new InterruptedException();
        }
        
        while (isAlive()) {
            /* wait sets this.state = WAITING; */
            wait();
            /* wait sets this.state = RUNNING; */
        }
    }

    public final synchronized void join(long millis)
            throws InterruptedException {
        if (millis == 0) {
            join();
        } else {
            // Test interrupted status
            if (vmThread.isInterrupted(true)) {
                throw new InterruptedException();
            }
            
            final long stopTime = VmSystem.currentKernelMillis() + millis;
            while (isAlive() && (millis > 0)) {
                /* wait sets this.state = WAITING; */
                wait(millis);
                millis = stopTime - VmSystem.currentKernelMillis();
                /* wait sets this.state = RUNNING; */
            }
        }
    }

    public final void join(long millis, int nanos) throws InterruptedException {
        join(millis);
    }

    /**
     * Method called only by the VM to let the object do some java-level
     * cleanup.
     */
    public final void onExit() {
        if (vmThread.isStopping()) {
            group.removeThread(this);
        }
    }

    /**
     * Resume this thread after a suspension.
     * 
     * @deprecated
     */
    public final void resume() {
        vmThread.resume();
    }

    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * Sets the context ClassLoader for this Thread. The context ClassLoader can
     * be set when a thread is created, and allows the creator of the thread to
     * provide the appropriate class loader to code running in the thread when
     * loading classes and resources. First, if there is a security manager, its
     * checkPermission method is called with a
     * RuntimePermission("setContextClassLoader") permission to see if it's ok
     * to set the context ClassLoader.
     * 
     * @param loader
     */
    public void setContextClassLoader(ClassLoader loader)
            throws SecurityException {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        this.contextClassLoader = loader;
    }

    public final void setDaemon(boolean daemon) {
        checkAccess();
        if (isAlive())
            throw new IllegalThreadStateException("already started");
        this.daemon = daemon;
    }

    public final void setName(String name) {
        checkAccess();
        this.name = name;
        this.vmThread.updateName();
    }

    public final void setPriority(int priority) {
        vmThread.setPriority(priority);
    }

    /**
     * Start this thread object. After calling this method there will be a new
     * active thread in the system (assuming things go well.) The
     * <code>Runnable</code> object, or the Thread's <code>run()</code>
     * method will be started in a new thread.
     */
    public void start() {
        vmThread.start();
    }

    /**
     * Stop this thread.
     * 
     * @deprecated
     */
    public final void stop() {
        vmThread.stop(new ThreadDeath());
    }

    /**
     * The polite way to kill a thread.
     * 
     * @param error
     *            <code>Throwable</code> to toss in stopped thread.
     * @deprecated
     */
    public final void stop(Throwable error) {
        checkAccess();

        if (error == null) {
            throw new NullPointerException("Throwable is null");
        }
        vmThread.stop(error);
    }

    /**
     * Suspend this thread
     * 
     * @deprecated
     */
    public final void suspend() {
        vmThread.suspend();
    }

    public String toString() {
        return "Thread[" + name + "," + getPriority() + "," + group.getName()
                + "," + vmThread + "]";
    }

    /**
     * Returns the map used by ThreadLocal to store the thread local values.
     */
    static Map getThreadLocals() {
        Thread thread = currentThread();
        Map locals = thread.locals;
        if (locals == null) {
            locals = thread.locals = new WeakHashMap();
        }
        return locals;
    }
  /**
   * <p>
   * Represents the current state of a thread, according to the VM rather
   * than the operating system.  It can be one of the following:
   * </p>
   * <ul>
   * <li>NEW -- The thread has just been created but is not yet running.</li>
   * <li>RUNNABLE -- The thread is currently running or can be scheduled
   * to run.</li>
   * <li>BLOCKED -- The thread is blocked waiting on an I/O operation
   * or to obtain a lock.</li>
   * <li>WAITING -- The thread is waiting indefinitely for another thread
   * to do something.</li>
   * <li>TIMED_WAITING -- The thread is waiting for a specific amount of time
   * for another thread to do something.</li>
   * <li>TERMINATED -- The thread has exited.</li>
   * </ul>
   *
   * @since 1.5
   */  
  public enum State
  {
    BLOCKED, NEW, RUNNABLE, TERMINATED, TIMED_WAITING, WAITING;

    /**
     * For compatability with Sun's JDK
     */
    private static final long serialVersionUID = 605505746047245783L;
  }


      /**
   * Returns the current state of the thread.  This
   * is designed for monitoring thread behaviour, rather
   * than for synchronization control.
   *
   * @return the current thread state.
   */
  public State getState()
  {
      //todo implement
    throw new UnsupportedClassVersionError();
  }
}
