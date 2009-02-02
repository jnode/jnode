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
 
package org.jnode.vm;

import org.jnode.vm.annotation.Internal;
import org.jnode.vm.annotation.KernelSpace;
import org.jnode.vm.annotation.MagicPermission;
import org.jnode.vm.classmgr.VmCompiledCode;
import org.jnode.vm.classmgr.VmMethod;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.unboxed.Address;
import org.vmmagic.unboxed.Offset;

/**
 * Abstract class for reading information from stack frames.
 *
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
@MagicPermission
public abstract class VmStackReader extends VmSystemObject {

    /**
     * Gets the previous frame (if any)
     *
     * @param sf The stackframe to get the previous frame from.
     * @return The previous frame or null.
     */
    @KernelSpace
    @Internal
    public final Address getPrevious(Address sf) {
        if (isValid(sf)) {
            return sf.loadAddress(getPreviousOffset(sf));
        } else {
            return null;
        }
    }

    /**
     * Gets the method of a given stackframe.
     *
     * @param sf Stackframe pointer
     * @return The method
     */
    @KernelSpace
    final VmMethod getMethod(Address sf) {
        final int ccid = sf.loadInt(getMethodIdOffset(sf));
        if (ccid == 0) {
            return null;
        } else {
            final VmCompiledCode cc = Vm.getCompiledMethods().get(ccid);
            if (cc == null) {
                Unsafe.die("Unknown ccid found on stack");
                return null;
            } else {
                return cc.getMethod();
            }
        }
    }

    /**
     * Gets the compiled code of a given stackframe.
     *
     * @param sf Stackframe pointer
     * @return The compiled code
     */
    final VmCompiledCode getCompiledCode(Address sf) {
        final int ccid = sf.loadInt(getMethodIdOffset(sf));
        if (ccid == 0) {
            return null;
        } else {
            return Vm.getCompiledMethods().get(ccid);
        }
    }

    /**
     * Gets the return address of a given stackframe.
     *
     * @param sf Stackframe pointer
     * @return The address
     */
    @Internal
    public final Address getReturnAddress(Address sf) {
        return sf.loadAddress(getReturnAddressOffset(sf));
    }

    /**
     * Is a given stackframe valid?
     *
     * @param sf
     * @return boolean
     */
    @KernelSpace
    final boolean isValid(Address sf) {
        if (sf == null) {
            return false;
        }
        if (getMethod(sf) == null) {
            return false;
        }
        return true;
    }

    /**
     * Is the given frame a bottom of stack marker?
     *
     * @param sf
     * @return boolean
     */
    final boolean isStackBottom(Address sf) {
        if (sf == null) {
            return true;
        }
        return (getMethod(sf) == null)
            && (getPrevious(sf) == null);
    }

    /**
     * Gets the stacktrace for a given current frame.
     *
     * @param argFrame
     * @param ip       The instruction pointer of the given frame
     * @param limit    Maximum length of returned array.
     * @return VmStackFrame[]
     */
    @Internal
    public final VmStackFrame[] getVmStackTrace(Address frame, Address ip, int limit) {

        final VmStackFrameEnumerator sfEnum = new VmStackFrameEnumerator(this, frame, ip);
        int count = 0;
        while (sfEnum.isValid() && (count < limit)) {
            count++;
            sfEnum.next();
        }
//      if ((f != null) && !isStackBottom(f) && (count < limit)) {
//          Unsafe.debug("Corrupted stack!, st.length=");
//          Unsafe.debug(count);
//          Unsafe.debug(" f.magic=");
//          //Unsafe.die();
//      }

        final VmStackFrame[] stack = new VmStackFrame[count];
        sfEnum.reset(frame, ip);
        for (int i = 0; i < count; i++) {
            stack[i] = new VmStackFrame(sfEnum.getMethod(), sfEnum.getProgramCounter());
            sfEnum.next();
        }

        return stack;
    }

    /**
     * Count the number of stackframe from a given frame.
     *
     * @param sf
     * @return int
     */
    @Internal
    public final int countStackFrames(Address sf) {
        int count = 0;
        while (isValid(sf)) {
            count++;
            sf = getPrevious(sf);
        }
        return count;
    }

    /**
     * Show the current stacktrace using Screen.debug.
     */
    @KernelSpace
    public final void debugStackTrace() {
        debugStackTrace(25);
    }

    /**
     * Show the current stacktrace using Screen.debug.
     */
    @KernelSpace
    public final void debugStackTrace(int max) {
        Address f = VmMagic.getCurrentFrame();
        Unsafe.debug("\nDebug stacktrace: ");
        boolean first = true;
        while (isValid(f) && (max > 0)) {
            if (first) {
                first = false;
            } else {
                Unsafe.debug(", ");
            }
            final VmMethod method = getMethod(f);
            final VmType<?> vmClass = method.getDeclaringClass();
            Unsafe.debug(vmClass.getName());
            Unsafe.debug("::");
            Unsafe.debug(method.getName());
            Unsafe.debug('\n');
            f = getPrevious(f);
            max--;
        }
        if (isValid(f)) {
            Unsafe.debug("...");
        }
    }

    /**
     * Show the current stacktrace using Screen.debug.
     * TODO that method only exist to have line numbers : find a way to add line numbers to debugStackTrace(max)
     */
    @KernelSpace
    public final void debugStackTraceWithLineNumbers(int max) {
        final VmThread current = VmThread.currentThread();
        final VmStackFrame[] frames = (VmStackFrame[]) VmThread.getStackTrace(current);
        if (frames == null) {
            Unsafe.debug("Debug stacktrace:<no stack trace>\n");
        } else {
            Unsafe.debug("Debug stacktrace: ");
            for (int i = 0; i < frames.length; i++) {
                final VmStackFrame s = (VmStackFrame) frames[i];
                Unsafe.debug(s.getMethod().getDeclaringClass().getName());
                Unsafe.debug("::");
                Unsafe.debug(s.getMethod().getName());
                Unsafe.debug(":");
                Unsafe.debug(s.getLocationInfo());
                Unsafe.debug('\n');
            }
        }
    }

    /**
     * Show the stacktrace of the given thread using Screen.debug.
     */
    @KernelSpace
    public final void debugStackTrace(VmThread thread) {
        Address f = thread.getStackFrame();
        Unsafe.debug("Debug stacktrace: ");
        boolean first = true;
        int max = 20;
        while (isValid(f) && (max > 0)) {
            if (first) {
                first = false;
            } else {
                Unsafe.debug(", ");
            }
            final VmMethod method = getMethod(f);
            final VmType vmClass = method.getDeclaringClass();
            Unsafe.debug(vmClass.getName());
            Unsafe.debug("::");
            Unsafe.debug(method.getName());
            f = getPrevious(f);
            max--;
        }
        if (isValid(f)) {
            Unsafe.debug("...");
        }
    }

    /**
     * Gets the offset within the frame of previous frame (if any)
     *
     * @param sf The stackframe to get the previous frame from.
     * @return The previous frame or null.
     */
    @KernelSpace
    protected abstract Offset getPreviousOffset(Address sf);

    /**
     * Gets the offset within the stackframe of compiled method id.
     *
     * @param sf Stackframe pointer
     * @return The method id offset
     */
    @KernelSpace
    protected abstract Offset getMethodIdOffset(Address sf);

    /**
     * Gets the offset within the stackframe of the return address.
     *
     * @param sf Stackframe pointer
     * @return The return address offset
     */
    @KernelSpace
    protected abstract Offset getReturnAddressOffset(Address sf);
}
