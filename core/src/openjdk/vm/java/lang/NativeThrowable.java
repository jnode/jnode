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

package java.lang;

import org.jnode.annotation.MagicPermission;
import org.jnode.vm.VmImpl;
import org.jnode.vm.VmStackFrame;
import org.jnode.vm.classmgr.VmInstanceField;
import org.jnode.vm.classmgr.VmType;
import org.jnode.vm.scheduler.VmProcessor;
import org.jnode.vm.scheduler.VmThread;
import org.vmmagic.unboxed.ObjectReference;

/**
 * @author Levente S\u00e1ntha
 * @see Throwable
 */
@MagicPermission
class NativeThrowable {
    private static int BACKTRACE_OFFSET = ((VmInstanceField) VmType.fromClass(Throwable.class).
        getField("backtrace")).getOffset();

    private static synchronized Throwable fillInStackTrace(Throwable instance) {
        ObjectReference.fromObject(instance).toAddress().add(BACKTRACE_OFFSET).
            store(ObjectReference.fromObject(VmThread.getStackTrace(VmProcessor.current().getCurrentThread())));
        return instance;
    }

    private static int getStackTraceDepth(Throwable instance) {
        return ((Object[]) ObjectReference.fromObject(instance).toAddress().add(BACKTRACE_OFFSET).
            loadObjectReference().toObject()).length;
    }

    private static StackTraceElement getStackTraceElement(Throwable instance, int index) {
        final VmStackFrame frame =
            (VmStackFrame) ((Object[]) ObjectReference.fromObject(instance).toAddress().add(BACKTRACE_OFFSET).
                loadObjectReference().toObject())[index];
        return VmImpl.getStackTraceElement(frame);
    }
}
