/* VMVirtualMachine.java -- A reference implementation of a JDWP virtual
   machine

   Copyright (C) 2005, 2006 Free Software Foundation

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
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */


package gnu.classpath.jdwp;

import gnu.classpath.jdwp.event.EventRequest;
import gnu.classpath.jdwp.exception.InvalidMethodException;
import gnu.classpath.jdwp.exception.JdwpException;
import gnu.classpath.jdwp.util.MethodResult;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.DataOutputStream;

/**
 * A virtual machine according to JDWP.
 *
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class VMVirtualMachine
{
  /**
   * Suspend a thread
   *
   * @param  thread  the thread to suspend
   */
  public static native void suspendThread (Thread thread)
    throws JdwpException;

  /**
   * Suspend all threads
   */
  public static void suspendAllThreads ()
    throws JdwpException
  {
    // Our JDWP thread group -- don't suspend any of those threads
    Thread current = Thread.currentThread ();
    ThreadGroup jdwpGroup = current.getThreadGroup ();

    // Find the root ThreadGroup
    ThreadGroup group = jdwpGroup;
    ThreadGroup parent = group.getParent ();
    while (parent != null)
      {
	group = parent;
	parent = group.getParent ();
      }
    
    // Get all the threads in the system
    int num = group.activeCount ();
    Thread[] threads = new Thread[num];
    group.enumerate (threads);

    for (int i = 0; i < num; ++i)
      {
	Thread t = threads[i];
	if (t != null)
	  {
	    if (t.getThreadGroup () == jdwpGroup || t == current)
	      {
		// Don't suspend the current thread or any JDWP thread
		continue;
	      }
	    else
	      suspendThread (t);
	  }
      }

    // Now suspend the current thread
    suspendThread (current);
  }

  /**
   * Resume a thread. A thread must be resumed as many times
   * as it has been suspended.
   *
   * @param  thread  the thread to resume
   */
  public static native void resumeThread (Thread thread)
    throws JdwpException;

  /**
   * Resume all threads. This simply decrements the thread's
   * suspend count. It can not be used to force the application
   * to run.
   */
  public static void resumeAllThreads ()
    throws JdwpException
  {
    // Our JDWP thread group -- don't resume
    Thread current = Thread.currentThread ();
    ThreadGroup jdwpGroup = current.getThreadGroup ();

    // Find the root ThreadGroup
    ThreadGroup group = jdwpGroup;
    ThreadGroup parent = group.getParent ();
    while (parent != null)
      {
	group = parent;
	parent = group.getParent ();
      }
    
    // Get all the threads in the system
    int num = group.activeCount ();
    Thread[] threads = new Thread[num];
    group.enumerate (threads);

    for (int i = 0; i < num; ++i)
      {
	Thread t = threads[i];
	if (t != null)
	  {
	    if (t.getThreadGroup () == jdwpGroup || t == current)
	      {
		// Don't resume the current thread or any JDWP thread
		continue;
	      }
	    else
	      resumeThread (t);
	  }
      }
  }

  /**
   * Get the suspend count for a give thread
   *
   * @param  thread  the thread whose suspend count is desired
   * @return the number of times the thread has been suspended
   */
  public static native int getSuspendCount (Thread thread)
    throws JdwpException;
 
  /**
   * Returns a count of the number of loaded classes in the VM
   */
  public static native int getAllLoadedClassesCount ()
    throws JdwpException;

  /**
   * Returns an iterator over all the loaded classes in the VM
   */
  public static native Iterator getAllLoadedClasses ()
    throws JdwpException;

  /**
   * Returns the status of the given class
   *
   * @param  clazz  the class whose status is desired
   * @return a flag containing the class's status
   * @see JdwpConstants.ClassStatus
   */
  public static native int getClassStatus (Class clazz)
    throws JdwpException;

  /**
   * Returns all of the methods defined in the given class. This
   * includes all methods, constructors, and class initializers.
   *
   * @param  klass  the class whose methods are desired
   * @return an array of virtual machine methods
   */
  public static native VMMethod[] getAllClassMethods (Class klass)
    throws JdwpException;

  /**
   * A factory method for getting valid virtual machine methods
   * which may be passed to/from the debugger.
   *
   * @param klass the class in which the method is defined
   * @param id    the ID of the desired method
   * @return the desired internal representation of the method
   * @throws InvalidMethodException if the method is not defined
   *           in the class
   * @throws JdwpException for any other error
   */
  public static native VMMethod getClassMethod(Class klass, long id)
    throws JdwpException;

  /**
   * Returns the thread's call stack
   *
   * @param  thread  thread for which to get call stack
   * @param  start   index of first frame to return
   * @param  length  number of frames to return (-1 for all frames)
   * @return a list of frames
   */
  public static native ArrayList getFrames (Thread thread, int start,
					    int length)
    throws JdwpException;

  /**
   * Returns the frame for a given thread with the frame ID in
   * the buffer
   *
   * I don't like this.
   *
   * @param  thread  the frame's thread
   * @param  bb      buffer containing the frame's ID
   * @return the desired frame
   */
  public static native VMFrame getFrame (Thread thread, ByteBuffer bb)
    throws JdwpException;

  /**
   * Returns the number of frames in the thread's stack
   *
   * @param  thread  the thread for which to get a frame count
   * @return the number of frames in the thread's stack
   */
  public static native int getFrameCount (Thread thread)
    throws JdwpException;


  /**
   * Returns the status of a thread
   *
   * @param  thread  the thread for which to get status
   * @return integer status of the thread
   * @see JdwpConstants.ThreadStatus
   */
  public static native int getThreadStatus (Thread thread)
    throws JdwpException;

  /**
   * Returns a list of all classes which this class loader has been
   * requested to load
   *
   * @param  cl  the class loader
   * @return a list of all visible classes
   */
  public static native ArrayList getLoadRequests (ClassLoader cl)
    throws JdwpException;

  /**
   * Executes a method in the virtual machine
   *
   * @param  obj         instance in which to invoke method (null for static)
   * @param  thread      the thread in which to invoke the method
   * @param  clazz       the class in which the method is defined
   * @param  method      the method to invoke
   * @param  values      arguments to pass to method
   * @param  nonVirtual  "otherwise, normal virtual invoke
   *                     (instance methods only) "
   * @return a result object containing the results of the invocation
   */
  public static native MethodResult executeMethod (Object obj, Thread thread,
					    Class clazz, Method method,
					    Object[] values,
					    boolean nonVirtual)
    throws JdwpException;

  /**
   * "Returns the name of source file in which a reference type was declared"
   *
   * @param  clazz  the class for which to return a source file
   * @return a string containing the source file name; "no path information
   *         for the file is included"
   */
  public static native String getSourceFile (Class clazz)
    throws JdwpException;

  /**
   * Register a request from the debugger
   *
   * Virtual machines have two options. Either do nothing and allow
   * the event manager to take care of the request (useful for broadcast-type
   * events like class prepare/load/unload, thread start/end, etc.)
   * or do some internal work to set up the event notification (useful for
   * execution-related events like breakpoints, single-stepping, etc.).
   */
  public static native void registerEvent (EventRequest request)
    throws JdwpException;

  /**
   * Unregisters the given request
   *
   * @param  request  the request to unregister
   */
  public static native void unregisterEvent (EventRequest request)
    throws JdwpException;


  /**
   * Clear all events of the given kind
   *
   * @param  kind  the type of events to clear
   */
  public static native void clearEvents (byte kind)
    throws JdwpException;

  public static native void redefineClass(Class old_class, byte[] classData);
}
