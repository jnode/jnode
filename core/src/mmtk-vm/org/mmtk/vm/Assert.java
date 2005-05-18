/*
 * $Id$
 *
 * JNode.org
 * Copyright (C) 2005 JNode.org
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
 * along with this library; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */
 
package org.mmtk.vm;

import org.jnode.system.BootLog;
import org.jnode.vm.Unsafe;
import org.jnode.vm.Vm;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Assert {
    public static final boolean VERIFY_ASSERTIONS = false;

    /**
     * This method should be called whenever an error is encountered.
     * 
     * @param str
     *            A string describing the error condition.
     */
    public static void error(String str) {
        BootLog.error(str);
    }

    /**
     * Logs a message and traceback, then exits.
     * 
     * @param message
     *            the string to log
     */
    public static void fail(String message) {
        BootLog.fatal(message);
        if (Vm.isRunningVm()) {
            Unsafe.die(message);
        } else {
            throw new Error(message);
        }
    }

    /**
     * ?
     * @param rc
     */
    public static void exit(int rc) {
        // TODO Understand me
    }

    /**
     * Checks that the given condition is true. If it is not, this method does a
     * traceback and exits.
     * 
     * @param cond
     *            the condition to be checked
     */
    public static void _assert(boolean cond) {
        if (!cond) {
            Unsafe.die("Assertion failed");
        }
    }

    /**
     * <code>true</code> if assertions should be verified
     */
    public static final boolean VerifyAssertions = false;

    public static void _assert(boolean cond, String s) {
        if (!cond) {
            Unsafe.die(s);
        }
    }

    public static final void dumpStack() {
    }

    /**
     * Throw an out of memory exception.
     */
    public static void failWithOutOfMemoryError() {
        Unsafe.die("Out of memory");
    }

    /**
     * Checks if the virtual machine is running. This value changes, so the
     * call-through to the VM must be a method. In Jikes RVM, just returns
     * VM.runningVM.
     * 
     * @return <code>true</code> if the virtual machine is running
     */
    public static boolean runningVM() {
        return Vm.isRunningVm();
    }

}
