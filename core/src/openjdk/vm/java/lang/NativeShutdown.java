/*
 * $Id: header.txt 2224 2006-01-01 12:49:03Z epr $
 *
 * JNode.org
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
 
package java.lang;

import org.jnode.vm.isolate.VmIsolate;
import org.jnode.vm.VmExit;
import javax.isolate.Isolate;

/**
 * @author Levente S\u00e1ntha
 * @see java.lang.Shutdown
 */
class NativeShutdown {
    /**
     * @see java.lang.Shutdown#halt0(int)
     */
    private static void halt0(int status) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        if (trace.length > 0) {
            StackTraceElement elem = trace[1];
            if (Shutdown.class.getName().equals(elem.getClassName()) &&
                "exit".equals(elem.getMethodName())) {
                vmExit(status);
                //end of execution
            }
        }

        vmHalt(status);
        //end of execution
    }

    /**
     * @see java.lang.Shutdown#runAllFinalizers()
     */
    private static void runAllFinalizers() {
        //todo implement it

    }

    /**
     * Native method that actually shuts down the virtual machine.
     *
     * @param status the status to end the process with
     */
    static void vmExit(int status) {
        if (VmIsolate.getRoot() == VmIsolate.currentIsolate()) {
            throw new VmExit(status);
        } else {
            VmIsolate.currentIsolate().systemExit(Isolate.currentIsolate(), status);
        }
    }

    /**
     * Native method that actually shuts down the virtual machine.
     *
     * @param status the status to end the process with
     */
    static void vmHalt(int status) {
        if (VmIsolate.getRoot() == VmIsolate.currentIsolate()) {
            throw new VmExit(status);
        } else {
            VmIsolate.currentIsolate().systemHalt(Isolate.currentIsolate(), status);
        }
    }
}
