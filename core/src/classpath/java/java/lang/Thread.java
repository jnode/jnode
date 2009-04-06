/* Thread -- an independent thread of executable code
   Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006
   Free Software Foundation
 
This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

package java.lang;

import java.util.Map;
import java.util.HashMap;


import sun.security.util.SecurityConstants;
import sun.nio.ch.Interruptible;

/* Written using "Java Class Libraries", 2nd edition, ISBN 0-201-31002-3
* "The Java Language Specification", ISBN 0-201-63451-1
* plus online API docs for JDK 1.2 beta from http://www.javasoft.com.
* Status:  Believed complete to version 1.4, with caveats. We do not
*          implement the deprecated (and dangerous) stop, suspend, and resume
*          methods. Security implementation is not complete.
*/

/**
 * Thread represents a single thread of execution in the VM. When an
 * application VM starts up, it creates a non-daemon Thread which calls the
 * main() method of a particular class.  There may be other Threads running,
 * such as the garbage collection thread.
 *
 * <p>Threads have names to identify them.  These names are not necessarily
 * unique. Every Thread has a priority, as well, which tells the VM which
 * Threads should get more running time. New threads inherit the priority
 * and daemon status of the parent thread, by default.
 *
 * <p>There are two methods of creating a Thread: you may subclass Thread and
 * implement the <code>run()</code> method, at which point you may start the
 * Thread by calling its <code>start()</code> method, or you may implement
 * <code>Runnable</code> in the class you want to use and then call new
 * <code>Thread(your_obj).start()</code>.
 *
 * <p>The virtual machine runs until all non-daemon threads have died (either
 * by returning from the run() method as invoked by start(), or by throwing
 * an uncaught exception); or until <code>System.exit</code> is called with
 * adequate permissions.
 *
 * <p>It is unclear at what point a Thread should be added to a ThreadGroup,
 * and at what point it should be removed. Should it be inserted when it
 * starts, or when it is created?  Should it be removed when it is suspended
 * or interrupted?  The only thing that is clear is that the Thread should be
 * removed when it is stopped.
 *
 * @author Tom Tromey
 * @author John Keiser
 * @author Eric Blake (ebb9@email.byu.edu)
 * @author Andrew John Hughes (gnu_andrew@member.fsf.org)
 * @see Runnable
 * @see Runtime#exit(int)
 * @see #run()
 * @see #start()
 * @see ThreadLocal
 * @since 1.0
 */
public class Thread implements Runnable 
{
  /** The minimum priority for a Thread. */
    public static final int MIN_PRIORITY = 1;

  /** The priority a Thread gets by default. */
    public static final int NORM_PRIORITY = 5;

  /** The maximum priority for a Thread. */
    public static final int MAX_PRIORITY = 10;

    private static final String NONAME = "Thread";

    /** The VM thread implementing this thread */
    final Object vmThread;

    /** The group this thread belongs to.
     *
     */
    ThreadGroup group;

  /** The object to run(), null if this is the target. */
    final Runnable runnable;

    /** The name of this thread */
    String name;

    /** Is this a daemon thread? */
    private boolean daemon;

    /** The context classloader of this thread */
    private ClassLoader contextClassLoader;

    /** The thread in which I was created */
    private final Thread parent;

    /** The default exception handler.  */
    private static UncaughtExceptionHandler defaultHandler;

     /* ThreadLocal values pertaining to this thread. This map is maintained
     * by the ThreadLocal class. */
    ThreadLocal.ThreadLocalMap threadLocals = null;

    /*
     * InheritableThreadLocal values pertaining to this thread. This map is
     * maintained by the InheritableThreadLocal class.
     */
    ThreadLocal.ThreadLocalMap inheritableThreadLocals = null;

    /** The uncaught exception handler.  */
    UncaughtExceptionHandler exceptionHandler;

    /**
     * Name and number of threads created. Used only for generating unique names.
     *
     * @see java.lang.Thread#autoName(String)
     */
    private static final HashMap<String, Integer> nameMap = new HashMap<String, Integer>();

    /**
   * Allocates a new <code>Thread</code> object. This constructor has
   * the same effect as <code>Thread(null, null,</code>
   * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is
   * a newly generated name. Automatically generated names are of the
   * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer.
   * <p>
   * Threads created this way must have overridden their
   * <code>run()</code> method to actually do anything.  An example
   * illustrating this method being used follows:
   * <p><blockquote><pre>
   *     import java.lang.*;
   *
   *     class plain01 implements Runnable {
   *         String name;
   *         plain01() {
   *             name = null;
   *         }
   *         plain01(String s) {
   *             name = s;
   *         }
   *         public void run() {
   *             if (name == null)
   *                 System.out.println("A new thread created");
   *             else
   *                 System.out.println("A new thread with name " + name +
   *                                    " created");
   *         }
   *     }
   *     class threadtest01 {
   *         public static void main(String args[] ) {
   *             int failed = 0 ;
   *
   *             <b>Thread t1 = new Thread();</b>
   *             if (t1 != null)
   *                 System.out.println("new Thread() succeed");
   *             else {
   *                 System.out.println("new Thread() failed");
   *                 failed++;
   *             }
   *         }
   *     }
   * </pre></blockquote>
   *
   * @see     java.lang.Thread#Thread(java.lang.ThreadGroup,
   *          java.lang.Runnable, java.lang.String)
   */
    public Thread()
    {
        this(null, null, NONAME);
    }

    /**
   * Allocates a new <code>Thread</code> object. This constructor has
   * the same effect as <code>Thread(null, target,</code>
   * <i>gname</i><code>)</code>, where <i>gname</i> is
   * a newly generated name. Automatically generated names are of the
   * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer.
     * 
   * @param target the object whose <code>run</code> method is called.
   * @see java.lang.Thread#Thread(java.lang.ThreadGroup,
   *                              java.lang.Runnable, java.lang.String)
     */
    public Thread(Runnable target)
    {
        this(null, target, NONAME);
    }

    /**
   * Allocates a new <code>Thread</code> object. This constructor has
   * the same effect as <code>Thread(null, null, name)</code>.
     *
   * @param   name   the name of the new thread.
   * @see     java.lang.Thread#Thread(java.lang.ThreadGroup,
   *          java.lang.Runnable, java.lang.String)
     */
    public Thread(String name)
    {
        this(null, null, name);
    }

    /**
   * Allocates a new <code>Thread</code> object. This constructor has
   * the same effect as <code>Thread(group, target,</code>
   * <i>gname</i><code>)</code>, where <i>gname</i> is
   * a newly generated name. Automatically generated names are of the
   * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer.
     *
   * @param group the group to put the Thread into
   * @param target the Runnable object to execute
   * @throws SecurityException if this thread cannot access <code>group</code>
   * @throws IllegalThreadStateException if group is destroyed
   * @see #Thread(ThreadGroup, Runnable, String)
     */
    public Thread(ThreadGroup group, Runnable target)
    {
        this(group, target, NONAME);
    }

    /**
   * Allocates a new <code>Thread</code> object. This constructor has
   * the same effect as <code>Thread(group, null, name)</code>
     *
   * @param group the group to put the Thread into
   * @param name the name for the Thread
   * @throws NullPointerException if name is null
   * @throws SecurityException if this thread cannot access <code>group</code>
   * @throws IllegalThreadStateException if group is destroyed
   * @see #Thread(ThreadGroup, Runnable, String)
     */
    public Thread(ThreadGroup group, String name)
    {
        this(group, null, name);
    }

    /**
   * Allocates a new <code>Thread</code> object. This constructor has
   * the same effect as <code>Thread(null, target, name)</code>.
     * 
   * @param target the Runnable object to execute
   * @param name the name for the Thread
   * @throws NullPointerException if name is null
   * @see #Thread(ThreadGroup, Runnable, String)
     */
    public Thread(Runnable target, String name)
    {
        this(null, target, name);
    }

    /**
   * Allocate a new Thread object, with the specified ThreadGroup and name, and
   * using the specified Runnable object's <code>run()</code> method to
   * execute.  If the Runnable object is null, <code>this</code> (which is
   * a Runnable) is used instead.
     * 
   * <p>If the ThreadGroup is null, the security manager is checked. If a
   * manager exists and returns a non-null object for
   * <code>getThreadGroup</code>, that group is used; otherwise the group
   * of the creating thread is used. Note that the security manager calls
   * <code>checkAccess</code> if the ThreadGroup is not null.
   *
   * <p>The new Thread will inherit its creator's priority and daemon status.
   * These can be changed with <code>setPriority</code> and
   * <code>setDaemon</code>.
   *
   * @param group the group to put the Thread into
   * @param target the Runnable object to execute
   * @param name the name for the Thread
   * @throws NullPointerException if name is null
   * @throws SecurityException if this thread cannot access <code>group</code>
   * @throws IllegalThreadStateException if group is destroyed
   * @see Runnable#run()
   * @see #run()
   * @see #setDaemon(boolean)
   * @see #setPriority(int)
   * @see SecurityManager#checkAccess(ThreadGroup)
   * @see ThreadGroup#checkAccess()
     */
    public Thread(ThreadGroup group, Runnable target, String name)
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

        group.addUnstarted();

        this.group = group;
        this.runnable = target;
        this.name = autoName(name);
        this.parent = current;

        this.daemon = current.isDaemon();

        this.vmThread  = createVmThread0(current);
        setPriority(current.getPriority());
        updateName0();


        if (parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals = ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
    }

    private native Object createVmThread0(Thread current);

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

        group.addUnstarted();

        this.group = group;
        this.runnable = target;
        this.name = autoName(name);
        this.parent = current;

        this.daemon = current.isDaemon();

      this.vmThread = createVmThread0(current);
      setPriority(current.getPriority());
      updateName0();


      if (parent.inheritableThreadLocals != null)
            this.inheritableThreadLocals = ThreadLocal.createInheritedMap(parent.inheritableThreadLocals);
  }

    /**
     * Create a new instance with a given group as containing group, a runnable
     * as thread runner and a given name.
     * 
     * @param group
     * @param target
     * @param name
     */
    protected Thread(ThreadGroup group, Runnable target, String name, Object isolatedStatics) {
        /*
        if (!(this instanceof IsolateThread)) {
            throw new SecurityException("Constructor can only be called from IsolateThread");
        }
        */
        if (!(this.getClass().getName().startsWith("org.jnode.vm.isolate"))) {
            throw new SecurityException("Constructor can only be called from IsolateThread");
        }
        if (group == null) {
            throw new InternalError("Isolate thread has invalid group: " + name);
        }

        group.addUnstarted();

        this.group = group;
        this.runnable = target;
        this.name = autoName(name);
        this.parent = null;
        this.daemon = false;

        this.vmThread  = createVmThread1(isolatedStatics);
        setPriority(this.getPriority());
        updateName0();

        //todo review it: should thread locals be inherited accorss isolates? Probably not... 
        ThreadLocal.ThreadLocalMap parentLocals = currentThread().inheritableThreadLocals;
        if (parentLocals != null)
            this.inheritableThreadLocals = ThreadLocal.createInheritedMap(parentLocals);
    }

    private native Object createVmThread1(Object isolatedStatics);

    /**
     * Create a new thread from a given VmThread. Only used for the main thread.
     * 
     * @param vmThread
     * @throws IllegalArgumentException
     *             If the given vmThread is not the root thread or the given
     *             vmThread already has an associated java thread.
     */
    public Thread(Object vmThread) throws IllegalArgumentException {
        checkArg0(vmThread);
        this.vmThread = vmThread;
        this.group = ROOT_GROUP;
        this.group.addUnstarted();
        this.name = autoName("System");
        this.runnable = null;
        this.parent = null;
        updateName0();
    }

    private static native void checkArg0(Object vmThread);

    private native void updateName0();
    /**
   * Get the number of active threads in the current Thread's ThreadGroup.
   * This implementation calls
   * <code>currentThread().getThreadGroup().activeCount()</code>.
   *
   * @return the number of active threads in the current ThreadGroup
   * @see ThreadGroup#activeCount()
   */
    public static int activeCount()
        {
        return currentThread().getThreadGroup().activeCount();
    }

    /**
   * Check whether the current Thread is allowed to modify this Thread. This
   * passes the check on to <code>SecurityManager.checkAccess(this)</code>.
   *
   * @throws SecurityException if the current Thread cannot modify this Thread
   * @see SecurityManager#checkAccess(Thread)
     */
  public final void checkAccess()
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkAccess(this);
        }
    }

    /**
   * Count the number of stack frames in this Thread.  The Thread in question
   * must be suspended when this occurs.
     *
   * @return the number of stack frames in this Thread
   * @throws IllegalThreadStateException if this Thread is not suspended
   * @deprecated pointless, since suspend is deprecated
     */
    public native int countStackFrames();

    /**
   * Get the currently executing Thread. In the situation that the
   * currently running thread was created by native code and doesn't
   * have an associated Thread object yet, a new Thread object is
   * constructed and associated with the native thread.
     *
   * @return the currently executing Thread
     */
    public static native Thread currentThread();

    /**
     * Originally intended to destroy this thread, this method was never
     * implemented by Sun, and is hence a no-op.
   *
   * @deprecated This method was originally intended to simply destroy
   *             the thread without performing any form of cleanup operation.
   *             However, it was never implemented.  It is now deprecated
   *             for the same reason as <code>suspend()</code>,
   *             <code>stop()</code> and <code>resume()</code>; namely,
   *             it is prone to deadlocks.  If a thread is destroyed while
   *             it still maintains a lock on a resource, then this resource
   *             will remain locked and any attempts by other threads to
   *             access the resource will result in a deadlock.  Thus, even
   *             an implemented version of this method would be still be
   *             deprecated, due to its unsafe nature.
   * @throws NoSuchMethodError as this method was never implemented.
     */
    public native void destroy();

    /**
   * Print a stack trace of the current thread to stderr using the same
   * format as Throwable's printStackTrace() method.
   *
   * @see Throwable#printStackTrace()
     */
    public static void dumpStack()
    {
    new Throwable().printStackTrace();
    }

    /**
   * Copy every active thread in the current Thread's ThreadGroup into the
   * array. Extra threads are silently ignored. This implementation calls
   * <code>getThreadGroup().enumerate(array)</code>, which may have a
   * security check, <code>checkAccess(group)</code>.
   *
   * @param array the array to place the Threads into
   * @return the number of Threads placed into the array
   * @throws NullPointerException if array is null
   * @throws SecurityException if you cannot access the ThreadGroup
   * @see ThreadGroup#enumerate(Thread[])
   * @see #activeCount()
   * @see SecurityManager#checkAccess(ThreadGroup)
   */
  public static int enumerate(Thread[] array)
    {
    return currentThread().group.enumerate(array);
    }

    /**
   * Get this Thread's name.
     *
   * @return this Thread's name
     */
    public final String getName()
    {
        return name;
    }

    /**
   * Get this Thread's priority.
     *
   * @return the Thread's priority
     */
    public native final int getPriority();

    /**
   * Get the ThreadGroup this Thread belongs to. If the thread has died, this
   * returns null.
     *
   * @return this Thread's ThreadGroup
     */
    public final ThreadGroup getThreadGroup()
    {
        return isAlive() ? group : null;
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
    public static native boolean holdsLock(Object obj);

    /**
   * Interrupt this Thread. First, there is a security check,
   * <code>checkAccess</code>. Then, depending on the current state of the
   * thread, various actions take place:
   *
   * <p>If the thread is waiting because of {@link #wait()},
   * {@link #sleep(long)}, or {@link #join()}, its <i>interrupt status</i>
   * will be cleared, and an InterruptedException will be thrown. Notice that
   * this case is only possible if an external thread called interrupt().
   *
   * <p>If the thread is blocked in an interruptible I/O operation, in
   * {@link java.nio.channels.InterruptibleChannel}, the <i>interrupt
   * status</i> will be set, and ClosedByInterruptException will be thrown.
   *
   * <p>If the thread is blocked on a {@link java.nio.channels.Selector}, the
   * <i>interrupt status</i> will be set, and the selection will return, with
   * a possible non-zero value, as though by the wakeup() method.
   *
   * <p>Otherwise, the interrupt status will be set.
   *
   * @throws SecurityException if you cannot modify this Thread
     */
    public native void interrupt();

    /**
   * Determine whether the current Thread has been interrupted, and clear
   * the <i>interrupted status</i> in the process.
   *
   * @return whether the current Thread has been interrupted
   * @see #isInterrupted()
     */
    public static native boolean interrupted();

    /**
   * Determine whether the given Thread has been interrupted, but leave
   * the <i>interrupted status</i> alone in the process.
   *
   * @return whether the Thread has been interrupted
   * @see #interrupted()
     */
    public native  boolean isInterrupted();

    /**
   * Determine whether this Thread is alive. A thread which is alive has
   * started and not yet died.
     *
   * @return whether this Thread is alive
     */
    public native final boolean isAlive();

  /**
   * Tell whether this is a daemon Thread or not.
   *
   * @return whether this is a daemon Thread or not
   * @see #setDaemon(boolean)
   */
    public final boolean isDaemon()
    {
        return daemon;
    }

    /**
   * Wait forever for the Thread in question to die.
   *
   * @throws InterruptedException if the Thread is interrupted; it's
   *         <i>interrupted status</i> will be cleared
     */
    public final synchronized void join() throws InterruptedException {
        // Test interrupted status
        if (isInterupted0()) {
            throw new InterruptedException();
        }

        while (isAlive()) {
            /* wait sets this.state = WAITING; */
            wait();
            /* wait sets this.state = RUNNING; */
        }
    }

    private native boolean isInterupted0();

    /**
   * Wait the specified amount of time for the Thread in question to die.
   *
   * @param ms the number of milliseconds to wait, or 0 for forever
   * @throws InterruptedException if the Thread is interrupted; it's
   *         <i>interrupted status</i> will be cleared
   */
    public final synchronized void join(long ms) throws InterruptedException
    {
        if (ms == 0) {
            join();
        } else {
            // Test interrupted status
            if (isInterupted0()) {
                throw new InterruptedException();
            }

            final long stopTime = currentKernelMillis0() + ms;
            while (isAlive() && (ms > 0)) {
                /* wait sets this.state = WAITING; */
                wait(ms);
                ms = stopTime - currentKernelMillis0();
                /* wait sets this.state = RUNNING; */
            }
        }
    }

    private static native long currentKernelMillis0();

    /**
   * Wait the specified amount of time for the Thread in question to die.
   *
   * <p>Note that 1,000,000 nanoseconds == 1 millisecond, but most VMs do
   * not offer that fine a grain of timing resolution. Besides, there is
   * no guarantee that this thread can start up immediately when time expires,
   * because some other thread may be active.  So don't expect real-time
   * performance.
   *
   * @param ms the number of milliseconds to wait, or 0 for forever
   * @param ns the number of extra nanoseconds to sleep (0-999999)
   * @throws InterruptedException if the Thread is interrupted; it's
   *         <i>interrupted status</i> will be cleared
   * @throws IllegalArgumentException if ns is invalid
   */
    public final void join(long ms, int ns) throws InterruptedException
    {
        join(ms);
    }

    /**
   * Resume this Thread.  If the thread is not suspended, this method does
   * nothing. To mirror suspend(), there may be a security check:
   * <code>checkAccess</code>.
     *
   * @throws SecurityException if you cannot resume the Thread
   * @see #checkAccess()
   * @see #suspend()
   * @deprecated pointless, since suspend is deprecated
     */
    public native final void resume();

  /**
   * The method of Thread that will be run if there is no Runnable object
   * associated with the Thread. Thread's implementation does nothing at all.
   *
   * @see #start()
   * @see #Thread(ThreadGroup, Runnable, String)
   */
    public void run()
    {
        if (runnable != null)
            runnable.run();        
    }

   /**
   * Set the daemon status of this Thread.  If this is a daemon Thread, then
   * the VM may exit even if it is still running.  This may only be called
   * before the Thread starts running. There may be a security check,
   * <code>checkAccess</code>.
   *
   * @param daemon whether this should be a daemon thread or not
   * @throws SecurityException if you cannot modify this Thread
   * @throws IllegalThreadStateException if the Thread is active
   * @see #isDaemon()
   * @see #checkAccess()
   */
    public final void setDaemon(boolean daemon)
    {
        checkAccess();
        if (isAlive())
            throw new IllegalThreadStateException("already started");
        this.daemon = daemon;
    }

    /**
   * Returns the context classloader of this Thread. The context
   * classloader can be used by code that want to load classes depending
   * on the current thread. Normally classes are loaded depending on
   * the classloader of the current class. There may be a security check
   * for <code>RuntimePermission("getClassLoader")</code> if the caller's
   * class loader is not null or an ancestor of this thread's context class
   * loader.
     *
   * @return the context class loader
   * @throws SecurityException when permission is denied
   * @see #setContextClassLoader(ClassLoader)
   * @since 1.2
     */
    public ClassLoader getContextClassLoader()
    {
        if (contextClassLoader != null) {
            return contextClassLoader;
        } else if (parent != null) {
            return parent.getContextClassLoader();
        } else {
            return getClass().getClassLoader();
        }
    }

    /**
   * Sets the context classloader for this Thread. When not explicitly set,
   * the context classloader for a thread is the same as the context
   * classloader of the thread that created this thread. The first thread has
   * as context classloader the system classloader. There may be a security
   * check for <code>RuntimePermission("setContextClassLoader")</code>.
     *
   * @param classloader the new context class loader
   * @throws SecurityException when permission is denied
   * @see #getContextClassLoader()
   * @since 1.2
     */
    public void setContextClassLoader(ClassLoader classloader) throws SecurityException
    {
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("setContextClassLoader"));
        }
        this.contextClassLoader = classloader;
    }

    /**
     * Set this Thread's name.  There may be a security check,
     * <code>checkAccess</code>.
     *
     * @param name the new name for this Thread
     * @throws NullPointerException if name is null
     * @throws SecurityException if you cannot modify this Thread
     */
    public final void setName(String name)
    {
        checkAccess();
        // The Class Libraries book says ``threadName cannot be null''.  I
        // take this to mean NullPointerException.
        if (name == null)
          throw new NullPointerException();
        this.name = autoName(name);
        updateName0();
    }

  /**
   * Yield to another thread. The Thread will not lose any locks it holds
   * during this time. There are no guarantees which thread will be
   * next to run, and it could even be this one, but most VMs will choose
   * the highest priority thread that has been waiting longest.
   */
    public static native void yield();

  /**
   * Suspend the current Thread's execution for the specified amount of
   * time. The Thread will not lose any locks it has during this time. There
   * are no guarantees which thread will be next to run, but most VMs will
   * choose the highest priority thread that has been waiting longest.
   *
   * @param ms the number of milliseconds to sleep, or 0 for forever
   * @throws InterruptedException if the Thread is (or was) interrupted;
   *         it's <i>interrupted status</i> will be cleared
   * @throws IllegalArgumentException if ms is negative
   * @see #interrupt()
   * @see #notify()
   * @see #wait(long)
   */
    public static void sleep(long ms) throws InterruptedException
    {
        sleep(ms, 0);
    }

    /**
   * Suspend the current Thread's execution for the specified amount of
   * time. The Thread will not lose any locks it has during this time. There
   * are no guarantees which thread will be next to run, but most VMs will
   * choose the highest priority thread that has been waiting longest.
   * <p>
   * Note that 1,000,000 nanoseconds == 1 millisecond, but most VMs
   * do not offer that fine a grain of timing resolution. When ms is
   * zero and ns is non-zero the Thread will sleep for at least one
   * milli second. There is no guarantee that this thread can start up
   * immediately when time expires, because some other thread may be
   * active.  So don't expect real-time performance.
   *
   * @param ms the number of milliseconds to sleep, or 0 for forever
   * @param ns the number of extra nanoseconds to sleep (0-999999)
   * @throws InterruptedException if the Thread is (or was) interrupted;
   *         it's <i>interrupted status</i> will be cleared
   * @throws IllegalArgumentException if ms or ns is negative
   *         or ns is larger than 999999.
   * @see #interrupt()
   * @see #notify()
   * @see #wait(long, int)
     */
    public static void sleep(long ms, int ns) throws InterruptedException
    {
    // Check parameters
    if (ms < 0 )
      throw new IllegalArgumentException("Negative milliseconds: " + ms);

    if (ns < 0 || ns > 999999)
      throw new IllegalArgumentException("Nanoseconds ouf of range: " + ns);

        sleep0(ms, ns);
    }

    private static native void sleep0(long ms, int ns) throws InterruptedException;

    /**
   * Start this Thread, calling the run() method of the Runnable this Thread
   * was created with, or else the run() method of the Thread itself. This
   * is the only way to start a new thread; calling run by yourself will just
   * stay in the same thread. The virtual machine will remove the thread from
   * its thread group when the run() method completes.
   *
   * @throws IllegalThreadStateException if the thread has already started
   * @see #run()
     */
    public void start()
    {
        group.add(this);
        start0();
    }

    private native void start0();

    /**
   * Cause this Thread to stop abnormally because of the throw of a ThreadDeath
   * error. If you stop a Thread that has not yet started, it will stop
   * immediately when it is actually started.
     *
   * <p>This is inherently unsafe, as it can interrupt synchronized blocks and
   * leave data in bad states.  Hence, there is a security check:
   * <code>checkAccess(this)</code>, plus another one if the current thread
   * is not this: <code>RuntimePermission("stopThread")</code>. If you must
   * catch a ThreadDeath, be sure to rethrow it after you have cleaned up.
   * ThreadDeath is the only exception which does not print a stack trace when
   * the thread dies.
   *
   * @throws SecurityException if you cannot stop the Thread
   * @see #interrupt()
   * @see #checkAccess()
   * @see #start()
   * @see ThreadDeath
   * @see ThreadGroup#uncaughtException(Thread, Throwable)
   * @see SecurityManager#checkAccess(Thread)
   * @see SecurityManager#checkPermission(java.security.Permission)
   * @deprecated unsafe operation, try not to use
     */
    public native final void stop();

    /**
   * Cause this Thread to stop abnormally and throw the specified exception.
   * If you stop a Thread that has not yet started, the stop is ignored
   * (contrary to what the JDK documentation says).
   * <b>WARNING</b>This bypasses Java security, and can throw a checked
   * exception which the call stack is unprepared to handle. Do not abuse
   * this power.
     *
   * <p>This is inherently unsafe, as it can interrupt synchronized blocks and
   * leave data in bad states.  Hence, there is a security check:
   * <code>checkAccess(this)</code>, plus another one if the current thread
   * is not this: <code>RuntimePermission("stopThread")</code>. If you must
   * catch a ThreadDeath, be sure to rethrow it after you have cleaned up.
   * ThreadDeath is the only exception which does not print a stack trace when
   * the thread dies.
   *
   * @param t the Throwable to throw when the Thread dies
   * @throws SecurityException if you cannot stop the Thread
   * @throws NullPointerException in the calling thread, if t is null
   * @see #interrupt()
   * @see #checkAccess()
   * @see #start()
   * @see ThreadDeath
   * @see ThreadGroup#uncaughtException(Thread, Throwable)
   * @see SecurityManager#checkAccess(Thread)
   * @see SecurityManager#checkPermission(java.security.Permission)
   * @deprecated unsafe operation, try not to use
     */
    public final void stop(Throwable t)
    {
        checkAccess();

        if (t == null) {
            throw new NullPointerException("Throwable is null");
        }
        stop0(t);
    }

    private native void stop0(Throwable t);

    /**
   * Suspend this Thread.  It will not come back, ever, unless it is resumed.
     *
   * <p>This is inherently unsafe, as the suspended thread still holds locks,
   * and can potentially deadlock your program.  Hence, there is a security
   * check: <code>checkAccess</code>.
   *
   * @throws SecurityException if you cannot suspend the Thread
   * @see #checkAccess()
   * @see #resume()
   * @deprecated unsafe operation, try not to use
     */
    public native final void suspend();

    /**
   * Set this Thread's priority. There may be a security check,
   * <code>checkAccess</code>, then the priority is set to the smaller of
   * priority and the ThreadGroup maximum priority.
   *
   * @param priority the new priority for this Thread
   * @throws IllegalArgumentException if priority exceeds MIN_PRIORITY or
   *         MAX_PRIORITY
   * @throws SecurityException if you cannot modify this Thread
   * @see #getPriority()
   * @see #checkAccess()
   * @see ThreadGroup#getMaxPriority()
   * @see #MIN_PRIORITY
   * @see #MAX_PRIORITY
   */
    public native final void setPriority(int priority);

    /**
   * Returns a string representation of this thread, including the
   * thread's name, priority, and thread group.
   *
   * @return a human-readable String representing this Thread
   */
    public String toString()
    {
        return "Thread[" + name + "," + getPriority() + "," + group.getName()
                + "," + vmThread + "]";
    }

    /**
   * Assigns the given <code>UncaughtExceptionHandler</code> to this
   * thread.  This will then be called if the thread terminates due
   * to an uncaught exception, pre-empting that of the
   * <code>ThreadGroup</code>.
   *
   * @param h the handler to use for this thread.
   * @throws SecurityException if the current thread can't modify this thread.
   * @since 1.5
   */
  public void setUncaughtExceptionHandler(UncaughtExceptionHandler h)
  {
    SecurityManager sm = SecurityManager.current; // Be thread-safe.
    if (sm != null)
      sm.checkAccess(this);
    exceptionHandler = h;
  }

    /**
   * <p>
   * Returns the handler used when this thread terminates due to an
   * uncaught exception.  The handler used is determined by the following:
   * </p>
   * <ul>
   * <li>If this thread has its own handler, this is returned.</li>
   * <li>If not, then the handler of the thread's <code>ThreadGroup</code>
   * object is returned.</li>
   * <li>If both are unavailable, then <code>null</code> is returned
   *     (which can only happen when the thread was terminated since
   *      then it won't have an associated thread group anymore).</li>
   * </ul>
   *
   * @return the appropriate <code>UncaughtExceptionHandler</code> or
   *         <code>null</code> if one can't be obtained.
   * @since 1.5
   */
  public UncaughtExceptionHandler getUncaughtExceptionHandler()
  {
    return exceptionHandler != null ? exceptionHandler : group;
  }

    /**
   * <p>
   * Sets the default uncaught exception handler used when one isn't
   * provided by the thread or its associated <code>ThreadGroup</code>.
   * This exception handler is used when the thread itself does not
   * have an exception handler, and the thread's <code>ThreadGroup</code>
   * does not override this default mechanism with its own.  As the group
   * calls this handler by default, this exception handler should not defer
   * to that of the group, as it may lead to infinite recursion.
   * </p>
   * <p>
   * Uncaught exception handlers are used when a thread terminates due to
   * an uncaught exception.  Replacing this handler allows default code to
   * be put in place for all threads in order to handle this eventuality.
   * </p>
   *
   * @param h the new default uncaught exception handler to use.
   * @throws SecurityException if a security manager is present and
   *                           disallows the runtime permission
   *                           "setDefaultUncaughtExceptionHandler".
   * @since 1.5
   */
  public static void
    setDefaultUncaughtExceptionHandler(UncaughtExceptionHandler h)
  {
    SecurityManager sm = SecurityManager.current; // Be thread-safe.
    if (sm != null)
      sm.checkPermission(new RuntimePermission("setDefaultUncaughtExceptionHandler"));
    defaultHandler = h;
  }

  /**
   * Returns the handler used by default when a thread terminates
   * unexpectedly due to an exception, or <code>null</code> if one doesn't
   * exist.
   *
   * @return the default uncaught exception handler.
   * @since 1.5
   */
  public static UncaughtExceptionHandler getDefaultUncaughtExceptionHandler()
  {
    return defaultHandler;
  }

    /**
     * Is this thread in the running state?
     * 
     * @return boolean
     */
    public native final boolean isRunning();

    /**
     * Is this thread waiting in a monitor?
     * 
     * @return boolean
     */
    public native boolean isWaiting();

    /**
     * Method called only by the VM to let the object do some java-level
     * cleanup.
     */
    public final void onExit() {
        if (isStopping0()) {
            group.remove(this);
            threadLocals = null;
            inheritableThreadLocals = null;
        }
    }

    private native boolean isStopping0();

    /**
     * Generate a "unique" name for a thread.
     */
    private static synchronized String autoName(String name) {
        Integer count = nameMap.get(name);
        if(count == null){
            nameMap.put(name, 1);
            return name;
        }

        nameMap.put(name, count + 1);

        return name + "-" + count;
    }

  /**
   * Returns the unique identifier for this thread.  This ID is generated
   * on thread creation, and may be re-used on its death.
     *
   * @return a positive long number representing the thread's ID.
     * @since 1.5
     */
  public native long getId();

    //openjdk
    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];
    //openjdk
    public StackTraceElement[] getStackTrace() {
        if (this != Thread.currentThread()) {
            // check for getStackTrace permission
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(new RuntimePermission("getStackTrace"));
            }
            if (!isAlive()) {
                return EMPTY_STACK_TRACE;
            }
	        return getStackTrace0();
        } else {
	        // Don't need JVM help for current thread
	        return (new Exception()).getStackTrace();
	    }
    }

    private native StackTraceElement[] getStackTrace0();

    /**
   * <p>
   * This interface is used to handle uncaught exceptions
   * which cause a <code>Thread</code> to terminate.  When
   * a thread, t, is about to terminate due to an uncaught
   * exception, the virtual machine looks for a class which
   * implements this interface, in order to supply it with
   * the dying thread and its uncaught exception.
   * </p>
   * <p>
   * The virtual machine makes two attempts to find an
   * appropriate handler for the uncaught exception, in
   * the following order:
   * </p>
   * <ol>
   * <li>
   * <code>t.getUncaughtExceptionHandler()</code> --
   * the dying thread is queried first for a handler
   * specific to that thread.
   * </li>
   * <li>
   * <code>t.getThreadGroup()</code> --
   * the thread group of the dying thread is used to
   * handle the exception.  If the thread group has
   * no special requirements for handling the exception,
   * it may simply forward it on to
   * <code>Thread.getDefaultUncaughtExceptionHandler()</code>,
   * the default handler, which is used as a last resort.
   * </li>
   * </ol>
   * <p>
   * The first handler found is the one used to handle
   * the uncaught exception.
   * </p>
   *
   * @author Tom Tromey <tromey@redhat.com>
   * @author Andrew John Hughes <gnu_andrew@member.fsf.org>
   * @since 1.5
   * @see Thread#getUncaughtExceptionHandler()
   * @see Thread#setUncaughtExceptionHandler(UncaughtExceptionHandler)
   * @see Thread#getDefaultUncaughtExceptionHandler()
   * @see
   * Thread#setDefaultUncaughtExceptionHandler(java.lang.Thread.UncaughtExceptionHandler)
   */
  public interface UncaughtExceptionHandler
  {
    /**
     * Invoked by the virtual machine with the dying thread
     * and the uncaught exception.  Any exceptions thrown
     * by this method are simply ignored by the virtual
     * machine.
     *
     * @param thr the dying thread.
     * @param exc the uncaught exception.
     */
    void uncaughtException(Thread thr, Throwable exc);
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
    NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING,  TERMINATED
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

    //jnode + openjdk
    static final ThreadGroup ROOT_GROUP;
    static {
        ThreadGroup g = null;
        try {
            /*
            Constructor<ThreadGroup> constr = ThreadGroup.class.getDeclaredConstructor();
            constr.setAccessible(true);
            g = constr.newInstance();
            constr.setAccessible(false);
                        */
            g = new ThreadGroup();
        }catch (Exception e){
            e.printStackTrace();
            die0();
        }
        ROOT_GROUP = g;
    }

    private static native void die0();

    /**
     * The argument supplied to the current call to
     * java.util.concurrent.locks.LockSupport.park.
     * Set by (private) java.util.concurrent.locks.LockSupport.setBlocker
     * Accessed using java.util.concurrent.locks.LockSupport.getBlocker
     */
    volatile Object parkBlocker;

    /**
     * Returns a map of stack traces for all live threads.
     * The map keys are threads and each map value is an array of
     * <tt>StackTraceElement</tt> that represents the stack dump
     * of the corresponding <tt>Thread</tt>.
     * The returned stack traces are in the format specified for
     * the {@link #getStackTrace getStackTrace} method.
     *
     * <p>The threads may be executing while this method is called.
     * The stack trace of each thread only represents a snapshot and
     * each stack trace may be obtained at different time.  A zero-length
     * array will be returned in the map value if the virtual machine has
     * no stack trace information about a thread.
     *
     * <p>If there is a security manager, then the security manager's
     * <tt>checkPermission</tt> method is called with a
     * <tt>RuntimePermission("getStackTrace")</tt> permission as well as
     * <tt>RuntimePermission("modifyThreadGroup")</tt> permission
     * to see if it is ok to get the stack trace of all threads.
     *
     * @return a <tt>Map</tt> from <tt>Thread</tt> to an array of
     * <tt>StackTraceElement</tt> that represents the stack trace of
     * the corresponding thread.
     *
     * @throws SecurityException
     *        if a security manager exists and its
     *        <tt>checkPermission</tt> method doesn't allow
     *        getting the stack trace of thread.
     * @see #getStackTrace
     * @see SecurityManager#checkPermission
     * @see RuntimePermission
     * @see Throwable#getStackTrace
     *
     * @since 1.5
     */
    public static Map<Thread, StackTraceElement[]> getAllStackTraces() {
        // check for getStackTrace permission
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(
                SecurityConstants.GET_STACK_TRACE_PERMISSION);
            security.checkPermission(
                SecurityConstants.MODIFY_THREADGROUP_PERMISSION);
        }

        // Get a snapshot of the list of all threads
        Thread[] threads = getThreads();
        StackTraceElement[][] traces = dumpThreads(threads);
        Map<Thread, StackTraceElement[]> m
	    = new HashMap<Thread, StackTraceElement[]>(threads.length);
        for (int i = 0; i < threads.length; i++) {
            if (threads[i].isAlive()) {
                StackTraceElement[] stackTrace = traces[i];
                if (stackTrace == null) {
                    stackTrace = EMPTY_STACK_TRACE;
                }
                m.put(threads[i], stackTrace);
            }
        }
        return m;
    }

    private static StackTraceElement[][] dumpThreads(Thread[] threads) {
        //todo implement it
        throw new UnsupportedOperationException();
    }
    private static Thread[] getThreads() {
        //todo implement it
        throw new UnsupportedOperationException();
    }

    /* Set the blocker field; invoked via sun.misc.SharedSecrets from java.nio code
     */
    void blockedOn(Interruptible b) {
        //todo implement it
//        synchronized (blockerLock) {
  //          blocker = b;
    //    }
    }
}
