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
 
package java.lang;

import org.jnode.vm.VmStackFrame;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmThread;

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
      vmt.backtrace = VmThread.getStackTrace(VmProcessor.current().getCurrentThread());
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
      return backTrace2stackTrace(backtrace);
  }

    static StackTraceElement[] backTrace2stackTrace(Object[] backtrace) {
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
            trace[i] = new StackTraceElement(cname, mname, fname, method == null || method.isNative() ? -2 : lineNumber);
        }
        return trace;
    }
}
