/* java.lang.VMThrowable -- VM support methods for Throwable.
   Copyright (C) 1998, 1999, 2002 Free Software Foundation, Inc.

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

import org.jnode.vm.VmSystem;
import org.jnode.vm.VmProcessor;
import org.jnode.vm.VmStackFrame;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;

/**
 * VM dependant state and support methods for Throwable.
 * It is deliberately package local and final and should only be accessed
 * by the Throwable class.
 * <p>
 * This is the GNU Classpath reference implementation, it should be adapted
 * for a specific VM. The reference implementation does nothing.
 *
 * @author Mark Wielaard (mark@klomp.org)
 */
final class VMThrowable
{
  /**
   * VM private data.
   */
  private transient Object vmdata;
  private Object[] backtrace;

  /**
   * Private contructor, create VMThrowables with fillInStackTrace();
   */
  private VMThrowable() { }

  /**
   * Fill in the stack trace with the current execution stack.
   * Called by <code>Throwable.fillInStackTrace()</code> to get the state of
   * the VM. Can return null when the VM does not support caputing the VM
   * execution state.
   *
   * @return a new VMThrowable containing the current execution stack trace.
   * @see Throwable#fillInStackTrace()
   */
  static VMThrowable fillInStackTrace(Throwable t)
  {
      VMThrowable vmt = new VMThrowable();
      vmt.backtrace = VmSystem.getStackTrace(VmProcessor.current().getCurrentThread());
      return vmt;

  }

  /**
   * Returns an <code>StackTraceElement</code> array based on the execution
   * state of the VM as captured by <code>fillInStackTrace</code>.
   * Called by <code>Throwable.getStackTrace()</code>.
   *
   * @return a non-null but possible zero length array of StackTraceElement.
   * @see Throwable#getStackTrace()
   */
  StackTraceElement[] getStackTrace(Throwable t)
  {
      final VmStackFrame[] vm_trace = (VmStackFrame[]) backtrace;
      final int length = vm_trace.length;
      final StackTraceElement[] trace = new StackTraceElement[length];
      for(int i = length; i-- > 0; ){
          final VmStackFrame frame = vm_trace[i];
          final String location = frame.getLocationInfo();
          final int lineNumber = "?".equals(location) ? -1 : Integer.parseInt(location);
          final VmMethod method = frame.getMethod();
          final VmType<?> vmClass = (method == null) ? null : method.getDeclaringClass();
          final String fname = (vmClass == null) ? null : vmClass.getSourceFile();
          final String cname = (vmClass == null) ? "<unknown class>" : vmClass.getName();
          final String mname = (method == null) ? "<unknown method>" : method.getName();
          trace[i] = new StackTraceElement(fname, lineNumber, cname,  mname, method.isNative());
      }
      return trace;
  }
}
